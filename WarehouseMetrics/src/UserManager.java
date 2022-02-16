import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Updates.set;

public class UserManager {
    ConnectionString uri = null;
    MongoClient myClient = null;
    MongoDatabase conn_db = null;
    MongoCollection<Document> collection = null;

    // Create connection to MongoDB
    public void openDB() {
        uri = new ConnectionString("mongodb://localhost:27017");
        myClient = MongoClients.create(uri);
        conn_db = myClient.getDatabase("warehouse_metrics");
    }

    // Close the connection to MongoDB
    public void closeDB() {
        myClient.close();
    }

    // Login User
    public User loginUser(BufferedReader br){
        openDB();
        collection = conn_db.getCollection("Users");
        // Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());
        Document userDoc = null;
        User user = null;

        try {
            System.out.println("Insert an Username");
            String username = br.readLine();
            System.out.println("Insert a Password");
            String password = br.readLine();
            Bson matchProduct = match(and(eq("username", username),eq("pw", password)));
            if (collection.aggregate(Arrays.asList(matchProduct)).first()!=null) {
                userDoc = collection.aggregate(Arrays.asList(matchProduct)).first();
                // System.out.println(userDoc.toJson());
                user = new User(userDoc.get("userID").toString(), userDoc.get("username").toString(), userDoc.get("pw").toString(), userDoc.get("wishlistCounter").toString(), userDoc.get("Admin").toString());
                System.out.println("__________________________________________");
                System.out.println("User " + username + " logged in succesfully");
                System.out.println("__________________________________________");
            }
            else {
                System.out.println("__________________________________________");
                System.out.println("Login Failed");
                System.out.println("__________________________________________");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (myClient != null)
                    closeDB();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return user;
    }

    // Register a new user
    public User registerUser(BufferedReader br)
    {
        openDB();
        collection = conn_db.getCollection("Users");
        User user = null;

        try {
            System.out.println("\nInsert an Username:");
            String username = br.readLine();

            System.out.println("\nInsert a Password:");
            String password = br.readLine();

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(username.getBytes());
            String userID = hash(username + password);

            Document userDoc = new Document("userID", userID)
                    .append("username", username)
                    .append("pw", password)
                    .append("reviews", new ArrayList<Document>())
                    .append("Admin", false)
                    .append("wishlistCounter", 0);

            collection.insertOne(userDoc);
            user = new User(userID, username, password, "0", "false");
            System.out.println("Registration of user " + username + " successful!");
            System.out.println(user);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return user;
    }

    // Hash function
    public static String hash(String string) {
        long h = 1125899906842597L;
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31*h + string.charAt(i);
        }
        if(h<0)
            return (""+(h*-1)).substring(0,13);
        return (""+h).substring(0,13);
    }

    // Update wishlistCounter of the specified product (UTILITY)
    private void updateWishlistCounter() {
        openDB();
        collection = conn_db.getCollection("Users");

        collection.updateMany(regex("userID", ""), set("wishlistCounter", 0));
        //System.out.println("Products updated: " + updateResult.getModifiedCount());
        closeDB();
    }

    // Retrive reviews of a specified product
    public ArrayList<Document> getReviews(String userId) {
        //openDB();
        MongoCollection<Document> reviewCollection = conn_db.getCollection("Reviews");
        ArrayList<Document> resultReviews = new ArrayList<>();

        try (MongoCursor<Document> cursor = reviewCollection.find(eq("reviewerID", userId)).iterator()) {
            while (cursor.hasNext()) {
                Document reviewDoc = cursor.next();
                if(reviewDoc == null)
                    return null;
                resultReviews.add(reviewDoc);
                // System.out.println(reviewDoc.toJson());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            resultReviews = null;
        }
        //closeDB();
        return resultReviews;
    }

    // Initialize "reviews" attribute of each user (UTILITY)
    public void updateUserReviews() {
        openDB();
        collection = conn_db.getCollection("Users");
        ArrayList<Document> resultReviews = new ArrayList<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document userDoc = cursor.next();
                String userId = userDoc.get("userID").toString();
                resultReviews = getReviews(userId);
                collection.updateOne(eq("userID", userId), set("reviews", resultReviews));
                // System.out.println("User updated: " + updateResult.getModifiedCount());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        closeDB();
    }

    // Update number of wishlist of a user in the DB
    public void updateUserWishlistCount(User user) {
        openDB();
        collection = conn_db.getCollection("Users");
        int nextWishlist = user.getWishlistCount();
        user.setWishlistCount(nextWishlist+1);
        collection.updateOne(eq("userID", user.getUserID()), set("wishlistCounter", user.getWishlistCount()));
        System.out.println("Wishlist " + nextWishlist + " created!");
        closeDB();
    }

    // Browse Users
    public void browseUsers(BufferedReader br) {
        // Show list of users
        showUsersList();

        // User menu
        while (true) {
            try {
                showUserMenu();
                int command = Integer.parseInt(br.readLine());

                switch (command) {
                    // View User
                    case 1 :
                        viewUser(br);
                        break;

                    // Go Back
                    case 0 :
                        return;

                    // Invalid input
                    default :
                        System.out.println("Invalid input, try again!");
                        break;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input, try again!");
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (myClient != null)
                        closeDB();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void showUsersList() {
        openDB();
        collection = conn_db.getCollection("Users");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());
        Bson projectProducts = project(fields(include("username"), computed("ID","$userID"), excludeId()));
        collection.aggregate(Arrays.asList(projectProducts)).forEach(printDocuments);
        closeDB();
    }

    // User brench menu
    public void showUserMenu() {
        System.out.println("\nSelect an operation: ");
        System.out.println("1) Search Username");
        System.out.println("0) Go Back ");
    }

    //Search user by username
    public void viewUser(BufferedReader br) {
        openDB();
        collection = conn_db.getCollection("Users");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        try {
            System.out.println("Insert an Username");
            String username = br.readLine();
            Bson matchProduct = match(eq("username", username));
            collection.aggregate(Arrays.asList(matchProduct)).forEach(printDocuments);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (myClient != null)
                    closeDB();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Delete a user from database
    public void deleteUser(String userId) {
        openDB();
        collection = conn_db.getCollection("Users");

        DeleteResult deleteResult = collection.deleteOne(eq("userID", userId));
        if (deleteResult.getDeletedCount() == 1) {
            // Delete also from K-V database
            KeyValueManager kv = new KeyValueManager();
            kv.adminDeleteUser(userId);
            System.out.println("User " + userId + " deleted! ");
        }
        else {
            System.out.println("Error occured deleting user " + userId + ", try again!");
        }
        closeDB();


    }

    // Admin branch
    public void adminZone(BufferedReader br) {
        ProductManager productManager = new ProductManager();
        String targetUserId;
        // Admin menu
        while (true) {
            try {
                showAdminMenu();
                int command = Integer.parseInt(br.readLine());

                switch (command) {
                    // Add product
                    case 1 :
                        boolean added = productManager.insertProduct(br);
                        if (!added)
                            System.out.println("Error, try again!");
                        break;

                    // Delete product
                    case 2 :
                        // Show list of products
                        productManager.showProducts();
                        System.out.println("Insert a product ID: ");
                        String productId = br.readLine();
                        productManager.deleteProduct(productId);
                        break;

                    // Add user
                    case 3 :
                        if (registerUser(br) == null)
                            System.out.println("Error in user registration, try again!");
                        break;

                    // Delete user
                    case 4 :
                        // Show list of users
                        showUsersList();
                        System.out.println("\nInsert a userID: ");
                        targetUserId = br.readLine();
                        deleteUser(targetUserId);
                        break;

                    // Promote/demote user
                    case 5 :
                        // Show list of users
                        showUsersList();
                        System.out.println("\nInsert a userID: ");
                        targetUserId = br.readLine();
                        System.out.println("\nDo you want to promote or demote the selected user? ");
                        System.out.println("1) Promote ");
                        System.out.println("2) Demote ");
                        int option = Integer.parseInt(br.readLine());
                        switch (option) {
                            // Promote
                            case 1 :
                                changeUserRole(targetUserId, true);
                                break;

                            // Demote
                            case 2 :
                                changeUserRole(targetUserId, false);
                                break;

                            // Invalid input
                            default:
                                System.out.println("Invalid input, try again!");
                                break;
                        }
                        break;

                    // Go Back
                    case 0 :
                        return;

                    // Invalid input
                    default :
                        System.out.println("Invalid input, try again!");
                        break;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input, try again!");
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (myClient != null)
                        closeDB();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // Promote a user to admin
    public void changeUserRole(String userId, boolean admin) {
        openDB();
        collection = conn_db.getCollection("Users");
        UpdateResult updateResult = collection.updateOne(eq("userID", userId), set("Admin", admin));
        if (updateResult.getModifiedCount() == 1) {
            if (admin)
                System.out.println("User " + userId + " promoted to admin role!");
            else
                System.out.println("User " + userId + " demoted!");
        }
        else {
            System.out.println("Error during promotion of user " + userId + ", try again!");
        }
        closeDB();
    }


    // User brench menu
    public void showAdminMenu() {
        System.out.println("\nSelect an operation: ");
        System.out.println("1) Add product ");
        System.out.println("2) Remove product ");
        System.out.println("3) Add user ");
        System.out.println("4) Remove user ");
        System.out.println("5) Promote/Demote user ");
        System.out.println("0) Go Back ");
    }

}
