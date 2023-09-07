package Ecommerce.model;

import java.io.Serializable;


public class CartItem implements Serializable {

    private String productId;
    private int quantity;

    private double total;

    private Product prodToAdd ;

    // Constructors, getters, setters, and other methods

    public CartItem(){

    }
    public CartItem(String productId, int quantity, double total){
        this.productId = productId;
        this.quantity = quantity;
        this.total = total;
    }

    public String getProductId() {
        return productId;
    }


    public int getQuantity() {
        return quantity;
    }



    public double getTotal() { return total;}

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity)  {
        this.quantity = quantity;
    }

    public void setTotal(double total) { this.total = total;}

}

