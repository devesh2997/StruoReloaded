package com.theneutrinos.struo;

/**
 * Created by Aman Deep Singh on 16-01-2017.
 */

public class AdFeed
{
    private String Item , Name , Image, Mobileno, Desc, Time, Userid;

    public AdFeed()
    {

    }

    public AdFeed(String item, String name, String image, String mobileno, String desc, String time, String userid) {
        Item = item;
        Name = name;
        Image = image;
        Mobileno = mobileno;
        Desc = desc;
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

    public String getItem() {
        return Item;
    }

    public void setItem(String item) {
        Item = item;
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

    public String getMobileno() {
        return Mobileno;
    }

    public void setMobileno(String mobileno) {
        Mobileno = mobileno;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }
}
