package Ecommerce.customExceptions;

public class ProductNotFound extends Exception{
    public ProductNotFound(String message) {
        super(message);
    }
}
