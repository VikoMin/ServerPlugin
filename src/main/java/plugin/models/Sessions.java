package plugin.models;

import mindustry.gen.Player;

import java.util.HashMap;

public class Sessions {
    HashMap<String, Session> sessions = new HashMap<>();

    Sessions(){}

    void createSession(Player player){
        sessions.put(player.uuid(), new Session(player));
    }

    Session getSession(Player player){
        if (!sessions.containsKey(player.uuid()))
            createSession(player);
        return sessions.get(player.uuid());
    }

    void closeSession(Player player){
        sessions.get(player.uuid()).commit();
        sessions.remove(player.uuid());
    }
}
