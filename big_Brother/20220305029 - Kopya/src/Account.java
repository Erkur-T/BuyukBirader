// Account.java
public abstract class Account {
    private final int id;
    private final String username;
    private final String password;
    private boolean active;

    public Account(int id, String username, String password) {
        this.id       = id;
        this.username = username;
        this.password = password;
        this.active   = false;
    }

    public int getId()               { return id; }
    public String getUsername()      { return username; }
    public boolean checkPassword(String pw) {
        return this.password.equals(pw);
    }
    public boolean isActive()        { return active; }
    public void setActive(boolean v) { this.active = v; }
}

