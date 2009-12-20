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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Vector;
import javax.sound.sampled.AudioFormat;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

import util.RTSI;

import com.meapsoft.featextractors.AvgMelSpec;

/**
 * Abstract class that all MEAPsoft utilities must extend. Defines some global
 * constants and useful static methods. Based on Mike Mandel's Meap.java
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 * @author Mike Mandel (mim@ee.columbia.edu)
 */
public abstract class MEAPUtil implements Runnable
{
	public static final String version = "2.0.2";

	public static final String slash = System.getProperty("file.separator");

	// error messages
	// public static final int FATAL_ERROR = 0;
	// public static final int MESSAGE = 1;

	// Gloabal audio format parameters
	public static final int numChannels = 1;

	public static final int bitsPerSamp = 16;

	public static final int samplingRate = 44100;

	// public static final int samplingRate = 22050;
	public static final boolean signed = true;

	public static final boolean bigEndian = false;

	AudioFormat format = new AudioFormat(samplingRate, bitsPerSamp,
			numChannels, signed, bigEndian);

	// Should we write the output MEAPFile(s)? - not always necessary
	// when using the GUI. Defaults to true.
	public static boolean writeMEAPFile = true;

	// Should we print verbose output?
	// Default is false because this might be called from a GUI.
	protected boolean verbose = false;

	// keep track of this MEAPsoft utlity's progress
	protected BoundedRangeModel progress = new DefaultBoundedRangeModel();

	protected static ExceptionHandler exceptionHandler = new ExceptionHandler();

	// All MEAPUtils need to implement this interface:
	// void setup() (optional - this class implements an empty version)
	// void run()

	public void setup() throws IOException, ParserException
	{
	}

	/**
	 * Set everything up, process input, and write output.
	 */
	public abstract void run();

	public static void printCommandLineOptions(char arg)
	{
		switch (arg)
		{
		case 'f':
			System.out
					.println("    -f feat_name    use feat_name features (defaults to AvgMelSpec, can have multiple -f arguments)\n"
							+ "Supported feat_names are: " + "");
			RTSI.find("com.meapsoft.featextractors",
					"com.meapsoft.featextractors.FeatureExtractor");
			break;
		case 'd':
			System.out
					.println("    -d dist_metric  distance metric to use (defaults to EuclideanDist, can string them together with multiple -d arguments)\n"
							+ "Supported distance metrics are: " + "");
			// this doesn't work unless you have a package name...
			RTSI.find("com.meapsoft", "com.meapsoft.ChunkDist");
			break;
		case 'i':
			System.out
					.println("    -i feat_dim     what feature dimentions to use (defaults to all)\n"
							+ "                    where feat_dim is a comma separated list (no spaces)\n"
							+ "                    of integer indices and ranges (e.g. 1-5,7:9,11)"
							+ "");
			break;
		}
	}

	public static void printCommandLineOptions(char[] args)
	{
		for (int x = 0; x < args.length; x++)
			printCommandLineOptions(args[x]);
	}

	/**
	 * Parse arguments common to many MEAPUtils - array of feature dimensions.
	 */
	public static int[] parseFeatDim(String[] args, String argString)
	{
		Vector features = new Vector();

		Getopt opt = new Getopt("MEAPUtil", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			if (c == 'i')
			{
				String[] dims = opt.getOptarg().split(",");
				for (int x = 0; x < dims.length; x++)
				{
					String[] range = dims[x].split("[:-]", 2);
					int start = Integer.parseInt(range[0]);
					features.add(new Integer(start));

					if (range.length > 1)
					{
						int end = Integer.parseInt(range[1]);
						for (int y = start + 1; y <= end; y++)
							features.add(new Integer(y));
					}
				}
			}
		}

		if (features.size() != 0)
		{
			int[] featdim = new int[features.size()];

			for (int x = 0; x < featdim.length; x++)
				featdim[x] = ((Integer) features.get(x)).intValue();

			return featdim;
		}

		return null;
	}

	/**
	 * Parse arguments common to many MEAPUtils - Distance metrics
	 */
	public static ChunkDist parseChunkDist(String[] args, String argString,
			int[] featdim)
	{
		ChunkDist dist = null;

		Getopt opt = new Getopt("MEAPUtil", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			if (c == 'd')
			{
				String className = opt.getOptarg();

				// Try to load the class named className that extends
				// ChunkDist. (This is ugly as hell)
				Class cl = null;
				try
				{
					cl = Class.forName(className);
				}
				catch (ClassNotFoundException e)
				{
					System.out.println(e);
				}

				try
				{
					if (cl != null
							&& Class.forName("ChunkDist").isAssignableFrom(cl))
					{
						try
						{
							// ChunkDist cd =
							// (ChunkDist)cl.newInstance(featdim);
							ChunkDist cd = null;
							if (featdim == null)
								cd = (ChunkDist) cl.newInstance();
							else
							{
								Class[] ctargs = new Class[1];
								Object arg = Array.newInstance(Integer.TYPE,
										featdim.length);
								ctargs[0] = arg.getClass();
								Constructor ct = cl.getConstructor(ctargs);

								for (int x = 0; x < featdim.length; x++)
									Array.setInt(arg, x, featdim[x]);

								Object[] o = new Object[1];
								o[0] = arg;
								cd = (ChunkDist) ct.newInstance(o);
							}

							if (dist == null)
								dist = cd;
							else
							{
								// tack cd on to the end of the list

								// of ChunkDists specified in dist
								ChunkDist curr = dist;
								for (curr = dist; curr.next != null; curr = curr.next)
									;
								curr.next = cd;
							}
						}
						catch (Exception e)
						{
							System.out.println("Error constructing ChunkDist "
									+ className);
							e.printStackTrace();
						}
					}
					else
						System.out.println("Ignoring unknown distance metric: "
								+ className);
				}
				catch (ClassNotFoundException e)
				{
					System.out.println("This should never ever happen....");
					e.printStackTrace();
				}
			}
		}

		// default
		if (dist == null)
			dist = new EuclideanDist(featdim);

		return dist;
	}

	/**
	 * Parse arguments common to many MEAPUtils - feature extractors
	 */
	public static Vector parseFeatureExtractor(String[] args)
	{
		return MEAPUtil.parseFeatureExtractor(args, "f:");
	}

	/**
	 * Parse arguments common to many MEAPUtils - feature extractors
	 */
	public static Vector parseFeatureExtractor(String[] args, String argString)
	{
		Vector featExts = new Vector();

		Getopt opt = new Getopt("MEAPUtil", args, argString);
		opt.setOpterr(false);

		int c = -1;
		while ((c = opt.getopt()) != -1)
		{
			if (c == 'f')
			{
				String featName = opt.getOptarg();

				// Try to load the class named featName that extends
				// featextractors.FeatureExtractor.
				Class cl = null;
				try
				{
					cl = Class.forName("com.meapsoft.featextractors."
							+ featName);
				}
				catch (ClassNotFoundException e)
				{
					try
					{
						cl = Class.forName(featName);
					}
					catch (ClassNotFoundException e2)
					{
						System.out.println(e2);
					}
				}

				try
				{
					if (cl != null
							&& Class
									.forName(
											"com.meapsoft.featextractors.FeatureExtractor")
									.isAssignableFrom(cl))
					{
						try
						{
							featExts.add(cl.newInstance());
							// feat_names += featName + " ";
						}
						catch (Exception e)
						{
							System.out
									.println("Error constructing FeatureExtractor "
											+ featName);
							e.printStackTrace();
						}
					}
					else
						System.out.println("Ignoring unknown feature: "
								+ featName);
				}
				catch (ClassNotFoundException e)
				{
					System.out.println("This should never ever happen....");
					e.printStackTrace();
				}
			}
		}

		// default to AvgMelSpec
		if (featExts.size() == 0)
			featExts.add(new AvgMelSpec());

		return featExts;
	}

	public void setExceptionHandler(ExceptionHandler eh)
	{
		exceptionHandler = eh;
	}

	/**
	 * Get the BoundedRangeModel that is keeping track of this MEAPUtil's
	 * progress.
	 */
	public BoundedRangeModel getProgress()
	{
		return progress;
	}

	public void setProgress(BoundedRangeModel p)
	{
		progress = p;
	}

	public static String[] getPaths()
	{
		String meapsoftDirectory = null;
		String dataDirectory = null;

		try
		{
			// This method will get the path to the jar or class file no-matter
			// where the jar is called from.

			String javaclasspath = System.getProperty("java.class.path").split(
					System.getProperty("path.separator"))[0];

			File binPath = new File(javaclasspath);

			binPath = binPath.getCanonicalFile().getParentFile();
			// System.out.println("Path: " + binPath.toString());

			meapsoftDirectory = binPath.getParent();
			dataDirectory = meapsoftDirectory + slash + "data";

			// System.out.println("meapsoftDirectory: " + meapsoftDirectory + "
			// dataDirectory: " + dataDirectory);
		}
		catch (Exception e)
		{
			System.out.println("problem getting paths! " + e.toString());
			// ShowDialog(e, "", FATAL_ERROR);
			return null;
		}

		String[] paths = { meapsoftDirectory, dataDirectory };
		return paths;
	}
	
	public static String getShortFeatureName(String fullName)
	{
		String[] parts = fullName.split("\\.");
		return parts[parts.length - 1];
	}
	
	public static void copyFile(File src, File dst) throws IOException 
	{
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
    
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
