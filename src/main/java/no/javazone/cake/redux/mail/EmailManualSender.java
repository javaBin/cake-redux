package no.javazone.cake.redux.mail;

import no.javazone.cake.redux.AcceptorSetter;
import no.javazone.cake.redux.Configuration;
import org.apache.commons.mail.SimpleEmail;

public class EmailManualSender {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage configfile address");
            return;
        }
        Configuration.setConfigFile(args);
        String content = "<html><body><h1>Mail er her</h1><p>Min mailparagraf</p>";
        String subject = "Dette er mailtest";
        send(args[1],content,subject);
    }

    public static void send(String to,String content,String subject) throws Exception{
        SimpleEmail simpleEmail = new SimpleEmail();
        simpleEmail.setContent(content,"text/html");
        AcceptorSetter.setupMailHeader(simpleEmail,subject);
        simpleEmail.addTo(to);
        MailSenderImplementation mailSenderImplementation = MailSenderImplementation.create(simpleEmail);
        MailSenderService.get().sendMail(mailSenderImplementation);
    }
}
