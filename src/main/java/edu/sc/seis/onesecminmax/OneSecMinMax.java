package edu.sc.seis.onesecminmax;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.iris.dmc.seedcodec.UnsupportedCompressionType;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;

public class OneSecMinMax {

    public OneSecMinMax(String key, Instant drStart, double samplesPerSecond, int[] y) {
        process(key, drStart, samplesPerSecond, y);
    }
    
    public OneSecMinMax(DataRecord dr) throws SeedFormatException, UnsupportedCompressionType, CodecException {
        Instant drStart = dr.getStartBtime().toInstant();
        double samplesPerSecond = dr.getSampleRate();
        
        
        DecompressedData decomp = dr.decompress();
        if (decomp.getType() != DecompressedData.INTEGER && decomp.getType() != DecompressedData.SHORT) {
            throw new UnsupportedCompressionType("Only integer data supprted by OneSecMinMax: "+decomp.getTypeString());
        }
        process(dr.getHeader().getCodes(), drStart, samplesPerSecond, decomp.getAsInt());
    }
    
    void process(String key, Instant drStart, double samplesPerSecond, int[] y) {
        System.out.println("Process: "+key+" "+drStart+"  "+y.length+" sps"+samplesPerSecond);
        this.key = key;
        this.start = drStart.truncatedTo(ChronoUnit.SECONDS);
        Instant lastSampleTime = drStart.plus(Duration.ofSeconds(0, Math.round(1.0*(y.length-1)/samplesPerSecond*NANO_IN_SEC)));
        Duration drDuration = Duration.between(start, lastSampleTime);

        int numSeconds = (int)drDuration.getSeconds();
        if (drDuration.getNano() > 0) {numSeconds++;}
        if (lastSampleTime.equals(lastSampleTime.truncatedTo(ChronoUnit.SECONDS))) {numSeconds++;}
        System.out.println("   numSec: "+numSeconds+"  "+y.length+" "+key);
        
        Duration offsetDur = Duration.between(start, drStart);
        int skipSamples = (int)Math.floor(offsetDur.getNano()*samplesPerSecond/NANO_IN_SEC);
        int firstSecSamples = 0;
        if (skipSamples != 0) {
            firstSecSamples = (int)Math.round(samplesPerSecond - skipSamples);
        }
        //System.out.println("firstSecSamples "+firstSecSamples+" samplesPerSecond "+samplesPerSecond+" offset "+offsetDur);

        minimum = new int[numSeconds];
        maximum = new int[numSeconds];
        
        int idx = 0;
        if (firstSecSamples != 0) {
            int min = y[0];
            int max = y[0];
            for( int i=1; i< firstSecSamples; i++) {
                min = Math.min(min, y[i]);
                max = Math.max(max, y[i]);
                //System.out.println(i+" "+idx+"  m "+min+" "+max);
            }
            minimum[idx] = min;
            maximum[idx] = max;
            idx++;
        }
        int i=firstSecSamples;
        //System.out.println("Before while: "+i +"<"+ y.length +" && "+ idx +" < "+ minimum.length);
        while (i < y.length && idx < minimum.length) {
            int min = y[i];
            int max = y[i];
            i++;
            for (int j=1; i<y.length && j<samplesPerSecond; j++) {
                min = Math.min(min, y[i]);
                max = Math.max(max, y[i]);
                //System.out.println(i+" "+idx+"  m "+min+" "+max);
                i++;
            }
            //System.out.println(i+" minmax "+idx+" "+min+" "+max);
            minimum[idx] = min;
            maximum[idx] = max;
            idx++;
        }
    }
    
    String key;
    Instant start;
    int[] minimum;
    int[] maximum;

    public final static int NANO_IN_SEC = 1000000000;

    public boolean isContiguous(OneSecMinMax onesec) {
        Duration dur = Duration.between(start, onesec.start);
       // System.out.println(("one sec contiguous "+dur.getSeconds()+" "+dur.getNano()+"  "+minimum.length));
        return dur.getSeconds() == minimum.length-1 || dur.getSeconds() == minimum.length;
    }

    public void concat(OneSecMinMax onesec) {

        Duration dur = Duration.between(start, onesec.start);
        if (dur.getSeconds() == minimum.length-1) {
            // last second overlaps
            int[] min = new int[minimum.length+onesec.minimum.length-1];
            int[] max = new int[minimum.length+onesec.minimum.length-1];
            System.arraycopy(minimum, 0, min, 0, minimum.length);
            System.arraycopy(maximum, 0, max, 0, maximum.length);
            min[minimum.length-1] = Math.min(min[minimum.length-1], onesec.minimum[0]);
            max[maximum.length-1] = Math.max(max[maximum.length-1], onesec.maximum[0]);
            System.arraycopy(onesec.minimum, 1, min, minimum.length, onesec.minimum.length-1);
            System.arraycopy(onesec.maximum, 1, max, maximum.length, onesec.maximum.length-1);
            this.minimum = min;
            this.maximum = max;
        } else if (dur.getSeconds() == minimum.length) {
            // no overlap
            int[] min = new int[minimum.length+onesec.minimum.length];
            int[] max = new int[minimum.length+onesec.minimum.length];
            System.arraycopy(minimum, 0, min, 0, minimum.length);
            System.arraycopy(maximum, 0, max, 0, maximum.length);
            System.arraycopy(onesec.minimum, 0, min, minimum.length, onesec.minimum.length);
            System.arraycopy(onesec.maximum, 0, max, maximum.length, onesec.maximum.length);
            this.minimum = min;
            this.maximum = max;
        } else {
            throw new RuntimeException("one sec not contiguous "+dur.getSeconds()+" "+dur.getNano());
        }
        
    }
}
