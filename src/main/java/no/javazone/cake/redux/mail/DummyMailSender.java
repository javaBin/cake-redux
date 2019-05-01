package no.javazone.cake.redux.mail;

import org.apache.commons.mail.SimpleEmail;

import javax.mail.internet.InternetAddress;
import java.util.List;

public class DummyMailSender implements MailSenderImplementation {

    private final MailToSend mailToSend;

    public DummyMailSender(MailToSend mailToSend) {
        this.mailToSend = mailToSend;
    }

    @Override
    public void send() {
        StringBuilder message = new StringBuilder("Sending mail to ");
        message.append(mailToSend.to.stream()
                .reduce((a,b) -> a + ", " + b)
                .orElse("UNKNOWN"));
        message.append(" -> ");
        message.append(mailToSend.subject);
        System.out.println(message);
        System.out.println(mailToSend.content);


    }
}
