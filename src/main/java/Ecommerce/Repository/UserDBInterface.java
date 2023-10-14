package Ecommerce.Repository;

import Ecommerce.model.User;

public interface UserDBInterface {


    public Integer addUser(User user);

    public User getUserById(String userId);

    public User getUserByEmail(String email);

    public void setDBFilePath(String path);

    public void shutdown();


//    public User getUserDetails(String email, String password);

    // Can also write first/last name getters


}
