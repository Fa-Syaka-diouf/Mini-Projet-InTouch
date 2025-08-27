//package com.elfstack.toys.taskmanagement.domain;
//
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Slice;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//
//public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
//
//    Slice<Task> findAllBy(Pageable pageable);
//}
package com.elfstack.toys.taskmanagement.domain;

import com.elfstack.toys.taskmanagement.domain.Task;
import com.elfstack.toys.taskmanagement.domain.TaskPriority;
import com.elfstack.toys.taskmanagement.domain.TaskStatus;
import org.springframework.data.domain.ManagedTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findAllBy(Pageable pageable);

    List<Task> findByStatut(StatutEnum statut);

    List<Task> findByResponsableUsername(String responsableUsername);

    List<Task> findByPriority(TaskPriority priority);

    @Query("SELECT t FROM Task t " +
            "WHERE (t.dateFin IS NOT NULL AND t.dateFin > t.dateLimite) " +
            "OR (t.dateFin IS NULL AND t.dateLimite < CURRENT_DATE)")
    List<Task> findTasksEnRetard(Pageable pageable);

    @Query("SELECT t FROM Task t " +
            "WHERE t.dateLimite BETWEEN :today AND :endOfWeek " +
            "ORDER BY t.dateLimite ASC")
    List<Task> findNextTasks(@Param("today") LocalDate today,
                             @Param("endOfWeek") LocalDate endOfWeek,
                             Pageable pageable);


}