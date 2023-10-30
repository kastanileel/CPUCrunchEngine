package src.engine.configuration;

import java.io.*;

/**
 * Created by Daniel on 10/30/2023.
 *
 * Class Description:
 * Other classes resive a reference to this class to get the configuration of the engine.
 *
 * Configuration is set in the "engine.properties" file.
 */
public class Configurator {

    public static Configurator instance;
    private String filePath = "./src/engine/configuration/engine.properties";

    public static Configurator getInstance(){
        if(instance == null){
            instance = new Configurator();
        }
        return instance;
    }

    private Configurator(){}

    public String get(String key){

        // read key from file
        try {
            BufferedReader reader = new BufferedReader (new FileReader(filePath));

            String line = null;
            while((line = reader.readLine()) != null){
                if(line.startsWith(key)){
                    return line.substring(line.indexOf("=") + 1);
                }
            }

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return null;
    }

}
