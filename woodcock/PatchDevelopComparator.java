package woodcock;

import java.util.*;
// Comparator for patch that is a developed area
// the further the developed area, the more suitable it will be for woodcock habitat;
// for pqueue usage
public class PatchDevelopComparator implements Comparator<Patch> {
	@Override
	 public int compare(Patch p1, Patch p2)
    {
    	if (p1.queuePos > p2.queuePos)
			return -1;
		else if (p1.queuePos < p2.queuePos) {
			return 1;
		}
    	return 0;
    }
}