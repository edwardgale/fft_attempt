/*
 * URLAudioReader.java
 *
 * Created on 2002�~7��4��, �U�� 4:31
 */

/**
 *
 * @author  Tsan-Kuang Lee
 */

//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.UnsupportedAudioFileException;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileWriter;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class URLAudio {

    private AudioInputStream audioInputStream;
    private boolean isBigEndian;
    private int bytesPerFrame;
    private long frameLength;
    private int channels;
    private float sampleRate;
    private int sampleSize;
    private long sampleLength;
    private int bytesPerSample;
    private int samplesPerFrame;
    private String audioStreamDescription;


    public URLAudio() {
        this("Conny_Hello.wav");
    }

    /** Creates a new instance of URLAudioReader */
    URLAudio(String urlstr) {
        try {
//            urlstr = "file://" + urlstr;
//            URL url = new File("BroadBone-D4.wav").toURI().toURL();
            URL url = new File(urlstr).toURI().toURL();
//            URL url = new URL(urlstr);

            audioInputStream = AudioSystem.getAudioInputStream(url);
            frameLength = audioInputStream.getFrameLength();
            if (audioInputStream.markSupported())
                audioInputStream.mark((int)frameLength);
            isBigEndian = audioInputStream.getFormat().isBigEndian();
            bytesPerFrame = audioInputStream.getFormat().getFrameSize();
            channels = audioInputStream.getFormat().getChannels();
            sampleRate = audioInputStream.getFormat().getSampleRate();
            sampleSize = audioInputStream.getFormat().getSampleSizeInBits();
            bytesPerSample = sampleSize / 8;
            samplesPerFrame = bytesPerFrame / bytesPerSample;
            sampleLength = (frameLength / channels) * samplesPerFrame; // total samples per channel
            audioStreamDescription =
                    urlstr + " :\n "
                            + Integer.toString(sampleSize) + " bits "
                            + Float.toString(sampleRate) + " Hz "
                            + Integer.toString(channels) + " channels "
                            + Long.toString(sampleLength) + " samples/channel "
                            + Float.toString((float)sampleLength / sampleRate) + " seconds.";
            System.out.println(audioStreamDescription);

            List<Float> allBytes = new ArrayList<Float>();
            long begin = 0;
            int noSamples = (int) audioInputStream.getFrameLength();
            float[] f = readAudioStream(begin, noSamples);
            for (int i = 0; i < f.length; i++) {
                allBytes.add(new Float(f[i]));
            }

            byte[] bytes = new byte[allBytes.size()];
            for (int i = 0; i < allBytes.size(); i++) {
                bytes[i] = allBytes.get(i).byteValue();
            }

            System.out.println("done creating list");
//            byteArrayOutputStream.write(allBytes);
            // Write output of ifft to new wav file.
            AudioFormat frmt = new AudioFormat(44100, 16, 1, true, false);
            AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(bytes), frmt,
                    bytes.length / frmt.getFrameSize());
//            audioInputStream.reset();

            AudioInputStream aosblah = AudioSystem.getAudioInputStream(url);
            if (aosblah.markSupported())
                aosblah.mark((int)aosblah.getFrameLength());

            AudioInputStream aosbla1 = AudioSystem.getAudioInputStream(url);


            AudioInputStream aos = AudioSystem.getAudioInputStream(url);
            byte[] bb = new byte[(int) aos.getFrameLength()];
            aos.mark(bb.length);
            int x = aos.read(bb);
            StringBuffer a = new StringBuffer();
            for (int i = 0; i < bb.length; i++) {
                a.append(bb[i] + "  " + i + "\n");
            }
            aos.reset();

            ByteArrayInputStream bis = new ByteArrayInputStream(bb);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(bis);
            AudioInputStream ais1 = new AudioInputStream(bis, aos.getFormat(), aos.getFrameLength());

            byte[] bbc = new byte[bb.length];
            bis.read(bbc, 0, bb.length);
            StringBuffer b = new StringBuffer();
            for (int i = 0; i < bbc.length; i++) {
                b.append(bbc[i] + "  " + i + "\n");
            }
            bis.reset();




            try {
                AudioSystem.write(ais1, AudioFileFormat.Type.WAVE, new File("testy.wav"));
                AudioInputStream aostest = AudioSystem.getAudioInputStream(new File("testy.wav").toURI().toURL());
                StringBuffer c = new StringBuffer();
                for (int i = 0; i < bb.length; i++) {
                    c.append(bb[i] + "  " + i + "\n");
                }

                if (a.equals(b) && a.equals(c)) {
                    System.out.println("all the same so what's going on!!!");
                }
                System.out.println();


                AudioInputStream aos2 = AudioSystem.getAudioInputStream(url);
                AudioInputStream aos3 = AudioSystem.getAudioInputStream(url);
                AudioInputStream appendedFiles =
                        new AudioInputStream(
                                new SequenceInputStream(aos2, aos3),
                                aos2.getFormat(),
                                aos2.getFrameLength() + aos3.getFrameLength());

                AudioSystem.write(appendedFiles,
                        AudioFileFormat.Type.WAVE,
                        new File("wavAppended.wav"));

            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        catch (MalformedURLException ex) {
            System.out.println(ex.getMessage());
        }
        catch (UnsupportedAudioFileException ex) {
            System.out.println(ex.getMessage());
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void createWAVFile(double[] d) {
//        AudioFileWriter

    }


    public long getSampleLength() {
        return sampleLength;
    }

    public String getAudioStreamDescription() {
        return audioStreamDescription;
    }


    public float[] readAudioStream(long beginFrom, int nSamples) {
        float[] readSamples = new float[nSamples];

        // a frame has all channel data.  Each channel data may have more than one byte data      
        int interval = channels * bytesPerSample; // skip one frame at a time


        int numBytes = nSamples * bytesPerSample;
        byte[] audioBytes = new byte[numBytes];
        int numBytesRead = 0;
        int numSamplesRead = 0;

        // set the file pointer to the beginning of the desired chunk to read
        try {
            audioInputStream.reset();
            audioInputStream.skip(beginFrom*bytesPerSample*channels);
        }
        catch (IOException ex) {
            System.out.println("Can't place the pointer to " + beginFrom + "th samples");
        }

        try {
            // Try to read numBytes bytes from the file.
            if ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
                // Calculate the number of frames actually read.
                numSamplesRead = numBytesRead / bytesPerSample;
                for (int i = 0 ; i < numBytesRead ; i += interval) {
                    // We only read first channel  (usually LEFT channel)

                    // 8-bit samples are stored as unsigned bytes, ranging from 0 to 255.
                    // 16-bit samples are stored as 2's-complement signed integers, ranging from -32768 to 32767
                    if (bytesPerSample == 2) { // 16 bits data
                        // bigEndian means the bytes come in non-reserved order (left byte is high byte)
                        int high, low;
                        if (isBigEndian) {
                            high = audioBytes[i] & 0xff;
                            low = audioBytes[i+1] & 0xff;
                        }
                        else {
                            low = audioBytes[i] & 0xff;
                            high = audioBytes[i+1] & 0xff;
                        }
                        short value = (short) (high << 8 | low);
                        readSamples[i/interval] = (float) value / (float) (2<<15);
                    }
                    else { // 8 bits data
                        readSamples[i/interval] = (float) audioBytes[i] / 255.0f;
                    }
                }
            }

        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return (readSamples);
    }
}

