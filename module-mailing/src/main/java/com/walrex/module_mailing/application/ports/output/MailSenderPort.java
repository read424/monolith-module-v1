package com.walrex.module_mailing.application.ports.output;

import com.walrex.module_mailing.domain.model.MailMessage;
import reactor.core.publisher.Mono;

public interface MailSenderPort {
    Mono<Void> send(MailMessage mailMessage);
}
