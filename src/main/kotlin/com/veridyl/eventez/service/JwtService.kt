package com.veridyl.eventez.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}")
    private val SECRET: String,

    @Value("\${jwt.expiry}")
    private val EXPIRY: Long

) {
    private val log = LoggerFactory.getLogger(javaClass)

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
        val keyBytes = Decoders.BASE64.decode(SECRET)
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
