package Ecommerce.service.serviceImpl.CsvServiceImpl;

import Ecommerce.model.Cart;
import Ecommerce.service.CartDBInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.concurrent.*;

public class CartCsvDB implements CartDBInterface {

    ExecutorService cartCsvWriteExe = Executors.newFixedThreadPool(1);

        /*
    Since we are having a file as DB, we have to ensure that reading and writing takes place via same thread.
    In case of SQL DB, read do not need to be synchronised and write on particular table to be synchronised.
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
    public String addCart(String CartId, String UserId) {
        try{
            Future<String> future = cartCsvWriteExe.submit(() -> addCartSerially(CartId ,UserId));
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

    private @Nullable Cart getCartSerially(String checkUserId){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for Cart CSV.");
        }
        else{
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath))) {
                String line;
                Cart result;

                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    //To ensure no corrupted line is read.
                    if (fields.length == 2) {

                        if (fields[1].equals(checkUserId)) {
                            String cartId = fields[0];

                            result = new Cart(cartId, checkUserId);

                            return result;
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


    @Override
    public void shutdown() {
        try {
            // Attempt to shut down the executor service gracefully
            cartCsvWriteExe.shutdown();

            // Wait for a while to allow pending tasks to complete
            if (!cartCsvWriteExe.awaitTermination(5, TimeUnit.SECONDS)) {
                // Forceful shutdown if tasks don't complete in time
                System.err.println("CartCsvDB did not shut down gracefully. Forcing shutdown.");
                cartCsvWriteExe.shutdownNow();
            }

            // Close any open resources if necessary
            // For example, close the CSV file writer, database connections, etc.

        } catch (InterruptedException e) {
            // Handle any exceptions that occur during shutdown
            Thread.currentThread().interrupt(); // Preserve the interrupt status
        }
    }

    private @NotNull String addCartSerially(String CartId, String UserId){
        if (DBfilePath.isEmpty()) {
            System.out.println("No file found at given filePath. Assign correct filePath for Cart CSV.");
        } else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(DBfilePath, true))) {
                String line = CartId + ',' + UserId;
                writer.write(line);
                writer.newLine();

                System.out.println("Cart CSV DB file updated with new Cart successfully!!");

                return "Cart DB Update operation successfull";

            } catch (IOException e) {
                System.out.println("Error writing to CSV file: " + e.getMessage());
            }
        }
        return "Cart DB Update operation unsuccessfull";
    }

}


