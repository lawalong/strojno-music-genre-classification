/*
 *  Copyright 2006-2007 Columbia University.
 *
 *  This file is part of MEAPsoft.
 *
 *  MEAPsoft is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *
 *  MEAPsoft is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MEAPsoft; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA
 *
 *  See the file "COPYING" for the text of the license.
 */

package com.meapsoft;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

/**
 * Can write to a file or to an audio stream. For some reason this appears to be
 * difficult to do in the JavaSound framework. This is just a wrapper class to
 * unify the interface.
 * 
 * @author Mike Mandel (mim@ee.columbia.edu)
 */

public class AudioWriter implements Runnable {
	AudioFormat format;

	File file;

	AudioFileFormat.Type targetType;

	SourceDataLine sdl;

	PipedOutputStream pos;

	PipedInputStream pis;

	AudioInputStream ais;

	byte[] bytes;

	// If filename is null, the AudioWriter writes directly to the sound
	// card. Otherwise it writes to a file with the given name. The
	// file type is determined by filename's extension.
	public AudioWriter(String filename, AudioFormat format)
			throws LineUnavailableException, IOException {
		if (filename == null) {
			this.sdl = obtainAndOpenMixerLine(format);
			this.format = sdl.getFormat();
			sdl.start();
		} else {
			this.file = new File(filename);
			this.targetType = AudioFileFormat.Type.WAVE;
			this.format = format;

			// Figure out the file extension
			String extension = null;
			int dotPosition = filename.lastIndexOf('.');
			if (dotPosition != -1)
				extension = filename.substring(dotPosition + 1);

			if (extension != null) {
				// Figure out the corresponding file type
				AudioFileFormat.Type[] aTypes = AudioSystem.getAudioFileTypes();
				for (int i = 0; i < aTypes.length; i++)
					if (aTypes[i].getExtension().equals(extension))
						targetType = aTypes[i];
			}

			// Write to the output stream
			pos = new PipedOutputStream();

			// It will then go to the file via the input streams
			pis = new PipedInputStream(pos);
			ais = new AudioInputStream(pis, format, AudioSystem.NOT_SPECIFIED);

			new Thread(this).start();
		}
	}

	// Write to a source data line. The line should be open before
	// passing it in here.
	public AudioWriter(SourceDataLine sdl) {
		this.sdl = sdl;
		format = sdl.getFormat();
		sdl.start();
	}

	// Create an AudioWriter object that writes directly to the sound
	// card using the given AudioFormat.
	public AudioWriter(AudioFormat format) throws LineUnavailableException {
		this.sdl = obtainAndOpenMixerLine(format);
		this.format = format;
		sdl.start();
	}

	private SourceDataLine obtainAndOpenMixerLine(AudioFormat format)
			throws LineUnavailableException {
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

		Mixer.Info[] mixers = AudioSystem.getMixerInfo();
		int mixerToUse = 0;
		for (int i = 0; i < mixers.length; i++)
			if (AudioSystem.getMixer(mixers[i]).isLineSupported(info))
				mixerToUse = i;

		// Obtain and open the line.
		SourceDataLine sdl = (SourceDataLine) AudioSystem.getMixer(
				mixers[mixerToUse]).getLine(info);
		sdl.open(format);
		return sdl;
	}

	// Write to a file
	public AudioWriter(File file, AudioFormat format,
			AudioFileFormat.Type targetType) throws IOException {
		this.format = format;
		this.targetType = targetType;
		this.file = file;

		// Write to the output stream
		pos = new PipedOutputStream();

		// It will then go to the file via the input streams
		pis = new PipedInputStream(pos);
		ais = new AudioInputStream(pis, format, AudioSystem.NOT_SPECIFIED);

		new Thread(this).start();
	}

	// ////////////////////////////////////////////////////////////////////////////////
	// // add and remove line listeners - in case we have more than one listener
	// ////////////////////////////////////////////////////////////////////////////////
	public void addLineListeners(Vector kListeners) {
		for (int i = 0; i < kListeners.size(); i++) {
			sdl.addLineListener((LineListener) kListeners.get(i));
		}
	}

	public void removeLineListeners(Vector kListeners) {
		for (int i = 0; i < kListeners.size(); i++) {
			sdl.removeLineListener((LineListener) kListeners.get(i));
		}
	}

	public void run() {
		try {
			AudioSystem.write(ais, targetType, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void write(double[] data) throws IOException {
		write(data, data.length);
	}

	public void write(double[] data, int length) throws IOException {
		// Allocate a new bytes array if necessary. If bytes is too long,
		// don't worry about it, just use as much as is needed.
		// int numBytes = length * format.getFrameSize();
		int numBytes = length * format.getFrameSize() / format.getChannels();
		if (bytes == null || numBytes > bytes.length)
			bytes = new byte[numBytes];

		// Limit data to [-1, 1]
		limit(data);

		// Convert doubles to bytes using format
		doubles2bytes(data, bytes, length);

		// Hack to avoid errors about writing non integer number of
		// frames. This is just a quick fix. The real bug is still unsolved.
		numBytes -= numBytes % format.getFrameSize();

		// write it
		if (pos != null)
			pos.write(bytes, 0, numBytes);
		if (sdl != null)
			sdl.write(bytes, 0, numBytes);
	}

	// Perform memoryless limiting on the audio data to keep all samples
	// in [-1,1]
	public static void limit(double[] data) {
		double t = 0.8;
		double c = 2 * (1 - t) / Math.PI;

		for (int i = 0; i < data.length; i++) {
			if (data[i] > t) {
				data[i] = c * Math.atan((data[i] - t) / c) + t;
			} else if (data[i] < -t) {
				data[i] = c * Math.atan((data[i] + t) / c) - t;
			}
		}
	}

	public void write(byte[] bytes) throws IOException {
		if (pos != null)
			pos.write(bytes, 0, bytes.length);
		if (sdl != null)
			sdl.write(bytes, 0, bytes.length);
	}

	public void close() throws IOException {
		if (pos != null) {
			// System.out.println("Closing input stream, bleg!");
			ais.close();
			pis.close();
			pos.close();
		}
		if (sdl != null) {
			// stop us, then close us
			sdl.stop();
			sdl.close();
		}
	}

	public AudioFormat getFormat() {
		return format;
	}

	// Convert an array of doubles into a byte stream
	private void doubles2bytes(double[] audioData, byte[] audioBytes, int length) {
		int in;
		if (format.getSampleSizeInBits() == 16) {
			if (format.isBigEndian()) {
				for (int i = 0; i < length; i++) {
					in = (int) (audioData[i] * 32767);
					/* First byte is MSB (high order) */
					audioBytes[2 * i] = (byte) (in >> 8);
					/* Second byte is LSB (low order) */
					audioBytes[2 * i + 1] = (byte) (in & 255);
				}
			} else {
				for (int i = 0; i < length; i++) {
					in = (int) (audioData[i] * 32767);
					/* First byte is LSB (low order) */
					audioBytes[2 * i] = (byte) (in & 255);
					/* Second byte is MSB (high order) */
					audioBytes[2 * i + 1] = (byte) (in >> 8);
				}
			}
		} else if (format.getSampleSizeInBits() == 8) {
			if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
				for (int i = 0; i < length; i++) {
					audioBytes[i] = (byte) (audioData[i] * 127);
				}
			} else {
				for (int i = 0; i < length; i++) {
					audioBytes[i] = (byte) (audioData[i] * 127 + 127);
				}
			}
		}
	}
}
