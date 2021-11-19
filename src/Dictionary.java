import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.*;


public class Dictionary {

    public static BiMap <String, String> dictionary = HashBiMap.create();
    public final String dataPath;
    public final String tempDataPath; // used for DeleteFromFile function

    public Dictionary() {
        dataPath = "./dictionary.txt";
        tempDataPath = "./temp_dictionary.txt";
        LoadDictionaryData();
    }
    public Dictionary(String filePath, String tempDataPath) {
        dataPath = filePath;
        this.tempDataPath = tempDataPath;
        LoadDictionaryData();
    }

    public void WriteToFile(String line)
    {
        try
        {
            FileWriter fw = new FileWriter("dictionary.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(line);
            bw.newLine();
            bw.close();
            fw.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void DeleteFromFile(String word) {
        try {
            FileInputStream file = new FileInputStream(dataPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(file));
            FileWriter fw = new FileWriter("temp_dictionary.txt", false);
            BufferedWriter bw = new BufferedWriter(fw);
            String line = null;
            while((line = br.readLine()) != null)
            {
                String[] tempContainer = line.split(";");
                if (tempContainer[0].equals(word) || tempContainer[1].equals(word))
                {
                    continue;
                }
                else
                {
                    bw.write(line);
                    bw.newLine();
                }
            }
            bw.close();
            br.close();
            file.close();
            fw.close();
        } catch (FileNotFoundException fnf) {
            System.out.println("Cannot find the specified database: " + dataPath);
            System.err.println(fnf.getMessage());
        } catch (IOException ioException){
            ioException.printStackTrace();
        }

        try
        {
            //Delete file
            Files.deleteIfExists(Paths.get(dataPath));
            //Rename file
            Files.move(Paths.get(tempDataPath),Paths.get(dataPath));
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public void LoadDictionaryData() {
        try {
            FileInputStream file = new FileInputStream(dataPath);
            BufferedReader br = new BufferedReader (new InputStreamReader(file));

            String currentLine;
            while ((currentLine = br.readLine()) != null){
                String[] tempContainer = currentLine.split(";");
                if (IsValidWordPair(tempContainer)) {
                    try {
                        //Eng-Vn
                        dictionary.put(tempContainer[0], tempContainer[1]); // inverse bi-map
                    } catch (IllegalArgumentException iae) {
                        System.out.println("Skipped a duplicated pair");
                        System.err.println(iae.getMessage());
                    }
                } else {
                    System.out.println("Skipped an invalid word pair");
                }
            }
            file.close();
            br.close();
        } catch (FileNotFoundException fnf) {
            System.out.println("Cannot find the specified database: " + dataPath);
            System.err.println(fnf.getMessage());
        } catch (IOException ioe) {
            System.out.println("Cannot load database from file");
            System.err.println(ioe.getMessage());
        }
    }

    public static boolean IsValidWordPair(String[] wordPair) {
        if (wordPair.length != 2) {
            return false;
        }
        return !NormalizeSpaces(wordPair[0]).isEmpty()
                && !NormalizeSpaces(wordPair[1]).isEmpty();
    }

    public String LookUp (String word){
        if (dictionary.get(word) != null){
            return dictionary.get(word);
        } else if (dictionary.inverse().get(word) != null){
            return dictionary.inverse().get(word);
        } else {
            return "Cannot find " + word + " in our dictionary.";
        }
    }

    public String HandleInput(String input) {
        // pre-processing
        input = NormalizeSpaces(input);
        input = input.toLowerCase();

        // command check
        if (input.contains(";")) {
            // probably a command if word contains ";"
            String[] separate = input.split(";"); // splits input into parts
            if (separate.length == 3) {
                // probably the add command
                // separate[0]: ADD
                // separate[1]: Eng word
                // separate[2]: Viet word
                if (separate[0].equals("add")) {
                    if (!separate[1].isEmpty()
                        && !separate[2].isEmpty()) {
                        // check if duplicated pair and add
                        System.out.println("adding");
                        return "added";
                    } else {
                        return "Invalid add command.\nEg: ADD;apple;tao";
                    }

                } else {
                    return "Unknown command";
                }
            } else if (separate.length == 2) {
                // probably the delete command
                // separate[0]: DEL
                // separate[1]: word
                if (separate[0].equals("del")) {
                    if (!separate[1].isEmpty()) {
                        // check if existed and del
                        System.out.println("Deleting");
                        return "deleted";
                    } else {
                        return "Invalid delete command.\nEg: DEL;apple";
                    }
                } else {
                    return "Unknown command";
                }
            } else {
                return "Invalid input. A word shouldn't contain ';'";
            }
        }

        return LookUp(input);
    }

    public static String NormalizeSpaces(String text){

        return text.trim().replaceAll(" +", " ");
    }

    public static void main(String[] args){
        /*
        Dictionary dictionary = new Dictionary();
        String input = "DEL;b";
        String processedInput = dictionary.HandleInput(input);
        System.out.println(processedInput);
        */

        Dictionary dictionary = new Dictionary();
        dictionary.DeleteFromFile("assault rifle");
    }
}
