package com.autotasker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "emails")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_id")
    private long emailId;

    @Column(nullable = false)
    private String email;

    @OneToOne
    @JoinColumn(name = "owner", nullable = false)
    private User owner;

    public Email() {}

    public Email(long emailId, String email, User owner) {
        this.emailId = emailId;
        this.email = email;
        this.owner = owner;
    }

    public long getEmailId() {
        return emailId;
    }

    public void setEmailId(long emailId) {
        this.emailId = emailId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
