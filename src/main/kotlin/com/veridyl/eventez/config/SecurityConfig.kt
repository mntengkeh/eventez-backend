package com.veridyl.eventez.config

import com.veridyl.eventez.config.filter.JwtAuthFilter
import com.veridyl.eventez.service.CustomUserDetailsService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import tools.jackson.databind.ObjectMapper

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val userDetailsService: CustomUserDetailsService,
    private val jwtAuthFilter: JwtAuthFilter,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/v1/auth/me").authenticated()
                    .requestMatchers("/v1/auth/**").permitAll()
                    .requestMatchers("/test/pro").hasAuthority("PROVIDER")
                    .requestMatchers("/test/pla").hasAuthority("PLANNER")
//                    .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/v1/providers", "/api/v1/providers/*").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/v1/providers/*/services").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/v1/providers/*/portfolio").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/v1/providers/*/availability").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/v1/providers/*/reviews").permitAll()
                    .anyRequest().authenticated()
            }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .sessionManagement { sess: SessionManagementConfigurer<HttpSecurity?> ->
                sess.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            }
            .exceptionHandling { exception ->
                exception.authenticationEntryPoint { request, response, authException ->
                    log.error(authException.message)
                    response.let {
                        it.contentType = MediaType.APPLICATION_JSON_VALUE
                        it.status = HttpStatus.UNAUTHORIZED.value()

                        val body: Map<String, Any> = mapOf(
                            "status" to HttpStatus.UNAUTHORIZED.value(),
                            "error" to "Unauthorised",
                            "message" to (authException.message ?: ""),
                            "path" to (request.servletPath ?: "")
                        )
                        ObjectMapper().writeValue(it.outputStream, body)
                    }
                }
            }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val authenticationProvider = DaoAuthenticationProvider(userDetailsService)
        authenticationProvider.setPasswordEncoder(passwordEncoder())
        return authenticationProvider
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}
