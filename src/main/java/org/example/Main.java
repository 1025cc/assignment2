package org.example;

import cn.hutool.core.date.StopWatch;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class Main {
    public static List<Polygon> polygons;
    public static List<Polygon> halfPolygons;
    /**
     * In order to imporve the code reuse,
     * let the following data about the MBR of D(Calculated by task1.1) be static
     */
    //the minimum longitude
    public static double lon1 = 113.9310037;
    //the maximum longitude
    public static double lon2 = 114.3763554;
    //the minimum latitude
    public static double lat1 = 22.1973011;
    //the maximum latitude
    public static double lat2 = 22.5069962;
    public static StopWatch stopWatch = new StopWatch();
    static {
        String fileName = "Buildings.xlsx";
        polygons = readPolygonsFromExcel(fileName);
        int halfSize = polygons.size() / 2;
        halfPolygons = new ArrayList<>(polygons.subList(0, halfSize));
    }
    @Test
    public void task13() {
        int[] maxChildrenValues = {8, 32};
        int[] bucketSizeValues = {64, 256};
        System.out.println("For half of D");
        for (int maxChildren : maxChildrenValues) {
            for (int bucketSize : bucketSizeValues) {
                RTree rTree = buildRTree(maxChildren, bucketSize, halfPolygons);
                assertNotNull(rTree);

                System.out.printf("maxChildren: %d, bucketSize: %d, treeHeight: %d, leafNodes: %d, nonLeafNodes: %d%n",
                        maxChildren, bucketSize, rTree.getTreeHeight(), rTree.getNumberOfLeafNodes(), rTree.getNumberOfNonLeafNodes());
            }
        }
        System.out.println("For entire D");
        for (int maxChildren : maxChildrenValues) {
            for (int bucketSize : bucketSizeValues) {
                RTree rTree = buildRTree(maxChildren, bucketSize, polygons);
                assertNotNull(rTree);

                System.out.printf("maxChildren: %d, bucketSize: %d, treeHeight: %d, leafNodes: %d, nonLeafNodes: %d%n",
                        maxChildren, bucketSize, rTree.getTreeHeight(), rTree.getNumberOfLeafNodes(), rTree.getNumberOfNonLeafNodes());
            }
        }
    }

    /**
     * @param d        max number of subtrees for non-leaf node
     * @param n        max number of polygons of leaf node
     * @param polygons
     * @return
     */
    RTree buildRTree(int d, int n, List<Polygon> polygons) {
        RTree rTree = new RTree(d, n);
        for (Polygon polygon : polygons) {
            rTree.insert(polygon);
        }
        return rTree;
    }

    private static List<Polygon> readPolygonsFromExcel(String fileName) {
        List<Polygon> polygons = new ArrayList<>();
        File file = new File(fileName);
        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                Polygon polygon = new Polygon();
                Cell cell1 = row.getCell(1);
                cell1.setCellType(CellType.STRING);
                polygon.setId(cell1.getStringCellValue());
                Cell cell2 = row.getCell(2);
                if (cell2 == null) {
                    polygon.setName(null);
                } else {
                    cell2.setCellType(CellType.STRING);
                    polygon.setName(cell2.getStringCellValue());
                }
                Cell cell3 = row.getCell(2);
                if (cell3 == null) {
                    polygon.setType(null);
                } else {
                    cell3.setCellType(CellType.STRING);
                    polygon.setType(cell2.getStringCellValue());
                }
                String geometry = row.getCell(4).getStringCellValue();
                String[] pointsStr = geometry.substring(10, geometry.length() - 2).split(", ");
                double minLon = Double.POSITIVE_INFINITY;
                double minLat = Double.POSITIVE_INFINITY;
                double maxLon = Double.NEGATIVE_INFINITY;
                double maxLat = Double.NEGATIVE_INFINITY;
                for (String pointStr : pointsStr) {
                    String[] coordsStr = pointStr.split(" ");
                    double lon = Double.parseDouble(coordsStr[0]);
                    double lat = Double.parseDouble(coordsStr[1]);
                    Point point = new Point(lon, lat);
                    polygon.addPoint(point);
                    //calculate mbr for each polygon
                    minLon = Math.min(minLon, lon);
                    maxLon = Math.max(maxLon, lon);
                    minLat = Math.min(minLat, lat);
                    maxLat = Math.max(maxLat, lat);
                }
                Point leftBottom = new Point(minLon, minLat);
                Point rightTop = new Point(maxLon, maxLat);
                MBR mbr = new MBR(leftBottom, rightTop);
                polygon.setMbr(mbr);
                polygons.add(polygon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return polygons;
    }

    /**
     * Use 30 randomly generated window queries of different sizes at different locations
     * For each query, run the algorithms 5 times and record the min/max/avg excution time
     */
    @Test
    public void task23(){
        RTree rTree = buildRTree(8, 256, polygons);
        for(int i = 0;i<30;i++){
            System.out.println();
            MBR window = generateQueryWindow();
            System.out.printf("Query %d: %s%n",i,window.toString());
            long minTimeExhaustive = Long.MAX_VALUE;
            long maxTimeExhaustive = Long.MIN_VALUE;
            long totalTimeExhaustive = 0;

            long minTimeRTree = Long.MAX_VALUE;
            long maxTimeRTree = Long.MIN_VALUE;
            long totalTimeRTree = 0;

            int res1 = 0;
            QueryResult res2 = null;
            System.out.println("-------------------Start exhaustiveWindowQuery-------------------");
            for (int j = 0; j < 5; j++) {
                stopWatch.start();
                res1 = exhaustiveWindowQuery(window.getLowerLeft().getLon(),window.getUpperRight().getLon(),window.getLowerLeft().getLat(),window.getUpperRight().getLat());
                stopWatch.stop();
                minTimeExhaustive = Math.min(minTimeExhaustive, stopWatch.getLastTaskTimeMillis());
                maxTimeExhaustive = Math.max(maxTimeExhaustive, stopWatch.getLastTaskTimeMillis());
                totalTimeExhaustive += stopWatch.getLastTaskTimeMillis();

            }
            System.out.printf("The number of objects inside Q is %d%n",res1);
            System.out.printf("The execution time is Min time: %d ms, Max time: %d ms, Avg time: %d ms%n", minTimeExhaustive, maxTimeExhaustive, totalTimeExhaustive / 5);
            System.out.printf("The number of polygons in D that have been checked is %d%n",polygons.size());

            System.out.println("-------------------Start rTreeWindowQuery-------------------");
            for(int j = 0;j<5;j++){
                stopWatch.start();
                res2 = rTree.windowQuery(window);
                stopWatch.stop();
                minTimeRTree = Math.min(minTimeRTree, stopWatch.getLastTaskTimeMillis());
                maxTimeRTree = Math.max(maxTimeRTree, stopWatch.getLastTaskTimeMillis());
                totalTimeRTree += stopWatch.getLastTaskTimeMillis();
            }
            System.out.printf("The number of objects inside Q is %d%n",res2.getPolygons().size());
            System.out.printf("The execution time is Min time: %d ms, Max time: %d ms, Avg time: %d ms%n", minTimeRTree, minTimeRTree, totalTimeRTree / 5);
            System.out.printf("The number of polygons in D that have been checked is %d%n",res2.getCheckedCount());
        }
    }

    public MBR generateQueryWindow(){
        double x1add = Math.random() * (lon2-lon1);
        double x2add = Math.random() * (lon2-lon1);
        double y1add = Math.random() * (lat2-lat1);
        double y2add = Math.random() * (lat2-lat1);
        //Avoid generating too many query windows that span the center point
        double r = Math.random();
        if(r<0.5){
            x1add *= 0.5;
            x2add *= 0.5;
        }else{
            y1add *=0.5;
            y2add*= 0.5;
        }
        double x1 = lon1 + x1add;
        double x2 = lon1 + x2add;
        double y1 = lat1 + y1add;
        double y2 = lat1 + y2add;
        return new MBR(new Point(Math.min(x1,x2),Math.min(y1,y2)),new Point(Math.max(x1,x2),Math.max(x1,x2)));

    }
    public int exhaustiveWindowQuery(double xLow, double xHigh, double yLow, double yHigh){
        int res = 0;
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
