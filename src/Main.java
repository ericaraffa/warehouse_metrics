import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        MongoManager mongo = new MongoManager();
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
        System.out.println("3) Browse products with price filter ");
        System.out.println("4) Browse products with category filter ");
        System.out.println("0) Exit application ");
    }
}
