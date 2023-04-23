package org.example;

import junit.framework.TestCase;

import java.util.List;
import java.util.Arrays;

public class RTreeTest extends TestCase {

    public void testInsert() {
        // Create an RTree with a maxEntries of 4 and a bucketSize of 3
        RTree rTree = new RTree(4, 3);

        // Create sample polygon geometries
        List<Point> geometry1 = Arrays.asList(new Point(0, 0), new Point(10, 0), new Point(10, 10), new Point(0, 10));
        List<Point> geometry2 = Arrays.asList(new Point(5, 5), new Point(15, 5), new Point(15, 15), new Point(5, 15));
        List<Point> geometry3 = Arrays.asList(new Point(10, 0), new Point(20, 0), new Point(20, 10), new Point(10, 10));
        List<Point> geometry4 = Arrays.asList(new Point(15, 5), new Point(25, 5), new Point(25, 15), new Point(15, 15));

        // Create sample polygons
        Polygon polygon1 = new Polygon("1", "Polygon1", "type1", geometry1);
        Polygon polygon2 = new Polygon("2", "Polygon2", "type2", geometry2);
        Polygon polygon3 = new Polygon("3", "Polygon3", "type3", geometry3);
        Polygon polygon4 = new Polygon("4", "Polygon4", "type4", geometry4);

        // Insert polygons into the RTree
        rTree.insert(polygon1);
        rTree.insert(polygon2);
        rTree.insert(polygon3);
        rTree.insert(polygon4);

        // Check that the root node has two children after the insertions
        assertEquals(2, rTree.getRoot().getChildren().size());


        // Check that the MBR of the root node correctly covers all inserted polygons
        MBR rootMBR = rTree.getRoot().getMbr();
        assertTrue(rootMBR.contains(polygon1.getMbr()));
        assertTrue(rootMBR.contains(polygon2.getMbr()));
        assertTrue(rootMBR.contains(polygon3.getMbr()));
        assertTrue(rootMBR.contains(polygon4.getMbr()));
    }
}