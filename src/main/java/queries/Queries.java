package queries;

import utilities.ConsoleReader;
import utilities.FileReadWrite;

import java.util.*;

public class Queries {

    public static ConsoleReader reader = new ConsoleReader();
    public static FileReadWrite fileReadWrite = new FileReadWrite();

    public static void menu () {

        while (true) {
            System.out.println("\nSelect Query Type: ");
            System.out.println("1. Create Database");
            System.out.println("2. Use Database");
            System.out.println("3. Create Table");
            System.out.println("4. Insert Row");
            System.out.println("5. Select Row");
            System.out.println("6. Update Row");
            System.out.println("7. Delete Row");
            System.out.println("8. Drop Table");
            System.out.println("9. Go Back");

            int choice = reader.readInt();

            switch (choice) {
                case 1:
                    createDatabase();
                    break;
                case 2:
//                useDatabase();
                    break;
                case 3:
//                createTable();
                    break;
                case 4:
//                insertRow();
                    break;
                case 5:
//                selectRow();
                    break;
                case 6:
//                updateRow();
                    break;
                case 7:
//                deleteRow();
                    break;
                case 8:
//                dropTable();
                    break;
                case 9:
                    break;
                default:
                    System.out.println("Invalid Choice");
            }

            if (choice == 9) {
                break;
            }
        }
    }

    public static void createDatabase () {

        System.out.println("Enter Database Name: ");
        String databaseName = reader.readString();

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
}
