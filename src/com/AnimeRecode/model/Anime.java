/*
 * 动漫参数
 * by shengjing19
 * create 2025-7-1
 * last modify
 */
package com.AnimeRecode.model;

import java.util.Date;

public class Anime {
    private int id;
    private String title;
    private String coverImage;
    private int totalEpisodes;
    private Date finishDate;
    private boolean isFavorite;
    private boolean isWatching;
    private String description;

    public Anime() {}

    public Anime(String title, String coverImage, int totalEpisodes, Date finishDate, boolean isFavorite, boolean isWatching, String description) {
        this.title = title;
        this.coverImage = coverImage;
        this.totalEpisodes = totalEpisodes;
        this.finishDate = finishDate;
        this.isFavorite = isFavorite;
        this.isWatching = isWatching;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public int getTotalEpisodes() { return totalEpisodes; }
    public void setTotalEpisodes(int totalEpisodes) { this.totalEpisodes = totalEpisodes; }
    public Date getFinishDate() { return finishDate; }
    public void setFinishDate(Date finishDate) { this.finishDate = finishDate; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public boolean isWatching() { return isWatching; }
    public void setWatching(boolean watching) { isWatching = watching; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}