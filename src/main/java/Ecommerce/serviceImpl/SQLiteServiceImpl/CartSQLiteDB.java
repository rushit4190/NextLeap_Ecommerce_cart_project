package Ecommerce.serviceImpl.SQLiteServiceImpl;

import Ecommerce.model.Cart;
import Ecommerce.service.CartDBInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.concurrent.*;

public class CartSQLiteDB implements CartDBInterface {
    ExecutorService cartSQLiteWriteExe = Executors.newFixedThreadPool(1);

    String JDBCpath = "";

    private CartSQLiteDB(){

    }

    private static class Loader{
        final private static CartSQLiteDB INSTANCE = new CartSQLiteDB();
    }
    public static CartSQLiteDB getInstance(){
        return CartSQLiteDB.Loader.INSTANCE;
    }


    @Override
    public Cart getCart(String userId) {
        try{
            Future<Cart> future = cartSQLiteWriteExe.submit(() -> getCartSQLite(userId));
            return future.get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public String addCart(String CartId, String UserId) {
        try{
            Future<String> future = cartSQLiteWriteExe.submit(() -> addCartSQLite(CartId, UserId));
            return future.get();
        }catch (InterruptedException | ExecutionException e) {
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
            cartSQLiteWriteExe.shutdown();

            // Wait for a while to allow pending tasks to complete
            if (!cartSQLiteWriteExe.awaitTermination(5, TimeUnit.SECONDS)) {
                // Forceful shutdown if tasks don't complete in time
                System.err.println("Carts SQLite table connection did not shut down gracefully. Forcing shutdown.");
                cartSQLiteWriteExe.shutdownNow();
            }

            // Close any open resources if necessary
            // For example, close the CSV file writer, database connections, etc.

        } catch (InterruptedException e) {
            // Handle any exceptions that occur during shutdown
            Thread.currentThread().interrupt(); // Preserve the interrupt status
        }

    }

    private @Nullable Cart getCartSQLite(String UserId){
        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for Carts table is empty. Assign correctly.");
            return null;
        }
        Connection conn;
        Cart cart = null;

        try{
            conn = DriverManager.getConnection(JDBCpath);

            String query = "SELECT * FROM Carts WHERE UserID = ?";

            PreparedStatement psmt = conn.prepareStatement(query);
            psmt.setString(1, UserId);

            ResultSet rs = psmt.executeQuery();

            if(rs.next()){
                String CartId = rs.getString("CartID");
                cart = new Cart(CartId, UserId);
            }

            psmt.close();
            rs.close();
            conn.close();

        }catch (SQLException e){
            System.out.println("error : " + e.getMessage());
        }

        return cart;
    }

    private @NotNull String addCartSQLite(String CartId, String UserId){
        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for Carts table is empty");

        }
        else {
            Connection conn;
            try {
                conn = DriverManager.getConnection(JDBCpath);

                String update = "INSERT INTO Carts(CartID, UserID) Values (?, ?)";

                PreparedStatement psmt = conn.prepareStatement(update);
                psmt.setString(1, CartId);
                psmt.setString(2, UserId);

                int result = psmt.executeUpdate();

                psmt.close();
                conn.close();

                if (result > 0) {
                    System.out.println("Cart SQLite table updated with new Cart successfully!!");
                    return "Cart SQLite table Update operation successfull";
                }

            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }

        return "Cart SQLite table Update operation unsuccessfull";

    }
}
