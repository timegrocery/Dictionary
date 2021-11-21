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

    public BiMap <String, String> GetBiMap()
    {
        return  dictionary;
    }

    public int WriteToFile(String line)
    {
        try
        {
            FileWriter fw = new FileWriter(dataPath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(line);
            bw.newLine();
            bw.close();
            fw.close();
            return 1; // write successfully
        } catch (IOException e){
            e.printStackTrace();
            return 0; // write failed
        }
    }

    public int WriteToFile(BiMap<String, String> dictionary)
    {
        try
        {
            FileWriter fw = new FileWriter("./test_dic.txt", false);
            BufferedWriter bw = new BufferedWriter(fw);
            for (var entry : dictionary.entrySet())
            {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(entry.getKey());
                stringBuilder.append(";");
                stringBuilder.append(entry.getValue());
                bw.write(stringBuilder.toString());
                bw.newLine();
            }
            bw.close();
            fw.close();
            return  1;
        } catch (IOException ioException){
            ioException.printStackTrace();
            return 0;
        }
    }

    public int DeleteFromFile(String word) {
        try {
            FileInputStream file = new FileInputStream(dataPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(file));
            FileWriter fw = new FileWriter(tempDataPath, false);
            BufferedWriter bw = new BufferedWriter(fw);
            String line;
            while((line = br.readLine()) != null)
            {
                String[] tempContainer = line.split(";");
                if (!tempContainer[0].equals(word)) // deletes specified Eng words only
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
            return 0; // failed
        } catch (IOException ioException){
            ioException.printStackTrace();
            return 0; // failed
        }

        try
        {
            //Delete file
            Files.deleteIfExists(Paths.get(dataPath));
            //Rename file
            Files.move(Paths.get(tempDataPath),Paths.get(dataPath));
        } catch (IOException e){
            e.printStackTrace();
            return -1; // failed to rename file
        }
        return 1; // success
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
                        dictionary.put(tempContainer[0], tempContainer[1]);
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
            return "Cannot find <" + word + "> in our dictionary.";
        }
    }

    public String HandleInput(String input) {
        // pre-processing
        input = NormalizeSpaces(input);
        input = input.toLowerCase();
        int semicolonCount = countChar(input, ';');
        // command check
        if (semicolonCount > 0) {
            // probably a command if word contains ";"
            String[] separate = input.split(";"); // splits input into parts
            // normalizes space every part
            for (int i = 0; i< separate.length; i++) {
                separate[i] = NormalizeSpaces(separate[i]);
            }
            if (semicolonCount == 2) {
                // probably the add command
                // separate[0]: ADD
                // separate[1]: Eng word
                // separate[2]: Viet word
                if (separate[0].equals("add")) {
                    if (separate.length != 3) {
                        // a valid add command should be split into 3 part
                        return "Invalid add command.\nEg: ADD;apple;tao";
                    }
                    if (!separate[1].isEmpty()
                        && !separate[2].isEmpty()) {
                        // check if duplicated pair and add
                        if (dictionary.containsKey(separate[1])) {
                            // duplicated pair
                            return "Cannot add new pair as <" + separate[1] + "> has already existed in our dictionary.";
                        } else {
                            // not existed yet
                            dictionary.put(separate[1], separate[2]); // push new pair into bi-map
                            StringBuilder newLine = new StringBuilder();
                            newLine.append(separate[1]);
                            newLine.append(";");
                            newLine.append(separate[2]);
                            int returnCode = WriteToFile(newLine.toString());
                            if (returnCode == 1) {
                                return "Successfully added new pair into our database.";
                            } else {
                                return "Failed to add new pair into our database. Error code: " +  returnCode;
                            }
                        }
                    } else {
                        return "Invalid add command.\nEg: ADD;apple;tao";
                    }
                } else {
                    return "Unknown command";
                }
            } else if (semicolonCount == 1) {
                // probably the delete command
                // separate[0]: DEL
                // separate[1]: word
                if (separate[0].equals("del")) {
                    if (separate.length != 2) {
                        // a valid delete command should be split into 2 part
                        return "Invalid delete command.\nEg: DEL;hammer";
                    }
                    if (!separate[1].isEmpty()) {
                        // check if existed and del
                        if (dictionary.containsKey(separate[1])) {
                            // exists
                            dictionary.remove(separate[1]);
                            int returnCode = DeleteFromFile(separate[1]);
                            if (returnCode == 1) {
                                return "Successfully deleted";
                            } else {
                                return "Failed to delete. Error code: " + returnCode;
                            }
                        } else {
                            return "Cannot find the specified word in our database.";
                        }
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

    public int countChar(String str, char c) {
        int count = 0;

        for(int i=0; i < str.length(); i++) {
            if(str.charAt(i) == c)
            count++;
        }

        return count;
    }
}
