import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import com.mongodb.client.model.UnwindOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.set;

public class ProductManager {
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

    // Browse products
    public void showProducts() {
        openDB();
        collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments = doc -> System.out.println(doc.toJson());

        Bson projectProducts = project(fields(include("title", "categories", "price"), computed("productID","$asin"), excludeId()));
        collection.aggregate(Arrays.asList(projectProducts)).forEach(printDocuments);

        closeDB();
    }

    // Products brench
    public void browseProducts(User user, BufferedReader br) {

        // Product menu
        while (true) {
            try {
                showProducts();
                showProductMenu();
                int command = Integer.parseInt(br.readLine());

                switch (command) {
                    // View Product
                    case 1 :
                        Document product = viewProduct(br);
                        if (product == null) {
                            System.out.println("Product not found, try again!");
                            break;
                        }
                        showProductOperations(user, product, br);
                        break;

                    // Sort by price (ascending)
                    case 2 :
                        ascendingSortPriceProducts();
                        break;

                    // Sort by price (descending)
                    case 3 :
                        descendingSortPriceProducts();
                        break;

                    // Browse between two price bound
                    case 4 :
                        browseProductsByPrice(br);
                        break;

                    // Browse products with category filter
                    case 5 :
                        browseProductsByCategory(br);
                        break;

                    // Browse products with keyword filter
                    case 6 :
                        browseProductsByKeyword(br);
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

    // Operations on products regarding reviews and wishlists
    private void showProductOperations(User user, Document product, BufferedReader br) {
        KeyValueManager kv = new KeyValueManager();
        openDB();
        // Product Operations menu
        while (true) {
            try {
                showProductOperationsMenu();
                int command = Integer.parseInt(br.readLine());
                int wishlistId;
                String userTargetId;
                switch (command) {
                    // Show reviews
                    case 1 :
                        System.out.println(product.get("reviews").toString());
                        break;

                    // Add review
                    case 2 :
                        insertReview(user, product.get("asin").toString(), br);
                        break;

                    // Delete review
                    case 3 :
                        break;

                    // Add to wishlist
                    case 4 :
                        kv.browseWishlist(user);
                        System.out.println(user.getWishlistCount() + " : Create new wishlist");
                        System.out.println("\nSelect a wishlist");
                        wishlistId = Integer.parseInt(br.readLine());
                        boolean needUpdate = kv.insertInWishlist(user, product, wishlistId);

                        // New wishlist created
                        if (needUpdate) {
                            UserManager userManager = new UserManager();
                            userManager.updateUserWishlistCount(user);
                        }
                        System.out.println("Product added to the wishlist");
                        break;

                    // Remove from wishlist
                    case 5 :
                        kv.browseWishlist(user);
                        System.out.println("\nInsert wishlist ID");
                        wishlistId = Integer.parseInt(br.readLine());
                        boolean found;

                        if (user.isAdmin()) {
                            System.out.println("Insert user ID");
                            userTargetId = br.readLine();
                            found = kv.adminDeleteFromWishlist(userTargetId, wishlistId, product.get("asin").toString());
                        } else {
                            found = kv.deleteFromWishlist(user, wishlistId, product.get("asin").toString());
                        }

                        if (found) {
                            System.out.println("Product deleted from wishlist!");
                        }
                        else {
                            System.out.println("Product not found in wishlist!");
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

    // List of operations possible on a single product
    private void showProductOperationsMenu() {
        System.out.println("\nSelect an operation: ");
        System.out.println("1) Show reviews ");
        System.out.println("2) Add review ");
        System.out.println("3) Delete review ");
        System.out.println("4) Add to wishlist");
        System.out.println("5) Remove from wishlist");
        System.out.println("0) Go Back ");
    }

    // View Product by id
    public Document viewProduct(BufferedReader br) {
        openDB();
        collection = conn_db.getCollection("Products");
        Document result = null;

        try {
            System.out.println("Insert product ID");
            String productId = br.readLine();
            MongoCursor<Document> cursor = collection.find(eq("asin", productId)).iterator();

            if (cursor.hasNext()) {
                result = cursor.next();
                if(result == null)
                    return null;
                System.out.println(result.toJson());
            }
            cursor.close();
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
        return result;
    }

    // Show operations of the product brench
    public void showProductMenu() {
        System.out.println("\nSelect an operation: ");
        System.out.println("1) View Product ");
        System.out.println("2) Sort by price (ascending) ");
        System.out.println("3) Sort by price (descending) ");
        System.out.println("4) Browse between two price bound ");
        System.out.println("5) Browse products by category ");
        System.out.println("6) Browse products by keyword ");
        System.out.println("0) Go Back ");
    }

    // Browse categories
    public void browseCategories() {
        openDB();
        collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        Bson groupCategories = group("$categories");
        Bson sortCategories = sort(ascending("_id"));
        Bson projectCategories = project(fields(computed("category","$_id"), excludeId()));
        collection.aggregate(Arrays.asList(groupCategories, sortCategories, projectCategories)).forEach(printDocuments);

        closeDB();
    }

    // Sort by price (ascending)
    public void ascendingSortPriceProducts () {
        openDB();
        collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        Bson matchProducts = match(ne("price", ""));
        Bson sortProducts = sort(ascending("price"));
        Bson projectProducts = project(fields(include("title", "categories", "price"), computed("productID","$asin"), excludeId()));
        collection.aggregate(Arrays.asList(matchProducts, sortProducts, projectProducts)).forEach(printDocuments);

        closeDB();
    }

    // Sort by price (descending)
    public void descendingSortPriceProducts () {
        openDB();
        collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        Bson matchProducts = match(ne("price", ""));
        Bson sortProducts = sort(descending("price"));
        Bson projectProducts = project(fields(include("title", "categories", "price"), computed("productID","$asin"), excludeId()));
        collection.aggregate(Arrays.asList(matchProducts, sortProducts, projectProducts)).forEach(printDocuments);

        closeDB();
    }

    // Browse products with price filter
    public void browseProductsByPrice(BufferedReader br) {
        openDB();
        collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        try {
            System.out.println("Insert the lower bound");
            int lowerPrice = Integer.parseInt(br.readLine());
            System.out.println("Insert the upper bound");
            int upperPrice = Integer.parseInt(br.readLine());
            Bson matchPrice = match(and(gt("price", lowerPrice), lte("price", upperPrice), ne("price", "")));
            Bson projectProducts = project(fields(include("title", "categories", "price"), computed("productID","$asin"), excludeId()));

            System.out.println("Sorting order?");
            System.out.println("1) Ascending");
            System.out.println("2) Descending");
            System.out.println("3) No preferences");
            int sortOrder = Integer.parseInt(br.readLine());
            Bson sortProducts = null;
            switch (sortOrder) {
                case (1) :
                    sortProducts = sort(ascending("price"));
                    break;

                case (2) :
                    sortProducts = sort(descending("price"));
                    break;

                default :
                    break;
            }
            if (sortProducts == null) {
                collection.aggregate(Arrays.asList(matchPrice, projectProducts)).forEach(printDocuments);
            } else {
                collection.aggregate(Arrays.asList(matchPrice, sortProducts, projectProducts)).forEach(printDocuments);
            }
        } catch (NumberFormatException ex) {
            System.out.println("Invalid input!");
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

    // Browse products with category filter
    public void browseProductsByCategory(BufferedReader br) {
        openDB();
        collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        Bson groupCategories = group("$categories");
        Bson sortCategories = sort(ascending("_id"));
        Bson projectCategories = project(fields(computed("category","$_id"), excludeId()));
        collection.aggregate(Arrays.asList(groupCategories, sortCategories, projectCategories)).forEach(printDocuments);

        System.out.println("Insert a category");
        try {
            String category = br.readLine();
            Bson matchCategories = match(eq("categories", category));
            Bson projectProducts = project(fields(include("title", "categories", "price"), computed("productID","$asin"), excludeId()));
            collection.aggregate(Arrays.asList(matchCategories, projectProducts)).forEach(printDocuments);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (myClient != null)
                    closeDB();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Browse products with keyword filter
    public void browseProductsByKeyword(BufferedReader br) {
        openDB();
        collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        System.out.println("Insert a keyword");
        try {
            String keyword = br.readLine();
            String patternKeyword = "." + keyword + ".?";
            Pattern pattern = Pattern.compile(patternKeyword, Pattern.CASE_INSENSITIVE);
            Bson matchProducts = match(regex("title", pattern));
            Bson projectProducts = project(fields(include("title", "categories", "price"), computed("productID","$asin"), excludeId()));
            collection.aggregate(Arrays.asList(matchProducts, projectProducts)).forEach(printDocuments);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (myClient != null)
                    closeDB();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Update salesrank of the specified product (UTILITY)
    private void updateSalesrank() {
        openDB();
        collection = conn_db.getCollection("Products");

        collection.updateMany(regex("asin", ""), set("salesrank", 0.0));
        //System.out.println("Products updated: " + updateResult.getModifiedCount());
        closeDB();
    }

    // Retrive reviews of a specified product
    public ArrayList<Document> getReviews(String productId) {
        //openDB();
        MongoCollection<Document> reviewCollection = conn_db.getCollection("Reviews");
        ArrayList<Document> resultReviews = new ArrayList<>();

        try (MongoCursor<Document> cursor = reviewCollection.find(eq("asin", productId)).iterator()) {
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

    // Initialize "reviews" attribute of each product (UTILITY)
    public void updateProductReviews() {
        openDB();
        collection = conn_db.getCollection("Products");

        ArrayList<Document> resultReviews = new ArrayList<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document productDoc = cursor.next();
                String productId = productDoc.get("asin").toString();
                resultReviews = getReviews(productId);
                collection.updateOne(eq("asin", productId), set("reviews", resultReviews));
                // System.out.println("Product updated: " + updateResult.getModifiedCount());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        closeDB();
    }

    // Insert a product into the DB (METADATAID and ASIN?)
    private void insertProduct(String metadata, String asin, String category, String title, double price) {
        openDB();
        collection = conn_db.getCollection("Products");

        Document newProduct = new Document("metadata", metadata)
                .append("asin", asin)
                .append("salesrank", 0.0)
                .append("imurl", "")
                .append("categories", category)
                .append("title", title)
                .append("price", price)
                .append("related", Arrays.asList());

        collection.insertOne(newProduct);
        System.out.println("New product:\n" + newProduct.toJson());
        closeDB();
    }

    // Delete a product from the DB (NEED TO HANDLE THE CONSISTENCY REGARDING THE REVIEWS AND THE WISHLISTS)
    private void deleteProduct(String productId) {
        openDB();
        collection = conn_db.getCollection("Products");

        Bson matchProduct = match(eq("asin", productId));
        DeleteResult deleteResult = collection.deleteOne(matchProduct);
        System.out.println("Products deleted: " + deleteResult.getDeletedCount());
        closeDB();
    }

    // Insert review in DB (need usrID from current session)
    public void insertReview(User user, String productID, BufferedReader br){
        openDB();
        collection = conn_db.getCollection("Users");

        try {
            System.out.println("Insert title: ");
            String title_review = br.readLine();
            System.out.println("Insert review text: ");
            String txt_review = br.readLine();
            System.out.println("Insert vote: ");
            double vote_review = Double.parseDouble(br.readLine());

            // Get current date
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate curr_date = LocalDate.now();
            long seconds = System.currentTimeMillis() / 1000l;

            Document newReview = new Document("id", 10)     // FOR TESTING
                    .append("reviewerID", user.getUserID())            // Get from curr session
                    .append("asin", productID)
                    .append("reviewerName", user.getUsername())       // Get from curr session
                    .append("reviewText", txt_review)
                    .append("overall", vote_review)
                    .append("summary", title_review)
                    .append("unixReviewTime", seconds)
                    .append("reviewTime", dtf.format(curr_date));

            //USERS
            collection.updateOne(eq("userID", user.getUserID()), Updates.addToSet("reviews", newReview));

            //PRODUCTS
            collection = conn_db.getCollection("Products");
            collection.updateOne(eq("asin", productID), Updates.addToSet("reviews", newReview));


        } catch (IOException e) {
            e.printStackTrace();
        }
        closeDB();
    }

    // Delete review from DB (need usrID from current session) (NEED TO HANDLE THE CONSISTENCY REGARDING THE REVIEWS AND THE WISHLISTS)
    public void deleteReview() {

    }

}
