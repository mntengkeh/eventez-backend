# 11. Security Configuration

Create `com.veridyl.eventez.config.SecurityConfig`.

## Access Rules Summary

| Pattern | Method | Access |
|---------|--------|--------|
| `/api/v1/auth/register` | POST | Public |
| `/api/v1/auth/login` | POST | Public |
| `/api/v1/auth/logout` | POST | Authenticated |
| `/api/v1/auth/me` | GET | Authenticated |
| `/api/v1/categories/**` | GET | Public |
| `/api/v1/providers` | GET | Public |
| `/api/v1/providers/{id}` | GET | Public |
| `/api/v1/providers/{id}/services` | GET | Public |
| `/api/v1/providers/{id}/portfolio` | GET | Public |
| `/api/v1/providers/{id}/availability` | GET | Public |
| `/api/v1/providers/{id}/reviews` | GET | Public |
| `/api/v1/providers/profile` | POST/PUT | PROVIDER |
| `/api/v1/providers/*/services` | POST | PROVIDER |
| `/api/v1/providers/services/*` | PUT/DELETE | PROVIDER |
| `/api/v1/providers/*/portfolio` | POST | PROVIDER |
| `/api/v1/providers/portfolio/*` | DELETE | PROVIDER |
| `/api/v1/providers/*/availability` | PUT | PROVIDER |
| `/api/v1/events/**` | ALL | PLANNER |
| `/api/v1/match` | POST | PLANNER |
| `/api/v1/bookmarks/**` | ALL | PLANNER |
| `/api/v1/inquiries` | POST | PLANNER |
| `/api/v1/inquiries/sent` | GET | PLANNER |
| `/api/v1/inquiries/received` | GET | PROVIDER |
| `/api/v1/inquiries/*/respond` | PUT | PROVIDER |
| `/api/v1/inquiries/*/read` | PUT | PROVIDER |
| `/api/v1/reviews` | POST | PLANNER |
| `/api/v1/reviews/*` | PUT/DELETE | PLANNER |

## Security Config Skeleton

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userDetailsService: CustomUserDetailsService
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }  // Disable for API usage; enable if using Thymeleaf forms
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/providers", "/api/v1/providers/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/providers/*/services").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/providers/*/portfolio").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/providers/*/availability").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/providers/*/reviews").permitAll()
                    // ... remaining rules per table above
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { _, response, _ ->
                    response.status = 401
                    response.contentType = "application/json"
                    response.writer.write("""{"status":401,"error":"Unauthorized","message":"Authentication required"}""")
                }
                ex.accessDeniedHandler { _, response, _ ->
                    response.status = 403
                    response.contentType = "application/json"
                    response.writer.write("""{"status":403,"error":"Forbidden","message":"Access denied"}""")
                }
            }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
```

## CustomUserDetailsService

```kotlin
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found: $email")

        return User.builder()
            .username(user.email)
            .password(user.passwordHash)
            .roles(user.role.name)       // yields ROLE_PLANNER or ROLE_PROVIDER
            .disabled(!user.enabled)
            .build()
    }
}
```
