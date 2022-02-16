import javax.swing.text.Document;
import java.util.ArrayList;
import java.util.List;

public class User {
    String UserID;
    String Username;
    String Password;
    ArrayList<Document> userReviews;
    int wishlistCount;
    boolean admin;

    public User(String userID, String username, String password, String count, String adm) {
        UserID = userID;
        Username = username;
        Password = password;
        userReviews = new ArrayList<>();
        wishlistCount = Integer.parseInt(count);
        admin = Boolean.parseBoolean(adm);
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

    public ArrayList<Document> getUserReviews() {
        return userReviews;
    }

    public int getWishlistCount() {
        return wishlistCount;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public void setUserReviews(ArrayList<Document> userReviews) {
        this.userReviews = userReviews;
    }

    public void setWishlistCount(int wishlistCount) {
        this.wishlistCount = wishlistCount;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public String toString() {
        return "User{" +
                "UserID='" + UserID + '\'' +
                ", Username='" + Username + '\'' +
                ", Password='" + Password + '\'' +
                ", userReviews=" + userReviews +
                ", wishlistCount=" + wishlistCount +
                ", admin=" + admin +
                '}';
    }
}