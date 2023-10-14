package Ecommerce.Repository;


import Ecommerce.model.CartItem;

import java.util.List;

public interface CartItemDBInterface {
     List<CartItem> getCartItems(String CartId);

     CartItem getCartItem(String CartId, String ProductId);

     String addCartItem(String CartId, String productId, int quantity, double total);

     String updateCartItem(String CartId, String productId, int quantity, double total);

     String removeCartItem(String CartId, String productId);

     Double getTotalCartValue(String CartId);

     Integer getTotalCartItems(String CartId);

     void setDBFilePath(String path);

     void shutdown();
}
