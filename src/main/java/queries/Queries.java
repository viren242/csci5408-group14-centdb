package queries;

import state.State;
import utilities.ConsoleReader;
import utilities.FileReadWrite;

import java.util.*;

public class Queries {

    public static ConsoleReader reader = new ConsoleReader();
    public static FileReadWrite fileReadWrite = new FileReadWrite();
    public static State state;

    public static void menu (State state) {

        while (true) {
            System.out.print("\nQUERY>");
            String query = reader.readString();

            String[] queryParts = query.split(" ");

            if(queryParts[0].equalsIgnoreCase("create") && queryParts[1].equalsIgnoreCase("database")) {
                createDatabase(queryParts[2]);
            } else if(queryParts[0].equalsIgnoreCase("use") && queryParts[1].equalsIgnoreCase("database")) {
                useDatabase(queryParts[2], state);
            } else if(queryParts[0].equalsIgnoreCase("exit")) {
                break;
            }
        }
    }

    public static void createDatabase (String databaseName) {

        List<String> databaseList = fileReadWrite.getDirectories("databases");

        if (databaseList.contains(databaseName.toUpperCase())) {
            System.out.println("Database already exists. Please choose a different name.");
            return;
        }

        String metaContent = "";

        metaContent += "DATABASE^" + databaseName + "\n";

        fileReadWrite.writeFile("databases/" + databaseName.toUpperCase() + "/METADATA" , metaContent);

        System.out.println("Database Created Successfully");
    }

    public static void useDatabase (String databaseName, State state) {

        List<String> databaseList = fileReadWrite.getDirectories("databases");

        if (!databaseList.contains(databaseName.toUpperCase())) {
            System.out.println("Database does not exist. Please create a database first.");
            return;
        }
        state.setActiveDatabase(databaseName);
        System.out.println("Now using database: " + state.getActiveDatabase());
    }
}
