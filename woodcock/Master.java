/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package woodcock;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * @author Z98
 */
public class Master {
    /**
     * @param args the command line arguments
     */
    public static RTree waterDepth = null;
    public static RTree timberSuitable = null;
    
    static int columns;
    static int rows;
    
    public static void main(String[] args) {
        // TODO code application logic here
        int tick = 0;
        int cIndex = 0;
        int rIndex = 0;
        double leastprofit = 0.0;
        double mostprofit = 0.0;
        int countSuitablePatch = 0; // for checking the total # of suitable patches for woodcock
        							// check for both water patch in vicinity and suitability of nearby 
        							// land for gathering lumber
        Charset charset = Charset.forName("US-ASCII");
        Path waterPath = Paths.get("wtdepthcmascii.txt");
        Path coverPath = Paths.get("cdl2011.txt");
        Path landPath = Paths.get("landingsuitability.txt");
        HashMap<List<Integer>, Patch> patches = new HashMap<>(); // update every year
        
        ArrayList<Patch> forestPatches = new ArrayList<>();
        LinkedList<Patch> grassPatches = new LinkedList<>();
        
        
        
        // rtree for nearby water area and suitability for harvesting lumber
        RTree waterDepthTree = new RTree(4, 8);
        RTree timberSuitableTree = new RTree(4, 8);
        RTree forestRtree = new RTree(4, 8);
        RTree developedArea = new RTree(4, 8);
        
        
        Calculation.initializeWeightedRandom();
        Calculation.initializeGrowth();
        System.err.println(System.getProperty("user.dir"));
        
        Patch testPatch = new Patch(0,0);
        testPatch.landCover = 141;
        /*
        for(int index = 1; index < 51; index++)
        {
            //System.out.println();
            System.out.print("Year " + index + " Value: ");
            testPatch.growTrees();
            //System.out.print(testPatch.baselArea +",\n");
            System.out.println(testPatch.calcValue());
        }
        */
        try (BufferedReader reader = Files.newBufferedReader(coverPath, charset))
        {
            cIndex = 0;
            rIndex = 0;
            String line = reader.readLine();
            String[] sCol = line.split("\\s+");
            columns = Integer.parseInt(sCol[1]);
            line = reader.readLine();
            String[] sRow = line.split("\\s+");
            rows = Integer.parseInt(sRow[1]);
            reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();
            while((line = reader.readLine()) != null)
            {
                String cols[] = line.split("\\s+");
                for(String col : cols)
                {
                    int landCover = Integer.parseInt(col);
                    if(landCover != -9999)
                    {
                        List<Integer> coord = Arrays.asList(cIndex, rIndex);
                        Patch patch = patches.get(coord);
                        if (patch == null) {
                            patch = new Patch(cIndex, rIndex);
                            patches.put(coord, patch);
                        }
                        patch.landCover = landCover;

                        /*
                         * Two different spatial partitioning trees are filled
                         * in, each one storing patches based on slightly
                         * different information.
                         */

                        /*
                         * Forested regions to check to make sure grasslands are
                         * within range of suitable nesting areas before being
                         * declared suitable habitats.
                         */
                        if(landCover == 141 || landCover == 142 || landCover == 143)
                        {
                            /*
                             * Unilaterally add any forested patches to list. We
                             * need to iterate through this list later to
                             * determine whether it is already suitable habitat
                             * or is "candidate" for cutting to become suitable.
                             */
                        	// insert into forest rtree to enable search for grassland area that is near forest
                        	// for habitat purpose
                        	AABB box = new AABB(rIndex, cIndex);
                        	forestRtree.insert(box);
                        	patch.age = Calculation.rand.nextInt(51);
                            forestPatches.add(patch);
                        }
                        

                        /*
                         * Grassland. If we ever decide to plant trees to create
                         * habitat or require that cut candidates be within some
                         * range of grasslands.
                         */
                        if(landCover == 152 || landCover == 171 || landCover == 195)
                        {
                            /*
                             * Unilaterally add any grassland patches to list.
                             * We need to iterate through this list later to
                             * determine whether it is already suitable habitat.
                             */
                            grassPatches.add(patch);
                        }
                        
                        // get the patches that are developed area
                        if(landCover >= 121 && landCover <= 124)
                        {
                        	AABB box = new AABB(rIndex, cIndex);
                        	developedArea.insert(box);
                        }
                    }
                    
                    ++cIndex;
                }
                
                cIndex = 0;
                ++rIndex;
            }
            
            reader.close();
        }
        catch(IOException ioe)
        {
            System.err.format("IOException: %s\n", ioe);
            System.exit(-1);
        }
        
        System.out.println("Done reading in cover data");
        
        try (BufferedReader reader = Files.newBufferedReader(waterPath, charset))
        {
            /*
             * The water depth data is used to create an R tree
             * to cover areas where the ground is likely to be
             * saturated. This is then searched to determine
             * proximity of suitable foraging ground for woodcock
             * habitats.
             */
            String line = reader.readLine();
            String[] sCol = line.split("\\s+");
            columns = Integer.parseInt(sCol[1]);
            line = reader.readLine();
            String[] sRow = line.split("\\s+");
            rows = Integer.parseInt(sRow[1]);
            reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();
            
            while((line = reader.readLine()) != null)
            {
                String cols[] = line.split("\\s+");
                for(String col : cols)
                {
                    int wDepth = Integer.parseInt(col);
                    if(wDepth != -9999)
                    {
                        List<Integer> coord = Arrays.asList(cIndex, rIndex);
                        Patch patch = patches.get(coord);
                        if (patch == null) {
                            patch = new Patch(cIndex, rIndex);
                            patches.put(coord, patch);
                        }
                        patch.waterDepth = wDepth;
                        
                        if(wDepth <= 30 && patch.landCover != 111)
                        {
                            //Insert into R tree.
                        	AABB box = new AABB(rIndex, cIndex);
                        	waterDepthTree.insert(box);
                        }
                    }
                    ++cIndex;
                }
                
                ++rIndex;
                cIndex = 0;
            }
            
            reader.close();
        }
        catch(IOException ioe)
        {
            System.err.format("IOException: %s\n", ioe);
            System.exit(-1);
        }
        
        System.out.println("Done reading in water data");
        
        try (BufferedReader reader = Files.newBufferedReader(landPath, charset))
        {
            /*
             * The landing data indicates suitability of
             * a patch for use landing harvested timber.
             * This helps determine the profitability of
             * harvesting specific patches based on distance
             * from the target patch to the nearest landing
             * patch.
             */
            String line = reader.readLine();
            String[] sCol = line.split("\\s+");
            columns = Integer.parseInt(sCol[1]);
            line = reader.readLine();
            String[] sRow = line.split("\\s+");
            rows = Integer.parseInt(sRow[1]);
            reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();
            
            while((line = reader.readLine()) != null)
            {
                String cols[] = line.split("\\s+");
                for(String col : cols)
                {
                    int landing = Integer.parseInt(col);
                    if(landing != -9999)
                    {
                        List<Integer> coord = Arrays.asList(cIndex, rIndex);
                        Patch patch = patches.get(coord);
                        if (patch == null) {
                            patch = new Patch(cIndex, rIndex);
                            patches.put(coord, patch);
                        }
                        patch.landing = landing;
                        
                        /*
                         * For now, let's only insert landings that are
                         * very suitable for landing into the R tree. If
                         * we feel like it, we can create another R tree
                         * for the moderately suitable and attribute a cost
                         * function of some sort for the two.
                         */
                        if(landing == 3)
                        {
                        	AABB box = new AABB(rIndex, cIndex);
                        	timberSuitableTree.insert(box);
                        }
                    }
                    
                    ++cIndex;
                }
                
                cIndex = 0;
                ++rIndex;
            }
            
            reader.close();
        }
        catch(IOException ioe)
        {
            System.err.format("IOException: %s\n", ioe);
            System.exit(-1);
        }
       
        System.out.println("Done loading in files.");
        
        // Comparator for both PQueue
        LumberCompany lumCompany = new LumberCompany(forestPatches.size());
        WCConservation conservGroup = new WCConservation(forestPatches.size());
        for(Patch p : forestPatches)
        {
            int x = p.x;
            int y = p.y;
            /*
             * Check hydrology info to see if this patch is a suitable
             * candidate for cutting.  If not, skip.
             */
            
            // calculate the value earned from the forest wood and store the profit in 
            // p.lumberProfit for comparison against other forest patch in pqueue
            p.calcValue();
            // check if the patch is suitable for lumber
            lumCompany.queueTimberPatch(timberSuitableTree, x, y, p);
            // check if the forest patch is near to any lumber gathering area
            // also, check if water patch(or patch with suitable water concentration) 
            // is within the range of 1; unit distance is in acre
            conservGroup.queueDevelopedPatch(developedArea, waterDepthTree, x, y, p);
            
            /*
             * If a suitable candidate, check age of forest.  If already
             * below 10 years of age, add to list of usable habitats.  If
             * not, add to cutting candidate list.  Order in which cutting
             * candidates are added needs to be determined based off of some
             * kind of priority, likely the age of the forest balanced against
             * information like road length to help determine ease of transport.
             */
        }
        
        // pqueue for both lumber company and conservative group
        PriorityQueue<Patch> lumberPQueue = new PriorityQueue<>();
        PriorityQueue<Patch> conserPQueue = new PriorityQueue<>();
        lumberPQueue = lumCompany.getPQueue();
        System.out.println("lumberPQueue size : " + lumberPQueue.size());
        conserPQueue = conservGroup.getPQueue();
        System.out.println("conserPQueue : " + conserPQueue.size());

        // test for rtree
        System.out.println("count of waterdepth rtree: " + waterDepthTree.count());
        AABB boxCheck = new AABB(300, 301, 956, 1000);
        // search if the patch is inside the water depth rtree(water area suitable for foraging)
        // return the range of coordinates if found(in (MinxCoor, MinyCoor):(MaxxCoor, MaxyCoor)), 
        // return null if not
        System.out.println("rtree query: " + waterDepthTree.queryOne(300, 956));    
        System.out.println("rtree query: " + waterDepthTree.queryOne(300, 957));    
        System.out.println("rtree query: " + waterDepthTree.queryOne(300, 1007));    
        System.out.println("rtree query: " + waterDepthTree.queryOne(300, 1014));    
        System.out.println("rtree query: " + waterDepthTree.queryOne(330, 1014));  
        System.out.println("rtree query: " + waterDepthTree.queryOne(boxCheck)); 
        // check if the rtree insert patch(x and y coordinates) correctly
        System.out.println("forestpatches: " + forestPatches.size());  
        System.out.println("grassPatches: " + grassPatches.size()); 
        // patch near water foraging area
        System.out.println("total forest patch near water foraging area: " + countSuitablePatch);
        System.out.println("most profit " + mostprofit + "\n least profit " + leastprofit);
        
        final ArrayList<Patch> finalForests = forestPatches;
        
        System.out.println("Generating forests.\n");
        /*
        for(Patch p : forestPatches)
        {
            p.generateTrees();
            p.age++;
        }
        */
        Parallel.withIndex(0, forestPatches.size() - 1, new Parallel.Each() {

            @Override
            public void run(int i) {
                Patch patch = finalForests.get(i);
                patch.generateTrees();
                patch.age++;
            }
            
        });
        
        System.out.println("Done generating forests.\n");
        
        while(true)
        {
            final int time = tick;
            Parallel.withIndex(0, forestPatches.size() - 1, new Parallel.Each() {

                @Override
                public void run(int i) {
                    Patch p = finalForests.get(i);
                    p.growTrees();
                    System.out.println("Value at time " + time + " at age " + p.age + ": " + p.calcValue());
                }
            });
            ++tick;
        }
    }
}
