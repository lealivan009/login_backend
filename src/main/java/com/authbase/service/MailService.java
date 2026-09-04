package com.authbase.service;

import com.authbase.config.AuthProperties;
import com.authbase.error.ApiException;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MailService {

    private static final Logger LOG = Logger.getLogger(MailService.class);

    private final Mailer mailer;
    private final AuthProperties properties;
    private final String from;

    public MailService(
            Mailer mailer,
            AuthProperties properties,
            @ConfigProperty(name = "quarkus.mailer.from", defaultValue = "Auth Base <noreply@localhost>") String from
    ) {
        this.mailer = mailer;
        this.properties = properties;
        this.from = from;
    }

    public void sendPasswordReset(String toEmail, String rawToken) {
        String base = properties.frontendUrl().replaceAll("/$", "");
        String link = base + "/reset-password?token=" + rawToken;
        String subject = "Restablecer contraseña — Auth Base";
        String body = """
                Hola,

                Recibimos un pedido para restablecer tu contraseña.
                Abrí este enlace (válido por un tiempo limitado):

                %s

                Si no pediste este cambio, ignorá este correo.
                """.formatted(link);

        try {
            mailer.send(Mail.withText(toEmail, subject, body).setFrom(from));
            LOG.infof("Mail de restablecimiento enviado a %s", toEmail);
        } catch (Exception e) {
            LOG.errorf(e, "No se pudo enviar el mail de restablecimiento a %s", toEmail);
            throw ApiException.serviceUnavailable(
                    "MAIL_SEND_FAILED",
                    "No se pudo enviar el correo. Probá de nuevo en unos minutos."
            );
        }
    }
}
