package comp5216.sydney.edu.au.unichat;

public class Messages {

    private String from, message, type, image;

    public  Messages(){

    }

    public Messages(String from, String message, String type, String image) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.image = image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
