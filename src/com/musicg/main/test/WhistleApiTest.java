package com.musicg.main.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.musicg.api.WhistleApi;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

public class WhistleApiTest{
	public static void main(String[] args){		
		String filename = "audio_work/soft_whistle.wav";

		// get the wave instance
		Wave wave=new Wave(filename);
		WaveHeader wavHeader=wave.getWaveHeader();
		
		// fft size 1024, no overlap
		int fftSampleSize=1024;
		int fftSignalByteLength=fftSampleSize*wavHeader.getBitsPerSample()/8;				
		byte[] audioBytes=wave.getBytes();
		ByteArrayInputStream inputStream=new ByteArrayInputStream(audioBytes);
		
		WhistleApi whistleApi=new WhistleApi(wavHeader);

		// read the byte signals
		try {
			int numFrames = inputStream.available()/fftSignalByteLength;
			byte[] bytes=new byte[fftSignalByteLength];			
			int checkLength=10;
			int passScore=10;
			
			ArrayList<Boolean> bufferList=new ArrayList<Boolean>();
			int numWhistles=0;
			int numPasses=0;
			
			// first 10(checkLength) frames
			for (int frameNumber=0; frameNumber<checkLength; frameNumber++){
				inputStream.read(bytes);
				boolean isWhistle=whistleApi.isWhistle(bytes);
				bufferList.add(isWhistle);
				if (isWhistle){
					numWhistles++;
				}
				
				if (numWhistles>=passScore){
					numPasses++;
				}
				
				System.out.println(frameNumber+": "+numWhistles);
			}
			
			// other frames
			for (int frameNumber=checkLength; frameNumber<numFrames; frameNumber++){
				inputStream.read(bytes);
				boolean isWhistle=whistleApi.isWhistle(bytes);
				if (bufferList.get(0)){
					numWhistles--;
				}
				
				bufferList.remove(0);
				bufferList.add(isWhistle);
				
				if (isWhistle){
					numWhistles++;
				}			
				
				if (numWhistles>=passScore){
					numPasses++;
				}
				
				System.out.println(frameNumber+": "+numWhistles);
			}
			
			System.out.println("num passes: "+numPasses);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}