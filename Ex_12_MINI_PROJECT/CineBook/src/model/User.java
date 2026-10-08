package model;

public abstract class User {

    private final int id;
    private final String name;
    private final String email;
    private final String password;
    private final String role;
    private boolean blocked;

    public User(int id, String name, String email, String password, String role, boolean blocked) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.blocked = blocked;
    }

    public User(int id, String name, String email, String password, String role) {
        this(id, name, email, password, role, false);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}