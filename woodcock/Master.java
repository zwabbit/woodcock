/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package woodcock;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 *
 * @author Z98
 */
public class Master {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int tick = 0;
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get("test");
        
        LinkedList<Patch> forestPatches = new LinkedList<>();
        LinkedList<Patch> grassPatches = new LinkedList<>();
        
        PriorityQueue<Patch> cutCandidates = new PriorityQueue<>();
        
        try(BufferedReader reader = Files.newBufferedReader(path, charset))
        {
            reader.readLine();
            String line = null;
            while((line = reader.readLine()) != null)
            {
                String cols[] = line.split(",");
                int landCover = Integer.parseInt(cols[3]);
                if(landCover == 255)
                    continue;
                int x = Integer.parseInt(cols[1]);
                int y = Integer.parseInt(cols[2]);
                int canopy = Integer.parseInt(cols[4]);
                int lakeSize = Integer.parseInt(cols[8]);
                int roadLength = Integer.parseInt(cols[9]);
                int soil = Integer.parseInt(cols[10]);
                
                Patch patch = new Patch(x, y, landCover, lakeSize, roadLength, soil, canopy);
                
                /*
                 * Three different spatial partitioning trees are created,
                 * each one storing patches based on slightly different
                 * information.
                 */
                /*
                 * "Wet" regions to help determine if candidates are within
                 * range of wet patches for foraging purposes.  May be replaced/
                 * refined if we can get hydrology data.
                 */
                if(landCover == 11 || landCover == 90 || landCover == 95)
                {
                    
                }
                
                /*
                 * Forested regions to check to make sure grasslands
                 * are within range of suitable nesting areas before
                 * being declared suitable habitats.
                 */
                if(landCover == 41 || landCover == 42 || landCover == 43)
                {
                    /*
                     * Unilaterally add any forested patches to list.
                     * We need to iterate through this list later to
                     * determine whether it is already suitable habitat
                     * or is "candidate" for cutting to become suitable.
                     */
                    forestPatches.add(patch);
                }
                
                /*
                 * Grassland.  If we ever decide to plant trees to
                 * create habitat or require that cut candidates be
                 * within some range of grasslands.
                 */
                if(landCover == 52 || landCover == 71 || landCover == 95)
                {
                    /*
                     * Unilaterally add any grassland patches to list.
                     * We need to iterate through this list later to
                     * determine whether it is already suitable habitat.
                     */
                    grassPatches.add(patch);
                }
            }
        }
        catch(IOException ioe)
        {
            System.err.format("IOException: %s\n", ioe);
            System.exit(-1);
        }
        
        for(Patch p : forestPatches)
        {
            int x = p.x;
            int y = p.y;
            /*
             * Check hydrology info to see if this patch is a suitable
             * candidate for cutting.  If not, skip.
             */
            
            /*
             * If a suitable candidate, check age of forest.  If already
             * below 10 years of age, add to list of usable habitats.  If
             * not, add to cutting candidate list.  Order in which cutting
             * candidates are added needs to be determined based off of some
             * kind of priority, likely the age of the forest balanced against
             * information like road length to help determine ease of transport.
             */
        }
    }
}
