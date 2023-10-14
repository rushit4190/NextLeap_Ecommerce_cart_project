package Ecommerce.Controller.APIController;

import Ecommerce.Service.ServiceImpl.CLIService.UserCLIService;
import Ecommerce.customExceptions.UserAlreadyExistsException;
import Ecommerce.customExceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/EcommerceCart/")
@RestController
public class UserAPIController {

    @Autowired
    private UserCLIService userCLIService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody Map<String, Object> requestBody) {
        String email = (String) requestBody.getOrDefault("email", "");
        String firstName = (String) requestBody.getOrDefault("firstName", "");
        String lastName = (String) requestBody.getOrDefault("lastName", "");
        String password = (String) requestBody.getOrDefault("password", "");

        if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Invalid request body", HttpStatus.BAD_REQUEST);
        }

        String status;

        try{
            status = userCLIService.signUp(email, firstName, lastName, password);

        } catch (UserAlreadyExistsException e) {
            status = e.getMessage();
            return new ResponseEntity<>(status, HttpStatus.CONFLICT);
        }

        if(status.equals("User sign up successful")){
            return new ResponseEntity<>(status, HttpStatus.CREATED);
        }

        return new ResponseEntity<>(status, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signIn(@RequestBody Map<String, Object> requestBody){
        String email = (String) requestBody.getOrDefault("email", "");
        String password = (String) requestBody.getOrDefault("password", "");

        Map<String, Object> response = new HashMap<>();
        String sessionId = "";
        response.put("sessionId", sessionId);


        if (email.isEmpty() || password.isEmpty()) {
            response.put("status", "Invalid request body" );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String status;

        try{
            sessionId = userCLIService.signIn(email, password);

        } catch (UserNotFoundException e) {
            status = e.getMessage();

            if(status.equals("User not found. Please Sign Up before")){

                response.put("status", status);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

            } else if (status.equals("Sign In Denied! Incorrect Username or Password")) {

                response.put("status", status);
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        }

        status = "User sign in successful. Session Started with id : " + sessionId;

        response.put("status", status);
        response.put("sessionId", sessionId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/signout")
    public ResponseEntity<String> signOut(@RequestBody Map<String, Object> requestBody){
        String sessionId = (String) requestBody.getOrDefault("sessionId","");

        if (sessionId.isEmpty()) {
            return new ResponseEntity<>("Invalid request body" , HttpStatus.BAD_REQUEST);
        }
        String status = userCLIService.signOut(sessionId);
        return new ResponseEntity<>(status , HttpStatus.OK);
    }

    @GetMapping("/hello")
    public String printHello(){
        return "Hello check";
    }

}
