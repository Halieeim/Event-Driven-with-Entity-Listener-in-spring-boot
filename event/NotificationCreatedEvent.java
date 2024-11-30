package com.portal.model.event;

import com.portal.model.Notification;

public class NotificationCreatedEvent {
	private final Notification notification;

    public NotificationCreatedEvent(Notification notification) {
        this.notification = notification;
    }

    public Notification getNotification() {
        return notification;
    }
}
