package no.javazone.cake.redux.mail;

import no.javazone.cake.redux.Configuration;
import org.apache.commons.mail.EmailException;

import java.util.LinkedList;

public class MailSenderService {
    public static MailSenderService get() {
        return new MailSenderService();
    }

    private static volatile LinkedList<MailSenderImplementation> messages = null;

    private static LinkedList<MailSenderImplementation> setupQue() {
        LinkedList<MailSenderImplementation> emailMessages = new LinkedList<>();
        long millis = Configuration.emailSleepTime();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                }
                MailSenderImplementation emailMessage;
                synchronized (messages) {
                    if (emailMessages.isEmpty()) {
                        continue;
                    }
                    emailMessage = emailMessages.poll();
                }
                emailMessage.send();

            }
        }).start();
        return emailMessages;
    }

    private static final String syncobj = "sync";

    public void sendMail(MailSenderImplementation mail) {
        if (messages == null) {
            synchronized (syncobj) {
                if (messages == null) {
                    messages = setupQue();
                }
            }
        }
        synchronized (messages) {
            messages.add(mail);
        }
    }
}
