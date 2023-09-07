package Ecommerce.model;

public class SessionInfo {
    private String userId;
    private long creationTime;

    public SessionInfo(String userId, long creationTime) {
        this.userId = userId;
        this.creationTime = creationTime;
    }

    public String getUserId() {
        return userId;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
