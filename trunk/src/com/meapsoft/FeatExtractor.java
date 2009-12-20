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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.meapsoft.featextractors.AvgMelSpec;
import com.meapsoft.featextractors.FeatureExtractor;
import com.meapsoft.featextractors.MetaFeatureExtractor;

/**
 * Program that extracts features from the chunks listed in the input files.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class FeatExtractor extends MEAPUtil
{
	// Files to use
	private FeatFile[] featFiles;

	private FeatFile outFile = null;

	// all of our feature extractors...
	private Vector featExts;

	// and their associated weights
	private Vector featExtWeights;

	// names of our feature extractors
	private String feat_names = "";

	// should this FeatExtractor clear any non meta features?
	private boolean clearNonMetaFeatures = true;

	public static final int feSamplingRate = 22050;

	// big enough to get good frequency resolution for AvgChroma
	public static int nfft = 1024;

	public static int nhop = 256;

	// if this buffer is smaller than a chunk's length, that chunk's
	// features not be calculated correctly
	// Doulgas upped this to from 2000 to 4000. Shouldn't this be dynamic
	// or set depending on the input .seg file?
	
	// yes, now we're setting this dynamically in setup()
	private int stftBufferSize = 4000;

	/**
	 * FeatExtractor constructor. If no extractors is empty, defaults to
	 * AvgMelSpec.
	 */
	public FeatExtractor(String infile, String outfile, Vector extractors)
	{
		this(new FeatFile(infile), new FeatFile(outfile), extractors);
	}

	/**
	 * FeatExtractor constructor. If no extractors is empty, defaults to
	 * AvgMelSpec.
	 */
	public FeatExtractor(FeatFile infile, FeatFile outfile, Vector extractors)
	{
		this((FeatFile[]) null, outfile, extractors);

		featFiles = new FeatFile[1];
		featFiles[0] = infile;
		
		//System.out.println("FeatExtractor constructor. outfile: " + outfile.filename + " infile: " + infile.filename);
	}

	/**
	 * FeatExtractor constructor. If no extractors is empty, defaults to
	 * AvgMelSpec.
	 */
	public FeatExtractor(FeatFile[] infiles, FeatFile outfile, Vector extractors)
	{
		featFiles = infiles;
		outFile = outfile;
		featExts = extractors;

		if (extractors.size() == 0)
			extractors.add(new AvgMelSpec());
			}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: FeatExtractor [-options] file1.feat file2.feat ... \n\n"
						+ "  where options include:\n"
						+ "    -o output_file  append features into output file (defaults to input file)\n"
						+ "    -w winSize      set STFT window size in seconds (defaults to "
						+ getWindowSize()
						+ ")\n"
						+ "    -o hopSize      set STFT hop size in seconds (defaults to "
						+ getHopSize() + ")" + "");
		printCommandLineOptions('f');
		System.out.println();
		System.exit(0);
	}

	/**
	 * FeatExtractor constructor. Parses command line arguments
	 */
	public FeatExtractor(String[] args)
	{
		if (args.length == 0)
			printUsageAndExit();

		// Parse arguments
		String argString = "f:o:w:h:";
		featExts = parseFeatureExtractor(args, argString);

		Getopt opt = new Getopt("FeatExtracter", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			switch (c)
			{
			case 'o':
				outFile = new FeatFile(opt.getOptarg());
				break;
			case 'h':
				setHopSize(Double.parseDouble(opt.getOptarg()));
				break;
			case 'w':
				setWindowSize(Double.parseDouble(opt.getOptarg()));
				break;
			case 'f': // already handled above
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

		featFiles = new FeatFile[args.length - ind];
		for (int i = ind; i < args.length; i++)
			featFiles[i - ind] = new FeatFile(args[i]);

		// What are the feature names?
		for (int i = 0; i < featExts.size(); i++)
			feat_names += featExts.get(i).getClass().getName() + " ";
		// chop off last space
		feat_names = feat_names.substring(0, feat_names.length() - 1);
	}

	public void setup() throws IOException, ParserException
	{
		double longestChunk = 0.0;
		
		//System.out.println("featFiles[0].filename: " + featFiles[0].filename);
		//System.out.println("featFiles.length: " + featFiles.length);
		
		for (int i = 0; i < featFiles.length; i++)
		{
			if (!featFiles[i].haveReadFile)
				featFiles[i].readFile();
			
			List chunks = featFiles[i].chunks;
			int numChunks = chunks.size();
			
			//System.out.println("numChunks: " + numChunks);
			for (int j = 0; j < numChunks; j++)
			{
				FeatChunk fC = (FeatChunk)chunks.get(j);
				
				double length = fC.length;
				
				if (length > longestChunk)
					longestChunk = length;
				
				//System.out.println("i: " + i + " j: " + j + " length: " + length + " longest: " + longestChunk);
			}
		}

		stftBufferSize = (int) ((longestChunk * samplingRate)/nhop);
		
		//really short chunks can generate zero length buffers because of int rounding above!
		if (stftBufferSize < 10)
			stftBufferSize = 10;
		//System.out.println("longestChunk: " + longestChunk + " stftBufferSize: " + stftBufferSize);
	}

	/**
	 * Where the magic happens. Extract features from featFiles.
	 */
	public FeatFile[] processFeatFiles() throws IOException,
			UnsupportedAudioFileException
	{
		for (int i = 0; i < featFiles.length; i++)
			processFeatFile(featFiles[i]);

		return featFiles;
	}

	/**
	 * Where the magic happens. Extract features from file.
	 */
	public FeatFile processFeatFile(FeatFile f) throws IOException,
			UnsupportedAudioFileException
	{
		FeatFile file = (FeatFile) f.clone();

		// keep track of our progress in extracting features from this FeatFile:
		progress.setMinimum(0);
		progress.setMaximum(file.chunks.size() * featExts.size());
		progress.setValue(0);

		STFT stft = null;

		boolean wroteFeatDesc = false;

		String lastAudioFile = "";
		file.chunks = new Vector(file.chunks);
		Collections.sort(file.chunks);
		Iterator c = file.chunks.iterator();

		// System.out.println("doing regular feature extractors...");

		while (c.hasNext())
		{
			FeatChunk ch = (FeatChunk) c.next();

			// let's get some new features
			if (!ch.srcFile.equals(lastAudioFile))
			{
				AudioReader reader = AudioReaderFactory.getAudioReader(
						ch.srcFile, format);
				stft = new STFT(reader, nfft, nhop, stftBufferSize);
			}
			lastAudioFile = ch.srcFile;

			// compute features from the STFT
			ListIterator i = featExts.listIterator();
			while (i.hasNext())
			{
				FeatureExtractor fe = (FeatureExtractor) i.next();
				// we don't want to run meta feature extractors yet!
				if (!(fe instanceof MetaFeatureExtractor))
				{
					//System.out.println("\ndoing: " + fe.description().substring(0,30));
					long chunkStartFrame = stft.seconds2fr(ch.startTime);
					int nframes = (int) stft.seconds2fr(ch.length);
					long chunkEndFrame = chunkStartFrame + nframes;

					// make sure stft contains valid data for us.
					long lastFrame = stft.getLastFrameAddress();
					
					int numFramesToRead = 0;
					int framesRead = -1;
					
					if (chunkStartFrame > lastFrame)
					{
						numFramesToRead = (int) (chunkStartFrame - lastFrame + nframes + 1);
						framesRead = stft.readFrames(numFramesToRead);
						//System.out.println("chunkStartFrame > lastFrame");
					}
					else if (chunkEndFrame > lastFrame)
					{
						numFramesToRead = (int) (chunkEndFrame - lastFrame + 1);
						framesRead = stft.readFrames(numFramesToRead);
						//System.out.println("chunkEndFrame > lastFrame");
					}
					
					//System.out.println("chunkStartFrame: " + chunkStartFrame + " nframes: " + nframes +
					//		" chunkEndFrame: " + chunkEndFrame + " numFramesToRead: " + numFramesToRead +
					//		" framesRead: " + framesRead + " lastFrame: " + lastFrame);
					double[] feats = fe.features(stft, (int) chunkStartFrame,
							nframes, true);

					ch.addFeature(feats);

					// what features are we adding?
					if (!wroteFeatDesc)
					{
						String fullName = fe.getClass().getName();
						//System.out.println("fullName: " + fullName);
						String[] nameParts = fullName.split("\\.");
						String featString = nameParts[nameParts.length - 1] + "("
								+ feats.length + ") ";
						//System.out.println("featString: " + featString);
						if (featExtWeights != null)
						{
							int idx = i.nextIndex() - 1;
							if (idx < featExtWeights.size())
								featString = featExtWeights.get(idx) + "*"
										+ featString;
						}
						file.featureDescriptions.add(featString);
					}
				}

				progress.setValue(progress.getValue() + 1);
			}

			wroteFeatDesc = true;
		}

		// now do meta feature extractors
		boolean descriptionsCleared = false;
		ListIterator i = featExts.listIterator();
		while (i.hasNext())
		{
			FeatureExtractor fe = (FeatureExtractor) i.next();
			if (fe instanceof MetaFeatureExtractor)
			{
				if (!descriptionsCleared)
				{
					if (clearNonMetaFeatures)
						file.featureDescriptions.clear();

					file.normalizeFeatures();
					file.applyFeatureWeights();

					descriptionsCleared = true;
				}

				// this obliterates any other features
				((MetaFeatureExtractor) fe)
						.features(file, clearNonMetaFeatures);

				// what features are we adding?
				String featString = fe.getClass().getName() + "(" + 1 + ") ";
				if (featExtWeights != null)
				{
					int idx = i.nextIndex() - 1;
					if (idx < featExtWeights.size())
						featString = featExtWeights.get(idx) + "*" + featString;
				}
				file.featureDescriptions.add(featString);

				progress.setValue(progress.getValue() + 1);
			}
		}

		stft.stop();

		if (outFile != null)
		{
			outFile.chunks.addAll(file.chunks);
			outFile.featureDescriptions = new Vector(file.featureDescriptions);

			// outFile now contains some chunks.
			outFile.haveReadFile = true;
		}

		return file;
	}

	/**
	 * Set everything up, process input, and write output.
	 */
	public void run()
	{
		try
		{
			setup();
		}
		catch (Exception e)
		{
			exceptionHandler.handleException(e);
		}

		FeatFile fn = outFile;

		// process supplied files
		for (int i = 0; i < featFiles.length; i++)
		{
			if (outFile == null)
				fn = featFiles[i];

			if (verbose)
				System.out.println("Extracting features (" + feat_names
						+ ") from " + featFiles[i].filename + " to "
						+ fn.filename + ".");

			long startTime = System.currentTimeMillis();
			try
			{
				FeatFile f = processFeatFile(featFiles[i]);
				if (writeMEAPFile)
					f.writeFile(fn.filename);
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

	/**
	 * Set weights associated with the different FeatureExtractors used by this
	 * object.
	 */
	public void setFeatureExtractorWeights(Vector v)
	{
		featExtWeights = v;
	}

	/**
	 * Should this FeatExtractor clear any non meta features?
	 */
	public void setClearNonMetaFeatures(boolean clearNonMF)
	{
		clearNonMetaFeatures = clearNonMF;
	}

	/**
	 * Set the STFT window size for the feature extractors to use.
	 * 
	 * @param winSize -
	 *            window size in seconds
	 */
	public void setWindowSize(double winSize)
	{
		nfft = (int) (winSize * feSamplingRate);
		nfft = (int) (winSize);

		System.out.println("window size = " + winSize + ", nfft = " + nfft);
	}

	/**
	 * Set the STFT hop size for the feature extractors to use.
	 * 
	 * @param hopSize -
	 *            hop size in seconds
	 */
	public void setHopSize(double hopSize)
	{
		nhop = (int) (hopSize * feSamplingRate);
		nhop = (int) (hopSize);

		System.out.println("hop size = " + hopSize + ", nhop = " + nhop);
	}

	/**
	 * Get the STFT window size used by the feature extractors.
	 */
	public double getWindowSize()
	{
		return (double) nfft / feSamplingRate;
	}

	/**
	 * Get the STFT hop size used by the feature extractors.
	 */
	public double getHopSize()
	{
		return (double) nhop / feSamplingRate;
	}

	public static void main(String[] args)
	{
		FeatExtractor o2or = new FeatExtractor(args);
		o2or.verbose = true;
		o2or.run();
		System.exit(0);
	}
}
