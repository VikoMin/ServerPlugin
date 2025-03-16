package plugin.models.collections;

import plugin.etc.Ranks;

public class User {
    public int userID;
    public Ranks.Rank rank = Ranks.Rank.None;

    public User(){}
    public User(int id){
        userID = id;
    }
}
