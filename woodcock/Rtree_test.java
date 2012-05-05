/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package woodcock;

/**
 *
 * @author KahJing
 */
public class Rtree_test {
        public static void main(String[] args) {
            RTree rtreeTest = new RTree(4,8);
            int i = 1;
            int countFound = 0;
            int countNotFound = 0;
            // Placing the coor in the rtree
            while (i < 101) {
                AABB box = new AABB (i, i);
                rtreeTest.insert(box);
                System.out.println("AABB coor: " + box);
                i++;
            }
            int j = 1;
            // Checking the coordinate in the rtree
            
            while (j < 201) {
                // use 0 in the third args to query the exact point instead of 
                // a range
                if (Calculation.rangeQuery (rtreeTest, j, j, 0) == null) {
                        countNotFound++;
                }
                else {
                        countFound++;
                }
                j++;
            }
            System.out.println("Found coor: " + countFound);
            System.out.println("Not found coor: " + countNotFound);
        }
}
