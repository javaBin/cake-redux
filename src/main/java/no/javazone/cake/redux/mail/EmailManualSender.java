package no.javazone.cake.redux.mail;

import no.javazone.cake.redux.Configuration;

import java.util.Collections;

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
        MailToSend mailToSend = new MailToSend(Collections.singletonList(to), subject, content);
        MailSenderImplementation mailSenderImplementation = MailSenderImplementation.create(mailToSend);
        MailSenderService.get().sendMail(mailSenderImplementation);
    }
}
