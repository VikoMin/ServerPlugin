package plugin.models;

import mindustry.gen.Player;

import java.util.HashMap;

public class Sessions {
    HashMap<String, Session> sessions = new HashMap<>();

    public Sessions() {
    }

    public void createSession(Player player) {
        sessions.put(player.uuid(), new Session(player));
    }

    public Session getSession(Player player) {
        if (!sessions.containsKey(player.uuid()))
            createSession(player);
        return sessions.get(player.uuid());
    }

    public void closeSession(Player player) {
        if (sessions.containsKey(player.uuid())) {
            sessions.get(player.uuid()).commit();
            sessions.remove(player.uuid());
        }
    }
}
