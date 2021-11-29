package state;

public class State {
    private String userName;
    private String activeDatabase;
    private Boolean userLoggedIn;

    public State() {

    }

    public String getUserName() {
        return userName;
    }

    public String getActiveDatabase() {
        return activeDatabase;
    }

    public Boolean getUserLoggedIn() {
        return userLoggedIn;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setActiveDatabase(String activeDatabase) {
        this.activeDatabase = activeDatabase;
    }

    public void setUserLoggedIn(Boolean userLoggedIn) {
        this.userLoggedIn = userLoggedIn;
    }
}
