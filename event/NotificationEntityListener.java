package com.portal.model.event;

import javax.persistence.PostPersist;

import org.springframework.context.ApplicationEventPublisher;

import com.portal.model.Notification;

public class NotificationEntityListener {
	private static ApplicationEventPublisher eventPublisher;

    public static void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        eventPublisher = publisher;
    }

    @PostPersist
    public void afterInsert(Notification notification) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new NotificationCreatedEvent(notification));
        }
    }
}
