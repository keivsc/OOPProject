import gui.*;
import server.Data;
import server.Posts;
import server.Users;

public class Main{
    public static void main(String[] args) {
        Data data = new Data(new Users(), new Posts());
        new ClimateActionProgram(data);
    }
}