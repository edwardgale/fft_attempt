import de.fau.cs.jstk.io.IOUtil;
import de.fau.cs.jstk.sampled.AudioFileReader;
import de.fau.cs.jstk.sampled.AudioSource;
import de.fau.cs.jstk.sampled.RawAudioFormat;
import de.fau.cs.jstk.sampled.filters.Butterworth;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.junit.Test;
import wavtester.WavFile;

import javax.sound.sampled.*;
import javax.xml.crypto.dsig.TransformService;
import java.io.*;
import java.nio.ByteOrder;
import java.text.NumberFormat;

import static org.junit.Assert.*;

public class URLAudioTest {

    @Test
    public void testGetSampleLength() throws Exception {
        URLAudio urlAudio = new URLAudio();
        urlAudio.getSampleLength();

    }

    @Test
    public void testButterWorth() throws Exception {
        FileInputStream fis = new FileInputStream("butters.wav");
        int i = 0;
        int count = 0;
        while ((i = fis.read()) != -1) {
            count++;
        }

        byte[] bytes = new byte[count];
        FileInputStream fis1 = new FileInputStream("butters.wav");
        fis1.read(bytes);
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < bytes.length; j++) {
            sb.append(new String("" + Byte.toString(bytes[j]) + "\n"));
        }
        System.out.println(sb);

    }
    @Test
    public void applyNewHeader2Wav() throws Exception {
        doButterworth();
        WaveAudioFileMeta waveAudioFileMeta = new WaveAudioFileMeta("Conny_Hello.wav");
        waveAudioFileMeta.displayVar();

        FileInputStream fis = new FileInputStream("butters.wav");
        int i = 0;
        int count = 0;
        while ((i = fis.read()) != -1) {
            count++;
        }

        byte[] bytes = new byte[count];
        FileInputStream fis1 = new FileInputStream("butters.wav");
        fis1.read(bytes);
        byte[] newWavFile = waveAudioFileMeta.copyMetaDataToNewWaveFile(bytes);

        FileOutputStream fos = new FileOutputStream("butters_new_header.wav");
        fos.write(newWavFile);
        fos.close();
        System.out.println("");

        Clip clip = AudioSystem.getClip();
        clip.open(AudioSystem.getAudioInputStream(new File("Conny_Hello.wav")));
        clip.start();
        while (clip.isRunning()) {
            System.out.println("Active");
        }
    }

    @Test
    public void doButterworth() throws Exception {
        RawAudioFormat rawAudioFormat = new RawAudioFormat(16, 44100, true, true, 44);


        AudioSource as = new AudioFileReader("Conny_Hello.wav", RawAudioFormat.getRawAudioFormat("wav/44"), true);
        Butterworth bwf;

//        wav/16 3 3000 340000
        bwf = new Butterworth(as, 3, 300,3000, true);

        double scale = Math.pow(2, 16 - 1) - 1;

        double [] buf = new double [512];
        short [] out = new short [512];
        int r;
        FileOutputStream fos = new FileOutputStream("butters.wav");
        StringBuffer sb = new StringBuffer();
        while ((r = bwf.read(buf)) > 0) {
            for (int i = 0; i < r; ++i) {
                double x = buf[i];
                out[i] = (short)(buf[i] * scale);
                if (out[i] > 0) {
                    sb.append(out[i] + "\n");
                }
            }
            IOUtil.writeShort(fos, out, r, ByteOrder.LITTLE_ENDIAN);
        }
        fos.close();

    }
    @Test
    public void testWavMetaDataStuff() throws Exception {
        WaveAudioFileMeta waveAudioFileMeta = new WaveAudioFileMeta("Conny_Hello_post_audacity.wav");
        waveAudioFileMeta.displayVar();
        System.out.println();
    }


    @Test
    public void testCreateSineWav() throws Exception {
            WavGenerator wavGenerator = new WavGenerator();
wavGenerator.generateTone();
           System.out.println();
    }

    @Test
    public void testFFTApache() throws Exception {
        URLAudio urlAudio = new URLAudio();
        float[] f = urlAudio.readAudioStream(100000, 1024);
        double[] d = new double[f.length];
        for (int i = 0; i < f.length; i++) {
            d[i] = f[i];
        }
        FastFourierTransformer fastFourierTransformer = new FastFourierTransformer(DftNormalization.STANDARD);

        Complex[] complex = fastFourierTransformer.transform(d, TransformType.FORWARD);

        Complex[] complex1 = fastFourierTransformer.transform(complex, TransformType.INVERSE);
        int multipleFactor=100;
        StringBuffer s = new StringBuffer(f.length * multipleFactor);
        byte[] outputData = new byte[(f.length-1)*multipleFactor];
        for (int k = 0; k < multipleFactor; k++) {
            for (int i = 1; i < f.length; i++) {
                double real = f[i];
                s.append(real + " ");
                System.out.print(real + " ");
                outputData[i] = (byte) real;
            }
        }

        // Write output of ifft to new wav file.
        AudioFormat frmt = new AudioFormat(44100, 16, 1, true, false);
        AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(outputData), frmt,
                outputData.length / frmt.getFrameSize()
        );

        try {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File("testy.wav"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        System.out.println("tester");
    }


    @Test
    public void testFFT() throws Exception {
        float[] signal;
        float[] windowedSignal;
        URLAudio urlAudio = new URLAudio();
        long beginFrom;
        beginFrom = 9001;
        int nSamples = 256;
        float[] spectrum;

        long sampleLength = urlAudio.getSampleLength();
        System.out.println("<sample>");
        signal = urlAudio.readAudioStream(0, 32000);
        for (int i = 0; i < signal.length; i++) {
            float f = signal[i];
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(7);
            System.out.print(f + " ");
        }
        System.out.println(signal);
        float maxValue = 0.0f;
        for (int i = 0; i < nSamples; i++)
            maxValue = Math.max(maxValue, Math.abs(signal[i]));
//        graphPlotSignal.setYmax(maxValue);
//        graphPlotSignal.setPlotValues(signal);

        // apply windowing
        Windowing windowing = new Windowing();
        windowing.setWindowType("Rectangle");
        windowedSignal = windowing.apply(signal);

        // plot windowed signal
        maxValue = 0.0f;
        for (int i = 0; i < nSamples; i++) {
            maxValue = Math.max(maxValue, Math.abs(windowedSignal[i]));
            System.out.print(windowedSignal[i] + " ");
        }
//        graphPlotWindowed.setYmax(maxValue);
//        graphPlotWindowed.setPlotValues(windowedSignal);


        spectrum = new float [nSamples / 2];
        FastFourierTransform fft = new FastFourierTransform();
        spectrum = fft.fftMag(windowedSignal);
//        graphPlotSpectrum.setPlotStyle(GraphPlot.SPECTRUM);
//        graphPlotSpectrum.setTracePlot(false);
        for (int i = 0; i < nSamples/2; i++) {
            maxValue = Math.max(maxValue, Math.abs(spectrum[i]));
            System.out.println(spectrum[i]);

        }
//        graphPlotSpectrum.setYmax(maxValue);
//        graphPlotSpectrum.setPlotValues(spectrum);
//        buttonPlotSpectrum.setEnabled(false);


    }

    @Test
    public void testReadAudioStream() throws Exception {

    }
}