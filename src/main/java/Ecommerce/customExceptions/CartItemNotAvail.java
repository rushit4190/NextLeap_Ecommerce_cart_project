package Ecommerce.customExceptions;

public class CartItemNotAvail extends Exception{
    public CartItemNotAvail(String message) {
        super(message);
    }
}
