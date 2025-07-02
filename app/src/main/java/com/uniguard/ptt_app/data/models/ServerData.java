package com.uniguard.ptt_app.data.models;

public class ServerData {
    private String host;
    private int id;
    private String name;
    private String password;
    private int port;
    private String username;

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }
}
