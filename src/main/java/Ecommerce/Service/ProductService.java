package Ecommerce.Service;

import Ecommerce.customExceptions.ProductNotFound;
import Ecommerce.customExceptions.QuanitityNotAvailException;
import Ecommerce.model.Product;

import java.util.List;

public interface ProductService {

    public List<Product> searchProduct(String searchTerm);

    public Product getProductInfoById(String productId) throws ProductNotFound;

    public List<Product> displayAllProducts();

    public void updateProductInfo(String productId, int quantity) throws ProductNotFound, QuanitityNotAvailException;
}
