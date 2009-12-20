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

/*
 * Created on Nov 17, 2006
 *
 */
package com.meapsoft.visualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.meapsoft.Chunk;
import com.meapsoft.EDLChunk;
import com.meapsoft.EDLFile;
import com.meapsoft.FeatChunk;
import com.meapsoft.FeatFile;
import com.meapsoft.MEAPUtil;
import com.meapsoft.Synthesizer;
import com.meapsoft.disgraced.GUIUtils;
import com.meapsoft.disgraced.HelpWindow;

/**
 * @author douglas
 * 
 */
public class Visualizer implements ActionListener, MouseListener,
		MouseMotionListener, ComponentListener
{
	String meapsoftDirectory;

	String dataDirectory;

	String slash;;

	EDLFile eDLInputFile;

	FeatFile featInputFile;

	EDLFile eDLOutputFile;

	FeatFile featOutputFile;

	DrawingPanel drawingPanel;

	private JScrollPane scroller;

	JPanel metaControlPanel;

	JLabel featInputFileLabel;

	JLabel eDLInputFileLabel;

	JComboBox rendererSelector;

	JLabel sourceLabel;

	JLabel featureNameLabel;

	JLabel featureRangeLabel;

	JLabel featureValueLabel;

	JLabel startTimeLabel;

	JLabel endTimeLabel;

	JLabel lengthLabel;

	JLabel destTimeLabel;

	JLabel chunksSelectedLabel;

	DecimalFormat fiveFiveNumberFormat = new java.text.DecimalFormat();

	DecimalFormat fiveTwoNumberFormat = new java.text.DecimalFormat();

	JPanel localControlPanel;

	// JLabel featureNameLabel;

	JPanel guiPanel;

	JFrame mainWindow;

	Color metaControlsColor;

	Color localControlsColor;

	Color drawingBackgroundColor;

	private JRadioButton playFeatChunksButton;

	private JRadioButton playEDLChunksButton;

	private JCheckBox playAddBlipsButton;

	private Point dragStart;

	private Point dragEnd;

	private boolean dragShift = false;

	private Thread playThread = null;

	private String helpURL;

	public Visualizer(FeatFile featFile, EDLFile eDLFile)
	{
		String paths[] = MEAPUtil.getPaths();
		if (paths != null)
		{
			meapsoftDirectory = paths[0];
			dataDirectory = paths[1];
		}
		else
			System.exit(-1);

		slash = MEAPUtil.slash;

		this.eDLInputFile = eDLFile;
		this.featInputFile = featFile;

		if (this.featInputFile == null)
			selectFeatInputFile();
		else if (this.featInputFile.chunks.size() == 0)
			selectFeatInputFile();

		fiveFiveNumberFormat.setMaximumIntegerDigits(5);
		fiveFiveNumberFormat.setMaximumFractionDigits(5);

		fiveTwoNumberFormat.setMaximumIntegerDigits(5);
		fiveTwoNumberFormat.setMaximumFractionDigits(2);

		setupGUI();
	}

	private void setupGUI()
	{
		mainWindow = new JFrame("MEAPsoft Visualizer");
		mainWindow.setSize(1024, 768);

		guiPanel = new JPanel();
		guiPanel.setLayout(new BorderLayout());
		guiPanel.setSize(1024, 768);
		mainWindow.setContentPane(guiPanel);

		metaControlsColor = new Color((int) (Math.random() * 127 + 127),
				(int) (Math.random() * 127 + 127),
				(int) (Math.random() * 127 + 127));

		localControlsColor = new Color((int) (Math.random() * 127 + 127),
				(int) (Math.random() * 127 + 127),
				(int) (Math.random() * 127 + 127));

		setupMetaControlsGUI();
		setupDrawingPanel();
		setupLocalControlsGUI();

		mainWindow.setVisible(true);
	}

	private void setupMetaControlsGUI()
	{
		metaControlPanel = new JPanel();
		metaControlPanel.setLayout(new BoxLayout(metaControlPanel,
				BoxLayout.Y_AXIS));
		metaControlPanel.setBackground(metaControlsColor);

		/*
		 * select type of visualization
		 */

		JPanel visTypesPanel = new JPanel();
		visTypesPanel.setLayout(new BoxLayout(visTypesPanel, BoxLayout.Y_AXIS));
		visTypesPanel.setAlignmentX(0.0f);
		visTypesPanel.setBackground(metaControlsColor);
		TitledBorder t1 = BorderFactory
				.createTitledBorder(BorderFactory
						.createEtchedBorder(EtchedBorder.LOWERED),
						"visualization type");
		t1.setTitleJustification(TitledBorder.CENTER);
		visTypesPanel.setBorder(t1);

		String[] rendererStrings = { "segment order", "scatterplot",
				"bar graph", "line graph" };
		// , "multi single features"};

		rendererSelector = new JComboBox(rendererStrings);
		rendererSelector.setMaximumSize(rendererSelector.getPreferredSize());
		rendererSelector.setBackground(metaControlsColor);
		rendererSelector.setActionCommand("rendererSelector");
		rendererSelector.addActionListener(this);

		visTypesPanel.add(rendererSelector);

		metaControlPanel.add(visTypesPanel);

		/*
		 * load files
		 */

		JPanel fileIOPanel = new JPanel();
		fileIOPanel.setLayout(new BoxLayout(fileIOPanel, BoxLayout.Y_AXIS));
		fileIOPanel.setAlignmentX(0.0f);
		fileIOPanel.setBackground(metaControlsColor);
		TitledBorder t2 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED), "file I/O");
		t2.setTitleJustification(TitledBorder.CENTER);
		fileIOPanel.setBorder(t2);

		featInputFileLabel = new JLabel("no file loaded");
		featInputFileLabel.setBackground(metaControlsColor);
		fileIOPanel.add(featInputFileLabel);

		eDLInputFileLabel = new JLabel("no file loaded");
		eDLInputFileLabel.setBackground(metaControlsColor);
		fileIOPanel.add(eDLInputFileLabel);

		JButton selectFeatsButton = new JButton("load files");
		selectFeatsButton.setBackground(metaControlsColor);
		selectFeatsButton.setActionCommand("selectFiles");
		selectFeatsButton.addActionListener(this);
		fileIOPanel.add(selectFeatsButton);

		JLabel featOutputFileLabel = new JLabel("save selected chunks");
		featOutputFileLabel.setBackground(metaControlsColor);
		fileIOPanel.add(featOutputFileLabel);

		JButton selectFeatOutputButton = new JButton("save .feat");
		selectFeatOutputButton.setBackground(metaControlsColor);
		selectFeatOutputButton.setActionCommand("saveFeatFile");
		selectFeatOutputButton.addActionListener(this);
		fileIOPanel.add(selectFeatOutputButton);

		JButton selectEDLOutputButton = new JButton("save .edl");
		selectEDLOutputButton.setBackground(metaControlsColor);
		selectEDLOutputButton.setActionCommand("saveEDLFile");
		selectEDLOutputButton.addActionListener(this);
		fileIOPanel.add(selectEDLOutputButton);

		metaControlPanel.add(fileIOPanel);

		/*
		 * audio playback
		 */
		JPanel audioPanel = new JPanel();
		audioPanel.setLayout(new BoxLayout(audioPanel, BoxLayout.Y_AXIS));
		audioPanel.setAlignmentX(0.0f);
		audioPanel.setBackground(metaControlsColor);
		TitledBorder t3 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED), "synthesizer");
		t3.setTitleJustification(TitledBorder.CENTER);
		audioPanel.setBorder(t3);

		playFeatChunksButton = new JRadioButton("play feat chunks");
		playFeatChunksButton.setBackground(metaControlsColor);
		playFeatChunksButton.setSelected(true);
		playFeatChunksButton.setEnabled(false);
		audioPanel.add(playFeatChunksButton);
		playEDLChunksButton = new JRadioButton("play edl chunks");
		playEDLChunksButton.setBackground(metaControlsColor);
		playEDLChunksButton.setEnabled(false);
		audioPanel.add(playEDLChunksButton);
		playAddBlipsButton = new JCheckBox("add blips");
		playAddBlipsButton.setBackground(metaControlsColor);
		playAddBlipsButton.setEnabled(true);
		audioPanel.add(playAddBlipsButton);

		ButtonGroup playTypeBG = new ButtonGroup();
		playTypeBG.add(playFeatChunksButton);
		playTypeBG.add(playEDLChunksButton);

		JButton playButton = new JButton("play selected");
		playButton.setBackground(metaControlsColor);
		playButton.setActionCommand("playChunks");
		playButton.addActionListener(this);
		audioPanel.add(playButton);
		JButton stopButton = new JButton("stop");
		stopButton.setBackground(metaControlsColor);
		stopButton.setActionCommand("stopPlayback");
		stopButton.addActionListener(this);
		audioPanel.add(stopButton);

		metaControlPanel.add(audioPanel);

		updateFileNameLabels();

		/*
		 * standard info display
		 */

		JPanel infoDisplayPanel = new JPanel();
		infoDisplayPanel.setLayout(new BoxLayout(infoDisplayPanel,
				BoxLayout.Y_AXIS));
		infoDisplayPanel.setBackground(metaControlsColor);
		infoDisplayPanel.setBounds(visTypesPanel.getBounds());

		TitledBorder t4 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED), "chunk data");
		t4.setTitleJustification(TitledBorder.CENTER);
		infoDisplayPanel.setBorder(t4);

		JLabel iL1 = new JLabel("file name:");
		infoDisplayPanel.add(iL1);

		sourceLabel = new JLabel("unknown");
		Font font = sourceLabel.getFont();
		sourceLabel.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
		infoDisplayPanel.add(sourceLabel);

		JLabel iL2 = new JLabel("feature name:");
		infoDisplayPanel.add(iL2);

		featureNameLabel = new JLabel("unknown feature...");
		featureNameLabel.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
		infoDisplayPanel.add(featureNameLabel);

		JLabel iL3 = new JLabel("feature range:");
		infoDisplayPanel.add(iL3);

		featureRangeLabel = new JLabel("unknown range");
		featureRangeLabel.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
		infoDisplayPanel.add(featureRangeLabel);

		JLabel iL4 = new JLabel("feature value:");
		infoDisplayPanel.add(iL4);

		featureValueLabel = new JLabel("unknown value");
		featureValueLabel.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
		infoDisplayPanel.add(featureValueLabel);

		JLabel iL5 = new JLabel("start time:");
		infoDisplayPanel.add(iL5);

		startTimeLabel = new JLabel("???");
		startTimeLabel.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
		infoDisplayPanel.add(startTimeLabel);

		JLabel iL6 = new JLabel("length:");
		infoDisplayPanel.add(iL6);

		lengthLabel = new JLabel("???");
		lengthLabel.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
		infoDisplayPanel.add(lengthLabel);

		JLabel iL7 = new JLabel("dest time:");
		infoDisplayPanel.add(iL7);

		destTimeLabel = new JLabel("???");
		destTimeLabel.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
		infoDisplayPanel.add(destTimeLabel);

		chunksSelectedLabel = new JLabel("0 selected");
		chunksSelectedLabel.setFont(font
				.deriveFont(font.getStyle() | Font.BOLD));
		infoDisplayPanel.add(chunksSelectedLabel);

		// ARRRRG! This is such a kludge.
		// If I don't do this then the freaking infoDisplayPanel jumps
		// all over tarnation every time a value changes...!!!
		JPanel dummyPanel = new JPanel();
		dummyPanel.setBackground(metaControlsColor);
		dummyPanel.setBounds(0, 0, visTypesPanel.getWidth(), 0);
		infoDisplayPanel.add(dummyPanel);

		metaControlPanel.add(infoDisplayPanel);

		JButton helpButton = new JButton("help");
		helpButton.setForeground(Color.blue);
		helpButton.setBackground(metaControlsColor);
		// helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		helpButton.setActionCommand("help");
		helpButton.addActionListener(this);
		helpURL = "file:///" + meapsoftDirectory + slash + "doc" + slash
				+ "manual.html#Visualizer";
		metaControlPanel.add(helpButton);

		guiPanel.add(metaControlPanel, BorderLayout.LINE_START);
	}

	private void setupDrawingPanel()
	{
		// we default to point-to-point
		SegmentOrderRenderer pTPV = new SegmentOrderRenderer(featInputFile,
				eDLInputFile);
		drawingPanel = new DrawingPanel(pTPV, this);
		pTPV.setDrawingPanel(drawingPanel);
		drawingPanel.setSize(800, 600);
		drawingPanel.addMouseListener(this);
		drawingPanel.addMouseMotionListener(this);

		drawingPanel.addComponentListener(this);

		scroller = new JScrollPane(drawingPanel);
		drawingPanel.setScroller(scroller);
		guiPanel.add(scroller, BorderLayout.CENTER);
	}

	private void setupLocalControlsGUI()
	{
		localControlPanel = drawingPanel.renderer.buildGUI(localControlsColor);
		guiPanel.add(localControlPanel, BorderLayout.PAGE_END);
	}

	public void selectEDLInputFile()
	{
		String names[] = GUIUtils.FileSelector(GUIUtils.OPENEDL, dataDirectory,
				mainWindow);

		if (names[0] == null)
			return;

		// System.out.println("you selected EDLFile: " + names[0]);
		eDLInputFile = new EDLFile(names[0]);
		try
		{
			eDLInputFile.readFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void selectFeatInputFile()
	{
		String names[] = GUIUtils.FileSelector(GUIUtils.OPENFEAT,
				dataDirectory, mainWindow);

		if (names[0] == null)
			return;

		featInputFile = new FeatFile(names[0]);
		try
		{
			featInputFile.readFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void saveEDLOutputFile()
	{
		String names[] = GUIUtils.FileSelector(GUIUtils.SAVEEDL, dataDirectory,
				mainWindow);

		if (names[0] == null)
			return;

		eDLOutputFile = new EDLFile(names[0]);

		Vector selectedChunks = drawingPanel.renderer.getSelectedEDLChunks();

		double currentTime = 0.0;

		for (int i = 0; i < selectedChunks.size(); i++)
		{
			EDLChunk oldChunk = (EDLChunk) selectedChunks.elementAt(i);

			EDLChunk newChunk = new EDLChunk(oldChunk.srcFile,
					oldChunk.startTime, oldChunk.length, currentTime);
			newChunk.addFeature(oldChunk.getFeatures());
			eDLOutputFile.chunks.add(newChunk);

			currentTime += oldChunk.length;
		}

		try
		{
			eDLOutputFile.writeFile();
		}
		catch (IOException e)
		{
			System.out.println("can't write .edl file!");
			e.printStackTrace();
		}
	}

	public void saveFeatOutputFile()
	{
		String names[] = GUIUtils.FileSelector(GUIUtils.SAVEFEAT,
				dataDirectory, mainWindow);

		if (names[0] == null)
			return;

		featOutputFile = new FeatFile(names[0]);

		Vector selectedChunks = drawingPanel.renderer.getSelectedFeatChunks();

		double currentTime = 0.0;

		for (int i = 0; i < selectedChunks.size(); i++)
		{
			// featOutputFile.chunks.add(selectedChunks.elementAt(i));
			FeatChunk oldChunk = (FeatChunk) selectedChunks.elementAt(i);

			FeatChunk newChunk = new FeatChunk(oldChunk.srcFile, currentTime,
					oldChunk.length, oldChunk.featureDescriptions);
			newChunk.addFeature(oldChunk.getFeatures());
			featOutputFile.chunks.add(newChunk);
			currentTime += oldChunk.length;
		}

		int numFeatDescs = featInputFile.featureDescriptions.size();

		for (int i = 0; i < numFeatDescs; i++)
		{
			String desc = (String) (featInputFile.featureDescriptions
					.elementAt(i))
					+ " ";
			featOutputFile.featureDescriptions.add(desc);
		}

		try
		{
			featOutputFile.writeFile();
		}
		catch (IOException e)
		{
			System.out.println("can't write .feat file!");
			e.printStackTrace();
		}
	}

	public void updateFileNameLabels()
	{
		if (featInputFile != null)
		{
			String fileNameFull = featInputFile.filename;
			// this doesn't work in windows since the slash character
			// is the same as the Java regular expression escape
			// character
			// String[] stringChunks = fileNameFull.split(slash);

			String shortName = new File(fileNameFull).getName();
			featInputFileLabel.setText(shortName);
			playFeatChunksButton.setEnabled(true);
		}
		else
		{
			featInputFileLabel.setText("no file loaded");
			playFeatChunksButton.setEnabled(false);
		}

		if (eDLInputFile != null)
		{
			String fileNameFull = eDLInputFile.filename;
			// this doesn't work in windows since the slash character
			// is the same as the Java regular expression escape
			// character
			// String[] stringChunks = fileNameFull.split(slash);

			String shortName = new File(fileNameFull).getName();

			eDLInputFileLabel.setText(shortName);
			playEDLChunksButton.setEnabled(true);
		}
		else
		{
			eDLInputFileLabel.setText("no file loaded");
			playEDLChunksButton.setEnabled(false);
		}
		/*
		 * if (eDLOutputFile != null) { String fileNameFull =
		 * eDLOutputFile.filename; String[] stringChunks =
		 * fileNameFull.split(slash);
		 * 
		 * int numStrings = stringChunks.length;
		 * 
		 * String shortName = stringChunks[numStrings - 1];
		 * 
		 * eDLOutputFileLabel.setText(shortName); } else {
		 * eDLOutputFileLabel.setText("no file specified"); }
		 */
	}

	public void updateNumChunksSelectedLabel(int nCS)
	{
		chunksSelectedLabel.setText(nCS + " selected");
	}

	public void updateDataLabels(String sN, String fN, double low, double high,
			double fV, double sT, double l, double dT)
	{
		sourceLabel.setText(sN);
		if (fN.length() > 16)
			fN = fN.substring(0, 15);
		featureNameLabel.setText(fN);
		featureRangeLabel.setText(fiveTwoNumberFormat.format(low) + " : "
				+ fiveTwoNumberFormat.format(high));
		featureValueLabel.setText(fiveFiveNumberFormat.format(fV));
		startTimeLabel.setText(fiveFiveNumberFormat.format(sT));
		lengthLabel.setText(fiveFiveNumberFormat.format(l));
		destTimeLabel.setText(fiveFiveNumberFormat.format(dT));
	}

	public void updateInfoLabelsForPoint(Point p)
	{
		Vector chunks = drawingPanel.renderer.getChunkVisInfosForPoint(p);
		if (chunks.size() == 0)
			return;

		// just use first one
		ChunkVisInfo cVI = (ChunkVisInfo) chunks.elementAt(0);

		String sF = new File(cVI.srcFile).getName();

		String fN = drawingPanel.renderer.getFeatureNameForPoint(p);
		double fV = drawingPanel.renderer.getFeatureValueForPoint(p);
		int featNum = drawingPanel.renderer.getFeatureNumberForPoint(p);
		double low = 0.0;
		double high = 0.0;

		// kludge alert!!!
		if (fN.equals("start time") || fN.equals("dest time"))
		{
			low = 0.0;
			high = drawingPanel.renderer.lastStartTime;
		}
		else if (fN.equals("length"))
		{
			low = drawingPanel.renderer.shortestChunk;
			high = drawingPanel.renderer.longestChunk;
		}
		else if (featNum == -1)
		{
			low = 0.0;
			high = 0.0;
		}
		else
		{
			low = drawingPanel.renderer.lowestFeatureValue[featNum];
			high = drawingPanel.renderer.highestFeatureValue[featNum];
		}

		updateDataLabels(sF, fN, low, high, fV, cVI.startTime, cVI.length,
				cVI.dstTime);
	}

	public void playSelectedChunks(boolean addBlips)
	{
		EDLFile edl = new EDLFile("temp file");

		boolean playFeatChunks = playFeatChunksButton.isSelected();
		Vector selectedChunks = null;

		String blipWav = dataDirectory + System.getProperty("file.separator")
				+ "blip.wav";
		double blipDuration = 0.1;

		if (playFeatChunks)
			selectedChunks = drawingPanel.renderer.getSelectedFeatChunks();
		else
			selectedChunks = drawingPanel.renderer.getSelectedEDLChunks();

		double currentTime = 0.0;

		for (int i = 0; i < selectedChunks.size(); i++)
		{
			Chunk oldChunk = null;
			if (playFeatChunks)
				oldChunk = (FeatChunk) selectedChunks.elementAt(i);
			else
				oldChunk = (EDLChunk) selectedChunks.elementAt(i);

			EDLChunk newChunk = new EDLChunk(oldChunk.srcFile,
					oldChunk.startTime, oldChunk.length, currentTime);

			// do some fading by default so it doesn't sound so bad
			newChunk.commands.add("fade");

			edl.chunks.add(newChunk);

			if (addBlips)
			{
				EDLChunk blip = new EDLChunk(blipWav, 0, blipDuration,
						currentTime);
				edl.chunks.add(blip);
			}

			currentTime += oldChunk.length;
		}
		edl.haveReadFile = true;

		Synthesizer synth = new Synthesizer(edl, null);
		playThread = new Thread(synth, "synthesizer");
		playThread.start();
	}

	public static void main(String[] args)
	{
		EDLFile eDLFile = null;
		FeatFile featFile = null;

		if (args.length == 1)
		{
			featFile = new FeatFile(args[0]);
		}
		else if (args.length == 2)
		{
			featFile = new FeatFile(args[0]);
			eDLFile = new EDLFile(args[1]);
		}
		else
		{
			System.out
					.println("I don't understand your command line arguments.");
			System.exit(-1);
		}

		try
		{
			if (featFile != null)
				featFile.readFile();

			if (eDLFile != null)
				eDLFile.readFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		Visualizer sV = new Visualizer(featFile, eDLFile);
	}

	public void actionPerformed(ActionEvent arg0)
	{
		String command = arg0.getActionCommand();

		if (command.equals("rendererSelector"))
		{
			if (rendererSelector.getSelectedIndex() == 0)
			{
				mainWindow.setVisible(false);
				drawingPanel.resetZoom();
				SegmentOrderRenderer pTPV;

				if (drawingPanel.renderer != null)
					pTPV = new SegmentOrderRenderer(drawingPanel.renderer);
				else
					pTPV = new SegmentOrderRenderer(featInputFile, eDLInputFile);
				drawingPanel.setRenderer(pTPV);
				pTPV.setDrawingPanel(drawingPanel);
				guiPanel.remove(localControlPanel);
				localControlPanel = drawingPanel.renderer
						.buildGUI(Color.orange);
				guiPanel.add(localControlPanel, BorderLayout.PAGE_END);
				guiPanel.repaint();
				mainWindow.setVisible(true);
			}
			else if (rendererSelector.getSelectedIndex() == 1)
			{
				mainWindow.setVisible(false);
				drawingPanel.resetZoom();
				ScatterPlotRenderer sPV;
				if (drawingPanel.renderer != null)
					sPV = new ScatterPlotRenderer(drawingPanel.renderer);
				else
					sPV = new ScatterPlotRenderer(featInputFile, eDLInputFile);
				drawingPanel.setRenderer(sPV);
				sPV.setDrawingPanel(drawingPanel);
				guiPanel.remove(localControlPanel);
				localControlPanel = drawingPanel.renderer
						.buildGUI(Color.orange);
				guiPanel.add(localControlPanel, BorderLayout.PAGE_END);
				guiPanel.repaint();
				mainWindow.setVisible(true);
			}
			else if (rendererSelector.getSelectedIndex() == 2)
			{
				mainWindow.setVisible(false);
				drawingPanel.resetZoom();
				BarGraphRenderer bGV;
				if (drawingPanel.renderer != null)
					bGV = new BarGraphRenderer(drawingPanel.renderer);
				else
					bGV = new BarGraphRenderer(featInputFile, eDLInputFile);
				drawingPanel.setRenderer(bGV);
				bGV.setDrawingPanel(drawingPanel);
				guiPanel.remove(localControlPanel);
				localControlPanel = drawingPanel.renderer
						.buildGUI(Color.orange);
				guiPanel.add(localControlPanel, BorderLayout.PAGE_END);
				guiPanel.repaint();
				mainWindow.setVisible(true);
			}
			else if (rendererSelector.getSelectedIndex() == 3)
			{
				mainWindow.setVisible(false);
				drawingPanel.resetZoom();
				LineGraphRenderer lGV;
				if (drawingPanel.renderer != null)
					lGV = new LineGraphRenderer(drawingPanel.renderer);
				else
					lGV = new LineGraphRenderer(featInputFile, eDLInputFile);
				drawingPanel.setRenderer(lGV);
				lGV.setDrawingPanel(drawingPanel);
				guiPanel.remove(localControlPanel);
				localControlPanel = drawingPanel.renderer
						.buildGUI(Color.orange);
				guiPanel.add(localControlPanel, BorderLayout.PAGE_END);
				guiPanel.repaint();
				mainWindow.setVisible(true);
			}
			/*
			 * else if (rendererSelector.getSelectedIndex() == 4) {
			 * mainWindow.setVisible(false); drawingPanel.resetZoom();
			 * MultiSingleFeatureRenderer fSFR; if (drawingPanel.renderer !=
			 * null) fSFR = new
			 * MultiSingleFeatureRenderer(drawingPanel.renderer); else fSFR =
			 * new MultiSingleFeatureRenderer(featInputFile, eDLInputFile);
			 * drawingPanel.setRenderer(fSFR);
			 * fSFR.setDrawingPanel(drawingPanel);
			 * guiPanel.remove(localControlPanel); localControlPanel =
			 * drawingPanel.renderer.buildGUI(Color.orange);
			 * guiPanel.add(localControlPanel, BorderLayout.PAGE_END);
			 * guiPanel.repaint(); mainWindow.setVisible(true); }
			 */
		}
		else if (command.equals("numChunksSelectedChanged"))
		{
			updateNumChunksSelectedLabel(drawingPanel.renderer
					.numChunksSelected());
		}
		else if (command.equals("selectFiles"))
		{
			selectFeatInputFile();
			// updateFileNameLabels();
			// drawingPanel.renderer.setFiles(featInputFile, eDLInputFile);
			// drawingPanel.repaint();

			selectEDLInputFile();
			updateFileNameLabels();
			drawingPanel.renderer.setFiles(featInputFile, eDLInputFile);
			drawingPanel.repaint();
		}
		/*
		 * else if (command.equals("selectEDL")) { selectEDLInputFile();
		 * updateFileNameLabels(); drawingPanel.renderer.setFiles(featInputFile,
		 * eDLInputFile); drawingPanel.repaint(); } else if
		 * (command.equals("selectFeats")) { selectFeatInputFile();
		 * updateFileNameLabels(); drawingPanel.renderer.setFiles(featInputFile,
		 * eDLInputFile); drawingPanel.repaint(); }
		 */
		else if (command.equals("saveFeatFile"))
		{
			saveFeatOutputFile();
		}
		else if (command.equals("saveEDLFile"))
		{
			saveEDLOutputFile();
		}
		else if (command.equals("updateFiles"))
		{
			drawingPanel.renderer.setFiles(featInputFile, eDLInputFile);
			drawingPanel.repaint();
		}
		else if (command.equals("playChunks"))
		{
			// stop any already existing threads
			if (playThread != null)
			{
				playThread.interrupt();
				playThread.stop();
			}

			playSelectedChunks(playAddBlipsButton.isSelected());
		}
		else if (command.equals("stopPlayback"))
		{
			if (playThread != null)
			{
				playThread.interrupt();
				playThread.stop();
				playThread = null;
			}
		}
		else if (command.equals("help"))
		{
			HelpWindow help = new HelpWindow(helpURL, "Visualizer Help",
					Color.WHITE);
		}
	}

	public void mouseClicked(MouseEvent arg0)
	{
		Point p = arg0.getPoint();

		drawingPanel.renderer.toggleSelectedForPoint(p);
		updateNumChunksSelectedLabel(drawingPanel.renderer.numChunksSelected());
		drawingPanel.repaint();
	}

	public void mousePressed(MouseEvent arg0)
	{

	}

	public void mouseReleased(MouseEvent arg0)
	{
		if (dragStart != null)
		{
			dragEnd = arg0.getPoint();

			// System.out.println(dragStart.toString() + " " +
			// dragEnd.toString());

			int xDist = dragEnd.x - dragStart.x;
			int yDist = dragEnd.y - dragStart.y;
			int x = dragStart.x;
			int y = dragStart.y;

			if (xDist < 0)
				x = dragEnd.x;

			if (yDist < 0)
				y = dragEnd.y;

			drawingPanel.renderer.setDragRect(new Rectangle(x, y, Math
					.abs(xDist), Math.abs(yDist)), dragShift);

			updateNumChunksSelectedLabel(drawingPanel.renderer
					.numChunksSelected());

			dragStart = null;
			dragEnd = null;
			dragShift = false;
		}
	}

	public void mouseEntered(MouseEvent arg0)
	{

	}

	public void mouseExited(MouseEvent arg0)
	{

	}

	public void mouseDragged(MouseEvent arg0)
	{
		if (dragStart == null)
		{
			dragStart = arg0.getPoint();

			if ((arg0.getModifiers() & MouseEvent.SHIFT_MASK) == MouseEvent.SHIFT_MASK)
				dragShift = true;
		}
		else
		{
			dragEnd = arg0.getPoint();

			// System.out.println(dragStart.toString() + " " +
			// dragEnd.toString());

			int xDist = dragEnd.x - dragStart.x;
			int yDist = dragEnd.y - dragStart.y;
			int x = dragStart.x;
			int y = dragStart.y;

			if (xDist < 0)
				x = dragEnd.x;

			if (yDist < 0)
				y = dragEnd.y;

			drawingPanel.renderer.updateDragRect(new Rectangle(x, y, Math
					.abs(xDist), Math.abs(yDist)), dragShift);
		}
	}

	public void mouseMoved(MouseEvent arg0)
	{
		Point p = arg0.getPoint();

		updateInfoLabelsForPoint(p);
	}

	public void componentResized(ComponentEvent arg0)
	{
		if (!drawingPanel.iJustZoomed)
		{

			drawingPanel.setOrigWH(drawingPanel.getWidth(), drawingPanel
					.getHeight());
			// System.out.println("cR...");
		}
		drawingPanel.iJustZoomed = false;
	}

	public void componentMoved(ComponentEvent arg0)
	{
	}

	public void componentShown(ComponentEvent arg0)
	{
	}

	public void componentHidden(ComponentEvent arg0)
	{
	}
}
