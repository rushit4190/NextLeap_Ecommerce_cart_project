package Ecommerce.service.serviceImpl.SQLiteServiceImpl;

import Ecommerce.model.User;
import Ecommerce.service.UserDBInterface;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.concurrent.*;

@Service
public class UserSQLiteDB implements UserDBInterface {
    ExecutorService userSQLiteWriteDBExe = Executors.newFixedThreadPool(1);
    private String JDBCpath = "jdbc:sqlite:D:/study/Coding/NextLeap/E-Commerce_Cart/Ecommerce_cart.db";
    private UserSQLiteDB(){

    }
    private static class Loader {
        final private static UserSQLiteDB INSTANCE = new UserSQLiteDB();
    }

    public static UserSQLiteDB getInstance(){
        return UserSQLiteDB.Loader.INSTANCE;
    }

    @Override
    public Integer addUser(User user) {
        try {
            Future<Integer> future = userSQLiteWriteDBExe.submit(() -> addUserSQLite(user));
            return future.get(); // This blocks until the result is available
        } catch (InterruptedException | ExecutionException e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            return null; // or throw an exception
        }
    }

    @Override
    public User getUserById(String userId) {
        try {
            Future<User> future = userSQLiteWriteDBExe.submit(() -> getUserByIdSQLite(userId));
            return future.get(); // This blocks until the result is available
        } catch (InterruptedException | ExecutionException e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            return null; // or throw an exception
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try {
            Future<User> future = userSQLiteWriteDBExe.submit(() -> getUserByEmailSQLite(email));
            return future.get(); // This blocks until the result is available
        } catch (InterruptedException | ExecutionException e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            return null; // or throw an exception
        }
    }

    @Override
    public void setDBFilePath(String path) {
        this.JDBCpath = path;
    }

    @Override
    public void shutdown() {
        try{
            // Attempt to shut down the executor service gracefully
            userSQLiteWriteDBExe.shutdown();

            // Wait for a while to allow pending tasks to complete
            if (!userSQLiteWriteDBExe.awaitTermination(5, TimeUnit.SECONDS)) {
                // Forceful shutdown if tasks don't complete in time
                System.err.println("User SQLite table connection did not shut down gracefully. Forcing shutdown.");
                userSQLiteWriteDBExe.shutdownNow();
            }

            // Close any open resources if necessary
            // For example, close the CSV file writer, database connections, etc.

        } catch (InterruptedException e) {
            // Handle any exceptions that occur during shutdown
            Thread.currentThread().interrupt(); // Preserve the interrupt status
        }
    }

    private int addUserSQLite(User user){
        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for Users table is empty");
        }
        else {
            Connection conn;
            try {
                conn = DriverManager.getConnection(JDBCpath);
                String update = "INSERT INTO Users(UserID,Email, FirstName, LastName, Password) Values (?,?,?,?,?)";

                PreparedStatement psmt = conn.prepareStatement(update);

                psmt.setString(1, user.getUserId());
                psmt.setString(2, user.getEmail());
                psmt.setString(3, user.getFirstName());
                psmt.setString(4, user.getLastName());
                psmt.setString(5, user.getPassword());

                int result = psmt.executeUpdate();

                psmt.close();
                conn.close();

                if (result > 0) {
                    System.out.println("User SQLite table updated with new User successfully!!");
                    return 2;
                }
            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }

        return 0;
    }

    private @Nullable User getUserByIdSQLite(String userId){
        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for Users table is empty");
            return null;
        }

        Connection conn;
        User user = null;

        try{
            conn = DriverManager.getConnection(JDBCpath);
            String query = "SELECT * FROM Users WHERE UserID = ?";

            PreparedStatement psmt = conn.prepareStatement(query);
            psmt.setString(1, userId);

            ResultSet rs = psmt.executeQuery();

            if(rs.next()){
                String email = rs.getString("Email");
                String firstName = rs.getString("FirstName");
                String lastName  = rs.getString("LastName");
                String password = rs.getString("Password");

                user = new User(userId, email, firstName, lastName, password);
            }

            rs.close();
            psmt.close();
            conn.close();

        }catch(SQLException e){
            System.out.println("Error : " + e.getMessage());
        }

        return user;
    }

    private @Nullable User getUserByEmailSQLite(String email){
        if(JDBCpath.isEmpty()){
            System.out.println(" JDBC url for Users table is empty");
            return null;
        }

        Connection conn;
        User user = null;

        try{
            conn = DriverManager.getConnection(JDBCpath);
            String query = "SELECT * FROM Users WHERE Email = ?";

            PreparedStatement psmt = conn.prepareStatement(query);
            psmt.setString(1, email);

            ResultSet rs = psmt.executeQuery();

            if(rs.next()){
                String userId = rs.getString("UserID");
                String firstName = rs.getString("FirstName");
                String lastName  = rs.getString("LastName");
                String password = rs.getString("Password");

                user = new User(userId, email, firstName, lastName, password);
            }

            rs.close();
            psmt.close();
            conn.close();

        }catch(SQLException e){
            System.out.println("Error : " + e.getMessage());
        }

        return user;
    }
}
