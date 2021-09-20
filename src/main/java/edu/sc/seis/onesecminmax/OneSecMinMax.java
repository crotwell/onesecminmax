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
    
    public OneSecMinMax(DataRecord dr) throws SeedFormatException, CodecException {
        Instant drStart = dr.getStartBtime().toInstant();
        double samplesPerSecond = dr.getSampleRate();
        if (dr.getHeader().getNumSamples() == 0) {
            throw new SeedFormatException("Record contains zero samples: "+dr);
        }
        
        DecompressedData decomp = dr.decompress();
        if (decomp.getType() != DecompressedData.INTEGER && decomp.getType() != DecompressedData.SHORT) {
            throw new UnsupportedCompressionType("Only integer data supprted by OneSecMinMax: "+decomp.getTypeString());
        }
        process(dr.getHeader().getCodes(), drStart, samplesPerSecond, decomp.getAsInt());
    }
    
    void process(String key, Instant drStart, double samplesPerSecond, int[] y) {
        //System.out.println("Process: "+key+" "+drStart+"  "+y.length+" sps"+samplesPerSecond);
        this.key = key;
        this.start = drStart.truncatedTo(ChronoUnit.SECONDS);
        Instant lastSampleTime = drStart.plus(Duration.ofSeconds(0, Math.round(1.0*(y.length-1)/samplesPerSecond*NANO_IN_SEC)));
        Duration drDuration = Duration.between(start, lastSampleTime);

        int numSeconds = (int)drDuration.getSeconds();
        if (drDuration.getNano() > 0) {numSeconds++;}
        if (lastSampleTime.equals(lastSampleTime.truncatedTo(ChronoUnit.SECONDS))) {numSeconds++;}
        //System.out.println("   numSec: "+numSeconds+"  "+y.length+" "+key);
        
        Duration offsetDur = Duration.between(start, drStart);
        int skipSamples = (int)Math.floor(offsetDur.getNano()*samplesPerSecond/NANO_IN_SEC);
        int firstSecSamples = 0;
        if (skipSamples != 0) {
            firstSecSamples = (int)Math.round(samplesPerSecond - skipSamples);
        }
        //System.out.println("firstSecSamples "+firstSecSamples+" samplesPerSecond "+samplesPerSecond+" offset "+offsetDur);

        minmax = new int[2*numSeconds];
        
        int idx = 0;
        if (firstSecSamples != 0) {
            int min = y[0];
            int max = y[0];
            for( int i=1; i < y.length && i< firstSecSamples; i++) {
                min = Math.min(min, y[i]);
                max = Math.max(max, y[i]);
                //System.out.println(i+" "+idx+"  m "+min+" "+max);
            }
            minmax[idx] = min;
            idx++;
            minmax[idx] = max;
            idx++;
        }
        int i=firstSecSamples;
        //System.out.println("Before while: "+i +"<"+ y.length +" && "+ idx +" < "+ minimum.length);
        while (i < y.length && idx < minmax.length) {
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
            minmax[idx] = min;
            idx++;
            minmax[idx] = max;
            idx++;
        }
    }
    
    public String[] keyAsNSLC() throws SeedFormatException {
        String[] split = key.split("\\.");
        if (split.length != 4) {
            throw new SeedFormatException("key does not look like Net.Sta.Loc.Chan: "+key);
        }
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }
    
    String key;
    Instant start;
    int[] minmax;

    public final static int NANO_IN_SEC = 1000000000;

    public boolean isContiguous(OneSecMinMax onesec) {
        Duration dur = Duration.between(start, onesec.start);
       // System.out.println(("one sec contiguous "+dur.getSeconds()+" "+dur.getNano()+"  "+minimum.length));
        return dur.getSeconds() == minmax.length/2-1 || dur.getSeconds() == minmax.length/2;
    }

    public void concat(OneSecMinMax onesec) {

        Duration dur = Duration.between(start, onesec.start);
        if (dur.getSeconds() == minmax.length/2-1) {
            // last second overlaps
            int[] tmpMinmax = new int[minmax.length+onesec.minmax.length-2];
            System.arraycopy(minmax, 0, tmpMinmax, 0, minmax.length-2);
            tmpMinmax[minmax.length-2] = Math.min(minmax[minmax.length-2], onesec.minmax[0]);
            tmpMinmax[minmax.length-1] = Math.max(minmax[minmax.length-1], onesec.minmax[1]);
            System.arraycopy(onesec.minmax, 2, tmpMinmax, minmax.length, onesec.minmax.length-2);
            this.minmax = tmpMinmax;
        } else if (dur.getSeconds() == minmax.length/2) {
            // no overlap
            int[] tmpMinmax = new int[minmax.length+onesec.minmax.length];
            System.arraycopy(minmax, 0, tmpMinmax, 0, minmax.length);
            System.arraycopy(onesec.minmax, 0, tmpMinmax, minmax.length, onesec.minmax.length);
            this.minmax = tmpMinmax;
        } else {
            throw new RuntimeException("one sec not contiguous "+dur.getSeconds()+" "+dur.getNano());
        }
        
    }
}
