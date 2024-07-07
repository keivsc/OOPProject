package server;

import com.keivsc.SQLiteJava.*;
import server.types.Comment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comments {
    private Database db;
    private final Map<Integer, Table> commentsTable = new HashMap<>();

    public Comments() {
        try {
            this.db = new Database("Comments.db");
        } catch (Errors.DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshDB() {
        try {
            this.db.close();
            this.db = new Database("Comments.db");
            for (Map.Entry<Integer, Table> entry : this.commentsTable.entrySet()) {
                commentsTable.put(entry.getKey(), this.db.connectTable(entry.getValue().Name));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Comment> getCommentsFromPost(int postID) {
        try {
            refreshDB();
            List<Comment> comments = new ArrayList<>();
            String query = "SELECT * FROM post_" + postID;
            ResultSet rs = getCommentsTable(postID).runQuery(query);

            while (rs.next()) {
                comments.add(new Comment(rs.getInt("id"), rs.getString("content"), rs.getInt("authorID"), (int)rs.getInt("commentEpoch")));
            }
            return comments;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ArrayList<>();
        }
    }

    public Comment getComment(int postID, int id) {
        try {
            refreshDB();
            List<Value> comments = getCommentsTable(postID).getItems("id=" + id);
            if (comments.isEmpty()) return null;

            Value commentValue = comments.getFirst();
            return new Comment((Integer) commentValue.get("id"), (String) commentValue.get("content"), (Integer) commentValue.get("authorID"), (Integer) commentValue.get("commentEpoch"));
        } catch (Errors.TableException e) {
            throw new RuntimeException(e);
        }
    }

    public int removeComment(int postID, int id) {
        try {
            refreshDB();
            getCommentsTable(postID).deleteItem("id=" + id);
            return 0;
        } catch (Errors.TableException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public int addComment(int postID, String comment, int authorID) {
        try {
            refreshDB();
            Value commentValue = new Value();
            commentValue.addItem("id", "AutoIncrement");
            commentValue.addItem("content", comment.replace("'", "''"));
            commentValue.addItem("authorID", authorID);
            commentValue.addItem("commentEpoch", (int) (Instant.now().getEpochSecond()));
            getCommentsTable(postID).addItem(commentValue, false);
            return 0;
        } catch (Errors.DatabaseException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public int getLastID(int postID){
        try {
            ResultSet rs = getCommentsTable(postID).runQuery("SELECT MAX(id) AS lastId FROM post_" + postID);
            while (rs.next()){
                return rs.getInt("lastId");
            }
            return 0;
        } catch (Errors.QueryException e) {
            return 0;
        } catch (Errors.TableException e) {
            return 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    private Table getCommentsTable(int postID) throws Errors.TableException {
        return commentsTable.computeIfAbsent(postID, k -> {
            try {
                return this.db.createTable("post_" + postID, new String[]{
                        "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT",
                        "content TEXT NOT NULL",
                        "authorID INTEGER NOT NULL",
                        "commentEpoch INTEGER NOT NULL",
                });
            } catch (Errors.TableException e) {
                throw new RuntimeException(e);
            }
        });
    }
}