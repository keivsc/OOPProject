package server;

import com.keivsc.SQLiteJava.*;
import server.types.Post;
import server.types.User;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Posts {
    private Database db;
    private Users users;
    private Table headersTable;
    private Table contentTable;
    private Map<Integer, Post> cachedPosts = new HashMap<>();

    public Posts() {
        try {
            this.db = new Database("Content.db");
            this.users = new Users();
            this.headersTable = this.db.createTable("PostsHeaders", new String[]{
                    "postID INTEGER PRIMARY KEY AUTOINCREMENT",
                    "authorID INTEGER NOT NULL",
                    "postTitle TEXT NOT NULL",
                    "postEpoch INTEGER NOT NULL",
                    "likes INTEGER NOT NULL",
                    "dislikes INTEGER NOT NULL"
            });
            this.contentTable = this.db.createTable("PostData", new String[]{
                    "postID INTEGER PRIMARY KEY AUTOINCREMENT",
                    "content TEXT NOT NULL",
                    "likedUsers TEXT NOT NULL",
                    "dislikedUsers TEXT NOT NULL"
            });
        } catch (Errors.DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshDB() {
        try {
            this.db.close();
            this.db = new Database("Content.db");
            this.headersTable = this.db.connectTable("PostsHeaders");
            this.contentTable = this.db.connectTable("PostData");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getLastID(int authorID) {
        try {
            ResultSet rs = this.headersTable.runQuery("SELECT MAX(postID) AS lastId FROM PostsHeaders WHERE authorID = " + authorID);
            if (rs.next()) {
                return rs.getInt("lastId");
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<Post> browseTopPosts(int page) {
        refreshDB();
        List<Post> posts = new ArrayList<>();
        try {
            ResultSet rs = this.headersTable.runQuery(
                    "SELECT *, (CAST(likes AS FLOAT) / dislikes) AS rating " +
                            "FROM PostsHeaders " +
                            "ORDER BY rating DESC " +
                            "LIMIT 11 " +
                            "OFFSET " + (page - 1) * 10
            );

            while (rs.next()) {
                Post currentPost = createPostFromResultSet(rs);
                posts.add(currentPost);
                cachedPosts.put(currentPost.getPostId(), currentPost);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Consider logging this instead of printing
        }
        return posts;
    }

    public Post getPost(int postID) {
        refreshDB();
        Post currentPost = cachedPosts.get(postID);

        try {
            if (currentPost == null) {
                List<Value> postSearch = this.headersTable.getItems("postID=" + postID);
                if (postSearch.isEmpty()) {
                    return null;
                }
                Value postResult = postSearch.get(0);
                currentPost = createPostFromValue(postResult);
            }
            List<Value> postList = this.contentTable.getItems("postID=" + postID);
            Value postData = postList.get(0);
            currentPost.LoadPost((String) postData.get("content"),
                    parseStringToList((String) postData.get("likedUsers")),
                    parseStringToList((String) postData.get("dislikedUsers")));
            return currentPost;
        } catch (Errors.DatabaseException e) {
            return null;
        }
    }

    public List<Post> searchPosts(String query, int page) {
        refreshDB();
        List<Post> posts = new ArrayList<>();
        try {
            String sql = "SELECT * FROM PostsHeaders WHERE LOWER(postTitle) LIKE LOWER('%" + query + "%') LIMIT 11 OFFSET " + (page - 1) * 10;
            ResultSet rs = this.headersTable.runQuery(sql);
            while (rs.next()) {
                posts.add(createPostFromResultSet(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return posts;
    }

    public int newPost(int authorID, String title, String content) {
        Value postHeaders = new Value();
        postHeaders.addItem("postID", "AutoIncrement");
        postHeaders.addItem("authorID", authorID);
        postHeaders.addItem("postTitle", title);
        postHeaders.addItem("postEpoch", (int) (Instant.now().getEpochSecond()));
        postHeaders.addItem("likes", 0);
        postHeaders.addItem("dislikes", 0);

        Value postContent = new Value();
        postContent.addItem("postID", "AutoIncrement");
        postContent.addItem("content", content.replace("'", "''"));
        postContent.addItem("likedUsers", "[0]");
        postContent.addItem("dislikedUsers", "[0]");

        try {
            this.contentTable.addItem(postContent, false);
            this.headersTable.addItem(postHeaders, false);
            this.refreshDB();

            User user = this.users.getUser(authorID);
            this.users.newPost(authorID, getLastID(authorID));
            return 0;
        } catch (Errors.DatabaseException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public int getTotalPosts(String query){
        try {
            ResultSet rs = this.headersTable.runQuery("SELECT COUNT(*) FROM PostsHeaders WHERE LOWER(postTItle) LIKE LOWER('%" + query + "%');");
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int likePost(int postID, int authorID) {
        Value newValue = new Value();
        newValue.addItem("likes", "likes + 1");
        Value likedUsers = new Value();
        likedUsers.addItem("likedUsers", " SUBSTR(likedUsers, 1, LENGTH(likedUsers) - 1) || ', " + authorID + "]'");
        try {
            this.headersTable.editItem("postID=" + postID, newValue, true);
            this.contentTable.editItem("postID=" + postID, likedUsers, true);
            this.refreshDB();
            return 0;
        } catch (Errors.DatabaseException e) {
            System.out.println(e.getMessage());
            return 1;
        }
    }

    public int dislikePost(int postID, int authorID) {
        Value newValue = new Value();
        newValue.addItem("dislikes", "dislikes + 1");
        Value dislikedUsers = new Value();
        dislikedUsers.addItem("dislikedUsers", " SUBSTR(dislikedUsers, 1, LENGTH(dislikedUsers) - 1) || ', " + authorID + "]'");
        try {
            this.headersTable.editItem("postID=" + postID, newValue, true);
            this.contentTable.editItem("postID=" + postID, dislikedUsers, true);
            this.refreshDB();
            return 0;
        } catch (Errors.DatabaseException e) {
            return 1;
        }
    }

    public void resetLikes(int postID, int authorID) {
        // Convert to modifiable lists
        List<Integer> likedUsers = new ArrayList<>(getPost(postID).getLikedUsers());
        List<Integer> dislikedUsers = new ArrayList<>(getPost(postID).getDislikedUsers());

        boolean likeRemoved = false;
        boolean dislikeRemoved = false;

        try {
            likeRemoved = likedUsers.remove(Integer.valueOf(authorID));  // Remove by value
            dislikeRemoved = dislikedUsers.remove(Integer.valueOf(authorID));  // Remove by value
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            this.contentTable.editItem("postID=" + postID, new Value() {{
                addItem("likedUsers", likedUsers.toString().replace(" ", "")); // Ensure the format is correct
                addItem("dislikedUsers", dislikedUsers.toString().replace(" ", "")); // Ensure the format is correct
            }}, false);

            Value updateValues = new Value();
            if (likeRemoved) {
                updateValues.addItem("likes", "likes - 1");
            }
            if (dislikeRemoved) {
                updateValues.addItem("dislikes", "dislikes - 1");
            }
            if(likeRemoved || dislikeRemoved) {
                this.headersTable.editItem("postID=" + postID, updateValues, true);
            }
            this.refreshDB();
        } catch (Errors.TableException e) {
            throw new RuntimeException(e);
        }
    }

    public int deletePost(int postID) {
        try{
            this.headersTable.deleteItem("postID="+postID);
            this.contentTable.deleteItem("postID="+postID);
            refreshDB();
            return 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 1;
        }
    }

    private static List<Integer> parseStringToList(String input) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(input.replaceAll("[\\[\\]]", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty()) // Filter out any empty strings
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private Post createPostFromResultSet(ResultSet rs) throws Exception {
        int postId = rs.getInt("postID");
        String postTitle = rs.getString("postTitle");
        int postAuthor = rs.getInt("authorID");
        int postEpoch = rs.getInt("postEpoch");
        int likes = rs.getInt("likes");
        int dislikes = rs.getInt("dislikes");
        return new Post(postId, postTitle, postAuthor, postEpoch, likes, dislikes);
    }

    private Post createPostFromValue(Value value) {
        int postId = (Integer) value.get("postID");
        String postTitle = (String) value.get("postTitle");
        int postAuthor = (Integer) value.get("authorID");
        int postEpoch = (Integer) value.get("postEpoch");
        return new Post(postId, postTitle, postAuthor, postEpoch, 0, 0);
    }
}