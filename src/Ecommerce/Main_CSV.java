package Ecommerce;

import Ecommerce.Controller.CartController;
import Ecommerce.Controller.ProductController;
import Ecommerce.Controller.UserController;
import Ecommerce.ControllerImpl.CartCsvController;
import Ecommerce.ControllerImpl.ProductCsvController;
import Ecommerce.ControllerImpl.UserCsvController;
import Ecommerce.customExceptions.*;
import Ecommerce.service.CartDBInterface;
import Ecommerce.service.ProductDBInterface;
import Ecommerce.service.UserDBInterface;
import Ecommerce.serviceImpl.*;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main_CSV {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ExecutorService dataLoaderExecutor = Executors.newFixedThreadPool(3);
//
////        UserDBInterface userDB = UserCsvDB.getInstance();
//
//        //Load product catalog from CSV
//        ProductCatalogServImpl catalog = ProductCatalogServImpl.getInstance();
//        catalog.setFilepath("src/Ecommerce/productCatalog.csv");
//
//        //Load User data from CSV
//        UserServImpl userService = new UserServImpl();
//        userService.setFilepath("src/Ecommerce/userData.csv");
//
//        //Load cart Data from CSV
//        CartServImpl cartService = new CartServImpl();
//        cartService.setFilepath("src/Ecommerce/cartData.csv");
//
        ProductDBInterface prodCSVdb = ProductCsvDB.getInstance();
        UserDBInterface userCSVdb = UserCsvDB.getInstance();
        CartDBInterface cartCSVdb = CartCsvDB.getInstance();

        dataLoaderExecutor.submit(() -> {
                    prodCSVdb.setDBFilePath("src/Ecommerce/CSVfiles/productCatalog.csv");
                });
        dataLoaderExecutor.submit(() -> {
                    userCSVdb.setDBFilePath("src/Ecommerce/CSVfiles/userData.csv");
                });

        dataLoaderExecutor.submit(() -> {
                    cartCSVdb.setDBFilePath("src/Ecommerce/CSVfiles/cartData.csv");
                });

        dataLoaderExecutor.shutdown();

        try{
            dataLoaderExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }catch (InterruptedException e){
            System.err.println("Interrupted while waiting for data loading tasks to complete.");
        }

        UserController userCSVcontroller = new UserCsvController(userCSVdb);
        ProductController prodCSVcontroller = new ProductCsvController(prodCSVdb);
        CartController cartCSVcontroller = new CartCsvController(cartCSVdb, prodCSVdb);

//         We can use this with ThreadLocal Class
        ThreadLocal<String> sessionId = new ThreadLocal<>();

        boolean running = true;
        while (running) {
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
                        System.out.println(userCSVcontroller.signUp(email,firstName,lastName,password));
                    } catch (UserAlreadyExistsException e) {
                        System.out.println(e.getMessage());
                    }

                }
                case 2 -> {

                    System.out.print("Please enter you Email id : ");
                    String email = scanner.nextLine();

                    System.out.print("Please enter you password: ");
                    String password = scanner.nextLine();

//                    System
                    try {
                        sessionId.set( userCSVcontroller.signIn(email, password));
                    } catch (UserNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case 3 -> {
                    // Not validating sessionId here
                    if(sessionId.get().isEmpty()){
                        System.out.println("Please sign in to view product catalog");
                    }
                    else {
                        System.out.print("Please enter the search term : ");
                        String searchTerm = scanner.nextLine();
                        prodCSVcontroller.searchProduct(searchTerm);
                    }
                }
                case 4 -> {
                    System.out.print("Please enter the product id to add product to your cart: ");
                    String productId = scanner.nextLine();

                    System.out.print("Please enter the quantity to buy: ");
                    int quantity = scanner.nextInt();

                    try{
                        cartCSVcontroller.addCartItem(sessionId.get(), productId, quantity);
                    } catch (ProductNotFound e) {
                        System.out.println("Product Id entered doesnt exist in the database. Check catalog");
                    } catch (SessionExpiredException | QuanitityNotAvailException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case 5 -> {
                    System.out.print("Please enter the product id to update the quantity : ");
                    String productId = scanner.nextLine();

                    System.out.print("Please enter the quantity to update: ");
                    int quantity = scanner.nextInt();

                    try{
                        System.out.println(cartCSVcontroller.updateCartItem(sessionId.get(), productId, quantity));
                    } catch (SessionExpiredException | QuanitityNotAvailException | CartItemNotAvail | ProductNotFound e) {
                        System.out.println(e.getMessage());
                    }
                }
                case 6 ->{
                    System.out.print("Please enter the product id to remove product from your cart: ");
                    String productId = scanner.nextLine();

                    try{
                        cartCSVcontroller.removeCartItem(sessionId.get(), productId);

                    } catch (CartItemNotAvail | ProductNotFound | SessionExpiredException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case 7 -> {

                    try{
                        cartCSVcontroller.getCartItems(sessionId.get());
                    } catch (SessionExpiredException | CartItemNotAvail e) {
                        System.out.println(e.getMessage());
                    }
                }
                case 8 ->{
                    // Anyone without sign in can also view catalog
                    prodCSVcontroller.displayAllProducts();
                }
                case 9 -> {
                    System.out.println(userCSVcontroller.signOut(sessionId.get()));
                    sessionId.remove();
                }
                case 10 -> {
                    // Save data to CSV file before exiting
                    System.out.println("Exiting...");
                    // shutdown executor services of DB
                    cartCSVdb.shutdown();
                    userCSVdb.shutdown();
                    prodCSVdb.shutdown();
                    running = false;

                }
                default -> System.out.println("Invalid choice. Please select again.");
            }
        }
        scanner.close();
        System.out.println("main thread finished");
    }
}
