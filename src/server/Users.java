package server;
import com.keivsc.SQLiteJava.*;

import java.util.List;
import java.util.Objects;

class User{
    private int id;
    private String username;
    private int clearance;
    private String email;


    public User(String email, String username, int clearance, int id){
        this.id = id;
        this.username = username;
        this.clearance = clearance;
        this.email = email;
    }

    public int getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public int getClearance() {
        return clearance;
    }
    public String getEmail() {
        return email;
    }

}

public class Users {
    private Database database;
    private Table tb;
    private final Utils utils = new Utils();
    public Users(){
        try {
            this.database = new Database("Server.db");
            this.tb = this.database.createTable("Users", new String[]{"id INTEGER PRIMARY KEY AUTOINCREMENT", "email TEXT NOT NULL", "username TEXT NOT NULL", "password TEXT NOT NULL", "clearance INTEGER NOT NULL"});
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
        try {
            this.tb.addItem(user, false);
            this.refreshDB();
        }catch(Errors.DatabaseException e){
            throw new RuntimeException(e);
        }
        return 0;
    };

    public User authorize(String email, String password){
        refreshDB();
        try {
            List<Value> user = this.tb.getItems("email = '" + email + "'AND password='" + this.utils.hashText(password+email) + "'");
            if (user.isEmpty()){
                return null;
            }
            Value userValue = user.getFirst();
            return new User((String) userValue.get("email"), (String) userValue.get("username"), (Integer) userValue.get("clearance"), (Integer) userValue.get("id"));
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


    public static void main(String[] args){
        Users users = new Users();
        users.newUser("Adwin Chee Hansen", "adwin.hansen@gmail.com", "0193173906abcd", null);

    }



}
