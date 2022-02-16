import org.bson.Document;
import org.iq80.leveldb.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class KeyValueManager {

    // KEY-VALUE CONFIGURATION
    // wishlist:$user_id:$wishlist_id:$product_id:$attribute_value = $value
    private DB kvDatabase = null;

    // Open K-V database
    private void openDB() {
        Options options = new Options();
        options.createIfMissing(true);
        try {
            kvDatabase = factory.open(new File("wishlist"), options);
        } catch (IOException ex) {
            closeDB();
        }
    }

    // Close K-V database
    private void closeDB() {
        try {
            if (kvDatabase != null)
                kvDatabase.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Retrieve the value correlated to the specified key (DRAFT)
    public String getValue(String userId, String wishlistId, String productId, String attributeName) {
        openDB();
        String key = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":" + attributeName;
        String value = asString(kvDatabase.get(bytes(key)));
        System.out.println(value);
        closeDB();
        return value;
    }

    // Wishlist brench
    public void browseWishlistOperations(User user, BufferedReader br) {
        browseWishlist(user);
        int wishlistId;
        String userTargetId;

        // Wishlist operations
        while (true) {
            try {
                showWishlistMenu();
                int command = Integer.parseInt(br.readLine());

                switch (command) {
                    // View wishlist
                    case 1 :
                        browseWishlist(user);
                        System.out.println("Insert wishlist ID");
                        wishlistId = Integer.parseInt(br.readLine());

                        if (user.isAdmin()) {
                            System.out.println("Insert user ID");
                            userTargetId = br.readLine();
                            adminViewWishlist(userTargetId, wishlistId);
                        }
                        else {
                            viewWishlist(user.getUserID(), wishlistId);
                        }
                        break;

                    // Show total price of a wishlist
                    case 2 :
                        browseWishlist(user);
                        System.out.println("Insert wishlist ID");
                        wishlistId = Integer.parseInt(br.readLine());
                        double total = 0;

                        if (user.isAdmin()) {
                            System.out.println("Insert user ID");
                            userTargetId = br.readLine();
                            total = adminShowTotalPrice(userTargetId, wishlistId);
                        }
                        else {
                            total = showTotalPrice(user, wishlistId);
                        }
                        System.out.println("Total price: " + total);
                        break;

                    // Delete wishlist
                    case 3 :
                        browseWishlist(user);
                        System.out.println("Insert wishlist ID");
                        wishlistId = Integer.parseInt(br.readLine());

                        if (user.isAdmin()) {
                            System.out.println("Insert user ID");
                            userTargetId = br.readLine();
                            adminDeleteWishlist(userTargetId, wishlistId);
                        }
                        else {
                            deleteWishlist(user, wishlistId);
                        }
                        break;

                    // Remove product from wishlist
                    case 4 :
                        browseWishlist(user);
                        System.out.println("Insert wishlist ID");
                        wishlistId = Integer.parseInt(br.readLine());
                        viewWishlist(user.getUserID(), wishlistId);
                        System.out.println("Insert ID of the product you want to delete");
                        String productId = br.readLine();

                        if (user.isAdmin()) {
                            System.out.println("Insert user ID");
                            userTargetId = br.readLine();
                            deleteWishlistProduct(userTargetId, wishlistId, productId);
                        }
                        else {
                            deleteWishlistProduct(user.getUserID(), wishlistId, productId);
                        }
                        break;

                    // Go Back
                    case 0 :
                        return;

                    // Invalid input
                    default :
                        System.out.println("Invalid input, try again");
                        break;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input, try again!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Show wishlist of a user
    public void browseWishlist (User user) {
        openDB();
        System.out.println("\nWishlists list: ");
        String compareKey = "wishlist:" + user.getUserID();
        String lastUserId = "";
        String lastWishlistId = "";
        try (DBIterator iterator = kvDatabase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                String[] wishlistKey = key.split(":");

                if (!wishlistKey[1].contentEquals(lastUserId) || !wishlistKey[2].contentEquals(lastWishlistId)) {
                    lastUserId = wishlistKey[1];
                    lastWishlistId = wishlistKey[2];
                    if (user.isAdmin()) {
                        System.out.println("User ID: " + wishlistKey[1] + " - Wishlist ID: " + wishlistKey[2]);
                    }
                    else {
                        if (key.contains(compareKey)) {
                            System.out.println("Wishlist ID: " + wishlistKey[2]);
                        } //else empty list?
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            closeDB();
        }
        closeDB();
    }

    // Show wishlist menu
    private void showWishlistMenu() {
        System.out.println("\nSelect an operation: ");
        System.out.println("1) View wishlist ");
        System.out.println("2) Show total price of a wishlist ");
        System.out.println("3) Delete wishlist");
        System.out.println("4) Delete product from wishlist ");
        System.out.println("0) Go Back ");
    }

    // Show the wishlist identified by wishlistId
    public void viewWishlist(String user, int wishlistId) {
        openDB();
        String compareKey = "wishlist:" + user + ":" + wishlistId;

        try (DBIterator iterator = kvDatabase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                if (key.contains(compareKey)) {
                    String value = asString(iterator.peekNext().getValue());
                    if (value.contentEquals(""))
                        value = "Not Available";
                    System.out.println(key + " = " + value);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            closeDB();
        }
    }

    // Show the wishlist of the specified user, identified by wishlistId (ADMIN OPERATION)
    public void adminViewWishlist(String userId, int wishlistId) {
        openDB();
        String compareKey = "wishlist:" + userId + ":" + wishlistId;

        try (DBIterator iterator = kvDatabase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                if (key.contains(compareKey)) {
                    String value = asString(iterator.peekNext().getValue());
                    if (value.contentEquals(""))
                        value = "Not Available";
                    System.out.println(key + " = " + value);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            closeDB();
        }
    }

    //login user from another user profile
    public void browseUserWishlist(String selectedId){
        openDB();
        System.out.println("\nWishlists list: ");
        String compareKey = "wishlist:" + selectedId;
        String lastUserId = "";
        String lastWishlistId = "";
        try (DBIterator iterator = kvDatabase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                String[] wishlistKey = key.split(":");

                if (!wishlistKey[1].contentEquals(lastUserId) || !wishlistKey[2].contentEquals(lastWishlistId)) {
                    lastUserId = wishlistKey[1];
                    lastWishlistId = wishlistKey[2];

                    if (key.contains(compareKey)) {
                        System.out.println("Wishlist ID: " + wishlistKey[2]);
                    } //else empty list?
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            closeDB();
        }
        closeDB();
    }

    public void showUserWishlists(String selectedId, BufferedReader br){
        try {
            browseUserWishlist(selectedId);
            System.out.println("Insert wishlist ID");
            int wishlistId = Integer.parseInt(br.readLine());
            viewWishlist(selectedId, wishlistId);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Insert a new key-value pair in a wishlist
    public void putValue(String userId, int wishlistId, String productId, String attributeName, String value) {
        openDB();
        String key = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":" + attributeName;
        kvDatabase.put(bytes(key), bytes(value));
        System.out.println(key + " = " + value);
        closeDB();
    }

    // Insert a product into the specified wishlist of the user
    public boolean insertInWishlist(User user, Document product, int wishlist) {
        boolean newWishlist = false;
        if (wishlist < 0 || wishlist >= user.getWishlistCount()) {
            wishlist = user.getWishlistCount();
            newWishlist = true;
        }
        String userId = user.getUserID();
        String productId = product.get("asin").toString();
        String categoriesValue = product.get("categories").toString();
        String priceValue = product.get("price").toString();
        String titleValue = product.get("title").toString();

        putValue(userId, wishlist, productId, "categories", categoriesValue);
        putValue(userId, wishlist, productId, "price", priceValue);
        putValue(userId, wishlist, productId, "title", titleValue);
        return newWishlist;
    }

    // Calculate the total price of a wishlist
    private double showTotalPrice(User user, int wishlistId) {
        openDB();
        double totalPrice = 0;
        String[] wishlistKey;
        String compareKey = "wishlist:" + user.getUserID() + ":" + wishlistId;
        try (DBIterator iterator = kvDatabase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                wishlistKey = key.split(":");
                if (key.contains(compareKey) && wishlistKey[4].contains("price")) {
                    String price = asString(iterator.peekNext().getValue());
                    totalPrice += Double.parseDouble(price);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            closeDB();
        }
        return totalPrice;
    }

    // Calculate the total price of a wishlist (ADMIN OPERATION)
    private double adminShowTotalPrice(String userId, int wishlistId) {
        openDB();
        double totalPrice = 0;
        String[] wishlistKey;
        String compareKey = "wishlist:" + userId + ":" + wishlistId;
        try (DBIterator iterator = kvDatabase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                wishlistKey = key.split(":");
                if (key.contains(compareKey) && wishlistKey[4].contains("price")) {
                    String price = asString(iterator.peekNext().getValue());
                    totalPrice += Double.parseDouble(price);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            closeDB();
        }
        return totalPrice;
    }

    // Remove a product from a specified wishlist
    public boolean deleteFromWishlist(User user, int wishlistId, String productId) {
        openDB();
        String userId = user.getUserID();
        String compareKey = "wishlist:" + userId + ":" + wishlistId + ":" + productId;
        boolean found = false;
        try (DBIterator iterator = kvDatabase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                // String[] wishlistKey = key.split(":");

                if (key.contains(compareKey)) {
                    String whislistKey = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":categories";
                    kvDatabase.delete(bytes(whislistKey));
                    whislistKey = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":title";
                    kvDatabase.delete(bytes(whislistKey));
                    whislistKey = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":price";
                    kvDatabase.delete(bytes(whislistKey));
                    found = true;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            closeDB();
        }
        return found;
    }

    // Remove a product from a specified wishlist (ADMIN OPERATION)
    public boolean adminDeleteFromWishlist(String userId, int wishlistId, String productId) {
        openDB();
        String compareKey = "wishlist:" + userId + ":" + wishlistId + ":" + productId;
        boolean found = false;
        try (DBIterator iterator = kvDatabase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                // String[] wishlistKey = key.split(":");

                if (key.contains(compareKey)) {
                    String whislistKey = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":categories";
                    kvDatabase.delete(bytes(whislistKey));
                    whislistKey = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":title";
                    kvDatabase.delete(bytes(whislistKey));
                    whislistKey = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":price";
                    kvDatabase.delete(bytes(whislistKey));
                    found = true;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            closeDB();
        }
        return found;
    }

    // Delete a key-value pair from the database
    private void deleteWishlistProduct(String userId, int wishlistId, String productId) {
        openDB();
        String key = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":categories";
        kvDatabase.delete(bytes(key));
        key = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":title";
        kvDatabase.delete(bytes(key));
        key = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":price";
        kvDatabase.delete(bytes(key));
        closeDB();
    }

    // Delete all the products of a specified wishlist
    private void deleteWishlist(User user, int wishlistId) {
        openDB();
        String compareKey = "wishlist:" + user.getUserID() + ":" + wishlistId;
        try (DBIterator iterator = kvDatabase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                if (key.contains(compareKey))
                    kvDatabase.delete(bytes(key));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            closeDB();
        }
    }

    // Delete all the products of a specified wishlist of a user (ADMIN OPERATION)
    private void adminDeleteWishlist(String userId, int wishlistId) {
        openDB();
        String compareKey = "wishlist:" + userId + ":" + wishlistId;
        try (DBIterator iterator = kvDatabase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                if (key.contains(compareKey))
                    kvDatabase.delete(bytes(key));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            closeDB();
        }
    }

}
