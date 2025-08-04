package com.elfstack.toys.admin.service;

import com.elfstack.toys.admin.domain.JourFerie;
import com.elfstack.toys.admin.domain.JourFerieRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final JourFerieRepository jourFerieRepository;

    public CalendarService(JourFerieRepository jourFerieRepository) {
        this.jourFerieRepository = jourFerieRepository;
    }

    /**
     * @param startDate Date de début
     * @param slaDays Nombre de jours ouvrés à ajouter
     * @param paysCode Code du pays pour les jours fériés
     * @return Date limite calculée
     */
    public LocalDate calculateDueDate(LocalDate startDate, int slaDays, String paysCode) {
        validateInputs(startDate, slaDays, paysCode);

        LocalDate currentDate = startDate;
        int workingDaysAdded = 0;

        LocalDate endSearchDate = startDate.plusDays(slaDays * 2L);
        Set<LocalDate> joursFeries = getJoursFeriesInPeriod(paysCode, startDate, endSearchDate);

        while (workingDaysAdded < slaDays) {
            currentDate = currentDate.plusDays(1);
            if (isWorkingDay(currentDate, joursFeries)) {
                workingDaysAdded++;
            }
        }

        return currentDate;
    }

    /**
     * Vérifie si une date est un jour ouvré (ni weekend ni jour férié)
     */
    private boolean isWorkingDay(LocalDate date, Set<LocalDate> joursFeries) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        return !joursFeries.contains(date);
    }

    private Set<LocalDate> getJoursFeriesInPeriod(String paysCode, LocalDate startDate, LocalDate endDate) {
        List<JourFerie> joursFeries = jourFerieRepository.findByPaysCodeAndDateDebutBetween(
                paysCode, startDate, endDate);

        return joursFeries.stream()
                .map(JourFerie::getDateDebut)
                .collect(Collectors.toSet());
    }

    private void validateInputs(LocalDate startDate, int slaDays, String paysCode) {
        if (startDate == null) {
            throw new IllegalArgumentException("La date de début ne peut pas être null");
        }
        if (paysCode == null || paysCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code pays ne peut pas être null ou vide");
        }
        if (slaDays <= 0) {
            throw new IllegalArgumentException("Le nombre de jours SLA doit être positif");
        }
        if (slaDays > 365) {
            throw new IllegalArgumentException("Le nombre de jours SLA ne peut pas dépasser 365 jours");
        }
    }

    public List<String> getAvailableCountries() {
        return jourFerieRepository.findDistinctPaysCode();
    }

    public int calculateWorkingDaysBetween(LocalDate startDate, LocalDate endDate, String paysCode) {
        validateInputs(startDate, 1, paysCode);
        if (endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Date de fin invalide");
        }

        Set<LocalDate> joursFeries = getJoursFeriesInPeriod(paysCode, startDate, endDate);
        int workingDays = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (isWorkingDay(currentDate, joursFeries)) {
                workingDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return workingDays;
    }
}