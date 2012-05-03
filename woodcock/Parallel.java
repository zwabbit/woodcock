/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package woodcock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Z98
 */
public class Parallel {
    static final int CPUs = Runtime.getRuntime().availableProcessors();
    
    public interface Each {
        void run(int i);
    }
    
    public static void withIndex(int start, int stop, final Each body) {
        int chunksize = (stop - start + CPUs - 1) / CPUs;
        int loops = (stop - start + chunksize - 1) / chunksize;
        ExecutorService executor = Executors.newFixedThreadPool(CPUs);
        final CountDownLatch latch = new CountDownLatch(loops);
        for (int i = start; i < stop;) {
            final int lo = i;
            i += chunksize;
            final int hi = (i < stop) ? i : stop;
            executor.submit(new Runnable() {

                public void run() {
                    for (int i = lo; i < hi; i++) {
                        body.run(i);
                    }
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
        executor.shutdown();
    }
}
