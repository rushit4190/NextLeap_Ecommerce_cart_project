package Ecommerce.service.serviceImpl;

import Ecommerce.model.User;
import Ecommerce.service.UserDBInterface;

import java.util.HashMap;
import java.util.Map;

public class UserInMemDB implements UserDBInterface {
    String DBPath = "";
    private static Map<String, User> usersDatabase = new HashMap<>(); // UserId -> User

    private static Map<String, String> emailUserId = new HashMap<>(); // Email -> UserId
    @Override
    public Integer addUser(User user) {
        usersDatabase.put(user.getUserId(), user);
        emailUserId.put(user.getEmail(), user.getUserId());
        return 0;
    }

    @Override
    public User getUserById(String userId) {
        return usersDatabase.getOrDefault(userId, null);

    }
    @Override
    public User getUserByEmail(String email) {

        return getUserById(emailUserId.getOrDefault(email, ""));
    }

    @Override
    public void setDBFilePath(String path) {

    }

    @Override
    public void shutdown() {

    }

    public boolean isValidCredentials(String email, String password) {
        return false;
    }
}
