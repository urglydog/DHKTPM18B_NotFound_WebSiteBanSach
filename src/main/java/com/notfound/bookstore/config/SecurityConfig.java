package com.notfound.bookstore.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final String[] PUBLIC_ENDPOINTS = {
            "/api/public/**",

            // Auth
            "/api/auth/**",
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/send-otp",
            "/api/auth/verify-otp",
            "/api/auth/verify-email",
            "/api/auth/confirm-email",
            "/api/auth/google/callback",
            "/api/auth/introspect",

            // Books – Categories – Authors
            "/api/books/**",
            "/api/categories/**",
            "/api/authors/**",

            // News
            "/api/news/**",

            // Review
            "/api/review/book/{bookId}",

            // Payment
            "/api/payment/vnpay/callback",
            "/api/payment/zalopay/callback",
            "/api/payment/zalopay/return",

            // Promotions
            "/api/promotions/active",
            "/api/promotions/book/**",
            "/api/promotions/validate",

            // OAuth2
            "/oauth2/**",
    };

        @Value("${jwt.signerKey}")
        private String signerKey;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                                                .decoder(jwtDecoder())
                                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(Customizer.withDefaults())
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                                .csrf(AbstractHttpConfigurer::disable);

                return http.build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

                return jwtAuthenticationConverter;
        }

        @Bean
        public JwtDecoder jwtDecoder() {
                SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
                return NimbusJwtDecoder
                                .withSecretKey(secretKeySpec)
                                .macAlgorithm(MacAlgorithm.HS512)
                                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(10);
        }
}