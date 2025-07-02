package com.uniguard.ptt_app.data.models;

import com.google.gson.annotations.SerializedName;

public class ServerApi {
    private ServerData server;
    @SerializedName("server_id")
    private String serverId;
    @SerializedName("user_id")
    private String userId;

    public String getServerId() {
        return this.serverId;
    }

    public String getUserId() {
        return this.userId;
    }

    public ServerData getServer() {
        return this.server;
    }
}
