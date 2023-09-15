package Ecommerce.serviceImpl.CsvServiceImpl;


import Ecommerce.model.CartItem;
import Ecommerce.service.CartItemDBInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.*;

public class CartItemCsvDB implements CartItemDBInterface {

    ExecutorService cartItemCsvWriteExe = Executors.newFixedThreadPool(1);

        /*
    Since we are having a file as DB, we have to ensure that reading and writing takes place via same thread.
    In case of SQL DB, read do not need to be synchronised and write on particular table to be synchronised.
     */

    private String DBfilePath = "";

    private CartItemCsvDB(){

    }

    private static class Loader {
        final private static CartItemCsvDB INSTANCE = new CartItemCsvDB();
    }

    public static CartItemCsvDB getInstance(){

        return CartItemCsvDB.Loader.INSTANCE;
    }


    @Override
    public List<CartItem> getCartItems(String CartId) {
        try{
            Future<List<CartItem>> future = cartItemCsvWriteExe.submit(() -> getCartItemsListSerially(CartId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CartItem getCartItem(String CartId, String productId){
        try{
            Future<CartItem> future = cartItemCsvWriteExe.submit(() -> getCartItemSerially(CartId, productId ));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String addCartItem(String CartId, String productId, int quantity, double total) {
        try{
            Future<String> future = cartItemCsvWriteExe.submit(() -> addCartItemSerially(CartId, productId, quantity, total));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String updateCartItem(String CartId, String productId, int quantity, double total) {
        try{
            Future<String> future = cartItemCsvWriteExe.submit(() -> updateCartItemSerially(CartId, productId, quantity, total));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String removeCartItem(String CartId, String productId) {
        try{
            Future<String> future = cartItemCsvWriteExe.submit(() -> removeCartItemSerially(CartId, productId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Double getTotalCartValue(String CartId) {
        try{
            Future<Double> future = cartItemCsvWriteExe.submit(() -> getTotalCartValueSerially(CartId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public Integer getTotalCartItems(String CartId) {
        try{
            Future<Integer> future = cartItemCsvWriteExe.submit(() -> getTotalCartItemsSerially(CartId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setDBFilePath(String path) {
        this.DBfilePath = path;
    }

    @Override
    public void shutdown() {
        try {
            // Attempt to shut down the executor service gracefully
            cartItemCsvWriteExe.shutdown();

            // Wait for a while to allow pending tasks to complete
            if (!cartItemCsvWriteExe.awaitTermination(5, TimeUnit.SECONDS)) {
                // Forceful shutdown if tasks don't complete in time
                System.err.println("CartItemCsvDB did not shut down gracefully. Forcing shutdown.");
                cartItemCsvWriteExe.shutdownNow();
            }

            // Close any open resources if necessary
            // For example, close the CSV file writer, database connections, etc.

        } catch (InterruptedException e) {
            // Handle any exceptions that occur during shutdown
            Thread.currentThread().interrupt(); // Preserve the interrupt status
        }
    }

    private @Nullable List<CartItem> getCartItemsListSerially(String checkCartId){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for CartItem CSV.");
        }
        else{
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath))) {
                List<CartItem> result = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    //To ensure no corrupted line is read.
                    if (fields.length == 4) {

                        if (fields[0].equals(checkCartId)) {
                            String cartId = fields[0];
                            String productId = fields[1];
                            int quantity = Integer.parseInt(fields[2]);
                            double total = Double.parseDouble(fields[3]);
                            CartItem cartItem = new CartItem(cartId, productId, quantity, total);

                            result.add(cartItem);
                        }
                    }
                }
                return result;
            } catch (IOException e) {
                System.out.println("Error reading the CartItem CSV file: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from CartItem CSV: " + e.getMessage());
            }
        }

        return null;
    }

    private @Nullable CartItem getCartItemSerially(String checkCartId, String checkProductId){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for CartItem CSV.");
        }
        else{
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath))) {
                CartItem result = null;
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    //To ensure no corrupted line is read.
                    if (fields.length == 4) {

                        if (fields[0].equals(checkCartId) && fields[1].equals(checkProductId)) {
                            String cartId = fields[0];
                            String productId = fields[1];
                            int quantity = Integer.parseInt(fields[2]);
                            double total = Double.parseDouble(fields[3]);
                            result = new CartItem(cartId, productId, quantity, total);

                            return result;
                        }
                    }
                }

            } catch (IOException e) {
                System.out.println("Error reading the CartItem CSV file: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from CartItem CSV: " + e.getMessage());
            }
        }

        return null;
    }

    private @NotNull String addCartItemSerially(String CartId, String productId, int quantity, double total){
        if (DBfilePath.isEmpty()) {
            System.out.println("No file found at given filePath. Assign correct filePath for CartItem CSV.");
        } else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(DBfilePath, true))) {
                String line = CartId + ',' + productId + ',' + quantity + ',' + total;
                writer.write(line);
                writer.newLine();
                writer.close();

                System.out.println("CartItem CSV DB file added with new CartItem successfully!!");

                return "CartItem DB Update operation successfull";

            } catch (IOException e) {
                System.out.println("Error writing to CartItem CSV file: " + e.getMessage());

            }
        }
        return "CartItem DB Update operation unsuccessfull";
    }

    private @NotNull String updateCartItemSerially(String CartId, String productId, int quantity, double total){
        if (DBfilePath.isEmpty()) {
            System.out.println("No file found at given filePath. Assign correct filePath for CartItem CSV.");
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(DBfilePath + ".tmp"))) {

                String line ;
                boolean toModify = true;
                while ((line = reader.readLine()) != null) {

                    String[] fields = line.split(",");
                    if(fields.length == 4){
                        if(toModify) {
                            if(fields[0].equals(CartId) && fields[1].equals(productId)){
                                line = CartId + ',' + productId + ',' + quantity + ',' + total;
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
                System.out.println("Error writing to CartItem CSV file: " + e.getMessage());
            }

            String tempFilePath = DBfilePath + ".tmp"; // The path to the temporary file
            String existingFilePath = DBfilePath; // The path to the existing file

            // Create Paths for the temporary and existing files
            Path tempFile = Path.of(tempFilePath);
            Path existingFile = Path.of(existingFilePath);

            try {
                // Rename the temporary file to the existing file
                Files.move(tempFile, existingFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("CartItem CSV DB file updated with desired product and quantity!!");
                return "CartItem DB Update operation successfull";

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to update CartItem CSV file with modified data " + e.getMessage());
            }
        }
        return "CartItem DB Update operation unsuccessfull";
    }

    private @NotNull String removeCartItemSerially(String CartId, String productId){
        if (DBfilePath.isEmpty()) {
            System.out.println("No file found at given filePath. Assign correct filePath for CartItem CSV.");
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(DBfilePath + ".tmp"))) {

                String line ;

                while ((line = reader.readLine()) != null) {
                    boolean toSkip = false;
                    String[] fields = line.split(",");
                    if(fields.length == 4){
//                        System.out.println(Arrays.toString(fields) + " " + CartId + " " + productId);

                        if(fields[0].equals(CartId) && fields[1].equals(productId)){
                            toSkip = true;
//                            System.out.println("executed");
                        }
                        if(!toSkip) {
                            writer.write(line);
                            writer.newLine();
                        }
                    }
                }
                reader.close();
                writer.close();

            } catch (IOException e) {
                System.out.println("Error writing to CartItem CSV file: " + e.getMessage());
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
                return "CartItem DB Update operation successfull";

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to remove cartItem from Cart CSV file" + e.getMessage());
            }
        }
        return "CartItem DB Update operation unsuccessfull";
    }

    private @Nullable Double getTotalCartValueSerially(String CartId){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for CartItem CSV.");
        }
        else{
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath))) {
                Double result = 0.0;
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    //To ensure no corrupted line is read.
                    if (fields.length == 4) {

                        if (fields[0].equals(CartId)) {
                            result += Double.parseDouble(fields[3]);
                        }
                    }
                }
                return result;
            } catch (IOException e) {
                System.out.println("Error reading the CartItem CSV file: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from CartItem CSV: " + e.getMessage());
            }
        }

        return null;
    }

    private @Nullable Integer getTotalCartItemsSerially(String CartId){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for CartItem CSV.");
        }
        else{
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath))) {
                Integer result = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    //To ensure no corrupted line is read.
                    if (fields.length == 4) {

                        if (fields[0].equals(CartId)) {
                            result += 1;
                        }
                    }
                }
                return result;
            } catch (IOException e) {
                System.out.println("Error reading the CartItem CSV file: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from CartItem CSV: " + e.getMessage());
            }
        }

        return null;
    }

}
