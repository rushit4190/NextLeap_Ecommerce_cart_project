package Ecommerce;


import Ecommerce.Repository.CartDBInterface;
import Ecommerce.Repository.CartItemDBInterface;
import Ecommerce.Repository.ProductDBInterface;
import Ecommerce.Repository.UserDBInterface;
import Ecommerce.Repository.RepositoryImpl.SQLiteRepoImpl.CartItemSQLiteDB;
import Ecommerce.Repository.RepositoryImpl.SQLiteRepoImpl.CartSQLiteDB;
import Ecommerce.Repository.RepositoryImpl.SQLiteRepoImpl.ProductSQLiteDB;
import Ecommerce.Repository.RepositoryImpl.SQLiteRepoImpl.UserSQLiteDB;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@SpringBootApplication
public class Main_WebAPI {

    public static void main(String[] args) {

        boolean flag = connectDatabase();

        if(flag) {
            SpringApplication.run(Main_WebAPI.class, args);
        }
        else{
            System.out.println("Failed to connect to database");
        }
    }

    private static boolean connectDatabase(){
        ExecutorService dataLoaderExecutor = Executors.newFixedThreadPool(4);
        String JDBCpath = "jdbc:sqlite:D:/study/Coding/NextLeap/E-Commerce_Cart/Ecommerce_cart.db";

        ProductDBInterface productSQLiteDB = ProductSQLiteDB.getInstance();
        UserDBInterface userSQLiteDB = UserSQLiteDB.getInstance();
        CartDBInterface cartSQLiteDB = CartSQLiteDB.getInstance();
        CartItemDBInterface cartItemSQLiteDB = CartItemSQLiteDB.getInstance();

        dataLoaderExecutor.submit(() -> productSQLiteDB.setDBFilePath(JDBCpath));
        dataLoaderExecutor.submit(() -> userSQLiteDB.setDBFilePath(JDBCpath));

        dataLoaderExecutor.submit(() -> cartSQLiteDB.setDBFilePath(JDBCpath));
        dataLoaderExecutor.submit(() -> cartItemSQLiteDB.setDBFilePath(JDBCpath));

        dataLoaderExecutor.shutdown();

        try{
            dataLoaderExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            return true;
        }catch (InterruptedException e){
            System.err.println("Interrupted while waiting for data loading tasks to complete.");
            return false;
        }


    }
}
