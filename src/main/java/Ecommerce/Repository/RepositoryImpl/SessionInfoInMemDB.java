package Ecommerce.Repository.RepositoryImpl;

import Ecommerce.customExceptions.SessionExpiredException;
import Ecommerce.model.SessionInfo;
import Ecommerce.Repository.SessionInfoDBInterface;

import java.util.HashMap;
import java.util.Map;

public class SessionInfoInMemDB implements SessionInfoDBInterface {

    private long SESSION_TIMEOUT = 18000000; // SESSION_TIMEOUT in milliseconds, 30 min = 30*60*1000
    private static Map<String, SessionInfo> sessionData = new HashMap<>(); // SessionId -> SessionInfo
    private static Map<String, String> userSessionId = new HashMap<>(); // userId -> SessionId

    /* Note - Instead of using synchronization blocks, we can use ConcurrentHashMaps for sessionData and userSessionId.
    CHM divides hashMap in 16 segments and locks the segment where write would be performed. so this type of implementation
    can be fast.
     */



    private SessionInfoInMemDB(){
    }

    private static class Loader {
        final private static SessionInfoInMemDB INSTANCE = new SessionInfoInMemDB();
    }

    public static SessionInfoInMemDB getInstance(){
        return Loader.INSTANCE;
    }
    @Override
    public void saveSessionInfo(String sessionId, SessionInfo sessionInfo) {
        synchronized(SessionInfoInMemDB.class) {
            sessionData.put(sessionId, sessionInfo);
            userSessionId.put(sessionInfo.getUserId(), sessionId);
        }
    }

    @Override
    public SessionInfo getSessionInfo(String sessionId) {
        synchronized (SessionInfoInMemDB.class) {
            return sessionData.getOrDefault(sessionId, null);
        }
    }

    @Override
    public String getSessionInfoUserId(String sessionId) throws SessionExpiredException {
        SessionInfo sessionInfo = validateSession(sessionId);

        return sessionInfo.getUserId();
    }

    @Override
    public SessionInfo validateSession(String sessionId) throws SessionExpiredException {
        SessionInfo sessionInfo = getSessionInfo(sessionId);

        if (sessionInfo == null) {
            throw new SessionExpiredException("Session not found. Sign in again");
        }

        long currentTime = System.currentTimeMillis();
        long sessionExpirationTime = sessionInfo.getCreationTime() + SESSION_TIMEOUT;

        if (currentTime > sessionExpirationTime) {
            System.out.println(removeSession(sessionId));
            throw new SessionExpiredException("Session expired. Sign in again to continue");
        }
        // Can print validation successfull messege.

        return sessionInfo;
    }

    @Override
    public String removeSession(String sessionId) {
        SessionInfo sessionInfo = getSessionInfo(sessionId);

        if (sessionInfo == null) {
            return "Session already expired";
        }
        synchronized (SessionInfoInMemDB.class) {
            sessionData.remove(sessionId);
            userSessionId.remove(sessionInfo.getUserId());
        }
        return "User signed out successfully";
    }


    public String getSessionIdFromUserId(String userId){
        synchronized (SessionInfoInMemDB.class){
            return userSessionId.getOrDefault(userId, "");
        }
    }


}
