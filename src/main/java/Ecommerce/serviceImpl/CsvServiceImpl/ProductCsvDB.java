package Ecommerce.serviceImpl.CsvServiceImpl;

import Ecommerce.model.Product;
import Ecommerce.service.ProductDBInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ProductCsvDB implements ProductDBInterface {

    ExecutorService productCsvWriteExe = Executors.newFixedThreadPool(1);

    /*
    Since we are having a file as DB, we have to ensure that reading and writing takes place via same thread.
    In case of SQL DB, read do not need to be synchronised and write on particular table to be synchronized.
     */

    private String DBfilePath = "";

    private ProductCsvDB(){

    }

    //Note the method to implement Singleton
    private static class Loader {
        final private static ProductCsvDB INSTANCE = new ProductCsvDB();
    }

    public static ProductCsvDB getInstance(){

        return Loader.INSTANCE;
    }
    @Override
    public Product getProductById(String productId) {
        try{
            Future<Product> future = productCsvWriteExe.submit(() -> getProductByIdSerially(productId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Product> searchProductDB(String searchTerm) {
        try{
            Future<List<Product>> future = productCsvWriteExe.submit(() -> searchProductDBSerially(searchTerm));

            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String updateProductDB(String productId, int quantity) {
        try{
            Future<String> future = productCsvWriteExe.submit(() -> updateProductDBSerially(productId, quantity));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Product> getAllProducts() {
        return getAllProductsSerially();
    }

    @Override
    public void setDBFilePath(String path){
        this.DBfilePath = path;
    }

    private @Nullable Product getProductByIdSerially(String prodId){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for Product Catalog.");
        }
        else{
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath))) {
                String line;
                Product product;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length >= 6) {
                        if(fields[0].equals(prodId)) {
                            String productId = fields[0];
                            String name = fields[1];
                            String inventoryStatus = fields[2];
                            double mrpPrice = Double.parseDouble(fields[3]);
                            double discount = Double.parseDouble(fields[4]);
                            Integer maxQuantity = (fields[5].equals("null")) ? null : Integer.parseInt(fields[5]);
                            product = new Product(productId, name, inventoryStatus, mrpPrice, discount, maxQuantity);
                            reader.close();
                            return product;
                        }

                    }
                }
//                System.out.println(products.size());
            } catch (IOException e) {
                System.out.println("Error reading the Product CSV file: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from Product CSV: " + e.getMessage());
            }
        }
        return null;
    }

    private @Nullable List<Product> searchProductDBSerially(String searchTerm){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for Product Catalog.");

        }
        else{
            // Should this be thread local?
            List<Product> searchResults = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath))) {
                String line;
                Product product ;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length >= 6) {
                        if(fields[1].toLowerCase().contains(searchTerm.toLowerCase())) {
                            String productId = fields[0];
                            String name = fields[1];
                            String inventoryStatus = fields[2];
                            double mrpPrice = Double.parseDouble(fields[3]);
                            double discount = Double.parseDouble(fields[4]);
                            Integer maxQuantity = (fields[5].equals("null")) ? null : Integer.parseInt(fields[5]);
                            product = new Product(productId, name, inventoryStatus, mrpPrice, discount, maxQuantity);

                            searchResults.add(product);
                        }

                    }
                }
                reader.close();
                return searchResults;
//                System.out.println(products.size());
            } catch (IOException e) {
                System.out.println("Error reading the Product CSV file: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from Product CSV: " + e.getMessage());
            }
        }

        return null;
    }

    private @NotNull String updateProductDBSerially(String productId, int quantity){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for Product Catalog.");
        }
        else {
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(DBfilePath + ".tmp"))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    boolean shouldModify = false;


                    if (fields.length >= 6) {
                        if (fields[0].equals(productId)) {
                            shouldModify = true;
                        }

                        // Write the fields if it should be retained
                        if (shouldModify) {
                            line = productId + "," +
                                    fields[1] + "," +
                                    ((quantity == 0) ? "Out of Stock" : "Available") + "," +
                                    fields[3] + "," +
                                    fields[4]+"," +
                                    (quantity == 0 ? "null" : quantity);
                        }
                        writer.write(line);
                        writer.newLine();
                    }
                }
                reader.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error occurred while updating Product CSV DB" + e.getMessage());
            }

            String tempFilePath = DBfilePath + ".tmp"; // The path to the temporary file
            String existingFilePath = DBfilePath; // The path to the existing file

            // Create Paths for the temporary and existing files
            Path tempFile = Path.of(tempFilePath);
            Path existingFile = Path.of(existingFilePath);

            try {
                // Rename the temporary file to the existing file
                Files.move(tempFile, existingFile, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Product CSV file updated and extra rows removed successfully!");

                return "Product DB Update operation successfull";

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to update Product CSV file with modified data :" + e.getMessage());
            }

        }


        return "Product DB Update operation unsuccessfull";
    }

    private @Nullable List<Product> getAllProductsSerially(){
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for Product Catalog.");
        }
        else{
            // Should this be thread local?
            List<Product> allProducts = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath))) {
                String line;
                Product product ;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length >= 6) {

                        String productId = fields[0];
                        String name = fields[1];
                        String inventoryStatus = fields[2];
                        double mrpPrice = Double.parseDouble(fields[3]);
                        double discount = Double.parseDouble(fields[4]);
                        Integer maxQuantity = (fields[5].equals("null")) ? null : Integer.parseInt(fields[5]);
                        product = new Product(productId, name, inventoryStatus, mrpPrice, discount, maxQuantity);

                        allProducts.add(product);

                    }
                }
                reader.close();
                return allProducts;
//                System.out.println(products.size());
            } catch (IOException e) {
                System.out.println("Error reading the Product CSV file: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from Product CSV: " + e.getMessage());
            }
        }

        return null;
    }
    public void shutdown() {
        try {
            // Attempt to shut down the executor service gracefully
            productCsvWriteExe.shutdown();

            // Wait for a while to allow pending tasks to complete
            if (!productCsvWriteExe.awaitTermination(5, TimeUnit.SECONDS)) {
                // Forceful shutdown if tasks don't complete in time
                System.err.println("UserCsvDB did not shut down gracefully. Forcing shutdown.");
                productCsvWriteExe.shutdownNow();
            }

            // Close any open resources if necessary
            // For example, close the CSV file writer, database connections, etc.

        } catch (InterruptedException e) {
            // Handle any exceptions that occur during shutdown
            Thread.currentThread().interrupt(); // Preserve the interrupt status
        }
    }

}
