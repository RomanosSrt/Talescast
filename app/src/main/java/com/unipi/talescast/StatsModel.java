package com.unipi.talescast;

import java.util.Map;

public class StatsModel {
    private String lastPlayed;
    private Map<String, TaleStat> listenedTales;
    private Map<String, TaleStat> readTales;

    public StatsModel() {
    }

    public String getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(String lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public Map<String, TaleStat> getListenedTales() {
        return listenedTales;
    }

    public void setListenedTales(Map<String, TaleStat> listenedTales) {
        this.listenedTales = listenedTales;
    }

    public Map<String, TaleStat> getReadTales() {
        return readTales;
    }

    public void setReadTales(Map<String, TaleStat> readTales) {
        this.readTales = readTales;
    }
}



