package no.javazone.cake.redux.util;

import no.javazone.cake.redux.mail.MailSenderImplementation;
import no.javazone.cake.redux.mail.MailToSend;

public class KotlinFix {
    public MailSenderImplementation createMailImpl(MailToSend mailToSend) {
        return MailSenderImplementation.create(mailToSend);
    }
}
