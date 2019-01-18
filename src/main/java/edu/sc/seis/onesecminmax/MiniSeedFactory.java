package edu.sc.seis.onesecminmax;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

import edu.iris.dmc.seedcodec.B1000Types;
import edu.iris.dmc.seedcodec.Steim2;
import edu.iris.dmc.seedcodec.SteimException;
import edu.iris.dmc.seedcodec.SteimFrameBlock;
import edu.sc.seis.seisFile.mseed.Blockette1000;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataHeader;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;

public class MiniSeedFactory {

    public static ArrayList<DataRecord> createMiniseed(OneSecMinMax onesec) throws SeedFormatException, SteimException {
        ArrayList<DataRecord> out = new ArrayList<DataRecord>();
        int seq = 1;
        String[] nslc = onesec.keyAsNSLC();
        Instant start = onesec.start;
        int samplesRemaining = onesec.minmax.length;
        int[] minMaxData = onesec.minmax;
        int offset = 0;
        while (samplesRemaining > 0) {
            DataRecord minMaxRecord = createEmptyDataRecord(nslc, start, seq++);

            SteimFrameBlock steimDataMinMax = Steim2.encode(minMaxData, STEIM_FRAME_IN_512);
            if (steimDataMinMax.getNumSamples() % 2 == 1) {
                // don't want min in one record and max in next
                int[] lessSamples = new int[steimDataMinMax.getNumSamples()-1];
                System.arraycopy(minMaxData, 0, lessSamples, 0, lessSamples.length);
                steimDataMinMax = Steim2.encode(lessSamples, STEIM_FRAME_IN_512);
            }
            

            try {
                minMaxRecord.setData(steimDataMinMax.getEncodedData());
                minMaxRecord.getHeader().setNumSamples((short)steimDataMinMax.getNumSamples());
                out.add(minMaxRecord);
                samplesRemaining -= steimDataMinMax.getNumSamples();
                start = start.plusSeconds(steimDataMinMax.getNumSamples()/2);
                offset += steimDataMinMax.getNumSamples();
                int[] tempData = new int[samplesRemaining];
                System.arraycopy(minMaxData, steimDataMinMax.getNumSamples(), tempData, 0, tempData.length);
                minMaxData = tempData;
            } catch (IOException e) {
                throw new SeedFormatException(e);
            }
        }
        return out;
    }
    
    static DataRecord createEmptyDataRecord(String[] nslc, Instant start, int seq) throws SeedFormatException {
        DataHeader header = new DataHeader(seq, 'D', false);
        String chan = "LX" + nslc[3].charAt(2);
        if (nslc[3].charAt(1) == 'N') {
            // strong motion HNZ -> LYZ
            chan = "LY" + nslc[3].charAt(2);
        }
        header.setNetworkCode(nslc[0]);
        header.setStationIdentifier(nslc[1]);
        header.setLocationIdentifier(nslc[2]); 
        header.setChannelIdentifier(chan); // L, X, orient
        header.setSampleRateFactor((short) 2);  // 2 samples per second, min and max
        header.setSampleRateMultiplier((short) 1);
        Btime btime = new Btime(start);
        header.setStartBtime(btime);

        DataRecord record = new DataRecord(header);
        Blockette1000 blockette1000 = new Blockette1000();
        blockette1000.setEncodingFormat((byte)B1000Types.STEIM2);
        blockette1000.setWordOrder(Blockette1000.SEED_BIG_ENDIAN);
        blockette1000.setDataRecordLength(RECORD_SIZE_512_POWER);
        record.addBlockette(blockette1000);
        return record;
    }

    public static final byte RECORD_SIZE_4096_POWER = 12;

    public static final int STEIM_FRAME_IN_4096 = 63;
    
    public static int RECORD_SIZE_4096 = (int)Math.pow(2, RECORD_SIZE_4096_POWER);

    public static final byte RECORD_SIZE_1024_POWER = 10;

    public static int RECORD_SIZE_1024 = (int)Math.pow(2, RECORD_SIZE_1024_POWER);
    
    public static final byte RECORD_SIZE_512_POWER = 9;

    public static final int STEIM_FRAME_IN_512 = 7;
    
    public static int RECORD_SIZE_512 = (int)Math.pow(2, RECORD_SIZE_512_POWER);
    
    public static final byte RECORD_SIZE_256_POWER = 8;

    public static int RECORD_SIZE_256 = (int)Math.pow(2, RECORD_SIZE_256_POWER);

}
