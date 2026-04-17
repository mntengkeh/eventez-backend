package com.veridyl.eventez.cotroller

import com.veridyl.eventez.dto.auth.AuthResponse
import com.veridyl.eventez.dto.auth.LoginRequest
import com.veridyl.eventez.dto.auth.LoginResponse
import com.veridyl.eventez.dto.auth.RegisterRequest
import com.veridyl.eventez.dto.auth.UserResponse
import com.veridyl.eventez.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    fun register(@RequestBody @Valid request: RegisterRequest): ResponseEntity<UserResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(authService.register(request))
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(authService.login(request))
    }

}
