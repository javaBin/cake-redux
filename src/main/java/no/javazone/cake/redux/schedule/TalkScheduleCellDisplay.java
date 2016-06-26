package no.javazone.cake.redux.schedule;

public class TalkScheduleCellDisplay {
    public final String title;
    public final String ref;

    public TalkScheduleCellDisplay(String title, String ref) {
        this.title = title;
        this.ref = ref;
    }

    @Override
    public String toString() {
        return "TalkScheduleCellDisplay{" +
                "title='" + title + '\'' +
                ", ref='" + ref + '\'' +
                '}';
    }
}
