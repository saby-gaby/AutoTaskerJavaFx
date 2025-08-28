package com.autotasker.service;

import com.autotasker.model.EmailMessage;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EmailScheduler {
    private final EmailService emailService;
    private final long intervalMillis;

    public EmailScheduler(EmailService emailService, long intervalMillis) {
        this.emailService = emailService;
        this.intervalMillis = intervalMillis;
    }

    public void start() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    List<EmailMessage> newEmails = emailService.fetchUnreadEmails();
//                    List<EmailMessage> newEmails = emailService.fetchUnreadEmails();
                    if (!newEmails.isEmpty()) {
                        System.out.println("New mails: " + newEmails.size());
                        newEmails.forEach(msg ->
                                System.out.println(msg.getSubject() + " from " + msg.getFrom()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, intervalMillis);
    }
}
