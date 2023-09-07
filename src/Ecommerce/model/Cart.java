package Ecommerce.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Cart implements Serializable {

    private Map<String,CartItem> cartItems; // ProductId -> CartItem

    private String userId; // to identify cart

    private double totalCartValue;

    public Cart(){
        if(cartItems == null){
            this.cartItems = new HashMap<>();
        }
    }





    public Map<String, CartItem> getCartItems() {
        return cartItems;
    }

    public String getUserIdCart() { return userId ;}

    public double getTotalCartValue() { return totalCartValue;}

    public void setUserId(String id){
        userId = id;
    }
    public void setTotalCartValue(double value){
        totalCartValue = value;
    }


    public double calculateCartValue() {
        double totalCartValue = 0;

        for(CartItem cartItem : cartItems.values()){
            totalCartValue += cartItem.getTotal();
        }
        return this.totalCartValue = totalCartValue;
    }

}
