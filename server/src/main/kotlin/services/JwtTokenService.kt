package services

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.*

object JwtTokenService {

    private val key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(State.credentials["JWT_SECRET"]))

    fun generateAccessToken(userId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + 15 * 60 * 1000) // 15 minutes expiry for access token
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun generateRefreshToken(userId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + 7 * 24 * 60 * 60 * 1000) // 7 days expiry for refresh token
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }
}
