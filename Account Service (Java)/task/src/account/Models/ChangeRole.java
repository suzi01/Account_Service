package account.Models;

import java.util.List;

public class ChangeRole {
    private String user;
    private String role;
    private String operation;


    public ChangeRole(String user, String role, String operation) {
        this.user = user;
        this.role = role;
        this.operation = operation;
    }

    public ChangeRole() {
    }

    public String getUser() {
        return user;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOperation() {
        return operation;
    }


}



