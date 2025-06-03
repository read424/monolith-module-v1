# Module Mailing

Módulo de mailing y notificaciones para el monolito modular, construido con Spring Boot.

## Funcionalidades
- Envío de emails transaccionales
- Plantillas de email personalizables
- Notificaciones push
- Cola de mensajes con RabbitMQ
- Gestión de templates
- Tracking de entregas

## Tecnologías
- Spring Boot
- Spring Mail
- Reactor RabbitMQ
- Thymeleaf (templates)
- JavaMailSender

## Uso en el monolito
Este módulo se integra como subtree y proporciona servicios de notificación para otros módulos.

## Configuración
- SMTP server configuration
- RabbitMQ queues para async processing
- Template engine setup
