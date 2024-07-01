package server;

import com.keivsc.SQLiteJava.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class Utils {
    private byte[] salt;
    public Utils(){
        try {
            Database db = new Database("Server.db");
            String[] Columns = {
                    "name TEXT NOT NULL",
                    "value TEXT NOT NULL",
            };
            Table tb = db.createTable("ServerData", Columns);
            List<Value> hashedList = tb.getItems("name = 'hashedSalt'");
            if (!hashedList.isEmpty()) {
                this.salt = base64Decode(hashedList.getFirst().get("value").toString()).getBytes();
            }else{
                this.salt = new byte[16];
                new SecureRandom().nextBytes(this.salt);
                Value hashedValue = new Value();
                hashedValue.addItem("name", "hashedSalt");
                hashedValue.addItem("value", base64Encode(Arrays.toString(this.salt)));
                tb.addItem(hashedValue, false);
            }
            db.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Utils utils = new Utils();
        String hashed = utils.hashText("0193173906abcd");
        System.out.println(hashed);
        String decode = utils.base64Decode(hashed);
        System.out.println(decode);
        String hashed2 = utils.hashText("0193173906abcd");
        System.out.println(hashed2);
        if (!hashed.equals(hashed2)) {
            System.out.println("Different");
        }

    }

    public String base64Encode(String text){
        return Base64.getEncoder().encodeToString(text.getBytes());
    }

    public String base64Decode(String text){
        return new String(Base64.getDecoder().decode(text));
    }

    public String hashText(String text) {

        KeySpec spec = new PBEKeySpec(text.toCharArray(), this.salt, 65536, 128);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            String hashed =Arrays.toString(factory.generateSecret(spec).getEncoded());
            return base64Encode(hashed);
        }catch(NoSuchAlgorithmException e) {
            return null;
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

    }

}


