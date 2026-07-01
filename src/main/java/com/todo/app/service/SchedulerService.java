package com.todo.app.service;

import com.todo.app.entity.Task;
import com.todo.app.entity.User;
import com.todo.app.repository.TaskRepository;
import com.todo.app.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Component
public class SchedulerService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final FCMService fcmService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public SchedulerService(TaskRepository taskRepository, UserRepository userRepository, FCMService fcmService, EmailService emailService, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.fcmService = fcmService;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 60000)
    public void scheduleDesktopNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> activeTasks = taskRepository.findByStatusNot("DONE");
        System.out.println("[Scheduler] Checking reminders. Active tasks count: " + activeTasks.size() + " | Current time: " + now);

        for (Task task : activeTasks) {
            if (task.getRemindAt() != null) {
                System.out.println("[Scheduler] Task: \"" + task.getTitle() + 
                                   "\" | RemindAt: " + task.getRemindAt() + 
                                   " | CurrentTime: " + now + 
                                   " | isReminderSent: " + task.isReminderSent());
            }

            if (task.getDueTime() != null && task.getUserId() != null) {
                long minutesUntilDue = ChronoUnit.MINUTES.between(now, task.getDueTime());

                // Trigger Desktop Notification via FCM 5 minutes before deadline
                if (minutesUntilDue == 5) {
                    sendDesktopAlert(task, "Tick-Tock!", "Your mission \"" + task.getTitle() + "\" is due in 5 mins. Time to wrap it up! ⚡", "https://cdn-icons-png.flaticon.com/512/564/564619.png");
                } 
                // Trigger Desktop Notification when exact deadline is hit (0 mins)
                else if (minutesUntilDue == 0) {
                    sendDesktopAlert(task, "Mission Overdue!", "The deadline for \"" + task.getTitle() + "\" just passed. Immediate action required! 🏃‍♂️", "https://cdn-icons-png.flaticon.com/512/595/595067.png");
                }
            }

            // Trigger custom reminder if set and not already sent
            if (task.getRemindAt() != null && !task.isReminderSent() && task.getUserId() != null) {
                if (now.isAfter(task.getRemindAt()) || now.isEqual(task.getRemindAt())) {
                    sendDesktopAlert(task, "Mission Reminder", "Reminder for your mission: \"" + task.getTitle() + "\"", "https://cdn-icons-png.flaticon.com/512/564/564619.png");
                    task.setReminderSent(true);
                    taskRepository.save(task);
                }
            }
        }
    }

    private void sendDesktopAlert(Task task, String title, String body, String imageUrl) {
        Optional<User> userOpt = userRepository.findById(task.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 1. Send Push (Desktop Notification) via FCMService (if FCM token is registered)
            fcmService.sendNotificationByEmail(
                    user.getEmail(),
                    title,
                    body,
                    imageUrl,
                    "http://localhost:5173/dashboard"
            );
            
            // 2. Send Push (Web Push / VAPID) via NotificationService
            notificationService.push(
                    user.getEmail(),
                    title,
                    body,
                    imageUrl,
                    "http://localhost:5173/dashboard"
            );
            
            // 3. Send Email Notification
            emailService.sendEmail(
                    user.getEmail(),
                    "Task Alert: " + title,
                    "Hello " + user.getName() + ",\n\n" + body
            );

            System.out.println("Triggered Multi-Channel Notification for Task: " + task.getTitle());
        }
    }
}