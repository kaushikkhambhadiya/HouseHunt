package com.conestoga.househunt.Model;

import java.io.Serializable;

public class Property implements Serializable {

    private String userid;
    private String type;
    private String location;
    private String available;
    private int price;
    private String imageId;
    private String imageFileName;
    private String offerdateandtime;
    private String dateofpost;
    private String uploader_name,uploader_email;
    private String uploader_profile_pic;

    public Property() {
    }

    public Property(String userid, String type, String location, String available, int price, String imageId, String imageFileName, String offerdateandtime, String dateofpost, String uploader_name, String uploader_email, String uploader_profile_pic) {
        this.userid = userid;
        this.type = type;
        this.location = location;
        this.available = available;
        this.price = price;
        this.imageId = imageId;
        this.imageFileName = imageFileName;
        this.offerdateandtime = offerdateandtime;
        this.dateofpost = dateofpost;
        this.uploader_name = uploader_name;
        this.uploader_email = uploader_email;
        this.uploader_profile_pic = uploader_profile_pic;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getOfferdateandtime() {
        return offerdateandtime;
    }

    public void setOfferdateandtime(String offerdateandtime) {
        this.offerdateandtime = offerdateandtime;
    }

    public String getDateofpost() {
        return dateofpost;
    }

    public void setDateofpost(String dateofpost) {
        this.dateofpost = dateofpost;
    }

    public String getUploader_name() {
        return uploader_name;
    }

    public void setUploader_name(String uploader_name) {
        this.uploader_name = uploader_name;
    }

    public String getUploader_email() {
        return uploader_email;
    }

    public void setUploader_email(String uploader_email) {
        this.uploader_email = uploader_email;
    }

    public String getUploader_profile_pic() {
        return uploader_profile_pic;
    }

    public void setUploader_profile_pic(String uploader_profile_pic) {
        this.uploader_profile_pic = uploader_profile_pic;
    }
}
