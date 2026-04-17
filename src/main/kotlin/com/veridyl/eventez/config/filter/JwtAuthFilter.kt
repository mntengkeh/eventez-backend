package com.veridyl.eventez.config.filter

import com.veridyl.eventez.service.CustomUserDetailsService
import com.veridyl.eventez.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import java.io.IOException

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val userDetailsService: CustomUserDetailsService,
) : OncePerRequestFilter() {
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val authHeader: String? = request.getHeader("Authorization")
            if (authHeader != null) {
                var token: String? = null
                var username: String? = null

                if (authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7)
                    username = jwtService.extractUsername(token)
                }

                if ((username != null) && SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userDetailsService.loadUserByUsername(username)
                    println(userDetails.toString())
                    if (jwtService.validateToken(token)) {
                        val authToken = UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.authorities
                        )
                        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authToken
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Cannot set user authentication: {}", e)
            throw RuntimeException(e)
        }
        filterChain.doFilter(request, response)
    }
}
