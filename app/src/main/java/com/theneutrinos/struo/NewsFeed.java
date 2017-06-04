package com.theneutrinos.struo;

/**
 * Created by Aman Deep Singh on 10-01-2017.
 */

public class NewsFeed
{
    private String Title, Desc, Time, Image, Name, Userid;

    public NewsFeed()
    {

    }

    public NewsFeed(String image, String time, String name, String title, String desc, String userid) {
        Image = image;
        Name = name;
        Time = time;
        Title = title;
        Desc = desc;
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

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }
}
