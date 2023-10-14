package Ecommerce.Repository.RepositoryImpl.SQLiteRepoImpl;

import Ecommerce.model.Product;
import Ecommerce.Repository.ProductDBInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Repository
public class ProductSQLiteDB implements ProductDBInterface {

    ExecutorService productSQLiteWriteExe = Executors.newFixedThreadPool(1);

    private String JDBCpath = "jdbc:sqlite:D:/study/Coding/NextLeap/E-Commerce_Cart/Ecommerce_cart.db";

    private ProductSQLiteDB(){

    }
    //Note the method to implement Singleton
    private static class Loader {
        final private static ProductSQLiteDB INSTANCE = new ProductSQLiteDB();
    }

    public static ProductSQLiteDB getInstance(){

        return ProductSQLiteDB.Loader.INSTANCE;
    }


    @Override
    public Product getProductById(String productId) {
        try{
            Future<Product> future = productSQLiteWriteExe.submit(() -> getProductByIdSQLite(productId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Product> searchProductDB(String searchTerm) {
        try{
            Future<List<Product>> future = productSQLiteWriteExe.submit(() -> searchProductSQLiteDB(searchTerm));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String updateProductDB(String productId, int quantity) {
        try{
            Future<String> future = productSQLiteWriteExe.submit(() -> updateProductSQLiteDB(productId, quantity));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Product> getAllProducts() {
        try{
            Future<List<Product>> future = productSQLiteWriteExe.submit(() -> getAllProductsSQLiteDB());
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setDBFilePath(String path) {
        this.JDBCpath = path;
    }

    @Override
    public void shutdown() {
        try {
            // Attempt to shut down the executor service gracefully
            productSQLiteWriteExe.shutdown();

            // Wait for a while to allow pending tasks to complete
            if (!productSQLiteWriteExe.awaitTermination(5, TimeUnit.SECONDS)) {
                // Forceful shutdown if tasks don't complete in time
                System.err.println("Product SQLite table connection did not shut down gracefully. Forcing shutdown.");
                productSQLiteWriteExe.shutdownNow();
            }

            // Close any open resources if necessary
            // For example, close the CSV file writer, database connections, etc.

        } catch (InterruptedException e) {
            // Handle any exceptions that occur during shutdown
            Thread.currentThread().interrupt(); // Preserve the interrupt status
        }
    }

    private @Nullable Product getProductByIdSQLite(String productId){
        Product product = null;

        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for Products table is empty");
            return null;
        }
        else {
            Connection conn;
            try {
                conn = DriverManager.getConnection(JDBCpath);
                String query = "SELECT * FROM Product WHERE ProductID = ?";

                PreparedStatement psmt = conn.prepareStatement(query);
                psmt.setString(1, productId);

                ResultSet rs = psmt.executeQuery();

                if (rs.next()) {
                    String name = rs.getString("Name");
                    String inventoryStatus = rs.getString("InventoryStatus");
                    double mrpPrice = rs.getDouble("MRPPrice");
                    double discount = rs.getDouble("Discount");
                    Integer maxQuantity = rs.getInt("MaxQuantityAllowed") == 0 ? null : rs.getInt("MaxQuantityAllowed");
                    product = new Product(productId, name, inventoryStatus, mrpPrice, discount, maxQuantity);
                }

                rs.close();
                psmt.close();
                conn.close();

            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }

        return product;
    }

    private @Nullable List<Product> getAllProductsSQLiteDB(){
        List<Product> allProducts = new ArrayList<>();

        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for Products table is empty");
        }
        else {
            Connection conn;
            try {
                conn = DriverManager.getConnection(JDBCpath);
                String query = "SELECT * FROM Product";

                PreparedStatement psmt = conn.prepareStatement(query);


                ResultSet rs = psmt.executeQuery();

                while (rs.next()) {
                    String productId = rs.getString("ProductID");
                    String name = rs.getString("Name");
                    String inventoryStatus = rs.getString("InventoryStatus");
                    double mrpPrice = rs.getDouble("MRPPrice");
                    double discount = rs.getDouble("Discount");
                    Integer maxQuantity = rs.getInt("MaxQuantityAllowed") == 0 ? null : rs.getInt("MaxQuantityAllowed");
                    Product product = new Product(productId, name, inventoryStatus, mrpPrice, discount, maxQuantity);
                    allProducts.add(product);
                }

                rs.close();
                psmt.close();
                conn.close();

            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }

        return allProducts;
    }

    private @Nullable List<Product> searchProductSQLiteDB(String searchTerm){
        List<Product> searchResults = new ArrayList<>();

        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for Products table is empty");
        }
        else{
            Connection conn;

            try{
                conn = DriverManager.getConnection(JDBCpath);
                String query = "SELECT * FROM Product WHERE LOWER(Name) LIKE '%' || LOWER(?) || '%'";

                PreparedStatement psmt = conn.prepareStatement(query);
                psmt.setString(1, searchTerm);

                ResultSet rs = psmt.executeQuery();

                while(rs.next()){
                    String productId = rs.getString("ProductID");
                    String name = rs.getString("Name");
                    String inventoryStatus = rs.getString("InventoryStatus");
                    double mrpPrice   = rs.getDouble("MRPPrice");
                    double discount = rs.getDouble("Discount");
                    Integer maxQuantity = rs.getInt("MaxQuantityAllowed") == 0 ? null : rs.getInt("MaxQuantityAllowed");
                    Product product = new Product(productId, name, inventoryStatus, mrpPrice, discount, maxQuantity);
                    searchResults.add(product);
                }

                rs.close();
                psmt.close();
                conn.close();

            }catch(SQLException e){
                System.out.println("Error : " + e.getMessage());
            }
        }

        return searchResults;
    }

    private @NotNull String updateProductSQLiteDB(String productId, int quantity){
        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for Products table is empty");
        }
        else {
            Connection conn;
            try {
                conn = DriverManager.getConnection(JDBCpath);
                String query = "UPDATE Product SET MaxQuantityAllowed = ?, InventoryStatus = ? WHERE ProductID = ?";

                PreparedStatement psmt = conn.prepareStatement(query);
                psmt.setInt(1, quantity);

                if ((quantity == 0)) {
                    psmt.setString(2, "Out of Stock");
                } else {
                    psmt.setString(2, "Available");
                }
                psmt.setString(3, productId);

                int result = psmt.executeUpdate();

                psmt.close();
                conn.close();

                if(result > 0){
                    System.out.println("Product SQLite table updated with new info successfully!!");
                    return "Product SQLite table Update operation successfull";
                }

            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }
        return "Product SQLite table Update operation unsuccessfull";
    }
}
