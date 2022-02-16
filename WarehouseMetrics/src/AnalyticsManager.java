import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UnwindOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

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
    public void showAnalytics(User user, BufferedReader br) {
        ProductManager productManager = new ProductManager();

        // Product menu
        while (true) {
            try {
                showAnalyticsMenu(user);
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
                        if (user.isAdmin()) {
                            viewMostActiveUsers(br);
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
    private void showAnalyticsMenu(User user) {
        System.out.println("\nSelect an operation: ");
        System.out.println("1) Top-k suggested products ");
        System.out.println("2) Product trend ");
        if (user.isAdmin()) {
            System.out.println("3) Top-k active users ");
        }
        System.out.println("0) Go Back ");

    }

    //Product trend
    public void viewProductTrend(String productId){
        openDB();
        collection = conn_db.getCollection("Products");
        Consumer<Document> printDocuments = doc -> System.out.println(doc.toJson());

        UnwindOptions options = new UnwindOptions();

        //Match asin==productId
        Bson matchProduct = match(eq("asin", productId));
        //Unwind reviews
        Bson unwindReviews = unwind("$reviews", options);
        //Avg reviews
        Bson avgReviews = group("$reviews.reviewTime", avg("avgReviews", "$reviews.overall"));
        //Sort reviews by unix time asc 1
        Bson sortReviews = sort(ascending("_id"));
        //Project
        Bson projectTrend = project(fields(excludeId(), computed("reviewTime", "$_id"), include("avgReviews")));

        System.out.println("Sorted reviews: ");
        collection.aggregate(Arrays.asList(matchProduct, unwindReviews, avgReviews, sortReviews, projectTrend)).forEach(printDocuments);

        closeDB();

    }

    // Show the Top-K active users (ADMIN)
    public void viewMostActiveUsers(BufferedReader br) {
        openDB();
        collection = conn_db.getCollection("Users");
        Consumer<Document> printDocuments = doc -> System.out.println(doc.toJson());
        UnwindOptions options = new UnwindOptions();
        options.preserveNullAndEmptyArrays(false);
        Bson unwindActive = unwind("$reviews", options);
        Bson groupActive = group("$userID", sum("occurrences", 1L));
        Bson sortActive = sort(descending("occurrences"));
        Bson projectActive = project(fields(excludeId(), computed("userID","$_id"), include("occurrences")));

        try {
            System.out.println("Number of users to rank?");
            int k = Integer.parseInt(br.readLine());
            System.out.println("Enter a start date in the format \" yyyy-MM-dd \"");
            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse((br.readLine()));
            System.out.println("Enter an end date in the format \" yyyy-MM-dd \"");
            Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse((br.readLine()));
            Bson matchActive = match(and(gte("reviews.unixReviewTime" , startDate.getTime()),lte("reviews.unixReviewTime",endDate.getTime())));

            if (k <= 0)
                k = 3;
            Bson limitActive= limit(k);
            System.out.println("Top " + k + " active Users");
            collection.aggregate(Arrays.asList(unwindActive,matchActive, groupActive,sortActive, limitActive, projectActive)).allowDiskUse(true).forEach(printDocuments);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid input, try again!");
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e)
        { e.printStackTrace();}
        finally {
            try {
                if (myClient != null)
                    closeDB();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
