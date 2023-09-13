package Ecommerce.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Cart implements Serializable {

    private Map<String,CartItem> cartItems = new HashMap<>(); // ProductId -> CartItem

    private String cartId;

    private String userId; // to identify cart

    private double totalCartValue = 0.0;


    public Cart(String cartId, String userId){
        this.cartId = cartId;
        this.userId = userId;
    }


    public Map<String, CartItem> getCartItems() {
        return cartItems;
    }

    public String getUserId() { return userId ;}

    public String getCartId() { return cartId; }

    public double getTotalCartValue() { return totalCartValue;}

    public void setUserId(String id){
        userId = id;
    }
    public void setTotalCartValue(double value){
        totalCartValue = value;
    }



}
