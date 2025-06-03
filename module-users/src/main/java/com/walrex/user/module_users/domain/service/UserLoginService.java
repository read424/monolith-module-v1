package com.walrex.user.module_users.domain.service;

import com.walrex.user.module_users.application.ports.input.SignInUserCase;
import com.walrex.user.module_users.application.ports.output.GetUserOutputPort;
import com.walrex.user.module_users.domain.mapper.UserMapper;
import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto.LoginRequestDto;
import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto.LoginResponseDTO;
import com.walrex.user.module_users.infrastructure.adapters.security.dto.UserDetailDTO;
import com.walrex.user.module_users.infrastructure.adapters.security.jwt.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserLoginService implements SignInUserCase {
    private final GetUserOutputPort getUserOutputPort;
    private final UserDomainService userDomainService;
    private final JwtProvider jwtService;
    private final UserMapper userMapper;

    @Override
    public Mono<LoginResponseDTO> validUserPassword(LoginRequestDto request){
        return getUserOutputPort.getUserValidLogin(request)
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                .flatMap( user -> {
                    boolean isValid = userDomainService.matches(request.getPassword(), user.getPassword_user());
                    if (!isValid) {
                        return Mono.error(new RuntimeException("Credenciales inválidas"));
                    }
                    UserDetailDTO userDetailDTO = userMapper.toUserDetailDTO(user);
                    String token = jwtService.generateToken(userDetailDTO);
                    return Mono.just(new LoginResponseDTO(token));
                }).onErrorResume(BadSqlGrammarException.class, e -> {
                    return Mono.error(new RuntimeException("Error interno: consulta SQL incorrecta"));
                }).onErrorResume(Exception.class, e -> {
                    // Manejo genérico de errores
                    System.err.println("Error inesperado: " + e.getMessage());
                    return Mono.error(new RuntimeException("Error interno inesperado"));
                });
    }
}
