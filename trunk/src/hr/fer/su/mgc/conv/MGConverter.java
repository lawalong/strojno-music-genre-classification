package hr.fer.su.mgc.conv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MGConverter {
	
	/**
	 * Maximum concurrent thread count.
	 */
	public static final int THREAD_COUNT = 4;
	
	/**
	 * Threshold for float comparisions. If the difference between two floats is
	 * smaller than DELTA, they are considered equal.
	 */
	private static final float DELTA = 1E-9F;
	
	/**
	 * List of active threads.
	 */
	protected static Map<Integer, Thread> threads;
	
	/**
	 * Log file writer.
	 */
	protected static BufferedWriter log;
	
	
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
		
		if(args.length != 1) {
			System.err.println("Expected one argument: dataset path");
			System.exit(1);
		}
		
		File dataset = new File(args[0]);
		File normalDataset = new File(
				dataset.getParent() + File.separator + dataset.getName() + "_normal");
		
		if(!dataset.isDirectory()) {
			System.err.println("Specified dataset is not a directory!?");
			System.exit(2);
		}
		normalDataset.mkdir();
		
		// Setting start params...
		
		threads = new HashMap<Integer, Thread>(THREAD_COUNT);
		
		int desiredAudioLength = 1323588; // About 30 seconds...
		
		int desiredChannels = 1; // MONO

		int desiredSampleSizeInBits = 16;

		AudioFormat.Encoding desiredEncoding = AudioFormat.Encoding.PCM_SIGNED;

		float desiredSampleRate = 22050;

		AudioFileFormat.Type desiredFileType = AudioFileFormat.Type.AU;

		boolean desiredBigEndian = true;
		
		boolean changeNames = true;
		
		
		// Init log file...
		log = new BufferedWriter(new FileWriter(
				new File(normalDataset.getAbsolutePath() + File.separator + "conv.log")));
		
		writeOut(System.out, "\n\t>>>>>>>>>> Starting dataset generation... <<<<<<<<<<\n");
		
		
		AudioInputStream inputStream = null;
		AudioFileFormat sourceFileFormat;
		AudioFormat sourceFormat;
		File normalGenre, normalAudioFile;
		String newName;
		int counter; int tmp;
		
		ConvThread thread;
		boolean fileDone;
		
		for(File genre : dataset.listFiles()) {
			counter = 0;
			normalGenre = new File(
					normalDataset.getAbsolutePath() + File.separator + genre.getName());
			if(genre.isDirectory()) {
				for(File audioFile : genre.listFiles()) {
					if(audioFile.isFile()) {
						counter++;
						
						// Construct new name...
						if(changeNames) {
							tmp = genre.getName().lastIndexOf('.');
							if(tmp == -1) newName = genre.getName();
							else newName = genre.getName().substring(0, genre.getName().lastIndexOf('.'));
							newName = newName.concat(".");
							tmp = 4-String.valueOf(counter).length();
							while(tmp > 0) {
								tmp--;
								newName = newName.concat("0");
							}
							newName = newName.concat(String.valueOf(counter));
						} else {
							newName = audioFile.getName().
								substring(0, audioFile.getName().lastIndexOf('.'));
						}
						
						normalAudioFile = new File(
								normalGenre.getAbsolutePath() + 
								File.separator + newName + '.' + desiredFileType.getExtension());
						
						try {
							inputStream = AudioSystem.getAudioInputStream(audioFile);
						} catch (UnsupportedAudioFileException ex) {
							writeOut(System.err, "Unsupported audio file: " + 
									audioFile.getName() + ". Skipping...");
							continue;
						} catch (IOException ex) {
							writeOut(System.err, "IO exception occurred while opening: " + 
									audioFile.getName() + ". Skipping...");
							continue;
						}
						
						
						sourceFileFormat = AudioSystem.getAudioFileFormat(audioFile);
						sourceFormat = sourceFileFormat.getFormat();
						
						if(normalAudioFile.exists()) normalAudioFile.delete();
						normalAudioFile.getParentFile().mkdirs();
						
						
						/*
						 * Step 1: convert to PCM, if necessary.
						 */
						if (!isPcm(sourceFormat.getEncoding())) {
							inputStream = convertEncoding(desiredEncoding, inputStream);
						}
						
						/*
						 * Step 2: convert number of channels, if necessary.
						 */
						if (inputStream.getFormat().getChannels() != desiredChannels) {
							inputStream = convertChannels(desiredChannels, inputStream);
						}
						
						/*
						 * Step 3: convert sample size and endianess, if necessary.
						 */
						boolean bDoConvertSampleSize = inputStream.getFormat()
								.getSampleSizeInBits() != desiredSampleSizeInBits;
						boolean bDoConvertEndianess = inputStream.getFormat().isBigEndian() != desiredBigEndian;
						if (bDoConvertSampleSize || bDoConvertEndianess) {
							inputStream = convertSampleSizeAndEndianess(desiredSampleSizeInBits,
									desiredBigEndian, inputStream);
						}
						
						/*
						 * Step 4: convert sample rate, if necessary.
						 */
						if (!equals(inputStream.getFormat().getSampleRate(), desiredSampleRate)) {
							inputStream = convertSampleRate(desiredSampleRate, inputStream);
						}
						
						// Assign idle thread to work...
						while(true) {
							fileDone = false;
							synchronized (threads) {
								if(threads.size() < THREAD_COUNT) {
									// Grab number...
									for(tmp = 0; tmp < THREAD_COUNT; tmp++)
										if(threads.get(tmp) == null) break;

									thread = new ConvThread(threads, Thread.currentThread(), 
											inputStream, audioFile, normalAudioFile, 
											desiredSampleSizeInBits, desiredAudioLength, 
											desiredFileType, tmp);
									threads.put(tmp, thread);
									thread.start();
									fileDone = true;
								}
							}
							
							if(fileDone) break;
							else {
								try {
									synchronized (Thread.currentThread()) {
										Thread.currentThread().wait();
									}
								} catch (InterruptedException Ignorable) { }
							}
						}
					}
				}
				
				writeOut(System.out, "\n\t>>>>>>>>>> Genre " + genre.getName() + " is completing... <<<<<<<<<<\n");
			}
		}
		
		// Wait for threads to finish...
		writeOut(System.out, "Completing conversion. Waiting for remaining threads...");
		while(true) {
			try {
				synchronized (Thread.currentThread()) {
					Thread.currentThread().wait();
				}
			} catch (InterruptedException Ignorable) { }
			synchronized (threads) {
				if(threads.size() == 0) break;
			}
		}
		writeOut(System.out, "\n\t>>>>>>>>>> Dataset generation completed. <<<<<<<<<<\n");
	}
	



	private static AudioInputStream convertEncoding(
			AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream) {
		return AudioSystem.getAudioInputStream(targetEncoding, sourceStream);
	}

	private static AudioInputStream convertChannels(int nChannels,
			AudioInputStream sourceStream) {
		AudioFormat sourceFormat = sourceStream.getFormat();
		AudioFormat targetFormat = new AudioFormat(sourceFormat.getEncoding(),
				sourceFormat.getSampleRate(), sourceFormat
						.getSampleSizeInBits(), nChannels, calculateFrameSize(
						nChannels, sourceFormat.getSampleSizeInBits()),
				sourceFormat.getFrameRate(), sourceFormat.isBigEndian());
		return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
	}

	private static AudioInputStream convertSampleSizeAndEndianess(
			int nSampleSizeInBits, boolean bBigEndian,
			AudioInputStream sourceStream) {
		AudioFormat sourceFormat = sourceStream.getFormat();
		AudioFormat targetFormat = new AudioFormat(sourceFormat.getEncoding(),
				sourceFormat.getSampleRate(), nSampleSizeInBits, sourceFormat
						.getChannels(), calculateFrameSize(sourceFormat
						.getChannels(), nSampleSizeInBits), sourceFormat
						.getFrameRate(), bBigEndian);
		return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
	}

	private static AudioInputStream convertSampleRate(float fSampleRate,
			AudioInputStream sourceStream) {
		AudioFormat sourceFormat = sourceStream.getFormat();
		AudioFormat targetFormat = new AudioFormat(sourceFormat.getEncoding(),
				fSampleRate, sourceFormat.getSampleSizeInBits(), sourceFormat
						.getChannels(), sourceFormat.getFrameSize(),
				fSampleRate, sourceFormat.isBigEndian());
		return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
	}

	private static int calculateFrameSize(int nChannels, int nSampleSizeInBits) {
		return ((nSampleSizeInBits + 7) / 8) * nChannels;
	}

	/**
	 * Compares two float values for equality.
	 */
	private static boolean equals(float f1, float f2) {
		return (Math.abs(f1 - f2) < DELTA);
	}
	
	/**
	 * Checks if the encoding is PCM.
	 */
	public static boolean isPcm(AudioFormat.Encoding encoding) {
		return encoding.equals(AudioFormat.Encoding.PCM_SIGNED)
				|| encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED);
	}
	
	public static synchronized void writeOut(PrintStream out, String message) {
		if(out.equals(System.err))
			message = "ERROR: " + message;
		
		out.println(message);
		try {
			log.write(message + "\n");
			log.flush();
		} catch (IOException e) {
			System.err.println("Error writing to log file. Ignoring...");
		}
	}
	


}
