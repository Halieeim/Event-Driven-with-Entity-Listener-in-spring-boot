package com.portal.service.impl.notifications;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.portal.model.event.NotificationCreatedEvent;
import com.portal.repository.NotificationRepository;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class FirebaseService {
	@Autowired
	private FirebaseMessaging firebaseMessaging;
	
	@Autowired
	private NotificationRepository notificationRepository;
	
	@Autowired
	WebClient.Builder webClientBuilder;
	
	public String subscribeToTopic(String topic, String recipientToken) {
		try {
			firebaseMessaging.subscribeToTopic(List.of(recipientToken), topic);
			return "User subscribed to topic successfully.";
		} catch (FirebaseMessagingException fme){
			fme.printStackTrace();
			return "An error occurred!!! Cannot subscribe to this topic.";
		}
	}
	
	public String unsubscribeToTopic(String topic, String recipientToken) {
		try {
			firebaseMessaging.unsubscribeFromTopic(List.of(recipientToken), topic);
			return "User unsubscribed from topic successfully.";
		} catch (FirebaseMessagingException fme){
			fme.printStackTrace();
			return "An error occurred!!! Cannot unsubscribe from this topic.";
		}
	}
	
	public Boolean sendNotificationByToken(com.portal.model.Notification notificationMessage, String recipientToken) {
		Notification notification = Notification.builder()
				.setTitle(notificationMessage.getTitle())
				.setBody(notificationMessage.getDescription())
				.build();
		
		Message message = Message.builder()
				.setToken(recipientToken)
				.setNotification(notification)
//				.putAllData(notificationMessage.getData())
				.build();
		
		try {
			firebaseMessaging.send(message);
			return true;
		} catch(FirebaseMessagingException fme) {
			fme.printStackTrace();
			return false;
		}
	}
	
	public Boolean sendNotificationByTopic(com.portal.model.Notification notificationMessage, String topic) {
		Notification notification = Notification.builder()
				.setTitle(notificationMessage.getTitle())
				.setBody(notificationMessage.getDescription())
				.build();
		
		Message message = Message.builder()
				.setTopic(topic)
				.setNotification(notification)
//				.putAllData(notificationMessage.getData())
				.build();
		
		try {
			firebaseMessaging.send(message);
			return true;
		} catch(FirebaseMessagingException fme) {
			fme.printStackTrace();
			return false;
		}
	}
	
//	@Scheduled(fixedRate = 5000)
	@EventListener
	public String pushNotifications(NotificationCreatedEvent event) {
		System.out.println("Notification Title = " + event.getNotification().getTitle());
		if(event != null) {
			com.portal.model.Notification unpublishedNotification = event.getNotification();
			if(unpublishedNotification.getUserId() == -1) {
				return pushNotificationToAllUsers(unpublishedNotification) ? "Notification has been pushed successfully." : "An error occurred!!! Failed to push a notification.";
			} else {
				return pushNotificationToUser(unpublishedNotification) ? "Notification has been pushed successfully." : "An error occurred!!! Failed to push a notification.";
			}
		}
		
		// work arround for about to end subs job problem
		Boolean flag = true;
		List<com.portal.model.Notification> allUnpublishedNotifications = notificationRepository.findUnpublishedNotifications();
		for(com.portal.model.Notification not: allUnpublishedNotifications) {
			if(not.getUserId() == -1) flag = flag && pushNotificationToAllUsers(not);
			else flag = flag && pushNotificationToUser(not);
		}
		return flag ? "Notifications have been pushed successfully." : "An error occurred!!! Failed to push one or more notifications.";
//		using fixed rate = 5000
//		List<com.portal.model.Notification> allUnpublishedNotifications = notificationRepository.findUnpublishedNotifications();
//		for(com.portal.model.Notification unpublishedNotification: allUnpublishedNotifications) {
//			if(unpublishedNotification.getUserId() == -1) pushNotificationToAllUsers(unpublishedNotification);
//			else pushNotificationToUser(unpublishedNotification);
//		}
	}
	
	private Boolean pushNotificationToAllUsers(com.portal.model.Notification unpublishedNotification) {
		return false;
	}
	
	private Boolean pushNotificationToUser(com.portal.model.Notification unpublishedNotification) {
		// get the fire-base token of this user
		JsonNode response = webClientBuilder.build().get()
		        .uri("lb://auth/auth/user/getUser/{userId}", unpublishedNotification.getUserId())
		        .retrieve()
		        .bodyToMono(JsonNode.class)
		        .block();
		String recipientToken = response.get("data").get("firebaseToken").asText();
		System.out.println("recipientToken = " + recipientToken + " , userId = " + unpublishedNotification.getUserId());
		// send notification to it
		if(sendNotificationByToken(unpublishedNotification, recipientToken)) {
			// if succeeded update isPublished flag to be true
			unpublishedNotification.setIsPublished(true);
			notificationRepository.save(unpublishedNotification);
			return true;
		}
		return false;
	}
}
