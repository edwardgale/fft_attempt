import com.sun.media.sound.WaveFileWriter;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;

/**
 * Created by galee on 19/11/2014.
 */
public class WavGenerator {
    public void generateTone()
            throws LineUnavailableException {
        Clip    clip = AudioSystem.getClip();

        boolean addHarmonic = false;

        int intSR = 44100;
        int intFPW = 512;

        float sampleRate = (float)intSR;

        // oddly, the sound does not loop well for less than
        // around 5 or so, wavelengths
        int wavelengths = 2000;
        byte[] buf = new byte[2*intFPW*wavelengths];
        AudioFormat af = new AudioFormat(
                sampleRate,
                16,  // sample size in bits
                2,  // channels
                true,  // signed
                false  // bigendian
        );

        int maxVol = 127;
        for(int i=0; i<intFPW*wavelengths; i++){
            double angle = ((float)(i*2)/((float)intFPW))*(Math.PI);
            System.out.println("Angle is " + angle);
            buf[i*2]=getByteValue(angle);
            if(addHarmonic) {
                buf[(i*2)+1]=getByteValue(2*angle);
            } else {
                buf[(i*2)+1] = buf[i*2];
            }
        }

        try {
            byte[] b = buf;
            AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(b),
                    af,
                    buf.length/2 );

            String filename = "its a beep.wav";
            FileOutputStream fos = new FileOutputStream(filename);
            AudioFileWriter audioFileWriter = new WaveFileWriter();
            audioFileWriter.write(ais, AudioFileFormat.Type.WAVE, fos);


            WaveAudioFileMeta waveAudioFileMeta = new WaveAudioFileMeta(filename);
            waveAudioFileMeta.displayVar();
            fos.close();
            clip.open( ais );
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /** Provides the byte value for this point in the sinusoidal wave. */
    private static byte getByteValue(double angle) {
        int maxVol = 127;
        return (new Integer((int)Math.round(Math.sin(angle)*maxVol))).byteValue();
    }
}
