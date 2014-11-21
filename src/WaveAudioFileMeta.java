import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by ed on 12/11/14.
 */
public class WaveAudioFileMeta {

    String filename;
//    Chunk header
    String chunkID;
    long chunkSize;
    String format;

//    fmt subchunk1
    String subchunk1ID;
    int subchunk1Size;
    int audioFormat;
    int numChannels;
    int sampleRate;
    int byteRate;
    int blockAlign;
    int bitsPerSample;
    int        extraParamSize;
    String extraParams;

//                    data subchunk
    String subchunk2ID;
    long    subchunk2Size;
    byte[]    data;
    byte[] headerData;

    // This is the position where the sound data starts in the wave file
    int dataStart;
    int totalBytes;

    public WaveAudioFileMeta(String filename) {
        this.filename = filename;

        File f = new File(filename);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);


        byte[] riffHeader = new byte[12];
        byte[] fmtSubchunk = new byte[24];
        byte[] dataSubchunk = new byte[100];


        byte[] dataSize = new byte[4];
        fis.read(riffHeader);
        chunkID = printBytes(riffHeader, 0, 4);
        chunkSize = printLEBytes(riffHeader, 4, 4);
        format = printBytes(riffHeader, 8, 4);

        fis.read(fmtSubchunk);
        subchunk1ID = printBytes(fmtSubchunk, 0, 4);
        subchunk1Size = (int) printLEBytes(fmtSubchunk, 4, 4);
        audioFormat = (int) printLEBytes(fmtSubchunk, 8, 2);
        numChannels = (int) printLEBytes(fmtSubchunk, 10, 2);
        sampleRate = (int) printLEBytes(fmtSubchunk, 12, 4);
        byteRate = (int) printLEBytes(fmtSubchunk, 16, 4);
        blockAlign = (int) printLEBytes(fmtSubchunk, 20, 2);
        bitsPerSample = (int) printLEBytes(fmtSubchunk, 22, 2);

        fis.read(dataSubchunk);
            totalBytes = (int) chunkSize + 8;
        for (int i = 0; i < dataSubchunk.length -4  && dataStart == 0; i++) {
            byte[] dataStr = new byte[4];
            for (int j = 0; j < 4; j++) {
                dataStr[j] = dataSubchunk[i+j];
            }
            if (new String(dataStr, "UTF-8").equals("data")) {
                subchunk2ID = "data";
                subchunk2Size = printLEBytes(fmtSubchunk, i+4, 4);
                dataStart = (i+4)+12+24+4;
            }
        }
            fillHeaderData();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillHeaderData() {
        if (dataStart != 0) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(filename);
                headerData = new byte[dataStart];
                fis.read(headerData);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] convertInt2ByteArray(int i) {
        byte[] bytes =  ByteBuffer.allocate(4).putInt(i).array();
        return bytes;
    }

    public byte[] convertInt2LEByteArray(int i) {
        byte[] bytes =  ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(i).array();
        return bytes;
    }

    public int convertByteArray2Int(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public int convertLEByteArray2Int(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }


    public byte[] copyMetaDataToNewWaveFile(byte[] newData) {
        int newChunkSize = newData.length + dataStart - 8;
        byte[] newChunkBytes =  convertInt2LEByteArray(newChunkSize);
        byte[] newHeaderData = new byte[headerData.length];
        int k = 0;
        for (int i = 0; i < headerData.length; i++) {
            if (i < 4 || i >= 8) {
                newHeaderData[i] = headerData[i];
            } else {
                newHeaderData[i] = newChunkBytes[k++];
            }
        }

        byte[] wavFile = new byte[newHeaderData.length + newData.length];
            int j=0;
            for (int i = 0; i <newHeaderData.length; i++) {
                wavFile[i] = newHeaderData[i];
                j=i;
            }
            for (int i = 0; i < newData.length; i++) {
                wavFile[j+i] = newData[i];
            }
        return wavFile;
    }

    private String printBytes(byte[] bytes) {
        String byteStr = new String(bytes).toString();
        System.out.println(byteStr);
        return byteStr;
    }


    private String printBytes(byte[] bytes, int offset, int length) {
        String byteStr = new String(bytes, offset, length).toString();
        System.out.println(byteStr);
        return byteStr;
    }

    private long printLEBytes(byte[] bytes, int offset, int length) {
        byte[] b = new byte[length];
        for (int i = offset; i < offset + length; i++) {
            b[i - offset] = bytes[i];
        }
        return printLEBytes(b);
    }

    private long printLEBytes(byte[] bytes) {

        int numBytes = bytes.length;
        int pos = 0;
        numBytes --;
        pos += numBytes;

        long val = bytes[pos] & 0xFF;
        for (int b=0 ; b<numBytes ; b++) {
            val = (val << 8) + (bytes[--pos] & 0xFF);
        }
        System.out.println(val);
        return val;
    }


    public void displayVar() {
        System.out.println("File: " + filename + "\n" +
        "chunk id " + chunkID + "\n" +
                "chunk size " + chunkSize + "\n" +
                "format " + format + "\n" +

                "subchunk1ID " + subchunk1ID + "\n" +
                "subchunk1Size " + subchunk1Size + "\n" +
                "audioFormat " + audioFormat + "\n" +
                "numChannels " + numChannels + "\n" +
                "sampleRate " + sampleRate + "\n" +
                "byteRate " + byteRate + "\n" +
                "blockAlign " + blockAlign + "\n" +
                "bitsPerSample " + bitsPerSample + "\n" +
                "extraParamSize " + extraParamSize + "\n" +
                "extraParams " + extraParams + "\n" +
                "subchunk2ID " + subchunk2ID + "\n" +
                "subchunk2Size " + subchunk2Size);
    }
}
