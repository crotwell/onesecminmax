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
        int samplesRemaining = onesec.minimum.length;
        int[] minData = onesec.minimum;
        int[] maxData = onesec.maximum;
        int offset = 0;
        while (samplesRemaining > 0) {
            DataRecord minRecord = createEmptyDataRecord(nslc, start, seq++, false);
            DataRecord maxRecord = createEmptyDataRecord(nslc, start, seq++, true);

            SteimFrameBlock steimDataMin = Steim2.encode(minData, STEIM_FRAME_IN_512);
            SteimFrameBlock steimDataMax = Steim2.encode(maxData, STEIM_FRAME_IN_512);
            if (steimDataMin.getNumSamples() < steimDataMax.getNumSamples()) {
                int[] lessSamples = new int[steimDataMin.getNumSamples()];
                System.arraycopy(maxData, 0, lessSamples, 0, lessSamples.length);
                steimDataMax = Steim2.encode(lessSamples, STEIM_FRAME_IN_512);
            } else if (steimDataMin.getNumSamples() > steimDataMax.getNumSamples()) {
                int[] lessSamples = new int[steimDataMax.getNumSamples()];
                System.arraycopy(minData, 0, lessSamples, 0, lessSamples.length);
                steimDataMin = Steim2.encode(lessSamples, STEIM_FRAME_IN_512);
            }
            

            try {
                minRecord.setData(steimDataMin.getEncodedData());
                minRecord.getHeader().setNumSamples((short)steimDataMin.getNumSamples());
                maxRecord.setData(steimDataMax.getEncodedData());
                maxRecord.getHeader().setNumSamples((short)steimDataMax.getNumSamples());
                out.add(minRecord);
                out.add(maxRecord);
                samplesRemaining -= steimDataMin.getNumSamples();
                start = start.plusSeconds(steimDataMin.getNumSamples());
                offset += steimDataMin.getNumSamples();
                int[] tempData = new int[samplesRemaining];
                System.arraycopy(maxData, steimDataMax.getNumSamples(), tempData, 0, tempData.length);
                maxData = tempData;
                tempData = new int[samplesRemaining];
                System.arraycopy(minData, steimDataMin.getNumSamples(), tempData, 0, tempData.length);
                minData = tempData;
            } catch (IOException e) {
                throw new SeedFormatException(e);
            }
        }
        return out;
    }
    
    static DataRecord createEmptyDataRecord(String[] nslc, Instant start, int seq, boolean isMax) throws SeedFormatException {
        DataHeader header = new DataHeader(seq, 'D', false);
        String chan = (isMax ? "LX" : "LI") + nslc[3].charAt(2);
        header.setStationIdentifier(nslc[1]);
        header.setChannelIdentifier(chan); // L, X or I, orient
        header.setNetworkCode(nslc[0]);
        header.setLocationIdentifier(nslc[3].substring(0, 2)); // or should be MI, MA
        header.setSampleRateFactor((short) 1);
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
