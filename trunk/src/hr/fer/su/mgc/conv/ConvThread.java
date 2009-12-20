package hr.fer.su.mgc.conv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFileFormat.Type;


public class ConvThread extends Thread {
	
	private Map<Integer, Thread> threads;
	private Thread mainThread;
	private AudioInputStream inputStream;
	private File audioFile;
	private File normalAudioFile;
	private int desiredSampleSizeInBits;
	private int desiredAudioLength;
	private Type desiredFileType;
	private int threadNumber;
	

	public ConvThread(Map<Integer, Thread> threads, Thread mainThread,
			AudioInputStream inputStream, File audioFile, File normalAudioFile,
			int desiredSampleSizeInBits, int desiredAudioLength, 
			Type desiredFileType, int threadNumber) {
		this.threads = threads;
		this.mainThread = mainThread;
		this.inputStream = inputStream;
		this.audioFile = audioFile;
		this.normalAudioFile = normalAudioFile;
		this.desiredSampleSizeInBits = desiredSampleSizeInBits;
		this.desiredAudioLength = desiredAudioLength;
		this.desiredFileType = desiredFileType;
		this.threadNumber = threadNumber;
	}

	@Override
	public void run() {
		try {
		long time = GregorianCalendar.getInstance().getTimeInMillis();
		
		int tmp, tmp2, midIndex;
		
		// Buffer vars...
		ByteArrayOutputStream baos;
		ByteArrayInputStream bais;
		AudioInputStream cutInputStream;
		int nBufferSize;
		byte[] abBuffer, abAudioData, abAudioDataSmall;

		/*
		 * Read the audio data into a memory buffer.
		 */
		baos = new ByteArrayOutputStream();
		nBufferSize = 32768 * desiredSampleSizeInBits/8;
		abBuffer = new byte[nBufferSize];
		int nBytesRead;
		while (true) {
			try {
				nBytesRead = inputStream.read(abBuffer);
				if (nBytesRead == -1) break;
				baos.write(abBuffer, 0, nBytesRead);
			} catch (IOException e) {
				MGConverter.writeOut(System.err, "\tThread " + (threadNumber+1) + 
						": IO error while reading audio: " + audioFile.getName());
				if(normalAudioFile.exists()) normalAudioFile.delete();
				threadFinish();
				return;
			}
		}
		
		abAudioData = baos.toByteArray();
		
		if(desiredAudioLength + 16 > abAudioData.length) {
			MGConverter.writeOut(System.err, "\tThread " + (threadNumber+1) + ": Audio too short: " + 
					audioFile.getName() + ". Skipping...");
			threadFinish();
			return;
		}
		
		abAudioDataSmall = new byte[desiredAudioLength];
		midIndex = ((abAudioData.length/2) % 2 == 1) ? 
				abAudioData.length/2-1 : abAudioData.length/2;
		midIndex = midIndex - (((desiredAudioLength/2) % 2 == 1) ? 
				desiredAudioLength/2-1 : desiredAudioLength/2);
		try {
			for(tmp = midIndex, tmp2 = 0; tmp < midIndex+desiredAudioLength; tmp++) {
				abAudioDataSmall[tmp2++] = abAudioData[tmp];
			}
		} catch (Exception ex) {
			MGConverter.writeOut(System.err, "\tThread " + (threadNumber+1) + 
					": Error ocurred while cuting audio: " + audioFile.getName());
			if(normalAudioFile.exists()) normalAudioFile.delete();
			threadFinish();
			return;
		}

		bais = new ByteArrayInputStream(abAudioDataSmall);
		cutInputStream = new AudioInputStream(bais, inputStream.getFormat(),
				abAudioDataSmall.length / inputStream.getFormat().getFrameSize());
		
		int writtenBytes = 0;
		try {
			writtenBytes = AudioSystem.write(cutInputStream, desiredFileType, normalAudioFile);
		} catch (IOException e) {
			MGConverter.writeOut(System.err, "\tThread " + (threadNumber+1) + 
					": IO error while writing audio: " + audioFile.getName());
			if(normalAudioFile.exists()) normalAudioFile.delete();
			threadFinish();
			return;
		}
		MGConverter.writeOut(System.out, "\tThread " + (threadNumber+1) + ": " + writtenBytes + " bytes written in " + 
				(float)(GregorianCalendar.getInstance().getTimeInMillis()-time)/1000 + " seconds.\n" + 
				audioFile.getName() + " -> " + normalAudioFile.getName());
		
		threadFinish();
		
		} catch (Throwable genex) {
			MGConverter.writeOut(System.err, "\tThread " + (threadNumber+1) + 
					": Generic error while processing audio: " + audioFile.getName() + 
					"\n Message: " + genex.getMessage());
			if(normalAudioFile.exists()) normalAudioFile.delete();
			threadFinish();
		}
	}

	private void threadFinish() {
		synchronized (threads) {
			threads.remove(threadNumber);
		}
		synchronized (mainThread) {
			mainThread.notify();
		}
	}
	
}