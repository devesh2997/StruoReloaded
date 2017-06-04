package com.theneutrinos.struo;

/**
 * Created by Aman Deep Singh on 22-01-2017.
 */

public class EventsFeed
{
    private String Date, Description, EndTime, Image, Mobileno, Name, StartTime, Title, Userid;

    public EventsFeed()
    {

    }

    public EventsFeed(String date, String description, String endTime, String image, String mobileno, String name, String startTime, String title, String userid) {
        Date = date;
        Description = description;
        EndTime = endTime;
        Image = image;
        Mobileno = mobileno;
        Name = name;
        StartTime = startTime;
        Title = title;
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

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getMobileno() {
        return Mobileno;
    }

    public void setMobileno(String mobileno) {
        Mobileno = mobileno;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
}
