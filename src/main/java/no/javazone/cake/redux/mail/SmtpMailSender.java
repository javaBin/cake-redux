package no.javazone.cake.redux.mail;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class SmtpMailSender implements MailSenderImplementation {
    private final SimpleEmail simpleEmail;

    public SmtpMailSender(SimpleEmail simpleEmail) {
        this.simpleEmail = simpleEmail;
    }

    @Override
    public void send() {
        try {
            simpleEmail.send();
        } catch (EmailException e) {
            System.out.println("Email send error " + e);
        }
    }
}
