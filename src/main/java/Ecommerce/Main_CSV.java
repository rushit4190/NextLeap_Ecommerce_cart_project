package Ecommerce;

import Ecommerce.Service.CartService;
import Ecommerce.Service.ProductService;
import Ecommerce.Service.UserService;
import Ecommerce.Service.ServiceImpl.CLIService.CartCLIService;
import Ecommerce.Service.ServiceImpl.CLIService.ProductCLIService;
import Ecommerce.Service.ServiceImpl.CLIService.UserCLIService;
import Ecommerce.customExceptions.*;
import Ecommerce.Repository.CartDBInterface;
import Ecommerce.Repository.CartItemDBInterface;
import Ecommerce.Repository.ProductDBInterface;
import Ecommerce.Repository.UserDBInterface;
import Ecommerce.Repository.RepositoryImpl.CsvRepoImpl.CartCsvDB;
import Ecommerce.Repository.RepositoryImpl.CsvRepoImpl.CartItemCsvDB;
import Ecommerce.Repository.RepositoryImpl.CsvRepoImpl.ProductCsvDB;
import Ecommerce.Repository.RepositoryImpl.CsvRepoImpl.UserCsvDB;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main_CSV {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ExecutorService dataLoaderExecutor = Executors.newFixedThreadPool(4);

        ProductDBInterface prodCSVdb = ProductCsvDB.getInstance();
        UserDBInterface userCSVdb = UserCsvDB.getInstance();
        CartDBInterface cartCSVdb = CartCsvDB.getInstance();
        CartItemDBInterface cartItemCSVdb = CartItemCsvDB.getInstance();

        dataLoaderExecutor.submit(() -> {
                    prodCSVdb.setDBFilePath("src/Ecommerce/CSVfiles/productCatalog.csv");
                });
        dataLoaderExecutor.submit(() -> {
                    userCSVdb.setDBFilePath("src/Ecommerce/CSVfiles/userData.csv");
                });

        dataLoaderExecutor.submit(() -> {
                    cartCSVdb.setDBFilePath("src/Ecommerce/CSVfiles/cartData.csv");
                });
        dataLoaderExecutor.submit(() -> {
            cartItemCSVdb.setDBFilePath("src/Ecommerce/CSVfiles/cartItem.csv");
        });

        dataLoaderExecutor.shutdown();

        try{
            dataLoaderExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }catch (InterruptedException e){
            System.err.println("Interrupted while waiting for data loading tasks to complete.");
        }

        UserService userCLIcontroller = new UserCLIService(userCSVdb);
        ProductService prodCLIcontroller = new ProductCLIService(prodCSVdb);
        CartService cartCLIcontroller = new CartCLIService(cartCSVdb, prodCSVdb, cartItemCSVdb);

//         We can use this with ThreadLocal Class
//        ThreadLocal<String> sessionId1 = new ThreadLocal<>();

        boolean running = true;
        while (running) {
            try{
                System.out.println("1. Sign Up");
                System.out.println("2. Sign In");
                System.out.println("3. Search Product Catalog");
                System.out.println("4. Add items to your cart");
                System.out.println("5. Update items to your cart");
                System.out.println("6. Remove items from your cart");
                System.out.println("7. Get items from your cart");
                System.out.println("8. Get products from Product Catalog");
                System.out.println("9. Sign Out");
                System.out.println("10. Exit");
                System.out.print("Enter your choice: ");

                Integer choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline

                switch (choice) {
                    case 1 -> {
                        System.out.print("Please enter you Email id : ");
                        String email = scanner.nextLine();
                        System.out.print("Please enter you first name : ");
                        String firstName = scanner.nextLine();
                        System.out.print("Please enter you last name : ");
                        String lastName = scanner.nextLine();
                        System.out.print("Please enter you password: ");
                        String password = scanner.nextLine();
                        try {
                            String response = userCLIcontroller.signUp(email,firstName,lastName,password);
                            System.out.println(response);
                        } catch (UserAlreadyExistsException e) {
                            System.out.println(e.getMessage());
                        }

                    }
                    case 2 -> {

                        System.out.print("Please enter you Email id : ");
                        String email = scanner.nextLine();

                        System.out.print("Please enter you password: ");
                        String password = scanner.nextLine();

                        try {

                            String sessionId =  userCLIcontroller.signIn(email, password);

                            System.out.println("Please note your session Id : " + sessionId + ". Copy this for future reference. It will be valid for 30 minutes.");

                        } catch (UserNotFoundException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    case 3 -> {
                        // Not validating sessionId here, anyone can search catalog

                        System.out.print("Please enter the search term : ");
                        String searchTerm = scanner.nextLine();

                        prodCLIcontroller.searchProduct(searchTerm);

                    }
                    case 4 -> {
                        System.out.print("Please enter your session Id : ");
                        String sessionId = scanner.nextLine();

                        System.out.print("Please enter the product id to add product to your cart: ");
                        String productId = scanner.nextLine();

                        System.out.print("Please enter the quantity to buy: ");
                        int quantity = scanner.nextInt();

                        try{
                            String response = cartCLIcontroller.addCartItem(sessionId, productId, quantity);
                            System.out.println(response);

                        } catch (ProductNotFound e) {
                            System.out.println("Product Id entered doesnt exist in the database. Check catalog");
                        } catch (SessionExpiredException | QuanitityNotAvailException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    case 5 -> {
                        System.out.print("Please enter your session Id : ");
                        String sessionId = scanner.nextLine();

                        System.out.print("Please enter the product id to update the quantity : ");
                        String productId = scanner.nextLine();

                        System.out.print("Please enter the quantity to update: ");
                        int quantity = scanner.nextInt();

                        try{
                            String response = cartCLIcontroller.updateCartItem(sessionId, productId, quantity);
                            System.out.println(response);

                        } catch (SessionExpiredException | QuanitityNotAvailException | CartItemNotAvail | ProductNotFound e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    case 6 ->{
                        System.out.print("Please enter your session Id : ");
                        String sessionId = scanner.nextLine();

                        System.out.print("Please enter the product id to remove product from your cart: ");
                        String productId = scanner.nextLine();

                        try{
                            String response = cartCLIcontroller.removeCartItem(sessionId, productId);
                            System.out.println(response);

                        } catch (CartItemNotAvail | ProductNotFound | SessionExpiredException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    case 7 -> {

                        System.out.print("Please enter your session Id : ");
                        String sessionId = scanner.nextLine();

                        try{
                           cartCLIcontroller.getCartItems(sessionId);


                        } catch (SessionExpiredException | CartItemNotAvail e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    case 8 ->{
                        // Anyone without sign in can also view catalog
                        prodCLIcontroller.displayAllProducts();
                    }
                    case 9 -> {
                        System.out.print("Please enter your session Id : ");
                        String sessionId = scanner.nextLine();

                        String response = userCLIcontroller.signOut(sessionId);
                        System.out.println(response);

                    }
                    case 10 -> {
                        // Save data to CSV file before exiting
                        System.out.println("Exiting...");
                        // shutdown executor services of DB
                        cartCSVdb.shutdown();
                        userCSVdb.shutdown();
                        prodCSVdb.shutdown();
                        cartItemCSVdb.shutdown();
                        running = false;

                    }
                    default -> System.out.println("Invalid choice. Please select again.");
                }
            }catch (InputMismatchException e){
                System.out.println("Please enter a valid option number" + e.getMessage());
            }
        }
        scanner.close();
        System.out.println("main thread finished");
    }
}
