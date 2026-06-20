package com.authmodule.service;

import com.authmodule.entity.RefreshToken;
import com.authmodule.entity.User;
import com.authmodule.exception.TokenRefreshException;
import com.authmodule.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${application.security.jwt.refresh-token.expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

   
    @Transactional
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByUser(user)
                        .orElse(new RefreshToken());

        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(
                Instant.now().plusMillis(refreshTokenDurationMs)
        );

        return refreshTokenRepository.save(refreshToken);
    }


    @Transactional
    public RefreshToken verifyExpiration(String tokenString) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new TokenRefreshException(tokenString,
                        "Refresh token not found. Please log in again."));

        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(tokenString,
                    "Refresh token has expired. Please log in again.");
        }

        return token;
    }

 
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
