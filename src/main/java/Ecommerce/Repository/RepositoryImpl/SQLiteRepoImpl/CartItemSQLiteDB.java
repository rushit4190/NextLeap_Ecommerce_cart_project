package Ecommerce.service.serviceImpl.SQLiteServiceImpl;

import Ecommerce.model.CartItem;
import Ecommerce.service.CartItemDBInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class CartItemSQLiteDB implements CartItemDBInterface {

    ExecutorService cartItemSQLiteWriteExe = Executors.newFixedThreadPool(1);

    private String JDBCpath = "jdbc:sqlite:D:/study/Coding/NextLeap/E-Commerce_Cart/Ecommerce_cart.db";

    private CartItemSQLiteDB(){
    }

    private static class Loader {
        final private static CartItemSQLiteDB INSTANCE = new CartItemSQLiteDB();
    }

    public static CartItemSQLiteDB getInstance(){
        return CartItemSQLiteDB.Loader.INSTANCE;
    }

    @Override
    public List<CartItem> getCartItems(String CartId) {
        try{
            Future<List<CartItem>> future = cartItemSQLiteWriteExe.submit(() -> getCartItemsListSQLite(CartId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CartItem getCartItem(String CartId, String ProductId) {
        try{
            Future<CartItem> future = cartItemSQLiteWriteExe.submit(() -> getCartItemSQLite(CartId, ProductId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String addCartItem(String CartId, String productId, int quantity, double total) {
        try{
            Future<String> future = cartItemSQLiteWriteExe.submit(() -> addCartItemSQLite(CartId, productId, quantity, total));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String updateCartItem(String CartId, String productId, int quantity, double total) {
        try{
            Future<String> future = cartItemSQLiteWriteExe.submit(() -> updateCartItemSQLite(CartId, productId, quantity, total));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String removeCartItem(String CartId, String productId) {
        try{
            Future<String> future = cartItemSQLiteWriteExe.submit(() -> removeCartItemSQLite(CartId, productId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Double getTotalCartValue(String CartId) {
        try{
            Future<Double> future = cartItemSQLiteWriteExe.submit(() -> getTotalCartValueSQLite(CartId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Integer getTotalCartItems(String CartId) {
        try{
            Future<Integer> future = cartItemSQLiteWriteExe.submit(() -> getTotalCartItemsSQLite(CartId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
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
            cartItemSQLiteWriteExe.shutdown();

            // Wait for a while to allow pending tasks to complete
            if (!cartItemSQLiteWriteExe.awaitTermination(5, TimeUnit.SECONDS)) {
                // Forceful shutdown if tasks don't complete in time
                System.err.println("CartItem SQLite connection did not shut down gracefully. Forcing shutdown.");
                cartItemSQLiteWriteExe.shutdownNow();
            }

            // Close any open resources if necessary
            // For example, close the CSV file writer, database connections, etc.

        } catch (InterruptedException e) {
            // Handle any exceptions that occur during shutdown
            Thread.currentThread().interrupt(); // Preserve the interrupt status
        }
    }

    private @Nullable List<CartItem> getCartItemsListSQLite(String cartId){
        List<CartItem> allItems = new ArrayList<>();

        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for CartItems table is empty");
        }
        else {
            Connection conn;
            try {
                conn = DriverManager.getConnection(JDBCpath);
                String query = "SELECT * FROM CartItems WHERE CartID = ? ";

                PreparedStatement psmt = conn.prepareStatement(query);
                psmt.setString(1, cartId);

                ResultSet rs = psmt.executeQuery();

                while (rs.next()) {
                    String productId = rs.getString("ProductID");
                    int quantity = rs.getInt("Quantity");
                    double total = rs.getDouble("TotalCost");

                    CartItem cartItem = new CartItem(cartId, productId, quantity, total);
                    allItems.add(cartItem);
                }

                rs.close();
                psmt.close();
                conn.close();

            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }
        return allItems;
    }

    private @Nullable CartItem getCartItemSQLite(String cartId, String productId){
        CartItem cartItemRequired = null;

        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for CartItems table is empty");
        }
        else {
            Connection conn;
            try {
                conn = DriverManager.getConnection(JDBCpath);
                String query = "SELECT * FROM CartItems WHERE CartID = ? AND ProductID = ?";

                PreparedStatement psmt = conn.prepareStatement(query);
                psmt.setString(1, cartId);
                psmt.setString(2, productId);

                ResultSet rs = psmt.executeQuery();

                if(rs.next()) {
                    int quantity = rs.getInt("Quantity");
                    double total = rs.getDouble("TotalCost");
                    cartItemRequired = new CartItem(cartId, productId, quantity, total);

                }

                rs.close();
                psmt.close();
                conn.close();

            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }
        return cartItemRequired;
    }

    private @NotNull String addCartItemSQLite(String cartId, String productId, int quantity, double total){
        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for CartItems table is empty");
        }
        else {
            Connection conn;
            try {
                conn = DriverManager.getConnection(JDBCpath);
                String query = "INSERT INTO CartItems(CartID, ProductID, Quantity, TotalCost) VALUES(?,?,?,?)";

                PreparedStatement psmt = conn.prepareStatement(query);
                psmt.setString(1, cartId);
                psmt.setString(2, productId);
                psmt.setInt(3, quantity);
                psmt.setDouble(4, total);

                int result = psmt.executeUpdate();

                psmt.close();
                conn.close();

                if(result > 0){
                    System.out.println("CartItems SQLite table added with new CartItem successfully!!");
                    return "CartItems SQLite table Update operation successfull";
                }

            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }
        return "CartItems SQLite table Update operation unsuccessfull";
    }

    private @NotNull String updateCartItemSQLite(String cartId, String productId, int quantity, double total){
        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for CartItems table is empty");
        }
        else {
            Connection conn;
            try {
                conn = DriverManager.getConnection(JDBCpath);
                String query = "UPDATE CartItems SET Quantity = ?, TotalCost = ? WHERE CartID = ? AND ProductID = ?";

                PreparedStatement psmt = conn.prepareStatement(query);
                psmt.setInt(1, quantity);
                psmt.setDouble(2, total);
                psmt.setString(3, cartId);
                psmt.setString(4, productId);

                int result = psmt.executeUpdate();

                psmt.close();
                conn.close();

                if(result > 0){
                    System.out.println("CartItems SQLite table updated with desired product and quantity successfully!!");
                    return "CartItems SQLite table Update operation successfull";
                }

            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }
        return "CartItems SQLite table Update operation unsuccessfull";
    }

    private @NotNull String removeCartItemSQLite(String cartId, String productId){
        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for CartItems table is empty");
        }
        else {
            Connection conn;
            try {
                conn = DriverManager.getConnection(JDBCpath);
                String query = "DELETE FROM CartItems WHERE CartID = ? AND ProductID = ?";

                PreparedStatement psmt = conn.prepareStatement(query);

                psmt.setString(1, cartId);
                psmt.setString(2, productId);

                int result = psmt.executeUpdate();

                psmt.close();
                conn.close();

                if(result > 0){
                    System.out.println("Required cartitem removed successfully from CartItems SQLite table !!");
                    return "CartItems SQLite table Update operation successfull";
                }

            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }
        return "CartItems SQLite table Update operation unsuccessfull";
    }

    private @Nullable Double getTotalCartValueSQLite(String cartId){
        Double totalCartValue = null;

        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for CartItems table is empty");
        }
        else {
            Connection conn;
            try {
                conn = DriverManager.getConnection(JDBCpath);
                String query = "SELECT SUM(TotalCost) FROM CartItems WHERE CartID = ?";

                PreparedStatement psmt = conn.prepareStatement(query);

                psmt.setString(1, cartId);
                ResultSet rs = psmt.executeQuery();

                if(rs.next()){
                    totalCartValue = rs.getDouble(1);
                }

                rs.close();
                psmt.close();
                conn.close();

            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }
        return totalCartValue;
    }

    private @Nullable Integer getTotalCartItemsSQLite(String cartId){
        Integer totalCartItems = null;

        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for CartItems table is empty");
        }
        else {
            Connection conn;
            try {
                conn = DriverManager.getConnection(JDBCpath);
                String query = "SELECT COUNT(*) FROM CartItems WHERE CartID = ?";

                PreparedStatement psmt = conn.prepareStatement(query);

                psmt.setString(1, cartId);
                ResultSet rs = psmt.executeQuery();

                if(rs.next()){
                    totalCartItems = rs.getInt(1);
                }

                rs.close();
                psmt.close();
                conn.close();

            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }
        return totalCartItems;
    }

}
