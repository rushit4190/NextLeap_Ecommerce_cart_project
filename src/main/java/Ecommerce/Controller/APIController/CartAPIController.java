package Ecommerce.Controller.APIController;

import Ecommerce.Service.ServiceImpl.CLIService.CartCLIService;
import Ecommerce.Service.ServiceImpl.CLIService.ProductCLIService;
import Ecommerce.customExceptions.CartItemNotAvail;
import Ecommerce.customExceptions.ProductNotFound;
import Ecommerce.customExceptions.QuanitityNotAvailException;
import Ecommerce.customExceptions.SessionExpiredException;
import Ecommerce.model.Cart;
import Ecommerce.model.CartItem;
import Ecommerce.model.Product;
import Ecommerce.Repository.RepositoryImpl.SQLiteRepoImpl.CartItemSQLiteDB;
import Ecommerce.Repository.RepositoryImpl.SessionInfoInMemDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/EcommerceCart/")
@RestController
public class CartAPIController {

    @Autowired
    ProductCLIService productCLIService;
    @Autowired
    CartCLIService cartCLIService;
    @Autowired
    CartItemSQLiteDB cartItemSQLiteDB;


    @GetMapping("/cart")
    public ResponseEntity<Map<String, Object>> getUserCart(@RequestBody Map<String, Object> requestBody){

        String sessionId = (String) requestBody.getOrDefault("sessionId","");
        Map<String, Object> response = new HashMap<>();

        if (sessionId.isEmpty()) {
            response.put("message", "Invalid request body");
            return new ResponseEntity<>(response , HttpStatus.BAD_REQUEST);
        }
        List<CartItem> cartItems;
        response.put("cartItems", null);

        try{
            cartItems = cartCLIService.getCartItems(sessionId);

        } catch (SessionExpiredException e) {
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response , HttpStatus.FORBIDDEN);

        } catch (CartItemNotAvail e) {
            response.put("message", "Cart is empty for the user.");
            return new ResponseEntity<>(response , HttpStatus.NO_CONTENT);
        }

        response.put("message", "Cart retrieval successfull for the user");
        response.put("cartItems", cartItems);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/cart/CartItem")
    public ResponseEntity<String> addCartItem(@RequestBody Map<String, Object> requestBody){
        String sessionId = (String) requestBody.getOrDefault("sessionId","");
        String productId = (String) requestBody.getOrDefault("productId","");
        int quantityToAdd = (int) requestBody.getOrDefault("quantityToAdd","");

        if(sessionId.isEmpty() || productId.isEmpty() || quantityToAdd <= 0){
            return new ResponseEntity<>("Invalid request body", HttpStatus.BAD_REQUEST);
        }

        String status ;

        try{
            status = cartCLIService.addCartItem(sessionId, productId, quantityToAdd);

        } catch (SessionExpiredException e) {

            return new ResponseEntity<>(e.getMessage() , HttpStatus.FORBIDDEN);

        } catch (ProductNotFound | QuanitityNotAvailException e) {
            return new ResponseEntity<>(e.getMessage() , HttpStatus.BAD_REQUEST);

        }

        return new ResponseEntity<>(status, HttpStatus.CREATED);

    }

    @PutMapping("/cart/CartItem")
    public ResponseEntity<String> updateCartItem(@RequestBody Map<String, Object> requestBody) {
        String sessionId = (String) requestBody.getOrDefault("sessionId", "");
        String productId = (String) requestBody.getOrDefault("productId", "");
        int quantityToUpdate = (int) requestBody.getOrDefault("quantityToUpdate", "");

        if (sessionId.isEmpty() || productId.isEmpty() || quantityToUpdate <= 0) {
            return new ResponseEntity<>("Invalid request body", HttpStatus.BAD_REQUEST);
        }

        String status ;

        try{
            status = cartCLIService.updateCartItem(sessionId,productId, quantityToUpdate);

        } catch (SessionExpiredException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
        catch (ProductNotFound | QuanitityNotAvailException e) {
            return new ResponseEntity<>(e.getMessage() , HttpStatus.BAD_REQUEST);

        } catch (CartItemNotAvail e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(status, HttpStatus.OK);

    }

    @DeleteMapping("/cart/CartItem")
    public ResponseEntity<String> removeCartItem(@RequestBody Map<String, Object> requestBody){
        String sessionId = (String) requestBody.getOrDefault("sessionId", "");
        String productId = (String) requestBody.getOrDefault("productId", "");

        if (sessionId.isEmpty() || productId.isEmpty() ) {
            return new ResponseEntity<>("Invalid request body", HttpStatus.BAD_REQUEST);
        }
        String status;

        try{
            status = cartCLIService.removeCartItem(sessionId, productId);

        } catch (ProductNotFound e) {
            return new ResponseEntity<>(e.getMessage() , HttpStatus.BAD_REQUEST);

        } catch (SessionExpiredException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);

        } catch (CartItemNotAvail e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(status, HttpStatus.OK);

    }

}
