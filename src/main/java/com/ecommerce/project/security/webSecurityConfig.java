package com.ecommerce.project.security;

import com.ecommerce.project.entity.AppRole;
import com.ecommerce.project.entity.Role;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.repository.RoleRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.security.jwt.AuthTokenFilter;
import com.ecommerce.project.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import com.ecommerce.project.security.jwt.AuthEntryPointJwt;

import javax.sql.DataSource;
import java.util.Set;

@Configuration
@EnableWebSecurity
//@EnableMethodSecurity
public class webSecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(this.userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;

    }


    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //request starts with url you don't want to authenticate by jwt below method is used
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable).exceptionHandling(exception -> {

                    exception.authenticationEntryPoint(unauthorizedHandler);
                }).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/**").permitAll()
                                 .requestMatchers("/v3/api-docs/**").permitAll()
                                 //.requestMatchers("/api/admin/**").permitAll()
                               //  .requestMatchers("/api/public/**").permitAll()
                                 .requestMatchers("/swagger-ui/**").permitAll()
                                 .requestMatchers("/api/test/**").permitAll()
                                 .requestMatchers("/images/**").permitAll()
                                 .anyRequest().authenticated()


                 );

          /*      .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**",
                        "/api/test/**", "/images/**").permitAll().
                        requestMatchers("/api/auth").hasAuthority("ROLE_USER").
                        requestMatchers("/api/auth").hasAuthority("ROLE_ADMIN").
                        requestMatchers("/api/auth").hasAuthority("ROLE_SELLER"));*/


        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        http.headers(headers -> {
            headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
        });

        return http.build();
    }

    /*
      The WebSecurityCustomizer interface in Java, particularly in the context of Spring Security,
      is used to customize the security settings for web applications. It provides a way to
      define specific rules and configurations for handling security aspects,
      such as which URLs should be ignored by security filters, thereby allowing
      for more flexible and granular security policies.

       Key Uses of WebSecurityCustomizer:
       Ignoring Security for Specific Paths: You can specify which URLs should not be
       secured by Spring Security. This is particularly useful for allowing public access
       to static resources (like images or CSS files) or certain endpoints
       (like login or error pages).

       Customization of Security Filters: It allows you to customize how and when certain
       security filters are applied. This can help optimize performance and reduce unnecessary
       checks on public endpoints.

       Configuration of Security Rules: By using WebSecurityCustomizer,
        you can define rules that can adapt based on application requirements,
        such as enabling or disabling security features for specific environments.
   */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().requestMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/security", "/swagger-ui.html", "/webjars/**"));
    }

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Retrieve or create roles
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseGet(() -> {
                Role newUserRole = new Role(AppRole.ROLE_USER);
                return roleRepository.save(newUserRole);
            });

            Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER).orElseGet(() -> {
                Role newSellerRole = new Role(AppRole.ROLE_SELLER);
                return roleRepository.save(newSellerRole);
            });

            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN).orElseGet(() -> {
                Role newAdminRole = new Role(AppRole.ROLE_ADMIN);
                return roleRepository.save(newAdminRole);
            });

            Set<Role> userRoles = Set.of(userRole);
            Set<Role> sellerRoles = Set.of(sellerRole);
            Set<Role> adminRoles = Set.of(userRole, sellerRole, adminRole);


            // Create users if not already present
            if (!userRepository.existsByUsername("user1") && !userRepository.existsByEmail("user1@example.com")) {
                User user1 = new User("user1", passwordEncoder.encode("password1"), "user1@example.com");
                userRepository.save(user1);
            }

            if (!userRepository.existsByUsername("seller1") && !userRepository.existsByEmail("seller1@example.com")) {
                User seller1 = new User("seller1", passwordEncoder.encode("password2"), "seller1@example.com");
                userRepository.save(seller1);
            }

            if (!userRepository.existsByUsername("admin") && !userRepository.existsByEmail("admin@example.com")) {
                User admin = new User("admin", passwordEncoder.encode("adminPass"), "admin@example.com");
                userRepository.save(admin);
            }

            // Update roles for existing users
            userRepository.findByUsername("user1").ifPresent(user -> {
                user.setRoles(userRoles);
                userRepository.save(user);
            });

            userRepository.findByUsername("seller1").ifPresent(seller -> {
                seller.setRoles(sellerRoles);
                userRepository.save(seller);
            });

            userRepository.findByUsername("admin").ifPresent(admin -> {
                admin.setRoles(adminRoles);
                userRepository.save(admin);
            });
        };
    }


}


