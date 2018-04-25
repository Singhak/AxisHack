package com.developer.tms.themeansquare.Utility;

/**
 * Created by ANIL on 23-Apr-18.
 */

public class AuthUser {
    private String user_name;
    private String uuid;
    private int enroll_count;
    private String phrase;

    public AuthUser(String user_name, String uuid, String phrase, int enroll_count) {
        this.user_name = user_name;
        this.uuid = uuid;
        this.enroll_count = enroll_count;
        this.phrase = phrase;
    }

    public int getEnroll_count() {
        return enroll_count;
    }

    public void setEnroll_count(int enroll_count) {
        this.enroll_count = enroll_count;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    @Override
    public String toString() {
        return user_name + " "+uuid+" "+enroll_count+" "+phrase;
    }
}
