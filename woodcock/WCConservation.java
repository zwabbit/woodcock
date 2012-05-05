/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package woodcock;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neos.client.NeosClient;
import org.neos.client.NeosJob;
import org.neos.client.NeosJobXml;
import org.neos.gams.*;

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
    
    boolean hasGams = false;
    public PriorityQueue<Patch> habitatCandidates;
    public HashMap<List<Integer>, Patch> candidateMap;
    int requiredHabitats = 500;
    
    public int rangeDevelop = 1;
    
    public RTree candidateTree = null;

    public WCConservation(int forestPatchSize) {
        Comparator<Patch> comparator = new PatchLumberComparator();
        habitatCandidates = new PriorityQueue<>(forestPatchSize, comparator);
        candidateMap = new HashMap<>();
        candidateTree = new RTree(4, 8);

        /*
        String pathEnv = System.getenv("PATH");
        hasGams = pathEnv.contains("GAMS");
        if (hasGams == false) {
            hasGams = pathEnv.contains("gams");
        }
        */
    }

    // get the suitable patch for woodcock habitat
    // still have to check against water patch and forest area
    public boolean checkSuitability(int x, int y, Patch p) {
        /*
        if (Calculation.rangeQuery(Master.developedArea, x, y, rangeDevelop) != null)
        {
            if(Master.DEBUG_FLAG) System.err.println("Too close to developed area.");
            return false;
        }
        */
        if (Calculation.rangeQuery(Master.waterDepth, x, y, waterDist) == null)
        {
            if(Master.DEBUG_FLAG) System.err.println("Too far from wet area.");
            return false;
        }
        if(Calculation.rangeQuery(Master.youngForests, x, y, 1) == null)
        {
            if(Master.DEBUG_FLAG) System.err.println("Too far from young forest.");
            return false;
        }
        if(Calculation.rangeQuery(candidateTree, x, y, 1) != null)
        {
            if(Master.DEBUG_FLAG) System.err.println("Too close to other candidate.");
            return false;
        }

        habitatCandidates.add(p);
        List<Integer> key = Arrays.asList(p.x, p.y);
        candidateMap.put(key, p);
        candidateTree.insert(p.box);
        
        return true;
    }

    public boolean isCandidate(Patch p) {
        List<Integer> key = Arrays.asList(p.x, p.y);
        Patch patch = candidateMap.get(key);
        if (patch == null) {
            return false;
        }

        return true;
    }

    public PriorityQueue<Patch> getPQueue() {
        return habitatCandidates;
    }
    static int waterDist = 10;
    int found = 0;
    public boolean ForceHabitat(ArrayList<Patch> forestPatches)
    {
        int required = 1000;
        int dev = 0;
        int water = 0;
        int candidate = 0;
        
        for(Patch forest : forestPatches)
        {
            /*
            if (Calculation.rangeQuery(Master.developedArea, forest.x, forest.y, rangeDevelop) != null) {
                if (Master.DEBUG_FLAG) {
                    System.err.println("Too close to developed area.");
                }
                ++dev;
                continue;
            }
            * 
            */
            if (Calculation.rangeQuery(Master.waterDepth, forest.x, forest.y, waterDist) == null) {
                if (Master.DEBUG_FLAG) {
                    System.err.println("Too far from wet area.");
                }
                ++water;
                continue;
            }
            if (Calculation.rangeQuery(candidateTree, forest.x, forest.y, 1) != null) {
                if (Master.DEBUG_FLAG) {
                    System.err.println("Too close to other candidate.");
                }
                ++candidate;
                continue;
            }
            
            habitatCandidates.add(forest);
            List<Integer> key = Arrays.asList(forest.x, forest.y);
            candidateMap.put(key, forest);
            candidateTree.insert(forest.box);
            forest.ClearCut();
            ++found;
            if(found >= required)
                return true;
        }
        System.err.println("Forced generation count: " + found);
        System.err.println("Failed due to dev: " + dev);
        System.err.println("Failed due to water: " + water);
        System.err.println("Failed due to candidate: " + candidate);
        waterDist *= 10;
        return false;
    }

    public LinkedList<Patch> optimizeCuts() {
        StringBuilder resultsBuilder = new StringBuilder();
        String results;
        HashMap<Integer, Integer> xValues = new HashMap<>();
        HashMap<Integer, Integer> yValues = new HashMap<>();
        PriorityQueue<Integer> xOrdered = new PriorityQueue<>();
        PriorityQueue<Integer> yOrdered = new PriorityQueue<>();
        ArrayList<PriorityQueue<Integer>> orderedSet = new ArrayList<>();
        orderedSet.add(xOrdered);
        orderedSet.add(yOrdered);
        ArrayList<org.neos.gams.Set> gSets = new ArrayList<>();
        final ArrayList<PriorityQueue<Integer>> fOrderedSet = orderedSet;
        StringBuilder modelContent = new StringBuilder();

        try {

            org.neos.gams.Set xSet = new org.neos.gams.Set("x", "X coordinates");
            org.neos.gams.Set ySet = new org.neos.gams.Set("y", "X coordinates");
            gSets.add(xSet);
            gSets.add(ySet);
            Parameter value = new Parameter("patchValue(x,y)", "Patch values");
            Parameter isCandidate = new Parameter("isCandidate(x,y)", "Is a candidate");
            Scalar minVal =
                    new Scalar(
                    "minVal",
                    "Minimally acceptable value",
                    String.valueOf(LumberCompany.MIN_VALUE));
            Scalar required =
                    new Scalar(
                    "requiredPatches",
                    "Required number of patches to cut",
                    String.valueOf(requiredHabitats));

            for (Patch candidate : habitatCandidates) {
                value.add(candidate.x + "." + candidate.y, String.valueOf(candidate.calcValue()));
                isCandidate.add(candidate.x + "." + candidate.y, "1");
                if (xValues.get(candidate.x) == null) {
                    xValues.put(candidate.x, candidate.x);
                    xOrdered.add(candidate.x);
                }
                if (yValues.get(candidate.y) == null) {
                    yValues.put(candidate.y, candidate.y);
                    yOrdered.add(candidate.y);
                }
            }

            final ArrayList<org.neos.gams.Set> fSets = gSets;

            /*
             * Add set members in parallel.
             */
            /*
            Parallel.withIndex(0, 1, new Parallel.Each() {

                @Override
                public void run(int i) {
                    PriorityQueue<Integer> xO = fOrderedSet.get(i);
                    for (Integer itgr : xO) {
                        fSets.get(i).addValue(String.valueOf(itgr));
                    }
                }
            });*/
            for(Integer itgr : xOrdered)
            {
                xSet.addValue(String.valueOf(itgr));
            }
            
            for(Integer itgr : yOrdered)
            {
                ySet.addValue(String.valueOf(itgr));
            }

            modelContent.append(xSet.toString()).append("\n");
            modelContent.append(ySet.toString()).append("\n");
            modelContent.append(value.toString()).append("\n");
            modelContent.append(isCandidate.toString()).append("\n");
            modelContent.append(minVal.toString()).append("\n");
            modelContent.append(required.toString()).append("\n");

            try (Scanner scanner = new Scanner(new FileInputStream(Calculation.inputTemplatePath),
                            "US-ASCII")) {
                while (scanner.hasNextLine()) {
                    modelContent.append(scanner.nextLine()).append("\n");
                }
            }

            FileWriter modelFile =
                    new FileWriter(Calculation.outputModelPath);

            modelFile.write(modelContent.toString());
            modelFile.close();
            if (hasGams) {
                ProcessBuilder pBuilder = new ProcessBuilder("gams");
                Process gamsProcess = pBuilder.start();
                BufferedReader gamsReader =
                        new BufferedReader(new InputStreamReader(gamsProcess.getInputStream()));
                try {
                    gamsProcess.waitFor();
                } catch (InterruptedException ex) {
                    //Logger.getLogger(WCConservation.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println(ex.getMessage());
                }
                String line;
                while ((line = gamsReader.readLine()) != null) {
                    resultsBuilder.append(line).append("\n");
                }
            }
        } catch(IOException ex) {
            System.err.println("Error: running GAMS locally failed: " + ex.getMessage());
        }
        
        if(resultsBuilder.length() == 0)
        {
            NeosClient neosClient = new NeosClient(Calculation.NEOS_HOST, Calculation.NEOS_PORT);
            NeosJobXml jobXml = new NeosJobXml("mip", "xpress", modelContent.toString());
            NeosJob neosJob = neosClient.submitJob(jobXml.toXMLString());
            results = neosJob.getResult();
        }
        else
        {
            results = resultsBuilder.toString();
        }
        
        if(results.isEmpty())
        {
            return null;
        }
        
        SolutionParser parser = new SolutionParser(results);
        if(parser.getModelStatusCode() != 1)
            return null;
        
        LinkedList<Patch> selectedPatches = new LinkedList<>();
        SolutionData bCut = parser.getSymbol("cut", SolutionData.VAR, habitatCandidates.size());
        
        for(SolutionRow sRow : bCut.getRows())
        {
            int level = sRow.getLevel().intValue();
            if(level == 1)
            {
                int xCoord = Integer.valueOf(sRow.getIndex(0));
                int yCoord = Integer.valueOf(sRow.getIndex(1));
                List<Integer> key = Arrays.asList(xCoord, yCoord);
                Patch cutPatch = candidateMap.get(key);
                selectedPatches.add(cutPatch);
            }
        }
        
        return selectedPatches;
    }
}
