package no.javazone.cake.redux.whyda;

public class UserInfo {
    public final String username;
    public final String firstname;
    public final String lastname;
    public final String email;
    public final String role;


    public UserInfo(String username, String firstname, String lastname, String email, String role) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "username='" + username + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
