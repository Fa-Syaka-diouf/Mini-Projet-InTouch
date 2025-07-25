package com.elfstack.toys.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@Data
@Slf4j
public class CountryCalendarConfig {

    @Value("${country.calendars.json.path:country-calendars.json}")
    private String calendarConfigPath;

    private Map<String, String> countryCalendars = new HashMap<>();

    @PostConstruct
    public void loadCountryCalendars() {
        try {
            System.out.println(calendarConfigPath);
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource(calendarConfigPath);
                countryCalendars = mapper.readValue(
                        resource.getInputStream(),
                        new TypeReference<LinkedHashMap<String, String>>() {}
                );
                log.info("Chargé {} calendriers de pays", countryCalendars.size());

        } catch (IOException e) {
            log.error("Erreur lors du chargement des calendriers pays", e);
        }
    }


    public String getCalendarId(String countryCode) {
        return countryCalendars.get(countryCode.toLowerCase());
    }

    public boolean hasCountry(String countryCode) {
        return countryCalendars.containsKey(countryCode.toLowerCase());
    }

    public LinkedHashMap<String, String> getAllCountryCalendars() {
        return new LinkedHashMap<>(countryCalendars);
    }
}
