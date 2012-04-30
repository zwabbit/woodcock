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
    public int landCover, waterDepth, roadLength, soil, canopy, age, landing;
    
    public Patch(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    public Patch(int x, int y, int landCover, int waterDepth, int roadLength, int soil, int canopy)
    {
        this.x = x;
        this.y = y;
        this.landCover = landCover;
        this.waterDepth = waterDepth;
        this.soil = soil;
        this.canopy = canopy;
    }
}
