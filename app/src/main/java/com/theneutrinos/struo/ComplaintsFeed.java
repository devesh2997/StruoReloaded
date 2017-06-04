package com.theneutrinos.struo;

/**
 * Created by Aman Deep Singh on 12-01-2017.
 */

public class ComplaintsFeed
{
    private String Category, Subject, Description, Image, Name, Time, Userid;

    public ComplaintsFeed()
    {

    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public ComplaintsFeed(String category, String subject, String description, String image, String name, String time, String userid) {
        Category = category;
        Subject = subject;
        Description = description;
        Image = image;
        Name = name;
        Time = time;
        Userid = userid;
    }

    public String getUserid()
    {
        return Userid;
    }

    public void setUserid(String userid)
    {
        Userid = userid;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }
}
