package com.veridyl.eventez.service

import com.veridyl.eventez.dto.auth.LoginRequest
import com.veridyl.eventez.dto.auth.LoginResponse
import com.veridyl.eventez.dto.auth.RegisterRequest
import com.veridyl.eventez.dto.auth.UserResponse
import com.veridyl.eventez.entity.AppUser
import com.veridyl.eventez.entity.enums.UserRole
import com.veridyl.eventez.exception.DuplicateResourceException
import com.veridyl.eventez.mapper.AppUserMapper
import com.veridyl.eventez.repository.UserRepository
import org.apache.coyote.BadRequestException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userMapper: AppUserMapper,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun register(request: RegisterRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateResourceException("Email '${request.email}' is already taken")
        }

        val role = try {
            UserRole.valueOf(request.role.toString().trim().uppercase())
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("Bad role")
        }

        val user = userRepository.save(
            AppUser(
                email = request.email,
                passwordHash = passwordEncoder.encode(request.password)
                    ?: throw IllegalStateException("Password encoding failed"),
                fullName = request.fullName,
                phone = request.phone,
                role = role,
            )
        )

        return userMapper.toUserResponse(user);
    }

    fun login(loginRequest: LoginRequest, ): LoginResponse {
        val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.email,
                    loginRequest.password,
                )
            )
        if (authentication.isAuthenticated) {
            return LoginResponse(jwtService.generateToken(loginRequest.email))
        } else {
            logger.debug("Bad credentials for: {}", loginRequest.email)
            throw UsernameNotFoundException("Invalid login request")
        }
    }

    fun getAuthenticatedUser(): UserDetails {
        return (SecurityContextHolder.getContext().authentication?.principal
            ?: throw RuntimeException("No authenticated user!")
            ) as UserDetails
    }
}
