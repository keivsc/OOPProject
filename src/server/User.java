package server;

import java.util.Arrays;
import java.util.List;

public class User{
    private int id;
    private String username;
    private int clearance;
    private String email;
    private List<Integer> postIDs;
    public boolean isAdmin;


    public User(String email, String username, int clearance, int id, String posts){
        this.id = id;
        this.username = username;
        this.clearance = clearance;
        this.email = email;
        this.isAdmin = (clearance != 0);
        String trimmedInput = posts.substring(1, posts.length() - 1);

        // Split the string by commas and trim whitespace
        this.postIDs = Arrays.stream(trimmedInput.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();

    }

    public int getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
    public List<Integer> getPosts() {
        return this.postIDs;
    }

}
