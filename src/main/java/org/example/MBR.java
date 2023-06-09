package org.example;

public class MBR {
    private Point lowerLeft;
    private Point upperRight;

    public MBR(Point lowerLeft, Point upperRight) {
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
    }
    // Getters and setters
    public Point getLowerLeft() {
        return lowerLeft;
    }

    public void setLowerLeft(Point lowerLeft) {
        this.lowerLeft = lowerLeft;
    }

    public Point getUpperRight() {
        return upperRight;
    }

    public void setUpperRight(Point upperRight) {
        this.upperRight = upperRight;
    }

    /**
     * Check if other MBR inside this MBR
     * @param other
     * @return
     */
    public boolean contains(MBR other) {
        // Get the coordinates of the lower-left and upper-right corners of both MBRs
        double thisMinX = this.lowerLeft.getLon();
        double thisMinY = this.lowerLeft.getLat();
        double thisMaxX = this.upperRight.getLon();
        double thisMaxY = this.upperRight.getLat();

        double otherMinX = other.lowerLeft.getLon();
        double otherMinY = other.lowerLeft.getLat();
        double otherMaxX = other.upperRight.getLon();
        double otherMaxY = other.upperRight.getLat();

        // Check if the minimum coordinates of 'other' are greater than or equal to the minimum coordinates of 'this'
        if (otherMinX < thisMinX || otherMinY < thisMinY) {
            return false;
        }

        // Check if the maximum coordinates of 'other' are less than or equal to the maximum coordinates of 'this'
        if (otherMaxX > thisMaxX || otherMaxY > thisMaxY) {
            return false;
        }

        return true;
    }

    /**
     * Check if other MBR intersect with this MBR
     * @param other
     * @return
     */
    public boolean intersects(MBR other) {
        // Check if there is no intersection along the x-axis
        if (this.lowerLeft.getLon() > other.upperRight.getLon() || other.lowerLeft.getLon() > this.upperRight.getLon()) {
            return false;
        }

        // Check if there is no intersection along the y-axis
        if (this.lowerLeft.getLat() > other.upperRight.getLat() || other.lowerLeft.getLat() > this.upperRight.getLat()) {
            return false;
        }

        // If there is no separation along both axes, the MBRs intersect
        return true;
    }

    /**
     * Combines two MBRs into a single new MBR
     * @param mbr1
     * @param mbr2
     * @return new MBR
     */
    public static MBR combineMBRs(MBR mbr1, MBR mbr2) {
        double minLon = Math.min(mbr1.getLowerLeft().getLon(), mbr2.getLowerLeft().getLon());
        double minLat = Math.min(mbr1.getLowerLeft().getLat(), mbr2.getLowerLeft().getLat());
        double maxLon = Math.max(mbr1.getUpperRight().getLon(), mbr2.getUpperRight().getLon());
        double maxLat = Math.max(mbr1.getUpperRight().getLat(), mbr2.getUpperRight().getLat());

        return new MBR(new Point(minLon, minLat), new Point(maxLon, maxLat));
    }

    /**
     * Compute area of this MBR
     * @param
     * @return
     */
    public double computeArea() {
        double lonDifference = upperRight.getLon() - lowerLeft.getLon();
        double latDifference = upperRight.getLat() - lowerLeft.getLat();

        return Math.abs(lonDifference * latDifference);
    }

    @Override
    public String toString() {
        return "MBR{" +
                "lowerLeft=" + lowerLeft +
                ", upperRight=" + upperRight +
                '}';
    }

}
