/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package woodcock;

import java.io.File;
import java.util.Random;

/**
 *
 * @author Z98
 */
public class Calculation {
    public static final String NEOS_HOST = "neos-server.org";
    public static final String NEOS_PORT = "3332";
    
    public static final String inputTemplatePath = System.getProperty("user.dir") + File.pathSeparator + "INPUT.gms";
    public static final String outputModelPath = System.getProperty("user.dir") + File.pathSeparator + "MODEL.gms";
    
    public static double conDefaultTreeWeight[] = {0.185236008, 0.258728096, 0.556035896};
    public static double decDefaultTreeWeight[] = {0.2, 0.37, 0.43};
    public static double mixDefaultTreeWeight[] = {0.224804403, 0.350450241, 0.424745355};
    
    public static int defaultTreeSize[] = {5, 8, 10};
    
    public static double conLessTreeWeight[] = {0.372470808,0.216336587,0.104601339,0.107220868,0.063811891,0.053101396,0.079521954,0.002935158};
    public static double decLessTreeWeight[] = {0.473433437,0.271229523,0.127051152,0.068338135,0.029715297,0.011826016,0.01840644,0};
    public static double mixLessTreeWeight[] = {0.369033307,0.232593812,0.114078886,0.10555826,0.061401365,0.046739204,0.068313732,0.002281434};
    
    public static int lessTreeSize[] = {0,2,4,6,8,10,15,20};
    
    public static double conUpgrowth[] = new double[12];
    public static double conMortality[] = new double[12];
    public static double conIngrowth;
    
    public static double decUpgrowth[] = new double[12];
    public static double decMortality[] = new double[12];
    public static double decIngrowth;
    
    public static double mixUpgrowth[] = new double[12];
    public static double mixMortality[] = new double[12];
    public static double mixIngrowth;
    
    public static double conGrowthMatrix[][] = new double[12][];
    public static double decGrowthMatrix[][] = new double[12][];
    public static double mixGrowthMatrix[][] = new double[12][];
    
    public static double BA = 0.005454;
    public static int sConstant = 80;
    
    public static Random rand = new Random();
    
    public static WeightedRandom<Integer> conDefRand = new WeightedRandom<>();
    public static WeightedRandom<Integer> decDefRand = new WeightedRandom<>();
    public static WeightedRandom<Integer> mixDefRand = new WeightedRandom<>();
        
    public static WeightedRandom<Integer> lessConDefRand = new WeightedRandom<>();
    public static WeightedRandom<Integer> lessDecDefRand = new WeightedRandom<>();
    public static WeightedRandom<Integer> lessMixDefRand = new WeightedRandom<>();
    
    public static double[] baAge = {
        0,
        0.10040239017503998,
        0.20586737742119446,
        0.31634904789653134,
        0.43180388422437715,
        0.55219072348986,
        0.6774707152108608,
        0.8076072792848064,
        0.9425660639109603,
        1.0823149034862578,
        1.22682377647127,
        1.3760647632215994,
        1.5300120037788212,
        1.6886416556140253,
        1.85193185131593,
        2.0198626562144057,
        2.1924160259289223,
        2.369575763829833,
        2.5513274783984565,
        2.7376585404694205,
        2.9285580403358358,
        3.1240167446942615,
        3.3240270534022995,
        3.528582956016948,
        3.7376799880765454,
        3.951315187083367,
        4.169487048137823,
        4.392195479168692,
        4.619441755697249,
        4.8512284750664865,
        5.0875595100601325,
        5.328439961829865,
        5.573876112043414,
        5.8238753741609575,
        6.078446243742722,
        6.337598247687234,
        6.601341892296851,
        6.8696886100659365,
        7.142650705086627,
        7.420241296968188,
        7.702474263168327,
        7.9893641796385335,
        8.280926259690647,
        8.577176290998397,
        8.878130570655495,
        9.183805838221156,
        9.49421920669425,
        9.80938809136927,
        10.129330136539647,
        10.454063140028152,
        10.783604975538493
    };
    
    public static void initializeWeightedRandom()
    {
        for(int index = 0; index < 3; index++)
        {
            conDefRand.add(conDefaultTreeWeight[index], defaultTreeSize[index]);
            decDefRand.add(decDefaultTreeWeight[index], defaultTreeSize[index]);
            mixDefRand.add(mixDefaultTreeWeight[index], defaultTreeSize[index]);
        }
        
        for(int index = 0; index < 8; index++)
        {
            lessConDefRand.add(conLessTreeWeight[index], lessTreeSize[index]);
            lessDecDefRand.add(decLessTreeWeight[index], lessTreeSize[index]);
            lessMixDefRand.add(mixLessTreeWeight[index], lessTreeSize[index]);
        }
    }
    
    public static void initializeGrowth()
    {
        for(int index = 0; index < 12; index++)
        {
            int diameter = (index + 1) * 2;
            
            conUpgrowth[index] = 0.0069 - 0.0001 * 0.005454 * Math.pow(diameter, 2) + 0.0059 * diameter - 0.0002 * Math.pow(diameter, 2);
            conMortality[index] = 0.0418 - 0.0009 * diameter;
            
            
            decUpgrowth[index] = 0.0164 - 0.0001 * 0.005454 * Math.pow(diameter, 2) + 0.0055 * diameter - 0.0002 * Math.pow(diameter, 2);
            decMortality[index] = 0.0336 - 0.0018 * diameter + 0.0001 * Math.pow(diameter, 2) - 0.00002 * sConstant * diameter;
            
            
            mixUpgrowth[index] = 0.0134 - 0.0002 * 0.005454 * Math.pow(diameter, 2) + 0.0051 * diameter - 0.0002 * Math.pow(diameter, 2) + 0.00002 * sConstant * diameter;
            mixMortality[index] = 0.0417 - 0.0033 * diameter + 0.0001 * Math.pow(diameter, 2);
            
        }
        
        conIngrowth = 7.622 - 0.059 * 0.005454 * 4;
        decIngrowth = 18.187 - 0.097 * 0.005454 * 4;
        decIngrowth = 4.603 - 0.035 * 0.005454 * 4;
        
        for(int index = 0; index < 11; index++)
        {
            double conRow[] = new double[12];
            double decRow[] = new double[12];
            double mixRow[] = new double[12];
            if(index == 0)
            {
                conRow[index] = (1 - conMortality[index]) * (1 - conUpgrowth[index]);
                decRow[index] = (1 - decMortality[index]) * (1 - decUpgrowth[index]);
                mixRow[index] = (1 - mixMortality[index]) * (1 - mixUpgrowth[index]);
            }
            else if(index == 11)
            {
                mixRow[index] = (1 - mixMortality[index]);
            }
            else
            {
                conRow[index] = (1 - conMortality[index]) * (1 - conUpgrowth[index]);
                conRow[index - 1] = (1 - conMortality[index - 1]) * conUpgrowth[index - 1];
                
                decRow[index] = (1 - decMortality[index]) * (1 - decUpgrowth[index]);
                decRow[index - 1] = (1 - decMortality[index - 1]) * decUpgrowth[index - 1];
                
                mixRow[index] = (1 - mixMortality[index]) * (1 - mixUpgrowth[index]);
                mixRow[index - 1] = (1 - mixMortality[index - 1]) * mixUpgrowth[index - 1];
            }
            
            conGrowthMatrix[index] = conRow;
            decGrowthMatrix[index] = decRow;
            mixGrowthMatrix[index] = mixRow;
        }
        
        double lastConRow[] = new double[12];
        lastConRow[11] = 1 - conMortality[11];
        conGrowthMatrix[11] = lastConRow;
        
        double lastDecRow[] = new double[12];
        lastDecRow[11] = 1 - decMortality[11];
        decGrowthMatrix[11] = lastDecRow;
        
        double lastMixRow[] = new double[12];
        lastMixRow[11] = 1 - mixMortality[11];
        mixGrowthMatrix[11] = lastMixRow;
    }
}
