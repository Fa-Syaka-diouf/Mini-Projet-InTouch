package com.elfstack.toys.admin.service;

import com.elfstack.toys.admin.domain.JourFerie;
import com.elfstack.toys.admin.domain.JourFerieRepository;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidaySyncService {

    private final GoogleCalendarService googleCalendarService;
    private final JourFerieRepository jourFerieRepository;
    private final CountryCalendarConfig countryCalendarConfig;
    private Map<String, String[]> countryCalendars;
    @Transactional
    public void syncAllCountries(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        countryCalendars = countryCalendarConfig.getAllCountryCalendars();
        for (Map.Entry<String, String[]> entry : countryCalendars.entrySet()) {
            String country = entry.getKey();
            String calendarId = entry.getValue()[0];
            String countryCode = entry.getValue()[1];

            try {
                syncCountryHolidays(countryCode,country, calendarId, startDate, endDate);
            } catch (Exception e) {
                log.error("Erreur lors de la synchronisation pour {}: {}", country, e.getMessage());
            }
        }
    }
    public String countryCodeSetup(String Country){
        countryCalendars = countryCalendarConfig.getAllCountryCalendars();
        for (Map.Entry<String, String[]> entry : countryCalendars.entrySet()){
            if(Country.equals(entry.getKey())){
                return entry.getValue()[1];
            }
        }
        return "Null";
    }

    public void syncCountryHolidays(String countryCode,String country, String calendarId, LocalDate startDate, LocalDate endDate) {
        log.info(" DÉBUT - Synchronisation pour {} ({})", country, calendarId);

        List<Event> events = googleCalendarService.getHolidayEvents(calendarId, startDate, endDate);
        log.info(" Récupéré {} événements pour {}", events.size(), country);

        if (events.isEmpty()) {
            log.warn(" Aucun événement pour {} - Arrêt du traitement", country);
            return;
        }

        int newHolidays = 0;
        int updatedHolidays = 0;

        for (Event event : events) {
            try {
                log.debug(" Traitement événement: {}", event.getSummary());
                JourFerie jourFerie = convertEventToJourFerie(event, country, countryCode);

                if (jourFerie != null) {
                    Optional<JourFerie> existing = jourFerieRepository.findByGoogleEventId(event.getId());
                    Optional<JourFerie> existing_name = jourFerieRepository.findByNomAndPaysCode(event.getSummary(), countryCode);
                    if (existing.isPresent()) {
                        JourFerie existingHoliday = existing.get();
                        updateExistingHoliday(existingHoliday, jourFerie);
                        jourFerieRepository.save(existingHoliday);
                        updatedHolidays++;
                        log.debug(" Mis à jour: {}", event.getSummary());
                    }
                    if (existing_name.isPresent()){
                        JourFerie existingHolidayName = existing_name.get();
                        updateExistingHolidayName(existingHolidayName, jourFerie);
                        jourFerieRepository.save(existingHolidayName);
                        updatedHolidays++;
                        log.debug(" Mis à jour de la date de fin de: {}", event.getSummary());

                    } else {
                        jourFerieRepository.save(jourFerie);
                        newHolidays++;
                        log.debug(" Créé: {}", event.getSummary());
                    }
                } else {
                    log.warn(" Événement ignoré (null): {}", event.getSummary());
                }
            } catch (Exception e) {
                log.error(" Erreur événement {}: {}", event.getSummary(), e.getMessage(), e);
            }
        }

        log.info("✅ FIN - {}: {} nouveaux, {} mis à jour", country, newHolidays, updatedHolidays);
    }

    private JourFerie convertEventToJourFerie(Event event, String country, String countryCode) {
      if (event.getSummary() == null) {
            return null;
        }

        JourFerie jourFerie = new JourFerie();
        jourFerie.setPaysCode(countryCode);
        jourFerie.setPays(country);
        jourFerie.setNom(event.getSummary());
        jourFerie.setDescription("No description");
        jourFerie.setGoogleEventId(event.getId());
        jourFerie.setType(event.getEventType());

        EventDateTime startDateTime = event.getStart();
        EventDateTime endDateTime = event.getEnd();

        if (startDateTime != null) {
            LocalDate startDate = extractLocalDate(startDateTime);
            jourFerie.setDateDebut(startDate);

            if (endDateTime != null) {
                LocalDate endDate = extractLocalDate(endDateTime);
                jourFerie.setDateFin(endDate);
            } else {
                jourFerie.setDateFin(startDate);
            }
        }

        return jourFerie;
    }

    private LocalDate extractLocalDate(EventDateTime eventDateTime) {
        if (eventDateTime.getDate() != null) {
            return LocalDate.parse(eventDateTime.getDate().toString());
        } else if (eventDateTime.getDateTime() != null) {
            DateTime dateTime = eventDateTime.getDateTime();
            dateTime.toStringRfc3339();
            return LocalDate.parse(dateTime.toStringRfc3339().substring(0, 10));
        }
        return LocalDate.now();
    }

    private void updateExistingHoliday(JourFerie existing, JourFerie updated) {
        existing.setNom(updated.getNom());
        existing.setDescription(updated.getDescription());
        existing.setDateDebut(updated.getDateDebut());
        existing.setPays(updated.getPays());
        existing.setPaysCode(updated.getPaysCode());
        existing.setDateFin(updated.getDateFin());
        existing.setType(updated.getType());
    }
    private void updateExistingHolidayName(JourFerie existing, JourFerie updated) {
        existing.setDateFin(updated.getDateFin());
    }

    public List<JourFerie> getHolidaysByCountry(String country) {
        return jourFerieRepository.findByPays(country);
    }
    public List<String> getAllCountries() {
        return jourFerieRepository.findDistinctPays();
    }
    public List<JourFerie> getHolidaysByCountryAndYear(String country, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return jourFerieRepository.findByPaysAndDateDebutBetween(country.toLowerCase(), startDate, endDate);
    }
    /**
     * Récupère tous les jours fériés de tous les pays
     */
    public List<JourFerie> getAllJourFerie() {
        return jourFerieRepository.getAllJourFerie();
    }
    public JourFerie saveJourFerie(JourFerie jourFerie) {
        String paysCode = countryCodeSetup(jourFerie.getPays());
        jourFerie.setPaysCode(paysCode);
        return jourFerieRepository.save(jourFerie);
    }
}