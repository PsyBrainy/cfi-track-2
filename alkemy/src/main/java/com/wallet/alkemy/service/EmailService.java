package com.wallet.alkemy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender; 


    public void sendActivationEmail(String toEmail, String producerName, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("no-reply@alkywall.com");
            helper.setTo(toEmail);
            helper.setSubject(" Alkywall • Credenciales de Activación de Cuenta");

            String htmlContent
                    = "<div style=\"background-color: #F3F4F6; padding: 40px 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; min-height: 100%;\">"
                    + "  <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px; background-color: #ffffff; border: 1px solid #E5E7EB; border-radius: 24px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02); text-align: left;\">"
                    + "    "
                    + "    <!-- ENCABEZADO CORPORATIVO (Azul Oscuro Alkywall) -->"
                    + "    <tr>"
                    + "      <td style=\"padding: 40px; background-color: #111C3A; text-align: center;\">"
                    + "        <h1 style=\"margin: 0; color: #FFFFFF; font-size: 28px; font-weight: 800; letter-spacing: -0.5px;\">Alkywall</h1>"
                    + "        <p style=\"margin: 6px 0 0 0; color: #0F766E; font-size: 11px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase;\">Resumen de Cuenta & Seguridad</p>"
                    + "      </td>"
                    + "    </tr>"
                    + "    "
                    + "    <!-- CUERPO PRINCIPAL DEL CORREO -->"
                    + "    <tr>"
                    + "      <td style=\"padding: 40px;\">"
                    + "        <p style=\"margin: 0 0 8px 0; font-size: 13px; font-weight: 700; color: #0F766E; letter-spacing: 1.5px; text-transform: uppercase;\">"
                    + "          ¡Hola, " + producerName + "!"
                    + "        </p>"
                    + "        <h2 style=\"margin: 0 0 16px 0; color: #111C3A; font-size: 22px; font-weight: 800; letter-spacing: -0.5px;\">"
                    + "          Activación de Billetera Virtual"
                    + "        </h2>"
                    + "        <p style=\"color: #6B7280; font-size: 15px; line-height: 1.6; margin: 0 0 30px 0;\">"
                    + "          Tu registro en nuestra plataforma de gestión financiera ha sido procesado con éxito. Para comenzar a mover tu dinero y monitorear tus activos en tiempo real, es necesario que verifiques tu identidad mediante el siguiente enlace."
                    + "        </p>"
                    + "        "
                    + "        <!-- BOTÓN DE ACCIÓN PRINCIPAL (Verde Institucional) -->"
                    + "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"margin-bottom: 35px;\">"
                    + "          <tr>"
                    + "            <td align=\"center\">"
                    + "              <a href=\"http://localhost:8080/api/auth/activate?token=" + token + "\" target=\"_blank\" style=\"background-color: #0F766E; color: #FFFFFF; font-size: 15px; font-weight: 700; text-decoration: none; padding: 16px 32px; border-radius: 12px; display: inline-block; transition: all 0.2s ease-in-out; box-shadow: 0 4px 12px rgba(15, 118, 110, 0.15);\">"
                    + "                Confirmar Correo Electrónico"
                    + "              </a>"
                    + "            </td>"
                    + "          </tr>"
                    + "        </table>"
                    + "        "
                    + "        <!-- PANEL DETALLE TÉCNICO (Copia de Metric Card de tu Web) -->"
                    + "        <div style=\"background-color: #FFFFFF; border: 1px solid #E5E7EB; border-radius: 16px; padding: 24px; margin-bottom: 30px;\">"
                    + "          <p style=\"margin: 0 0 12px 0; color: #111C3A; font-size: 13px; font-weight: 700; letter-spacing: 0.5px; text-transform: uppercase;\">"
                    + "            Información de Seguridad"
                    + "          </p>"
                    + "          <p style=\"margin: 0 0 8px 0; color: #374151; font-size: 14px; font-weight: 500;\">"
                    + "            <strong style=\"color: #6B7280;\">Identificador:</strong> " + toEmail + ""
                    + "          </p>"
                    + "          <p style=\"margin: 0; color: #374151; font-size: 14px; font-weight: 500;\">"
                    + "            <strong style=\"color: #6B7280;\">Estado Inicial:</strong> "
                    + "            <span style=\"font-size: 11px; padding: 4px 8px; border-radius: 6px; font-weight: 700; background-color: #FEF7E0; color: #B06000; text-transform: uppercase;\">Pendiente</span>"
                    + "          </p>"
                    + "        </div>"
                    + "        "
                    + "        <!-- AVISO DE VENCIMIENTO -->"
                    + "        <p style=\"color: #9CA3AF; font-size: 12px; line-height: 1.5; margin: 0; text-align: center;\">"
                    + "           Por razones de seguridad del entorno, este enlace de validación caducará automáticamente en las próximas 24 horas."
                    + "        </p>"
                    + "      </td>"
                    + "    </tr>"
                    + "    "
                    + "    <!-- PIE DE PÁGINA (Estilo Corporativo Limpio) -->"
                    + "    <tr>"
                    + "      <td style=\"padding: 24px 40px; background-color: #FFFFFF; border-top: 1px solid #F3F4F6; text-align: center;\">"
                    + "        <p style=\"margin: 0; color: #9CA3AF; font-size: 11px; letter-spacing: 0.5px;\">"
                    + "          © 2026 Alkywall Inc. Argentina. Todos los derechos reservados.<br>"
                    + "          Este es un correo automático, por favor no respondas a esta dirección."
                    + "        </p>"
                    + "      </td>"
                    + "    </tr>"
                    + "  </table>"
                    + "</div>";

            // Inyectamos el texto HTML mapeado en la petición de red
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {

            // 🛡️ REGLA SOBERANA DE PRODUCCIÓN: Lanzamos el error para que @Transactional haga el Rollback en Postgres
            throw new RuntimeException("No se pudo despachar el correo electrónico de bienvenida. Registro cancelado.");
        }
    }
}
