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

import gnu.getopt.Getopt;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Program to segment input files and output text feature files compatible with
 * other MEAPsoft tools.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */

public class Segmenter extends MEAPUtil
{
	// Files to use
	String[] audioFiles;

	String outFileName = "out.segments";

	FeatFile outFile;

	// Extractor parts
	STFT stft;

	boolean useBeatOD = true;

	OnsetDetector detector;

	SegmentExtractor extractor;

	boolean onsetAtFirstFrame = false;

	int frameLatency = 200;

	// sensitivity of DpweOnsetDetector. higher is more sensitive
	// I think this goes from 0.0->3.0 ???
	double threshMult = 1.0;

	// length of smoothing window in seconds.
	double smtime = 0.1;

	double tempoMult = 1;

	public Segmenter(String[] inFiles, String outputFile)
	{
		audioFiles = inFiles;

		outFileName = outputFile;
		outFile = new FeatFile(outFileName);
	}

	public Segmenter(String inFile, String outputFile)
	{
		audioFiles = new String[1];
		audioFiles[0] = inFile;

		outFileName = outputFile;
		outFile = new FeatFile(outFileName);
	}

	public Segmenter(String[] inFiles, String outputFile, double thr,
			boolean beatOD, boolean oaff)
	{
		this(inFiles, outputFile);

		threshMult = thr;
		useBeatOD = beatOD;
		onsetAtFirstFrame = oaff;
	}

	public Segmenter(String[] inFiles, String outputFile, double thr,
			double smt, boolean beatOD, boolean oaff)
	{
		this(inFiles, outputFile);

		threshMult = thr;
		useBeatOD = beatOD;
		onsetAtFirstFrame = oaff;
		smtime = smt;
	}

	public Segmenter(String inFile, String outputFile, double thr,
			boolean beatOD, boolean oaff)
	{
		this(inFile, outputFile);

		threshMult = thr;
		useBeatOD = beatOD;
		onsetAtFirstFrame = oaff;
	}

	public Segmenter(String inFile, String outputFile, double thr, double smt,
			boolean beatOD, boolean oaff)
	{
		this(inFile, outputFile);

		threshMult = thr;
		useBeatOD = beatOD;
		onsetAtFirstFrame = oaff;
		smtime = smt;
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: Segmenter [-options] source1.wav source2.mp3 ... \n\n"
						+ "  where options include:\n"
						+ "    -o output_file  the file to write the output to (defaults to ./out.segments)\n"
						+ "    -t D.D  [1.0]   onset detector threshold (event detector), tempo multiplier (beat detector)\n"
						+ "    -s D.D  [0.1]   length of smoothing window in seconds for event detector\n"
						+ "    -d              use the old style onset detector (defaults to BeatOnsetDetector)\n"
						+ "    -0              add an onset at the very beginning of the file\n"
						+ "");
		System.exit(0);
	}

	/**
	 * Segmenter constructor. Parses command line arguments
	 */
	public Segmenter(String[] args)
	{
		if (args.length == 0)
			printUsageAndExit();

		// Parse arguments
		Getopt opt = new Getopt("Segmenter", args, "t:do:s:0");
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			switch (c)
			{
			case 't':
				threshMult = Double.parseDouble(opt.getOptarg());
				tempoMult = threshMult;
				break;
			case 's':
				smtime = Double.parseDouble(opt.getOptarg());
				break;
			case 'd':
				useBeatOD = false;
				break;
			case 'o':
				outFileName = opt.getOptarg();
				break;
			case '0':
				onsetAtFirstFrame = true;
				break;
			case '?':
				printUsageAndExit();
				break;
			default:
				System.out.print("getopt() returned " + c + "\n");
			}
		}

		// parse arguments
		int ind = opt.getOptind();
		if (ind > args.length)
			printUsageAndExit();

		audioFiles = new String[args.length - ind];
		for (int i = ind; i < args.length; i++)
			audioFiles[i - ind] = args[i];

		outFile = new FeatFile(outFileName);
	}

	public FeatFile processAudioFile() throws IOException,
			UnsupportedAudioFileException
	{
		return processAudioFile(outFile.filename);
	}

	public FeatFile processAudioFile(String fn) throws IOException,
			UnsupportedAudioFileException
	{
		// Create Extractor
		// ais = openInputStream(fn);
		// downsample to 8kHz
		AudioReader audioReader = AudioReaderFactory.getAudioReader(
            fn, new AudioFormat(DpweOnsetDetector.sr, bitsPerSamp, 1, signed,
                                bigEndian));

		// One sample per frame because we converted the file to mono.
		long sampleLength = audioReader.getFrameLength();
		// the mp3/flac decoders don't like to tell us the frame
		// length, so we have to make a guess. You might get an
		// error and progress bars will be off.
		if (sampleLength < 0)
		{
			sampleLength = 50000 * DpweOnsetDetector.swin / 2;

		}
		int numFrames = (int) sampleLength / (DpweOnsetDetector.swin / 2);

		// keep track of our progress in segmenting this file:
		progress.setMinimum(0);
		progress.setMaximum(numFrames);
		progress.setValue(0);

		// use 30ms window
		stft = new STFT(audioReader, DpweOnsetDetector.swin,
				DpweOnsetDetector.swin / 2, frameLatency);

		if (useBeatOD)
		{
			if (verbose)
				System.out.println("Using beat detector. TempoMult: " + tempoMult);
			
			// detector = new BeatOnsetDetector(stft, bandThresh, bandFrac);
			detector = new DpweBeatOnsetDetector(stft, numFrames, tempoMult);
		}
		else
		{
			if (verbose)
				System.out.println("Using event detector. threshMult: " + threshMult + " smtime: " + smtime);
			// detector = new OnsetDetector(stft, bandThresh, bandFrac);
			detector = new DpweOnsetDetector(stft, numFrames, threshMult,
					smtime);
		}
		extractor = new SegmentExtractor(stft, fn, outFile, progress);
		detector.addOnsetListener(extractor);

		// Hook things together
		stft.addFrameListener(extractor);

		// insert an onset at frame 0?
		if (onsetAtFirstFrame)
			extractor.newOnset(0, 0);

		// Extract the entire source file
		extractor.run();

		// outFile now contains some chunks.
		outFile.haveReadFile = true;

		return outFile;
	}

	/**
	 * Set everything up, process input, and write output.
	 */
	public void run()
	{
		// process supplied files
		for (int i = 0; i < audioFiles.length; i++)
		{

			if (verbose)
				System.out.println("Writing segments from " + audioFiles[i]
						+ " to " + outFileName + ".");

			long startTime = System.currentTimeMillis();

			try
			{
				processAudioFile(audioFiles[i]);

				if (writeMEAPFile)
					outFile.writeFile();
			}
			catch (Exception e)
			{
				exceptionHandler.handleException(e);
			}

			if (verbose)
				System.out.println("Done.  Took "
						+ ((System.currentTimeMillis() - startTime) / 1000.0)
						+ "s");
		}
	}

	// Set the tempo multiplier for the beat onset detector.
	public void setTempoMultiplier(double mult)
	{
		tempoMult = mult;
	}

	public FeatFile getSegFile()
	{
		return outFile;
	}

	public static void main(String[] args)
	{
		Segmenter o2or = new Segmenter(args);
		o2or.verbose = true;
		o2or.run();
		System.exit(0);
	}
}
