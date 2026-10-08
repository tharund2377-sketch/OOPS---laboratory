package model;

public class Admin extends User {

    public Admin(int id, String name, String email, String password, boolean blocked) {
        super(id, name, email, password, "ADMIN", blocked);
    }

    public Admin(int id, String name, String email, String password) {
        super(id, name, email, password, "ADMIN", false);
    }
}