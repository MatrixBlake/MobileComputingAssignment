package comp5216.sydney.edu.au.unichat;

public class Contacts {
    private String name, status, image;

    public Contacts(){

    }

    public Contacts(String name, String status, String image) {
        this.name = name;
        this.status = status;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getImage() {
        return image;
    }
}
