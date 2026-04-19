package com.veridyl.eventez.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {
    private val log = LoggerFactory.getLogger(javaClass)
    companion object {
        const val SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437"
        const val EXPIRY = 24 * 60* 60 * 1000L
    }

    fun generateToken(userName: String): String {
        val claims: Map<String, Any> = HashMap()
        return createToken(claims, userName)
    }

    fun createToken(extraClaims: Map<String, Any>, username: String): String {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + EXPIRY))
            .signWith(getSignInKey())         // Algorithm is inferred from key type
            .compact()
    }

    private fun getSignInKey(): SecretKey {
        val keyBytes = SECRET.toByteArray(Charsets.UTF_8)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    fun extractExpiration(token: String): Date {
        return extractAllClaims(token).expiration
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSignInKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun isTokenExpired(token: String?): Boolean {
        if (token != null) {
            return extractExpiration(token).before(Date())
        } else {
            throw RuntimeException("no token")
        }
    }

    fun validateToken(token: String?): Boolean {
        return (!isTokenExpired(token))
    }

}
