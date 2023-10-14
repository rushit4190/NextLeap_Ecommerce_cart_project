package Ecommerce.Repository;

import Ecommerce.model.Cart;


public interface CartDBInterface {

     Cart getCart(String userId);

     String addCart(String CartId, String UserId);

     void setDBFilePath(String path);

     void shutdown();

}
