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

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import com.meapsoft.DSP;
import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;

/**
 * Program that learns a simple fully connected hidden Markov model from a
 * FeatFile and generates chunk sequences from it.
 * 
 * This doesn't produce very compelling compositions because the markov
 * assumption (that the currect chunk only depends on the previous chunk) is not
 * at all valid for most music which has a more complex structure. Later
 * versions will support more constrained HMM topologies that should make for
 * more interesting compositions.
 * 
 * All about HMMs: http://en.wikipedia.org/wiki/Hidden_Markov_model
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class HMMComposer extends VQComposer
{
	public static String oldDesc = "HMMComposer uses a features file to train a simple statistical model of a song and uses it to randomly generate a new sequence of chunks.  This works best when used with chunks created by the beat detector.";

	private int sequenceLength = 50;

	// prior probability of starting in a given state
	private double[] startProbs;

	// probability of transitioning from one state to another
	private double[][] transitionMatrix;

	//@mike: empty constructor just to sniff this guy
	//please don't call this constructor, it will not work
	public HMMComposer()
	{
		initNameAndDescription("HMM Composer", new String(oldDesc));
	}
	
	public HMMComposer(FeatFile trainFN, EDLFile outFN)
	{
		super(trainFN, outFN);
	}

	public void printUsageAndExit()
	{
		System.out
				.println("Usage: HMMComposer [-options] features.feat \n\n"
						+ "  where options include:\n"
						+ "    -o output_file   the file to write the output to (defaults to "
						+ outFileName
						+ ")\n"
						+ "    -g               debug mode\n"
						+ "    -q codebook_size number of states in the HMM (defaults to "
						+ cbSize
						+ ")\n"
						+ "    -b nbeats        number of beats each HMM state should contain (defaults to "
						+ beatsPerCodeword
						+ ")\n"
						+ "    -s sequence_len  length of chunk sequence to generate (defaults to "
						+ sequenceLength + ").");
		printCommandLineOptions('i');
		printCommandLineOptions('d');
		printCommandLineOptions('c');
		System.out.println();
		System.exit(0);
	}

	public HMMComposer(String[] args)
	{
		// java demands that we do this
		super(null, null);

		if (args.length == 0)
			printUsageAndExit();

		// Vector features = new Vector();

		// Parse arguments
		String argString = "o:c:q:i:gd:s:b:";
		featdim = parseFeatDim(args, argString);
		dist = parseChunkDist(args, argString, featdim);
		parseCommands(args, argString);

		Getopt opt = new Getopt("HMMComposer", args, argString);
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
			case 's':
				sequenceLength = Integer.parseInt(opt.getOptarg());
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
		outFile = new EDLFile(outFileName);

		System.out.println("Composing " + outFileName + " from "
				+ args[args.length - 1] + ".");
	}

	public void setSequenceLength(int len)
	{
		sequenceLength = len;
	}

	private void learnTransitionMatrix(FeatFile trainFile)
	{
		startProbs = new double[cbSize];
		Arrays.fill(startProbs, 0);

		transitionMatrix = new double[cbSize][cbSize];
		for (int x = 0; x < cbSize; x++)
			Arrays.fill(transitionMatrix[x], 0);

		// sort the chunks in order of increasing startTime, while
		// keeping all chunks from the same srcFile together
		trainFile = (FeatFile) trainFile.clone();
        Collections.sort(trainFile.chunks);

		int ndat = trainFile.chunks.size();
		int prevState = -1;
		String lastSrcFile = "";
		for (int n = 0; n < ndat; n++)
		{
			FeatChunk ch = (FeatChunk) trainFile.chunks.get(n);

			int currState = quantizeChunk(ch);

			// is this the beginning of a srcFile?
			if (!lastSrcFile.equals(ch.srcFile))
			{
				lastSrcFile = ch.srcFile;

				startProbs[currState] += 1.0;
			}
			else
				transitionMatrix[prevState][currState] += 1.0;

			prevState = currState;
		}

		// normalize probabilities
		double s = DSP.sum(startProbs);
		for (int x = 0; x < startProbs.length; x++)
			startProbs[x] /= s;

		for (int x = 0; x < transitionMatrix.length; x++)
		{
			s = DSP.sum(transitionMatrix[x]);

			for (int y = 0; y < transitionMatrix[x].length; y++)
				transitionMatrix[x][y] /= s;
		}

		if (debug)
		{
			FeatFile f = new FeatFile("tmp");
			f.chunks = templateChunks;
			DSP.imagesc(f.getFeatures(), "codebook");
			DSP.imagesc(transitionMatrix, "transitionMatrix");
			DSP.imagesc(startProbs, "startProbs");
		}
	}

	private int multinomialSample(double uniformRV, double[] prob)
	{
		if (uniformRV <= prob[0])
			return 0;

		double[] cdf = DSP.cumsum(prob);

		for (int x = 1; x < cdf.length; x++)
			if (uniformRV > cdf[x - 1] && uniformRV <= cdf[x])
				return x;

		return prob.length - 1;
	}

	public EDLFile compose()
	{
		learnCodebook(trainFile);

		learnTransitionMatrix(trainFile);

		// generate a sequence of chunks from the codebook and
		// transition matrix

		Random rand = new Random();
		double currTime = 0;

		// get first chunk from startProbs
		int lastIdx = multinomialSample(rand.nextDouble(), startProbs);
		EDLChunk nc = new EDLChunk((FeatChunk) templateChunks.get(lastIdx),
				currTime);
		outFile.chunks.add(nc);
		currTime += nc.length;
		progress.setValue(progress.getValue() + 1);

		// use transitionMatrix for the remaining chunks
		for (int x = 1; x < sequenceLength; x++)
		{
			int currIdx = multinomialSample(rand.nextDouble(),
					transitionMatrix[lastIdx]);

			nc = new EDLChunk((FeatChunk) templateChunks.get(currIdx), currTime);
			outFile.chunks.add(nc);
			currTime += nc.length;
			progress.setValue(progress.getValue() + 1);

			lastIdx = currIdx;
		}

		return outFile;
	}

	public static void main(String[] args)
	{
		HMMComposer m = new HMMComposer(args);
		long startTime = System.currentTimeMillis();
		m.run();
		System.out.println("Done. Took "
				+ ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
		System.exit(0);
	}
}
