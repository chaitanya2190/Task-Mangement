package com.taskapp.service;

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

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Page<TaskResponse> getAllTasks(TaskStatus status, TaskPriority priority, Long assignedToId, Pageable pageable, User currentUser) {
        if (currentUser.getRole() == Role.MEMBER) {
            assignedToId = currentUser.getId(); // Force filter to only show their own tasks
        }
        
        return taskRepository.findAllFiltered(status, priority, assignedToId, pageable)
                .map(this::mapToResponse);
    }

    public TaskResponse getTaskById(Long id, User currentUser) {
        Task task = getTaskEntity(id);
        
        if (currentUser.getRole() == Role.MEMBER && !task.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only view your own tasks");
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
        boolean isCreator = task.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAssignee = task.getAssignedTo().getId().equals(currentUser.getId());

        if (!isAdmin && !isCreator && !isAssignee) {
            throw new AccessDeniedException("You do not have permission to update this task");
        }

        // Reassignment logic: Only ADMIN or creator can change assignedTo
        if (request.getAssignedToId() != null && !Objects.equals(task.getAssignedTo().getId(), request.getAssignedToId())) {
            if (!isAdmin && !isCreator) {
                throw new AccessDeniedException("Only ADMIN or the creator can reassign a task");
            }
            User newAssignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            task.setAssignedTo(newAssignee);
        }

        // Update fields
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        return mapToResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id, User currentUser) {
        Task task = getTaskEntity(id);

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isCreator = task.getCreatedBy().getId().equals(currentUser.getId());

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
