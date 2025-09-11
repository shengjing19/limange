/*
 * 用户参数
 * by shengjing19
 * create 2025-7-15
 * last modify
 */
package com.AnimeRecode.model;

public class User {
    private int uid;
    private String username;
    private String password;

    // 构造方法、getter和setter
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public int getId() { return uid; }
    public void setId(int id) { this.uid = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}