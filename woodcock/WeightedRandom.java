/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package woodcock;

import java.util.Random;
import java.util.TreeMap;

/**
 *
 * @author Z98
 */
public class WeightedRandom<E> {
    private final TreeMap<Double, E> map = new TreeMap<Double, E>();
    private final Random rand;
    private double total = 0;
    
    public WeightedRandom()
    {
        rand = new Random();
    }
    
    public void add(double weight, E result)
    {
        if(weight <= 0) return;
        total += weight;
        map.put(total, result);
    }
    
    public E next()
    {
        double value = rand.nextDouble() * total;
        return map.ceilingEntry(value).getValue();
    }
}
