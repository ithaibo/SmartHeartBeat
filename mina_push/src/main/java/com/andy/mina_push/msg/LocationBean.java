package com.andy.mina_push.msg;

import java.io.Serializable;

/**
 * Created by Andy on 2017/5/4.
 */

public class LocationBean implements Serializable {
    /**经度*/
    private double longitude;
    /**纬度*/
    private double latitude;

    public LocationBean(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public LocationBean() {
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return "LocationBean{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}
