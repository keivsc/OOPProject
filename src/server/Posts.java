package server;

import com.keivsc.SQLiteJava.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


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
            throw new RuntimeException(e);
        }
    }

    public List<Post> browseTopPosts(int page) {
        refreshDB();
        try {
            List<Post> posts = new ArrayList<>();

            // Check cache first
            if (cachedPosts.containsKey(page)) {
                cachedPosts.get(page).values().forEach(posts::add);
                return posts;
            }

            // Query the database
            ResultSet rs = this.headersTable.runQuery(
                    "SELECT *, (CAST(likes AS FLOAT) / dislikes) AS rating " +
                            "FROM PostsHeaders " +
                            "ORDER BY rating DESC " +
                            "LIMIT 10 " +
                            "OFFSET " + (page - 1) * 10
            );

            // Process the result set
            cachedPosts.put(page, new HashMap<>());
            while (rs.next()) {
                int postId = rs.getInt("postID");
                System.out.println(postId);
                String postTitle = rs.getString("postTitle");
                int postAuthor = rs.getInt("authorID");
                int postEpoch = rs.getInt("postEpoch");
                int likes = rs.getInt("likes");
                int dislikes = rs.getInt("dislikes");
                Post currentPost = new Post(postId, postTitle, postAuthor, postEpoch, likes, dislikes);
                posts.add(currentPost);
                cachedPosts.get(page).put(postId, currentPost);
            }

            // Maintain cache size
            if (cachedPosts.size() > 3) {
                int oldestPage = Collections.min(cachedPosts.keySet());
                cachedPosts.remove(oldestPage);
            }

            return posts;
        } catch (Exception e) {
            e.printStackTrace(); // Consider logging this instead of printing
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
                currentPost = new Post((Integer) postResult.get("postID"), (String)postResult.get("postTitle"), (Integer)postResult.get("authorID"), (Integer)postResult.get("postEpoch"), 0, 0);
            }
            List<Value> postList = this.contentTable.getItems("postID=" + postID);
            Value postData = postList.getFirst();
            currentPost.LoadPost((String) postData.get("content"), parseStringToList((String)postData.get("likedUsers")), parseStringToList((String)postData.get("dislikedUsers")));
            return currentPost;
        }catch (Errors.DatabaseException e) {
            return null;
        }

    }
    
    public List<Post> searchPosts(String query, int page){
        refreshDB();
        List<Post> posts = new ArrayList<>();
        try{
            ResultSet rs = this.headersTable.runQuery("SELECT * FROM PostsHeaders WHERE column_name LIKE '"+query+"' LIMIT 10 OFFSET"+(page-1)*10+";");
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()){
                int postId = rs.getInt("postID");
                String postTitle = rs.getString("postTitle");
                int postAuthor = rs.getInt("authorID");
                int postEpoch = rs.getInt("postEpoch");
                int likes = rs.getInt("likes");
                int dislikes = rs.getInt("dislikes");
                Post currentPost = new Post(postId, postTitle, postAuthor, postEpoch, likes, dislikes);
                posts.add(currentPost);
            }
            return posts;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int newPost(int authorID, String title, String Content){
        int epochTime = (int)(Instant.now().getEpochSecond());
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
