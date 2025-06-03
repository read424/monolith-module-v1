package com.walrex.module_mailing.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MailMessage {
    private String to;
    private String subject;
    private String template;
    private Map<String, Object> variables;
}
