package no.javazone.cake.redux.mail;

import no.javazone.cake.redux.Configuration;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class SmtpMailSender implements MailSenderImplementation {
    private final MailToSend mailToSend;

    public SmtpMailSender(MailToSend mailToSend) {
        this.mailToSend = mailToSend;
    }



    @Override
    public void send() {
        SimpleEmail simpleEmail = new SimpleEmail();

        try {
            simpleEmail.setFrom(mailToSend.from,mailToSend.fromName);
            for (String sendTo : mailToSend.to) {
                simpleEmail.addTo(sendTo);
            }
            for (String bcc : mailToSend.bcc) {
                simpleEmail.addBcc(bcc);
            }
            simpleEmail.setSubject(mailToSend.subject);
            simpleEmail.setContent(mailToSend.content,"text/html");

            if (Configuration.useMailSSL()) {
                simpleEmail.setSSLOnConnect(true);
                simpleEmail.setSslSmtpPort("" + Configuration.smtpPort());
            } else {
                simpleEmail.setSmtpPort(Configuration.smtpPort());

            }
            String mailUser = Configuration.mailUser();
            if (mailUser != null) {
                simpleEmail.setAuthentication(mailUser, Configuration.mailPassword());
            }


            simpleEmail.send();
        } catch (EmailException e) {
            System.out.println("Email send error " + e);
        }
    }
}
