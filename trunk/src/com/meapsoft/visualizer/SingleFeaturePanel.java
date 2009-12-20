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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.meapsoft.Chunk;
import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;
import com.meapsoft.MEAPUtil;
import com.meapsoft.disgraced.SegmenterPanel;

/**
 * displays a single feature meant to be stacked in various ways to create
 * multi-file visualizations
 * 
 * @author Douglas Repetto (douglas@music.columbia.edu) and the MEAP team
 */

public abstract class SingleFeaturePanel extends JPanel implements
		MouseListener, LineListener, ComponentListener
{
	FeatFile featFile = null;
	EDLFile edlFile = null;

	String shortFileName;

	String featureName;

	// @mike: the slash will return "\\" for windows, which is correct. however,
	// we really want "\\\\" because when we get a string like this:
	// "C:\\Meap\\blah.wav", and we try and split it using "\\", it will
	// interpret "\\" to mean "\" because of the escape character and split()
	// will
	// eventually fail. even though it will print the string as
	// "C:\Meap\blah.wav",
	// internally it the "\\" takes up 2 characters in the string and we need to
	// split it using "\\\\". fun little annoyance with java & windows!
	String slash = MEAPUtil.slash.equals("\\") ? "\\\\" : MEAPUtil.slash;

	// featureNumbers -1 -2 -3 etc are special cases for Length, Waveform,
	// Spectrum, etc.
	// featureNumber -1000 is an error condition
	int featureNumber = -1000;

	// how many data points are in one chunk of this feature
	int featureSize = -1;

	// can we draw multi-dimensional features?
	// set max features, or -1 for don't care
	int numDrawableFeatures = 1;

	boolean initialized = false;

	List events = new Vector();

	int numChunks;
	int numEDLChunks;
	
	double highestValue = 0.0d;
	double lowestValue = 0.0d;
	double featureRange = 0.0;
	double firstEventTime = 0.0;
	double lastEventTime = 0.0;
	double endTime = 0.0;
	double timeRange = 0.0;

	Color bgColor = Color.WHITE;
	Color fGColor = null;
	Color outlineColor = Color.black;
	Color segmentTicksColor = Color.gray;
	Color panelInfoColor = Color.gray;
	Color timelineColor = Color.yellow;
	Color waveformColor = Color.orange.darker();

	boolean showScale = false;

	// boolean showSegmentTicks = true;
	boolean showPanelInfo = false;

	boolean drawWaveform = true;

	boolean selected = false;

	public static final int NO_TIMELINE = 0;
	public static final int TENTHS_TIMELINE = 1;
	public static final int SECS_TIMELINE = 2;
	public static final int MINS_TIMELINE = 3;
	public static final int NO_SEG_TICKS = 0;
	public static final int SHORT_SEG_TICKS = 1;
	public static final int FULL_SEG_TICKS = 2;
	int segTickType = NO_SEG_TICKS;

	int timelineType = NO_TIMELINE;

	double zoomLevel = 1.0;

	int firstChunkToDraw = 0;

	// DecimalFormat oneTwoNumberFormat = new java.text.DecimalFormat();
	DecimalFormat oneTwoNumberFormat = new java.text.DecimalFormat();
	DecimalFormat anyZeroNumberFormat = new java.text.DecimalFormat();

	boolean drawingOn = true;

	// keep track of our
	protected BoundedRangeModel progress = new DefaultBoundedRangeModel();

	// protected BoundedRangeModel progress = null;

	// the time tick panel
	protected TimeTickPanel m_kTimeTickPanel = null;

	// stub method for consistency here...
	public abstract void updateData();

	public SingleFeaturePanel()
	{
		// create a timer tick panel here and add it to us
		// m_kTimeTickPanel = new TimeTickPanel(this);
		// add(m_kTimeTickPanel);

		setLayout(new BorderLayout());

		// do all the normal jazz
		addMouseListener(this);
		// addComponentListener(this);

		fGColor = new Color((int) (Math.random() * 127.0) + 100, (int) (Math
				.random() * 127.0) + 100, (int) (Math.random() * 127.0) + 100);

		oneTwoNumberFormat.setMaximumIntegerDigits(1);
		oneTwoNumberFormat.setMaximumFractionDigits(2);
		oneTwoNumberFormat.setMinimumFractionDigits(1);

		anyZeroNumberFormat.setMinimumIntegerDigits(1);
		anyZeroNumberFormat.setMaximumFractionDigits(0);
	}

	public int initialize(FeatFile featFile, String featureName)
	{
		return initialize(featFile, null, featureName);
	}

	public int initialize(FeatFile featFile, EDLFile edlFile, String featureName)
	{
		this.featFile = featFile;
		this.edlFile = edlFile;
		this.featureName = featureName;

		highestValue = Double.MIN_VALUE;
		lowestValue = Double.MAX_VALUE;
		featureRange = 0.0;
		firstEventTime = 0.0;
		lastEventTime = 0.0;
		endTime = 0.0;
		timeRange = 0.0;

		// check to see if our feature is in the input file
		featureNumber = featFile.getFeatureNumberForName(featureName);
			//verifyFeature(featureName);
		if (featureNumber == -1000)
		{
			System.out.println("hmm, I don't find that feature! Bye.");
			return -1;
		}
		else if (featureNumber < -1)
		{
			System.out
					.println("you weirdos should have your own initialize() method! Bailing!");
			return -1;
		}
		// System.out.println("using feature number: " + featureNumber);

		int elementsPerFeature[] = { 0 };

		if (featureNumber >= 0)
		{
			elementsPerFeature = featFile.getFeatureLengths();
			featureSize = elementsPerFeature[featureNumber];
		}
		else
			featureSize = 1;

		// System.out.println("featureSize: " + featureSize);

		// bail if we can't draw the feature
		if (featureSize > numDrawableFeatures && numDrawableFeatures != -1)
		{
			String message = "Sorry, " + getDisplayType()
					+ " display can only " + "display features having "
					+ numDrawableFeatures + " feature(s).";
			JOptionPane.showMessageDialog(null, message, "whoops",
					JOptionPane.ERROR_MESSAGE);

			// System.out.println("I can't draw that feature, too many
			// dimensions!");
			return -1;
		}
		
		if (edlFile == null)
		{
			events = featFile.chunks;
			
			// extract feature data, find highest/lowest feature values
			numChunks = events.size();
			
			for (int i = 0; i < numChunks; i++)
			{
				double feature = 0.0;
				
				FeatChunk fC = (FeatChunk) events.get(i);
	
				for (int j = 0; j < featureSize; j++)
				{
					if (featureNumber == -1)
						feature = fC.length;
					else
					{
						double[] featureArray = featFile.getFeatureByName(featureName, i);
						feature = featureArray[j];
					}
	
					if (feature < lowestValue)
						lowestValue = feature;
	
					if (feature > highestValue)
						highestValue = feature;
	
					if (fC.startTime < firstEventTime)
						firstEventTime = fC.startTime;
	
					if (fC.startTime > lastEventTime)
						lastEventTime = fC.startTime;
	
					if (fC.startTime + fC.length > endTime)
						endTime = fC.startTime + fC.length;
				}
			}
		}
		else
		{
			events = edlFile.chunks;
			
			// extract feature data, find highest/lowest feature values
			numChunks = events.size();
			
			for (int i = 0; i < numChunks; i++)
			{
				double feature = 0.0;
				
				EDLChunk edlC = (EDLChunk)events.get(i);
				FeatChunk fC = (FeatChunk)featFile.chunks.get(i);
				
				//System.out.println("i: " + i + " eC.sT: " + edlC.startTime + 
				//		" fC.sT: " + fC.startTime + 
				//		" eC.dT: " + edlC.dstTime);
	
				for (int j = 0; j < featureSize; j++)
				{
					if (featureNumber == -1)
						feature = fC.length;
					else
					{
						double[] featureArray = featFile.getFeatureByName(featureName, i);
						feature = featureArray[j];
					}
	
					if (feature < lowestValue)
						lowestValue = feature;
	
					if (feature > highestValue)
						highestValue = feature;
	
					if (edlC.dstTime < firstEventTime)
						firstEventTime = edlC.dstTime;
	
					if (edlC.dstTime > lastEventTime)
						lastEventTime = edlC.dstTime;
	
					if (edlC.dstTime + edlC.length > endTime)
						endTime = edlC.dstTime + edlC.length;
				}
			}
		}

		
		featureRange = highestValue - lowestValue;
		timeRange = endTime - firstEventTime;
		
		//System.out.println("tR: " + timeRange +
		//		" eT: " + endTime + 
		//		" fET: " + firstEventTime);

		String fileNameParts[];
		
		if (edlFile == null)
			fileNameParts = featFile.filename.split(slash);
		else
			fileNameParts = edlFile.filename.split(slash);
		
		shortFileName = fileNameParts[fileNameParts.length - 1];

		initialized = true;

		return 1;
	}

	public void setShowPanelInfo(boolean sST)
	{
		showPanelInfo = sST;
	}

	public boolean getShowPanelInfo()
	{
		return showPanelInfo;
	}

	public void setShowTimeline(int tT)
	{
		timelineType = tT;
	}

	public int getShowTimeline()
	{
		return timelineType;
	}

	public void setSegTickType(int sTT)
	{
		segTickType = sTT;
	}

	public int getSegTickType()
	{
		return timelineType;
	}

	public void setShowScale(boolean show)
	{
		showScale = show;
	}

	public boolean getShowScale()
	{
		return showScale;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
		// System.out.println(featureName + ": selected = " + selected);
	}

	public void toggleSelected()
	{
		selected = !selected;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public int setFeatureName(String fN)
	{
		String oldFeatureName = featureName;
		int oldFeatureNumber = featureNumber;

		featureName = fN;

		int error = initialize(featFile, edlFile, featureName);

		if (error == -1)
		{
			featureName = oldFeatureName;
			featureNumber = oldFeatureNumber;
			return error;
		}
		else
			return 0;
	}

	protected void paintComponent(Graphics g)
	{
		if (featFile != null)
		{
			if (featureSize <= numDrawableFeatures || numDrawableFeatures == -1)
				drawData(g);
			else
				errorDrawer(g,
						"I can't draw that feature, too many dimensions!");
		}
		else
		{
			drawNoData(g);
			return;
		}

		if (segTickType != NO_SEG_TICKS)
			drawSegmentTicks(g);

		if (timelineType != NO_TIMELINE)
			drawTimeline(g);

		if (showPanelInfo)
			drawPanelInfo(g);

		if (showScale)
			drawScale(g);

		if (selected)
			drawBorder(g);
	}

	public abstract void drawData(Graphics g);

	public void drawNoData(Graphics g)
	{
		int yOffset = (int) (this.getHeight() * 0.2);

		g.setColor(panelInfoColor);

		g.drawString("waiting for data...", 20, yOffset);

	}

	// weird drawing sFPs may want to override this to draw file info in a
	// different way/place
	void drawPanelInfo(Graphics g)
	{
		int yOffset = (int) (this.getHeight() * 0.2);

		g.setColor(panelInfoColor);

		g.drawString(shortFileName + " - " + featureName + " - zoom:"
				+ zoomLevel, 20, yOffset);
	}

	// weird drawing sFPs may want to override this to draw scale in a different
	// way/place
	void drawScale(Graphics g)
	{
		g.setColor(Color.BLACK);
		g.drawString("max: " + highestValue, 5, 15);
		// g.drawString("" + (highestValue + lowestValue)/2.0, 5,
		// (this.getHeight() - 10)/2);
		g.drawString("min: " + lowestValue, 5, this.getHeight() - 10);
	}

	// weird drawing sFPs may want to override this to draw timeline in a
	// different way/place
	void drawTimeline(Graphics g)
	{
		double tickTimeIncr = 1.0;
		double localFirstEventTime = ((Chunk) events
				.get(firstChunkToDraw)).startTime;

		double firstTickTime = localFirstEventTime;// (int)Math.ceil(localFirstEventTime);

		switch (timelineType)
		{
		case TENTHS_TIMELINE:
			tickTimeIncr = 0.1;
			// need to shift around so that we only draw ticks on exactly
			// .1s increments
			firstTickTime *= 10;
			firstTickTime = (int) Math.ceil(firstTickTime);
			firstTickTime *= 0.1;
			break;
		case SECS_TIMELINE:
			tickTimeIncr = 1.0;
			firstTickTime = (int) Math.ceil(localFirstEventTime);
			break;
		case MINS_TIMELINE:
			tickTimeIncr = 60.0;
			if (localFirstEventTime % 60 != 0)
			{
				int offset = 60 - (int) (localFirstEventTime % 60);
				firstTickTime += offset;
				firstTickTime = (int) Math.floor(firstTickTime);
			}
			break;
		}

		int w = (int) (getWidth() * zoomLevel);
		int h = this.getHeight();
		// height of ticks
		int ySize = (int) (h * 0.1);

		double xScaler = w / timeRange;

		g.setColor(timelineColor);

		// System.out.println("localFirstEventTime: " + localFirstEventTime + "
		// firstTickTime: " + firstTickTime);

		double tickTime = firstTickTime;

		int x = 0;

		while (tickTime <= lastEventTime && x < getWidth())
		{
			String timeLabel = "" + tickTime;

			switch (timelineType)
			{
			case TENTHS_TIMELINE:
				timeLabel = oneTwoNumberFormat.format(tickTime);
				break;
			case SECS_TIMELINE:
			case MINS_TIMELINE:
				timeLabel = anyZeroNumberFormat.format(tickTime);
				break;
			}

			double drawTime = tickTime - localFirstEventTime;
			x = (int) (drawTime * xScaler);
			g.drawLine(x, h, x, h - ySize);
			g.drawString(timeLabel, x, h - ySize);
			tickTime += tickTimeIncr;
		}
		// System.out.println("bailed at: " + x + " tickTime: " + tickTime);
	}

	// weird drawing sFPs may want to override this to draw segment ticks in a
	// different way/place
	void drawSegmentTicks(Graphics g)
	{
		int w = (int) (this.getWidth() * zoomLevel) - 1;
		int h = this.getHeight();
		// size of ticks
		int ySize = (int) (h * 0.05);

		double xScaler = w / timeRange;
		//xScaler *= 0.5;

		g.setColor(segmentTicksColor);

		//double localFirstEventTime = ((Chunk) events.get(firstChunkToDraw)).startTime;
		double localFirstEventTime = 0.0;
		
		if (edlFile == null)
			localFirstEventTime = ((FeatChunk)events.get(firstChunkToDraw)).startTime;
		else
			localFirstEventTime = ((EDLChunk)events.get(firstChunkToDraw)).dstTime;
		
		Iterator it = events.iterator();

		for (int i = 0; i < firstChunkToDraw; i++)
			it.next();

		int xStart = 0;
		int xEnd = 0;

		//System.out.println("dST seyz: w: " + w + " h: " + h + " xScaler: " + xScaler + " localFET: " + localFirstEventTime + 
			//	" firstChunkToDraw: " + firstChunkToDraw + " timeRange: " + timeRange);
		
		while (it.hasNext() && xEnd <= getWidth())
		{
			Chunk chunk = (Chunk) it.next();
			double startTime = 0.0;
			
			if (edlFile == null)
				startTime = chunk.startTime - localFirstEventTime;
			else
				startTime = ((EDLChunk)chunk).dstTime;
			
			double endTime = startTime + chunk.length;

			xStart = (int) (startTime * xScaler);
			xEnd = (int) (endTime * xScaler);

			//System.out.println("sT: " + startTime + " eT: " + endTime + " xS: " + xStart + " xE: " + xEnd);
			if (segTickType == SHORT_SEG_TICKS)
			{
				g.setColor(Color.orange);
				g.drawLine(xStart, 0, xStart, ySize);
				g.drawLine(xStart, h, xStart, h - ySize);
				g.setColor(Color.blue);
				g.drawLine(xEnd, 0, xEnd, ySize + 5);
				g.drawLine(xEnd, h, xEnd, h - (ySize + 5));
			}
			else
			{
				g.setColor(Color.blue);
				g.drawLine(xStart, 0, xStart, h);
				g.setColor(Color.orange);
				g.drawLine(xEnd, 0, xEnd, h);
			}
		}
	}

	void drawBorder(Graphics g)
	{
		g.setColor(outlineColor);

		Rectangle r = getLocalBounds();
		g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
		g.drawRect(r.x + 1, r.y + 1, r.width - 3, r.height - 3);
	}

	void errorDrawer(Graphics g, String error)
	{
		int w = this.getWidth();
		int h = this.getHeight();

		g.setColor(bgColor);
		g.fillRect(0, 0, w, h);

		g.setColor(fGColor);
		g.drawString(error, 10, 20);
	}

	Rectangle getLocalBounds()
	{
		Rectangle bounds = getBounds();

		return new Rectangle(0, 0, bounds.width, bounds.height);
	}

	public abstract String getDisplayType();

	void setBackgroundColor(Color c)
	{
		bgColor = c;
		repaint();
	}

	void setForegroundColor(Color c)
	{
		fGColor = c;
	}

	void zoomIn()
	{
		zoomLevel++;// += zoomLevel * .25;
	}

	void zoomOut()
	{
		zoomLevel--;// -= zoomLevel * .25;
		if (zoomLevel < 1.0)
			zoomLevel = 1.0;
	}

	void resetZoom()
	{
		zoomLevel = 1.0;
		firstChunkToDraw = 0;
	}

	void setZoomLevel(double zL)
	{
		if (zL >= 1.0)
			zoomLevel = zL;
	}

	double getZoomLevel()
	{
		return zoomLevel;
	}

	void incrFirstChunkToDraw()
	{
		firstChunkToDraw++;

		if (firstChunkToDraw > events.size() - 1)
			firstChunkToDraw = events.size() - 1;
	}

	void decrFirstChunkToDraw()
	{
		firstChunkToDraw--;

		if (firstChunkToDraw < 0)
			firstChunkToDraw = 0;
	}

	void setFirstChunkToDraw(int fCTD)
	{
		firstChunkToDraw = fCTD;

		if (firstChunkToDraw > events.size() - 1)
			firstChunkToDraw = events.size() - 1;

		if (firstChunkToDraw < 0)
			firstChunkToDraw = 0;
	}

	int getFirstChunkToDraw()
	{
		return firstChunkToDraw;
	}

	//return the feature chunk at the given location in the vector
	//we need this indirect method so that we can swap in the
	//appropriate FeatChunk if our timeline is determined by an
	//EDLFile.	
	FeatChunk getFeatChunkByNumber(int chunkNum)
	{
		FeatChunk fC = null;
		
		if (edlFile == null)
			return (FeatChunk)events.get(chunkNum);
		else
		{
			EDLChunk eC = (EDLChunk)events.get(chunkNum);
			
			for (int featChunkNum = 0; featChunkNum < numChunks; featChunkNum++)
			{
				fC = (FeatChunk) featFile.chunks.get(featChunkNum);
				
				if (fC.compareTo(eC) == 0)
					return fC;
			}
		}
		
		System.out.println("can't find matching feat chunk!");
		return fC; 
	}
	
	public BoundedRangeModel getProgress()
	{
		return progress;
	}

	// public void setProgress(BoundedRangeModel p)
	// {
	// this.progress = p;
	// }

	public void setProgress(DefaultBoundedRangeModel dBRM)
	{
		progress = dBRM;
	}

	public void mouseClicked(MouseEvent arg0)
	{
		// System.out.println(featureName + ": thanks for clicking in me!");
	}

	public void mouseEntered(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void componentHidden(ComponentEvent e)
	{
		// System.out.println(e.getComponent().getClass().getName() + " ---
		// Hidden");
	}

	public void componentMoved(ComponentEvent e)
	{
		// System.out.println(e.getComponent().getClass().getName() + " ---
		// Moved");
	}

	public void componentResized(ComponentEvent e)
	{
		// System.out.println(e.getComponent().getClass().getName() + " ---
		// Resized ");
		// updateWaveformPoints();
	}

	public void componentShown(ComponentEvent e)
	{
		// System.out.println(e.getComponent().getClass().getName() + " ---
		// Shown");

	}

	// ///////////////////////////////////////////////////////////////////////////////////////////
	// //update() - implemented by the line listener (so it will synch!)
	// ////////////////////////////////////////////////////////////////////////////
	public void update(LineEvent event)
	{
		//System.out.println("Line event fired: " + event.getType());

		if (event.getType().equals(LineEvent.Type.START))
		{
			// create a timer tick panel here and add it to us
			m_kTimeTickPanel = new TimeTickPanel(this);
			m_kTimeTickPanel.setMaximumSize(new Dimension(getWidth(),
					getHeight()));
			m_kTimeTickPanel.setPreferredSize(new Dimension(getWidth(),
					getHeight()));

			// add the time tick panel, then validate and repaint us
			add(m_kTimeTickPanel);
			validate();
			repaint();

			// call play on the timer tick
			m_kTimeTickPanel.play();
		}
		else if (event.getType().equals(LineEvent.Type.STOP))
		{
			// stop the timer tick panel
			m_kTimeTickPanel.stop();

			// remove, validate and repaint
			remove(m_kTimeTickPanel);
			validate();
			repaint();
		}
	}

	public double getTimeRange()
	{
		return timeRange;
	}
}
