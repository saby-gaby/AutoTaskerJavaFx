package com.autotasker.service;

import com.autotasker.model.EmailMessage;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// not used because Timer may block if a task throws an exception or is delayed
// decided for ScheduledExecutorService because works with threads and is more flexible
// (for example, you can stop the scheduler, add other tasks, etc.)
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
                    // Logic with newEmails-list
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, intervalMillis);
    }
}
// usage: in EmailBackgroundMain
//        EmailService emailService = new EmailService();
//        every 60 secs
//        EmailScheduler scheduler = new EmailScheduler(emailService, 60_000);
//        scheduler.start();