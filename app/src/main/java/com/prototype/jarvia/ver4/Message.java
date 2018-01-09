package com.prototype.jarvia.ver4;

/**
 * Created by Joon.Y.K on 2017-03-19.
 */

public class Message {
    //Why need ID?
    //protected int id;
    protected String message;
    protected String senderName;

    public Message(String message, String senderName) {
        //this.id = id;
        this.message = message;
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

  /*  public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }*/

}