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
public class WCConservation {
    /*
     * This class handles searching for candidate
     * habitats where woodcock can live and finding
     * candidates for cutting to create these habitats.
     */
	private PriorityQueue<Patch> habitatCandidates;
        public HashMap<List<Integer>, Patch> candidateMap;
	public WCConservation (int forestPatchSize) {
		Comparator<Patch> comparator = new PatchLumberComparator();
		habitatCandidates = new PriorityQueue<Patch>(forestPatchSize, comparator);
                candidateMap = new HashMap<>();
	}
	
	// get the suitable patch for woodcock habitat
	// still have to check against water patch and forest area
	public void queueDevelopedPatch(RTree developedArea, RTree waterArea, int x, int y, Patch p) {
		int rangeDevelop = 1000;
		while (rangeDevelop > 100) {
			if (LumberCompany.rangeQuery(developedArea, x, y, rangeDevelop) == null) {
				if (LumberCompany.rangeQuery(waterArea, x, y, 1) != null) {
					p.developDistance = rangeDevelop / 100;
					habitatCandidates.add(p);
                                        List<Integer> key = Arrays.asList(p.x, p.y);
                                        candidateMap.put(key, p);
				}	
			}
			rangeDevelop -= 100;
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
		return habitatCandidates;
	}
}
