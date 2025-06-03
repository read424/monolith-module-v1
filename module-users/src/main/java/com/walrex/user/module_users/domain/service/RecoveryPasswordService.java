package com.walrex.user.module_users.domain.service;

import com.walrex.user.module_users.application.ports.input.RecoveryPasswordUseCase;
import com.walrex.user.module_users.application.ports.output.PasswordRecoveryProducer;
import com.walrex.user.module_users.application.ports.output.PasswordRecoveryRepository;
import com.walrex.user.module_users.application.ports.output.UserOutputPort;
import com.walrex.user.module_users.domain.exception.EmailNotFoundException;
import com.walrex.user.module_users.domain.model.PasswordRecoveryData;
import com.walrex.user.module_users.domain.model.PasswordRecoveryToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveryPasswordService  implements RecoveryPasswordUseCase {
    private final UserOutputPort userRepository;
    private final PasswordRecoveryRepository recoveryRepository;
    private final PasswordRecoveryProducer recoveryProducer;
    private final TotpService totpService;

    @Override
    public Mono<Void> initiatePasswordRecovery(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new EmailNotFoundException("Email no encontrado")))
                .flatMap(user -> {
                    // Generar código TOTP de 6 dígitos
                    String recoveryCode = totpService.generateRecoveryCode();

                    // Crear token de recuperación (expiración en 10 minutos)
                    PasswordRecoveryToken token = PasswordRecoveryToken.builder()
                            .userId(user.getIdUsuario())
                            .email(email)
                            .code(recoveryCode)
                            .expiresAt(LocalDateTime.now().plusMinutes(10))
                            .build();
                    // Guardar token en repositorio
                    return recoveryRepository.saveRecoveryCode(token)
                            .map(saveToken -> PasswordRecoveryData.builder()
                                    .userId(user.getIdUsuario())
                                    .username(user.getUsername())
                                    .email(user.getEmail())
                                    .recoveryCode(recoveryCode)
                                    .build()

                            );
                })
                .flatMap(data -> {
                   return recoveryProducer.sendPasswordRecoveryEvent(data);
                });
    }
}
