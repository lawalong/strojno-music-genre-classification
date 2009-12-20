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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of a MEAPsoft segment/feature file.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class FeatFile extends MEAPFile implements Cloneable
{
	// Keep track of the types (and dimensions) of features contained
	// in the chunks in this file
	public Vector featureDescriptions = new Vector();

	// The FeatChunks contained in this file
	public List chunks;

	// regular expressions for parsing FeatFiles
	protected static final Pattern commentPattern = Pattern.compile("#\\.*");

	protected static final Pattern linePattern = Pattern
			.compile("\\s*([^#\\s]+)\\s*");

	// TODO: add feature weights and dimensions parsing into this pattern
	protected static final Pattern featDescPattern = Pattern
			.compile("^#\\s*filename\\tonset_time\\tchunk_length\\t\\s*");
			//.compile("^#\\s*Features:\\s*");

	// TODO: add feature weights and dimensions parsing into this pattern
	protected static final Pattern featDescPattern_alt = Pattern
			.compile("^#\\s*Features:\\s*");
	
	public FeatFile(String fn)
	{
		filename = fn;
		chunks = new Vector(100, 0);
	}

	// Java bitches if this is not present for some reason. Don't use
	// it - it's bad news.
	protected FeatFile()
	{
		this("BUG");
	}

	/**
	 * Get a matrix (2D double array) of all of the features in all of the
	 * chunks contained in this file. Each row corresponds to a single feature
	 * vector. Index it as you would in Matlab ([row][column] =
	 * [chunk][featdim]).
	 */
	public double[][] getFeatures()
	{
		return getFeatures(null);
	}

	/**
	 * Get a matrix (2D double array) of all of the features in all of the
	 * chunks contained in this file. Each row corresponds to a single feature
	 * vector. Index it as you would in Matlab ([row][column] =
	 * [chunk][featdim]).
	 */
	
	//this is dopey! Hopefully the convenience methods below
	//will make it so that we rarely need to use it raw...
	public double[][] getFeatures(int[] featdim)
	{
		// how many feature dimensions are we using?
		int maxdim = 0;
		if (featdim != null)
			maxdim = featdim.length;
		else
			maxdim = ((FeatChunk) chunks.get(0)).numFeatures();

		double[][] mat = new double[chunks.size()][maxdim];

		for (int x = 0; x < chunks.size(); x++)
		{
			FeatChunk c = (FeatChunk) chunks.get(x);

			double[] currFeat = c.getFeatures(featdim);
			for (int y = 0; y < currFeat.length; y++)
				mat[x][y] = currFeat[y];
		}

		return mat;
	}

	//return data for all chunks
	public double[][] getFeatureByName(String featName)
	{
		//System.out.println("getting feature: " + featName + " (all chunks)");
		
		int featNum = getFeatureNumberForName(featName);
		
		//we don't have that feature
		if (featNum == -1000)
		{
			System.out.println("I don't have a feature by that name: " + featName);
			return null;
		}
		
		//not sure what to do about waveform and spectrum right now...
		if (featNum == -2 || featNum == -3)
		{
			System.out.println("I can't return waveform or spectrum right now.");
			return null;	
		}
		
		//do normal features
		if (featNum >= 0)
		{
			double[][] features = getFeatures(getIndexRangeForFeatureNumber(featNum));
			return features;
		}	
		
		//special case for Length
		if (featNum == -1)
		{
			double[][] lengths = new double[chunks.size()][1];
			
			for (int i = 0; i < chunks.size(); i++)
			{
				FeatChunk fC = (FeatChunk) chunks.get(i);
				lengths[i][0] = fC.length;
			}
			
			return lengths;
		}
		
		return null;

	}
	
	//return data for specified chunk
	public double[] getFeatureByName(String featName, int chunkNum)
	{
		//System.out.println("getting feature: " + featName + " for chunkNum: " + chunkNum);

		if (chunkNum >= chunks.size())
		{
			System.out.println("chunkNum >= chunks.size()");
			return null;
		}
		
		int featNum = getFeatureNumberForName(featName);
		
		//we don't have that feature
		if (featNum == -1000)
		{
			System.out.println("I don't have a feature by that name: " + featName);
			return null;
		}
		
		//not sure what to do about waveform and spectrum right now...
		if (featNum == -2 || featNum == -3)
		{
			System.out.println("I can't return waveform or spectrum right now.");
			return null;	
		}
		
		//do normal features
		if (featNum >= 0)
		{
			double[][] features = getFeatures(getIndexRangeForFeatureNumber(featNum));
			return features[chunkNum];
		}	
		
		//special case for Length
		if (featNum == -1)
		{
			FeatChunk fC = (FeatChunk) chunks.get(chunkNum);
			
			double[] length = {fC.length};
			return length;
		}
		
		return null;

	}

	public int getFeatureNumberForName(String featName)
	{	
		//do length
		if (featName.endsWith("ength"))
			return -1;
		
		if (featName.endsWith("aveform"))
			return -2;
		
		if (featName.endsWith("ectrum"))
			return -3;
		
		int numFeats = featureDescriptions.size();
		//System.out.println("gFNFN numFeats: " + numFeats);
		for (int i = 0; i < numFeats; i++)
		{
			String desc = (String) featureDescriptions.get(i);
			//System.out.println("gFNFN trying: " + desc);
//			 dumb 1.4.2 way of telling if a string contains another string
			//if (desc.indexOf(featName) > 0)
			if (desc.contains(featName))
			{
				return i;
			}
		}
		
		System.out.println(featName + " doesn't seem to be a valid feature.");
		
		return -1000;
	}
	
	public int[] getIndexRangeForFeatureNumber(int featNum)
	{
		int numFeats = featureDescriptions.size();
		
		if (featNum >= numFeats)
		{
			System.out.println("featNum >= numFeats");
			return null;
		}
		
		int[] featLengths = getFeatureLengths();
		int featLength = featLengths[featNum];

		
		int indexOffset = 0;
		
		for (int i = 0; i < featNum; i++)
			indexOffset += featLengths[i];
		
		int[] indexs = new int[featLength];
		
		for (int i = 0; i < featLength; i++)
			indexs[i] = indexOffset + i;
		
		return indexs;
	}
	
	public boolean containsFeature(String featName)
	{
		if (getFeatureNumberForName(featName) > -1000)
			return true;
		else
			return false;
	}
	
	/**
	 * Return the number of elements in each feature
	 * 
	 * i.e. for AvgMelSpec (0-39) and AvgChroma (40-51) we return [40, 12]
	 * 
	 */

	public int[] getFeatureLengths()
	{
		int[] lengths = new int[featureDescriptions.size()];

		for (int i = 0; i < featureDescriptions.size(); i++)
		{
			int numDim = Integer.parseInt(((String) featureDescriptions.get(i))
					.split("[()]")[1]);
			lengths[i] = numDim;
		}
		return lengths;
	}


	
	/**
	 * Normalize the features contained in this file such that the feature
	 * dimensions corresponding to the outputs of each FeatureExtractor are
	 * normalized independently to lie between 0 and 1.
	 * 
	 * I.e. if this featfile contains outputs of both AvgMelSpec (dim 0-39) and
	 * AvgChroma (dim 40-51), dimensions 0-39 will be normalized independently
	 * of dimensions 40-51 and vice versa.
	 */
	public void normalizeFeatures()
	{
		// operate on each feature type separately
		int startDim = 0;
		for (int featType = 0; featType < featureDescriptions.size(); featType++)
		{
			int numDim = Integer.parseInt(((String) featureDescriptions
					.get(featType)).split("[()]")[1]);

			int[] featDim = new int[numDim];
			for (int x = 0; x < numDim; x++)
				featDim[x] = startDim + x;

			// find the max and min values in these dimensions
			double[][] feat = getFeatures(featDim);
			double minFeat = DSP.min(DSP.min(feat));
			double maxFeat = DSP.max(DSP.max(feat));

			for (int x = 0; x < chunks.size(); x++)
			{
				FeatChunk c = (FeatChunk) chunks.get(x);

				double[] currFeat = c.getFeatures(featDim);

				for (int d = 0; d < featDim.length; d++)
					c.setFeature(featDim[d], (currFeat[d] - minFeat)
							/ (maxFeat - minFeat));
			}

			startDim += numDim;
		}
	}

	/**
	 * Apply weights (as listed in the "# Features: x.x*FeatureExtractor(ndim)"
	 * line ) to the features contained in this file.
	 */
	public void applyFeatureWeights()
	{
		// operate on each feature type separately
		int startDim = 0;
		for (int featType = 0; featType < featureDescriptions.size(); featType++)
		{
			int numDim = Integer.parseInt(((String) featureDescriptions
					.get(featType)).split("[()]")[1]);

			double weight = 1.0;
			try
			{
				weight = Double.parseDouble(((String) featureDescriptions
						.get(featType)).split("[*]")[0]);
			}
			catch (NumberFormatException e)
			{
				// the featureDescription does not contain a weight
				continue;
			}

			int[] featDim = new int[numDim];
			for (int x = 0; x < numDim; x++)
				featDim[x] = startDim + x;

			for (int x = 0; x < chunks.size(); x++)
			{
				FeatChunk c = (FeatChunk) chunks.get(x);

				double[] currFeat = c.getFeatures(featDim);

				for (int d = 0; d < featDim.length; d++)
					c.setFeature(featDim[d], weight * currFeat[d]);
			}

			startDim += numDim;
		}
	}

	/**
	 * Read in a feature file
	 */
	public void readFile() throws IOException, ParserException
	{
		BufferedReader in = new BufferedReader(new FileReader(filename));

		String audioFile;
		double chunkStartTime;
		double chunkLength;

		// each line (excluding comments) should look like:
		// audioFile chunkStartTime chunkLength feature1 feature2 ...

		// Parse each line of the input file
		// boolean haveWrittenHeader = false;
		long lineno = 0;
		String line;
		while ((line = in.readLine()) != null)
		{
			//System.out.println("doing line: " + line);
			lineno++;

			// extract any comments from the current line
			String comment = "";
			Matcher c = commentPattern.matcher(line + "\n");
			if (c.find())
			{
				// comments go all the way to the end of the line
				comment = c.group() + line.substring(c.end()) + "\n";
				line = line.substring(0, c.start());


                Matcher[] matchers = {featDescPattern.matcher(comment),
                                      featDescPattern_alt.matcher(comment)};
                for (int x = 0; x < matchers.length; x++)
                {
                    if (matchers[x].find())
                    {
                        String featString = comment.substring(matchers[x].end()).trim();
                        if (featString.length() > 0)
                            featureDescriptions.addAll(Arrays.asList(featString.split("\\s+")));
                        break;
                    }
                }
			}
			Matcher p = linePattern.matcher(line);
			// is there anything else?
			if (!p.find())
				continue;
			audioFile = p.group(1);
			// decode spaces in the file name
			audioFile = audioFile.replaceAll("%20", " ");
			// System.out.println(audioFile);

			if (!p.find())
				throw new ParserException(filename, lineno,
						"Could not find chunk start time.");
			try
			{
				chunkStartTime = Double.parseDouble(p.group(1));
			}
			catch (NumberFormatException nfe)
			{
				throw new ParserException(filename, lineno,
						"Could not parse chunk start time \"" + p.group(1)
								+ "\".");
			}
			if (!p.find())
				throw new ParserException(filename, lineno,
						"Could not find chunk length.");
			try
			{
				chunkLength = Double.parseDouble(p.group(1));
			}
			catch (NumberFormatException nfe)
			{
				throw new ParserException(filename, lineno,
						"Could not parse chunk length \"" + p.group(1) + "\".");
			}

			FeatChunk ch = new FeatChunk(audioFile, chunkStartTime, chunkLength, featureDescriptions);
			ch.comment = comment;

			// save the remaining features on the line
			while (p.find())
			{
				// what kind of feature is this? If its not a double then its a
				// string;
				try
				{
					ch.addFeature(Double.parseDouble(p.group(1)));
				}
				catch (NumberFormatException e)
				{
					ch.addFeature(p.group(1));
				}
			}

			chunks.add(ch);
		}
		
		in.close();
		haveReadFile = true;
	}

	/**
	 * Remove any chunks in this file
	 */
	public void clearChunks()
	{
		chunks.clear();
	}

	/**
	 * Remove the features of all chunks in this file
	 */
	public void clearFeatures()
	{
		Iterator i = chunks.iterator();
		while (i.hasNext())
			((FeatChunk) i.next()).clearFeatures();
	}

	/**
	 * Write the contents of this FeatFile
	 */
	protected void write(Writer w) throws IOException
	{
		// write the header
		w.write("#filename\tonset_time\tchunk_length");
		// don't write "Features: " if we don't have any!
		if (featureDescriptions.size() > 0)
		{
			//w.write("# Features: ");

			for (int i = 0; i < featureDescriptions.size(); i++)
			{
				w.write("\t" + (String) featureDescriptions.get(i));
			}
			w.write("\n");
		}
		else
			w.write("\n");

		Iterator i = chunks.iterator();
		while (i.hasNext())
			w.write(i.next().toString());
	}

	/**
	 * Clone this FeatFile
	 */
	public Object clone()
	{
		FeatFile o = new FeatFile(this.filename);

		// superclass (MEAPFile) fields
		o.haveReadFile = this.haveReadFile;
		o.haveWrittenFile = this.haveWrittenFile;

		// local fields
		o.featureDescriptions = new Vector(this.featureDescriptions);

		o.chunks = new Vector(100);
		Iterator i = this.chunks.iterator();
		while (i.hasNext())
			o.chunks.add(((FeatChunk) i.next()).clone());

		return o;
	}

	/**
	 * Check if another FeatFile is compatible with this one. Two feature files
	 * are said to be incompatible if they contain different features.
	 */
	public boolean isCompatibleWith(FeatFile f)
	{
		// TODO:{Should really check if feature names match excluding
		// whitespace and other meaningless gunk.}

		// System.out.println(this.featureDescriptions.size()+" "+
		// f.featureDescriptions.size());

		if (this.featureDescriptions.size() != f.featureDescriptions.size())
			return false;

		for (int x = 0; x < this.featureDescriptions.size(); x++)
		{
			String featOne = ((String) this.featureDescriptions.get(x))
					.trim();
			String featTwo = ((String) f.featureDescriptions.get(x))
					.trim();

			// System.out.println(x+": "+featOne+" == "+ featTwo + " : " +
			// featOne.equalsIgnoreCase(featTwo));

			if (!featOne.equalsIgnoreCase(featTwo))
				return false;
		}

		return true;
	}
	
	public void dumpFeatsToTabsFile()
	{

		System.out.println("dumping tab-delimited file: " + filename + ".tabs");

		try
		{
			FeatFile inFF = new FeatFile(filename);
			inFF.readFile();

			FileWriter fWriter = null;

			fWriter = new FileWriter(filename + ".tabs");

			BufferedWriter outFile = new BufferedWriter(fWriter);

			int numFeatures = inFF.featureDescriptions.size();
			int[] featLengths = inFF.getFeatureLengths();

			outFile.write("filename\tonset_time\tchunk_length\t");
			
			for (int i = 0; i < numFeatures; i++)
			{
				String fullName = (String) inFF.featureDescriptions.get(i);
				String[] splitName = fullName.split("\\.");
				String name = splitName[splitName.length - 1];
			
				if (i > 0)
					outFile.write("\t");
				
				if (featLengths[i] > 1)
				{
					int numValues = featLengths[i];

					for (int j = 0; j < numValues; j++)
					{
						if (j > 0)
							outFile.write("\t");
						
						String newName = name + "[" + j + "]";
						outFile.write(newName);
					}
				}
				else
					outFile.write(name);
			}
			outFile.write("\n");

			double[][] features = inFF.getFeatures();

			int numChunks = features.length;
			int valuesPerChunk = features[0].length;

			for (int chunk = 0; chunk < numChunks; chunk++)
			{
				FeatChunk fC = (FeatChunk)chunks.get(chunk);
				outFile.write(fC.srcFile + "\t");
				outFile.write(fC.startTime + "\t");
				outFile.write(fC.length + "\t");
				
				for (int feat = 0; feat < valuesPerChunk; feat++)
				{
					if (feat > 0)
						outFile.write("\t");

						//System.out.println("numChunks: " + numChunks + " featLengths[" + featNum + "]: " + featLengths[featNum]);
						double value = features[chunk][feat];
						outFile.write("" + value);
				}
				outFile.write("\n");
			}

			outFile.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public String getFeatureStats()
	{
		String outString = "stats for: " + filename + "\n";
		
		FeatFile fF = new FeatFile(filename);
		try
		{
			fF.readFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		int numFeatures = fF.featureDescriptions.size();
		int numChunks = fF.chunks.size();
		
		outString += "numChunks: " + numChunks + "\n";
		
		outString += "features: " + "\n";
		for (int i = 0; i < numFeatures; i++)
			outString += fF.featureDescriptions.get(i) + "\n";
		outString += "\n";
		
		for (int featNum = 0; featNum < numFeatures; featNum++)
		{
			String featName = (String)fF.featureDescriptions.get(featNum);
			featName = MEAPUtil.getShortFeatureName(featName);
			
			outString += "stats for: " + featName + "\n";

			//double total = 0.0;
			double[] totals = null;
			double[] superValues = null;
			int numDimensions = -1;
			double lowest = Double.MAX_VALUE;
			//using Double.MIN_VALUE causes some strange behavior...
			double highest = -1000000;//Double.MIN_VALUE;
			int highestDimension = -1;
			int lowestDimension = -1;
			int highestChunk = -1;
			int lowestChunk = -1;
			
			for (int chunkNum = 0; chunkNum < numChunks; chunkNum++)
			{
				FeatChunk fC = (FeatChunk) fF.chunks.get(chunkNum);
				double values[] = fC.getFeatureByName(featName);
				
				if (numDimensions == -1)
				{
					numDimensions = values.length;
					totals = new double[numDimensions];
					superValues = new double[numChunks * values.length];
				}
				
				for (int i = 0; i < numDimensions; i++)
				{
					int superIndex = (chunkNum * numDimensions) + i;
					superValues[superIndex] = values[i];
					
					totals[i] += values[i];
					if (values[i] > highest)
					{
						highest = values[i];
						highestDimension = i;
						highestChunk = chunkNum;
					}
					if (values[i] < lowest)
					{
						lowest = values[i];
						lowestDimension = i;
						lowestChunk = chunkNum;
					}
				}
			}
			
			if (numDimensions > 1)
			{
				double totalTotal = 0.0;
					
				for (int i = 0; i < numDimensions; i++)
				{
					totalTotal += totals[i];
					
					outString += "total[" + i + "]: " + totals[i] + "\n";
					
					outString += "mean[" + i + "]: " + totals[i]/numChunks + "\n";
					
					Arrays.sort(superValues);
					
					if (superValues.length % 2 == 0)
					{
						double v1 = superValues[superValues.length/2];
						double v2 = superValues[(superValues.length/2 - 1)];
						double avg = (v1 + v2)/2.0;
						outString += "median[" + i + "]: " + avg + "\n";
						
					//	System.out.println("superValues.length: " + superValues.length + 
					//		" superValues.length/2: " + superValues.length/2 + 
					//			" (superValues.length/2 - 1): " + (superValues.length/2 - 1));
					}
					else
					{
						//System.out.println("superValues.length: " + superValues.length + 
						//		" superValues.length/2: " + superValues.length/2);
						outString += "median[" + i + "]: " + superValues[superValues.length/2] + "\n";
					}
					
				}
				outString += "total total: " + totalTotal + "\n";
				outString += "total mean: " + totalTotal/(numChunks * numDimensions) + "\n";
			}
			
			else
			{
				outString += "total: " + totals[0] + "\n";
				outString += "mean: " + totals[0]/numChunks + "\n";
				
				Arrays.sort(superValues);
				
				if (superValues.length % 2 == 0)
				{
					double v1 = superValues[superValues.length/2];
					double v2 = superValues[(superValues.length/2 - 1)];
					double avg = (v1 + v2)/2.0;
					outString += "median: " + avg + "\n";
				}
				else
				{
					outString += "median: " + superValues[superValues.length/2] + "\n";
				}
				
			}
			outString += "highest: " + highest + " at chunkNum: " + highestChunk + " dimension: " + highestDimension + "\n";
			outString += "lowest: " + lowest + " at chunkNum: " + lowestChunk + " dimension: " + lowestDimension + "\n";		
			outString += "range: " + (highest - lowest) + "\n";
			outString += "\n";
		}
		return outString;
	}
}
