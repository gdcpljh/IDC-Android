package com.id.connect.diaspora.model;

public class StoryModel {
    public String story_key;
    public String diaspora_key;
    public String status;
    public String created_at;
    public String img_url;

    public StoryModel(String story_key, String diaspora_key, String status, String created_at, String img_url) {
        this.story_key = story_key;
        this.diaspora_key = diaspora_key;
        this.status = status;
        this.created_at = created_at;
        this.img_url = img_url;
    }

    public StoryModel() {
    }

    public String getStory_key() {
        return story_key;
    }

    public void setStory_key(String story_key) {
        this.story_key = story_key;
    }

    public String getDiaspora_key() {
        return diaspora_key;
    }

    public void setDiaspora_key(String diaspora_key) {
        this.diaspora_key = diaspora_key;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}
