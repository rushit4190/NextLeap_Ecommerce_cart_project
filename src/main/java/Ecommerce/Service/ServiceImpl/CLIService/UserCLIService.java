package Ecommerce.Controller.ControllerImpl.CLIController;

import Ecommerce.Controller.UserController;
import Ecommerce.customExceptions.SessionExpiredException;
import Ecommerce.customExceptions.UserAlreadyExistsException;
import Ecommerce.customExceptions.UserNotFoundException;
import Ecommerce.model.SessionInfo;
import Ecommerce.model.User;
import Ecommerce.Repository.UserDBInterface;
import Ecommerce.Repository.RepositoryImpl.SessionInfoInMemDB;

import java.util.UUID;

public class UserCLIController implements UserController {

    private UserDBInterface userDBInterface;


    public UserCLIController(UserDBInterface userDBIntf){
        this.userDBInterface = userDBIntf;
    }

    public String signUp(String email, String firstName, String lastName, String password) throws UserAlreadyExistsException {
        User RegUser = userDBInterface.getUserByEmail(email);

        String status = "User sign up unsuccessful";

        if(RegUser != null){
            throw new UserAlreadyExistsException("User with this email already exists.");
        }
        else{
            String userID = generateUserId();

            User user = new User(userID, email, firstName, lastName, password);

            int response = userDBInterface.addUser(user); // also need to store in disk to keep system persistent

            if(response == 2) {
                status = "User sign up successful";
            }
        }
        return status;
    }



    @Override
    public String signIn(String email, String password) throws UserNotFoundException {
        String status = "User sign in successful. Session Started";

        User RegUser = userDBInterface.getUserByEmail(email);

        if(RegUser == null){
            throw new UserNotFoundException("User not found. Please Sign Up before");
        }

        boolean isVal = RegUser.getPassword().equals(password);
        if(!isVal){
            throw new UserNotFoundException("Sign In Denied! Incorrect Username or Password");
        }


        SessionInfoInMemDB sessionInfoService = SessionInfoInMemDB.getInstance();
        String sessionId = "";
        String checkSessionId = sessionInfoService.getSessionIdFromUserId(RegUser.getUserId());
        boolean newSignIn = true;

        if(!checkSessionId.isEmpty()){
            try{
                sessionInfoService.validateSession(checkSessionId);
                newSignIn = false;
                status = "User Id - " + RegUser.getUserId() + " already signed in with session Id : " + checkSessionId + ". Continue shopping!";
                sessionId = checkSessionId;
            }catch (SessionExpiredException e){

            }
        }
        if(newSignIn) {
            // Store sessionId and user details in a session management system
            sessionId = generateSessionId();
            SessionInfo sessionInfo = new SessionInfo(RegUser.getUserId(), System.currentTimeMillis());


            sessionInfoService.saveSessionInfo(sessionId, sessionInfo);
            status = "User sign in successful. Session Started with id : " + sessionId;
        }
        System.out.println(status);

        return sessionId;
    }

    @Override
    public String signOut(String sessionId) {
        SessionInfoInMemDB sessionInfoService = SessionInfoInMemDB.getInstance();
        String status = sessionInfoService.removeSession(sessionId);
        return status;
    }


    public String generateUserId() {
        String userId = "U" + UUID.randomUUID().toString();
        return userId;
    }


    public String generateSessionId() {
        String sessionID = "S" + UUID.randomUUID().toString();
        return sessionID;
    }


}


