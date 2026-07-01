
package com.todo.app.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final EmailService emailService;
    private final WebPushService webPushService;

    public NotificationService(EmailService emailService, WebPushService webPushService) {
        this.emailService = emailService;
        this.webPushService = webPushService;
    }

    public void email(String email, String msg) {
        System.out.println("EMAIL: " + msg);
        emailService.sendEmail(email, "Notification", msg);
    }

    public void push(String email, String title, String body, String imageUrl, String link) {
        webPushService.notifyUserByEmail(email, title, body, link);
    }

    public void sms(String msg) {
        System.out.println("SMS: " + msg);
    }
}
