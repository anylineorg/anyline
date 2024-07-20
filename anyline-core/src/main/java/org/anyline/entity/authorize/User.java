package org.anyline.entity.authorize;

public class User {
    private String name;
    private String password;
    private String host;
    public User(){

    }
    public User(String name){
        this.name = name;
    }
    public User(String name, String password){
        this.name = name;
        this.password = password;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
