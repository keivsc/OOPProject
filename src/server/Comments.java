package server;

import com.keivsc.SQLiteJava.*;


class Comment{
    private int id;
    private String comment;
    private String commentAuthor;

    public Comment(int id, String comment, String commentAuthor) {
        this.id = id;
        this.comment = comment;
        this.commentAuthor = commentAuthor;
    }

    public int getId() {
        return id;
    }
    public String getComment() {
        return comment;
    }
    public String getCommentAuthor() {
        return commentAuthor;
    }

}

//COMMENTID SHOULD BE STRUCTURED LIKE "postID-authorID-(randomNumber/IncrementNumber)"

public class Comments {
    private Database db;
    private Table commentsTable;
    private Utils utils;

    public Comments(){
        Utils utils = new Utils();
        try{
            this.db = new Database("Comments.db");
            this.commentsTable = db.createTable("PostComments", new String[]{"commentID TEXT PRIMARY KEY", "content TEXT NOT NULL", "authorID TEXT NOT NULL", "postID INTEGER NOT NULL", "commentEpoch INTEGER NOT NULL"});
        } catch (Errors.DatabaseException e) {
            throw new RuntimeException(e);
        }
    }
    private void refreshDB(){
        try {
            this.db.close();
            this.db = new Database("Content.db");
            this.commentsTable = this.db.connectTable("PostComments");
        }catch(Exception e){

        }
    }
//
//    public Comment getComment(int postID, int id){
//
//    }
//
//    public int removeComment(int postID, int id){
//
//    }
//
//    public int addComment(int postID, String comment, int authorID){
//        String commentID = postID+"-"+authorID;
//    }
}
