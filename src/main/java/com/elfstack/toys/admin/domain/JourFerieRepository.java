package com.elfstack.toys.admin.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface JourFerieRepository extends JpaRepository<JourFerie,Long> {
    List<JourFerie> findByDateDebutBetween(LocalDate start, LocalDate end);
    boolean existsByPaysCodeAndDateDebutAndNom(String paysCode, LocalDate dateDebut, String nom);
    Optional<JourFerie> findByGoogleEventId(String googleEventId);
    Optional<JourFerie> findByNomAndPaysCode(String nom, String paysCode);
    List<JourFerie> findByPays(String pays);
    @Query("SELECT DISTINCT j.pays FROM JourFerie j ORDER BY j.pays")
    List<String> findDistinctPays();

    @Query("SELECT j FROM JourFerie j")
    List<JourFerie> getAllJourFerie();

    List<JourFerie> findByPaysAndDateDebutBetween(
            String pays,
            LocalDate startDate,
            LocalDate endDate
    );

//    Arrays findByPaysCodeAndDateDebutBetween(String paysCode, LocalDate currentDate, LocalDate currentDate1);
    List<JourFerie> findByPaysCodeAndDateDebutBetween(String paysCode, LocalDate start, LocalDate end);


    @Query("SELECT DISTINCT j.paysCode FROM JourFerie j ORDER BY j.paysCode")
    List<String> findDistinctPaysCode();

}
