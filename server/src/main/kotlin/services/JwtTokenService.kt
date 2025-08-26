package services

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.*

object JwtTokenService {

    private val key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(State.credentials["JWT_SECRET"]))

    fun generateAccessToken(userId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + 60 * 1000)
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("typ", "access")
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun generateRefreshToken(userId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + 120 * 1000)
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("typ", "refresh")
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun decodeToken(token: String): Jws<Claims> {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token) // Проверяет подпись и декодирует
    }
}
