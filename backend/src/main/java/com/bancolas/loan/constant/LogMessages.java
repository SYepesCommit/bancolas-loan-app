package com.bancolas.loan.constant;

public final class LogMessages {

    private LogMessages() {
    }

    public static final String LOAN_PROCESSING = "Procesando solicitud de préstamo. Referencia: {}, Cliente: {}, Monto: {}";
    public static final String LOAN_APPROVED = "Capacidad descontada con éxito. Aprobando solicitud {}";
    public static final String LOAN_REJECTED_CAPACITY = "Capacidad insuficiente para el cliente {}. Rechazando.";
    public static final String LOAN_REJECTED_BLOCKED = "Cliente {} bloqueado. Rechazando solicitud.";
    public static final String LOAN_REJECTED_NOT_FOUND = "Cliente {} no existe en el sistema.";
    public static final String LOAN_IDEMPOTENT_RESPONSE = "Solicitud previamente procesada encontrada. Retornando respuesta idempotente para referencia {}";
    public static final String RABBIT_EVENT_RECEIVED = "[RabbitMQ] Mensaje recibido de la cola - Referencia: {} | Cliente: {} | Estado: {}";
    public static final String RABBIT_APPROVED_SIMULATION = "Simulación: Enviando notificación de aprobación al cliente {}";
    public static final String RABBIT_REJECTED_SIMULATION = "Simulación: Registrando alerta de crédito rechazado por: {}";
    public static final String RABBIT_EVENT_PUBLISHED = "Evento publicado en RabbitMQ para la referencia: {}";
    public static final String RABBIT_EVENT_ERROR = "Error al publicar evento en RabbitMQ para la referencia {}: {}";


    public static final String RABBIT_PROCESSING_EMAIL = "[RabbitMQ - Notification Queue] Procesando envío de correo para la referencia: {}";
    public static final String RABBIT_EMAIL_ERROR = "Error al enviar el correo para la referencia {}: {}";
    public static final String EMAIL_SENT_SUCCESS = "Correo enviado con éxito a: {}";


    public static final String EMAIL_SUBJECT_STATUS = "Estado de tu Solicitud de Crédito: %s";
    public static final String EMAIL_BODY_APPROVED = "¡Felicidades! Tu solicitud de préstamo %s por un monto de $%.2f COP ha sido APROBADA con éxito.";
    public static final String EMAIL_BODY_REJECTED = "Lamentamos informarte que tu solicitud %s fue RECHAZADA. Motivo: %s";
    public static final String MOCK_EMAIL_DOMAIN = "@correo.com";
}