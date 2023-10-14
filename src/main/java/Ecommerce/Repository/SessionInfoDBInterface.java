package Ecommerce.Repository;

import Ecommerce.customExceptions.SessionExpiredException;
import Ecommerce.model.SessionInfo;

public interface SessionInfoDBInterface {

    public void saveSessionInfo(String sessionId, SessionInfo sessionInfo);

    public SessionInfo getSessionInfo(String sessionId);

    public String getSessionInfoUserId(String sessionId) throws SessionExpiredException;

    public SessionInfo validateSession(String sessionId) throws SessionExpiredException;

    public String removeSession(String sessionId);
}
