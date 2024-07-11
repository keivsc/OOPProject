package server;
import com.keivsc.SQLiteJava.*;
import server.types.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;



public class Users {
    private Database database;
    private Table tb;
    private final Utils utils = new Utils();
    public Users(){
        try {
            this.database = new Database("Server.db");
            this.tb = this.database.createTable("Users", new String[]{"id INTEGER PRIMARY KEY AUTOINCREMENT", "email TEXT NOT NULL", "username TEXT NOT NULL", "password TEXT NOT NULL", "clearance INTEGER NOT NULL", "posts TEXT NOT NULL"});
            this.refreshDB();
        }catch(Errors.DatabaseException ignored){

        }
    }

    private void refreshDB(){
        try {
            this.database.close();
            this.database = new Database("Server.db");
            this.tb = this.database.connectTable("Users");
        }catch(Errors.DatabaseException e){
            throw new RuntimeException(e);
        }
    }

    public List<Integer> getPosts(int id){
        return getUser(id).getPosts();
    }

    public void newPost(int authorID, int PostID){
        refreshDB();
        try{
            List<Integer> postIDs = new ArrayList<>(getPosts(authorID)); // Ensure postIDs is mutable
            postIDs.add(PostID);
            String postIDsString = postIDs.toString().replace(" ", ""); // Format the list as a string without spaces
            this.tb.editItem("id="+authorID, new Value(){{addItem("posts", postIDsString);}}, false);
        }catch(Errors.DatabaseException e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deletePost(int userID, int postID){
        try {
            List<Integer> newPosts = new ArrayList<>(this.getPosts(userID));
            newPosts.remove((Integer) postID);
            this.tb.editItem("id="+userID, new Value(){{addItem("posts", newPosts.toString().replace(" ", ""));}}, false);
            refreshDB();
        } catch (Errors.TableException e) {
            throw new RuntimeException(e);
        }
    }

    public int newUser(String username, String email, String password, String authCode){
        refreshDB();
        int clearance = 0;
        if (Objects.equals(authCode, "AUTH")){
            clearance = 1;
        }
        try {
            List<Value> userCheck = this.tb.getItems("email='" + email + "'");
            if(!userCheck.isEmpty()){
                return 1;
            }
        }catch(Errors.DatabaseException e){
            throw new RuntimeException(e);
        }

        Value user = new Value();
        user.addItem("id", "AutoIncrement");
        user.addItem("email", email);
        user.addItem("username", username);
        user.addItem("password", this.utils.hashText(password+email));
        user.addItem("clearance", clearance);
        user.addItem("posts", "[]");
        try {
            this.tb.addItem(user, false);
            this.refreshDB();
        }catch(Errors.DatabaseException e){
            throw new RuntimeException(e);
        }
        return 0;
    };

    public User getUser(int id){
        refreshDB();
        try{
            List<Value> user = this.tb.getItems("id="+id);
            if(user.isEmpty()){
                return null;
            }
            Value userValue = user.getFirst();
            return new User((String) userValue.get("email"), (String) userValue.get("username"), (Integer) userValue.get("clearance"), (Integer) userValue.get("id"), (String) userValue.get("posts"));
        } catch (Errors.TableException e) {
            throw new RuntimeException(e);
        }
    }

    public User authorize(String email, String password){
        refreshDB();
        try {
            List<Value> user = this.tb.getItems("email = '" + email + "'AND password='" + this.utils.hashText(password+email) + "'");
            if (user.isEmpty()){
                return null;
            }
            Value userValue = user.getFirst();
            return new User((String) userValue.get("email"), (String) userValue.get("username"), (Integer) userValue.get("clearance"), (Integer) userValue.get("id"), (String) userValue.get("posts"));
        }catch(Errors.DatabaseException e){
            return null;
        }
    }

    public int changePassword(String email, String oldPassword, String newPassword){
        refreshDB();
        try{
            if(this.authorize(email, oldPassword) != null){
                Value newPasswordValue = new Value();
                newPasswordValue.addItem("password", this.utils.hashText(newPassword+email));
                this.tb.editItem("email = '" + email + "'", newPasswordValue, false);
                this.refreshDB();
                return 0;
            };
        } catch (Errors.TableException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    public String getUsername(int id){
        refreshDB();
        try {
            Value user = this.tb.getItems("id=" + id).getFirst();
            return (String)user.get("username");
        } catch (Errors.TableException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args){
        Users users = new Users();
        users.newUser("Adwin Chee Hansen", "adwin.hansen@gmail.com", "0193173906abcd", null);

    }



}
