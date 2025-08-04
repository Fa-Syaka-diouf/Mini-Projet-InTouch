package com.elfstack.toys.admin.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface JourFerieRepository extends JpaRepository<JourFerie,Long> {
    List<JourFerie> findByDateDebutBetween(LocalDate start, LocalDate end);
    boolean existsByPaysCodeAndDateDebutAndNom(String paysCode, LocalDate dateDebut, String nom);
    Optional<JourFerie> findByGoogleEventId(String googleEventId);
    Optional<JourFerie> findByNomAndPaysCode(String nom, String paysCode);

    List<JourFerie> findByPaysCode(String paysCode);
    List<JourFerie> findByPaysCodeAndDateDebutBetween(
            String paysCode,
            LocalDate startDate,
            LocalDate endDate
    );

    List<JourFerie> findByPays(String pays);
    List<JourFerie> findByPaysAndDateDebutBetween(
            String pays,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT DISTINCT j.pays FROM JourFerie j ORDER BY j.pays")
    List<String> findDistinctPays();

    @Query("SELECT DISTINCT j.paysCode FROM JourFerie j ORDER BY j.paysCode")
    List<String> findDistinctPaysCode();

    @Query("SELECT j FROM JourFerie j")
    List<JourFerie> getAllJourFerie();
}