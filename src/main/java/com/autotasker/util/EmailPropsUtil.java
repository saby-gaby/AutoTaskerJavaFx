package com.autotasker.util;

import com.autotasker.service.EmailService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class EmailPropsUtil {
    private static final Properties properties = new Properties();

    static {
        try (InputStream in = EmailService.class.getClassLoader().getResourceAsStream("email.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // get full email address
    public static final String USER = getEnvOrProp("MAIL_USER", "mail.user")
            .orElseThrow(() -> new IllegalStateException("MAIL_USER is not set."));
    // get app password
    public static final String PASS = getEnvOrProp("MAIL_PASS", "mail.pass")
            .orElseThrow(() -> new IllegalStateException("MAIL_PASS is not set."));
    // get the address for incoming mail (imap.gmail.com)
    public static final String IMAP_HOST = getEnvOrProp("MAIL_IMAP_HOST" , "mail.imap.host")
            .orElseThrow(() -> new IllegalStateException("MAIL_IMAP_HOST is not set."));
    // get the address for outgoing mail (smtp.gmail.com)
    public static final String SMTP_HOST = getEnvOrProp("MAIL_SMTP_HOST", "mail.smtp.host")
            .orElseThrow(() -> new IllegalStateException("MAIL_SMTP_HOST is not set."));
    // get the port for SMTP with encryption (587 for Gmail)
    public static final int SMTP_PORT = Integer.parseInt(getEnvOrProp("MAIL_SMTP_PORT", "mail.smtp.port").orElse("587"));


    private static Optional<String> getEnvOrProp(String envKey, String propKey) {
        String value = System.getenv(envKey);
        if (value != null && !value.isBlank()) return Optional.of(value);

        value = properties.getProperty(propKey);
        if (value != null && !value.isBlank()) return Optional.of(value);

        return Optional.empty();
    }

    private EmailPropsUtil() {}
}
