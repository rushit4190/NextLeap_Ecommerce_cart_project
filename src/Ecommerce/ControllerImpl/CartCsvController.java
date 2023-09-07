package Ecommerce.ControllerImpl;

import Ecommerce.Controller.CartController;
import Ecommerce.customExceptions.CartItemNotAvail;
import Ecommerce.customExceptions.ProductNotFound;
import Ecommerce.customExceptions.QuanitityNotAvailException;
import Ecommerce.customExceptions.SessionExpiredException;
import Ecommerce.model.Cart;
import Ecommerce.model.CartItem;
import Ecommerce.model.Product;
import Ecommerce.service.CartDBInterface;
import Ecommerce.service.ProductDBInterface;
import Ecommerce.serviceImpl.SessionInfoInMemDB;

import java.util.Collection;

public class CartCsvController implements CartController {

    private CartDBInterface cartDBInterface;

    private ProductDBInterface productDBInterface;

    public CartCsvController(CartDBInterface cartDBInterf, ProductDBInterface prodDBinterf){
        this.cartDBInterface = cartDBInterf;
        this.productDBInterface = prodDBinterf;
    }
    @Override
    public String addCartItem(String sessionid, String productId, int quantity) throws SessionExpiredException, ProductNotFound, QuanitityNotAvailException {
        String userId = getUserIdFromSessionId(sessionid);
        Product prodToAdd = getProductInfo(productId);
        checkAvailQuantity(prodToAdd, quantity);
        double total = getTotalCost(prodToAdd, quantity);

        CartItem cartItemToAdd = new CartItem(productId, quantity, total);

        Cart cartToAdd = cartDBInterface.getCart(userId);

        if(cartToAdd == null){
            cartDBInterface.addNewCartWithItem(userId,1,total,productId,quantity, total);
        }
        else{
            cartDBInterface.addCartItem(userId, cartToAdd.getCartItems().size() + 1, cartToAdd.getTotalCartValue() + total, productId, quantity, total);
        }
        updateProductInfo(prodToAdd, quantity);
        return "Required product added to the cart";
    }

    @Override
    public String updateCartItem(String sessionId, String productId, int quantity) throws SessionExpiredException, CartItemNotAvail, ProductNotFound, QuanitityNotAvailException {
        if(quantity == 0){
            return removeCartItem(sessionId, productId);
        }

        String userId = getUserIdFromSessionId(sessionId);
        Cart cartToAdd = cartDBInterface.getCart(userId);

        if(cartToAdd == null){
            throw new CartItemNotAvail("Cart is not available for the user. Add cart item first");
        }

        CartItem cartItemToUpdate = cartToAdd.getCartItems().getOrDefault(productId, null);

        if(cartItemToUpdate == null){
            throw new CartItemNotAvail("Cart doesnt contain the desired product. Add cart item");
        }
        int oldQuantity = cartItemToUpdate.getQuantity();
        if(oldQuantity == quantity) { return "Updated Successfully";} // nothing to update


        Product prodToAdd = getProductInfo(productId);
        Integer maxAvailableQuantity = prodToAdd.getMaxQuantity();

        int availableQuantity = (maxAvailableQuantity == null ? 0 : maxAvailableQuantity);
        int totalAvailable = oldQuantity + availableQuantity;

        if(totalAvailable >= quantity){
            double total = getTotalCost(prodToAdd, quantity);
            double totalCartValue = cartToAdd.getTotalCartValue() + getTotalCost(prodToAdd,quantity-oldQuantity);

            // Update Cart to CSV
            cartDBInterface.updateCartItem(userId, totalCartValue, productId, quantity, getTotalCost(prodToAdd, quantity));

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
        Cart cartToAdd = cartDBInterface.getCart(userId);

        if(cartToAdd == null){
            throw new CartItemNotAvail("Cart is not available for the user.");
        }

        int noOfItems = cartToAdd.getCartItems().size();

        if(noOfItems == 1){
            cartDBInterface.deleteCart(userId);
        }
        else{
            double total = cartToAdd.getCartItems().get(productId).getTotal();
            double totalCartValue = cartToAdd.getTotalCartValue() - total;
            cartDBInterface.removeCartItem(userId, noOfItems - 1, totalCartValue, productId );
        }
        int cartQuantity = cartToAdd.getCartItems().get(productId).getQuantity();
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
        readCartItems(cartToRead.getCartItems().values(), cartToRead);
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

    public void readCartItems(Collection<CartItem> cartItems, Cart cart){
        if(cartItems.isEmpty()){
            System.out.println("No product items present in the cart");
        }
        else{
            System.out.println("Following product items are present in cart : ");

            for(CartItem cartItem : cartItems) {

                try {
                    Product prod = getProductInfo(cartItem.getProductId());
                    System.out.print("Product ID : " + prod.getProductId() + "     ");
                    System.out.print("Product name : " + prod.getName() + "     ");
                    System.out.print("Product quantity : " + cartItem.getQuantity() + "     ");
                    System.out.print("Product price : " + prod.getMrpPrice() + "     ");
                    System.out.print("Product discount : " + prod.getDiscount() + "     ");
                    System.out.print("Cart item net cost : " + cartItem.getTotal() + "     ");
                    System.out.println();

                } catch (ProductNotFound e) {
                    System.out.println(e.getMessage());
                }

            }
            System.out.println("Total cart value :" + cart.getTotalCartValue());
        }

    }


}
