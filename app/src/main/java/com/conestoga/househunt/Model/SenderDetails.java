package com.conestoga.househunt.Model;

public class SenderDetails {

    String sendto,senderimage,sendid;
    String lastmessage;

    public SenderDetails() {
    }

    public SenderDetails(String sendto, String senderimage, String sendid, String lastmessage) {
        this.sendto = sendto;
        this.senderimage = senderimage;
        this.sendid = sendid;
        this.lastmessage = lastmessage;
    }

    public String getSendto() {
        return sendto;
    }

    public void setSendto(String sendto) {
        this.sendto = sendto;
    }

    public String getSenderimage() {
        return senderimage;
    }

    public void setSenderimage(String senderimage) {
        this.senderimage = senderimage;
    }

    public String getSendid() {
        return sendid;
    }

    public void setSendid(String sendid) {
        this.sendid = sendid;
    }

    public String getLastmessage() {
        return lastmessage;
    }

    public void setLastmessage(String lastmessage) {
        this.lastmessage = lastmessage;
    }
}
