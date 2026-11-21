package com.taskapp.controller;

import com.taskapp.dto.TaskResponse;
import com.taskapp.exception.GlobalExceptionHandler;
import com.taskapp.model.Role;
import com.taskapp.model.TaskStatus;
import com.taskapp.model.User;
import com.taskapp.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).email("admin@test.com").role(Role.ADMIN).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JsonNullableModule());
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getAllTasks_NegativePage_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("page", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Page index must not be less than zero"));
    }

    @Test
    void getAllTasks_ZeroOrNegativeSize_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Page size must be between 1 and 100"));
    }

    @Test
    void getAllTasks_ExcessiveSize_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("size", "101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Page size must be between 1 and 100"));
    }

    @Test
    void patchTask_SuccessfulPartialUpdate_WithOmittedFields() throws Exception {
        String jsonPayload = "{\"status\": \"DONE\", \"description\": \"Patched status only\"}";

        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Original Title")
                .description("Patched status only")
                .status(TaskStatus.DONE)
                .build();

        when(taskService.patchTask(eq(1L), any(), any())).thenReturn(response);

        mockMvc.perform(patch("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.description").value("Patched status only"));
    }

    @Test
    void patchTask_SuccessfulExplicitNullDueDate() throws Exception {
        String jsonPayload = "{\"dueDate\": null}";

        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Original Title")
                .dueDate(null)
                .build();

        when(taskService.patchTask(eq(1L), any(), any())).thenReturn(response);

        mockMvc.perform(patch("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").doesNotExist());
    }

    @Test
    void patchTask_EmptyTitle_ReturnsBadRequestDueToValidation() throws Exception {
        String jsonPayload = "{\"title\": \"\"}";

        mockMvc.perform(patch("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchTask_NullStatus_ReturnsBadRequestDueToValidation() throws Exception {
        String jsonPayload = "{\"status\": null}";

        mockMvc.perform(patch("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void globalExceptionHandler_MasksInternalExceptionMessage() throws Exception {
        when(taskService.getTaskById(any(), any()))
                .thenThrow(new RuntimeException("Sensitive database column syntax failure details"));

        mockMvc.perform(get("/api/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("An unexpected internal server error occurred. Please contact support or try again later."));
    }
}
