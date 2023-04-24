package org.example;

import java.util.ArrayList;

public class RTreeNode {
    private MBR mbr;
    private ArrayList<RTreeNode> children;
    private ArrayList<Polygon> polygons;

    public RTreeNode(MBR mbr) {
        this.mbr = mbr;
        this.children = new ArrayList<>();
        this.polygons = new ArrayList<>();
    }
    private RTreeNode parent;

    // Add a getter and setter for the parent reference
    public RTreeNode getParent() {
        return parent;
    }

    public void setParent(RTreeNode parent) {
        this.parent = parent;
    }


    public ArrayList<Polygon> getPolygons() {
        return polygons;
    }

    // Getters and setters
    public void setPolygons(ArrayList<Polygon> polygons) {
        this.polygons = polygons;
    }

    public ArrayList<RTreeNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<RTreeNode> children) {
        this.children = children;
    }

    public MBR getMbr() {
        return mbr;
    }

    public void setMbr(MBR mbr) {
        this.mbr = mbr;
    }

    public boolean isLeaf(){
        return children.isEmpty();
    }


}

