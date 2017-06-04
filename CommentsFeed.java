package com.theneutrinos.struo;

/**
 * Created by Aman Deep Singh on 13-01-2017.
 */

public class CommentsFeed
{
    private String Name, Comment, Time, Userid;

    public CommentsFeed()
    {

    }

    public CommentsFeed(String name, String comment, String time, String userid) {
        Name = name;
        Comment = comment;
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

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }
}
