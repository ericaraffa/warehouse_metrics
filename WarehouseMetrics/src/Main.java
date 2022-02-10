import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        MongoManager mongo = new MongoManager();
        KeyValueManager kv = new KeyValueManager();
        int command;

        while (true) {
            try {
                showMainMenu();
                command = Integer.parseInt(br.readLine());
                switch (command) {

                    // Browse products
                    case 1 :
                        mongo.browseProducts(br);
                        break;

                    // Browse products
                    case 2 :
                        mongo.browseCategories();
                        break;

                    // Browse products with price filter
                    case 3 :
                        mongo.browseProductsByPrice(br);
                        break;

                    // Browse products with category filter
                    case 4 :
                        mongo.browseProductsByCategory(br);
                        break;

                    // Browse products with keyword filter
                    case 5 :
                        mongo.browseProductsByKeyword(br);
                        break;

                    // KeyValue Operations
                    case 6 :
                        mongo.showAnalytics(br);
                        break;

                    // KeyValue Operations (TESTING PURPOSE)
                    case 7 :
                        // System.out.println("Insert wishlist ID");
                        kv.insertInWishlist("A2STD12XLS4DK5", "1", "B0007UDXF2", "categories", "Clothing");
                        kv.insertInWishlist("A2STD12XLS4DK5", "1", "B0007UDXF2", "title", "Rainbow Sandals Women's Premier Leather Single Layer Narrow Strap");
                        kv.insertInWishlist("A2STD12XLS4DK5", "1", "B0007UDXF2", "price", "");
                        kv.viewWishlist("A2STD12XLS4DK5");
                        break;

                    // Exit
                    case 0 :
                        return;

                    // Invalid input
                    default :
                        System.out.println("Invalid input, try again!");
                        break;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input, try again!");
                continue;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void showMainMenu() {
        System.out.println("\nSelect an operation: ");
        System.out.println("1) Browse products ");
        System.out.println("2) Browse categories ");
        System.out.println("3) Browse products by price ");
        System.out.println("4) Browse products by category ");
        System.out.println("5) Browse products by keyword ");
        System.out.println("6) Analytics ");
        System.out.println("7) Browse wishlists TESTING");
        System.out.println("0) Exit application ");
    }
}
