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

package com.meapsoft.composers;

import gnu.getopt.Getopt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import com.meapsoft.ChunkDist;
import com.meapsoft.DSP;
import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.EuclideanDist;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;
import com.meapsoft.ParserException;

/**
 * Program that learns a vector quantizer from a FeatFile and uses it to
 * quantize the chunks in another FeatFile.
 * 
 * All about VQ: http://www.data-compression.com/vq.html
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class VQComposer extends Composer
{

	public static String oldDesc = "VQComposer trains a vector quantizer on the chunks in the input file.  It then uses it to quantize the chunks in another file.  For best results use the beat segmenter so each chunk has roughly the same length.";

	protected String outFileName = "vq.edl";

	// file to train the VQ
	protected FeatFile trainFile;

	// file to quantize. If this is null, the composer will output an
	// EDL containing the cluster template chunks.
	protected FeatFile featsToQuantize = null;

	// number of codewords to use
	protected int cbSize = 32;

	protected Vector templateChunks = new Vector(cbSize);

	// how many beats should we put in each state?
	protected int beatsPerCodeword = 4;

	// only supports euclidean distance for now
	protected ChunkDist dist = new EuclideanDist();

	protected int[] featdim = null;

	protected boolean debug = false;

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public VQComposer()
	{
		initNameAndDescription("Vector Quantizer", new String(oldDesc));
	}
	
	public VQComposer(FeatFile trainFN, EDLFile outFN)
	{
		if (trainFN == null || outFN == null)
			return;

		trainFile = trainFN;
		outFile = outFN;

		if (outFile == null)
			outFile = new EDLFile("");
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: VQComposer [-options] features.feat \n\n"
						+ "  where options include:\n"
						+ "    -o output_file   the file to write the output to (defaults to "
						+ outFileName
						+ ")\n"
						+ "    -g               debug mode\n"
						+ "    -f file.feat     feature file to quantize (uses training features file by default)\n"
						+ "    -b nbeats        number of beats each codeword should contain (defaults to "
						+ beatsPerCodeword
						+ ")\n"
						+ "    -q codebook_size number of templates to use in the VQ codebook (defaults to 8).");
		printCommandLineOptions('i');
		printCommandLineOptions('d');
		printCommandLineOptions('c');
		System.out.println();
		System.exit(0);
	}

	public VQComposer(String[] args)
	{
		if (args.length == 0)
			printUsageAndExit();

		// Vector features = new Vector();

		// Parse arguments
		String argString = "o:c:q:i:gd:f:b:";
		featdim = parseFeatDim(args, argString);
		dist = parseChunkDist(args, argString, featdim);
		parseCommands(args, argString);

		Getopt opt = new Getopt("VQComposer", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			switch (c)
			{
			case 'o':
				outFileName = opt.getOptarg();
				break;
			case 'g':
				debug = true;
				break;
			case 'q':
				cbSize = Integer.parseInt(opt.getOptarg());
				break;
			case 'b':
				beatsPerCodeword = Integer.parseInt(opt.getOptarg());
				break;
			case 'f':
				featsToQuantize = new FeatFile(opt.getOptarg());
				break;
			case 'c': // already handled above
				break;
			case 'd': // already handled above
				break;
			case 'i': // already handled above
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

		trainFile = new FeatFile(args[args.length - 1]);
		if (featsToQuantize == null)
			featsToQuantize = trainFile;
		outFile = new EDLFile(outFileName);

		System.out.println("Composing " + outFileName + " from "
				+ args[args.length - 1] + ".");
	}

	public void setup() throws IOException, ParserException
	{
		super.setup();

		if (!trainFile.haveReadFile)
			trainFile.readFile();

		if (trainFile.chunks.size() == 0)
			throw new ParserException(trainFile.filename, "No chunks found");

		trainFile = (FeatFile) trainFile.clone();
		trainFile.normalizeFeatures();
		trainFile.applyFeatureWeights();

		// To change the number of beats per state all we have to do
		// is modify the chunks in trainFile by joining every
		// beatsPerCodeword chunk into one a superchunk.
		Vector newChunks = new Vector();
		for (int x = 0; x < trainFile.chunks.size() - beatsPerCodeword + 1; x += beatsPerCodeword)
		{
			FeatChunk newChunk = (FeatChunk) ((FeatChunk) trainFile.chunks
					.get(x)).clone();

			// double length = 0;
			for (int y = 1; y < beatsPerCodeword; y++)
			{
				FeatChunk f = (FeatChunk) trainFile.chunks.get(x + y);

				newChunk.addFeature(f.getFeatures());
				newChunk.length += f.length;
			}

			newChunks.add(newChunk);
		}

		trainFile.chunks = newChunks;

		progress.setMaximum(trainFile.chunks.size());

		if (featsToQuantize != null)
		{
			if (!featsToQuantize.haveReadFile)
				featsToQuantize.readFile();

			// What if features don't match
			if (!featsToQuantize.isCompatibleWith(trainFile))
				throw new ParserException(trainFile.filename,
						"Features do not match those in "
								+ featsToQuantize.filename);

			featsToQuantize = (FeatFile) featsToQuantize.clone();
			featsToQuantize.normalizeFeatures();
			featsToQuantize.applyFeatureWeights();

			// To change the number of beats per state all we have to do
			// is modify the chunks in featFile by joining every
			// beatsPerCodeword chunk into one a superchunk.
			newChunks = new Vector();
			for (int x = 0; x < featsToQuantize.chunks.size()
					- beatsPerCodeword + 1; x += beatsPerCodeword)
			{
				FeatChunk newChunk = (FeatChunk) ((FeatChunk) featsToQuantize.chunks
						.get(x)).clone();

				// double length = 0;
				for (int y = 1; y < beatsPerCodeword; y++)
				{
					FeatChunk f = (FeatChunk) featsToQuantize.chunks.get(x + y);

					newChunk.addFeature(f.getFeatures());
					newChunk.length += f.length;
				}

				newChunks.add(newChunk);
			}

			featsToQuantize.chunks = newChunks;

			progress.setMaximum(featsToQuantize.chunks.size());
		}
	}

	/**
	 * Use the LBG splitting algorithm to learn a VQ codebook
	 */
	protected void learnCodebook(FeatFile trainFile)
	{
		double[][] features = trainFile.getFeatures();
		int ndat = features.length;
		int ndim = features[0].length;

		progress.setMaximum(progress.getMaximum()
				+ (int) (Math.log(cbSize) / Math.log(2)));

		// initial codebook:
		templateChunks = new Vector(cbSize);

		// create a placeholder template chunk
		FeatChunk template0 = new FeatChunk("templateChunk0", 0, 0, null);
		template0.setFeatures(DSP.mean(DSP.transpose(features)));
		templateChunks.add(template0);

		// distortions of between each codeword and each chunk
		double[][] distortion = new double[cbSize][ndat];
		for (int x = 0; x < distortion.length; x++)
			Arrays.fill(distortion[x], Double.MAX_VALUE);
		// indicies into cbMeans for each chunk
		int[] idx = new int[ndat];

		// how much should the means be nudged when splitting
		double delta = 1e-3;

		// start from one codeword and go from there
		for (int nValidCW = 2; nValidCW <= cbSize; nValidCW = Math.min(
				2 * nValidCW, cbSize))
		{
			if (debug)
				System.out
						.println("Splitting into " + nValidCW + " codewords.");

			// split codewords
			for (int c = 0; c < nValidCW; c += 2)
			{
				FeatChunk ch = (FeatChunk) templateChunks.get(c);
				ch.setFeatures(DSP.minus(ch.getFeatures(), delta));
				templateChunks.set(c, ch);

				FeatChunk newch = new FeatChunk("templateChunk" + c, 0, 0, null);
				newch.setFeatures(DSP.plus(ch.getFeatures(), delta));
				templateChunks.add(c + 1, newch);
			}

			double currTotalDist = 0;
			double prevTotalDist = Double.MAX_VALUE;
			do
			{
				prevTotalDist = currTotalDist;
				currTotalDist = 0;
				for (int c = 0; c < nValidCW; c++)
				{
					FeatChunk cw = (FeatChunk) templateChunks.get(c);
					for (int n = 0; n < ndat; n++)
					{
						FeatChunk ch = (FeatChunk) trainFile.chunks.get(n);
						distortion[c][n] = dist.distance(cw, ch);
						currTotalDist += distortion[c][n];
					}
				}

				// quantize
				for (int n = 0; n < ndat; n++)
					idx[n] = DSP.argmin(DSP.getColumn(distortion, n));

				// update means
				double[] newCW = new double[ndim];
				for (int c = 0; c < nValidCW; c++)
				{
					FeatChunk ch = (FeatChunk) templateChunks.get(c);
					Arrays.fill(newCW, 0);
					int nmatch = 0;
					for (int n = 0; n < ndat; n++)
					{
						if (idx[n] == c)
						{
							nmatch++;

							for (int i = 0; i < ndim; i++)
								newCW[i] += features[n][i];
						}
					}

					if (nmatch != 0)
						ch.setFeatures(DSP.rdivide(newCW, nmatch));
				}
				if (debug)
					System.out.println("  distortion = "
							+ Math.abs(currTotalDist - prevTotalDist));
			}
			while (Math.abs(currTotalDist - prevTotalDist) > 0.0);

			progress.setValue(progress.getValue() + 1);

			// make sure we exit the loop once we're done splitting
			if (nValidCW == cbSize)
				break;
		}

		// use the chunk closest to cbMeans as the template for each
		// codeword
		templateChunks = new Vector(cbSize);
		for (int c = 0; c < cbSize; c++)
		{
			int n = DSP.argmin(distortion[c]);
			templateChunks.add(c, trainFile.chunks.get(n));
		}
	}

	protected int quantizeChunk(FeatChunk f)
	{
		double minDist = Double.MAX_VALUE;
		int match = -1;
		for (int c = 0; c < templateChunks.size(); c++)
		{
			FeatChunk chunk = (FeatChunk) templateChunks.get(c);

			double d = dist.distance(chunk, f);

			if (d < minDist)
			{
				minDist = d;
				match = c;
			}
		}

		return match;
	}

	public EDLFile compose()
	{
		// learn vq codebook from trainFile
		learnCodebook(trainFile);

		if (featsToQuantize == null)
		{
			double currTime = 0.0;
			Iterator i = templateChunks.iterator();
			while (i.hasNext())
			{
				FeatChunk currChunk = (FeatChunk) i.next();

				EDLChunk nc = new EDLChunk((FeatChunk) templateChunks
						.get(quantizeChunk(currChunk)), currTime);
				outFile.chunks.add(nc);

				currTime += nc.length;
				progress.setValue(progress.getValue() + 1);
			}

		}
		else
		// quantize featsToQuantize
		{
			Iterator i = featsToQuantize.chunks.iterator();
			while (i.hasNext())
			{
				FeatChunk currChunk = (FeatChunk) i.next();

				EDLChunk nc = new EDLChunk((FeatChunk) templateChunks
						.get(quantizeChunk(currChunk)), currChunk.startTime);

				outFile.chunks.add(nc);
				progress.setValue(progress.getValue() + 1);
			}
		}

		return outFile;
	}

	public void setCodebookSize(int cb)
	{
		cbSize = cb;

		if (cbSize == 0)
			cbSize = 1;
	}

	public void setBeatsPerCodeword(int nbeats)
	{
		if (nbeats > 0)
			beatsPerCodeword = nbeats;
	}

	public void setFeatsToQuantize(FeatFile featFile)
	{
		featsToQuantize = featFile;
	}

	public static void main(String[] args)
	{
		VQComposer m = new VQComposer(args);
		long startTime = System.currentTimeMillis();
		m.run();
		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
