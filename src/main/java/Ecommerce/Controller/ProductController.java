package Ecommerce.Controller;

import Ecommerce.customExceptions.ProductNotFound;
import Ecommerce.customExceptions.QuanitityNotAvailException;
import Ecommerce.model.Product;

public interface ProductController {

    public void searchProduct(String searchTerm);

    public Product getProductInfoById(String productId) throws ProductNotFound;

    public void displayAllProducts();

    public void updateProductInfo(String productId, int quantity) throws ProductNotFound, QuanitityNotAvailException;
}
