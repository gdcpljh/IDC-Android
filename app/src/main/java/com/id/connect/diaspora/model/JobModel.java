package com.id.connect.diaspora.model;

public class JobModel {
    public String job_key;
    public String company_name;
    public String description;
    public String job_id;
    public String location;
    public String position;
    public String published_by;

    public JobModel(String job_key, String company_name, String description, String job_id, String location, String position, String published_by) {
        this.job_key = job_key;
        this.company_name = company_name;
        this.description = description;
        this.job_id = job_id;
        this.location = location;
        this.position = position;
        this.published_by = published_by;
    }

    public JobModel() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJob_key() {
        return job_key;
    }

    public void setJob_key(String job_key) {
        this.job_key = job_key;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPublished_by() {
        return published_by;
    }

    public void setPublished_by(String published_by) {
        this.published_by = published_by;
    }
}
