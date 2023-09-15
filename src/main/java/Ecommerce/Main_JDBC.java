package Ecommerce;

import Ecommerce.Controller.CartController;
import Ecommerce.Controller.ProductController;
import Ecommerce.Controller.UserController;
import Ecommerce.ControllerImpl.CLIController.CartCLIController;
import Ecommerce.ControllerImpl.CLIController.ProductCLIController;
import Ecommerce.ControllerImpl.CLIController.UserCLIController;
import Ecommerce.customExceptions.*;
import Ecommerce.service.CartDBInterface;
import Ecommerce.service.CartItemDBInterface;
import Ecommerce.service.ProductDBInterface;
import Ecommerce.service.UserDBInterface;

import Ecommerce.serviceImpl.SQLiteServiceImpl.CartItemSQLiteDB;
import Ecommerce.serviceImpl.SQLiteServiceImpl.CartSQLiteDB;
import Ecommerce.serviceImpl.SQLiteServiceImpl.ProductSQLiteDB;
import Ecommerce.serviceImpl.SQLiteServiceImpl.UserSQLiteDB;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main_JDBC {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ExecutorService dataLoaderExecutor = Executors.newFixedThreadPool(4);
        String JDBCpath = "jdbc:sqlite:D:/study/Coding/NextLeap/E-Commerce_Cart/Ecommerce_cart.db";

        ProductDBInterface productSQLiteDB = ProductSQLiteDB.getInstance();
        UserDBInterface userSQLiteDB = UserSQLiteDB.getInstance();
        CartDBInterface cartSQLiteDB = CartSQLiteDB.getInstance();
        CartItemDBInterface cartItemSQLiteDB = CartItemSQLiteDB.getInstance();

        dataLoaderExecutor.submit(() -> {
                    productSQLiteDB.setDBFilePath(JDBCpath);
                });
        dataLoaderExecutor.submit(() -> {
                    userSQLiteDB.setDBFilePath(JDBCpath);
                });

        dataLoaderExecutor.submit(() -> {
                    cartSQLiteDB.setDBFilePath(JDBCpath);
                });
        dataLoaderExecutor.submit(() -> {
            cartItemSQLiteDB.setDBFilePath(JDBCpath);
        });

        dataLoaderExecutor.shutdown();

        try{
            dataLoaderExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }catch (InterruptedException e){
            System.err.println("Interrupted while waiting for data loading tasks to complete.");
        }

        UserController userCLIcontroller = new UserCLIController(userSQLiteDB);
        ProductController prodCLIcontroller = new ProductCLIController(productSQLiteDB);
        CartController cartCLIcontroller = new CartCLIController(cartSQLiteDB, productSQLiteDB, cartItemSQLiteDB);

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

                int choice = scanner.nextInt();
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
                           String response = cartCLIcontroller.getCartItems(sessionId);
                           System.out.println(response);

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
                        cartSQLiteDB.shutdown();
                        userSQLiteDB.shutdown();
                        productSQLiteDB.shutdown();
                        cartItemSQLiteDB.shutdown();
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
