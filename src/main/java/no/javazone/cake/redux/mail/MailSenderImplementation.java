package no.javazone.cake.redux.mail;

import no.javazone.cake.redux.Configuration;
import org.apache.commons.mail.SimpleEmail;

public interface MailSenderImplementation {
    static MailSenderImplementation create(MailToSend mailToSend) {
        if ("dummy".equals(Configuration.mailSenderImplementation())) {
            return new DummyMailSender(mailToSend,false);
        }
        if ("dummysilent".equals(Configuration.mailSenderImplementation())) {
            return new DummyMailSender(mailToSend,true);
        }
        if ("sendgrid".equals(Configuration.mailSenderImplementation())) {
            return new SendGridSender(mailToSend);
        }

        return new SmtpMailSender(mailToSend);
    }
    boolean send();
}
