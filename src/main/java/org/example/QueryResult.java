package org.example;

import java.util.List;

public class QueryResult {
    private List<Polygon> polygons;
    private int checkedCount;

    public QueryResult(List<Polygon> polygons, int checkedCount) {
        this.polygons = polygons;
        this.checkedCount = checkedCount;
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }

    public int getCheckedCount() {
        return checkedCount;
    }
}

