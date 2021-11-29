package utilities;

import java.util.*;
import java.io.*;

public class FileReadWrite {

    public final String rootPath = "src/main/resources/";

    public String readFile(String path) {

        StringBuilder stringBuilder = new StringBuilder();
        String fileName = rootPath + path + ".data";

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

    public void writeFile(String path, String content) {

        String fileName = rootPath + path + ".data";

        try {

            File file = new File(fileName);
            file.getParentFile().mkdirs();

            FileWriter fileWriter = new FileWriter(file, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            printWriter.print(content);
            printWriter.close();

            fileWriter.close();

        } catch (Exception e) {
            System.out.println("Error writing file: " + fileName);
            e.printStackTrace();
        }
    }

    public List<String> getDirectories (String path) {
        List<String> directories = new ArrayList<>();
        File directory = new File(rootPath + path);
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                directories.add(file.getName());
            }
        }
        return directories;
    }

}
