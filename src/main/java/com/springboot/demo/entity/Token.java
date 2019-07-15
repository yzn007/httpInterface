package com.springboot.demo.entity;

import org.antlr.v4.runtime.misc.Interval;

import javax.persistence.Transient;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yzn00 on 2019/7/11.
 */
public class Token {

    public Date getDataTm() {
        return dataTm;
    }

    public void setDataTm(Date dateTm) {
        this.dataTm = dateTm;
    }

    @Transient
    private Date dateExp;

    public Date getDateExp(){
        return this.dateExp;
    }

    public Date SetDateExp(){
        if(dataTm != null && expiresIn>0){
            Calendar cl = Calendar.getInstance();
            cl.setTime(dataTm);
            cl.add(Calendar.SECOND,expiresIn);
            return cl.getTime();
        }
        return getDataTm();
    }

    private Date dataTm;

    private String accessToken;

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    private int expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
