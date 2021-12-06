import utilities.FileReadWrite;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("SELECT(\\s.*)FROM(\\s.*)|WHERE(\\s.*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher("select * from table");
        FileReadWrite fileReadWrite = new FileReadWrite();
    }
}
