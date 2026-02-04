package com.example.webapp.config;

import com.example.webapp.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// WHAT: Configuration class je Spring Security setup kore
// HOW: @Configuration = Spring ei class theke beans create korbe, @EnableWebSecurity = security features activate kore
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Method level e @PreAuthorize use korte pare
public class SecurityConfig {

    // WHAT: Database theke user info load korar service
    // HOW: Spring automatically inject kore constructor diye (Constructor Injection - best practice)
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // WHAT: Password hash kore store korar jonno encoder
    // HOW: BCrypt algorithm use kore one-way hashing - password reverse kora jay na
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // WHAT: Database check kore user ke authenticate kore (password match kore)
    // HOW: UserDetailsService diye user load kore, then PasswordEncoder diye plain password ar hashed password compare kore
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder()); // Ekhane password matching hoy internally
        return authProvider;
    }

    // WHAT: Authentication manager je overall authentication process handle kore
    // HOW: Controller e manually authentication korte hole use kora jay
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // WHAT: Main security configuration - kon URL te ke access korte parbe seta define kore
    // HOW: HTTP request ashle filter chain check kore: authentication ache ki? authorized ki na? then allow/deny kore
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Amader custom authentication provider set korchi
            .authenticationProvider(authenticationProvider())
            
            // ==== AUTHORIZATION: Ke kon URL e jete parbe ==== (upor theke niche order e check hoy)
            .authorizeHttpRequests(auth -> auth
                // WHAT: Public URLs - login korte hobe na
                // HOW: permitAll() = Spring Security filter skip kore, directly allow kore
                .requestMatchers("/", "/auth/login", "/auth/register", "/css/**", "/js/**", "/images/**").permitAll()
                
                // WHAT: Restricted URLs - TEACHER role charai access hobena
                // HOW: hasRole() check kore user er role ROLE_TEACHER ache kina, nahole 403 error
                .requestMatchers("/students/new", "/students/*/delete").hasRole("TEACHER")
                .requestMatchers("/teachers/new", "/teachers/*/edit", "/teachers/*/delete").hasRole("TEACHER")
                .requestMatchers("/courses/new", "/courses/*/edit", "/courses/*/delete").hasRole("TEACHER")
                .requestMatchers("/departments/new", "/departments/*/edit", "/departments/*/delete").hasRole("TEACHER")
                
                // WHAT: Baki shob URLs authenticated user ra access korte parbe
                // HOW: authenticated() = kono logged-in user hole cholbe, role matter kore na
                .anyRequest().authenticated()
            )
            
            // ==== LOGIN: Form-based authentication setup ====
            .formLogin(form -> form
                .loginPage("/auth/login")              // WHAT: Custom login page, HOW: ei URL e GET request e page dekhay
                .loginProcessingUrl("/auth/login")     // WHAT: Form submit hole ekhane POST hoy, HOW: Spring internally handle kore
                .defaultSuccessUrl("/", true)          // WHAT: Login success hole homepage e redirect
                .failureUrl("/auth/login?error=true")  // WHAT: Login fail hole login page e error message niye back
                .usernameParameter("username")         // WHAT: HTML form er input field name
                .passwordParameter("password")         // WHAT: HTML form er password field name
                .permitAll()                           // HOW: Login page publicly accessible hobei
            )
            
            // ==== LOGOUT: Session destroy kore user ke logout kore ====
            .logout(logout -> logout
                .logoutUrl("/auth/logout")                      // WHAT: Logout trigger URL
                .logoutSuccessUrl("/auth/login?logout=true")    // WHAT: Logout er por kothai jabe
                .invalidateHttpSession(true)                     // HOW: Server side session destroy kore
                .deleteCookies("JSESSIONID")                     // HOW: Browser theke session cookie remove kore
                .permitAll()
            )
            
            // ==== ERROR HANDLING: Access denied hole ki hobe ====
            // WHAT: Unauthorized access hole custom error page dekhabe
            // HOW: 403 Forbidden error catch kore /access-denied page e redirect
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            )
            
            // ==== SESSION MANAGEMENT: Concurrent login control ====
            // WHAT: Ek user ek time e sudhu 1 ta device theke login thakte parbe
            // HOW: Notun jaygay login korle purano session expire hoye jabe
            .sessionManagement(session -> session
                .maximumSessions(1)                      // Maximum 1 active session allowed
                .expiredUrl("/auth/login?expired=true")  // Session expire hole login page e back
            );

        return http.build();
    }
}
