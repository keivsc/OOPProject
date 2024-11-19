package server;

import server.types.User;

public class Data {
    private Users users;
    private Posts posts;
    private Comments comments;
    private User user = null;
    public Data(Users users, Posts posts, Comments comments) {
        this.users = users;
        this.posts = posts;
        this.comments = comments;
    }
    public Users Users() {
        return users;
    }
    public Posts Posts() {
        return posts;
    }
    public User getUser() {
        return user;
    }
    public Comments Comments() {return comments;}
    public void setUser(User user) {
        this.user = user;
    }
}
