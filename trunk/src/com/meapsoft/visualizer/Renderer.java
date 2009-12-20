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

/**
 * Created on Nov 15, 2006
 *
 * various ways of viewing EDL files
 * 
 */
package com.meapsoft.visualizer;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;
import com.meapsoft.disgraced.ColorMap;

/**
 * 
 * @author douglas@music.columbia.edu
 * 
 */
public abstract class Renderer implements ActionListener
{
	DrawingPanel drawingPanel;

	String name = "none";

	EDLFile eDLFile;

	FeatFile featFile;

	int numColors = 256;

	ColorMap colormap = ColorMap.getJet(numColors);

	Vector events;

	// by this we mean number of different features!
	int numFeatures = 0;

	Vector featureDescriptions;

	int[] elementsPerFeature;

	// one of these for each feature
	double lowestFeatureValue[];

	double highestFeatureValue[];

	double featureValueSpan[];

	double colorMultipliers[];

	double longestChunk = Double.MIN_VALUE;

	double shortestChunk = Double.MAX_VALUE;

	double lastStartTime = 0.0;

	double totalTime = 0.0;

	JPanel controlsPanel;

	JComboBox featureSelector;

	JTextField rangeInput;

	// JRadioButton linFeatureValues;
	// JRadioButton sqrtFeatureValues;
	// JRadioButton squareFeatureValues;
	// JComboBox cOptions;

	String[] optionBoxStrings;

	JPanel labelsPanel;

	JLabel featureNameLabel;

	JLabel featureValueLabel;

	JLabel startTimeLabel;

	JLabel endTimeLabel;

	JLabel lengthLabel;

	JLabel destTimeLabel;

	// final static int LINEAR = 0;
	// final static int SQRT = 1;
	// final static int SQUARE = 2;

	protected Rectangle dragRect = null;

	protected boolean dragShift = false;

	public Renderer(FeatFile featFile, EDLFile eDLFile, String name)
	{
		this.eDLFile = eDLFile;
		this.featFile = featFile;

		this.name = name;

		parseFiles();
	}

	public Renderer(Renderer r)
	{
		drawingPanel = r.drawingPanel;
		name = r.name;
		eDLFile = r.eDLFile;
		featFile = r.featFile;
		events = r.events;

		numFeatures = r.numFeatures;
		featureDescriptions = r.featureDescriptions;
		elementsPerFeature = r.elementsPerFeature;

		lowestFeatureValue = r.lowestFeatureValue;
		highestFeatureValue = r.highestFeatureValue;
		featureValueSpan = r.featureValueSpan;
		colorMultipliers = r.colorMultipliers;

		longestChunk = r.longestChunk;
		shortestChunk = r.shortestChunk;

		lastStartTime = r.lastStartTime;
		totalTime = r.totalTime;

		updateColorMultipliers();
	}

	public void setDrawingPanel(DrawingPanel dP)
	{
		drawingPanel = dP;
	}

	public void setFiles(FeatFile featFile, EDLFile eDLFile)
	{
		if (featFile != null)
			this.featFile = featFile;

		if (eDLFile != null)
			this.eDLFile = eDLFile;

		parseFiles();
	}

	public void parseFiles()
	{
		if (featFile == null)
			return;

		events = new Vector();

		int numChunks = featFile.chunks.size();

		featureDescriptions = featFile.featureDescriptions;
		numFeatures = featureDescriptions.size();

		elementsPerFeature = featFile.getFeatureLengths();

		highestFeatureValue = new double[numFeatures];
		lowestFeatureValue = new double[numFeatures];
		featureValueSpan = new double[numFeatures];
		colorMultipliers = new double[numFeatures];

		longestChunk = Double.MIN_VALUE;
		shortestChunk = Double.MAX_VALUE;
		lastStartTime = 0.0;

		for (int i = 0; i < numFeatures; i++)
		{
			highestFeatureValue[i] = Double.MIN_VALUE;
			lowestFeatureValue[i] = Double.MAX_VALUE;
			featureValueSpan[i] = 0.0;
			colorMultipliers[i] = 0.0;
		}

		for (int i = 0; i < numChunks; i++)
		{
			// first we fill in a cVI
			FeatChunk fC = (FeatChunk) ((FeatChunk) featFile.chunks
					.get(i)).clone();
			ChunkVisInfo cVI = new ChunkVisInfo(fC.srcFile, fC.startTime,
					fC.length, -1);
			cVI.addFeature(fC.getFeatures());
			events.add(cVI);

			if (eDLFile != null)
			{
				int numEDLChunks = eDLFile.chunks.size();

				for (int j = 0; j < numEDLChunks; j++)
				{
					EDLChunk eC = (EDLChunk) eDLFile.chunks.get(j);

					if (eC.startTime == cVI.startTime)
						cVI.dstTime = eC.dstTime;
				}
			}

			if (cVI.startTime > lastStartTime)
			{
				lastStartTime = cVI.startTime;
				totalTime = lastStartTime + cVI.length;
			}

			if (cVI.dstTime > lastStartTime)
			{
				lastStartTime = cVI.dstTime;
				totalTime = lastStartTime + cVI.length;
			}

			if (cVI.length < shortestChunk)
				shortestChunk = cVI.length;

			if (cVI.length > longestChunk)
				longestChunk = cVI.length;

			int currIndex = 0;

			double[] features = cVI.getFeatures();

			for (int k = 0; k < numFeatures; k++)
			{
				for (int m = 0; m < elementsPerFeature[k]; m++)
				{
					// System.out.println("k: " + k + " m: " + m + " currIndex:
					// " + currIndex);

					double value = features[currIndex];
					// System.out.println("value: " + value);
					if (value > highestFeatureValue[k])
						highestFeatureValue[k] = value;

					if (value < lowestFeatureValue[k])
						lowestFeatureValue[k] = value;

					currIndex++;
				}
				/*
				 * System.out.println("lowestFeatureValue[" + k + "]: " +
				 * lowestFeatureValue[k] + "highestFeatureValue[" + k + "]: " +
				 * highestFeatureValue[k]);
				 */
			}
		}

		// updateOptionBoxStrings();

		// first time through we haven't built our GUI yet!
		// if (cOptions != null)
		// updateOptionBoxes();

		updateColorMultipliers();
	}

	public void updateOptionBoxStrings()
	{
		int numItems = numFeatures + 3;
		optionBoxStrings = new String[numItems];
		optionBoxStrings[0] = "start time";
		optionBoxStrings[1] = "dest time";
		optionBoxStrings[2] = "length";

		for (int i = 0; i < numFeatures; i++)
		{
			String bigName = (String) featureDescriptions.get(i);
			String[] name = bigName.split("[//.]");
			optionBoxStrings[i + 3] = name[name.length - 1];
		}
	}

	public void updateColorMultipliers()
	{
		/*
		 * int whichOption;//cOptions.getSelectedIndex();
		 * 
		 * //first time through we don't have a gui yet... if (cOptions == null) {
		 * whichOption = 3; } else whichOption = cOptions.getSelectedIndex();
		 * 
		 * 
		 * 
		 * if (whichOption == 0) { for (int i = 0; i < numFeatures; i++) {
		 * colorMultipliers[i] = (numColors - 1.0)/lastStartTime; } } else if
		 * (whichOption == 1) { for (int i = 0; i < numFeatures; i++) {
		 * colorMultipliers[i] = (numColors - 1.0)/lastStartTime; } } else if
		 * (whichOption == 2) { for (int i = 0; i < numFeatures; i++) {
		 * colorMultipliers[i] = (numColors - 1.0)/longestChunk; } } else if
		 * (whichOption >= 3) { int wO = whichOption - 3;
		 * 
		 * for (int i = 0; i < numFeatures; i++) { double lowValue =
		 * lowestFeatureValue[wO] - lowestFeatureValue[wO]; double highValue =
		 * highestFeatureValue[wO] - lowestFeatureValue[wO];
		 * 
		 * featureValueSpan[i] = highValue - lowValue; colorMultipliers[i] =
		 * (numColors - 1.0)/featureValueSpan[i]; }
		 *  }
		 */

		// we'll bag this for now...too complicated trying to add in color
		// options!
		/*
		 * if (colorMapType == SQRT) { //System.out.println("sqrt!"); for (int i =
		 * 0; i < numFeatures; i++) { double lowValue =
		 * Math.sqrt(lowestFeatureValue[i] - lowestFeatureValue[i]); double
		 * highValue = Math.sqrt(highestFeatureValue[i] -
		 * lowestFeatureValue[i]);
		 * 
		 * featureValueSpan[i] = highValue - lowValue; colorMultipliers[i] =
		 * (numColors - 1.0)/featureValueSpan[i]; } } else if (colorMapType ==
		 * SQUARE) { for (int i = 0; i < numFeatures; i++) { double lowValue =
		 * lowestFeatureValue[i] - lowestFeatureValue[i]; double highValue =
		 * highestFeatureValue[i] - lowestFeatureValue[i];
		 * 
		 * lowValue *= lowValue; highValue *= highValue;
		 * 
		 * featureValueSpan[i] = highValue - lowValue; colorMultipliers[i] =
		 * (numColors - 1.0)/featureValueSpan[i]; } } else {
		 */
		for (int i = 0; i < numFeatures; i++)
		{
			double lowValue = lowestFeatureValue[i] - lowestFeatureValue[i];
			double highValue = highestFeatureValue[i] - lowestFeatureValue[i];

			featureValueSpan[i] = highValue - lowValue;
			colorMultipliers[i] = (numColors - 1.0) / featureValueSpan[i];
		}
		// }

	}

	public JPanel buildGUI(Color bgColor)
	{
		JPanel panel = new JPanel();
		panel.setBackground(bgColor);

		/*
		 * standard controls
		 */
		controlsPanel = new JPanel();
		controlsPanel.setBackground(bgColor);

		JPanel zoomPanel = new JPanel();
		zoomPanel.setBackground(bgColor);
		zoomPanel.setLayout(new BoxLayout(zoomPanel, BoxLayout.Y_AXIS));
		zoomPanel.setAlignmentX((float) 0.5);

		JLabel zL = new JLabel("zoom:");
		zL.setAlignmentX((float) 0.5);
		zL.setBackground(bgColor);
		zoomPanel.add(zL);

		JPanel zoomInOutPanel = new JPanel();
		zoomInOutPanel.setAlignmentX((float) 0.5);
		zoomInOutPanel.setBackground(bgColor);

		JButton zoomInButton = new JButton("+");
		zoomInButton.setBackground(bgColor);
		zoomInButton.setActionCommand("zoomIn");
		zoomInButton.addActionListener(this);
		zoomInOutPanel.add(zoomInButton);

		JButton zoomOutButton = new JButton("-");
		zoomOutButton.setBackground(bgColor);
		zoomOutButton.setActionCommand("zoomOut");
		zoomOutButton.addActionListener(this);
		zoomInOutPanel.add(zoomOutButton);

		zoomPanel.add(zoomInOutPanel);

		JButton resetZoomButton = new JButton("reset");
		resetZoomButton.setBackground(bgColor);
		resetZoomButton.setAlignmentX((float) 0.5);
		resetZoomButton.setActionCommand("resetZoom");
		resetZoomButton.addActionListener(this);
		zoomPanel.add(resetZoomButton);

		controlsPanel.add(zoomPanel);

		JPanel selectionPanel = new JPanel();
		selectionPanel.setBackground(bgColor);
		// selectionPanel.setLayout(new BoxLayout(selectionPanel,
		// BoxLayout.Y_AXIS));

		JPanel clickedSelectorPanel = new JPanel();
		clickedSelectorPanel.setBackground(bgColor);
		clickedSelectorPanel.setLayout(new BoxLayout(clickedSelectorPanel,
				BoxLayout.Y_AXIS));

		JLabel selectionLabel = new JLabel("selection control:");
		selectionLabel.setBackground(bgColor);
		clickedSelectorPanel.add(selectionLabel);

		JButton selectAllButton = new JButton("select all");
		selectAllButton.setBackground(bgColor);
		selectAllButton.setActionCommand("selectAll");
		selectAllButton.addActionListener(this);
		clickedSelectorPanel.add(selectAllButton);

		JButton selectNoneButton = new JButton("select none");
		selectNoneButton.setBackground(bgColor);
		selectNoneButton.setActionCommand("selectNone");
		selectNoneButton.addActionListener(this);
		clickedSelectorPanel.add(selectNoneButton);

		JButton toggleAllButton = new JButton("invert selection");
		toggleAllButton.setBackground(bgColor);
		toggleAllButton.setActionCommand("invertAll");
		toggleAllButton.addActionListener(this);
		clickedSelectorPanel.add(toggleAllButton);

		selectionPanel.add(clickedSelectorPanel);

		JPanel rangeSelectorPanel = new JPanel();
		rangeSelectorPanel.setBackground(bgColor);
		rangeSelectorPanel.setLayout(new BoxLayout(rangeSelectorPanel,
				BoxLayout.Y_AXIS));

		updateOptionBoxStrings();

		featureSelector = new JComboBox(optionBoxStrings);
		featureSelector.setAlignmentX(0.0f);
		featureSelector.setMaximumSize(featureSelector.getPreferredSize());
		featureSelector.setBackground(bgColor);
		featureSelector.setActionCommand("rangeFilterSelectionChanged");
		featureSelector.addActionListener(this);
		rangeSelectorPanel.add(featureSelector);

		rangeInput = new JTextField("0.00:1.00");
		rangeInput.setAlignmentX(0.0f);
		// rangeInput.setColumns(5);
		rangeInput.setBackground(bgColor);
		rangeSelectorPanel.add(rangeInput);

		JButton selectRangeButton = new JButton("apply selection filter");
		selectRangeButton.setAlignmentX(0.0f);
		selectRangeButton.setBackground(bgColor);
		selectRangeButton.setActionCommand("applyRangeFilter");
		selectRangeButton.addActionListener(this);
		rangeSelectorPanel.add(selectRangeButton);

		selectionPanel.add(rangeSelectorPanel);

		controlsPanel.add(selectionPanel);

		/*
		 * JPanel linSqrSqrdPanel = new JPanel();
		 * linSqrSqrdPanel.setBackground(bgColor); linSqrSqrdPanel.setLayout(new
		 * BoxLayout(linSqrSqrdPanel, BoxLayout.Y_AXIS));
		 * 
		 * ButtonGroup bg = new ButtonGroup();
		 * 
		 * JLabel linSqrSqrdLabel = new JLabel("color mapping:");
		 * linSqrSqrdLabel.setBackground(bgColor);
		 * linSqrSqrdPanel.add(linSqrSqrdLabel);
		 * 
		 * linFeatureValues = new JRadioButton("linear"); if (colorMapType ==
		 * LINEAR) linFeatureValues.setSelected(true);
		 * linFeatureValues.setBackground(bgColor);
		 * linFeatureValues.addActionListener(this);
		 * linSqrSqrdPanel.add(linFeatureValues);
		 * 
		 * sqrtFeatureValues = new JRadioButton("sqrt(feature)"); if
		 * (colorMapType == SQRT) sqrtFeatureValues.setSelected(true);
		 * sqrtFeatureValues.setBackground(bgColor);
		 * sqrtFeatureValues.addActionListener(this);
		 * linSqrSqrdPanel.add(sqrtFeatureValues);
		 * 
		 * squareFeatureValues = new JRadioButton("feature^2"); if (colorMapType ==
		 * SQUARE) squareFeatureValues.setSelected(true);
		 * squareFeatureValues.setBackground(bgColor);
		 * squareFeatureValues.addActionListener(this);
		 * linSqrSqrdPanel.add(squareFeatureValues);
		 * 
		 * bg.add(linFeatureValues); bg.add(sqrtFeatureValues);
		 * bg.add(squareFeatureValues);
		 * 
		 * controlsPanel.add(linSqrSqrdPanel);
		 */

		/*
		 * standard labels
		 */
		labelsPanel = new JPanel();
		BoxLayout bL = new BoxLayout(labelsPanel, BoxLayout.X_AXIS);
		labelsPanel.setLayout(bL);
		labelsPanel.setBackground(bgColor);

		/*
		 * JPanel cOptionsPanel = new JPanel();
		 * cOptionsPanel.setBackground(bgColor); cOptionsPanel.setLayout(new
		 * BoxLayout(cOptionsPanel, BoxLayout.Y_AXIS));
		 * 
		 * JLabel cCL = new JLabel("color is: "); cCL.setBackground(bgColor);
		 * cOptionsPanel.add(cCL);
		 * 
		 * updateOptionBoxStrings(); cOptions = new JComboBox(optionBoxStrings);
		 * //start with color = 1st feature cOptions.setSelectedIndex(3);
		 * cOptions.setBackground(bgColor);
		 * cOptions.setActionCommand("cOptions");
		 * cOptions.addActionListener(this); cOptionsPanel.add(cOptions);
		 * 
		 * controlsPanel.add(cOptionsPanel);
		 */

		panel.add(controlsPanel);

		return panel;
	}

	public abstract void draw(BufferedImage image, int width, int height);

	// returns a Vector of EDLChunks
	public Vector getSelectedEDLChunks()
	{
		Vector v = new Vector();
		for (int i = 0; i < events.size(); i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.get(i);
			if (cVI.selected)
			{
				EDLChunk c = new EDLChunk(cVI.srcFile, cVI.startTime,
						cVI.length, cVI.dstTime);
				c.comment = cVI.comment;
				v.add(c);
			}
		}

		// return v in increasing order of destTime.
        Collections.sort(v);

		return v;
	}

	// returns a Vector of FeatChunks
	public Vector getSelectedFeatChunks()
	{
		Vector v = new Vector();
		for (int i = 0; i < events.size(); i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.get(i);
			if (cVI.selected)
			{
				FeatChunk c = new FeatChunk(cVI.srcFile, cVI.startTime,
						cVI.length, null);
				c.addFeature(cVI.getFeatures());
				c.comment = cVI.comment;
				v.add(c);
			}
		}

		return v;
	}

	public abstract Vector getChunkVisInfosForPoint(Point p);

	public void toggleSelectedForPoint(Point p)
	{
		Vector chunks = getChunkVisInfosForPoint(p);

		for (int i = 0; i < chunks.size(); i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) chunks.get(i);

			if (cVI != null)
			{
				cVI.selected = !cVI.selected;
			}
		}
	}

	public abstract int getFeatureNumberForPoint(Point p);

	public abstract String getFeatureNameForPoint(Point p);

	public abstract double getFeatureValueForPoint(Point p);

	public abstract void rangeFilterSelectionChanged();

	public void updateDragRect(Rectangle r, boolean dS)
	{
		dragRect = r;
		dragShift = dS;
		drawingPanel.repaint();
	}

	public abstract void setDragRect(Rectangle r, boolean dS);

	public void selectAll()
	{
		int numEvents = events.size();

		for (int i = 0; i < numEvents; i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.get(i);
			cVI.selected = true;
		}

		ActionEvent a = new ActionEvent(this, 0, "numChunksSelectedChanged");
		drawingPanel.actionListener.actionPerformed(a);
	}

	public void selectNone()
	{
		int numEvents = events.size();

		for (int i = 0; i < numEvents; i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.get(i);
			cVI.selected = false;
		}
		ActionEvent a = new ActionEvent(this, 0, "numChunksSelectedChanged");
		drawingPanel.actionListener.actionPerformed(a);
	}

	public void invertAll()
	{
		int numEvents = events.size();

		for (int i = 0; i < numEvents; i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.get(i);
			cVI.selected = !cVI.selected;
		}
		ActionEvent a = new ActionEvent(this, 0, "numChunksSelectedChanged");
		drawingPanel.actionListener.actionPerformed(a);
	}

	public void applyFilterRange()
	{
		String[] numbers = rangeInput.getText().split(":");
		int whichFeature = featureSelector.getSelectedIndex();
		double low = 0.0;
		double high = 0.0;

		try
		{
			low = new Double(numbers[0]).doubleValue();
			high = new Double(numbers[1]).doubleValue();
		}
		catch (java.lang.NumberFormatException e)
		{
			System.out
					.println("Please use the form 0.00:1.00 to indicate a selection range.");
			return;
		}

		// System.out.println("range: " + low + " to " + high);

		// make sure we're in the right order, in case they enter
		// something like 0.00:-5.0
		if (low > high)
		{
			double tempLow = high;
			high = low;
			low = tempLow;
		}

		int numEvents = events.size();

		for (int i = 0; i < numEvents; i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.get(i);
			cVI.selected = false;

			if (whichFeature == 0)
			{
				if (cVI.startTime >= low && cVI.startTime <= high)
					cVI.selected = true;
			}
			else if (whichFeature == 1)
			{
				if (cVI.dstTime >= low && cVI.dstTime <= high)
					cVI.selected = true;
			}
			else if (whichFeature == 2)
			{
				if (cVI.length >= low && cVI.length <= high)
					cVI.selected = true;
			}
			else if (whichFeature >= 3)
			{
				int which = whichFeature - 3;
				int featNum[] = { 0 };

				for (int j = 0; j < which; j++)
					featNum[0] += elementsPerFeature[j];

				double features[] = cVI.getFeatures(featNum);

				if (features[0] >= low && features[0] <= high)
					cVI.selected = true;
			}

		}
	}

	public int numChunksSelected()
	{
		int numChunksSelected = 0;

		for (int i = 0; i < events.size(); i++)
		{
			ChunkVisInfo cVI = (ChunkVisInfo) events.get(i);
			if (cVI.selected)
				numChunksSelected++;
		}
		return numChunksSelected;
	}

	public void actionPerformed(ActionEvent arg0)
	{
		// Object source = arg0.getSource();

		String command = arg0.getActionCommand();
		// System.out.println(command);

		if (command.equals("zoomIn"))
			drawingPanel.zoomIn();
		else if (command.equals("zoomOut"))
			drawingPanel.zoomOut();
		else if (command.equals("resetZoom"))
			drawingPanel.resetZoom();
		else if (command.equals("selectAll"))
		{
			selectAll();
		}
		else if (command.equals("selectNone"))
		{
			selectNone();
		}
		else if (command.equals("invertAll"))
		{
			invertAll();
		}
		else if (command.equals("applyRangeFilter"))
		{
			applyFilterRange();
		}
		else if (command.equals("rangeFilterSelectionChanged"))
		{
			rangeFilterSelectionChanged();
		}
		/*
		 * else if (source == sqrtFeatureValues) { colorMapType = SQRT;
		 * updateColorMultiplier(); } else if (source == squareFeatureValues) {
		 * colorMapType = SQUARE; updateColorMultiplier(); } else if (source ==
		 * linFeatureValues) { colorMapType = LINEAR; updateColorMultiplier(); }
		 */
		drawingPanel.repaint();
	}
}
