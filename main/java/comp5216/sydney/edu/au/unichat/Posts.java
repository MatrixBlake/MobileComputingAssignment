package comp5216.sydney.edu.au.unichat;

public class Posts {


    public String postid, uid, time, date, name, description, image;
    public long lastTime;
    public Posts()
    {

    }

    public Posts(String postid, String uid, String time, String date,  String description,  String name, String image, long lastTime) {
        this.postid = postid;
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.description = description;
        this.name = name;
        this.image = image;
        this.lastTime = lastTime;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPostid() {
        return postid;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }
}



