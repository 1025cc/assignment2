package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Polygon implements Serializable {
    private static final long serialVersionUID = 49728144305187204L;
    private String osm_id;
    private String name;
    private String type;
    private List<Point> geometry;

    private MBR mbr;
    public Polygon(String osm_id, String name, String type, List<Point> geometry) {
        this.osm_id = osm_id;
        this.name = name;
        this.type = type;
        this.geometry = geometry;

        computeMBR();
    }
    public Polygon( String name,List<Point> geometry) {
        this.name = name;
        this.geometry = geometry;

        computeMBR();
    }

    private void computeMBR() {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Point point : geometry) {
            minX = Math.min(minX, point.getLon());
            minY = Math.min(minY, point.getLat());
            maxX = Math.max(maxX, point.getLon());
            maxY = Math.max(maxY, point.getLat());
        }
        Point leftBottom = new Point(minX,minY);
        Point rightTop = new Point(maxX,maxY);
        this.mbr = new MBR(leftBottom,rightTop);
    }

    public Polygon() {
        geometry = new ArrayList<>();
    }

    public String getId() {
        return osm_id;
    }

    public void setId(String id) {
        this.osm_id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Point> getGeometry() {
        return geometry;
    }

    public void setGeometry(List<Point> geometry){
        this.geometry = geometry;
    }

    public void addPoint(Point point) {
        geometry.add(point);
    }

    public MBR getMbr() {
        return mbr;
    }

    public void setMbr(MBR mbr) {
        this.mbr = mbr;
    }
}