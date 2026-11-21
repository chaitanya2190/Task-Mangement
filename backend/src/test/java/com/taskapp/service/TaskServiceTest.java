package com.taskapp.service;

import com.taskapp.dto.TaskPatchRequest;
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
    private Task taskCreatedByMember1;

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
                .dueDate(LocalDateTime.of(2026, 12, 1, 10, 0))
                .assignedTo(member1)
                .createdBy(admin)
                .createdAt(LocalDateTime.now())
                .build();

        taskCreatedByMember1 = Task.builder()
                .id(101L)
                .title("Created by Member 1")
                .description("Assigned to Member 2")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .assignedTo(member2)
                .createdBy(member1)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllTasks_MemberScopedToOwnOrCreatedTasks() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(taskRepository.findAllFiltered(null, null, null, member1.getId(), pageRequest))
                .thenReturn(new PageImpl<>(List.of(task1)));

        Page<TaskResponse> result = taskService.getAllTasks(null, null, null, pageRequest, member1);

        assertEquals(1, result.getTotalElements());
        verify(taskRepository).findAllFiltered(null, null, null, member1.getId(), pageRequest);
    }

    @Test
    void getTaskById_MemberCannotViewUnrelatedTask() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));

        // member2 is neither assignee (member1) nor creator (admin)
        assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(100L, member2));
    }

    @Test
    void getTaskById_CreatorCanViewTaskEvenIfAssignedToSomeoneElse() {
        when(taskRepository.findById(101L)).thenReturn(Optional.of(taskCreatedByMember1));

        // member1 created task101, which is assigned to member2
        TaskResponse response = taskService.getTaskById(101L, member1);

        assertNotNull(response);
        assertEquals(101L, response.getId());
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
    void updateTask_AssigneeCanUpdateStatusAndDescription() {
        TaskRequest request = new TaskRequest();
        request.setTitle(task1.getTitle()); // unchanged
        request.setDescription("Updated description");
        request.setStatus(TaskStatus.DONE);
        request.setPriority(task1.getPriority()); // unchanged
        request.setDueDate(task1.getDueDate()); // unchanged

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTask(100L, request, member1);

        assertEquals(TaskStatus.DONE, response.getStatus());
        assertEquals("Updated description", response.getDescription());
        assertEquals(TaskPriority.HIGH, response.getPriority());
    }

    @Test
    void updateTask_AssigneeCannotUpdateTitle() {
        TaskRequest request = new TaskRequest();
        request.setTitle("New Unauthorized Title");
        request.setStatus(TaskStatus.DONE);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskService.updateTask(100L, request, member1));

        assertTrue(exception.getMessage().contains("Assignees cannot update task title"));
    }

    @Test
    void updateTask_AssigneeCannotUpdatePriority() {
        TaskRequest request = new TaskRequest();
        request.setTitle(task1.getTitle());
        request.setPriority(TaskPriority.LOW); // trying to change priority from HIGH to LOW
        request.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskService.updateTask(100L, request, member1));

        assertTrue(exception.getMessage().contains("Assignees cannot update task priority"));
    }

    @Test
    void updateTask_AssigneeCannotUpdateDueDate() {
        TaskRequest request = new TaskRequest();
        request.setTitle(task1.getTitle());
        request.setPriority(task1.getPriority());
        request.setDueDate(LocalDateTime.of(2027, 1, 1, 0, 0)); // changed due date
        request.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskService.updateTask(100L, request, member1));

        assertTrue(exception.getMessage().contains("Assignees cannot update task due date"));
    }

    @Test
    void updateTask_MemberCannotReassignTask() {
        TaskRequest request = new TaskRequest();
        request.setAssignedToId(member2.getId()); // Trying to reassign to member2

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
                () -> taskService.updateTask(100L, request, member1));
        
        assertTrue(exception.getMessage().contains("Only ADMIN or the creator can reassign"));
    }

    @Test
    void updateTask_AdminCanUpdateAllFieldsAndReassign() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Admin Updated Title");
        request.setDescription("Admin Updated Desc");
        request.setStatus(TaskStatus.DONE);
        request.setPriority(TaskPriority.LOW);
        request.setAssignedToId(member2.getId());

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));
        when(userRepository.findById(member2.getId())).thenReturn(Optional.of(member2));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTask(100L, request, admin);

        assertEquals("Admin Updated Title", response.getTitle());
        assertEquals("Admin Updated Desc", response.getDescription());
        assertEquals(TaskStatus.DONE, response.getStatus());
        assertEquals(TaskPriority.LOW, response.getPriority());
        assertEquals(member2.getId(), response.getAssignedTo().getId());
    }

    @Test
    void patchTask_AssigneeCanPatchOnlyStatusAndDescriptionWithoutOtherFields() {
        TaskPatchRequest patch = TaskPatchRequest.builder()
                .status(TaskStatus.DONE)
                .description("Patched description")
                .build();

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.patchTask(100L, patch, member1);

        assertEquals(TaskStatus.DONE, response.getStatus());
        assertEquals("Patched description", response.getDescription());
        assertEquals("Task 1", response.getTitle()); // unchanged
        assertEquals(TaskPriority.HIGH, response.getPriority()); // unchanged
    }

    @Test
    void patchTask_AssigneeCannotPatchPriority() {
        TaskPatchRequest patch = TaskPatchRequest.builder()
                .priority(TaskPriority.LOW)
                .build();

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));

        assertThrows(AccessDeniedException.class, () -> taskService.patchTask(100L, patch, member1));
    }

    @Test
    void deleteTask_MemberCannotDeleteTaskCreatedBySomeoneElse() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task1));

        // task1 was created by admin, assigned to member1
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
                () -> taskService.deleteTask(100L, member1));
        
        assertTrue(exception.getMessage().contains("Only ADMIN or the creator can delete"));
    }

    @Test
    void deleteTask_CreatorCanDeleteOwnTask() {
        when(taskRepository.findById(101L)).thenReturn(Optional.of(taskCreatedByMember1));
        doNothing().when(taskRepository).delete(taskCreatedByMember1);

        assertDoesNotThrow(() -> taskService.deleteTask(101L, member1));
        verify(taskRepository).delete(taskCreatedByMember1);
    }
}
