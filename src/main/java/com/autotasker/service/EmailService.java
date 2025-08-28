package com.autotasker.service;

import com.autotasker.model.EmailMessage;
import com.autotasker.util.EmailPropsUtil;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.*;

public class EmailService {

    // read mails
    public List<EmailMessage> fetchUnreadEmails() throws MessagingException, IOException {
        List<EmailMessage> messagesList = new ArrayList<>();

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");

        Session session = Session.getDefaultInstance(props);
        Store store = session.getStore("imaps");
        store.connect(EmailPropsUtil.IMAP_HOST, EmailPropsUtil.USER, EmailPropsUtil.PASS);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        // get unread
        Message[] messages = inbox.search(
                new FlagTerm(new Flags(Flags.Flag.SEEN), false)
        );

        for (Message msg : messages) {
            String from = msg.getFrom()[0].toString();
            String subject = msg.getSubject();
            String content = getTextFromMessage(msg);
            messagesList.add(new EmailMessage(from, subject, content, msg));
        }

        inbox.close(false);
        store.close();

        return messagesList;
    }

    public boolean markAsRead(Message message) {
        try {
            if (!message.isSet(Flags.Flag.SEEN))
                message.setFlag(Flags.Flag.SEEN, true);
            return true;
        } catch (MessagingException e) {
            return false;
        }
    }

    // send mail
    public void sendEmail(String to, String subject, String text) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", EmailPropsUtil.SMTP_HOST);
        props.put("mail.smtp.port", EmailPropsUtil.SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EmailPropsUtil.USER, EmailPropsUtil.PASS);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EmailPropsUtil.USER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(text);

        Transport.send(message);
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            return Jsoup.parse(message.getContent().toString()).text();
            // clean HTML and returns only the text
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                if (bodyPart.isMimeType("text/plain")) {
                    return bodyPart.getContent().toString();
                } else if (bodyPart.isMimeType("text/html")) {
                    return Jsoup.parse(bodyPart.getContent().toString()).text().toString();
                }
            }
        }
        return "";
    }
}
