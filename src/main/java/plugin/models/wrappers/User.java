package plugin.models.wrappers;

public class User {
    private final plugin.models.collections.User collection;

    public User(int id){
        collection = new plugin.models.collections.User(id);
    }
}
