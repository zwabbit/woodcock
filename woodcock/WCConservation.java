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
    public PriorityQueue<Patch> habitatZone;
    public PriorityQueue<Patch> habitatCandidates;
    public HashMap<List<Integer>, Patch> candidateMap;
    public static HashMap<List<Integer>, Patch> habitatMap;
    public static final int requiredHabitats = 40;
    public int alreadySuitable = 0;
    
    public HashMap<List<Integer>, Integer> selectFreq;
    
    public PriorityQueue<Patch> cutCandidates;
    
    public int rangeDevelop = 1;
    
    //public RTree candidateTree = null;
    
    public int waterDist = 25;
    int found = 0;
    
    public int closeToWater = 0;
    
    Comparator<Patch> comparator = null;
    
    private StringBuilder modelTemplate;
    private StringBuilder modelTemplateS1;
    private StringBuilder modelTemplateS4;
    private StringBuilder modelTemplateS5;

    public WCConservation(int forestPatchSize) {
        comparator = new PatchLumberComparator();
        habitatCandidates = new PriorityQueue<>(forestPatchSize, comparator);
        habitatZone = new PriorityQueue<>(forestPatchSize, comparator);
        cutCandidates = new PriorityQueue<>(forestPatchSize, comparator);
        candidateMap = new HashMap<>();
        habitatMap = new HashMap<>();
        selectFreq = new HashMap<>();
        modelTemplate = new StringBuilder();
        modelTemplateS1 = new StringBuilder();
        modelTemplateS4 = new StringBuilder();
        modelTemplateS5 = new StringBuilder();
        
        Scanner scanner; 
        try {
            scanner = new Scanner(new FileInputStream(Calculation.inputTemplatePath),
                    "US-ASCII");
            while (scanner.hasNextLine()) {
                modelTemplate.append(scanner.nextLine()).append("\n");
            }
            
            scanner = new Scanner(new FileInputStream(Calculation.inputTemplatePathS1),
                    "US-ASCII");
            while (scanner.hasNextLine()) {
                modelTemplateS1.append(scanner.nextLine()).append("\n");
            }
                
            scanner = new Scanner(new FileInputStream(Calculation.inputTemplatePathS4), "US-ASCII");
            while(scanner.hasNextLine())
                modelTemplateS4.append(scanner.nextLine()).append("\n");
            
            scanner = new Scanner(new FileInputStream(Calculation.inputTemplatePathS5), "US-ASCII");
            while(scanner.hasNextLine())
                modelTemplateS5.append(scanner.nextLine()).append("\n");
        } catch (FileNotFoundException ex) {
            //Logger.getLogger(WCConservation.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("FATAL: Failed to read in model file.");
            System.exit(-1);
        }
            
        
        //candidateTree = new RTree(4, 8);

        /*
        String pathEnv = System.getenv("PATH");
        hasGams = pathEnv.contains("GAMS");
        if (hasGams == false) {
            hasGams = pathEnv.contains("gams");
        }
        */
    }
    
    public PriorityQueue<Patch> CheckForestSuitability()
    {
        for(Patch p : habitatMap.values())
        {
            checkSuitability(p);
        }
        
        if(habitatCandidates.size() + alreadySuitable < requiredHabitats)
        {
            waterDist += 25;
            System.err.println("Water distance increased to: " + waterDist);
        }
        
        return habitatCandidates;
    }

    // get the suitable patch for woodcock habitat
    // still have to check against water patch and forest area
    public boolean checkSuitability(Patch p) {
        int x = p.x;
        int y = p.y;
        
        if(candidateMap.get(p.key) != null)
        {
            return false;
        }
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
        ++closeToWater;
        if(Calculation.rangeQuery(Master.youngForests, x, y, 1) == null)
        {
            if(Master.DEBUG_FLAG) System.err.println("Too far from young forest.");
            return false;
        }
        /*
        if(Calculation.rangeQuery(candidateTree, x, y, 1) != null)
        {
            if(Master.DEBUG_FLAG) System.err.println("Too close to other candidate.");
            return false;
        }
        */
        
        if(p.age < 3)
        {
            ++alreadySuitable;
            return true;
        }

        habitatCandidates.add(p);
        candidateMap.put(p.key, p);
        //candidateTree.insert(p.box);
        
        return true;
    }

    public boolean isCandidate(Patch p) {
        Patch patch = candidateMap.get(p.key);
        if (patch == null) {
            return false;
        }

        return true;
    }

    public PriorityQueue<Patch> getPQueue() {
        return habitatCandidates;
    }
    
    public boolean ForceHabitat(ArrayList<Patch> forestPatches)
    {
        int required = 500;
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
            /*
            if (Calculation.rangeQuery(candidateTree, forest.x, forest.y, 1) != null) {
                if (Master.DEBUG_FLAG) {
                    System.err.println("Too close to other candidate.");
                }
                ++candidate;
                continue;
            }
            */
            //habitatCandidates.add(forest);
            habitatMap.put(forest.key, forest);
            //candidateTree.insert(forest.box);
            ++found;
        }
        if (found >= required) {
            return true;
        }
        System.err.println("Forced generation count: " + found);
        System.err.println("Failed due to distance from developed land: " + dev);
        System.err.println("Failed due to distance from water: " + water);
        System.err.println("Failed due to distance from existing candidate: " + candidate);
        waterDist += 25;
        return false;
    }

    public PriorityQueue<Patch> optimizeCuts() {
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
            org.neos.gams.Set xSubSet = new org.neos.gams.Set("xSub(x)", "X coordinates");
            org.neos.gams.Set ySubSet = new org.neos.gams.Set("ySub(y)", "X coordinates");
            
            xSet.addValue("0*" + String.valueOf(Master.columns - 1));
            ySet.addValue("0*" + String.valueOf(Master.rows - 1));
            gSets.add(xSubSet);
            gSets.add(ySubSet);
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
            while(xOrdered.size() > 0)
            {
                xSubSet.addValue(String.valueOf(xOrdered.remove()));
            }
            
            while(yOrdered.size() > 0)
            {
                ySubSet.addValue(String.valueOf(yOrdered.remove()));
            }

            modelContent.append("$offdigit").append("\n");
            modelContent.append(xSet.toString()).append("\n");
            modelContent.append(ySet.toString()).append("\n");
            modelContent.append(xSubSet.toString()).append("\n");
            modelContent.append(ySubSet.toString()).append("\n");
            modelContent.append(value.toString()).append("\n");
            //modelContent.append(isCandidate.toString()).append("\n");
            modelContent.append(minVal.toString()).append("\n");
            modelContent.append(required.toString()).append("\n");

            modelContent.append(modelTemplate);
            
            if(Master.DEBUG_FLAG)
            {
                try (FileWriter modelFile = new FileWriter(Calculation.outputModelPath)) {
                    modelFile.write(modelContent.toString());
                }
            }
            
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
            NeosJobXml jobXml = new NeosJobXml("milp", "XpressMP", "GAMS");
            jobXml.addParam("model", modelContent.toString());
            jobXml.addParam("email", "klee224@wisc.edu");
            NeosJob neosJob;
            while((neosJob = neosClient.submitJob(jobXml.toXMLString())) == null)
            {
                System.err.println("Error: failed to submit job to NEOS.");
            }
            
            System.out.println("Job ID: " + neosJob.getJobNo());
            System.out.println("Job password: " + neosJob.getJobPass());
            results = neosJob.getResult();
        }
        else
        {
            results = resultsBuilder.toString();
        }
        
        if(results.isEmpty())
        {
            System.err.println("Error submitting results to NEOS.");
            return null;
        }
        try (FileWriter solWriter = new FileWriter(Calculation.outputSolPath))
        {
            solWriter.write(results);
            solWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(WCConservation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        SolutionParser parser = new SolutionParser(results);
        if(parser.getModelStatusCode() != 8 && parser.getModelStatusCode() != 1)
            return null;
        
        cutCandidates.clear();
        SolutionData bCut = parser.getSymbol("cut", SolutionData.VAR, 2);
        
        for(SolutionRow sRow : bCut.getRows())
        {
            int level = sRow.getLevel().intValue();
            if(level == 1)
            {
                int xCoord = Integer.valueOf(sRow.getIndex(0));
                int yCoord = Integer.valueOf(sRow.getIndex(1));
                List<Integer> key = Arrays.asList(xCoord, yCoord);
                Patch cutPatch = candidateMap.get(key);
                if (cutPatch != null) {
                    cutCandidates.add(cutPatch);
                    Integer sValue = selectFreq.get(key);
                    if(sValue != null)
                        selectFreq.put(key, sValue + 1);
                    else
                        selectFreq.put(key, 1);
                }
            }
        }
        
        int uniqeCount = 0;
        for(Integer sFreq : selectFreq.values())
        {
            if(sFreq == 1) ++uniqeCount;
        }
        
        System.out.println("Current number of uniquely selected patches: " + uniqeCount);
        
        double totalCost = parser.getObjective();
        
        System.out.println("Total subsidy cost: " + totalCost);
        
        if(cutCandidates.size() < requiredHabitats)
        {
            try (FileWriter modelFile = new FileWriter(Calculation.outputModelPath)) {
                modelFile.write(modelContent.toString());
            }
            catch(IOException ioe)
            {
                
            }
        }
        
        return cutCandidates;
    }
    
    public PriorityQueue<Patch> OptimizeCutsScenario4()
    {
        String results;
        StringBuilder modelContent = new StringBuilder();
        HashMap<Integer, Patch> candidateIDMap = new HashMap<>();
        org.neos.gams.Set patchSet = new org.neos.gams.Set("M", "patches");
        
        Parameter subsidy = new Parameter("subsidy(M)", "subsidy cost");
        
        Scalar requiredPatchScalar = new Scalar("requiredPatches", "required number of patces", String.valueOf(requiredHabitats - alreadySuitable));
        
        for(Patch p : habitatCandidates)
        {
            String pID = String.valueOf(p.patchID);
            patchSet.addValue(pID);
            double subsidyCost = 250 - p.lumberProfit;
            if(subsidyCost < 0) subsidyCost = 0;
            subsidy.add(pID, String.valueOf(subsidyCost));
            candidateIDMap.put(p.patchID, p);
        }
        
        modelContent.append("$offdigit\n");
        modelContent.append(patchSet.toString()).append("\n");
        modelContent.append(requiredPatchScalar.toString());
        modelContent.append(subsidy.toString()).append("\n");
        modelContent.append(modelTemplateS4);
        
        try (FileWriter modelFile = new FileWriter(Calculation.outputModelPathS4)) {
            modelFile.write(modelContent.toString());
            modelFile.close();
        }
        catch(IOException ioe)
        {
            
        }
        
        NeosClient neosClient = new NeosClient(Calculation.NEOS_HOST, Calculation.NEOS_PORT);
        NeosJobXml jobXml = new NeosJobXml("milp", "gurobi", "GAMS");
        jobXml.addParam("model", modelContent.toString());
        jobXml.addParam("email", "ziliang@cs.wisc.edu");
        NeosJob neosJob;
        while ((neosJob = neosClient.submitJob(jobXml.toXMLString())) == null) {
            System.err.println("Error: failed to submit job to NEOS.");
        }
        
        System.out.println("Job ID: " + neosJob.getJobNo());
        System.out.println("Job password: " + neosJob.getJobPass());
        results = neosJob.getResult();
        
        SolutionParser parser = new SolutionParser(results);
        if (parser.getModelStatusCode() != 8 && parser.getModelStatusCode() != 1) {
            return null;
        }

        cutCandidates.clear();
        SolutionData bCut = parser.getSymbol("selected", SolutionData.VAR, 1);
        
        for (SolutionRow sRow : bCut.getRows()) {
            int level = sRow.getLevel().intValue();
            if (level == 1) {
                int pID = Integer.valueOf(sRow.getIndex(0));
                Patch cutPatch = candidateIDMap.get(pID);
                if (cutPatch != null) {
                    cutCandidates.add(cutPatch);
                }
            }
        }
        
        return cutCandidates;
    }
    
    public PriorityQueue<Patch> OptimizeCutsScenario5()
    {
        String results;
        StringBuilder modelContent = new StringBuilder();
        HashMap<Integer, Patch> candidateIDMap = new HashMap<>();
        org.neos.gams.Set patchSet = new org.neos.gams.Set("M", "patches");
        org.neos.gams.Set dimSet = new org.neos.gams.Set("N", "dimensions");
        
        Parameter subsidy = new Parameter("subsidy(M)", "subsidy cost");
        Parameter coord = new Parameter("coord(M,N)", "coordinates");
        
        dimSet.addValue("0");
        dimSet.addValue("1");
        
        for(Patch p : habitatCandidates)
        {
            String pID = String.valueOf(p.patchID);
            patchSet.addValue(pID);
            double subsidyCost = 250 - p.lumberProfit;
            if(subsidyCost < 0) subsidyCost = 0;
            subsidy.add(pID, String.valueOf(subsidyCost));
            candidateIDMap.put(p.patchID, p);
            coord.add(pID + ".0", String.valueOf(p.x));
            coord.add(pID + ".1", String.valueOf(p.y));
        }
        
        modelContent.append("$offdigit\n");
        modelContent.append(patchSet.toString()).append("\n");
        modelContent.append(dimSet.toString()).append("\n");
        modelContent.append(subsidy.toString()).append("\n");
        modelContent.append(coord.toString()).append("\n");
        modelContent.append(modelTemplateS5);
        
        try (FileWriter modelFile = new FileWriter(Calculation.outputModelPathS5)) {
            modelFile.write(modelContent.toString());
            modelFile.close();
        }
        catch(IOException ioe)
        {
            try (FileWriter modelFile = new FileWriter(Calculation.outputModelPath)) {
                    modelFile.write(modelContent.toString());
                }
            catch(IOException ioExcept)
            {
                
            }
        }
        
        return null;
    }
    
    public PriorityQueue<Patch> OptimizeCutsScenario1()
    {
        StringBuilder resultsBuilder = new StringBuilder();
        String results;
        StringBuilder modelContent = new StringBuilder();
        HashMap<Integer, Patch> candidateIDMap = new HashMap<>();

        try {
            org.neos.gams.Set patchSet = new org.neos.gams.Set("M", "patches");
            org.neos.gams.Set dimSet = new org.neos.gams.Set("N", "dimensions");

            Parameter subsidy = new Parameter("subsidy(M)", "subsidy cost");
            Parameter coord = new Parameter("coord(M,N)", "coordinates");

            dimSet.addValue("0");
            dimSet.addValue("1");

            for (Patch p : habitatCandidates) {
                String pID = String.valueOf(p.patchID);
                patchSet.addValue(pID);
                double subsidyCost = 250 - p.lumberProfit;
                if (subsidyCost < 0) {
                    subsidyCost = 0;
                }
                subsidy.add(pID, String.valueOf(subsidyCost));
                candidateIDMap.put(p.patchID, p);
                coord.add(pID + ".0", String.valueOf(p.x));
                coord.add(pID + ".1", String.valueOf(p.y));
            }
            Scalar minVal =
                    new Scalar(
                    "minVal",
                    "Minimally acceptable value",
                    String.valueOf(LumberCompany.MIN_VALUE));
            Scalar required =
                    new Scalar(
                    "requiredPatches",
                    "Required number of patches to cut",
                    String.valueOf(requiredHabitats - alreadySuitable));


            modelContent.append("$offdigit").append("\n");
            modelContent.append(patchSet.toString()).append("\n");
            modelContent.append(dimSet.toString()).append("\n");
            modelContent.append(subsidy.toString()).append("\n");
            modelContent.append(coord.toString()).append("\n");
            //modelContent.append(isCandidate.toString()).append("\n");
            modelContent.append(minVal.toString()).append("\n");
            modelContent.append(required.toString()).append("\n");
            modelContent.append(modelTemplateS1);
            
 //           if(Master.DEBUG_FLAG)
 //           {
                try (FileWriter modelFile = new FileWriter(Calculation.outputModelPathS1)) {
                    modelFile.write(modelContent.toString());
                    modelFile.close();
                }
 //           }
            
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
            NeosJobXml jobXml = new NeosJobXml("milp", "gurobi", "GAMS");
            jobXml.addParam("model", modelContent.toString());
            NeosJob neosJob;
            while((neosJob = neosClient.submitJob(jobXml.toXMLString())) == null)
            {
                System.err.println("Error: failed to submit job to NEOS.");
            }
            
            System.out.println("Job ID: " + neosJob.getJobNo());
            System.out.println("Job password: " + neosJob.getJobPass());
            results = neosJob.getResult();
        }
        else
        {
            results = resultsBuilder.toString();
        }
        
        if(results.isEmpty())
        {
            System.err.println("Error submitting results to NEOS.");
            return null;
        }
        try (FileWriter solWriter = new FileWriter(Calculation.outputSolPath))
        {
            solWriter.write(results);
            solWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(WCConservation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        SolutionParser parser = new SolutionParser(results);
        if(parser.getModelStatusCode() != 8 && parser.getModelStatusCode() != 1)
            return null;
        
        cutCandidates.clear();
        SolutionData bCut = parser.getSymbol("selected", SolutionData.VAR, 1);
        
        for (SolutionRow sRow : bCut.getRows()) {
            int level = sRow.getLevel().intValue();
            if (level == 1) {
                int pID = Integer.valueOf(sRow.getIndex(0));
                Patch cutPatch = candidateIDMap.get(pID);
                if (cutPatch != null) {
                    cutCandidates.add(cutPatch);
                }
            }
        }
        
        int uniqeCount = 0;
        for(Integer sFreq : selectFreq.values())
        {
            if(sFreq == 1) ++uniqeCount;
        }
        
        System.out.println("Current number of uniquely selected patches: " + uniqeCount);
        
        double totalCost = parser.getObjective();
        
        System.out.println("Total subsidy cost: " + totalCost);
        
        if(cutCandidates.size() < requiredHabitats)
        {
            try (FileWriter modelFile = new FileWriter(Calculation.outputModelPath)) {
                modelFile.write(modelContent.toString());
            }
            catch(IOException ioe)
            {
                
            }
        }
        
        return cutCandidates;
    }
}
