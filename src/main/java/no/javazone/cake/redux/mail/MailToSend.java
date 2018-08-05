package no.javazone.cake.redux.mail;

import java.util.Collections;
import java.util.List;

public class MailToSend {
    public final String from;
    public final String fromName;
    public final List<String> bcc;
    public final List<String> to;
    public final String subject;
    public final String content;

    public MailToSend(String from, String fromName, List<String> bcc, List<String> to, String subject, String content) {
        this.from = from;
        this.fromName = fromName;
        this.bcc = bcc;
        this.to = to;
        this.subject = subject;
        this.content = content;
    }

    public MailToSend(List<String> to, String subject, String content) {
        this("program@java.no", "Javazone program commitee", Collections.singletonList("program-auto@java.no"),to,subject,content);
    }
}
