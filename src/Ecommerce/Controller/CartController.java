package Ecommerce.Controller;

import Ecommerce.customExceptions.CartItemNotAvail;
import Ecommerce.customExceptions.ProductNotFound;
import Ecommerce.customExceptions.QuanitityNotAvailException;
import Ecommerce.customExceptions.SessionExpiredException;

public interface CartController {

    public String addCartItem(String sessionID, String productId, int quantity) throws SessionExpiredException, ProductNotFound, QuanitityNotAvailException;

    public String updateCartItem(String sessionId, String productId, int quantity) throws SessionExpiredException, CartItemNotAvail, ProductNotFound, QuanitityNotAvailException;

    public String removeCartItem(String sessionId, String productId) throws SessionExpiredException, CartItemNotAvail, ProductNotFound;

    public String getCartItems(String sessionId) throws SessionExpiredException, CartItemNotAvail;

}
