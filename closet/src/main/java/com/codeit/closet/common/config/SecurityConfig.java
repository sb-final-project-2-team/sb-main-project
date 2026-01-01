package com.codeit.closet.common.config;

import com.codeit.closet.common.security.Http401UnauthorizedEntryPoint;
import com.codeit.closet.common.security.Http403ForbiddenAccessDeniedHandler;
import com.codeit.closet.common.security.LoginFailureHandler;
import com.codeit.closet.common.security.SpaCsrfTokenRequestHandler;
import com.codeit.closet.common.security.jwt.InMemoryJwtRegistry;
import com.codeit.closet.common.security.jwt.JwtAuthenticationFilter;
import com.codeit.closet.common.security.jwt.JwtLoginSuccessHandler;
import com.codeit.closet.common.security.jwt.JwtLogoutHandler;
import com.codeit.closet.common.security.jwt.JwtRegistry;
import com.codeit.closet.common.security.jwt.JwtTokenProvider;
import com.codeit.closet.module.user.entity.UserRole;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      JwtLoginSuccessHandler jwtLoginSuccessHandler,
      JwtLogoutHandler jwtLogoutHandler,
      LoginFailureHandler loginFailureHandler,
      DaoAuthenticationProvider daoAuthenticationProvider,
      Http403ForbiddenAccessDeniedHandler forbiddenAccessDeniedHandler,
      Http401UnauthorizedEntryPoint unauthorizedEntryPoint) throws Exception {
    http
        .authenticationProvider(daoAuthenticationProvider)

        // 로그인
        .formLogin(login -> login
            .loginProcessingUrl("/api/auth/sign-in")
            .successHandler(jwtLoginSuccessHandler)
            .failureHandler(loginFailureHandler)
        )

        .logout(logout -> logout
            .logoutUrl("/api/auth/sign-out")
            .addLogoutHandler(jwtLogoutHandler)
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()))

        // CSRF 사용용 설정
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))

        // 권한 범위 허용
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll()
        )

        // 예외 처리 설정 401, 403
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(unauthorizedEntryPoint)
            .accessDeniedHandler(forbiddenAccessDeniedHandler))

        // Session사용 X STATELESS로 사용한다 JWT사용때 처리
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )

        // 필터 처리용
        .addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class
        )

        // CORS 설정 커스텀 사용
        .cors(Customizer.withDefaults())

        .httpBasic(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of("http://localhost:3000"));

    config.setAllowedMethods(List.of(
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    ));

    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Authorization"));

    config.setAllowCredentials(true); // 🔥 쿠키 사용 시 필수

    UrlBasedCorsConfigurationSource source =
        new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role(UserRole.ADMIN.name()).implies(UserRole.USER.name())
        .build();
  }

  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder,
      RoleHierarchy roleHierarchy) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    provider.setAuthoritiesMapper(new RoleHierarchyAuthoritiesMapper(roleHierarchy));
    return provider;
  }

  @Bean
  public JwtRegistry<UUID> jwtRegistry(JwtTokenProvider jwtTokenProvider) {
    return new InMemoryJwtRegistry(1, jwtTokenProvider);
  }
}
