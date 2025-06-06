package de.tomalbrc.balloons.config;

public class MongoConfig {
    public boolean enabled = false;
    public String host = "127.0.0.1";
    public int port = 27017;
    public String collection = "balloons";
    public String username = "user";
    public String password = "pass";
    public String database = "game";
    public String authSource = "admin";
    public boolean useSSL = false;
}