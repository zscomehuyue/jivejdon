/*
 * Copyright 2003-2005 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.jdon.jivejdon.service.imp.message;

import com.jdon.annotation.Service;
import com.jdon.annotation.intercept.Poolable;
import com.jdon.annotation.intercept.SessionContextAcceptable;
import com.jdon.container.visitor.data.SessionContext;
import com.jdon.controller.events.EventModel;
import com.jdon.jivejdon.Constants;
import com.jdon.jivejdon.auth.NoPermissionException;
import com.jdon.jivejdon.auth.ResourceAuthorization;
import com.jdon.jivejdon.manager.filter.InFilterManager;
import com.jdon.jivejdon.model.*;
import com.jdon.jivejdon.model.attachment.AttachmentsVO;
import com.jdon.jivejdon.model.dci.ThreadManagerContext;
import com.jdon.jivejdon.model.message.AnemicMessageDTO;
import com.jdon.jivejdon.model.message.output.RenderingFilterManager;
import com.jdon.jivejdon.model.proptery.MessagePropertysVO;
import com.jdon.jivejdon.repository.ForumFactory;
import com.jdon.jivejdon.service.ForumMessageService;
import com.jdon.jivejdon.service.UploadService;
import com.jdon.jivejdon.service.util.SessionContextUtil;
import com.jdon.util.UtilValidate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * ForumMessageShell is the shell of ForumMessage core implementions.
 * 
 * invoking order: ForumMessageShell --> MessageContentFilter --->
 * ForumMessageServiceImp
 * 
 * @author <a href="mailto:banq@163.com">banq</a>
 * 
 */
@Poolable
@Service("forumMessageService")
public class ForumMessageShell implements ForumMessageService {
	private final static Logger logger = LogManager.getLogger(ForumMessageShell.class);
	protected final InFilterManager inFilterManager;
	protected final SessionContextUtil sessionContextUtil;
	protected final ResourceAuthorization resourceAuthorization;
	protected final MessageKernelIF messageKernel;
	protected final UploadService uploadService;
	protected final ForumFactory forumBuilder;
	private final ThreadManagerContext threadManagerContext;
	private final RenderingFilterManager renderingFilterManager;
	protected SessionContext sessionContext;

	public ForumMessageShell(SessionContextUtil sessionContextUtil, ResourceAuthorization messageServiceAuth, InFilterManager inFilterManager,
							 MessageKernelIF messageKernel, UploadService uploadService, ForumFactory forumBuilder, RenderingFilterManager renderingFilterManager,
			ThreadManagerContext threadManagerContext) {
		this.sessionContextUtil = sessionContextUtil;
		this.resourceAuthorization = messageServiceAuth;
		this.inFilterManager = inFilterManager;
		this.renderingFilterManager = renderingFilterManager;
		this.messageKernel = messageKernel;
		this.uploadService = uploadService;
		this.forumBuilder = forumBuilder;
		this.threadManagerContext = threadManagerContext;
	}

	/**
	 * must be often checked, every request, if use login.
	 * 
	 * @param forumMessage
	 */
	public boolean checkIsAuthenticated(ForumMessage forumMessage) {
		boolean allowEdit = false;
		if (forumMessage == null) return allowEdit;
		if (!forumMessage.isSolid())
			return allowEdit;// forumMessage is null or only has mesageId
		com.jdon.jivejdon.model.Account account = sessionContextUtil.getLoginAccount(sessionContext);
		allowEdit = resourceAuthorization.isAuthenticated(forumMessage, account);
		return allowEdit;
	}

	/**
	 * create Topic Message
	 */
	public Long createTopicMessage(EventModel em) throws Exception {
        AnemicMessageDTO forumMessagePostDTO = (AnemicMessageDTO) em.getModelIF();
		if (!prepareCreate(forumMessagePostDTO))
			return null;
		Forum forum = forumBuilder.getForum(forumMessagePostDTO.getForum().getForumId());
		if (forum == null)
			return null;
		Long mIDInt = forumBuilder.getNextId(Constants.MESSAGE);
		try {
			forumMessagePostDTO.setMessageId(mIDInt);
			forumMessagePostDTO.setForum(forum);
			// upload
			Collection uploads = uploadService.loadAllUploadFilesOfMessage(mIDInt, sessionContext);
			AttachmentsVO attachmentsVO = new AttachmentsVO(mIDInt, uploads);
			forumMessagePostDTO.setAttachment(attachmentsVO);

			Account operator = sessionContextUtil.getLoginAccount(sessionContext);
			// forumMessagePostDTO.setOperator(operator);

			Collection properties = new ArrayList();
			properties.add(new Property(MessagePropertysVO.PROPERTY_IP, operator.getPostIP()));
			MessagePropertysVO messagePropertysVO = new MessagePropertysVO(mIDInt, properties);
			forumMessagePostDTO.setMessagePropertysVO(messagePropertysVO);

			forumMessagePostDTO.setMessageVO(inFilterManager.applyFilters
					(forumMessagePostDTO.getMessageVO()));

			if (!UtilValidate.isEmpty(forumMessagePostDTO.getMessageVO().getBody()) ||
					!UtilValidate.isEmpty(forumMessagePostDTO.getMessageVO().getSubject())) {
				messageKernel.post(forum.getForumId(), forum, forumMessagePostDTO);
			}

		} catch (Exception e) {
			logger.error(e);
			em.setErrors(Constants.ERRORS);
		} finally {
			uploadService.clearSession(sessionContext);
		}
		return mIDInt;
	}

	/**
	 * set the login account into the domain model
	 */
	public Long createReplyMessage(EventModel em) throws Exception {
        AnemicMessageDTO forumMessageReplyPostDTO = (AnemicMessageDTO) em.getModelIF();
		if (UtilValidate.isEmpty(forumMessageReplyPostDTO.getMessageVO().getBody()))
			return null;
		if (!prepareCreate(forumMessageReplyPostDTO))
			return null;
		Long mIDInt = this.forumBuilder.getNextId(Constants.MESSAGE);
		try {

			forumMessageReplyPostDTO.setMessageId(mIDInt);

			Collection uploads = uploadService.loadAllUploadFilesOfMessage(mIDInt, sessionContext);
			AttachmentsVO attachmentsVO = new AttachmentsVO(mIDInt, uploads);
			forumMessageReplyPostDTO.setAttachment(attachmentsVO);

			Account operator = sessionContextUtil.getLoginAccount(sessionContext);
			forumMessageReplyPostDTO.setOperator(operator);
            forumMessageReplyPostDTO.setAccount(operator);

			Collection properties = new ArrayList();
			properties.add(new Property(MessagePropertysVO.PROPERTY_IP, operator.getPostIP()));
			MessagePropertysVO messagePropertysVO = new MessagePropertysVO(mIDInt, properties);
			forumMessageReplyPostDTO.setMessagePropertysVO(messagePropertysVO);

			forumMessageReplyPostDTO.setMessageVO(inFilterManager.applyFilters
					(forumMessageReplyPostDTO.getMessageVO()));

			ForumMessage parentMessage = getMessage(forumMessageReplyPostDTO.getParentMessage().getMessageId());
			if (parentMessage == null) {
				logger.error("not this parent Message: " + forumMessageReplyPostDTO.getParentMessage().getMessageId());
				return null;
			}
			messageKernel.addreply(parentMessage.getForumThread().getThreadId(), parentMessage, forumMessageReplyPostDTO);

		} catch (Exception e) {
			logger.error(e);
			em.setErrors(Constants.ERRORS);
		} finally {
			uploadService.clearSession(sessionContext);
		}
		return mIDInt;
	}

	private boolean prepareCreate(AnemicMessageDTO forumMessage) throws Exception {
		// the poster
		com.jdon.jivejdon.model.Account account = sessionContextUtil.getLoginAccount(sessionContext);
		if (account == null)
			return false;
		forumMessage.setAccount(account);
		return true;
	}

	// reblog retweet
	public void reBlog(Long replyMessageId, Long topicMessageId) throws Exception {

		threadManagerContext.postReBlog(replyMessageId, topicMessageId);
	}

	public void associateThread(EventModel em) throws Exception {

	}

	private boolean isAuthenticated(ForumMessage forumMessage) {
		try {
			com.jdon.jivejdon.model.Account account = sessionContextUtil.getLoginAccount(sessionContext);
			resourceAuthorization.verifyAuthenticated(forumMessage, account);
			return true;
		} catch (NoPermissionException e) {
			logger.error("No Permission to operate it mesageId=" + forumMessage.getMessageId());
		}
		return false;
	}

	/**
	 * 1. auth check: amdin and the owner can modify this nessage. 2. if the
	 * message has childern, only admin can update it. before business logic, we
	 * must get a true message from persistence layer, now the ForumMessage
	 * packed in EventModel object is not full, it is a DTO from prensentation
	 * layer.
	 * 
	 * 
	 */
	public void updateMessage(EventModel em) throws Exception {
        AnemicMessageDTO newForumMessageInputparamter = (AnemicMessageDTO) em.getModelIF();
		try {
			ForumMessage oldforumMessage = messageKernel.getMessage(newForumMessageInputparamter.getMessageId());
			if (oldforumMessage == null)
				return;
			if (!isAuthenticated(oldforumMessage)) {
				em.setErrors(Constants.NOPERMISSIONS);
				return;
			}
			Account operator = sessionContextUtil.getLoginAccount(sessionContext);
			newForumMessageInputparamter.setOperator(operator);

			Collection uploads = uploadService.loadAllUploadFilesOfMessage(oldforumMessage.getMessageId(), this.sessionContext);
			AttachmentsVO attachmentsVO = new AttachmentsVO(newForumMessageInputparamter.getMessageId(), uploads);
			newForumMessageInputparamter.setAttachment(attachmentsVO);

			Collection properties = new ArrayList();
			properties.add(new Property(MessagePropertysVO.PROPERTY_IP, operator.getPostIP()));
			MessagePropertysVO messagePropertysVO = new MessagePropertysVO(newForumMessageInputparamter.getMessageId(), properties);
			newForumMessageInputparamter.setMessagePropertysVO(messagePropertysVO);

			newForumMessageInputparamter.setMessageVO(inFilterManager.applyFilters
					(newForumMessageInputparamter.getMessageVO()));
			// oldforumMessage.update(newForumMessageInputparamter);
			messageKernel.update(oldforumMessage.getForumThread().getThreadId(), oldforumMessage, newForumMessageInputparamter);

		} catch (Exception e) {
			logger.error(e);
			em.setErrors(Constants.ERRORS);
		} finally {
			uploadService.clearSession(sessionContext);
		}
	}

	public void updateThreadName(Long threadId, String name) throws Exception{
		Optional<ForumThread> forumThreadOptional = messageKernel.getThread(threadId);
		if (!forumThreadOptional.isPresent())
			return;
		if (!isAuthenticated(forumThreadOptional.get().getRootMessage())) {
			return;
		}
		forumThreadOptional.get().updateName(name);
	}

	/**
	 * delete a message the auth is same as to the updateMessage
	 * 
	 * 
	 */
	public void deleteMessage(EventModel em) throws Exception {
		AnemicMessageDTO anemicMessageDTO = (AnemicMessageDTO) em.getModelIF();
		ForumMessage forumMessage = messageKernel.getMessage(anemicMessageDTO.getMessageId
				());
		if (forumMessage == null)
			return;
		if (!isAuthenticated(forumMessage)) {
			em.setErrors(Constants.NOPERMISSIONS);
			return;
		}
//		em.setModelIF(forumMessage);
		try {
			messageKernel.deleteMessage(anemicMessageDTO);

		} catch (Exception e) {
			em.setErrors(Constants.ERRORS);
			logger.error(e);
		}
	}

	public void deleteUserMessages(String username) throws Exception {
		try {
			com.jdon.jivejdon.model.Account account = sessionContextUtil.getLoginAccount(sessionContext);
			if (resourceAuthorization.isAdmin(account))
				messageKernel.deleteUserMessages(username);
		} catch (Exception e) {
			logger.error(e);
			throw new Exception(e);

		}
	}

	/**
	 * only admin or moderator can mask a message
	 */
	public void maskMessage(EventModel em) throws Exception {
		logger.debug("enter maskMessage");
		com.jdon.jivejdon.model.Account account = sessionContextUtil.getLoginAccount(sessionContext);
		if (!resourceAuthorization.isAdmin(account))
			return;

		ForumMessage forumMessageParamter = (ForumMessage) em.getModelIF();
		// masked value is from messageForm and from
		// /message/messageMaskAction.shtml?method=maskMessage&masked=false
		boolean masked;
		try {
			masked = forumMessageParamter.isMasked();
			ForumMessage forumMessage = messageKernel.getMessage(forumMessageParamter.getMessageId());
			if (forumMessage == null) {
				logger.error("the message don't existed!");
				return;
			}
			forumMessage.updateMasked(masked);
		} catch (Exception e) {
			logger.error(e);
		}

	}

	/**
	 * get the full forum in forumMessage, and return it.
	 */
	public AnemicMessageDTO initMessage(EventModel em) {
		return messageKernel.initMessage(em);
	}

	public AnemicMessageDTO initReplyMessage(EventModel em) {
		return messageKernel.initReplyMessage(em);
	}

	/*
	 * return message with body filter to client; return a full ForumMessage
	 * need solve the relations with Forum ForumThread
	 */
	public ForumMessage getMessage(Long messageId) {
		if (messageId == null)
			return null;
		return messageKernel.getMessage(messageId);
	}

	/**
	 * find a Message for modify
	 */
	public AnemicMessageDTO findMessage(Long messageId) {
		if (messageId == null)
			return null;

		logger.debug("enter ForumMessageShell's findMessage");
		ForumMessage message = messageKernel.getMessage(messageId);
		if (message == null)
			return null;
		try {
			ForumMessage newMessage = (ForumMessage) message.clone();
			newMessage.reloadMessageVOOrignal();
			AnemicMessageDTO anemicMessageDTO = new AnemicMessageDTO();
			anemicMessageDTO.setMessageId(newMessage.getMessageId());
			anemicMessageDTO.setForum(newMessage.getForum());
			anemicMessageDTO.setAccount(newMessage.getAccount());
			anemicMessageDTO.setMessageVO(newMessage.getMessageVO());
			anemicMessageDTO.setForumThread(newMessage.getForumThread());

			// after update must enable it see updateMessage;
			return anemicMessageDTO;
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}


	/**
	 * return a full ForumThread one ForumThread has one rootMessage need solve
	 * the realtion with Forum rootForumMessage lastPost
	 * 
	 * @param threadId
	 * @return
	 */
	public ForumThread getThread(Long threadId) {
		return messageKernel.getThread(threadId).orElse(null);
	}

	public RenderingFilterManager getFilterManager() {
		return this.renderingFilterManager;
	}

	/**
	 * @return Returns the sessionContext.
	 */
	public SessionContext getSessionContext() {
		return sessionContext;
	}

	/**
	 * @param sessionContext
	 *            The sessionContext to set.
	 */
	@SessionContextAcceptable
	public void setSessionContext(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}

}
