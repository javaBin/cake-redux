package no.javazone.cake.redux.mail;

import no.javazone.cake.redux.Configuration;

public interface MailSenderImplementation {
    static MailSenderImplementation create(MailToSend mailToSend) {
        String mailSenderImpl = Configuration.mailSenderImplementation();
        if ("dummy".equals(mailSenderImpl)) {
            return new DummyMailSender(mailToSend,false);
        }
        if ("dummysilent".equals(mailSenderImpl)) {
            return new DummyMailSender(mailToSend,true);
        }
        if ("sendgrid".equals(mailSenderImpl)) {
            return new SendGridSender(mailToSend);
        }

        throw new RuntimeException("Unkown mail sender impl config " + mailSenderImpl);
    }
    boolean send();
}
