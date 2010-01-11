package hr.fer.su.mgc.conv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MGCconv {
	
	/**
	 * Threshold for float comparisions. If the difference between two floats is
	 * smaller than DELTA, they are considered equal.
	 */
	private static final float DELTA = 1E-9F;
	
	public static File convertForClassification(
			File inputFile) throws ConversionException {
		
		int desiredAudioLength = 1323588; // About 30 seconds...
		
		int desiredChannels = 1; // MONO

		int desiredSampleSizeInBits = 16;

		AudioFormat.Encoding desiredEncoding = AudioFormat.Encoding.PCM_SIGNED;

		float desiredSampleRate = 22050;

		AudioFileFormat.Type desiredFileType = AudioFileFormat.Type.AU;

		boolean desiredBigEndian = true;
		
		
		File outputFile;
		try {
			outputFile = File.createTempFile("mgc-temp-audio-", "." + desiredFileType.getExtension());
		} catch (IOException e1) {
			throw new ConversionException(
					"Error creating temp file from: " + inputFile.getName());
		}
		
		AudioInputStream inputStream;
		try {
			inputStream = AudioSystem.getAudioInputStream(inputFile);
		} catch (UnsupportedAudioFileException e1) {
			if(outputFile.exists()) outputFile.delete();
			throw new ConversionException(
					inputFile.getName() + " -> Audio file not supported!");
		} catch (IOException e1) {
			if(outputFile.exists()) outputFile.delete();
			throw new ConversionException(
					inputFile.getName() + " -> Error getting audio input stream!");
		}
		
		AudioFormat sourceFormat = inputStream.getFormat();
		
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
		
		try {
			
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
					if(outputFile.exists()) outputFile.delete();
					throw new ConversionException(
							inputFile.getName() + " -> IO error while reading audio!");
				}
			}
			
			abAudioData = baos.toByteArray();
			
			if(desiredAudioLength + 16 > abAudioData.length) {
				bais = new ByteArrayInputStream(abAudioData);
				
			} else {
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
					if(outputFile.exists()) outputFile.delete();
					throw new ConversionException(
							inputFile.getName() + " -> Error ocurred while cuting audio!");
				}

				bais = new ByteArrayInputStream(abAudioDataSmall);
			}
			
			cutInputStream = new AudioInputStream(bais, inputStream.getFormat(),
					desiredAudioLength / inputStream.getFormat().getFrameSize());
			
			try {
				AudioSystem.write(cutInputStream, desiredFileType, outputFile);
			} catch (IOException e) {
				if(outputFile.exists()) outputFile.delete();
				throw new ConversionException(
						inputFile.getName() + " -> IO error while writing audio!");
			}
			
			return outputFile;
			
		} catch (Throwable genex) {
			if(outputFile.exists()) outputFile.delete();
			throw new ConversionException(ConversionException.EX_TYPE_GENERIC, 
					"Generic error while processing audio: " + inputFile.getName() + 
					"\n Message: " + genex.getMessage());
		}
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

}
