import java.util.ArrayList;
import java.util.List;

public class User {
    String UserID;
    String Username;
    String Password;
    List userReviews  = new ArrayList();;
    public User(String userID, String username, String password) {
        UserID = userID;
        Username = username;
        Password = password;
    }

    public String getUserID() {
        return UserID;
    }

    public String getUsername() {
        return Username;
    }

    public String getPassword() {
        return Password;
    }

    @Override
    public String toString() {
        return "User{" +
                "UserID='" + UserID + '\'' +
                ", Username='" + Username + '\'' +
                ", Password='" + Password + '\'' +
                '}';
    }
}
