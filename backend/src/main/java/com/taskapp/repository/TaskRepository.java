package com.taskapp.repository;

import com.taskapp.model.Task;
import com.taskapp.model.TaskPriority;
import com.taskapp.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:assignedToId IS NULL OR t.assignedTo.id = :assignedToId) AND " +
           "(:memberId IS NULL OR t.assignedTo.id = :memberId OR t.createdBy.id = :memberId)")
    Page<Task> findAllFiltered(
           @Param("status") TaskStatus status,
           @Param("priority") TaskPriority priority,
           @Param("assignedToId") Long assignedToId,
           @Param("memberId") Long memberId,
           Pageable pageable);
}
