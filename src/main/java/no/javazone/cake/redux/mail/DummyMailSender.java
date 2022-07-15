package no.javazone.cake.redux.mail;


public class DummyMailSender implements MailSenderImplementation {

    private final MailToSend mailToSend;
    private final boolean isSilent;
    public DummyMailSender(MailToSend mailToSend,boolean isSilent) {
        this.mailToSend = mailToSend;
        this.isSilent = isSilent;
    }

    @Override
    public boolean send() {
        StringBuilder message = new StringBuilder("Sending mail to ");
        message.append(mailToSend.to.stream()
                .reduce((a,b) -> a + ", " + b)
                .orElse("UNKNOWN"));
        message.append(" -> ");
        message.append(mailToSend.subject);
        if (!isSilent) {
            System.out.println(message);
            System.out.println(mailToSend.content);
        }
        return true;

    }
}
