package plugin.configs;

import arc.util.Log;
import mindustry.Vars;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ServersConfig {
    public static JSONObject makeServersConfig() throws IOException, ParseException {
            File config = new File(Vars.dataDirectory.absolutePath() + "/" + "serversConfig.json");
            if (!config.exists()) {
                FileWriter writer = new FileWriter(Vars.dataDirectory.absolutePath() + "/" + "serversConfig.json");
                writer.write(
                        """
                                {
                                \t"servers": [
                                \t\t{ "servername":"testserver", "ip":"0.0.0.0", "port":6969 }
                                \t]
                                }"""
                        );
                writer.close();
            }
        return (JSONObject) new JSONParser().parse(new FileReader(Vars.dataDirectory.absolutePath() + "/serversConfig.json"));
        }
    public static void resetServersConfig() throws IOException, ParseException {
        FileWriter writer = new FileWriter(Vars.dataDirectory.absolutePath() + "/" + "serversConfig.json");
        writer.flush();
        writer.write(
                """
                        {
                        \t"servers": [
                        \t\t{ "servername":"testserver", "ip":"0.0.0.0", "port":6969 }
                        \t]
                        }"""
        );writer.close();
        Log.warn("Config resetted!");
    }

}