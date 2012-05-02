/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package woodcock;

import java.util.Comparator;
import java.util.PriorityQueue;

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
	public WCConservation (int forestPatchSize) {
		Comparator<Patch> comparator = new PatchLumberComparator();
		habitatCandidates = new PriorityQueue<Patch>(forestPatchSize, comparator);
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
				}	
			}
			rangeDevelop -= 100;
		}
	}
	
	public PriorityQueue<Patch> getPQueue () {
		return habitatCandidates;
	}
}
