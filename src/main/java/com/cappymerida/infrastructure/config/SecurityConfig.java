package com.cappymerida.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()

                        // Endpoints de API requieren autenticación
                        .requestMatchers("/api/v1/patients/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return jwtConverter;
    }

    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return new Converter<Jwt, Collection<GrantedAuthority>>() {
            @Override
            public Collection<GrantedAuthority> convert(Jwt jwt) {
                // Obtener roles de realm_access.roles
                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                Collection<GrantedAuthority> authorities = List.of();

                if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
                    authorities = roles.stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .map(role -> "ROLE_" + role)
                            .map(SimpleGrantedAuthority::new)
                            .map(GrantedAuthority.class::cast)
                            .toList();
                }

                // También obtener roles de resource_access si existen
                Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
                if (resourceAccess != null) {
                    Object clientRoles = resourceAccess.get("patients-service");
                    if (clientRoles instanceof Map<?, ?> clientMap) {
                        Object rolesObj = ((Map<?, ?>) clientMap).get("roles");
                        if (rolesObj instanceof List<?> clientRolesList) {
                            List<GrantedAuthority> clientAuthorities = clientRolesList.stream()
                                    .filter(String.class::isInstance)
                                    .map(String.class::cast)
                                    .map(role -> "ROLE_" + role)
                                    .map(SimpleGrantedAuthority::new)
                                    .map(GrantedAuthority.class::cast)
                                    .toList();

                            authorities = Stream.concat(authorities.stream(), clientAuthorities.stream())
                                    .distinct()
                                    .toList();
                        }
                    }
                }

                return authorities;
            }
        };
    }
}
