package queries;

import state.State;
import utilities.ConsoleReader;
import utilities.FileReadWrite;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Queries {

    public static ConsoleReader reader = new ConsoleReader();
    public static FileReadWrite fileReadWrite = new FileReadWrite();

    public static void menu (State state) {

        while (true) {
            System.out.print("\nQUERY>");
            String query = reader.readString();

            String[] queryParts = query.split(" ");

            if(queryParts[0].equalsIgnoreCase("create") && queryParts[1].equalsIgnoreCase("database")) {
                createDatabase(queryParts[2]);
            } else if(queryParts[0].equalsIgnoreCase("use") && queryParts[1].equalsIgnoreCase("database")) {
                useDatabase(queryParts[2], state);
            } else if (queryParts[0].equalsIgnoreCase("create") && queryParts[1].equalsIgnoreCase("table")) {
                createTable(queryParts[2], state, query);
            } else if (queryParts[0].equalsIgnoreCase("insert") && queryParts[1].equalsIgnoreCase("into")) {
                insertInto(queryParts[2], state, query);
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

    public static void createTable (String tableName, State state, String query) {

        if(state.getActiveDatabase() == null) {
            System.out.println("Please use a database first.");
            return;
        }

        List<String> tableList = fileReadWrite.getDirectories("databases/" + state.getActiveDatabase());

        tableName = tableName.trim();
        String[] tableNameParts = tableName.split("\\(");
        tableName = tableNameParts[0].trim();

        if (tableList.contains(tableName.toUpperCase())) {
            System.out.println("Table already exists. Please choose a different name.");
            return;
        }

        StringBuilder metaContent = new StringBuilder();
        metaContent.append("TABLE^").append(tableName).append("\n");

        String dbMetaContent = "TABLE^" + tableName + "\n";

        Pattern pattern = Pattern.compile("\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (matcher.find()) {
            String[] columnCommands = matcher.group(1).split(",");
            for (String command : columnCommands) {
                command = command.trim();

                String[] commandParts = command.split(" ");
                String columnName = commandParts[0];
                String columnType = commandParts[1];

                metaContent.append("COLUMN").append("^").append(columnName).append("^").append(columnType).append("\n");
            }
        } else {
            System.out.println("Invalid syntax. Please use the following syntax: create table <tableName> (columnName columnType, columnName columnType, ...)");
        }

        fileReadWrite.writeFile("databases/" + state.getActiveDatabase() + "/" + tableName.toUpperCase() + "/METADATA" , metaContent.toString());
        fileReadWrite.writeFile("databases/" + state.getActiveDatabase() + "/METADATA" , dbMetaContent);
        System.out.println("Table Created Successfully");
    }

    public static void insertInto (String tableName, State state, String query) {

        if(state.getActiveDatabase() == null) {
            System.out.println("Please use a database first.");
            return;
        }

        List<String> tableList = fileReadWrite.getDirectories("databases/" + state.getActiveDatabase());

        tableName = tableName.trim();
        String[] tableNameParts = tableName.split("\\(");
        tableName = tableNameParts[0].trim();

        if (!tableList.contains(tableName.toUpperCase())) {
            System.out.println("Table does not exist. Please create a table first.");
            return;
        }

        String tableMetaContent = fileReadWrite.readFile("databases/" + state.getActiveDatabase() + "/" + tableName.toUpperCase() + "/METADATA");

        Pattern pattern = Pattern.compile("\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        String[] insertValues = null;

        if (matcher.find()) {
            insertValues = matcher.group(1).split(",");
        } else {
            System.out.println("Invalid syntax. Please use the following syntax: insert into <tableName> values (value, value, ...)");
        }

        String[] tableMetaParts = tableMetaContent.split("\n");
        int columnCount = 0;
        for (String tableMetaPart : tableMetaParts) {
            if (tableMetaPart.startsWith("COLUMN")) {
                String[] columnMetaParts = tableMetaPart.split("\\^");
                String columnName = columnMetaParts[1];
                String columnType = columnMetaParts[2];

                if(columnType.equals("int")) {
                    try {
                        assert insertValues != null;
                        Integer.parseInt(insertValues[columnCount]);
                    } catch (Exception e) {
                        System.out.println("Invalid syntax. Please use the following syntax: insert into <tableName> values (value, value, ...)");
                        return;
                    }
                } else if(columnType.equals("varchar")) {
                    try {
                        assert insertValues != null;
                        if(insertValues[columnCount] == null) {
                            System.out.println("Invalid syntax. Please use the following syntax: insert into <tableName> values (value, value, ...)");
                            return;
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid syntax. Please use the following syntax: insert into <tableName> values (value, value, ...)");
                        return;
                    }
                }
                columnCount++;
            }
        }

        StringBuilder rowContent = new StringBuilder();
        assert insertValues != null;
        for (String insertValue : insertValues) {
            rowContent.append(insertValue.trim()).append("^");
        }
        rowContent.deleteCharAt(rowContent.length() - 1);
        rowContent.append("\n");

        fileReadWrite.writeFile("databases/" + state.getActiveDatabase() + "/" + tableName.toUpperCase() + "/DATA", rowContent.toString());

        System.out.println("Inserted Successfully");
    }
}
