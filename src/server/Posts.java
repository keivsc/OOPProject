package server;

import com.keivsc.SQLiteJava.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Post{
    private int postId;
    private String postTitle;
    private String postContent = null;
    private String postDate;
    private String postAuthor;
    private boolean loaded;
    private List<String> likedUsers = null;
    private List<String> dislikedUsers = null;

    public Post(int postID, String postTitle, String postAuthor, int postEpoch) {
        this.postId = postID;
        this.postTitle = postTitle;
        this.postAuthor = postAuthor;
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(postEpoch), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.postDate = dateTime.format(formatter);
        this.loaded = false;
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
    public String getPostAuthor() {
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

}
public class Posts {
    private Database db;
    private Table headersTable;
    private Table contentTable;
    private Table commentsTable;
    private Map<Integer, Map<Integer, Post>> cachedPosts = new HashMap<>();

    private static List<String> parseStringToList(String input) {
        input = input.substring(1, input.length() - 1);
        String[] array = input.split(",");
        return Arrays.asList(array);
    }

    public Posts(){
        try{
            this.db = new Database("Content.db");
            this.headersTable = this.db.createTable("PostsHeaders", new String[]{"postID INTEGER PRIMARY KEY AUTOINCREMENT", "authorID INTEGER NOT NULL", "postTitle TEXT NOT NULL", "postEpoch INTEGER NOT NULL", "likes INTEGER NOT NULL", "dislikes INTEGER NOT NULL"});
            this.contentTable = this.db.createTable("PostData", new String[]{"postID INTEGER PRIMARY KEY AUTOINCREMENT", "content TEXT NOT NULL", "likedUsers TEXT NOT NULL", "dislikedUsers TEXT NOT NULL"});
            this.commentsTable = this.db.createTable("PostComments", new String[]{"commentID INTEGER PRIMARY KEY", "content TEXT NOT NULL", "authorID TEXT NOT NULL", "postID INTEGER NOT NULL", "commentEpoch INTEGER NOT NULL"});
        } catch (Errors.DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshDB(){
        try {
            this.db.close();
            this.db = new Database("Content.db");
            this.headersTable = this.db.connectTable("PostsHeaders");
            this.contentTable = this.db.connectTable("PostData");
            this.commentsTable = this.db.connectTable("PostComments");
        }catch(Exception e){

        }
    }

    private List<Post> browseTopPosts(int page){
        refreshDB();
        try {
            List<Post> posts = new ArrayList<>();
            if (cachedPosts.containsKey(page)){
                cachedPosts.get(page).forEach((postID, post)->{
                    posts.add(post);
                });
                return posts;
            }

            ResultSet rs = this.headersTable.runQuery("SELECT *, (CAST(likes AS FLOAT) / dislikes) AS rating\n" +
                    "FROM PostsHeaders\n" +
                    "WHERE dislikes > 0\n" +
                    "ORDER BY rating DESC\n" +
                    "LIMIT 10\n" +
                    "OFFSET "+(page-1)*10);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            cachedPosts.put(page, new HashMap<>());
            while (rs.next()){
                for(int i = 1; i <= columnCount; i++){
                    int postId = rs.getInt("postID");
                    String postTitle = rs.getString("postTitle");
                    String postAuthor = rs.getString("authorID");
                    int postEpoch = rs.getInt("epochDate");
                    Post currentPost = new Post(postId, postTitle, postAuthor, postEpoch);
                    posts.add(currentPost);
                    cachedPosts.get(page).put(postId, currentPost);
                }
            }

            if (cachedPosts.size() > 3) {
                int oldestPage = Collections.min(cachedPosts.keySet());
                cachedPosts.remove(oldestPage);
            }
            return posts;
        }catch(Exception e){
            return new ArrayList<>();
        }
    }

    public Post getPost(int postID){
        refreshDB();
        Post currentPost = null;
        for (int i=1; i<=cachedPosts.size(); i++){
            currentPost = cachedPosts.get(i).get(postID);
        }

        try {
            if (currentPost == null) {
                List<Value> postSearch = this.headersTable.getItems("postID=" + postID);
                if (postSearch.isEmpty()) {
                    return currentPost;
                }
                Value postResult = postSearch.getFirst();
                currentPost = new Post((Integer) postResult.get("postID"), (String)postResult.get("postTitle"), (String)postResult.get("authorID"), (Integer)postResult.get("postEpoch"));
            }
            List<Value> postList = this.contentTable.getItems("postID=" + postID);
            Value postData = postList.getFirst();
            currentPost.LoadPost((String) postData.get("content"), parseStringToList((String)postData.get("likedUsers")), parseStringToList((String)postData.get("dislikedUsers")));
            return currentPost;
        }catch (Errors.DatabaseException e) {
            return null;
        }

    }

    public int newPost(int authorID, String title, String Content){
        int epochTime = (int)(Instant.now().getEpochSecond()/1000);
        Value postHeaders = new Value();
        postHeaders.addItem("postID", "AutoIncrement");
        postHeaders.addItem("authorID", authorID);
        postHeaders.addItem("postTitle", title);
        postHeaders.addItem("postEpoch", epochTime);
        postHeaders.addItem("likes", 0);
        postHeaders.addItem("dislikes", 0);
        Value postContent = new Value();
        postContent.addItem("postID", "AutoIncrement");
        postContent.addItem("content", Content);
        postContent.addItem("likedUsers", "[0]");
        postContent.addItem("dislikedUsers", "[0]");
        try {
            this.headersTable.addItem(postHeaders, false);
            this.contentTable.addItem(postContent, false);
            this.refreshDB();
            return 0;
        }catch (Errors.DatabaseException e) {
            System.out.println(e.getMessage());
            return 1;
        }
    }


    //RESTRUCTURE TO NOT ALLOW REPEATEDLIKES
    public int likePost(int postID, int authorID){
        Value newValue = new Value();
        newValue.addItem("likes", "likes + 1");
        Value likedUsers = new Value();
        likedUsers.addItem("likedUsers", " SUBSTR(likedUsers, 1, LENGTH(likedUsers) - 1) || ', "+authorID+"]'");
        try {
            this.headersTable.editItem("postID=" + postID, newValue, true);
            this.contentTable.editItem("postID=" + postID, likedUsers, true);
            this.refreshDB();
        }catch(Errors.DatabaseException e) {
            System.out.println(e.getMessage());
            return 1;
        }
        return 0;
    }

    public int dislikePost(int postID, int authorID){
        Value newValue = new Value();
        newValue.addItem("dislikes", "likes + 1");
        Value dislikedUsers = new Value();
        dislikedUsers.addItem("dislikedUsers", " SUBSTR(dislikedUsers, 1, LENGTH(dislikedUsers) - 1) || ', "+authorID+"]'");
        try {
            this.headersTable.editItem("postID=" + postID, newValue, true);
            this.contentTable.editItem("postID=" + postID, dislikedUsers, true);
            this.refreshDB();
        }catch(Errors.DatabaseException e) {
            return 1;
        }
        return 0;
    }

    public static void main(String[] args){
        Posts posts = new Posts();
        posts.newPost(1, "How i Defrauded the US Government", "HELLO WORLD");
        posts.likePost(1, 1);
    }

}
