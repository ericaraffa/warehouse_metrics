import org.bson.Document;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        KeyValueManager kv = new KeyValueManager();
        ProductManager productManager = new ProductManager();
        UserManager userManager = new UserManager();
        AnalyticsManager analyticsManager = new AnalyticsManager();
        User user = null;

        int command;
        boolean logged = false;

        // Register
        System.out.println("\nSelect an operation: ");
        System.out.println("1) Login ");
        System.out.println("2) Register ");

        // Login/Register Menu
        while (!logged) {
            try {
                command = Integer.parseInt(br.readLine());
                switch (command) {
                    // Login
                    case 1 :
                        while (true) {
                            user = userManager.loginUser(br);
                            if (user != null) {
                                logged = true;
                                break;
                            }
                            System.out.println("Please try again!");
                        }
                        break;

                    // Register
                    case 2 :
                        while (true) {
                            user = userManager.registerUser(br);
                            if (user != null) {
                                logged = true;
                                break;
                            }
                            System.out.println("Please try again!");
                        }
                        break;

                    // Invalid input
                    default :
                        System.out.println("Invalid input, try again!");
                        break;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input, try again!");
                continue;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Main Menu
        while (true) {
            try {
                showMainMenu();
                command = Integer.parseInt(br.readLine());
                switch (command) {

                    // Browse products
                    case 1 :
                        productManager.browseProducts(user, br);
                        break;

                    // Browse categories
                    case 2 :
                        productManager.browseCategories();
                        break;

                    // Browse products with price filter
                    case 3 :
                        productManager.browseProductsByPrice(br);
                        break;

                    // Browse products with category filter
                    case 4 :
                        productManager.browseProductsByCategory(br);
                        break;

                    // Browse products with keyword filter
                    case 5 :
                        productManager.browseProductsByKeyword(br);
                        break;

                    // Analytics Operations
                    case 6 :
                        analyticsManager.showAnalytics(br);
                        break;

                    // Wishlist Operations
                    case 7 :
                        kv.browseWishlistOperations(user, br);
                        break;

                    // Browse users
                    case 8 :
                        userManager.browseUsers(br);
                        break;

                    // User Profile
                    case 9 :
                        System.out.println(user);
                        break;

                    // Logout
                    case 0 :
                        System.out.println("__________________________________________");
                        System.out.println("Logout");
                        System.out.println("__________________________________________");
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
        System.out.println("7) Browse wishlists ");
        System.out.println("8) Browse users ");
        System.out.println("9) Your Profile ");
        System.out.println("0) Logout ");
    }
}
