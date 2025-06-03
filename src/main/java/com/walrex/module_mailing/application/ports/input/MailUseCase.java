package com.walrex.module_mailing.application.ports.input;

import com.walrex.module_mailing.domain.model.MailMessage;
import reactor.core.publisher.Mono;

public interface MailUseCase {
    Mono<Void> sendMail(MailMessage mailMessage);
}
