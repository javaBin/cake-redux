package no.javazone.cake.redux.mail;

import no.javazone.cake.redux.Configuration;
import org.apache.commons.mail.SimpleEmail;

public interface MailSenderImplementation {
    static MailSenderImplementation create(SimpleEmail simpleEmail) {
        if ("dummy".equals(Configuration.mailSenderImplementation())) {
            return new DummyMailSender(simpleEmail);
        }
        return new SmtpMailSender(simpleEmail);
    }
    void send();
}
