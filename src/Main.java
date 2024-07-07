import gui.*;
import server.Comments;
import server.Data;
import server.Posts;
import server.Users;

public class Main{
    public static void main(String[] args) {
        Data data = new Data(new Users(), new Posts(), new Comments());
        new ClimateActionProgram(data);
    }
}