package com.taskapp.service;

import com.taskapp.dto.TaskPatchRequest;
import com.taskapp.dto.TaskRequest;
import com.taskapp.dto.TaskResponse;
import com.taskapp.dto.UserSummaryResponse;
import com.taskapp.exception.ResourceNotFoundException;
import com.taskapp.model.Role;
import com.taskapp.model.Task;
import com.taskapp.model.TaskPriority;
import com.taskapp.model.TaskStatus;
import com.taskapp.model.User;
import com.taskapp.repository.TaskRepository;
import com.taskapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Page<TaskResponse> getAllTasks(TaskStatus status, TaskPriority priority, Long assignedToId, Pageable pageable, User currentUser) {
        Long memberId = null;
        if (currentUser.getRole() == Role.MEMBER) {
            memberId = currentUser.getId(); // Scope to tasks assigned to or created by this member
        }
        
        return taskRepository.findAllFiltered(status, priority, assignedToId, memberId, pageable)
                .map(this::mapToResponse);
    }

    public TaskResponse getTaskById(Long id, User currentUser) {
        Task task = getTaskEntity(id);
        
        boolean isCreator = task.getCreatedBy() != null && task.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAssignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUser.getId());

        if (currentUser.getRole() == Role.MEMBER && !isAssignee && !isCreator) {
            throw new AccessDeniedException("You can only view tasks assigned to you or created by you");
        }
        
        return mapToResponse(task);
    }

    public TaskResponse createTask(TaskRequest request, User currentUser) {
        User assignee = currentUser;
        
        if (request.getAssignedToId() != null) {
            assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .assignedTo(assignee)
                .createdBy(currentUser)
                .build();

        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponse updateTask(Long id, TaskRequest request, User currentUser) {
        Task task = getTaskEntity(id);

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isCreator = task.getCreatedBy() != null && task.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAssignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUser.getId());

        if (!isAdmin && !isCreator && !isAssignee) {
            throw new AccessDeniedException("You do not have permission to update this task");
        }

        // Reassignment logic: Only ADMIN or creator can change assignedTo
        if (request.getAssignedToId() != null && !Objects.equals(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null, request.getAssignedToId())) {
            if (!isAdmin && !isCreator) {
                throw new AccessDeniedException("Only ADMIN or the creator can reassign a task");
            }
            User newAssignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            task.setAssignedTo(newAssignee);
        }

        // Privilege restrictions: Assignees who are neither ADMIN nor creator can only update status and description
        if (!isAdmin && !isCreator) {
            if (request.getTitle() != null && !Objects.equals(task.getTitle(), request.getTitle())) {
                throw new AccessDeniedException("Assignees cannot update task title");
            }
            if (request.getPriority() != null && !Objects.equals(task.getPriority(), request.getPriority())) {
                throw new AccessDeniedException("Assignees cannot update task priority");
            }
            if (request.getDueDate() != null && !Objects.equals(task.getDueDate(), request.getDueDate())) {
                throw new AccessDeniedException("Assignees cannot update task due date");
            }
            task.setStatus(request.getStatus());
            task.setDescription(request.getDescription());
        } else {
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setStatus(request.getStatus());
            task.setPriority(request.getPriority());
            task.setDueDate(request.getDueDate());
        }

        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponse patchTask(Long id, TaskPatchRequest request, User currentUser) {
        Task task = getTaskEntity(id);

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isCreator = task.getCreatedBy() != null && task.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAssignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUser.getId());

        if (!isAdmin && !isCreator && !isAssignee) {
            throw new AccessDeniedException("You do not have permission to update this task");
        }

        // Reassignment logic: Only ADMIN or creator can change assignedTo
        if (request.getAssignedToId() != null && request.getAssignedToId().isPresent()) {
            Long newAssignedToId = request.getAssignedToId().get();
            Long currentAssignedToId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;
            if (!Objects.equals(currentAssignedToId, newAssignedToId)) {
                if (!isAdmin && !isCreator) {
                    throw new AccessDeniedException("Only ADMIN or the creator can reassign a task");
                }
                if (newAssignedToId != null) {
                    User newAssignee = userRepository.findById(newAssignedToId)
                            .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + newAssignedToId));
                    task.setAssignedTo(newAssignee);
                } else {
                    task.setAssignedTo(null); // Explicit null clears assignee
                }
            }
        }

        // Field modifications
        if (!isAdmin && !isCreator) {
            // Assignees can only update status and description
            if (request.getTitle() != null && request.getTitle().isPresent()) {
                String newTitle = request.getTitle().get();
                if (!Objects.equals(task.getTitle(), newTitle)) {
                    throw new AccessDeniedException("Assignees cannot update task title");
                }
            }
            if (request.getPriority() != null && request.getPriority().isPresent()) {
                TaskPriority newPriority = request.getPriority().get();
                if (!Objects.equals(task.getPriority(), newPriority)) {
                    throw new AccessDeniedException("Assignees cannot update task priority");
                }
            }
            if (request.getDueDate() != null && request.getDueDate().isPresent()) {
                LocalDateTime newDueDate = request.getDueDate().get();
                if (!Objects.equals(task.getDueDate(), newDueDate)) {
                    throw new AccessDeniedException("Assignees cannot update task due date");
                }
            }

            // Update status if provided
            if (request.getStatus() != null && request.getStatus().isPresent()) {
                task.setStatus(request.getStatus().get());
            }

            // Update description if provided (can be text or null to clear)
            if (request.getDescription() != null && request.getDescription().isPresent()) {
                task.setDescription(request.getDescription().get());
            }
        } else {
            // Admin or Creator can update any field
            if (request.getTitle() != null && request.getTitle().isPresent()) {
                task.setTitle(request.getTitle().get());
            }
            if (request.getDescription() != null && request.getDescription().isPresent()) {
                task.setDescription(request.getDescription().get());
            }
            if (request.getStatus() != null && request.getStatus().isPresent()) {
                task.setStatus(request.getStatus().get());
            }
            if (request.getPriority() != null && request.getPriority().isPresent()) {
                task.setPriority(request.getPriority().get());
            }
            if (request.getDueDate() != null && request.getDueDate().isPresent()) {
                task.setDueDate(request.getDueDate().get()); // Sets value or clears to null if explicitly null
            }
        }

        return mapToResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id, User currentUser) {
        Task task = getTaskEntity(id);

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isCreator = task.getCreatedBy() != null && task.getCreatedBy().getId().equals(currentUser.getId());

        if (!isAdmin && !isCreator) {
            throw new AccessDeniedException("Only ADMIN or the creator can delete this task");
        }

        taskRepository.delete(task);
    }

    private Task getTaskEntity(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .assignedTo(mapUserSummary(task.getAssignedTo()))
                .createdBy(mapUserSummary(task.getCreatedBy()))
                .build();
    }

    private UserSummaryResponse mapUserSummary(User user) {
        if (user == null) return null;
        return UserSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
