package comp5216.sydney.edu.au.unichat;

public class GroupMessages {

    private String fromID, message,fromName;

    public GroupMessages(){

    }

    public GroupMessages(String fromID, String message, String fromName) {
        this.fromID = fromID;
        this.message = message;
        this.fromName = fromName;
    }

    public String getFromID() {
        return fromID;
    }

    public String getMessage() {
        return message;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromID(String fromID) {
        this.fromID = fromID;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
}
