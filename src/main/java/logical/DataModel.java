package logical;

import java.util.List;

import utilities.ConsoleReader;
import utilities.FileReadWrite;

public class DataModel {

	public static ConsoleReader reader = new ConsoleReader();
	public static FileReadWrite fileReadWrite = new FileReadWrite();
	
	public static void menu() {
		while(true) {
			System.out.print("\nData Modelling - Please enter the database name>");
	        String query = reader.readString();
	        query = query.toUpperCase();
	        
	        if(databaseExist(query)) {
	        	createModel(query);
	        }
	        else {
	        	System.out.print("\nTry again>");;
	        }
		}
	}
	
	public static boolean databaseExist(String databaseName) {
		List<String> databaseList = fileReadWrite.getDirectories("databases");
		
		if (databaseList.contains(databaseName)) {
            return true;
        }
		else {
			System.out.println("Database " + databaseName + " does not exist");
            return false;
		}
	}
	
	public static void createModel(String databaseName) {
		String path = "databases/" + databaseName;
		List<String> tableNames = fileReadWrite.getDirectories(path);
		System.out.println(tableNames);
		
		for(String tableName : tableNames) {
			String tableMetaData = fileReadWrite.readFile(path + "/" + tableName + "/METADATA");
			System.out.println(tableMetaData);
		}
	}
	
	
}
