package com.elfstack.toys.admin.service;

import com.elfstack.toys.admin.domain.JourFerieRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class CalendarService {

    private static JourFerieRepository jourFerieRepository ;

    public CalendarService(JourFerieRepository jourFerieRepository) {
        CalendarService.jourFerieRepository = jourFerieRepository;
    }

    public static LocalDate calculateDueDate(LocalDate startDate, int slaDays, String paysCode) {
        if (startDate == null || paysCode == null || slaDays <= 0) {
            throw new IllegalArgumentException("Données invalides pour le calcul de la date limite");
        }

        LocalDate currentDate = startDate;
        int workingDaysAdded = 0;

        // On boucle jusqu'à ajouter tous les jours ouvrés
        while (workingDaysAdded < slaDays) {
            currentDate = currentDate.plusDays(1);

            // On saute les samedis et dimanches
            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            // Vérifie si c’est un jour férié dans ce pays
//            boolean isFerie = jourFerieRepository.findByPaysCodeAndDateDebutBetween(
//                    paysCode,
//                    currentDate,
//                    currentDate
//            ).stream().findAny().isPresent();

//            boolean isFerie = !jourFerieRepository.findByPaysCodeAndDateDebutBetween(
//                    paysCode,
//                    currentDate,
//                    currentDate
//            ).isEmpty();


//            if (isFerie) {
//                continue;
//            }

            // Si ni week-end ni jour férié, on compte ce jour comme ouvré
            workingDaysAdded++;
        }

        return currentDate;
    }


    public List<String> getAvailableCountries() {
        return jourFerieRepository.findDistinctPaysCode();
    }
}

