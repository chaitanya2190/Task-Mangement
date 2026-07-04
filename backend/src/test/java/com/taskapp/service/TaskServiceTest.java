package com.taskapp.service;

import com.taskapp.dto.TaskRequest;
import com.taskapp.dto.TaskResponse;
import com.taskapp.model.Role;
import com.taskapp.model.Task;
import com.taskapp.model.TaskPriority;
import com.taskapp.model.TaskStatus;
import com.taskapp.model.User;
import com.taskapp.repository.TaskRepository;
import com.taskapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User admin;
    private User member1;
    private User member2;
    private Task task1;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).email("admin@test.com").role(Role.ADMIN).build();
        member1 = User.builder().id(2L).email("m1@test.com").role(Role.MEMBER).build();
        member2 = User.builder().id(3L).email("m2@test.com").role(Role.MEMBER).build();

        task1 = Task.builder()
                .id(100L)
                .title("Task 1")
                .description("Desc 1")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .assignedTo(member1)
                .createdBy(admin)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllTasks_MemberForcedToSeeOnlyOwnTasks() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(taskRepository.findAllFiltered(null, null, member1.getId(), pageRequest))
                .thenReturn(new PageImpl<>(List.of(task1)));

        // Requesting with assignedToId = 999 should be overridden by member1.getId() (2L)
        Page<TaskResponse> result = taskService.getAllTasks(null, null, 999L, pageRequest, member1);

        assertEquals(1, result.getTotalElements());
        verify(taskRepository).findAllFiltered(null, null, member1.getId(), pageRequest);
    }

    @Test
    void getTaskById_MemberCannotViewOtherUsersTask() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));

        // member2 tries to view task1 (assigned to member1)
        assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(100L, member2));
    }

    @Test
    void createTask_DefaultsAssigneeToCreatorIfNotProvided() {
        TaskRequest request = new TaskRequest();
        request.setTitle("New Task");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.LOW);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.createTask(request, member1);

        assertEquals(member1.getId(), response.getAssignedTo().getId());
        assertEquals(member1.getId(), response.getCreatedBy().getId());
    }

    @Test
    void updateTask_MemberCannotReassignTask() {
        TaskRequest request = new TaskRequest();
        request.setAssignedToId(member2.getId()); // Trying to reassign to member2

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));

        // task1 is assigned to member1. member1 tries to reassign it.
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
                () -> taskService.updateTask(100L, request, member1));
        
        assertTrue(exception.getMessage().contains("Only ADMIN or the creator can reassign"));
    }

    @Test
    void updateTask_MemberCanUpdateStatusOfOwnTask() {
        TaskRequest request = new TaskRequest();
        request.setStatus(TaskStatus.DONE);
        request.setPriority(TaskPriority.LOW);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTask(100L, request, member1);

        assertEquals(TaskStatus.DONE, response.getStatus());
        assertEquals(TaskPriority.LOW, response.getPriority());
    }

    @Test
    void deleteTask_MemberCannotDeleteTaskCreatedBySomeoneElse() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));

        // task1 was created by admin, assigned to member1
        // member1 tries to delete it
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
                () -> taskService.deleteTask(100L, member1));
        
        assertTrue(exception.getMessage().contains("Only ADMIN or the creator can delete"));
    }
}
