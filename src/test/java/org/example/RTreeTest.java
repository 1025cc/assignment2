package org.example;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RTreeTest {


    @Test
    public void testBuildRTreeWithDifferentParameters() {
        List<Polygon> polygons = new ArrayList<>();
        polygons.add(new Polygon("1",Arrays.asList(new Point(0, 0), new Point(2, 2))));
        polygons.add(new Polygon("2",Arrays.asList(new Point(1, 1), new Point(3, 3))));
        polygons.add(new Polygon("3",Arrays.asList(new Point(4, 1), new Point(6, 3))));
        polygons.add(new Polygon("4",Arrays.asList(new Point(7, 0), new Point(9, 2))));
        polygons.add(new Polygon("5",Arrays.asList(new Point(8, 3), new Point(10, 5))));
        polygons.add(new Polygon("6",Arrays.asList(new Point(0, 4), new Point(2, 6))));
        polygons.add(new Polygon("7",Arrays.asList(new Point(1, 5), new Point(3, 7))));
        polygons.add(new Polygon("8",Arrays.asList(new Point(4, 5), new Point(6, 7))));
        polygons.add(new Polygon("9",Arrays.asList(new Point(7, 4), new Point(9, 6))));
        polygons.add(new Polygon("10",Arrays.asList(new Point(1, 3), new Point(2, 5))));
        RTree rTree = buildRTree(3, 3, polygons);
        MBR queryMBR = new MBR(new Point(1, 1), new Point(8, 6));
        QueryResult resultSet = rTree.windowQuery(queryMBR);
        int test = exhaustiveWindowQuery(queryMBR.getLowerLeft().getLon(),queryMBR.getUpperRight().getLon(),queryMBR.getLowerLeft().getLat(),queryMBR.getUpperRight().getLat(),polygons);
        // Print the result set
        System.out.println("Window query result:");
        for (Polygon polygon : resultSet.getPolygons()) {
            System.out.println(polygon.getMbr());
        }
    }
    RTree buildRTree(int d, int n, List<Polygon> polygons) {
        RTree rTree = new RTree(d, n);
        for (Polygon polygon : polygons) {
            rTree.insert(polygon);
        }
        return rTree;
    }
    public int exhaustiveWindowQuery(double xLow, double xHigh, double yLow, double yHigh,List<Polygon> polygons){
        int res = 0;
        System.out.printf("The number of objects searched is %d%n",polygons.size());
        for (int i = 0;i<polygons.size();i++) {
            MBR mbr = polygons.get(i).getMbr();
            double minLon = mbr.getLowerLeft().getLon();
            double minLat = mbr.getLowerLeft().getLat();
            double maxLon = mbr.getUpperRight().getLon();
            double maxLat = mbr.getUpperRight().getLat();
            if(minLon >xLow && maxLon < xHigh && minLat > yLow && maxLat < yHigh){
                res++;
            }
        }
        return res;
    }
}
