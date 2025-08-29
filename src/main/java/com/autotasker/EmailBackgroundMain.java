package com.autotasker;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.EmailDAO;
import com.autotasker.dao.TaskDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.EmailMessage;
import com.autotasker.service.EmailService;
import com.autotasker.model.Department;
import com.autotasker.model.Task;
import com.autotasker.model.User;
import com.autotasker.service.FuzzyTextExtractor;
import com.autotasker.util.ConvertDateUtil;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmailBackgroundMain {
    private static final EmailService E_SERVICE = new EmailService();
    private static final EmailDAO EMAIL_DAO = new EmailDAO();
    private static final TaskDAO TASK_DAO = new TaskDAO();
    private static final UserDAO USER_DAO = new UserDAO();
    private static final DepartmentDAO DEPARTMENT_DAO = new DepartmentDAO();

    public static void main(String[] args) {
        // don't want to close it -> so not in try with resource
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            List<EmailMessage> messages;
            try {
                messages = E_SERVICE.fetchUnreadEmails();
                // if no new messages -> stop
                if (messages.isEmpty()) return;

                for (EmailMessage msg : messages) {
                    handleMessage(msg);
                }
            } catch (IOException | MessagingException e) {
                e.printStackTrace();
            }
        };

        // runs every 2 min
        scheduler.scheduleAtFixedRate(task, 0, 2, TimeUnit.MINUTES);
    }

    private static void handleMessage(EmailMessage msg) {
        String from = msg.getFrom();
        boolean isEmailInDB = EMAIL_DAO.isEmailPresent(from.substring(from.indexOf("<") + 1, from.indexOf(">")));

        // if the sender is not in the DB -> ignore and mark "seen"
        if (!isEmailInDB) {
            E_SERVICE.markAsRead(msg.getRawMessage());
            return;
        }
        System.out.println(msg.getContent());

        // if empty email-body or subject
        if (msg.getContent().trim().isEmpty() || msg.getSubject() == null) {
            E_SERVICE.markAsRead(msg.getRawMessage());
            sendTaskCreateInstructionsEmail(msg.getFrom());
            return;
        }

        // if task in subject -> data in content expected
        if (FuzzyTextExtractor.checkIfContainsKeyword(
                msg.getSubject(),
                new String[]{"task", "aufgabe"})) {
            Task newTask = createTask(msg, msg.getFrom());
            if (newTask != null) {
                boolean isTaskSaved = TASK_DAO.addTask(newTask);
                if (isTaskSaved) {
                    if (E_SERVICE.markAsRead(msg.getRawMessage())) {
                        sendConfirmationEmail(newTask, msg.getFrom());
                    }
                }
            }
        }
    }

    private static Task createTask(EmailMessage msg, String from) {
        Task task = new Task();
        Map<String, String> extractedData = FuzzyTextExtractor.extractSections(msg.getContent());
        if (!extractedData.isEmpty()) {

            if (!extractedData.containsKey("name")) {
                return null;
            }
            task.setName(extractedData.get("name"));
            task.setCreatedAt(LocalDateTime.now());
            if (extractedData.containsKey("description")) task.setDescription(extractedData.get("description"));
            if (extractedData.containsKey("dueDate")) {
                String dueDateStr = extractedData.get("dueDate");
                LocalDate date = ConvertDateUtil.convertDate(dueDateStr);
                if (date != null) {
                    task.setDueDate(date);
                }
            }
            if (extractedData.containsKey("user")) {
                User user = USER_DAO.findByUsername(extractedData.get("user"));
                task.setAssignedUser(user);
                task.setAssignedDepartment(user.getDepartment());
            }
            if (extractedData.containsKey("department")) {
                List<String> allDepartments = DEPARTMENT_DAO.findAllDepartmentNames();
                int index = FuzzyTextExtractor.findBestMatchIndexInList(allDepartments, extractedData.get("department"));
                if (index != -1) {
                    Department department = DEPARTMENT_DAO.findByName(extractedData.get("department"));
                    if (task.getAssignedDepartment() != null && !task.getAssignedDepartment().equals(department)) {
                        String x = from.substring(from.indexOf("<") + 1, from.indexOf(">"));
                        if (E_SERVICE.markAsRead(msg.getRawMessage())) {
                            sendInvalidDataEmail(x, extractedData, msg);
                            return null;
                        }

                    }
                    task.setAssignedDepartment(department);
                }
            }
            return task;
        }
        return null;
    }

    private static void sendTaskCreateInstructionsEmail(String to) {
        String subject = "Create task? - Information incl. Sample Format Email";
        String user = to.substring(0, to.indexOf("<"));
        if (user.isEmpty()) user = to;
        String text = "Hello " + user + ",\r\n" +
                "if you want to create a new task:\r\n\r\n" +
                "1. Create a new email with the subject that contains \"task\".\r\n" +
                "2. Copy the following format into the new email:\r\n \r\n" +
                "       name: sample task name*\r\n" +
                "       description: task description\r\n" +
                "       due date: DD-MM-YYYY\r\n" +
                "       user: the username to whom the task should be assigned\r\n" +
                "       department: the name of the department that will be responsible for this task\r\n\r\n" +
                "3. Replace the sample information with the information about your task\r\n" +
                "  - The minimum required information is the name of the task plus at least one other field\r\n" +
                "  - If a user is specified, it is not recommended to specify a department\r\n" +
                "4. Submit the final email to: autotasker2025@gmail.com\r\n" +
                "5. Shortly after that you will receive a confirmation email\r\n\r\n" +
                "Best regards\r\n" +
                "Your AutoTasker2025";
        try {
            E_SERVICE.sendEmail(to.substring(to.indexOf('<'), to.indexOf('>') + 1), subject, text);
        } catch (MessagingException ignored) {
        }
    }

    private static void sendConfirmationEmail(Task task, String to) {
        String subject = "New task \"" + task.getName() + "\" created successfully";
        String text = "Successfully created task:\n\n" +
                "Name: " + task.getName() + "\n" +
                (task.getDescription() != null ? "Description: " + task.getDescription() + "\n" : "") +
                (task.getDueDate() != null ? "Due Date: " + task.getDueDate() + "\n" : "") +
                (task.getAssignedUser() != null ? "assigned to: " + task.getAssignedUser().getUsername() + "\n" : "") +
                (task.getAssignedDepartment() != null ?
                        "department: " + task.getAssignedDepartment().getDepartmentName() : "");
        try {
            E_SERVICE.sendEmail(to, subject, text);
        } catch (MessagingException ignored) {
        }
    }

    private static void sendInvalidDataEmail(String to, Map<String, String> extractedData, EmailMessage msg) {
        String subject = "Invalid data!";

        String text = String.format(
                "The task data is invalid!\r\n\r\n" +
                        "User '%s' does not belong to department '%s'!\r\n\r\n" +
                        "If you want to create this task, please send it again with valid data!\r\n" +
                        "       -> To assign it to user '%s', please DO NOT specify a DEPARTMENT.\r\n" +
                        "       -> To assign it to a department '%s', please DO NOT specify a USER.\r\n\r\n" +
                        "Your original message:\r\n\"%s\"",
                extractedData.get("user"),
                extractedData.get("department"),
                extractedData.get("user"),
                extractedData.get("department"),
                msg.getContent()
        );

        try {
            E_SERVICE.sendEmail(to, subject, text);
        } catch (MessagingException ignored) {
        }
    }
}
