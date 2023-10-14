package Ecommerce.Controller.APIController;

import Ecommerce.Service.ServiceImpl.CLIService.ProductCLIService;
import Ecommerce.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/EcommerceCart/")
@RestController
public class ProductAPIController {

    @Autowired
    private ProductCLIService productCLIService;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> displayAllProducts(){
        List<Product> response = productCLIService.displayAllProducts();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/searchProducts")
    public ResponseEntity<List<Product>> searchProductCatalog(@RequestBody Map<String, Object> requestBody){
        String searchTerm = (String) requestBody.getOrDefault("searchTerm", "");
        List<Product> response = productCLIService.searchProduct(searchTerm);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
