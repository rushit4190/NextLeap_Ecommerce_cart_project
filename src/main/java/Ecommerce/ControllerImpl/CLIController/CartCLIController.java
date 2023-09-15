package Ecommerce.ControllerImpl.CLIController;

import Ecommerce.Controller.CartController;
import Ecommerce.customExceptions.CartItemNotAvail;
import Ecommerce.customExceptions.ProductNotFound;
import Ecommerce.customExceptions.QuanitityNotAvailException;
import Ecommerce.customExceptions.SessionExpiredException;
import Ecommerce.model.Cart;
import Ecommerce.model.CartItem;
import Ecommerce.model.Product;
import Ecommerce.service.CartDBInterface;
import Ecommerce.service.CartItemDBInterface;
import Ecommerce.service.ProductDBInterface;
import Ecommerce.serviceImpl.SessionInfoInMemDB;


import java.util.List;
import java.util.UUID;

public class CartCLIController implements CartController {

    private CartDBInterface cartDBInterface;

    private ProductDBInterface productDBInterface;

    private CartItemDBInterface cartItemDBInterface;

    public CartCLIController(CartDBInterface cartDBInterf, ProductDBInterface prodDBinterf, CartItemDBInterface cartItemDBInterf){
        this.cartDBInterface = cartDBInterf;
        this.productDBInterface = prodDBinterf;
        this.cartItemDBInterface = cartItemDBInterf;
    }
    @Override
    public String addCartItem(String sessionId, String productId, int quantity) throws SessionExpiredException, ProductNotFound, QuanitityNotAvailException {
        String userId = getUserIdFromSessionId(sessionId);
        Product prodToAdd = getProductInfo(productId);
        checkAvailQuantity(prodToAdd, quantity);
        double total = getTotalCost(prodToAdd, quantity);

        Cart cartToAdd = cartDBInterface.getCart(userId);

        if(cartToAdd == null){
            String cartId = generateCartId();
            cartDBInterface.addCart(cartId, userId);
            cartToAdd = new Cart(cartId, userId);
        }
        cartItemDBInterface.addCartItem(cartToAdd.getCartId(), productId, quantity, total);
        updateProductInfo(prodToAdd, quantity);
        return "Required product added to the cart";
    }

    @Override
    public String updateCartItem(String sessionId, String productId, int quantity) throws SessionExpiredException, CartItemNotAvail, ProductNotFound, QuanitityNotAvailException {
        if(quantity == 0){
            return removeCartItem(sessionId, productId);
        }

        String userId = getUserIdFromSessionId(sessionId);
        Cart cartToUpdate = cartDBInterface.getCart(userId);

        if(cartToUpdate == null){
            throw new CartItemNotAvail("Cart is not available for the user. Add cart item first");
        }

        CartItem cartItem = cartItemDBInterface.getCartItem(cartToUpdate.getCartId(), productId);

        if(cartItem == null){
            throw new CartItemNotAvail("Cart doesnt contain the desired product. Add cart item");
        }
        int oldQuantity = cartItem.getQuantity();
        if(oldQuantity == quantity) { return "Updated Successfully";} // nothing to update


        Product prodToAdd = getProductInfo(productId);
        Integer maxAvailableQuantity = prodToAdd.getMaxQuantity();

        int availableQuantity = (maxAvailableQuantity == null ? 0 : maxAvailableQuantity);
        int totalAvailable = oldQuantity + availableQuantity;

        if(totalAvailable >= quantity){
            double total = getTotalCost(prodToAdd, quantity);
            // Update Cart to CSV
            cartItemDBInterface.updateCartItem(cartToUpdate.getCartId(), productId, quantity, total);

            //Update Product catalog
            updateProductInfo(prodToAdd, quantity-oldQuantity);

            return  "Required product quantity updated in the cart";
        }
        else{
            throw new QuanitityNotAvailException("Desired quantity not available. Max available quantity is " + totalAvailable);
        }

    }

    @Override
    public String removeCartItem(String sessionId, String productId) throws SessionExpiredException, CartItemNotAvail, ProductNotFound {
        String userId = getUserIdFromSessionId(sessionId);
        Cart cartToRemove = cartDBInterface.getCart(userId);

        if(cartToRemove == null){
            throw new CartItemNotAvail("Cart is not available for the user.");
        }

        CartItem cartItem = cartItemDBInterface.getCartItem(cartToRemove.getCartId(), productId);

        if(cartItem == null){
            throw new CartItemNotAvail("Cart doesn't contain the desired cartItem. CartItem already removed");
        }

        cartItemDBInterface.removeCartItem(cartToRemove.getCartId(), productId);

        int cartQuantity = cartItem.getQuantity();
        Product prodToBeUpdated = getProductInfo(productId);
        //Update Product catalog
        updateProductInfo(prodToBeUpdated, -cartQuantity);

        return "Cart Item removed successfully";
    }

    @Override
    public String getCartItems(String sessionId) throws SessionExpiredException, CartItemNotAvail {
        String userId = getUserIdFromSessionId(sessionId);
        Cart cartToRead = cartDBInterface.getCart(userId);

        if(cartToRead == null){
            throw new CartItemNotAvail("Cart is not available for the user.");
        }
        readCartItems(cartItemDBInterface.getCartItems(cartToRead.getCartId()), cartToRead);
        return "Cart items read successfully";
    }

    private static String getUserIdFromSessionId(String sessionId) throws SessionExpiredException {
        SessionInfoInMemDB sessionInfoInMemDB = SessionInfoInMemDB.getInstance();

        return sessionInfoInMemDB.getSessionInfoUserId(sessionId);
    }

    private Product getProductInfo(String productId) throws ProductNotFound {
        Product product = productDBInterface.getProductById(productId);

        if (product == null) {
            throw new ProductNotFound("ProductId invalid. Product not found in database.Please enter correct product Id");
        }
        return product;
    }

    private int checkAvailQuantity(Product product, int quantity) throws QuanitityNotAvailException {
        if(product.getMaxQuantity() == null ){
            throw new QuanitityNotAvailException("Desired Product is Out of Stock");
        }
        else if(product.getMaxQuantity() < quantity){
            throw new QuanitityNotAvailException("Desired quantity not available. Max Available quantity is " + product.getMaxQuantity());
        }
        return quantity;
    }

    private double getTotalCost(Product product, int quantity){
        double total = quantity*(product.getMrpPrice()- product.getDiscount());

        return total;
    }

    private String updateProductInfo(Product prodToBeUpdated, int quantity){

        int updatedQuantity = (prodToBeUpdated.getMaxQuantity()==null ? 0 : prodToBeUpdated.getMaxQuantity())- quantity;

        return productDBInterface.updateProductDB(prodToBeUpdated.getProductId(), updatedQuantity);

    }

    public void readCartItems(List<CartItem> cartItems, Cart cart){
        if(cartItems.isEmpty()){
            System.out.println("No product items present in the cart");
        }
        else{
            System.out.println("Following product items are present in cart of User Id - " + cart.getUserId() +" : ");
            int count = 1;
            double totalCartValue = 0.0;

            for(CartItem cartItem : cartItems) {

                try {
                    Product prod = getProductInfo(cartItem.getProductId());
                    System.out.print("Sr.No : " + count + "     ");
                    System.out.print("Product ID : " + prod.getProductId() + "     ");
                    System.out.print("Product name : " + prod.getName() + "     ");
                    System.out.print("Product quantity : " + cartItem.getQuantity() + "     ");
                    System.out.print("Product price : " + prod.getMrpPrice() + "     ");
                    System.out.print("Product discount : " + prod.getDiscount() + "     ");
                    System.out.print("Cart item net cost : " + cartItem.getTotal() + "     ");
                    System.out.println();
                    count++;
                    totalCartValue += cartItem.getTotal();

                } catch (ProductNotFound e) {
                    System.out.println(e.getMessage());
                }

            }
            System.out.println("Total cart value :" + totalCartValue);
        }

    }

    public String generateCartId() {
        String CartId = "C" + UUID.randomUUID();
        return CartId;
    }


}
