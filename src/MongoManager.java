import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import com.mongodb.client.model.UnwindOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;
import static com.mongodb.client.model.Updates.*;

public class MongoManager {
    ConnectionString uri = null;
    MongoClient myClient = null;
    MongoDatabase conn_db = null;

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
    public void browseProducts(BufferedReader br) {
        openDB();
        MongoCollection<Document> collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        Bson projectProducts = project(fields(include("title", "categories", "price"), computed("productID","$asin"), excludeId()));
        collection.aggregate(Arrays.asList(projectProducts)).forEach(printDocuments);

        closeDB();

        // Product menu
        while (true) {
            try {
                showProductMenu();
                int command = Integer.parseInt(br.readLine());

                switch (command) {
                    // View Product
                    case 1 :
                        viewProduct(br);
                        break;

                    // Sort by price (ascending)
                    case 2 :
                        ascendingSortProducts();
                        break;

                    // Sort by price (descending)
                    case 3 :
                        descendingSortProducts();
                        break;

                    // Browse between two price bound
                    case 4 :
                        browseProductsByPrice(br);
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

    // View Product by asin
    public void viewProduct(BufferedReader br) {
        openDB();
        MongoCollection<Document> collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        try {
            System.out.println("Insert product ID");
            String productId = br.readLine();
            Bson matchProduct = match(eq("asin", productId));
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

    // Show operations of the product brench
    public void showProductMenu() {
        System.out.println("\nSelect an operation: ");
        System.out.println("1) View Product ");
        System.out.println("2) Sort by price (ascending) ");
        System.out.println("3) Sort by price (descending) ");
        System.out.println("4) Browse between two price bound ");
        System.out.println("0) Go Back ");
    }

    // Browse categories
    public void browseCategories() {
        openDB();
        MongoCollection<Document> collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        Bson groupCategories = group("$categories");
        Bson sortCategories = sort(ascending("_id"));
        Bson projectCategories = project(fields(computed("category","$_id"), excludeId()));
        collection.aggregate(Arrays.asList(groupCategories, sortCategories, projectCategories)).forEach(printDocuments);

        closeDB();
    }

    // Sort by price (ascending)
    public void ascendingSortProducts () {
        openDB();
        MongoCollection<Document> collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        Bson matchProducts = match(ne("price", ""));
        Bson sortProducts = sort(ascending("price"));
        Bson projectProducts = project(fields(include("title", "categories", "price"), computed("productID","$asin"), excludeId()));
        collection.aggregate(Arrays.asList(matchProducts, sortProducts, projectProducts)).forEach(printDocuments);

        closeDB();
    }

    // Sort by price (descending)
    public void descendingSortProducts () {
        openDB();
        MongoCollection<Document> collection = conn_db.getCollection("Products");
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
        MongoCollection<Document> collection = conn_db.getCollection("Products");
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
                // collection.aggregate(Arrays.asList(matchPrice, projectProducts)).forEach(printDocuments);
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
        MongoCollection<Document> collection = conn_db.getCollection("Products");
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

    // Analytics brench
    public void showAnalytics(BufferedReader br) {
        // Product menu
        while (true) {
            try {
                showAnalyticsMenu();
                int command = Integer.parseInt(br.readLine());

                switch (command) {
                    // Top-k suggested products
                    case 1 :
                        viewMostSuggestedProducts(br);
                        break;

                    // Top-k active users
                    case 2 :
                        viewMostActiveUsers(br);
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
            }
        }
    }

    // Show the Top-K suggested products
    private void viewMostSuggestedProducts(BufferedReader br) {
        openDB();
        MongoCollection<Document> collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        UnwindOptions options = new UnwindOptions();
        options.preserveNullAndEmptyArrays(false);
        Bson unwindSuggested = unwind("$related", options);
        Bson groupSuggested = group("$related", sum("occurrences", 1L));
        Bson matchSuggested = match(ne("related", ""));
        Bson sortSuggested = sort(descending("occurrences"));
        Bson projectSuggested = project(fields(excludeId(), computed("ProductId","$_id"), include("occurrences")));

        System.out.println("Number of products to rank?");
        try {
            int k = Integer.parseInt(br.readLine());
            if (k <= 0)
                k = 3;
            Bson limitSuggested = limit(k);
            System.out.println("Top " + k + " suggested products");
            collection.aggregate(Arrays.asList(unwindSuggested, matchSuggested, groupSuggested, sortSuggested, limitSuggested, projectSuggested)).forEach(printDocuments);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid input, try again!");
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

    // Show the Top-K active users
    private void viewMostActiveUsers(BufferedReader br) {
        openDB();
        MongoCollection<Document> collection = conn_db.getCollection("Users");
        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        closeDB();
    }

    // Show operations of the analytics brench
    private void showAnalyticsMenu() {
        System.out.println("\nSelect an operation: ");
        System.out.println("1) Top-k suggested products ");
        System.out.println("2) Top-k active users ");
        System.out.println("0) Go Back ");
    }

    // Update salesrank of the specified product
    private void updateSalesrank(String productId) {
        openDB();
        MongoCollection<Document> collection = conn_db.getCollection("Products");

        UpdateResult updateResult = collection.updateOne(eq("asin", productId), set("salesrank", 0.0));
        System.out.println("Products updated: " + updateResult.getModifiedCount());
        closeDB();
    }

    // Delete a product from the DB (NEED TO HANDLE THE CONSISTENCY REGARDING THE REVIEWS AND THE WISHLISTS)
    private void deleteProduct(String productId) {
        openDB();
        MongoCollection<Document> collection = conn_db.getCollection("Products");

        Bson matchProduct = match(eq("asin", productId));
        DeleteResult deleteResult = collection.deleteOne(matchProduct);
        System.out.println("Products deleted: " + deleteResult.getDeletedCount());
        closeDB();
    }

}