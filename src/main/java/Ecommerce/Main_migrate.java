package Ecommerce;

import Ecommerce.model.Product;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main_migrate {

    public static List<Product> productCsvParser(){
        String DBfilePath = "D:/study/Coding/NextLeap/E-Commerce_Cart/src/main/java/Ecommerce/CSVfiles/productCatalog.csv";

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

        return allProducts;
    }

    public static void productCsvToSQLite(List<Product> allProducts) throws SQLException {
        String prodJDBC = "jdbc:sqlite:D:/study/Coding/NextLeap/E-Commerce_Cart/Ecommerce_cart.db";
        try {
            Connection conn = DriverManager.getConnection(prodJDBC);

            String insertProd = "INSERT INTO Product(ProductID, Name, InventoryStatus, MRPPrice, Discount, MaxQuantityAllowed) VALUES (?,?,?,?,?,?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertProd)) {
                for (Product product : allProducts) {
                    pstmt.setString(1, product.getProductId());
                    pstmt.setString(2, product.getName());
                    pstmt.setString(3, product.getInventoryStatus());
                    pstmt.setDouble(4, product.getMrpPrice());
                    pstmt.setDouble(5, product.getDiscount());
                    pstmt.setInt(6, (product.getMaxQuantity() == null ? 0: product.getMaxQuantity()));
                    pstmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.out.print("in Csv to SQLite " + e.getMessage());
        }

    }

    public static void main(String[] args) {
        List<Product> result = productCsvParser();
        try {
            productCsvToSQLite(result);
        } catch (SQLException e) {
            System.out.println("in main function");
        }
    }
}
