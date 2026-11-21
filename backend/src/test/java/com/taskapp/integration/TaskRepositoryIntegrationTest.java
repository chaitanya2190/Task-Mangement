package com.taskapp.integration;

import com.taskapp.model.Role;
import com.taskapp.model.Task;
import com.taskapp.model.TaskPriority;
import com.taskapp.model.TaskStatus;
import com.taskapp.model.User;
import com.taskapp.repository.TaskRepository;
import com.taskapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PostgreSQL Integration Test using Testcontainers.
 * Automatically disabled without error when Docker is not present on the execution host.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
public class TaskRepositoryIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("jwt.secret", () -> "dGhpcy1pcy1hLXN1cGVyLXNlY3JldC1rZXktdGhhdC1zaG91bGQtYmUtYXQtbGVhc3QtMjU2LWJpdHMtbG9uZw==");
    }

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testPostgreSqlDatabaseOperations() {
        User user = userRepository.save(User.builder()
                .name("Postgres User")
                .email("tc-user@example.com")
                .password("hash")
                .role(Role.ADMIN)
                .build());

        Task task = taskRepository.save(Task.builder()
                .title("Testcontainers Task")
                .description("Verified against real PostgreSQL")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .assignedTo(user)
                .createdBy(user)
                .build());

        assertNotNull(task.getId());
        Task found = taskRepository.findById(task.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Testcontainers Task", found.getTitle());
    }
}
