/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package woodcock;

import java.util.Random;

/**
 *
 * @author Z98
 */
public class Patch {
    public int x, y;
    public int landCover, waterDepth, roadLength, soil, canopy, age, landing;
    public double[] trees = null;
    public static Random rand = null;
    
    double[][] growthMatrix = null;
    double ingrowthMatrix;
    
    public Patch(int x, int y)
    {
        this.x = x;
        this.y = y;
        trees = new double[12];
        if(rand == null) rand = new Random();
    }
    
    public Patch(int x, int y, int landCover, int waterDepth, int roadLength, int soil, int canopy)
    {
        this(x, y);
        this.landCover = landCover;
        this.waterDepth = waterDepth;
        this.soil = soil;
        this.canopy = canopy;
    }
    
    public void generateTrees(int diameter)
    {
        double targetBasel = rand.nextInt(51) + 60;
        double currentBasel = 0;
        /*
         * Reenable after we get that negative exponential
         * diameter code implemented.
         */
        //int normOrNot = rand.nextInt(2);
        int normOrNot = 1;
        while(currentBasel < targetBasel)
        {
            if(normOrNot == 1)
            {
                double x = (double)rand.nextInt(101) / 100.0;
                double y = (double)rand.nextInt(101) / 100.0;
                double sD = x/(x+y) * diameter;
                int treeSize = rand.nextInt(13);
                ++trees[treeSize];
                int treeDia = (treeSize + 1) * 2;
                currentBasel += Calculation.BA * Math.pow(treeDia, 2);
            }
            else
            {
                
            }
        }
    }
    
    public void growTrees()
    {
        if(growthMatrix == null)
        {
            switch(landCover)
            {
                case 141:
                    growthMatrix = Calculation.decGrowthMatrix;
                    ingrowthMatrix = Calculation.decIngrowth;
                    break;
                case 142:
                    growthMatrix = Calculation.conGrowthMatrix;
                    ingrowthMatrix = Calculation.conIngrowth;
                    break;
                case 143:
                    growthMatrix = Calculation.mixGrowthMatrix;
                    ingrowthMatrix = Calculation.mixIngrowth;
            }
        }
        
        double nextGrowth[] = new double[12];
        for(int y = 0; y < 12; y++)
        {
            nextGrowth[y] = 0;
            for(int x = 0; x < 12; x++)
            {
                nextGrowth[y] += trees[x] * growthMatrix[y][x];
            }
            
            if(y == 0) nextGrowth[y] += ingrowthMatrix;
        }
        
        trees = nextGrowth;
    }
    
    public double calcValue()
    {
        return 0;
    }
}
