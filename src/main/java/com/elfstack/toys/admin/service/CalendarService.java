package com.elfstack.toys.admin.service;

import com.elfstack.toys.admin.domain.JourFerie;
import com.elfstack.toys.admin.domain.JourFerieRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class CalendarService {

    private static JourFerieRepository jourFerieRepository ;

    public CalendarService(JourFerieRepository jourFerieRepository) {
        CalendarService.jourFerieRepository = jourFerieRepository;
    }

    public static LocalDate calculateDueDate(LocalDate start, int slaDays, String countryCode) {
        LocalDate date = start;
        int added = 0;

        List<LocalDate> joursFeries = jourFerieRepository
                .findByPaysCodeAndDateDebutBetween(countryCode, start, start.plusYears(1))
                .stream()
                .map(JourFerie::getDateDebut) // ou adapter pour gérer les périodes dateDebut - dateFin
                .toList();

        while (added < slaDays) {
            date = date.plusDays(1);
            if (!isWeekend(date) && !joursFeries.contains(date)) {
                added++;
            }
        }

        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() >= 6;
    }


    public List<String> getAvailableCountries() {
        return jourFerieRepository.findDistinctPaysCode();
    }
}

