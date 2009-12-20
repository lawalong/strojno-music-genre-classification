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

package com.meapsoft.visualizer;

import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.meapsoft.AudioReader;
import com.meapsoft.AudioReaderFactory;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatExtractor;
import com.meapsoft.FeatFile;
import com.meapsoft.MEAPUtil;
import com.meapsoft.featextractors.AvgMelSpec;

/**
 * 
 * @author douglas@music.columbia.edu
 * 
 */

public class SingleFeatureSpectrumPanel extends SingleFeatureColorBarsPanel
{
	private static final long serialVersionUID = 1L;

	List shortEvents = new Vector();

	int numShortChunks = 0;

	boolean updating = false;

	// 2 means recompute waveform every 2 zoom levels
	public int zoomLevelRecomputeMod = 2;

	public SingleFeatureSpectrumPanel()
	{
		numDrawableFeatures = -1;
	}

	public String getDisplayType()
	{
		return "Spectrum";
	}

	// override initialize for special spectrum weirdness
	public int initialize(FeatFile featFile, String featureName)
	{
		this.featFile = featFile;
		this.featureName = featureName;

		highestValue = 0.0d;
		lowestValue = 0.0d;
		featureRange = 0.0;
		firstEventTime = 0.0;
		lastEventTime = 0.0;
		endTime = 0.0;
		timeRange = 0.0;

		featureSize = 1;

		events = featFile.chunks;

		// extract feature data, find highest/lowest feature values
		numChunks = events.size();
		// numChunks = featFile.chunks.size();

		for (int i = 0; i < numChunks; i++)
		{
			for (int j = 0; j < featureSize; j++)
			{
				FeatChunk fC = (FeatChunk) events.get(i);

				/*
				 * int[] dim = {featureNumber + j}; double feature =
				 * fC.getFeatures(dim)[0];
				 * 
				 * 
				 * if (feature < lowestValue) lowestValue = feature;
				 * 
				 * if (feature > highestValue) highestValue = feature;
				 */
				if (fC.startTime < firstEventTime)
					firstEventTime = fC.startTime;

				if (fC.startTime > lastEventTime)
					lastEventTime = fC.startTime;

				if (fC.startTime + fC.length > endTime)
					endTime = fC.startTime + fC.length;
			}
		}

		featureRange = 1.0;// highestValue - lowestValue;
		timeRange = endTime - firstEventTime;

		String fileNameParts[] = featFile.filename.split(slash);
		shortFileName = fileNameParts[fileNameParts.length - 1];

		initialized = true;

		return 1;
	}

	public void zoomIn()
	{
		super.zoomIn();
		if (this.zoomLevel % zoomLevelRecomputeMod == 1)
			updateSpectra();
	}

	public void zoomOut()
	{
		super.zoomOut();
		if (this.zoomLevel % zoomLevelRecomputeMod == 1)
			updateSpectra();
	}

	public void resetZoom()
	{
		super.resetZoom();
		updateSpectra();
	}

	public void incrFirstChunkToDraw()
	{
		super.incrFirstChunkToDraw();
		updateSpectra();
	}

	public void decrFirstChunkToDraw()
	{
		super.decrFirstChunkToDraw();
		updateSpectra();
	}

	// just for consistency from superclass...
	public void updateData()
	{
		updateSpectra();
	}

	public void updateSpectra()
	{
		// HEY! This is broken for segment files that have more than one
		// srcFile, you know?

		if (events.size() == 0)
		{
			// System.out.println("events.size() == 0, returning.");
			return;
		}
		else if (this.getWidth() == 0)
		{
			// System.out.println("width == 0, returning.");
			return;
		}

		updating = true;

		double zoomMulti = (zoomLevel * 4.0) / 4.0;
		int w = (int) (this.getWidth() * zoomMulti) - 1;
		FeatChunk fCTD = (FeatChunk) events.get(firstChunkToDraw);
		double localFirstEventTime = fCTD.startTime;

		AudioReader reader = null;

		// don't need hirez data for display...
		AudioFormat format = new AudioFormat(8000, 16, 1, MEAPUtil.signed,
				MEAPUtil.bigEndian);

		try
		{
			//reader = new AudioReader(fCTD.srcFile, format);
            reader = AudioReaderFactory.getAudioReader(fCTD.srcFile, format);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
			return;
		}
		catch (UnsupportedAudioFileException e1)
		{
			e1.printStackTrace();
			return;
		}

		int frameSize = format.getFrameSize();
		double frameRate = (double) format.getFrameRate();

		long fileFrameLength = reader.getFrameLength();
		double fileTimeLength = ((double) fileFrameLength / frameRate);//	/ frameSize;

		int framesPerPixel = (int) Math.ceil((double) fileFrameLength
				/ (double) w);
		double timePerPixel = ((double) framesPerPixel / frameRate);// / frameSize;

		firstEventTime = localFirstEventTime;
		timeRange = fileTimeLength - localFirstEventTime;

		//System.out.println("spectrum seyz: timeRange: " + timeRange + " fileTL: " + fileTimeLength +
		//		" firstET: " + firstEventTime);
		
		//System.out.println("spectrum seyz: frameSize: " + frameSize + " frameRate: " + frameRate +
			//	" fileFrameLength: " + fileFrameLength + " fileTimeLength: " + fileTimeLength +
			//	" framesPerPixel: " + framesPerPixel + " timePerPixel: " + timePerPixel + " w: " + w);
		
		FeatFile fF = new FeatFile("small_chunks_temp.feat");

		double currTime = localFirstEventTime;
		String srcFileName = fCTD.srcFile;

		for (int x = 0; x < w; x++)
		{
			FeatChunk fC = new FeatChunk(srcFileName, currTime, timePerPixel, null);
			fF.chunks.add(fC);
			currTime += timePerPixel;
		}

		try
		{
			fF.writeFile();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

		Vector extractors = new Vector();
		extractors.add(new AvgMelSpec());

		FeatFile spectrumFeats = new FeatFile("temp.feat");

		FeatExtractor fE = new FeatExtractor(fF, spectrumFeats, extractors);

		fE.setProgress(progress);

		try
		{
			fE.setup();
			fE.processFeatFiles();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		int elementsPerFeature[] = { 0 };

		elementsPerFeature = spectrumFeats.getFeatureLengths();
		featureSize = elementsPerFeature[0];

		featureNumber = 0;

		shortEvents = spectrumFeats.chunks;
		numShortChunks = shortEvents.size();

		for (int i = 0; i < numShortChunks; i++)
		{
			for (int j = 0; j < featureSize; j++)
			{
				FeatChunk fC = (FeatChunk) shortEvents.get(i);
				int[] dim = { featureNumber + j };
				double feature = fC.getFeatures(dim)[0];

				if (feature < lowestValue)
					lowestValue = feature;

				if (feature > highestValue)
					highestValue = feature;
			}
		}

		featureRange = highestValue - lowestValue;

		updating = false;
	}

	public void drawData(Graphics g)
	{
		// don't draw if we don't have any points!
		if (!initialized)
		{
			// System.out.println("can't draw, not initialized.");
			return;
		}
		else if (updating)
		{
			return;
		}
		// if we have data but we haven't collected waveformPoints yet...
		else if (initialized && shortEvents.size() == 0)
		{
			// System.out.println("seems to be first time drawing, need to
			// update spectra...");
			updateSpectra();
		}

		double zoomMulti = (zoomLevel * 4.0) / 4.0;
		int w = (int) (this.getWidth() * zoomMulti) - 1;
		int h = this.getHeight();

		double xScaler = w / timeRange;

		g.setColor(bgColor);
		g.fillRect(0, 0, w, h);

		double yIncr = (double) h / featureSize;

		double localFirstEventTime = ((FeatChunk) events
				.get(firstChunkToDraw)).startTime;
		
		//System.out.println("spectrum panel seyz: zoomMulti: " + zoomMulti + " w: " + w + " h: " + h +
			//	" xScaler: " + xScaler + " yIncr: " + yIncr);

		int x = 0;
		for (int i = firstChunkToDraw; i < numShortChunks && x < getWidth(); i++)
		{
			FeatChunk fC = (FeatChunk) shortEvents.get(i);
			x = (int) ((fC.startTime - localFirstEventTime) * xScaler);
			int width = (int) (fC.length * xScaler) + 1;

			//System.out.println("spectrum: x: " + x);
			// adjust to zero
			double dataPoints[] = fC.getFeatures();// featureData[i];
			// System.out.println("dataPoints.length: " + dataPoints.length);

			for (int j = 1; j < featureSize + 1; j++)
			{
				double dataPoint = dataPoints[j - 1] - lowestValue;
				double colorIndex = (dataPoint / featureRange) * 255.0;
				double y = j * yIncr;
				// System.out.println("i: " + i + " j: " + j + " x: " + x + " y:
				// " + y + " h: " + h + " yIncr: " + yIncr);
				// System.out.println("x: " + x + " y: " + y + "cI: " +
				// colorIndex);
				// try
				// {
				g.setColor(colormap.table[(int) colorIndex]);
				/*
				 * } catch (Exception e) { e.printStackTrace();
				 * System.out.println("j: " + j + " x: " + x + " y: " + y + "
				 * cI: " + colorIndex); System.out.println("dataPoints[j - 1]: " +
				 * dataPoints[j - 1]); System.out.println("lowestValue: " +
				 * lowestValue + " highestValue: " + highestValue + "
				 * featureRange: " + featureRange); return; }
				 */
				g.fillRect(x, h - (int) (y), width, (int) yIncr + 1);
			}
		}
	}

	public void componentResized(ComponentEvent e)
	{
		// System.out.println(e.getComponent().getClass().getName() + " ---
		// Resized ");
		updateSpectra();
	}

	public static void main(String[] args)
	{

		if (args.length < 2)
		{
			System.out
					.println("usage: SingleFeatureSpectrumPanel myfilename myfeaturename\n");
			System.exit(-1);
		}

		final String fileName = args[0];
		final String featureName = args[1];
		final FeatFile fF = new FeatFile(fileName);
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("SingleFeatureSpectrumPanel");

				try
				{
					fF.readFile();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.exit(-1);
				}
				SingleFeaturePanel sFP = new SingleFeatureSpectrumPanel();
				if (sFP.initialize(fF, featureName) == -1)
				{
					System.out.println("hmm, something wrong, bailing.");
					System.exit(-1);
				}
				sFP.setSize(600, 400);

				frame.setContentPane(sFP);
				frame.pack();
				frame.setVisible(true);
				frame.setBounds(100, 100, 600, 400);

				sFP.repaint();
			}
		});
	}
}
