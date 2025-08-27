package com.elfstack.toys.security.dev;

import com.elfstack.toys.security.controlcenter.ControlCenterSecurityConfig;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@EnableWebSecurity
@Configuration
@Import({ VaadinAwareSecurityContextHolderStrategyConfiguration.class })
@ConditionalOnMissingBean(ControlCenterSecurityConfig.class)
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.keycloak.client-id")
class DevSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(DevSecurityConfig.class);

    DevSecurityConfig() {
        log.warn("Using DEVELOPMENT security configuration with Keycloak OAuth2. This should not be used in production environments!");
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/reset-otp")
                        .ignoringRequestMatchers("/api/**", "/VAADIN/**")
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(this.oidcUserService())
                        )
                        .defaultSuccessUrl("/", false)
                        .failureHandler(this.oAuth2AuthenticationFailureHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/users-management").hasRole("ADMIN")
                        .requestMatchers("/system-config").hasRole("ADMIN")
                        .requestMatchers("/api/reset-otp").permitAll()
                        .requestMatchers("/api/confirm-reset-otp").permitAll()
                        .requestMatchers("/VAADIN/**").permitAll()
                        .requestMatchers("/VAADIN/**", "/themes/**", "/images/**").permitAll()
                        .requestMatchers("/images/**").permitAll()

                        .requestMatchers("/task-list/**").hasAnyRole("ADMIN", "USER")
                )
                .with(VaadinSecurityConfigurer.vaadin(), configurer -> {
                })
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
    @Bean
    public AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return (request, response, exception) -> {
            log.error("=== OAUTH2 AUTHENTICATION FAILURE ===");
            log.error("Request URI: {}", request.getRequestURI());
            log.error("Request URL: {}", request.getRequestURL());
            log.error("Query String: {}", request.getQueryString());
            log.error("Remote Address: {}", request.getRemoteAddr());
            log.error("Session ID: {}", request.getSession().getId());

            // Headers
            log.error("=== REQUEST HEADERS ===");
            Collections.list(request.getHeaderNames()).forEach(headerName -> {
                log.error("{}: {}", headerName, request.getHeader(headerName));
            });

            // Parameters
            log.error("=== REQUEST PARAMETERS ===");
            request.getParameterMap().forEach((key, values) -> {
                log.error("{}: {}", key, Arrays.toString(values));
            });

            // Exception details
            log.error("Exception Type: {}", exception.getClass().getSimpleName());
            log.error("Exception Message: {}", exception.getMessage());
            log.error("Exception Cause: {}", exception.getCause() != null ? exception.getCause().getMessage() : "null");

            // Stack trace complète
            log.error("Full Stack Trace:", exception);

            // Si c'est une OAuth2AuthenticationException, extraire plus de détails
            if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
                log.error("OAuth2 Error Code: {}", oauth2Exception.getError().getErrorCode());
                log.error("OAuth2 Error Description: {}", oauth2Exception.getError().getDescription());
                log.error("OAuth2 Error URI: {}", oauth2Exception.getError().getUri());
            }

            // Redirection avec détails d'erreur encodés
            String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
            String errorType = exception.getClass().getSimpleName();

            response.sendRedirect("/?error=oauth_failed&type=" + errorType + "&message=" + errorMessage);
        };
    }
    /**
     * Service personnalisé pour extraire les rôles depuis Keycloak
     */
    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return (userRequest) -> {
            // Charger l'utilisateur avec l'implémentation par défaut
            OidcUser oidcUser = delegate.loadUser(userRequest);

            Set<SimpleGrantedAuthority> mappedAuthorities = new HashSet<>();

            // EXTRAIRE L'ACCESS TOKEN
            String accessTokenValue = userRequest.getAccessToken().getTokenValue();

            // Décoder le token JWT
            JwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(userRequest.getClientRegistration().getProviderDetails().getIssuerUri());
            Jwt jwt = jwtDecoder.decode(accessTokenValue);

            // Accéder à realm_access.roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof List rolesList) {
                List<String> roles = (List<String>) rolesList;
                mappedAuthorities.addAll(
                        roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                .collect(Collectors.toSet())
                );
            }

            if (mappedAuthorities.isEmpty()) {
                System.out.println("Access token trouvé, mais aucun rôle dans realm_access.");
                mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        };
    }


}