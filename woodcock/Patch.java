/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package woodcock;

/**
 *
 * @author Z98
 */
public class Patch {
    public int x, y;
    public int landCover, lakeSize, roadLength, soil, canopy, age;
    
    public Patch(int x, int y, int landCover, int lakeSize, int roadLength, int soil, int canopy)
    {
        this.x = x;
        this.y = y;
        this.landCover = landCover;
        this.lakeSize = lakeSize;
        this.soil = soil;
        this.canopy = canopy;
    }
}
