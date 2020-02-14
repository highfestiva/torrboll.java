package com.pixeldoctrine.torrboll.entity;

import javax.mail.Message;
import java.util.Calendar;
import java.util.Date;

public class BackupResult {

    private Date date;
    private String service;
    private String client;
    private String system;
    private String job;
    private int percent;
    private final Message msg;

    public BackupResult(Date date, String service, String client, String system, String job, int percent, Message msg) {
        this.date = date;
        this.service = service;
        this.client = client;
        this.system = system;
        this.job = job;
        this.percent = percent;
        this.msg = msg;
    }

    public Date getDate() {
        return date;
    }

    public Date getYesterdaysDate() {
        Date now = getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.HOUR, -21); // earlier than 21:00 means yesterday
        return cal.getTime();
    }

    public String getService() {
        return service;
    }

    public String getClient() {
        return client;
    }

    public String getSystem() {
        return system;
    }

    public String getJob() {
        return job;
    }

    public int getPercent() {
        return percent;
    }

    public Message getMsg() {
        return msg;
    }
}
