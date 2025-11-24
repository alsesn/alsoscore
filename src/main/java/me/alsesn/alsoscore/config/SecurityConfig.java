package me.alsesn.alsoscore.config;

import me.alsesn.alsoscore.model.enums.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = false)
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder().encode("adminpass"))
                .roles(Role.ADMIN.name())
                .build();
        UserDetails creator = User.withUsername("creator")
                .password(passwordEncoder().encode("creatorpass"))
                .roles(Role.CREATOR.name())
                .build();
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder().encode("userpass"))
                .roles(Role.USER.name())
                .build();
        return new InMemoryUserDetailsManager(admin, creator, user);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/tests/**").hasAnyRole(Role.ADMIN.name(), Role.CREATOR.name())
                        .requestMatchers("/api/v1/sessions/**", "/api/v1/reports/**").hasRole(Role.USER.name())
                        .anyRequest().authenticated()
                )
                .httpBasic(h -> {});
        return http.build();
    }
}