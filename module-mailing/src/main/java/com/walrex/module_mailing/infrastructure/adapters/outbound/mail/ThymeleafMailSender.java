package com.walrex.module_mailing.infrastructure.adapters.outbound.mail;

import com.walrex.module_mailing.application.ports.output.MailSenderPort;
import com.walrex.module_mailing.domain.model.MailMessage;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.ISpringWebFluxTemplateEngine;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class ThymeleafMailSender implements MailSenderPort {
    private final ISpringWebFluxTemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    @Override
    public Mono<Void> send(MailMessage mailMessage) {
        log.debug("Preparando mail usando plantilla: {}", mailMessage.getTemplate());

        // Procesamiento de template con Thymeleaf reactivo
        return Mono.fromCallable(() -> {
                Context context = new Context(Locale.getDefault(), mailMessage.getVariables());
                return templateEngine.process(mailMessage.getTemplate(), context);
            })
            .subscribeOn(Schedulers.boundedElastic()) // Importante para operaciones bloqueantes
            .flatMap(htmlContent ->
                Mono.fromCallable(() -> {
                    MimeMessage mimeMessage = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                    helper.setTo(mailMessage.getTo());
                    helper.setSubject(mailMessage.getSubject());
                    helper.setFrom(fromEmail);
                    helper.setText(htmlContent, true);

                    mailSender.send(mimeMessage);
                    log.info("✉️ Email enviado a: {}", mailMessage.getTo());
                    return htmlContent;
                })
                .subscribeOn(Schedulers.boundedElastic()) // Para operación bloqueante de JavaMailSender
            )
            .then();
    }
}
