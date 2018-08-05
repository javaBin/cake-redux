package no.javazone.cake.redux.mail;

import com.sendgrid.*;
import no.javazone.cake.redux.Configuration;

import java.io.IOException;
import java.util.Optional;

public class SendGridSender implements MailSenderImplementation {
    private final MailToSend mailToSend;

    public SendGridSender(MailToSend mailToSend) {
        this.mailToSend = mailToSend;
    }

    @Override
    public void send() {
        SendGrid sg = new SendGrid(Optional.ofNullable(Configuration.sendGridKey()).orElseThrow(() -> new RuntimeException("Send grid key not set")));
        Request request = new Request();
        try {
            Email from = new Email(mailToSend.from,mailToSend.fromName);
            Content content = new Content("text/html", mailToSend.content);

            Mail mail = new Mail();

            mail.setFrom(from);
            mail.setSubject(mailToSend.subject);
            Personalization personalization = new Personalization();
            mailToSend.bcc.stream().map(Email::new).forEach(personalization::addBcc);
            mailToSend.to.stream().map(Email::new).forEach(personalization::addTo);
            mail.addPersonalization(personalization);
            mail.addContent(content);

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
