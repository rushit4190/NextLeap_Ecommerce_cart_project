package Ecommerce.Repository.RepositoryImpl.CsvRepoImpl;

import Ecommerce.model.User;
import Ecommerce.Repository.UserDBInterface;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.concurrent.*;

public class UserCsvDB implements UserDBInterface {

    ExecutorService userCsvWriteDBExe = Executors.newFixedThreadPool(1);
    private String DBfilePath = "";
    private UserCsvDB(){

    }
    private static class Loader {
        final private static UserCsvDB INSTANCE = new UserCsvDB();
    }

    public static UserCsvDB getInstance(){
        return Loader.INSTANCE;
    }


    @Override
    public Integer addUser(User user) {
        try {
            Future<Integer> future = userCsvWriteDBExe.submit(() -> addUserSerially(user));
            return future.get(); // This blocks until the result is available
        } catch (InterruptedException | ExecutionException e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            return null; // or throw an exception
        }
    }

    // What to do about read threads?
    @Override
    public User getUserById(String userId) {
        try {
            Future<User> future = userCsvWriteDBExe.submit(() -> getUserByIdSerially(userId));
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
            Future<User> future = userCsvWriteDBExe.submit(() -> getUserByEmailSerially(email));
            return future.get(); // This blocks until the result is available
        } catch (InterruptedException | ExecutionException e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            return null; // or throw an exception
        }
    }


    @Override
    public void setDBFilePath(String path){
        this.DBfilePath = path;
    }

    private int addUserSerially(User user) {
        //Save to CSV
        if (DBfilePath.isEmpty()) {
            System.out.println("No file found at given filePath. Assign correct filePath for User CSV.");
            return 1;

        } else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(DBfilePath, true))) {
                String line = user.getUserId() + "," +
                        user.getEmail() + "," +
                        user.getFirstName() + "," +
                        user.getLastName() + "," +
                        user.getPassword();
                writer.write(line);
                writer.newLine();
                writer.close();
                System.out.println("User CSV DB file updated with new User successfully!!");
                return 2;

            } catch (IOException e) {
                System.out.println("Error writing to CSV file: " + e.getMessage());
                return 3;

            }

        }
    }


    private @Nullable User getUserByIdSerially(String checkUserId){
        //get User info by Id

        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for User CSV.");
        }
        else{
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath))) {
                String line;
                User user ;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    //To ensure no corrupted line is read.
                    if (fields.length >= 5) {

                        if (fields[0].equals(checkUserId)) {
                            String userId = fields[0];
                            String email = fields[1];
                            String firstName = fields[2];
                            String lastName = fields[3];
                            String password = fields[4];
                            user = new User(userId, email, firstName, lastName, password);
                            reader.close();
                            return user;
                        }

                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading the User CSV file: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from User CSV: " + e.getMessage());
            }
        }

        return null;
    }

    private @Nullable User getUserByEmailSerially(String checkEmail){
        //get User info by Email
        if(DBfilePath.isEmpty()){
            System.out.println("No file found at given filePath. Assign correct filePath for User CSV.");
        }
        else{
            try (BufferedReader reader = new BufferedReader(new FileReader(DBfilePath))) {
                String line;
                User user ;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    //To ensure no corrupted line is read.

                    if (fields.length >= 5) {
                        if (fields[1].equals(checkEmail)) {
                            String userId = fields[0];
                            String email = fields[1];
                            String firstName = fields[2];
                            String lastName = fields[3];
                            String password = fields[4];
                            user = new User(userId, email, firstName, lastName, password);
                            reader.close();
                            return user;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading the User CSV file: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing numeric values from User CSV: " + e.getMessage());
            }
        }
        return null;
    }

    public void shutdown() {
        try {
            // Attempt to shut down the executor service gracefully
            userCsvWriteDBExe.shutdown();

            // Wait for a while to allow pending tasks to complete
            if (!userCsvWriteDBExe.awaitTermination(5, TimeUnit.SECONDS)) {
                // Forceful shutdown if tasks don't complete in time
                System.err.println("UserCsvDB did not shut down gracefully. Forcing shutdown.");
                userCsvWriteDBExe.shutdownNow();
            }

            // Close any open resources if necessary
            // For example, close the CSV file writer, database connections, etc.

        } catch (InterruptedException e) {
            // Handle any exceptions that occur during shutdown
            Thread.currentThread().interrupt(); // Preserve the interrupt status
        }
    }

}
