package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
    public static List<Polygon> polygons;
    public static List<Polygon> halfPolygons;
    static {
        String fileName = "Buildings.xlsx";
        polygons = readPolygonsFromExcel(fileName);
        int halfSize = polygons.size() / 2;
        halfPolygons = new ArrayList<>(polygons.subList(0, halfSize));
    }
    @Test
    public void task13() {

        RTree rTree1 = buildRTree(8, 256, halfPolygons);
        System.out.printf("RTree(d = %d, n = %d, polygons = half of D)%n", 8, 256);
        System.out.println(rTree1.output());
        /*RTree rTree2 = buildRTree(32, 64, new ArrayList<>(halfPolygons));
        System.out.printf("RTree(d = %d, n = %d, polygons = half of D)%n", 32, 64);
        System.out.println(rTree2.output());
        RTree rTree3 = buildRTree(8, 256, new ArrayList<>(halfPolygons));
        System.out.printf("RTree(d = %d, n = %d, polygons = half of D)%n", 8, 256);
        System.out.println(rTree3.output());
        RTree rTree4 = buildRTree(32, 256, new ArrayList<>(halfPolygons));
        System.out.printf("RTree(d = %d, n = %d, polygons = half of D)%n", 32, 256);
        System.out.println(rTree4.output());
        RTree rTree5 = buildRTree(8, 64, new ArrayList<>(polygons));
        System.out.printf("RTree(d = %d, n = %d, polygons = entire D)%n", 8, 64);
        System.out.println(rTree5.output());
        RTree rTree6 = buildRTree(32, 64, new ArrayList<>(polygons));
        System.out.printf("RTree(d = %d, n = %d, polygons = entire D)%n", 32, 64);
        System.out.println(rTree6.output());
        RTree rTree7 = buildRTree(8, 256, new ArrayList<>(polygons));
        System.out.printf("RTree(d = %d, n = %d, polygons = entire D)%n", 8, 256);
        System.out.println(rTree7.output());
        RTree rTree8 = buildRTree(32, 256, new ArrayList<>(polygons));
        System.out.printf("RTree(d = %d, n = %d, polygons = entire D)%n", 32, 256);
        System.out.println(rTree8.output());*/

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


}
