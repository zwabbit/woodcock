package woodcock;

import java.util.*;
//Comparator for patch that is a lumber gathering area
//the nearer the lumber gathering area, the more profitable for lumber company;
//for pqueue usage
public class PatchLumberComparator implements Comparator<Patch> {
	@Override
	public int compare(Patch p1, Patch p2) {
		if (p1.lumberProfit > p2.lumberProfit)
			return 1;
		else if (p1.lumberProfit < p2.lumberProfit) {
			return -1;
		}
		return 0;
	}
}