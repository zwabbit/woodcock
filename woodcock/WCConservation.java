/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package woodcock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import org.neos.gams.Parameter;
import org.neos.gams.Scalar;
import org.neos.gams.SolutionParser;

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
    private PriorityQueue<Patch> habitatCandidates;
    public HashMap<List<Integer>, Patch> candidateMap;

    public WCConservation(int forestPatchSize) {
        Comparator<Patch> comparator = new PatchLumberComparator();
        habitatCandidates = new PriorityQueue<>(forestPatchSize, comparator);
        candidateMap = new HashMap<>();

        String pathEnv = System.getenv("PATH");
        hasGams = pathEnv.contains("GAMS");
        if (hasGams == false) {
            hasGams = pathEnv.contains("gams");
        }
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
                    List<Integer> key = Arrays.asList(p.x, p.y);
                    candidateMap.put(key, p);
                }
            }
            rangeDevelop -= 100;
        }
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

    public LinkedList<Patch> optimizeCuts() {
        LinkedList<Patch> selectedPatches = new LinkedList<>();
        String results = new String();

        try {
            if (hasGams) {
                Scalar xScalar = new Scalar("x", "X coordinate", "0.." + Master.columns);
                Scalar yScalar = new Scalar("y", "Y coordinate", "0.." + Master.rows);
                Parameter value = new Parameter("patchValue(x,y)", "Patch values");
                Parameter isCandidate = new Parameter("isCandidate(x,y)", "Is a candidate");
                
                for(Patch candidate : habitatCandidates)
                {
                    value.add(candidate.x + "." + candidate.y, String.valueOf(candidate.calcValue()));
                    isCandidate.add(candidate.x + "." + candidate.y, "1");
                }
                
                ProcessBuilder pBuilder = new ProcessBuilder("gams");
                pBuilder.redirectOutput();
                Process gamsProcess = pBuilder.start();
                BufferedReader gamsReader = new BufferedReader(new InputStreamReader(gamsProcess.getInputStream()));

                
                String line;
                while((line = gamsReader.readLine()) != null)
                {
                    results.concat(line + "\n");
                    if (gamsProcess.exitValue() == 0) {
                        
                    }
                }
            }
        } catch(IOException ex) {
            System.err.println("Error: running GAMS locally failed.");
        }
        
        SolutionParser parser = new SolutionParser(results);
        
        return selectedPatches;
    }
}
