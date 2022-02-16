import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UnwindOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.query.Criteria;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

public class AnalyticsManager {

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

    // Analytics brench
    public void showAnalytics(BufferedReader br) {
        ProductManager productManager = new ProductManager();

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

                    // Product trend
                    case 2 :
                        productManager.showProducts(); //show all products
                        System.out.println("\nSelect a productID: ");
                        String productId = br.readLine();
                        viewProductTrend(productId);
                        break;

                    // Top-k active users
                    case 3 :
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
        collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments = doc -> System.out.println(doc.toJson());

        UnwindOptions options = new UnwindOptions();
        options.preserveNullAndEmptyArrays(false);
        Bson unwindSuggested = unwind("$related", options);
        Bson matchSuggested = match(ne("related", ""));
        Bson groupSuggested = group("$related", sum("occurrences", 1L));
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

    // Show operations of the analytics brench
    private void showAnalyticsMenu() {
        System.out.println("\nSelect an operation: ");
        System.out.println("1) Top-k suggested products ");
        System.out.println("2) Product trend ");
        System.out.println("3) Top-k active users ");
        System.out.println("0) Go Back ");

    }

    //Product trend
    public void viewProductTrend(String productId){
        openDB();
        collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments = doc -> System.out.println(doc.toJson());


        //Match asin==productId
        Bson matchProduct = match(eq("asin", productId));
        //Unwind reviews
        UnwindOptions options = new UnwindOptions();
        Bson unwindReviews = unwind("$reviews", options);
        //Sort reviews by unix time asc 1
        Bson sortReviews = sort(ascending("reviews.unixReviewTime"));

        System.out.println("Sorted reviews: ");
        collection.aggregate(Arrays.asList(matchProduct, unwindReviews, sortReviews)).forEach(printDocuments);

        Criteria whereID = Criteria.byExample(Criteria.where("asin").equals(productId));


        //.sum("overall").as("sumOverall")
        /*
        Aggregation pipeline = Aggregation.newAggregation(Aggregation.match(whereID),
                                                            Aggregation.unwind("reviews"),
                                                            Aggregation.group("reviewTime").count().as("numReviews"),
                                                            Aggregation.sort(Sort.Direction.ASC, "unixReviewTime"),
                                                            Aggregation.project("reviewTime").and("numReviews").as("totReviews")
                    );

        */

        closeDB();

    }

    // Show the Top-K active users
    public void viewMostActiveUsers(BufferedReader br) {
        openDB();
        collection = conn_db.getCollection("Users");
        Consumer<Document> printDocuments = doc -> System.out.println(doc.toJson());

        closeDB();
    }
}
