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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JFrame;

import com.meapsoft.AudioReader;
import com.meapsoft.AudioReaderFactory;
import com.meapsoft.EDLFile;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;
import com.meapsoft.MEAPUtil;

/**
 * 
 * @author douglas@music.columbia.edu
 *
 */

public class SingleFeatureWaveformPanel extends SingleFeaturePanel
{	
	private static final long serialVersionUID = 1L;
	
	Vector waveformPoints = new Vector(); 
	boolean waveformPointsValid = false;
	
	int totalNumSamples = 0;
	
	boolean updating = false;
	
	//2 means recompute spectrum every 2 zoom levels
	public int zoomLevelRecomputeMod = 2;
	
	public SingleFeatureWaveformPanel() 
	{
		super();
		
		numDrawableFeatures = 1;
		setProgress(new DefaultBoundedRangeModel());
	}
	
	public String getDisplayType() 
	{
		return "Waveform";
	}
	
	public int initialize(FeatFile featFile, String featureName)
	{
		return initialize(featFile, null, featureName);
	}

	public int initialize(FeatFile featFile, EDLFile edlFile, String featureName)
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
		
		totalNumSamples = 0;
		
		//System.out.println("initializing: " + featFile.filename);
		
		//check to see if our feature is in the input file
		featureNumber = featFile.getFeatureNumberForName(featureName);
			//verifyFeature(featureName);
		if (featureNumber == -1000)
		{
			System.out.println("hmm, I don't find that feature! Bye.");
			return -1;
		}
		
		featureSize = 1;		
		
		events = featFile.chunks;
	
		//extract feature data, find highest/lowest feature values
		numChunks = events.size();
		
		//System.out.println("numChunks: " + numChunks);
	
		double totalLength = 0.0;
		
		
		for (int i = 0; i < numChunks; i++)
		{			
			FeatChunk fC = (FeatChunk)events.get(i);
			
			if (fC.startTime < firstEventTime)
				firstEventTime = fC.startTime;
			
			if (fC.startTime > lastEventTime)
				lastEventTime = fC.startTime;
			
			if (fC.startTime + fC.length > endTime)
				endTime = fC.startTime + fC.length;
			
			totalLength += fC.length;	
			
		}
		timeRange = endTime - firstEventTime;

		//System.out.println("waveform sez: tR: " + timeRange +
		//		" eT: " + endTime + 
		//		" fET: " + firstEventTime + 
		//		" tR: " + timeRange);
		
//		System.out.println("fET: " + firstEventTime + " endTime: " + endTime + " timeRange: " + timeRange + 
//				" totalLength: " + totalLength);

		//FeatChunk lastChunk = (FeatChunk)events.lastElement();
		//System.out.println("last change sT + length: " + (lastChunk.startTime + lastChunk.length));
		
		String fileNameParts[] = featFile.filename.split(slash);
		shortFileName = fileNameParts[fileNameParts.length - 1];

		initialized = true;
		return 1;
	}

	public void drawData(Graphics g) 
	{	
		//don't draw if we don't have any points!
		if (!initialized)
		{
			//System.out.println("can't draw, not initialized.");
			return;
		}
		//avoid concurrent modification exceptions when drawing overlaps with window switch
		else if (updating)
		{
			return;
		}
		//if we have data but we haven't collected waveformPoints yet...
		else if (initialized && waveformPoints.size() == 0)
		{
			//System.out.println("seems to be first time drawing, need to updateWaveformPoints...");
			updateWaveformPoints();
		}
		
		//System.out.println("redrawing at zoomLevel: " + zoomLevel);
		double zoomMulti = (zoomLevel * 4.0)/4.0;
		int w = (int)(this.getWidth() * zoomMulti) - 1;
		int h = this.getHeight();
		
		g.setColor(bgColor);
		g.fillRect(0, 0, w, h);		
		g.setColor(fGColor);

		Iterator it = waveformPoints.iterator();

		double xIncr = (double)w/(double)waveformPoints.size();
		double x = 0.0;
		int xPrev = 0;
		
		//System.out.println("waveform panel seyz: zoomMulti: " + zoomMulti + " w: " + w + " h: " + h +
		//		" xIncr: " + xIncr);
		
		while (it.hasNext())
		{
			Point p = (Point)it.next();
			g.drawLine((int)x, p.yH, (int)x, p.yL);
			
			//take care of blank spots when xIncr > 1
			if ((int)x - xPrev > 1)
				g.drawLine((int)x - 1, p.yH, (int)x - 1, p.yL);

			xPrev = (int)x;
			x += xIncr;
		}
	}
	
	public void zoomIn()
	{
		super.zoomIn();
		if (this.zoomLevel % zoomLevelRecomputeMod == 1)
			updateWaveformPoints();
	}
	
	public void zoomOut()
	{
		super.zoomOut();
		if (this.zoomLevel % zoomLevelRecomputeMod == 1)
			updateWaveformPoints();
	}

	public void resetZoom()
	{
		super.resetZoom();
		updateWaveformPoints();
	}

	public void incrFirstChunkToDraw()
	{
		super.incrFirstChunkToDraw();
		updateWaveformPoints();
	}

	public void decrFirstChunkToDraw()
	{
		super.decrFirstChunkToDraw();
		updateWaveformPoints();
	}
	
	//just for consistency from superclass...
	public void updateData()
	{
		updateWaveformPoints();
	}
	
	public void updateWaveformPoints()
	{
		// TODO
		// HEY! This is broken for segment files that have more than one srcFile, you know?

		if (events.size() == 0)
		{
			//System.out.println("events.size() == 0, returning.");
			return;			
		}
		else if (this.getWidth() == 0)
		{
			//System.out.println("width == 0, returning.");
			return;
		}

		updating = true;
				
		waveformPoints = new Vector();
		
		double zoomMulti = (zoomLevel * 4.0)/4.0;
		int w = (int)(this.getWidth() * zoomMulti) - 1;
		int h = getHeight();
		//double xScaler = w/timeRange;
		double yScaler = h/2.0;
		FeatChunk fCTD = (FeatChunk)events.get(firstChunkToDraw);
		double localFirstEventTime = fCTD.startTime;
		
		
        // keep track of our progress:
        progress.setMinimum(0);
        progress.setMaximum(w); 
        progress.setValue(0);
        

		// don't need hirez data for display...
		//AudioFormat format = new AudioFormat(8000, 8, 1, MEAPUtil.signed,
		//		MEAPUtil.bigEndian);
		
		//don't need hirez data for waveform display...
	    AudioFormat format = new AudioFormat(8000, 16, 1, MEAPUtil.signed,
                                             MEAPUtil.bigEndian);
		
        AudioReader reader = null;
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
		
		//double startTime = fC.startTime;		
		
        int frameSize = format.getFrameSize();
        double frameRate = (double)format.getFrameRate();
        
		long fileFrameLength = reader.getFrameLength();
		double fileTimeLength = ((double)fileFrameLength / frameRate);// / frameSize;
		
		int framesPerPixel = (int) Math.ceil((double)fileFrameLength/(double)w);
		double timePerPixel = ((double)framesPerPixel / frameRate);// / frameSize;

		firstEventTime = localFirstEventTime;
		timeRange = fileTimeLength - localFirstEventTime;
		
		//System.out.println("waveform seyz: timeRange: " + timeRange + " fileTL: " + fileTimeLength +
		//		" firstET: " + firstEventTime);
		
		//System.out.println("waveform seyz: frameSize: " + frameSize + " frameRate: " + frameRate +
		//		" fileFrameLength: " + fileFrameLength + " fileTimeLength: " + fileTimeLength +
		//		" framesPerPixel: " + framesPerPixel + " timePerPixel: " + timePerPixel + " w: " + w);

        try
        {
            // One sample per frame because we converted the file to mono.
            long n = reader.skipSamples((long)(localFirstEventTime * frameRate));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

		int x = 0;
		while (x < w)
		{
			double[] samples = new double[framesPerPixel];
            //double[] samples = new double[100];
			try
			{
                int n = reader.readSamples(samples);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			int numSamps = samples.length;
			
			double highest = 0.0;
			double lowest = 0.0;
			
			for (int sampNum = 0; sampNum < numSamps; sampNum++)
			{
				double sample = samples[sampNum];
				
				if (sample > highest)
					highest = sample;
				else if (sample < lowest)
					lowest = sample;
			}
			
			int yL = (int)((lowest + 1.0) * yScaler);
			int yH = (int)((highest + 1.0) * yScaler);

			waveformPoints.add(new Point(yL, yH));
			
			x++;
			
			progress.setValue(progress.getValue()+1);
		}
		
		updating = false;
	}

    public void componentResized(ComponentEvent e) 
    {
    	//System.out.println(e.getComponent().getClass().getName() + " --- Resized "); 
    	updateWaveformPoints();
    }

	class Point
	{
		//int x;
		int yL;
		int yH;
		
		Point(int yL, int yH)
		{
			//this.x = x;
			this.yL = yL;
			this.yH = yH;
		}
	}
		
	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.println("usage: SingleFeatureWaveformPanel myfilename myfeaturename\n");
			System.exit(-1);
		}

		final String fileName = args[0];
		final String featureName = args[1];
		final FeatFile fF = new FeatFile(fileName);
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{
			public void run() 
			{
				JFrame frame = new JFrame("SingleFeatureWaveformPanel");
				
				try
				{
					fF.readFile();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					System.exit(-1);
				}
				SingleFeaturePanel sFP = new SingleFeatureWaveformPanel();
				if (sFP.initialize(fF, featureName) == -1)
				{
					System.out.println("hmm, something wrong, bailing.");
					System.exit(-1);
				}
				//sFP.setSize(600, 400);
				
				frame.setContentPane(sFP);
				frame.pack();
				frame.setVisible(true);
				frame.setBounds(100, 100, 600, 400);

				sFP.repaint();
			}
		});
	}
}
