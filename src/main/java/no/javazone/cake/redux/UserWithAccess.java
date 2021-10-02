package no.javazone.cake.redux;

public class UserWithAccess {
    public final String username;
    public final UserAccessType userAccessType;

    public UserWithAccess(String username, UserAccessType userAccessType) {
        this.username = username;
        this.userAccessType = userAccessType;
    }
}
