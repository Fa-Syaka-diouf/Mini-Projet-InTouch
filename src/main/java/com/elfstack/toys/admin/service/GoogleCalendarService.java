package com.elfstack.toys.admin.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GoogleCalendarService {

    @Value("${google.calendar.api.key}")
    private String apiKey;

    @Value("${google.calendar.application.name}")
    private String applicationName;

    private Calendar calendarService;

    public Calendar getCalendarService() throws Exception {
        if (calendarService == null) {
            calendarService = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    null)
                    .setApplicationName(applicationName)
                    .build();
        }
        return calendarService;
    }

    public List<Event> getHolidayEvents(String calendarId, LocalDate startDate, LocalDate endDate) {
        try {
            Calendar service = getCalendarService();

            String timeMin = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "T00:00:00Z";
            String timeMax = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "T23:59:59Z";

            log.info("Récupération des événements pour le calendrier: {} du {} au {}",
                    calendarId, startDate, endDate);

            Events events = service.events().list(calendarId)
                    .setKey(apiKey)
                    .setTimeMin(com.google.api.client.util.DateTime.parseRfc3339(timeMin))
                    .setTimeMax(com.google.api.client.util.DateTime.parseRfc3339(timeMax))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setMaxResults(500)
                    .execute();

            List<Event> items = events.getItems();
            log.info("Trouvé {} événements pour {}", items.size(), calendarId);

            return items;

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des événements pour {}: {}", calendarId, e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean testCalendarAccess(String calendarId) {
        try {
            Calendar service = getCalendarService();
            service.events().list(calendarId)
                    .setKey(apiKey)
                    .setMaxResults(1)
                    .execute();
            return true;
        } catch (Exception e) {
            log.error("Test d'accès échoué pour le calendrier {}: {}", calendarId, e.getMessage());
            return false;
        }
    }
}
