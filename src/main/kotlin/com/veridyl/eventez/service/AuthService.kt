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
    fun register(request: RegisterRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateResourceException("Email '${request.email}' is already taken")
        }

        val user = userRepository.save(
            AppUser(
                email = request.email,
                passwordHash = passwordEncoder.encode(request.password)!!,
                fullName = request.fullName,
                phone = request.phone,
                role = request.role,
            )
        )

        return userMapper.toUserResponse(user);
    }

    fun login(loginRequest: LoginRequest, ): LoginResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.email,
                loginRequest.password,
                )
        )
        return LoginResponse(jwtService.generateToken(loginRequest.email))
    }

    fun getAuthenticatedUser(): UserResponse {
        val user =  (SecurityContextHolder.getContext().authentication?.principal
            ?: throw RuntimeException("No authenticated user!")
            ) as UserDetails
        return userMapper.toUserResponse(user as AppUser)
    }
}
