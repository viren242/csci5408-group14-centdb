package utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FileReadWrite {

    public final String rootPath = "src/main/resources/";

    public String getFileName(String type) {
        switch (type) {
            case "USER":
                return rootPath + "USER.txt";
            default:
                return rootPath + "FLUSH.txt";
        }
    }

    public String readFile(String type) {

        StringBuilder stringBuilder = new StringBuilder();
        String fileName = getFileName(type);

        try {

            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String currentLine = null;

            while ((currentLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(currentLine + "\n");
            }

            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("Error reading file: " + fileName);
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public void writeFile(String type, String content) {

        String fileName = getFileName(type);

        try {

            FileWriter fileWriter = new FileWriter(fileName, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            printWriter.print(content);
            printWriter.close();

            fileWriter.close();

        } catch (Exception e) {
            System.out.println("Error writing file: " + fileName);
            e.printStackTrace();
        }
    }

}
