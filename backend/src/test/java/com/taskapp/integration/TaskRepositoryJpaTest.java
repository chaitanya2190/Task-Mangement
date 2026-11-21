package com.taskapp.integration;

import com.taskapp.model.Role;
import com.taskapp.model.Task;
import com.taskapp.model.TaskPriority;
import com.taskapp.model.TaskStatus;
import com.taskapp.model.User;
import com.taskapp.repository.TaskRepository;
import com.taskapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class TaskRepositoryJpaTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User admin;
    private User member;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        admin = userRepository.save(User.builder()
                .name("Admin")
                .email("admin-test@example.com")
                .password("hash")
                .role(Role.ADMIN)
                .build());

        member = userRepository.save(User.builder()
                .name("Member")
                .email("member-test@example.com")
                .password("hash")
                .role(Role.MEMBER)
                .build());

        // Task 1: Assigned to member, created by admin
        taskRepository.save(Task.builder()
                .title("Task 1")
                .description("Desc 1")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .assignedTo(member)
                .createdBy(admin)
                .build());

        // Task 2: Assigned to admin, created by member
        taskRepository.save(Task.builder()
                .title("Task 2")
                .description("Desc 2")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .assignedTo(admin)
                .createdBy(member)
                .build());

        // Task 3: Assigned to admin, created by admin (member has no relation)
        taskRepository.save(Task.builder()
                .title("Task 3")
                .description("Desc 3")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.LOW)
                .assignedTo(admin)
                .createdBy(admin)
                .build());
    }

    @Test
    void findAllFiltered_MemberOnlySeesAssignedOrCreatedTasks() {
        Page<Task> memberTasks = taskRepository.findAllFiltered(
                null, null, null, member.getId(), PageRequest.of(0, 10));

        // Member is assignee of Task 1 and creator of Task 2. Should see 2 tasks, not Task 3.
        assertEquals(2, memberTasks.getTotalElements());
    }

    @Test
    void findAllFiltered_AdminSeesAllTasksWhenMemberIdIsNull() {
        Page<Task> allTasks = taskRepository.findAllFiltered(
                null, null, null, null, PageRequest.of(0, 10));

        assertEquals(3, allTasks.getTotalElements());
    }

    @Test
    void findAllFiltered_FilterByStatusAndPriority() {
        Page<Task> highPriorityTodo = taskRepository.findAllFiltered(
                TaskStatus.TODO, TaskPriority.HIGH, null, null, PageRequest.of(0, 10));

        assertEquals(1, highPriorityTodo.getTotalElements());
        assertEquals("Task 1", highPriorityTodo.getContent().get(0).getTitle());
    }

    @Test
    void saveAndFindById_PersistsCorrectlyWithAuditing() {
        Task newTask = taskRepository.save(Task.builder()
                .title("New Persisted Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .assignedTo(member)
                .createdBy(admin)
                .build());

        assertNotNull(newTask.getId());
        assertNotNull(newTask.getCreatedAt());

        Task retrieved = taskRepository.findById(newTask.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals("New Persisted Task", retrieved.getTitle());
        assertEquals(member.getId(), retrieved.getAssignedTo().getId());
    }
}
