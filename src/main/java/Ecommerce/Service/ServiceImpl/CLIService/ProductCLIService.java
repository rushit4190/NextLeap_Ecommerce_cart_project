package Ecommerce.Service.ServiceImpl.CLIService;

import Ecommerce.Service.ProductService;
import Ecommerce.customExceptions.ProductNotFound;
import Ecommerce.customExceptions.QuanitityNotAvailException;
import Ecommerce.model.Product;
import Ecommerce.Repository.ProductDBInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCLIService implements ProductService {

    @Autowired
    private ProductDBInterface productDBInterface;

    public ProductCLIService(ProductDBInterface prodDBInterface){
        this.productDBInterface = prodDBInterface;
    }
    @Override
    public List<Product> searchProduct(String searchTerm) {

        List<Product> searchResults = productDBInterface.searchProductDB(searchTerm);

        if (searchResults.isEmpty()) {
            System.out.println("No products match the search term.");
        } else {
            System.out.println("Matching products in below format:");
//            System.out.println("Product_id ," + " Name ," + " Inventory Status ," + " MRP price ," + " Discount ," + " Max quantity available");
            for (Product product : searchResults) {
                System.out.print("Product ID : " + product.getProductId() + "     ");
                System.out.print("Product name : " + product.getName() + "     ");
                System.out.print("Product Inventory : " + product.getInventoryStatus() + "     ");
                System.out.print("Product price : " + product.getMrpPrice() +"     ");
                System.out.print("Product discount : " + product.getDiscount() +"     ");
                System.out.print("Product Max quantity : " + (product.getMaxQuantity() == null ? "null" : product.getMaxQuantity()) + "     ");
                System.out.println();
            }
        }
        return searchResults;
    }

    @Override
    public Product getProductInfoById(String productId) throws ProductNotFound {
        Product product = productDBInterface.getProductById(productId);

        if(product == null){
            throw new ProductNotFound("Desired product doesnt exist in DB. Please enter correct product Id");
        }
        return product;
    }

    @Override
    public List<Product> displayAllProducts() {
        List<Product> allProducts = productDBInterface.getAllProducts();

        if (allProducts.isEmpty()) {
            System.out.println("No products present in DB.");
        } else {
            System.out.println("Please find the products available in below format:");
//            System.out.println("Product_id ," + " Name ," + " Inventory Status ," + " MRP price ," + " Discount ," + " Max quantity available");
            for (Product product : allProducts) {
                System.out.print("Product ID : " + product.getProductId() + "     ");
                System.out.print("Product name : " + product.getName() + "     ");
                System.out.print("Product Inventory : " + product.getInventoryStatus() + "     ");
                System.out.print("Product price : " + product.getMrpPrice() +"     ");
                System.out.print("Product discount : " + product.getDiscount() +"     ");
                System.out.print("Product Max quantity : " + (product.getMaxQuantity() == null ? "null" : product.getMaxQuantity()) + "     ");
                System.out.println();
            }
        }
        return allProducts;
    }

    @Override
    public void updateProductInfo(String productId, int quantity) throws ProductNotFound, QuanitityNotAvailException {
        Product prodToBeUpdated = productDBInterface.getProductById(productId);

        if(prodToBeUpdated == null){
            throw new ProductNotFound("Desired product doesnt exist in DB. Please enter correct product Id");
        }

        int updatedQuantity = (prodToBeUpdated.getMaxQuantity()==null ? 0 : prodToBeUpdated.getMaxQuantity())- quantity;

        if(updatedQuantity < 0){
            throw new QuanitityNotAvailException("Desired quantity not available. Max available quantity is " + prodToBeUpdated.getMaxQuantity());
        }

        productDBInterface.updateProductDB(productId, updatedQuantity);

    }
}
