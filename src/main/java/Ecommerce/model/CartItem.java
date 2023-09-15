package Ecommerce.model;

import java.io.Serializable;


public class CartItem implements Serializable {


    private String cartId;
    private String productId;
    private int quantity;

    private double total;



    // Constructors, getters, setters, and other methods

    public CartItem(){

    }
    public CartItem(String cartId, String productId, int quantity, double total){
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.total = total;
    }

    public String getCartId(){ return cartId;}
    public String getProductId() {
        return productId;
    }


    public int getQuantity() {
        return quantity;
    }



    public double getTotal() { return total;}

    public void setCartId(String cartId) { this.cartId = cartId; }
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity)  {
        this.quantity = quantity;
    }

    public void setTotal(double total) { this.total = total;}

}

