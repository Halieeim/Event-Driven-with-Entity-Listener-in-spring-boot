package com.portal.web.controller.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.service.impl.notifications.FirebaseService;
import com.portal.service.response.SuccessResponse;

@RestController
@RequestMapping("/fcm")
public class FCMController {
	@Autowired
	private FirebaseService firebaseService;
	
	@PostMapping("/subscribe-to-topic/{topic}/{recipientToken}")
	public SuccessResponse<String> subscribeToTopic(@PathVariable String topic, @PathVariable String recipientToken) {
		return new SuccessResponse<>(firebaseService.subscribeToTopic(topic, recipientToken));
	}
	
	@PostMapping("/push-notifications")
	public SuccessResponse<String> pushNotifications(){
		return new SuccessResponse<>(firebaseService.pushNotifications(null));
	}
}
