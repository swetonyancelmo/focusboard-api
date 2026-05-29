package com.swetonyancelmo.focusboard.service;

import com.swetonyancelmo.focusboard.config.security.JwtService;
import com.swetonyancelmo.focusboard.dtos.request.LoginRequestDTO;
import com.swetonyancelmo.focusboard.dtos.request.RefreshTokenRequestDTO;
import com.swetonyancelmo.focusboard.dtos.request.RegisterRequestDTO;
import com.swetonyancelmo.focusboard.dtos.response.AuthResponseDTO;
import com.swetonyancelmo.focusboard.dtos.response.RegisterResponseDTO;
import com.swetonyancelmo.focusboard.model.RefreshToken;
import com.swetonyancelmo.focusboard.model.User;
import com.swetonyancelmo.focusboard.repository.RefreshTokenRepository;
import com.swetonyancelmo.focusboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;

    @Autowired
    public void setAuthenticationManager(@Lazy AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + username));
    }

    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("Email já registrado");
        }

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));

        User userSaved = userRepository.save(user);
        return new RegisterResponseDTO(userSaved.getId(), userSaved.getName(), userSaved.getEmail());
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + dto.email()));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = createOrReplaceRefreshToken(user);

        return new AuthResponseDTO(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponseDTO refresh(RefreshTokenRequestDTO dto) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(dto.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token expirado, faça login novamente");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = createOrReplaceRefreshToken(user);

        return new AuthResponseDTO(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public String createOrReplaceRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getToken();
    }
}
