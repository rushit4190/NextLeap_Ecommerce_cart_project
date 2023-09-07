package Ecommerce.service;

import Ecommerce.model.Cart;


public interface CartDBInterface {

    public Cart getCart(String userId);

    public String addCartItem(String userId, int totalCartItems, double totalCartValue, String productId, int quantity, double total);

    public String addNewCartWithItem(String userId, int totalCartItems, double totalCartValue, String productId, int quantity, double total);

    public String updateCartItem(String userId, double totalCartValue, String productId, int quantity, double total);

    public String removeCartItem(String userId, int totalCartItems, double totalCartValue, String productId);

    public String deleteCart(String userId);

    public void setDBFilePath(String path);

    public void shutdown();

}
