package queries;

import logGenerator.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import state.State;
import utilities.ConsoleReader;
import utilities.FileReadWrite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Queries {

    public static ConsoleReader reader = new ConsoleReader();
    public static FileReadWrite fileReadWrite = new FileReadWrite();
    public static State state;
    public static Logger logger = new Logger();

    public static void menu(State newState) {

        while (true) {
            state = newState;
            System.out.print("\nQUERY>");
            String query = reader.readString();

            if (query.equalsIgnoreCase("exit")) {
                break;
            } else {
                processQuery(query);
            }
        }
    }

    public static void processQuery(String query) {
        String[] queryParts = query.split(" ");

        logger.generalLog(query, state);
        logger.queryLog(query, state);
        logger.eventLog("QUERY_EXECUTED", "A query was executed: " + query, state);

        if (queryParts[0].equalsIgnoreCase("create") && queryParts[1].equalsIgnoreCase("database")) {
            createDatabase(queryParts[2]);
        } else if (queryParts[0].equalsIgnoreCase("use") && queryParts[1].equalsIgnoreCase("database")) {
            useDatabase(queryParts[2]);
        } else if (queryParts[0].equalsIgnoreCase("create") && queryParts[1].equalsIgnoreCase("table")) {
            createTable(queryParts[2], query);
        } else if (queryParts[0].equalsIgnoreCase("insert") && queryParts[1].equalsIgnoreCase("into")) {
            insertInto(queryParts[2], query);
        } else if (queryParts[0].equalsIgnoreCase("select")) {
            select(query);
        } else if (queryParts[0].equalsIgnoreCase("update")) {
            update(query);
        } else if (queryParts[0].equalsIgnoreCase("delete")) {
            delete(query);
        } else if (queryParts[0].equalsIgnoreCase("drop")) {
            logger.eventLog("DROP_TABLE", "A table has been dropped", state);
            dropTable(query);
        } else if (queryParts[0].equalsIgnoreCase("begin") && queryParts[1].equalsIgnoreCase("transaction;")) {
            logger.eventLog("TRANSACTION_START", "A new transaction has started", state);
            Transaction();
            logger.eventLog("TRANSACTION_END", "The transaction has ended", state);
        }
    }

    public static void createDatabase(String databaseName) {

        List<String> databaseList = fileReadWrite.getDirectories("databases");

        if (databaseList.contains(databaseName.toUpperCase())) {
            System.out.println("Database already exists. Please choose a different name.");
            return;
        }

        String metaContent = "";
        metaContent += "DATABASE^" + databaseName + "\n";
        fileReadWrite.writeFile("databases/" + databaseName.toUpperCase() + "/METADATA", metaContent);

        System.out.println("Database Created Successfully");
    }

    public static void useDatabase(String databaseName) {

        List<String> databaseList = fileReadWrite.getDirectories("databases");

        if (!databaseList.contains(databaseName.toUpperCase())) {
            System.out.println("Database does not exist. Please create a database first.");
            return;
        }
        state.setActiveDatabase(databaseName);
        System.out.println("Now using database: " + state.getActiveDatabase());
    }

    public static void createTable(String tableName, String query) {

        if (state.getActiveDatabase() == null) {
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

        fileReadWrite.writeFile("databases/" + state.getActiveDatabase() + "/" + tableName.toUpperCase() + "/METADATA", metaContent.toString());
        fileReadWrite.writeFile("databases/" + state.getActiveDatabase() + "/METADATA", dbMetaContent);
        System.out.println("Table Created Successfully");
    }

    public static void insertInto(String tableName, String query) {

        if (state.getActiveDatabase() == null) {
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

                if (columnType.equals("int")) {
                    try {
                        assert insertValues != null;
                        Integer.parseInt(insertValues[columnCount]);
                    } catch (Exception e) {
                        System.out.println("Invalid syntax. Please use the following syntax: insert into <tableName> values (value, value, ...)");
                        return;
                    }
                } else if (columnType.equals("varchar")) {
                    try {
                        assert insertValues != null;
                        if (insertValues[columnCount] == null) {
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

    public static void select(String query) {

        if (state.getActiveDatabase() == null) {
            System.out.println("Please use a database first.");
            return;
        }

        Pattern pattern;
        String regex;
        if (query.contains("where")) {
            regex = "SELECT(\\s.*)FROM(\\s.*)WHERE(\\s.*)";
        } else {
            regex = "SELECT(\\s.*)FROM(\\s.*)|WHERE(\\s.*)";
        }

        pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        String columns = null;
        String tableName = null;
        String whereClause = null;
        String whereColumnName = null;
        String whereColumnValue = null;

        if (matcher.find()) {
            columns = matcher.group(1).trim();
            tableName = matcher.group(2).trim();
            if (query.contains("where")) {
                whereClause = matcher.group(3).trim();
            }
        } else {
            System.out.println("Invalid syntax. Please use the following syntax: select <columns> from <tableName> where <condition>");
        }

        if (whereClause != null) {
            Pattern pattern2 = Pattern.compile("(\\w+)(\\s*)(=)(\\s*)(\\w+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = pattern2.matcher(whereClause);
            if (!matcher2.find()) {
                System.out.println("Invalid syntax. Please use the following syntax: select <columns> from <tableName> where <condition>");
            }

            whereColumnName = matcher2.group(1);
            whereColumnValue = matcher2.group(5);
        }

        List<String> tableList = fileReadWrite.getDirectories("databases/" + state.getActiveDatabase());
        assert tableName != null;
        if (!tableList.contains(tableName.trim().toUpperCase())) {
            System.out.println("Table does not exist. Please create a table first.");
            return;
        }

        List<String> allColumnsList = new ArrayList<>();
        String tableMetaContent = fileReadWrite.readFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/METADATA");
        String[] tableMetaParts = tableMetaContent.split("\n");
        for (String tableMetaPart : tableMetaParts) {
            if (tableMetaPart.startsWith("COLUMN")) {
                String[] columnMetaParts = tableMetaPart.split("\\^");
                String columnName = columnMetaParts[1];
                allColumnsList.add(columnName);
            }
        }

        List<Integer> selectedColumnIndex = new ArrayList<>();
        StringBuilder tableContent = new StringBuilder();
        String tableDataContent = fileReadWrite.readFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/DATA");

        int columnCount = 0;
        for (String columnName : allColumnsList) {
            if (columns.contains(columnName) || columns.contains("*")) {
                tableContent.append(columnName).append("\t\t");
                selectedColumnIndex.add(columnCount);
            }
            columnCount++;
        }
        tableContent.append("\n");

        String[] tableDataParts = tableDataContent.split("\n");
        int whereColumnIndex = -1;
        if (query.contains("where")) {
            whereColumnIndex = allColumnsList.indexOf(whereColumnName);
        }

        for (String tableDataPart : tableDataParts) {
            String[] rowContent = tableDataPart.split("\\^");

            if (!query.contains("where") || (query.contains("where") && rowContent[whereColumnIndex].equals(whereColumnValue))) {
                int columnIndex = 0;
                for (String rowContentPart : rowContent) {

                    if (selectedColumnIndex.contains(columnIndex)) {
                        tableContent.append(rowContentPart).append("\t\t");
                    }
                    columnIndex++;
                }
                tableContent.append("\n");
            }
        }
        System.out.println(tableContent.toString());
    }

    public static void update(String query) {
        if (state.getActiveDatabase() == null) {
            System.out.println("Please use a database first.");
            return;
        }

        Pattern pattern = Pattern.compile("UPDATE(\\s.*)SET(\\s.*)WHERE(\\s.*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        String tableName = null;
        String setClause = null;
        String whereClause = null;

        if (matcher.find()) {
            tableName = matcher.group(1).trim();
            setClause = matcher.group(2).trim();
            whereClause = matcher.group(3).trim();
        } else {
            System.out.println("Invalid syntax. Please use the following syntax: UPDATE <tableName> SET <columnName> = <value> WHERE <condition>");
            return;
        }

        List<String> tableList = fileReadWrite.getDirectories("databases/" + state.getActiveDatabase());
        if (!tableList.contains(tableName.toUpperCase())) {
            System.out.println("Table does not exist. Please create a table first.");
            return;
        }

        List<String> allColumnsList = new ArrayList<>();
        String tableMetaContent = fileReadWrite.readFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/METADATA");
        String[] tableMetaParts = tableMetaContent.split("\n");
        for (String tableMetaPart : tableMetaParts) {
            if (tableMetaPart.startsWith("COLUMN")) {
                String[] columnMetaParts = tableMetaPart.split("\\^");
                String columnName = columnMetaParts[1];
                allColumnsList.add(columnName);
            }
        }

        String[] setClauseParts = setClause.split("=");
        String setColumnName = setClauseParts[0];
        String setColumnValue = setClauseParts[1];

        String[] whereClauseParts = whereClause.split("=");
        String whereColumnName = whereClauseParts[0];
        String whereColumnValue = whereClauseParts[1];

        int whereColumnIndex = allColumnsList.indexOf(whereColumnName);
        int setColumnIndex = allColumnsList.indexOf(setColumnName);

        if (setColumnIndex == -1 || whereColumnIndex == -1) {
            System.out.println("Invalid syntax. Please use the following syntax: UPDATE <tableName> SET <columnName> = <value> WHERE <condition>");
            return;
        }

        String tableDataContent = fileReadWrite.readFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/DATA");
        String[] tableDataParts = tableDataContent.split("\n");
        List<String> rows = new ArrayList<>(Arrays.asList(tableDataParts));

        for (int i = 0; i < rows.size(); i++) {
            String[] rowContent = rows.get(i).split("\\^");
            if (rowContent[whereColumnIndex].equals(whereColumnValue)) {
                rowContent[setColumnIndex] = setColumnValue;
                rows.set(i, String.join("^", rowContent));
            }
        }

        StringBuilder newTableContent = new StringBuilder();
        for (String row : rows) {
            newTableContent.append(row).append("\n");
        }
        fileReadWrite.overWriteFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/DATA", newTableContent.toString());

        System.out.println("Update successful.");
    }

    public static void delete(String query) {

        if (state.getActiveDatabase() == null) {
            System.out.println("Please use a database first.");
            return;
        }

        Pattern pattern = Pattern.compile("DELETE\\s*FROM(\\s.*)WHERE(\\s.*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        String tableName = null;
        String whereClause = null;

        if (matcher.find()) {
            tableName = matcher.group(1).trim();
            whereClause = matcher.group(2).trim();
        } else {
            System.out.println("Invalid syntax. Please use the following syntax: DELETE FROM <tableName> WHERE <condition>");
            return;
        }

        List<String> tableList = fileReadWrite.getDirectories("databases/" + state.getActiveDatabase());
        if (!tableList.contains(tableName.toUpperCase())) {
            System.out.println("Table does not exist. Please create a table first.");
            return;
        }

        List<String> allColumnsList = new ArrayList<>();
        String tableMetaContent = fileReadWrite.readFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/METADATA");
        String[] tableMetaParts = tableMetaContent.split("\n");
        for (String tableMetaPart : tableMetaParts) {
            if (tableMetaPart.startsWith("COLUMN")) {
                String[] columnMetaParts = tableMetaPart.split("\\^");
                String columnName = columnMetaParts[1];
                allColumnsList.add(columnName);
            }
        }

        String[] whereClauseParts = whereClause.split("=");
        String whereColumnName = whereClauseParts[0];
        String whereColumnValue = whereClauseParts[1];

        int whereColumnIndex = allColumnsList.indexOf(whereColumnName);

        if (whereColumnIndex == -1) {
            System.out.println("Invalid syntax. Please use the following syntax: DELETE FROM <tableName> WHERE <condition>");
            return;
        }

        String tableDataContent = fileReadWrite.readFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/DATA");
        String[] tableDataParts = tableDataContent.split("\n");
        List<String> rows = new ArrayList<>(Arrays.asList(tableDataParts));

        for (int i = 0; i < rows.size(); i++) {
            String[] rowContent = rows.get(i).split("\\^");
            if (rowContent[whereColumnIndex].equals(whereColumnValue)) {
                rows.remove(i);
            }
        }

        StringBuilder newTableContent = new StringBuilder();
        for (String row : rows) {
            newTableContent.append(row).append("\n");
        }
        fileReadWrite.overWriteFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/DATA", newTableContent.toString());

        System.out.println("Delete successful.");
    }

    public static void dropTable(String query) {

        if (state.getActiveDatabase() == null) {
            System.out.println("Please use a database first.");
            return;
        }

        Pattern pattern = Pattern.compile("DROP\\s*TABLE(\\s.*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        String tableName = null;

        if (matcher.find()) {
            tableName = matcher.group(1).trim();
        } else {
            System.out.println("Invalid syntax. Please use the following syntax: DROP TABLE <tableName>");
            return;
        }

        List<String> tableList = fileReadWrite.getDirectories("databases/" + state.getActiveDatabase());
        if (!tableList.contains(tableName.toUpperCase())) {
            System.out.println("Table does not exist. Please create a table first.");
            return;
        }

        fileReadWrite.deleteDirectory("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase());

        String dbMetadata = fileReadWrite.readFile("databases/" + state.getActiveDatabase() + "/METADATA");
        String[] dbMetadataParts = dbMetadata.split("\n");
        List<String> dbMetadataList = new ArrayList<>(Arrays.asList(dbMetadataParts));

        for (int i = 0; i < dbMetadataList.size(); i++) {
            if (dbMetadataList.get(i).startsWith("TABLE")) {
                String[] tableMetaParts = dbMetadataList.get(i).split("\\^");
                if (tableMetaParts[1].equals(tableName)) {
                    dbMetadataList.remove(i);
                }
            }
        }

        StringBuilder newDbMetadata = new StringBuilder();
        for (String dbMetadataPart : dbMetadataList) {
            newDbMetadata.append(dbMetadataPart).append("\n");
        }
        fileReadWrite.overWriteFile("databases/" + state.getActiveDatabase() + "/METADATA", newDbMetadata.toString());

        System.out.println("Drop Table successful.");
    }

    public static void Transaction() {
        System.out.print("\nTransaction has started");
        System.out.print("\nTRANSACTION>");
        // ArrayList<String> transaction = new ArrayList<String>();
        String transaction = "";
        String input = null;
        while (!"Commit;".equalsIgnoreCase(input)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            try {
                input = br.readLine();
            } catch (IOException ioe) {
                System.out.println("Wrong");
                System.exit(1);
            }
            if (!"Commit;".equalsIgnoreCase(input) && !"Rollback;".equalsIgnoreCase(input))
                transaction = transaction + input;
        }

        System.out.println("Transaction = " + transaction);
        String[] transactionParts = transaction.split(";");
        String[] transactionQuery;
        String query;
        String tableName;
        for (int i = 0; i < transactionParts.length; i++) {
            System.out.println(transactionParts[i]);
            query = transactionParts[i];
            transactionQuery = query.split(" ");
            // Insert here
            if (transactionQuery[0].equalsIgnoreCase("Insert")) {
                System.out.println("insert here");
                tableName = transactionQuery[2];
                if (state.getActiveDatabase() == null) {
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

                        if (columnType.equals("int")) {
                            try {
                                assert insertValues != null;
                                Integer.parseInt(insertValues[columnCount]);
                            } catch (Exception e) {
                                System.out.println("Invalid syntax. Please use the following syntax: insert into <tableName> values (value, value, ...)");
                                return;
                            }
                        } else if (columnType.equals("varchar")) {
                            try {
                                assert insertValues != null;
                                if (insertValues[columnCount] == null) {
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
                List a = new ArrayList();
                assert insertValues != null;
                for (String insertValue : insertValues) {
                    rowContent.append(insertValue.trim()).append("^");
                }
                rowContent.deleteCharAt(rowContent.length() - 1);
                rowContent.append("\n");
                System.out.println(rowContent);
                String rowValues = rowContent.toString();
                List myList = new ArrayList();
                myList.add(rowValues);
                System.out.println(myList.get(0));
                System.out.println("DO you want to insert the changes entre Y or N");
                Scanner scanner = new Scanner(System.in);
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("y")) {
                    fileReadWrite.writeFile("databases/" + state.getActiveDatabase() + "/" + tableName.toUpperCase() + "/DATA", String.valueOf(myList.get(0)));

                    System.out.println("Inserted Successfully");

                } else {
                    System.out.println("entre the new values seperated by ^");
                    userInput = scanner.nextLine();
                    myList.clear();
                    myList.add(userInput);

                    fileReadWrite.writeFile("databases/" + state.getActiveDatabase() + "/" + tableName.toUpperCase() + "/DATA", String.valueOf(myList.get(0)));
                    System.out.println("Inserted Successfully");


                }
            }
            String flag = "0";
            String logFlag;
            // update here
            if (transactionQuery[0].equalsIgnoreCase("Update")) {
                tableName = transactionQuery[1];
                String checkTableContent = fileReadWrite.readFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/QUERYLOG");
                System.out.println("table check 1 " + checkTableContent);
                String compareString = tableName + " " + "1";
                System.out.println("compare string " + compareString.length());
                System.out.println("compare string 1 " + checkTableContent.length());
                if (compareString.trim().equals(checkTableContent.trim())) {
                    System.out.println("table in use wait");
                } else {
                    System.out.println("you can update");
                    logFlag = "1";
                    fileReadWrite.writeFile("databases/" + state.getActiveDatabase() + "/" + tableName.toUpperCase() + "/QUERYLOG", tableName.trim() + " " + logFlag + "\n");
                    if (state.getActiveDatabase() == null) {
                        System.out.println("Please use a database first.");
                        return;
                    }

                    Pattern pattern = Pattern.compile("UPDATE(\\s.*)SET(\\s.*)WHERE(\\s.*)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(query);

                    tableName = null;
                    String setClause = null;
                    String whereClause = null;

                    if (matcher.find()) {
                        tableName = matcher.group(1).trim();
                        setClause = matcher.group(2).trim();
                        whereClause = matcher.group(3).trim();
                    } else {
                        System.out.println("Invalid syntax. Please use the following syntax: UPDATE <tableName> SET <columnName> = <value> WHERE <condition>");
                        return;
                    }

                    List<String> tableList = fileReadWrite.getDirectories("databases/" + state.getActiveDatabase());
                    if (!tableList.contains(tableName.toUpperCase())) {
                        System.out.println("Table does not exist. Please create a table first.");
                        return;
                    }

                    List<String> allColumnsList = new ArrayList<>();
                    String tableMetaContent = fileReadWrite.readFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/METADATA");
                    String[] tableMetaParts = tableMetaContent.split("\n");
                    for (String tableMetaPart : tableMetaParts) {
                        if (tableMetaPart.startsWith("COLUMN")) {
                            String[] columnMetaParts = tableMetaPart.split("\\^");
                            String columnName = columnMetaParts[1];
                            allColumnsList.add(columnName);
                        }
                    }

                    String[] setClauseParts = setClause.split("=");
                    String setColumnName = setClauseParts[0];
                    String setColumnValue = setClauseParts[1];

                    String[] whereClauseParts = whereClause.split("=");
                    String whereColumnName = whereClauseParts[0];
                    String whereColumnValue = whereClauseParts[1];

                    int whereColumnIndex = allColumnsList.indexOf(whereColumnName);
                    int setColumnIndex = allColumnsList.indexOf(setColumnName);

                    if (setColumnIndex == -1 || whereColumnIndex == -1) {
                        System.out.println("Invalid syntax. Please use the following syntax: UPDATE <tableName> SET <columnName> = <value> WHERE <condition>");
                        return;
                    }

                    String tableDataContent = fileReadWrite.readFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/DATA");
                    String[] tableDataParts = tableDataContent.split("\n");
                    List<String> rows = new ArrayList<>(Arrays.asList(tableDataParts));

                    for (i = 0; i < rows.size(); i++) {
                        String[] rowContent = rows.get(i).split("\\^");
                        if (rowContent[i].equals(whereColumnValue)) {
                            rowContent[setColumnIndex] = setColumnValue;
                            rows.set(i, String.join("^", rowContent));
                        }
                    }

                    StringBuilder newTableContent = new StringBuilder();
                    for (String row : rows) {
                        newTableContent.append(row).append("\n");
                    }
                    String updateValues = newTableContent.toString();
                    List myList = new ArrayList();
                    myList.add(updateValues);
                    System.out.println("DO you want to update the changes entre Y or N");
                    Scanner scanner = new Scanner(System.in);
                    String userInput = scanner.nextLine();
                    if (userInput.equalsIgnoreCase("y")) {
                        System.out.println("update values here" + myList.get(0));
                        fileReadWrite.overWriteFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/DATA", String.valueOf(myList.get(0)));
                        System.out.println("Update successful.");
                        fileReadWrite.overWriteFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/QUERYLOG", "");
                    } else {
                        System.out.println("entre the new values seperated by ^");
                        userInput = scanner.nextLine();
                        myList.clear();
                        myList.add(userInput);
                        fileReadWrite.writeFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/DATA", String.valueOf(myList.get(0)));
                        System.out.println("update Successfully");
                        fileReadWrite.overWriteFile("databases/" + state.getActiveDatabase() + "/" + tableName.trim().toUpperCase() + "/QUERYLOG", "");
                    }
                }
            }
            processQuery(transactionParts[0]);
        }
    }
}
