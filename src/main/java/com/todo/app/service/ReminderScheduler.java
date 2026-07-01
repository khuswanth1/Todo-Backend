package com.todo.app.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.todo.app.entity.Task;
import com.todo.app.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReminderScheduler {

    private final TaskRepository taskRepo;
    private final WebPushService webPushService;

    public ReminderScheduler(TaskRepository taskRepo, WebPushService webPushService) {
        this.taskRepo = taskRepo;
        this.webPushService = webPushService;
    }

    @Scheduled(fixedRate = 60000)  // every 60 seconds
    public void fireDueReminders() {
        List<Task> due = taskRepo.findByRemindAtBeforeAndReminderSentFalse(LocalDateTime.now());
        for (Task task : due) {
            System.out.println("⏰ ReminderScheduler triggering reminder for: " + task.getTitle());
            webPushService.notifyUser(task);
            task.setReminderSent(true);
            taskRepo.save(task);
        }
    }
}
