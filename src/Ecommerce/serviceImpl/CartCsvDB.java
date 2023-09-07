package Ecommerce.serviceImpl;

import Ecommerce.model.Cart;
import Ecommerce.model.CartItem;
import Ecommerce.service.CartDBInterface;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.*;

public class CartCsvDB implements CartDBInterface {

    ExecutorService cartCsvWriteExe = Executors.newFixedThreadPool(1);

        /*
    Since we are having a file as DB, we have to ensure that reading and writing takes place via same thread.
    In case of SQL DB, read do not need to be synchronised and write on particular table to be synchronized.
     */

    private String DBfilePath = "";



    private CartCsvDB(){

    }

    private static class Loader {
        final private static CartCsvDB INSTANCE = new CartCsvDB();
    }

    public static CartCsvDB getInstance(){

        return Loader.INSTANCE;
    }
    @Override
    public Cart getCart(String userId) {
        try{
            Future<Cart> future = cartCsvWriteExe.submit(() -> getCartSerially(userId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String addCartItem(String userId, int totalCartItems, double totalCartValue, String productId, int quantity, double total) {
        try{
            Future<String> future = cartCsvWriteExe.submit(() -> addCartItemSerially(userId, totalCartItems, totalCartValue, productId, quantity, total));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String addNewCartWithItem(String userId, int totalCartItems, double totalCartValue, String productId, int quantity, double total){
        try{
            Future<String> future = cartCsvWriteExe.submit(() -> addNewCartWithItemSerially(userId, totalCartItems, totalCartValue, productId, quantity, total));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String updateCartItem(String userId, double totalCartValue, String productId, int quantity, double total) {
        try{
            Future<String> future = cartCsvWriteExe.submit(() -> updateCartItemSerially(userId, totalCartValue, productId, quantity, total));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String removeCartItem(String userId, int totalCartItems, double totalCartValue, String productId) {
        try{
            Future<String> future = cartCsvWriteExe.submit(() -> removeCartItemSerially(userId, totalCartItems, totalCartValue, productId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String deleteCart(String userId) {
        try{
            Future<String> future = cartCsvWriteExe.submit(() -> deleteCartSerially(userId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setDBFilePath(String filePath){
        this.DBfilePath = filePath;
    }

    private Cart getCartSerially(String checkUserId){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for Cart CSV.");
        }
        else{
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath))) {
                String line;
                Cart cart = null;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    //To ensure no corrupted line is read.
                    if (count == 0 && fields.length == 3) {

                        if (fields[0].equals(checkUserId)) {
                            String userId = fields[0];
                            Integer noOfCartItems = Integer.parseInt(fields[1]);
                            double totalCartValue = Double.parseDouble(fields[2]);

                            cart = new Cart();
                            cart.setUserId(userId);
                            cart.setTotalCartValue(totalCartValue);
                            count = noOfCartItems;
                        }
                    }
                    else if(count > 0 && fields.length == 3) {

                        String productId = fields[0];
                        int quantityInCart = Integer.parseInt(fields[1]);
                        double totalCartItemCost = Double.parseDouble(fields[2]);

                        CartItem cartItem = new CartItem();
                        cartItem.setProductId(productId);
                        cartItem.setQuantity(quantityInCart);
                        cartItem.setTotal(totalCartItemCost);

                        cart.getCartItems().put(productId, cartItem);
                        count--;

                        if (count == 0) {
                            reader.close();
                            return cart;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading the Cart CSV file: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from Cart CSV: " + e.getMessage());
            }
        }

        return null;
    }

    private String addCartItemSerially(String userId, int totalCartItems, double totalCartValue, String productId, int quantity, double total){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for Cart CSV.");
        }
        else {
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(DBfilePath + ".tmp"))) {

                String line ;
                boolean toModify = true;
                while ((line = reader.readLine()) != null) {
                    if(toModify) {
                        String[] fields = line.split(",");

                        if(fields.length == 3){
                            if (fields[0].equals(userId)) {
                                line = userId + ',' + totalCartItems + "," + totalCartValue;
                                writer.write(line);
                                writer.newLine();
                                line = productId + ',' + quantity + "," + total;
                                toModify = false;
                            }
                        }
                    }

                    writer.write(line);
                    writer.newLine();

                }
                reader.close();
                writer.close();

            } catch (IOException e) {
                System.out.println("Error reading the Cart CSV file: " + e.getMessage());
            }catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from Cart CSV: " + e.getMessage());
            }

            String tempFilePath = DBfilePath + ".tmp"; // The path to the temporary file
            String existingFilePath = DBfilePath; // The path to the existing file

            // Create Paths for the temporary and existing files
            Path tempFile = Path.of(tempFilePath);
            Path existingFile = Path.of(existingFilePath);

            try {
                // Rename the temporary file to the existing file
                Files.move(tempFile, existingFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Cart CSV file added with new CartItem and extra rows removed successfully!");

                return "Cart DB Update operation successfull";
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to update Cart CSV file with modified data " + e.getMessage());
            }
        }

        return "Cart DB Update operation unsuccessfull";
    }

    private String addNewCartWithItemSerially(String userId, int totalCartItems, double totalCartValue, String productId, int quantity, double total) {
        if (DBfilePath.isEmpty()) {
            System.out.println("No file found at given filePath. Assign correct filePath for Cart CSV.");
        } else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(DBfilePath, true))) {
                String line = userId + ',' + totalCartItems + ',' + totalCartValue;
                writer.write(line);
                writer.newLine();

                line = productId + ',' + quantity + ',' + total;
                writer.write(line);
                writer.newLine();
                writer.close();

                System.out.println("Cart CSV DB file updated with new Cart and CartItem successfully!!");

                return "Cart DB Update operation successfull";

            } catch (IOException e) {
                System.out.println("Error writing to CSV file: " + e.getMessage());

            }
        }
        return "Cart DB Update operation unsuccessfull";
    }

    private String updateCartItemSerially(String userId, double totalCartValue, String productId, int quantity, double total){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for Cart CSV.");
        }
        else {
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(DBfilePath + ".tmp"))) {

                String line ;
                boolean toModify = true;
                while ((line = reader.readLine()) != null) {

                    String[] fields = line.split(",");

                        if(fields.length == 3) {
                            if(toModify) {
                                if (fields[0].equals(userId)) {
                                    line = userId + ',' + fields[1] + "," + totalCartValue;
                                } else if (fields[0].equals(productId)) {
                                    line = productId + ',' + quantity + "," + total;
                                    toModify = false;
                                }
                            }
                            writer.write(line);
                            writer.newLine();
                        }
                    }
                reader.close();
                writer.close();

            } catch (IOException e) {
                System.out.println("Error reading the Cart CSV file: " + e.getMessage());
            }catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from Cart CSV: " + e.getMessage());
            }

            String tempFilePath = DBfilePath + ".tmp"; // The path to the temporary file
            String existingFilePath = DBfilePath; // The path to the existing file

            // Create Paths for the temporary and existing files
            Path tempFile = Path.of(tempFilePath);
            Path existingFile = Path.of(existingFilePath);

            try {
                // Rename the temporary file to the existing file
                Files.move(tempFile, existingFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Cart CSV file updated and extra rows removed successfully!");

                return "Cart DB Update operation successfull";
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to update Cart CSV file with modified data " + e.getMessage());
            }
        }

        return "Cart DB Update operation unsuccessfull";
    }

    private String removeCartItemSerially(String userId, int totalCartItems, double totalCartValue, String productId){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for Cart CSV.");
        }
        else {
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(DBfilePath + ".tmp"))) {

                String line ;
                boolean toModify = true;

                while ((line = reader.readLine()) != null) {
                    boolean toSkip = false;
                    String[] fields = line.split(",");
                    if(fields.length == 3) {
                        if (toModify) {
                            if (fields[0].equals(userId)) {
                                line = userId + ',' + totalCartItems + "," + totalCartValue;
                            } else if (fields[0].equals(productId)) {
                                toSkip = true;
                                toModify = false;
                            }

                        }

                        if (!toSkip) {
                            writer.write(line);
                            writer.newLine();
                        }
                    }

                }
                reader.close();
                writer.close();

            } catch (IOException e) {
                System.out.println("Error reading the Cart CSV file: " + e.getMessage());
            }catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from Cart CSV: " + e.getMessage());
            }

            String tempFilePath = DBfilePath + ".tmp"; // The path to the temporary file
            String existingFilePath = DBfilePath; // The path to the existing file

            // Create Paths for the temporary and existing files
            Path tempFile = Path.of(tempFilePath);
            Path existingFile = Path.of(existingFilePath);

            try {
                // Rename the temporary file to the existing file
                Files.move(tempFile, existingFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Required cartitem removed successfully!");

                return "Cart DB Update operation successfull";
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to remove cartItem from Cart CSV file" + e.getMessage());
            }
        }

        return "Cart DB Update operation unsuccessfull";
    }

    private String deleteCartSerially(String userId){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for Cart CSV.");
        }
        else {
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(DBfilePath + ".tmp"))) {

                String line ;
                boolean toModify = true;
                int count = 0;

                while ((line = reader.readLine()) != null) {
                    boolean toSkip = false;

                    String[] fields = line.split(",");
                    if(fields.length == 3) {
                        if (fields[0].equals(userId)) {
                            count = Integer.parseInt(fields[1]);
                            toSkip = true;
                        } else if (count > 0) {
                            toSkip = true;
                            count--;
                        }

                        if (!toSkip) {
                            writer.write(line);
                            writer.newLine();
                        }
                    }

                }
                reader.close();
                writer.close();

            } catch (IOException e) {
                System.out.println("Error reading the Cart CSV file: " + e.getMessage());
            }catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from Cart CSV: " + e.getMessage());
            }

            String tempFilePath = DBfilePath + ".tmp"; // The path to the temporary file
            String existingFilePath = DBfilePath; // The path to the existing file

            // Create Paths for the temporary and existing files
            Path tempFile = Path.of(tempFilePath);
            Path existingFile = Path.of(existingFilePath);

            try {
                // Rename the temporary file to the existing file
                Files.move(tempFile, existingFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Cart removed successfully!");

                return "Cart DB Update operation successfull";
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to remove Cart from Cart CSV file" + e.getMessage());
            }
        }

        return "Cart DB Update operation unsuccessfull";
    }

    @Override
    public void shutdown() {
        try {
            // Attempt to shut down the executor service gracefully
            cartCsvWriteExe.shutdown();

            // Wait for a while to allow pending tasks to complete
            if (!cartCsvWriteExe.awaitTermination(5, TimeUnit.SECONDS)) {
                // Forceful shutdown if tasks don't complete in time
                System.err.println("UserCsvDB did not shut down gracefully. Forcing shutdown.");
                cartCsvWriteExe.shutdownNow();
            }

            // Close any open resources if necessary
            // For example, close the CSV file writer, database connections, etc.

        } catch (InterruptedException e) {
            // Handle any exceptions that occur during shutdown
            Thread.currentThread().interrupt(); // Preserve the interrupt status
        }
    }


}


