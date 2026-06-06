package com.swetonyancelmo.focusboard.service;

import com.swetonyancelmo.focusboard.config.security.JwtService;
import com.swetonyancelmo.focusboard.dtos.request.LoginRequestDTO;
import com.swetonyancelmo.focusboard.dtos.request.RefreshTokenRequestDTO;
import com.swetonyancelmo.focusboard.dtos.request.RegisterRequestDTO;
import com.swetonyancelmo.focusboard.dtos.response.AuthResponseDTO;
import com.swetonyancelmo.focusboard.dtos.response.RegisterResponseDTO;
import com.swetonyancelmo.focusboard.exceptions.BusinessRuleException;
import com.swetonyancelmo.focusboard.exceptions.EmailAlreadyExistsException;
import com.swetonyancelmo.focusboard.model.RefreshToken;
import com.swetonyancelmo.focusboard.model.User;
import com.swetonyancelmo.focusboard.repository.RefreshTokenRepository;
import com.swetonyancelmo.focusboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${security.jwt.refresh-token-expiration-days:7}")
    private long refreshTokenExpirationDays;

    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Email já registrado");
        }

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .build();

        User saved = userRepository.save(user);
        return new RegisterResponseDTO(saved.getId(), saved.getName(), saved.getEmail());
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO dto) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        User user = (User) authentication.getPrincipal();
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponseDTO refresh(RefreshTokenRequestDTO dto) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(dto.refreshToken())
                .orElseThrow(() -> new BusinessRuleException("Refresh token inválido"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessRuleException("Refresh token expirado, faça login novamente");
        }

        return buildAuthResponse(refreshToken.getUser());
    }

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(email)
                .ifPresent(refreshTokenRepository::deleteByUser);
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = createOrReplaceRefreshToken(user);
        return AuthResponseDTO.of(accessToken, refreshToken);
    }

    private String createOrReplaceRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS))
                .build();

        refreshTokenRepository.save(token);
        return token.getToken();
    }
}