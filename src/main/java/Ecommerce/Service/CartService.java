package Ecommerce.Service;

import Ecommerce.customExceptions.CartItemNotAvail;
import Ecommerce.customExceptions.ProductNotFound;
import Ecommerce.customExceptions.QuanitityNotAvailException;
import Ecommerce.customExceptions.SessionExpiredException;
import Ecommerce.model.CartItem;

import java.util.List;

public interface CartService {

     String addCartItem(String sessionId, String productId, int quantity) throws SessionExpiredException, ProductNotFound, QuanitityNotAvailException;

     String updateCartItem(String sessionId, String productId, int quantity) throws SessionExpiredException, CartItemNotAvail, ProductNotFound, QuanitityNotAvailException;

     String removeCartItem(String sessionId, String productId) throws SessionExpiredException, CartItemNotAvail, ProductNotFound;

     List<CartItem> getCartItems(String sessionId) throws SessionExpiredException, CartItemNotAvail;

}
