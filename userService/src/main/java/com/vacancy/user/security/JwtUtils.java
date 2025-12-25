package com.vacancy.user.security;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    private final Key key;
    private final long expirationMs;

    public JwtUtils(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserDetails userDetails) {
        Claims claims = Jwts.claims();
        claims.put("roles", userDetails.getAuthorities().stream().map(Object::toString).toList());
        claims.setSubject(userDetails.getUsername()); // id

        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setHeader(Map.of("typ", "JWT"))
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate token. Return new Authentication if valid, null otherwise
     * 
     * @return Authentication(principal: String (userId), authorities:
     *         List<SimpleGrantedAuthority>)
     */
    public Authentication tokenToAuthentication(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            List<String> authorities = claims.get("roles", List.class);
            return new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    null,
                    authorities.stream().map(SimpleGrantedAuthority::new).toList());
        } catch (Exception e) {
            return null;
        }
    }

}
