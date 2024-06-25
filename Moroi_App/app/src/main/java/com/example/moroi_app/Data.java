package com.example.moroi_app;


public class Data {
    private static Data instance;

    private String player1, player2, manager;

    private Data() {}

    public static synchronized Data getInstance() {
        if (instance == null) {
            instance = new Data();
        }
        return instance;
    }

}
