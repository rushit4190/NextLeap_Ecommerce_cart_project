package Ecommerce.Service;

import Ecommerce.customExceptions.UserAlreadyExistsException;
import Ecommerce.customExceptions.UserNotFoundException;

public interface UserService {

    public String signUp(String email, String firstName, String lastName, String password) throws UserAlreadyExistsException;

    public String signIn(String email, String password) throws UserNotFoundException;

    public String signOut(String sessionId);
}
