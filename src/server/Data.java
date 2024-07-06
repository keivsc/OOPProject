package server;

public class Data {
    private Users users;
    private Posts posts;
    private User user = null;
    public Data(Users users, Posts posts) {
        this.users = users;
        this.posts = posts;
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
    public void setUser(User user) {
        this.user = user;
    }
}
