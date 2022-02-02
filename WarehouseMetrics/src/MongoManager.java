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
    public void browseProducts() {
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase conn_db = myClient.getDatabase("warehouse_metrics");
        MongoCollection<Document> collection = conn_db.getCollection("Products");

        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());
        collection.find().forEach(printDocuments);
        myClient.close();
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
        Bson projectCategories = project(fields(computed("categories","$_id")));
        collection.aggregate(Arrays.asList(groupCategories, sortCategories, projectCategories)).forEach(printDocuments);
    }

    // Browse products with price filter
    public void browseProductsByPrice(BufferedReader br) {
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase conn_db = myClient.getDatabase("warehouse_metrics");
        MongoCollection<Document> collection = conn_db.getCollection("Products");

        Consumer<Document> printDocuments =  doc -> System.out.println(doc.toJson());

        try {
            System.out.println("Insert the lower bound");
            int lowerPrice = Integer.parseInt(br.readLine());
            System.out.println("Insert the upper bound");
            int upperPrice = Integer.parseInt(br.readLine());

            collection.find(and(gt("price", lowerPrice), lte("price", upperPrice))).forEach(printDocuments);

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
        Bson sortCategories = sort(ascending("categories"));
        Bson projectCategories = project(fields(excludeId(), include("categories")));
        collection.aggregate(Arrays.asList(sortCategories, groupCategories)).forEach(printDocuments);

        System.out.println("Insert a category");
        try {
            String category = br.readLine();
            Bson matchCategories = match(eq("categories", category));
            collection.aggregate(Arrays.asList(matchCategories)).forEach(printDocuments);
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