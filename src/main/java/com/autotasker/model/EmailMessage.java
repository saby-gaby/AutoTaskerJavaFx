package com.autotasker.model;

import jakarta.mail.Message;

public class EmailMessage {
    private final String from;
    private final String subject;
    private final String content;
    // adding raw message so it can be marked as seen later
    private final transient Message rawMessage;

    public EmailMessage(String from, String subject, String content, Message rawMessage) {
        this.from = from;
        this.subject = subject;
        this.content = content;
        this.rawMessage = rawMessage;
    }

    public String getFrom() { return from; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public Message getRawMessage() { return rawMessage; }
}
