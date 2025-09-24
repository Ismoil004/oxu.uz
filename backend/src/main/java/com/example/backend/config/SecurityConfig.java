package com.example.backend.config;


import com.example.backend.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MyFilter myFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,"/api/auth/refresh","api/auth/test","api/auth/token","/api/auth/user").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/bino","/api/bino/{binoId}","/api/bino/{binoId}/floor/{floorId}","/api/bino/{binoId}/floor/{floorId}/rooms").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/auth/login","/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/problems/statistics","/api/problems/active",
                                "/api/problems/floor/{floorId}","/api/problems/room/{roomId}",
                                "/api/problems/status/{status}",
                                "/api/problems","/api/problems/room/{roomId}/with-problems",
                                "/api/problems/room/{roomId}/problem-types","/api/problems/room/{roomId}/problem-type/{problemType}",
                                "/api/problems/technician/{technicianId}","/api/problems/technician/{technicianId}/status/{status}",
                                "/api/problems/statistics/counts","/api/problems/statistics/completed-by-technician","/api/problems/statistics/today-completed").permitAll()
                        .requestMatchers(HttpMethod.PATCH,"/api/problems/{problemId}/status").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/problems","/api/problems/room/{roomId}/selected-problems").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(myFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

