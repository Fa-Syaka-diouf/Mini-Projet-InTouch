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
//
//    // Recherche par titre
//    List<Task> findByTitleContainingIgnoreCase(String title);
//
//    // Recherche par pays
//    List<Task> findByCountry(String country);
//
//    // Recherche par statut
//    List<Task> findByStatus(TaskStatus status);
//
//    // Vérifier l'unicité du titre
//    boolean existsByTitleIgnoreCase(String title);
//
//    // Tâches par priorité
//    @Query("SELECT t FROM Task t WHERE t.priority = :priority ORDER BY t.createdDate DESC")
//    List<Task> findByPriorityOrderByCreatedDateDesc(TaskPriority priority);
//
//    // Tâches en retard
//    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_DATE AND t.status != :status")
//    List<Task> findOverdueTasks(TaskStatus status);

//    ManagedTypes findAllBy(Pageable pageable);

//    @Override
    Page<Task> findAllBy(Pageable pageable);

    List<Task> findByStatut(StatutEnum statut);

    List<Task> findByResponsableFullname(String fullname);

    List<Task> findByPriority(TaskPriority priority);

    @Query("SELECT t FROM Task t WHERE t.dateLimite < CURRENT_DATE")
    List<Task> findTasksEnRetard(Pageable pageable);

    @Query("SELECT t FROM Task t " +
            "WHERE t.dateLimite BETWEEN :today AND :endOfWeek " +
            "AND t.statut = 'A_FAIRE' " +
            "ORDER BY t.dateLimite ASC")
    List<Task> findNextTasks(@Param("today") LocalDate today,
                             @Param("endOfWeek") LocalDate endOfWeek,
                             Pageable pageable);


}