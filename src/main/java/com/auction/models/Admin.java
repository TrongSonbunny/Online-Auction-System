package com.auction.models;

public class Admin extends User {
    private String roleLevel;

    public Admin(String username, String password, String roleLevel) {
        super(username, password);
        this.roleLevel = roleLevel;
    }

    public String getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(String roleLevel) {
        this.roleLevel = roleLevel;
    }

    @Override
    public void printInfo() {
        System.out.println("[Admin] " + getUsername() + " - Cấp bậc: " + roleLevel);
    }
}
