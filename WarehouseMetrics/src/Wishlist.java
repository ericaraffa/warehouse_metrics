

public class Wishlist {
    String userID;
    String wishlistID;
    String productID;
    String categories;
    double price;
    String title;

    public Wishlist(String userID, String wishlistID, String productID, String categories, double price, String title) {
        this.userID = userID;
        this.wishlistID = wishlistID;
        this.productID = productID;
        this.categories = categories;
        this.price = price;
        this.title = title;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getWishlistID() {
        return wishlistID;
    }

    public void setWishlistID(String wishlistID) {
        this.wishlistID = wishlistID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Wishlist{" +
                "userID='" + userID + '\'' +
                ", wishlistID='" + wishlistID + '\'' +
                ", productID='" + productID + '\'' +
                ", categories='" + categories + '\'' +
                ", price=" + price +
                ", title='" + title + '\'' +
                '}';
    }
}
