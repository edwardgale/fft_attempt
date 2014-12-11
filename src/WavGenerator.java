import com.sun.media.sound.WaveFileWriter;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;

/**
 * Created by galee on 19/11/2014.
 */
public class WavGenerator {
    int maxVol=127;
    int sampleRate = 44100;
    int seconds = 100;
    int targetFreq = 4000;


    public void generateTone() throws LineUnavailableException {
        Clip clip = AudioSystem.getClip();

        boolean addHarmonic = true;

        // oddly, the sound does not loop well for less than
        // around 5 or so, wavelengths
        byte[] buf = new byte[seconds * targetFreq + 1];
        AudioFormat af = new AudioFormat(
                sampleRate,
                16,  // sample size in bits
                1,  // channels
                true,  // signed
                false  // bigendian
        );

        for (int i = 0; i < seconds * targetFreq; i++) {
            double angle = ((float) (i * 2) / ((float) targetFreq)) * (Math.PI);
            buf[i] = getByteValue(angle);
//            System.out.println("Angle is " + angle + " byte is " + buf[i]);
            if (addHarmonic) {
                buf[(i) + 1] = getByteValue(2 * angle);
            } else {
                buf[(i) + 1] = buf[i];
            }
        }

        try {
            byte[] b = buf;
            AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(b),
                    af,
                    buf.length / 2);

            String filename = "sin_"+"-"+ this.sampleRate +"_"+maxVol+"_"+".wav";
            FileOutputStream fos = new FileOutputStream(filename);
            AudioFileWriter audioFileWriter = new WaveFileWriter();
            audioFileWriter.write(ais, AudioFileFormat.Type.WAVE, fos);

            WaveAudioFileMeta waveAudioFileMeta = new WaveAudioFileMeta(filename);
            waveAudioFileMeta.displayVar();
            fos.close();
            clip.open(ais);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMaxVol(int maxVol) {
        this.maxVol = maxVol;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }


    /**
     * Provides the byte value for this point in the sinusoidal wave.
     */
    private byte getByteValue(double angle) {
        return (new Integer((int) Math.round(Math.sin(angle) * maxVol))).byteValue();
    }
}
