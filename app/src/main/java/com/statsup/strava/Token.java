package com.statsup.strava;

public class Token {
    private String value;

    public Token(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Bearer " + value;
    }
}
