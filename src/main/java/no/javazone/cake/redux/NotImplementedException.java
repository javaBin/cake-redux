package no.javazone.cake.redux;

public class NotImplementedException extends RuntimeException {
    public NotImplementedException() {
        super("This functionality is not supported as of now");
    }
}
