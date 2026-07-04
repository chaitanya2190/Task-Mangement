package com.taskapp.dto;

import com.taskapp.model.TaskPriority;
import com.taskapp.model.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private UserSummaryResponse assignedTo;
    private UserSummaryResponse createdBy;
}
