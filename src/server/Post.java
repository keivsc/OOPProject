package server;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Post{
    private int postId;
    private String postTitle;
    private String postContent = null;
    private String postDate;
    private int postAuthor;
    private boolean loaded;
    private List<String> likedUsers = null;
    private List<String> dislikedUsers = null;
    private int likes = 0;
    private int dislikes = 0;

    public Post(int postID, String postTitle, int postAuthor, int postEpoch, int likes, int dislikes) {
        this.postId = postID;
        this.postTitle = postTitle;
        this.postAuthor = postAuthor;
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(postEpoch), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.postDate = dateTime.format(formatter);
        this.loaded = false;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    public void LoadPost(String content, List<String> likedUsers, List<String> dislikedUsers){
        this.loaded = true;
        this.postContent = content;
        this.likedUsers = likedUsers;
        this.dislikedUsers = dislikedUsers;
    }

    public int getPostId() {
        return postId;
    }
    public String getPostTitle() {
        return postTitle;
    }
    public String getPostContent() {
        return postContent;
    }
    public String getPostDate() {
        return postDate;
    }
    public int getPostAuthor() {
        return postAuthor;
    }
    public boolean isLoaded() {
        return loaded;
    }
    public List<String> getLikedUsers() {
        return likedUsers;
    }
    public List<String> getDislikedUsers() {
        return dislikedUsers;
    }
    public int getLikes() {
        return likes;
    }
    public int getDislikes() {
        return dislikes;
    }

}
