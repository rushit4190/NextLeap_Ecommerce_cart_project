package Ecommerce.Controller;

import Ecommerce.customExceptions.CartItemNotAvail;
import Ecommerce.customExceptions.ProductNotFound;
import Ecommerce.customExceptions.QuanitityNotAvailException;
import Ecommerce.customExceptions.SessionExpiredException;

public interface CartController {

     String addCartItem(String sessionId, String productId, int quantity) throws SessionExpiredException, ProductNotFound, QuanitityNotAvailException;

     String updateCartItem(String sessionId, String productId, int quantity) throws SessionExpiredException, CartItemNotAvail, ProductNotFound, QuanitityNotAvailException;

     String removeCartItem(String sessionId, String productId) throws SessionExpiredException, CartItemNotAvail, ProductNotFound;

     String getCartItems(String sessionId) throws SessionExpiredException, CartItemNotAvail;

}
