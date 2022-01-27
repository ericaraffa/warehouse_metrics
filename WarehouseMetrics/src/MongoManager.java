import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.Arrays;

public class MongoManager {
    public static void main(String[] args) {

        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase conn_db = myClient.getDatabase("warehouse_metrics");
        MongoCollection<Document> collection = conn_db.getCollection("Reviews");

        Document doc = new Document("name", "MongoDB")
                .append("count", 1)
                .append("versions", Arrays.asList("v3.2", "v3.0", "v2.6"))
                .append("info", new Document("x", 203).append("y", 102));

        //collection.insertOne(doc);

        MongoCursor<Document> cursor = collection.find().iterator();
        {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        }
    }
}