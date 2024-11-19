package server.types;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Comment{
    private int id;
    private String comment;
    private int authorID;
    private String postDate;

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

    public Comment(int id, String comment, int authorID, int postEpoch) {
        this.id = id;
        this.comment = comment;
        this.authorID = authorID;
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(postEpoch), ZoneId.systemDefault());
        this.postDate = formatRelativeTime(dateTime);
    }

    public int getId() {
        return id;
    }
    public String getComment() {
        return comment;
    }
    public int getAuthorID() {
        return authorID;
    }
    public String getPostDate() {return postDate;}

}