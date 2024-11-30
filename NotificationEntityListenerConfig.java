package com.portal.service.impl.notifications;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.portal.model.event.NotificationEntityListener;

@Component
public class NotificationEntityListenerConfig {
	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @PostConstruct
    public void init() {
        NotificationEntityListener.setApplicationEventPublisher(applicationEventPublisher);
    }
}
