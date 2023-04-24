package org.example;

import java.util.ArrayList;
import java.util.List;


public class RTree {
    private RTreeNode root;
    /**
     * each non-leaf node can have a maximum of d MBRs/subtrees
     */
    private int maxChildren;
    /**
     * each leaf node can contain a maximum of n polygons
     */
    private int bucketSize;

    public RTreeNode getRoot(){
        return root;
    }
    public RTree(int maxChildren, int bucketSize) {
        this.maxChildren = maxChildren;
        this.bucketSize = bucketSize;
    }

    public void insert(Polygon polygon) {
        MBR mbr = polygon.getMbr();
        //empty tree
        if (root == null) {
            root = new RTreeNode(mbr);
            root.getPolygons().add(polygon);
            root.setParent(null);
            return;
        }
        //find the leaf node suitable for inserting the polygon
        RTreeNode leaf = chooseLeaf(root, mbr);
        //insert into the leaf node
        leaf.getPolygons().add(polygon);
        //if there is not enough space, split node
        if (leaf.getPolygons().size() > bucketSize) {
            RTreeNode newNode = splitNode(leaf);
            //adjust the tree to keep balance
            adjustTree(leaf, newNode);
        }else {
            leaf.setMbr(MBR.combineMBRs(leaf.getMbr(),polygon.getMbr()));
            //backtrack and update mbrs
            updateParentMBRs(leaf);
        }
    }

    /**
     *  backtracking and updating MBR of parent node
     * @param node
     */
    private void updateParentMBRs(RTreeNode node) {
        // Stop backtracking if the node is the root
        if (node == root) {
            return;
        }

        // Get the parent node
        RTreeNode parentNode = node.getParent();

        // Update the parent node's MBR to include the current node's MBR
        parentNode.setMbr(MBR.combineMBRs(parentNode.getMbr(),node.getMbr()));

        // Recursively update the MBRs of parent nodes upwards
        updateParentMBRs(parentNode);
    }

    /**
     * select a suitable leaf node for inserting a given MBR
     * it tries to insert MBRs that are close to each other into the same leaf node
     * @param node
     * @param mbr
     * @return
     */
    private RTreeNode chooseLeaf(RTreeNode node, MBR mbr) {
        //0.start from root
        //1.if the current node is a leaf node, return that node
        if (node.isLeaf()) {
            return node;
        }

        double minAreaIncrease = Double.MAX_VALUE;
        double minArea = Double.MAX_VALUE;
        RTreeNode bestChild = null;
        //2.else iterate through all child nodes
        for (RTreeNode child : node.getChildren()) {
            //2.1.calculate the area increase and find the best child node with the smallest area increase
            double areaIncrease = computeAreaIncrease(child.getMbr(), mbr);
            if (areaIncrease < minAreaIncrease || (areaIncrease == minAreaIncrease && child.getMbr().computeArea() < minArea)) {
                minAreaIncrease = areaIncrease;
                minArea = child.getMbr().computeArea();
                bestChild = child;
            }
        }
        //3.recursively traverse the best node currently found
        return chooseLeaf(bestChild, mbr);
    }


    /**
     * Computes the area increase of mbr1 when mbr2 is inserted into it
     * @param mbr1
     * @param mbr2
     * @return area increase
     */
    private double computeAreaIncrease(MBR mbr1, MBR mbr2) {
        //1.combine two mbrs
        MBR combinedMBR = MBR.combineMBRs(mbr1, mbr2);
        //2.calculate the diff
        return combinedMBR.computeArea() - mbr1.computeArea();
    }


    /**
     *  using the Quadratic-Cost algorithm
     * @param node
     * @return
     */
    private RTreeNode splitNode(RTreeNode node) {
        //for choosing the seeds
        List<MBR> mbrs = new ArrayList<>();
        //split the leaf node
        if (node.isLeaf()) {
            // select two entries with the highest quadratic-cost
            List<Polygon> polygons = node.getPolygons();
            for(Polygon polygon:polygons){
                mbrs.add(polygon.getMbr());
            }
            int[] seedEntries = selectSeeds(mbrs);
            Polygon polygon1 = polygons.get(seedEntries[0]);
            Polygon polygon2 = polygons.get(seedEntries[1]);
            // create two new leaf nodes and assign the seed entries to them
            RTreeNode leafNode1 = new RTreeNode(polygon1.getMbr());
            leafNode1.setParent(node.getParent());
            leafNode1.getPolygons().add(polygon1);
            RTreeNode leafNode2 = new RTreeNode(polygon2.getMbr());
            leafNode2.setParent(node.getParent());
            leafNode2.getPolygons().add(polygon2);

            // remove the seed entries from the original node
            node.getPolygons().remove(polygon1);
            node.getPolygons().remove(polygon2);

            // assign remaining entries to the new nodes
            for (Polygon polygon : polygons) {
                int flag = assignEntry(polygon.getMbr(), leafNode1.getMbr(), leafNode2.getMbr());
                if(flag == 1){
                    //update the mbr of node
                    MBR newMBR = MBR.combineMBRs(polygon.getMbr(),leafNode1.getMbr());
                    leafNode1.getPolygons().add(polygon);
                    leafNode1.setMbr(newMBR);
                }else if(flag == 2){
                    MBR newMBR = MBR.combineMBRs(polygon.getMbr(),leafNode2.getMbr());
                    leafNode2.getPolygons().add(polygon);
                    leafNode2.setMbr(newMBR);
                }
            }
            node.getPolygons().clear();
            node.getPolygons().addAll(leafNode1.getPolygons());
            node.setMbr(leafNode1.getMbr());
            return leafNode2;
        }else {
            // select two entries with the highest quadratic-cost
            List<RTreeNode> children = node.getChildren();
            for(RTreeNode child:children){
                mbrs.add(child.getMbr());
            }
            int[] seedEntries = selectSeeds(mbrs);
            RTreeNode rTreeNode1 = children.get(seedEntries[0]);
            RTreeNode rTreeNode2 = children.get(seedEntries[1]);
            // create two new non leaf nodes and assign the seed entries to them
            RTreeNode nonLeafNode1 = new RTreeNode(rTreeNode1.getMbr());
            nonLeafNode1.getChildren().add(rTreeNode1);
            nonLeafNode1.setParent(node.getParent());
            rTreeNode1.setParent(nonLeafNode1);
            RTreeNode nonLeafNode2 = new RTreeNode(rTreeNode2.getMbr());
            nonLeafNode2.setParent(node.getParent());
            nonLeafNode2.getChildren().add(rTreeNode2);
            rTreeNode2.setParent(nonLeafNode2);

            // remove the seed entries from the original node
            node.getChildren().remove(rTreeNode1);
            node.getChildren().remove(rTreeNode2);

            // assign remaining entries to the new nodes
            for (RTreeNode child : children) {
                int flag = assignEntry(child.getMbr(), nonLeafNode1.getMbr(), nonLeafNode2.getMbr());
                if(flag == 1){
                    //update the mbr of node
                    MBR newMBR = MBR.combineMBRs(child.getMbr(),nonLeafNode1.getMbr());
                    nonLeafNode1.getChildren().add(child);
                    child.setParent(nonLeafNode1);
                    nonLeafNode1.setMbr(newMBR);
                }else if(flag == 2){
                    MBR newMBR = MBR.combineMBRs(child.getMbr(),nonLeafNode2.getMbr());
                    nonLeafNode2.getChildren().add(child);
                    child.setParent(nonLeafNode2);
                    nonLeafNode2.setMbr(newMBR);
                }
            }
            node.getChildren().clear();
            node.getChildren().addAll(nonLeafNode1.getChildren());
            for(RTreeNode child:nonLeafNode1.getChildren()){
                child.setParent(node);
            }
            node.setMbr(nonLeafNode1.getMbr());
            return nonLeafNode2;

        }
    }
    /**
     * Selects two entries that have the highest quadratic-cost.
     * Quadratic-cost:combined mbr - mbr1 - mbr2 (area difference)
     *
     * @param entries
     * @return entries' indexes
     */
    private int[] selectSeeds(List<MBR> entries) {
        int[] seeds = new int[2];
        double maxQuadraticCost = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < entries.size(); i++) {
            MBR mbr1 = entries.get(i);

            for (int j = i + 1; j < entries.size(); j++) {
                MBR mbr2 = entries.get(j);

                // Calculate the combined MBR
                MBR combinedMBR = MBR.combineMBRs(mbr1, mbr2);

                // Compute the quadratic cost
                double quadraticCost = combinedMBR.computeArea() - mbr1.computeArea() - mbr2.computeArea();

                // Update the seeds if a higher quadratic cost is found
                if (quadraticCost > maxQuadraticCost) {
                    maxQuadraticCost = quadraticCost;
                    seeds[0] = i;
                    seeds[1] = j;
                }
            }
        }
        return seeds;
    }

    /**
     * Assigns an entry to one of the two given nodes based on the least area increase.
     * In case of a tie, the entry is assigned to the node with the least area.
     * @param entry
     * @param node1
     * @param node2
     * @return which node to assign
     */
    private int assignEntry(MBR entry, MBR node1, MBR node2) {

        // Calculate area increases
        double areaIncrease1 = computeAreaIncrease(node1, entry);
        double areaIncrease2 = computeAreaIncrease(node2, entry);

        // Assign the entry to the node with the least area increase
        int flag = 0;
        if (areaIncrease1 < areaIncrease2) {
            flag = 1;
        } else if (areaIncrease1 > areaIncrease2) {
            flag = 2;
        } else {
            // In case of a tie, assign the entry to the node with the least area
            double area1 = node1.computeArea();
            double area2 = node2.computeArea();

            if (area1 <= area2) {
                flag = 1;
            } else {
                flag = 2;
            }
        }
        return flag;

    }

    /**
     * traversing the tree upwards from the split node
     * and making necessary adjustments to the parent node
     * @param node
     * @param newNode
     */
    private void adjustTree(RTreeNode node, RTreeNode newNode) {
        // If reached the root, create a new root
        if (node == root) {
            MBR newRootMBR = MBR.combineMBRs(node.getMbr(),newNode.getMbr());
            RTreeNode newRoot = new RTreeNode(newRootMBR);
            newRoot.setParent(null);
            node.setParent(newRoot);
            newNode.setParent(newRoot);
            newRoot.getChildren().add(node);
            newRoot.getChildren().add(newNode);
            root = newRoot;
            return;
        }
        RTreeNode parent = node.getParent();
        // Update the MBR of node in the parent
        parent.setMbr(MBR.combineMBRs(parent.getMbr(),node.getMbr()));
        // Add the new node to the parent
        parent.getChildren().add(newNode);
        newNode.setParent(parent);
        // If the parent is full,split it
        if (parent.getChildren().size() > maxChildren) {
            RTreeNode newSibling = splitNode(parent);
            adjustTree(parent, newSibling);
        }else {
            //Update the mbr
            parent.setMbr(MBR.combineMBRs(parent.getMbr(),newNode.getMbr()));
        }
    }


    /**
     * @return the height of the tree
     */
    public int getTreeHeight() {
        return getTreeHeight(root);
    }

    private int getTreeHeight(RTreeNode node) {
        if (node == null) {
            return 0;
        }

        if (node.isLeaf()) {
            return 1;
        }

        int maxHeight = 0;
        for (RTreeNode child : node.getChildren()) {
            int childHeight = getTreeHeight(child);
            maxHeight = Math.max(maxHeight, childHeight);
        }

        return maxHeight + 1;
    }

    /**
     * @return the number of non-leaf nodes
     */
    public int getNumberOfNonLeafNodes() {
        return getNumberOfNodes(root, false);
    }
    /**
     * @return the number of leaf nodes
     */
    public int getNumberOfLeafNodes() {
        return getNumberOfNodes(root, true);
    }

    /**
     *
     * @param node search from this node
     * @param countLeafNodes true-count leaf false-count non-leaf
     * @return
     */
    private int getNumberOfNodes(RTreeNode node, boolean countLeafNodes) {
        if (node == null) {
            return 0;
        }

        if (node.isLeaf()) {
            return countLeafNodes ? 1 : 0;
        } else {
            if (countLeafNodes) {
                int leafNodeCount = 0;
                for (RTreeNode child : node.getChildren()) {
                    leafNodeCount += getNumberOfNodes(child, true);
                }
                return leafNodeCount;
            } else {
                int nonLeafNodeCount = 1;
                for (RTreeNode child : node.getChildren()) {
                    nonLeafNodeCount += getNumberOfNodes(child, false);
                }
                return nonLeafNodeCount;
            }
        }
    }


    public QueryResult windowQuery(MBR queryMBR) {
        List<Polygon> results = new ArrayList<>();
        int checkedCount = windowQueryRecursive(root, queryMBR, results);
        return new QueryResult(results,checkedCount);
    }

    /**
     * @param node
     * @param queryMBR
     * @param results
     * @return the number of checked polygons
     */
    private int windowQueryRecursive(RTreeNode node, MBR queryMBR, List<Polygon> results) {
        int checkedCount = 0;
        if (node.isLeaf()) {
            // If the node is a leaf, add all intersecting polygons to the result set
            for (Polygon polygon : node.getPolygons()) {
                checkedCount++;
                if (queryMBR.contains(polygon.getMbr())) {
                    results.add(polygon);
                }
            }
        } else {
            // If the node is not a leaf, visit child nodes whose MBRs intersect with the query MBR
            for (RTreeNode child : node.getChildren()) {
                if (queryMBR.intersects(child.getMbr())) {
                    checkedCount += windowQueryRecursive(child, queryMBR, results);
                }
            }
        }
        return checkedCount;
    }
}

