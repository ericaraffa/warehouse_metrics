import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;

public class MongoManager {

    // Browse products
    public void browseProducts(BufferedReader br) {
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase conn_db = myClient.getDatabase("warehouse_metrics");
        MongoCollection<Document> collection = conn_db.getCollection("Products");

        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        Bson projectProducts = project(fields(include("title", "categories", "price"), computed("productID","$asin"), excludeId()));
        collection.aggregate(Arrays.asList(projectProducts)).forEach(printDocuments);

        myClient.close();

        // Product menu
        while (true) {
            try {
                showProductMenu();
                int command = Integer.parseInt(br.readLine());

                switch (command) {

                    // View Product
                    case 1 :
                        System.out.println("Insert product ID");
                        String productId = br.readLine();
                        viewProduct(productId);
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
            }
        }

    }

    // View Product by matadataid
    public void viewProduct(String productId) {
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase conn_db = myClient.getDatabase("warehouse_metrics");
        MongoCollection<Document> collection = conn_db.getCollection("Products");

        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        Bson matchProduct = match(eq("asin", productId));
        collection.aggregate(Arrays.asList(matchProduct)).forEach(printDocuments);

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
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase conn_db = myClient.getDatabase("warehouse_metrics");
        MongoCollection<Document> collection = conn_db.getCollection("Products");

        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        Bson groupCategories = group("$categories");
        Bson sortCategories = sort(ascending("_id"));
        Bson projectCategories = project(fields(computed("category","$_id"), excludeId()));
        collection.aggregate(Arrays.asList(groupCategories, sortCategories, projectCategories)).forEach(printDocuments);

        myClient.close();
    }

    // Sort by price (ascending)
    public void ascendingSortProducts () {
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase conn_db = myClient.getDatabase("warehouse_metrics");
        MongoCollection<Document> collection = conn_db.getCollection("Products");

        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        Bson sortProducts = sort(ascending("price"));
        Bson projectProducts = project(fields(include("title", "categories", "price"), computed("productID","$asin"), excludeId()));
        collection.aggregate(Arrays.asList(sortProducts, projectProducts)).forEach(printDocuments);

        myClient.close();
    }

    // Sort by price (descending)
    public void descendingSortProducts () {
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase conn_db = myClient.getDatabase("warehouse_metrics");
        MongoCollection<Document> collection = conn_db.getCollection("Products");

        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        Bson sortProducts = sort(descending("price"));
        Bson projectProducts = project(fields(include("title", "categories", "price"), computed("productID","$asin"), excludeId()));
        collection.aggregate(Arrays.asList(sortProducts, projectProducts)).forEach(printDocuments);

        myClient.close();
    }

    // Browse products with price filter
    public void browseProductsByPrice(BufferedReader br) {
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase conn_db = myClient.getDatabase("warehouse_metrics");
        MongoCollection<Document> collection = conn_db.getCollection("Products");

        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());
        int command;

        try {
            System.out.println("Insert the lower bound");
            int lowerPrice = Integer.parseInt(br.readLine());
            System.out.println("Insert the upper bound");
            int upperPrice = Integer.parseInt(br.readLine());

            collection.find(and(gt("price", lowerPrice), lte("price", upperPrice))).forEach(printDocuments);

            /*
            Bson sortProducts = sort(ascending("price"));
            Bson projectProducts = project(fields(include("title", "categories", "price"), computed("productID","$asin"), excludeId()));
            collection.aggregate(Arrays.asList(sortProducts, projectProducts)).forEach(printDocuments);


            // Sorting products by price
            while (true) {
                command = Integer.parseInt(br.readLine());
                switch (command) {

                    // Sort by price (ascending)
                    case 1 :
                        break;

                    // Sort by price (descending)
                    case 2 :
                        break;

                    // Go Back
                    case 0 :
                        return;

                    // Invalid input
                    default:
                        System.out.println("Invalid input, try again!");
                        break;
                }
            }
             */

        } catch (NumberFormatException ex) {
            System.out.println("Invalid input!");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (myClient != null)
                    myClient.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Browse products with category filter
    public void browseProductsByCategory(BufferedReader br) {
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase conn_db = myClient.getDatabase("warehouse_metrics");
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
                    myClient.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}