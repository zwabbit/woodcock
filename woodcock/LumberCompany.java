/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package woodcock;
import java.util.*;
/**
 *
 * @author Z98
 */
public class LumberCompany{
    /*
     * This class goes through forest patches
     * in order to order them based on how desirable
     * or profitable cutting a patch would be.
     */
	private PriorityQueue<Patch> lumberCandidates;
        public HashMap<List<Integer>, Patch> candidateMap;
	
	public LumberCompany (int forestPatchSize) {
		Comparator<Patch> comparator = new PatchLumberComparator();
		lumberCandidates = new PriorityQueue<Patch>(forestPatchSize, comparator);
                candidateMap = new HashMap<>();
	}
	
	// check within the range 1000 for suitable landing area. 
	// assume that the shipping cost is a direct approximation to the distance travelled
	public void queueTimberPatch(RTree timberSuitable, int x, int y, Patch p) {
		int rangeLanding = 100;
		while (rangeLanding < 1000) {
			if (rangeQuery(timberSuitable, x, y, rangeLanding) != null) {
				p.lumberProfit -= rangeLanding;
				if (p.lumberProfit > 250) {
					lumberCandidates.add(p);
                                        List<Integer> key = Arrays.asList(p.x, p.y);
                                        candidateMap.put(key, p);
				}
			}
			rangeLanding += 100;
		}
	}
        
        public boolean isCandidate(Patch p)
        {
            List<Integer> key = Arrays.asList(p.x, p.y);
            Patch patch = candidateMap.get(key);
            if(patch == null)
                return false;
            
            return true;
        }
	
	public PriorityQueue<Patch> getPQueue () {
		return lumberCandidates;
	}
	
	/*
     * Search associated rtree with point(x, y) coordinates as the centre point with the radius specified.
     * Radius is in acre unit.  
     * 
     * @parameter	rtree - rtree to search from 
     * @parameter	xCoor - xCoor of the centre point for the range search
     * @parameter	yCoor - yCoor of the centre point for the range search
     * @parameter	radius - radius from the point for the range search
     * 
     * @return 	one of the point within the range
     */
    public static BoundedObject rangeQuery (RTree rtree, int xCoor, int yCoor, int radius) {
    	AABB o = new AABB(xCoor - radius, xCoor + radius, yCoor - radius, yCoor + radius);
    	return rtree.queryOne(o);
    }
}


