package com.jdon.jivejdon.event.domain.consumer.read;

import com.jdon.annotation.Consumer;
import com.jdon.async.disruptor.EventDisruptor;
import com.jdon.domain.message.DomainEventHandler;
import com.jdon.jivejdon.model.ForumMessage;
import com.jdon.jivejdon.model.util.OneOneDTO;
import com.jdon.jivejdon.repository.ForumFactory;
import com.jdon.jivejdon.repository.dao.ForumDao;
import com.jdon.jivejdon.repository.dao.MessageQueryDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Consumer("loadForumState")
public class ForumStateLoader implements DomainEventHandler {
    private final static Logger logger = LogManager.getLogger(ThreadStateLoader.class);

    private final MessageQueryDao messageQueryDao;
    private final ForumDao forumDao;

    protected final ForumFactory forumAbstractFactory;

    public ForumStateLoader(MessageQueryDao messageQueryDao, ForumFactory forumAbstractFactory, ForumDao forumDao) {
        super();
        this.messageQueryDao = messageQueryDao;
        this.forumAbstractFactory = forumAbstractFactory;
        this.forumDao = forumDao;
    }

    public void onEvent(EventDisruptor event, boolean endOfBatch) throws Exception {
        try {
            long forumId = (Long) event.getDomainMessage().getEventSource();

            Long lastMessageId = messageQueryDao.getForumLastPostMessageId(forumId);
            if (lastMessageId == null) {
                logger.warn("maybe first running, not found lastMessageId for forumId: " + forumId);
                return;
            }
            ForumMessage lastPost = forumAbstractFactory.getMessage(lastMessageId);

            long messagereplyCount;
            long messageCount = messageQueryDao.getForumMessageCount(forumId);
            if (messageCount >= 1)
                messagereplyCount = messageCount - 1;
            else
                messagereplyCount = messageCount;

            OneOneDTO oneOneDTO = new OneOneDTO(lastPost, messagereplyCount);

            long threadeCount  = forumDao.getThreadCount(forumId);
            OneOneDTO oneOneDTO2 = new OneOneDTO(oneOneDTO, threadeCount);
            event.getDomainMessage().setEventResult(oneOneDTO2);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
