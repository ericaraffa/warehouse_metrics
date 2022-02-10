import org.iq80.leveldb.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;

import java.io.File;
import java.io.IOException;

public class KeyValueManager {

    private DB kvDatabase = null;

    // Open K-V database
    private void openDB() {
        Options options = new Options();
        options.createIfMissing(true);
        try {
            kvDatabase = factory.open(new File("warehouse_metrics"), options);
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

    // Delete a key-value pair from the database (DRAFT)
    public void deleteValue(String userId, String wishlistId, String productId, String attributeName) {
        openDB();
        String key = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":" + attributeName;
        kvDatabase.delete(bytes(key));
        closeDB();
    }

    // Browse wishlists
    public void browseWishlist() {
        openDB();
        try (DBIterator iterator = kvDatabase.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                System.out.println(key);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            closeDB();
        }
    }

    // Show the wishlists of the specified user
    public void viewWishlist(String userId) {
        openDB();
        String compareKey = "wishlist:" + userId;
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

    // Insert a product into a wishlist of the user TESTING
    public void insertInWishlist(String userId, String wishlistId, String productId, String attributeName, String value) {
        openDB();
        String key = "wishlist:" + userId + ":" + wishlistId + ":" + productId + ":" + attributeName;
        System.out.println(key);
        kvDatabase.put(bytes(key), bytes(value));
        closeDB();
    }

}
