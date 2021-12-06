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
	        
	        if(query.contentEquals("EXIT")) {
	        	break;
	        }
	        
	        if(databaseExist(query)) {
	        	createModel(query);
	        }
	        else {
	        	System.out.print("\nTry again");;
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
		String dataModelFile = databaseName + "_ERD_MODEL";
		StringBuilder fileContent = new StringBuilder();
		fileContent.append("**************************Entity Relationship Diagram for database " + databaseName + "**************************" + "\n\n");
		List<String> tableNames = fileReadWrite.getDirectories(path);
		fileContent.append("Total number of relations/tables in the database " + databaseName + " = " + tableNames.size() + "\n\n");
		//System.out.println(tableNames);
		
		for(String tableName : tableNames) {
			int columnNumber = 0;
			String columnName = "";
			String tableMetaData = fileReadWrite.readFile(path + "/" + tableName + "/METADATA");
			String tableMetaDataParts[];
			tableMetaDataParts = tableMetaData.split("\n");

			for(String part : tableMetaDataParts) {
				if(part.contains("TABLE")) {
					continue;
				}
				else if(part.contains("COLUMN")) {
					columnName = part.substring(part.indexOf("^") + 1).substring(0, part.substring(part.indexOf("^") + 1).indexOf("^"));
					columnNumber++;
					
					fileContent.append("TABLE NAME: " + tableName + " ::" + " COLUMN_NAME_" + columnNumber + " : "+ columnName + "\n");
				}
				
			}
		}
		fileReadWrite.writeFile(dataModelFile, fileContent.toString());
	}	
}
