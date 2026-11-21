package com.taskapp.dto;

import com.taskapp.model.TaskPriority;
import com.taskapp.model.TaskStatus;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskPatchRequest {

    @Builder.Default
    private JsonNullable<String> title = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<String> description = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<TaskStatus> status = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<TaskPriority> priority = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<LocalDateTime> dueDate = JsonNullable.undefined();

    @Builder.Default
    private JsonNullable<Long> assignedToId = JsonNullable.undefined();

    @AssertTrue(message = "Title cannot be empty when provided")
    public boolean isTitleValid() {
        if (title != null && title.isPresent()) {
            return title.get() != null && !title.get().trim().isEmpty();
        }
        return true;
    }

    @AssertTrue(message = "Status cannot be null when provided")
    public boolean isStatusValid() {
        if (status != null && status.isPresent()) {
            return status.get() != null;
        }
        return true;
    }

    @AssertTrue(message = "Priority cannot be null when provided")
    public boolean isPriorityValid() {
        if (priority != null && priority.isPresent()) {
            return priority.get() != null;
        }
        return true;
    }

    @AssertTrue(message = "Description cannot exceed 2000 characters")
    public boolean isDescriptionValid() {
        if (description != null && description.isPresent() && description.get() != null) {
            return description.get().length() <= 2000;
        }
        return true;
    }
}
