package com.autotasker.model;

import jakarta.persistence.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")  // table-name in db
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto-generate ID
    @Column(name = "task_id")
    private int id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed")
    private boolean completed;
    @Column(name = "in_progress")
    private boolean inProgress;

    public Task() {
    }

    // constructor
    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.completed = false;
    }

    // getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public StringProperty nameProperty() {
        return new SimpleStringProperty(name);
    }

    public StringProperty descriptionProperty() {
        return new SimpleStringProperty(description);
    }

    public StringProperty dueDateProperty() {
        return new SimpleStringProperty(dueDate != null ? dueDate.toString() : "no date");
    }

    public BooleanProperty completedProperty() {
        return new SimpleBooleanProperty(completed);
    }

    public BooleanProperty inProgressProperty() {
        return new SimpleBooleanProperty(inProgress);
    }
}
