package org.example;

import java.io.Serializable;

class Point implements Serializable {
    private static final long serialVersionUID = 5019436592726456470L;
    private double lon;
    private double lat;

    public Point(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return  ""+ lon+ " "+lat+"";
    }
}