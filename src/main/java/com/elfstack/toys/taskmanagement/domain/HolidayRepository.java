package com.elfstack.toys.taskmanagement.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    @Query("SELECT h FROM Holiday h WHERE h.pays_code = :country AND h.date_fin >= :startDate")
    List<Holiday> findRelevantHolidays(@Param("pays_code") String country, @Param("date_debut") LocalDate startDate);
}
