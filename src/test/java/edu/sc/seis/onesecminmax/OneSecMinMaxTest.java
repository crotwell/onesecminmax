package edu.sc.seis.onesecminmax;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

public class OneSecMinMaxTest {

    @Test
    public void testProcessOnSec() {
        // even second
        String iso = "2017-12-03T10:15:30.00Z";

        Instant start = Instant.parse(iso);
        // even second
        int sps = 10;
        int numSeconds = 2;
        int[] y = new int[numSeconds*sps];
        for (int i = 0; i < y.length; i++) {
            y[i] = 1+ (i % sps);
        }
        for (int i = 0; i< sps && i < y.length; i++) {
            System.out.println(i+" "+y[i]);
        }
        OneSecMinMax oneSec = new OneSecMinMax("FAKE", start, sps, y);
        assertEquals("start", Instant.parse(iso), oneSec.start);
        assertEquals("len", numSeconds, oneSec.minmax.length/2);
        for (int i = 0; i < oneSec.minmax.length; i+=2) {
            assertEquals("min idx "+i, 1, oneSec.minmax[i]);
            assertEquals("max idx "+i, sps, oneSec.minmax[i+1]);
        }
    }

    @Test
    public void testProcessOffSec() {
        // even second
        String iso = "2017-12-03T10:15:30.00Z";
        int offsetMillis = 101;
        Instant start = Instant.parse(iso).plusMillis(offsetMillis);
        // off even second
        int sps = 10;
        int numSeconds = 120;
        int[] y = new int[numSeconds*sps];
        for (int i = 0; i < y.length; i++) {
            y[i] = 1+ (i % sps);
        }
        for (int i = 0; i< sps && i < y.length; i++) {
            System.out.println(i+" "+y[i]);
        }
        OneSecMinMax oneSec = new OneSecMinMax("FAKE", start, sps, y);
        assertEquals("start", Instant.parse(iso), oneSec.start);
        assertEquals("len", numSeconds+1, oneSec.minmax.length/2);
        // first sec special
        assertEquals("min idx 0", 1, oneSec.minmax[0]);
        assertEquals("max idx 0", sps-1, oneSec.minmax[1]);
        int i;
        for (i = 2; i < oneSec.minmax.length-2; i+=2) {
            assertEquals("min idx "+i, 1, oneSec.minmax[i]);
            assertEquals("max idx "+i, sps, oneSec.minmax[i+1]);
        }
        // last sec special
        assertEquals("min idx "+i, sps, oneSec.minmax[i]);
        assertEquals("max idx "+i, sps, oneSec.minmax[i+1]);
    }
    


    @Test
    public void testProcessOffSec999() {
        // even second
        String iso = "2017-12-03T10:15:30.00Z";
        int offsetMillis = 999;
        Instant start = Instant.parse(iso).plusMillis(offsetMillis);
        // off even second
        int sps = 10;
        int numSeconds = 120;
        int[] y = new int[numSeconds*sps];
        for (int i = 0; i < y.length; i++) {
            y[i] = 1+ (i % sps);
        }
        for (int i = 0; i< sps && i < y.length; i++) {
            System.out.println(i+" "+y[i]);
        }
        OneSecMinMax oneSec = new OneSecMinMax("FAKE", start, sps, y);
        assertEquals("start", Instant.parse(iso), oneSec.start);
        assertEquals("len", numSeconds+1, oneSec.minmax.length/2);
        
        // first sec special
        assertEquals("min idx 0", 1, oneSec.minmax[0]);
        assertEquals("max idx 0", 1, oneSec.minmax[1]);
        int i;
        for (i = 2; i < oneSec.minmax.length-2; i+=2) {
            assertEquals("min idx "+i, 1, oneSec.minmax[i]);
            assertEquals("max idx "+i, sps, oneSec.minmax[i+1]);
        }
        // last sec special
        assertEquals("min idx "+i, 2, oneSec.minmax[i]);
        assertEquals("max idx "+i, sps, oneSec.minmax[i+1]);
    }
    
    @Test
    public void testLastSampTime() {

        String iso = "2017-12-03T10:15:30.00Z";
        int offsetMillis = 101;
        Instant drStart = Instant.parse(iso).plusMillis(offsetMillis);
        // off even second
        int sps = 10;
        int numSeconds = 120;
        int[] y = new int[numSeconds*sps];
        double samplesPerSecond = sps;
        
        Instant lastSampleTime = drStart.plus(Duration.ofSeconds(0, Math.round(1.0*(y.length-1)/samplesPerSecond*OneSecMinMax.NANO_IN_SEC)));
        assertEquals("last samp time ", "2017-12-03T10:17:30.001Z", ""+lastSampleTime);
    }
    

    @Test
    public void testContiguous() {
        // even second
        String iso = "2017-12-03T10:15:30.00Z";

        Instant start = Instant.parse(iso);
        // even second
        int sps = 10;
        int numSeconds = 2;
        int[] y = new int[numSeconds*sps];
        for (int i = 0; i < y.length; i++) {
            y[i] = 1+ (i % sps);
        }
        for (int i = 0; i< sps && i < y.length; i++) {
            System.out.println(i+" "+y[i]);
        }
        OneSecMinMax prev = new OneSecMinMax("FAKE", start, sps, y);
        OneSecMinMax next = new OneSecMinMax("FAKE", start.plusSeconds(numSeconds), sps, y);
        assertTrue("contiguous "+"prev: "+prev.start+"  "+prev.minmax.length/2+"  next: "+next.start, prev.isContiguous(next));

        Instant nextStart = Instant.parse("2017-12-03T10:15:31.100Z");
        next = new OneSecMinMax("FAKE", nextStart, sps, y);
        assertTrue("contiguous "+"prev: "+prev.start+"  "+prev.minmax.length/2+"  next: "+next.start, prev.isContiguous(next));
        
        nextStart = Instant.parse("2017-12-03T10:15:32.100Z");
        next = new OneSecMinMax("FAKE", nextStart, sps, y);
        assertTrue("contiguous "+"prev: "+prev.start+"  "+prev.minmax.length/2+"  next: "+next.start, prev.isContiguous(next));
        
        nextStart = Instant.parse("2017-12-03T10:15:33.100Z");
        next = new OneSecMinMax("FAKE", nextStart, sps, y);
        assertFalse("contiguous "+"prev: "+prev.start+"  "+prev.minmax.length/2+"  next: "+next.start, prev.isContiguous(next));

        nextStart = Instant.parse("2017-12-03T10:15:34.100Z");
        next = new OneSecMinMax("FAKE", nextStart, sps, y);
        assertFalse("contiguous "+"prev: "+prev.start+"  "+prev.minmax.length/2+"  next: "+next.start, prev.isContiguous(next));
        
    }

}
