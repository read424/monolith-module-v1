package com.walrex.module_mailing.domain.service;

import com.walrex.module_mailing.application.ports.input.MailUseCase;
import com.walrex.module_mailing.application.ports.output.MailSenderPort;
import com.walrex.module_mailing.domain.model.MailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService implements MailUseCase {
    private final MailSenderPort mailSenderPort;

    @Override
    public Mono<Void> sendMail(MailMessage mailMessage) {
        log.debug("Procesando solicitud de mail para: {}", mailMessage.getTo());

        // Validación y lógica de negocio aquí
        return validateMailMessage(mailMessage)
                .flatMap(mailSenderPort::send);
    }

    private Mono<MailMessage> validateMailMessage(MailMessage message) {
        if (message.getTo() == null || message.getTo().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Destinatario no puede estar vacío"));
        }
        if (message.getSubject() == null) {
            return Mono.error(new IllegalArgumentException("Asunto no puede ser nulo"));
        }
        if (message.getTemplate() == null) {
            return Mono.error(new IllegalArgumentException("Plantilla no puede ser nula"));
        }
        return Mono.just(message);
    }
}
