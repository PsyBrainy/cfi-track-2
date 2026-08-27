package com.wallet.alkemy.service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.hibernate.mapping.List;
import org.springframework.cglib.core.internal.Function;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String SECRET_KEY = "4A8EB5FEEDCBB2923F600E47AF3D32EC123D017EA8C7CD218A0D36F6D2EB4B3E";
    private static final long TOKEN_EXPIRATION = 1000 * 60 * 60 * 1; // 1 hora tarda el Token de logeo en expirar


    public String generateToken(UserDetails  userDetails) {
        Map<String, Object> claims = Map.of("authorities", userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
        );
        return generateToken(claims, userDetails.getUsername());
    }

    public String generateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact()
                ;

    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);

    }

    private Claims getAllClaims(String token) {

        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws (token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            return e.getClaims();

        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token or mal formed", e);
        }
    }

    private <T> T getClaim(String token, Function<Claims, T> claimsMapper) {
        Claims allClaims = getAllClaims(token);
        return claimsMapper.apply(allClaims);
    }

    public String getUsername(String token) {
        return getClaim(token, Claims::getSubject);

    }

//    public List<String> getAuthorities(String token) {
//        return getClaim(token, claims -> claims.get("authorities", List.class));
//    }


}
