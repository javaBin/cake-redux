package no.javazone.cake.redux.mail;

import org.apache.commons.mail.SimpleEmail;

import javax.mail.internet.InternetAddress;
import java.util.List;

public class DummyMailSender implements MailSenderImplementation {

    private final SimpleEmail simpleEmail;

    public DummyMailSender(SimpleEmail simpleEmail) {
        this.simpleEmail = simpleEmail;
    }

    @Override
    public void send() {
        StringBuilder message = new StringBuilder("Sending mail to ");
        message.append(simpleEmail.getToAddresses().stream()
                .map(InternetAddress::getAddress)
                .reduce((a,b) -> a + ", " + b)
                .orElse("UNKNOWN"));
        message.append(" -> ");
        message.append(simpleEmail.getSubject());
        System.out.println(message);

    }
}
