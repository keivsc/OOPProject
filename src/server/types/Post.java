package server.types;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Post{
    private int postId;
    private String postTitle;
    private String postContent = null;
    private String postDate;
    private int postAuthor;
    private boolean loaded;
    private List<Integer> likedUsers = null;
    private List<Integer> dislikedUsers = null;
    private int likes = 0;
    private int dislikes = 0;

    private String formatRelativeTime(LocalDateTime postDateTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(postDateTime, now);

        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        long years = ChronoUnit.YEARS.between(postDateTime, now);

        if (years > 0) {
            return years + " year" + (years > 1 ? "s" : "") + " ago";
        } else if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return seconds + " second" + (seconds > 1 ? "s" : "") + " ago";
        }
    }


    public Post(int postID, String postTitle, int postAuthor, int postEpoch, int likes, int dislikes) {
        this.postId = postID;
        this.postTitle = postTitle;
        this.postAuthor = postAuthor;
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(postEpoch), ZoneId.systemDefault());
        this.postDate = formatRelativeTime(dateTime);
        this.loaded = false;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    public void LoadPost(String content, List<Integer> likedUsers, List<Integer> dislikedUsers){
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
    public List<Integer> getLikedUsers() {
        return likedUsers;
    }
    public List<Integer> getDislikedUsers() {
        return dislikedUsers;
    }
    public int getLikes() {
        return likes;
    }
    public int getDislikes() {
        return dislikes;
    }

}
