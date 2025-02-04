/*
 * Copyright 2003-2009 the original author or authors.
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
package com.jdon.jivejdon.event.domain.producer.write;

import com.jdon.annotation.Component;
import com.jdon.annotation.Introduce;
import com.jdon.annotation.model.Send;
import com.jdon.domain.message.DomainMessage;
import com.jdon.jivejdon.model.dci.ThreadEventSourcingRole;
import com.jdon.jivejdon.model.event.MessageRemoveCommand;
import com.jdon.jivejdon.model.event.MessageRemovedEvent;
import com.jdon.jivejdon.model.event.PostTopicMessageCommand;
import com.jdon.jivejdon.model.event.TopicMessagePostedEvent;
import com.jdon.jivejdon.model.util.OneOneDTO;

@Component
@Introduce("message")
public class ThreadEventSourcingRoleImpl implements ThreadEventSourcingRole {

	@Send("postTopicMessageCommand")
	public DomainMessage postTopicMessage(PostTopicMessageCommand command) {
		return new DomainMessage(command);
	}


	@Send("topicMessagePostedEvent")
	public DomainMessage topicMessagePosted(TopicMessagePostedEvent topicMessagePostedEvent) {
		return new DomainMessage(topicMessagePostedEvent);
	}


	@Override
	@Send("postReBlog")
	public DomainMessage postReBlog(OneOneDTO oneOneDTO) {
		return new DomainMessage(oneOneDTO);
	}

	@Override
	@Send("deleteMessage")
	public DomainMessage deleteMessage(MessageRemoveCommand event) {
		return new DomainMessage(event);
	}

	@Override
	@Send("postThread")
	public DomainMessage postThread(DomainMessage domainMessage) {
		return new DomainMessage(domainMessage);
	}

	@Send("delThread")
	public DomainMessage delThread(MessageRemovedEvent event) {
		return new DomainMessage(event);
	}

}
