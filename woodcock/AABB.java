
package woodcock;

/**
 * 3D Axis Aligned Bounding Box
 * @author Colonel32
 */
public class AABB implements BoundedObject
{
	int minx, miny;
	int maxx, maxy;
	public int landCover, soil, canopy, age;
	public float lakeSize, roadLength;
	    
	public AABB(int minx, int maxx, int miny, int maxy) {
		this.minx = minx;
		this.maxx = maxx;
		this.miny = miny;
		this.maxy = maxy;
	}

	public AABB(int x, int y)
	{
		this.minx = x;
		this.maxx = x;
		this.miny = y;
		this.maxy = y;
	}
	public AABB()
	{
		minx = miny = 0;
		maxx = maxy = 0;
	}

	public void setMinCorner(int px, int py)
	{
		minx = px;
		miny = py;
	}
	public void setMaxCorner(int px, int py)
	{
		maxx = px;
		maxy = py;
	}

	public boolean contains(int px, int py)
	{
		return px >= minx && px <= maxx &&
				py >= miny && py <= maxy;
	}

	public boolean overlaps(AABB other)
	{
		if(minx > other.maxx) return false;
		if(maxx < other.minx) return false;
		if(miny > other.maxy) return false;
		if(maxy < other.miny) return false;
		return true;
	}
	
	/**
	 * Returns the amount of overlap between 2 AABBs. Result will be negative if they
	 * do not overlap.
	 */
	public int getOverlap(AABB other)
	{
		int overlapx =  (maxx - minx +other.maxx - other.minx) - Math.abs(minx+maxx-other.minx-other.minx);
		int overlapy =  (maxy - miny +other.maxy - other.miny) - Math.abs(minx+maxy-other.miny-other.miny);
		
		return Math.max(overlapx, overlapy);
		
	}

	/**
	 * Returns the amount that other will need to be expanded to fit this.
	 */
	public int expansionNeeded(AABB other)
	{
		int total = 0;

		if(other.minx < minx) total += minx - other.minx;
		if(other.maxx > maxx) total += other.maxx - maxx;

		if(other.miny < miny) total += miny - other.miny;
		if(other.maxy > maxy) total += other.maxy - maxy;

		return total;
	}

	/**
	 * Computes an AABB that contains both this and other and stores it in this.
	 * @return this
	 */
	public AABB merge(AABB other)
	{
		minx = Math.min(minx, other.minx);
		maxx = Math.max(maxx, other.maxx);

		miny = Math.min(miny, other.miny);
		maxy = Math.max(maxy, other.maxy);

		return this;
	}

	public int getVolume()
	{
		return (maxx - minx) * (maxy - miny);
	}

	public AABB clone()
	{
		AABB clone = new AABB();
		clone.minx = minx;
		clone.miny = miny;

		clone.maxx = maxx;
		clone.maxy = maxy;
		return clone;
	}

	public void cloneInto(AABB target)
	{
		target.minx = minx;
		target.miny = miny;

		target.maxx = maxx;
		target.maxy = maxy;
	}

	public boolean equals(AABB other)
	{
		return minx == other.minx && maxx == other.maxx &&
				miny == other.miny && maxy == other.maxy;
	}

	public AABB getBounds() { return this; }
	public String toString()
	{
		return String.format("(%1$d,%2$d):(%3$d,%4$d)", minx, miny, maxx, maxy);
	}
}
