package com.spring.app.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.spring.app.auth.JwtPrincipalDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;



/* ===== (#JWT-NOTICE-02) ===== */

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public Authentication getAuthentication(String accessToken) {

        Claims claims = parseClaims(accessToken);

        String loginId = claims.getSubject();
        String principalType = claims.get("principalType", String.class);

        Number principalNoNumber = claims.get("principalNo", Number.class);
        Long principalNo = (principalNoNumber != null) ? principalNoNumber.longValue() : null;

        String name = claims.get("name", String.class);
        String roles = claims.get("roles", String.class);
        String adminType = claims.get("adminType", String.class);

        Number hotelIdNumber = claims.get("hotelId", Number.class);
        Long hotelId = (hotelIdNumber != null) ? hotelIdNumber.longValue() : null;

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream((roles != null ? roles : "").split(","))
                      .filter(role -> role != null && !role.isBlank())
                      .map(SimpleGrantedAuthority::new)
                      .collect(Collectors.toList());

        JwtPrincipalDTO principal = JwtPrincipalDTO.builder()
                .principalType(principalType)
                .principalNo(principalNo)
                .loginId(loginId)
                .name(name)
                .adminType(adminType)
                .hotelId(hotelId)
                .roles(authorities.stream()
                                  .map(GrantedAuthority::getAuthority)
                                  .collect(Collectors.toList()))
                .build();

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);

            return true;
        }
        catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                       .verifyWith(getSigningKey())
                       .build()
                       .parseSignedClaims(token)
                       .getPayload();
        }
        catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public String resolveToken(String bearerToken) {

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}