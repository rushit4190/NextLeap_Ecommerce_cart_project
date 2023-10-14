package Ecommerce.service;

import Ecommerce.model.Product;

import java.util.List;

public interface ProductDBInterface {

    public Product getProductById(String productId);

    public List<Product> searchProductDB(String searchTerm);

    public String updateProductDB(String productId, int quantity);

    public List<Product> getAllProducts();
    public void setDBFilePath(String path);

    public void shutdown();
}
