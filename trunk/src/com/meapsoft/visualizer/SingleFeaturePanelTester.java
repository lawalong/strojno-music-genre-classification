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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.meapsoft.FeatFile;
import com.meapsoft.MEAPUtil;
import com.meapsoft.disgraced.GUIUtils;

/**
 * 
 * @author douglas@music.columbia.edu
 * 
 */

public class SingleFeaturePanelTester implements MouseListener, ActionListener
{
	JFrame frame;

	JPanel mainPanel;

	JPanel checkBoxesPanel;

	JPanel buttonsPanel;

	JCheckBox showPanelInfoCheckBox;

	JComboBox showTimelineComboBox;

	JCheckBox showScaleCheckBox;

	JComboBox showSegTicksComboBox;

	JLabel infoText;

	String meapsoftDirectory = null;

	String dataDirectory = null;

	String slash = null;

	Vector aTFs = new Vector();

	Vector sFPanels = new Vector();

	Color bgColor = Color.white;

	public SingleFeaturePanelTester(String featFileName)//FeatFile fF)
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

		// System.out.println("meapsoftDirectory: " + meapsoftDirectory);
		// System.out.println("dataDirectory: " + dataDirectory);

		// if we just pass the fF that was passed to us there's a strange bug
		// were the featureDescriptions occure twice...
		if (featFileName != null)
			aTFs = processPassedFF(new FeatFile(featFileName));//fF.filename));
		else
			// we weren't passed anything in our constructor
			aTFs = selectATFF();

		buildGUI();
	}

	void buildGUI()
	{
		// System.out.println("sFPT buildGUI()");
		frame = new JFrame("SingleFeaturePanelTester");

		mainPanel = new JPanel();
		mainPanel.setBackground(bgColor);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		frame.setContentPane(mainPanel);

		buildCheckBoxesPanel();
		mainPanel.add(checkBoxesPanel);

		buildButtonsPanel();
		mainPanel.add(buttonsPanel);

		Iterator it = aTFs.iterator();
		while (it.hasNext())
			addAPanel((TypeFileFeature) it.next());

		frame.pack();
		frame.setBounds(100, 100, 600, 400);
		frame.setVisible(true);

		// System.out.println("calling frame.repaint()");
		// frame.repaint();

		// System.out.println("buildGUI() finished.");
	}

	void buildCheckBoxesPanel()
	{
		checkBoxesPanel = new JPanel();
		checkBoxesPanel.setBackground(bgColor);
		checkBoxesPanel.setLayout(new BoxLayout(checkBoxesPanel,
				BoxLayout.X_AXIS));

		showPanelInfoCheckBox = new JCheckBox("show panel info");
		showPanelInfoCheckBox.setBackground(bgColor);
		showPanelInfoCheckBox.setActionCommand("panelInfo");
		showPanelInfoCheckBox.addActionListener(this);
		showPanelInfoCheckBox.setSelected(false);
		checkBoxesPanel.add(showPanelInfoCheckBox);

		String[] timeLineOptions = { "no time ticks", "ticks every .1s",
				"ticks every 1s", "ticks every 60s" };
		showTimelineComboBox = new JComboBox(timeLineOptions);
		showTimelineComboBox.setMaximumSize(showTimelineComboBox
				.getPreferredSize());
		showTimelineComboBox.setBackground(bgColor);
		showTimelineComboBox.setActionCommand("timeLine");
		showTimelineComboBox.addActionListener(this);
		showTimelineComboBox.setSelectedIndex(0);
		checkBoxesPanel.add(showTimelineComboBox);

		String[] setTicksOptions = { "no segment ticks", "short segment ticks",
				"full segment ticks" };
		showSegTicksComboBox = new JComboBox(setTicksOptions);
		showSegTicksComboBox.setMaximumSize(showSegTicksComboBox
				.getPreferredSize());
		showSegTicksComboBox.setBackground(bgColor);
		showSegTicksComboBox.setActionCommand("segmentTicks");
		showSegTicksComboBox.addActionListener(this);
		showSegTicksComboBox.setSelectedIndex(1);
		checkBoxesPanel.add(showSegTicksComboBox);

		showScaleCheckBox = new JCheckBox("show scale");
		showScaleCheckBox.setBackground(bgColor);
		showScaleCheckBox.setActionCommand("scale");
		showScaleCheckBox.addActionListener(this);
		showScaleCheckBox.setSelected(false);
		checkBoxesPanel.add(showScaleCheckBox);

	}

	void buildButtonsPanel()
	{
		buttonsPanel = new JPanel();
		buttonsPanel.setBackground(bgColor);
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

		JButton addPanelButton = new JButton("add panel");
		addPanelButton.setBackground(bgColor);
		addPanelButton.setActionCommand("newPanel");
		addPanelButton.addActionListener(this);
		buttonsPanel.add(addPanelButton);

		JButton deletePanelButton = new JButton("delete");
		deletePanelButton.setBackground(bgColor);
		deletePanelButton.setActionCommand("deletePanel");
		deletePanelButton.addActionListener(this);
		buttonsPanel.add(deletePanelButton);

		JButton shiftUpButton = new JButton("shift up");
		shiftUpButton.setBackground(bgColor);
		shiftUpButton.setActionCommand("shiftUp");
		shiftUpButton.addActionListener(this);
		buttonsPanel.add(shiftUpButton);

		JButton shiftDownButton = new JButton("shift down");
		shiftDownButton.setBackground(bgColor);
		shiftDownButton.setActionCommand("shiftDown");
		shiftDownButton.addActionListener(this);
		buttonsPanel.add(shiftDownButton);

		JButton zoomInButton = new JButton("zoom in");
		zoomInButton.setBackground(bgColor);
		zoomInButton.setActionCommand("zoomIn");
		zoomInButton.addActionListener(this);
		buttonsPanel.add(zoomInButton);

		JButton zoomOut = new JButton("zoom out");
		zoomOut.setBackground(bgColor);
		zoomOut.setActionCommand("zoomOut");
		zoomOut.addActionListener(this);
		buttonsPanel.add(zoomOut);

		JButton resetZoom = new JButton("reset zoom");
		resetZoom.setBackground(bgColor);
		resetZoom.setActionCommand("resetZoom");
		resetZoom.addActionListener(this);
		buttonsPanel.add(resetZoom);

		JButton scrollRight = new JButton("scroll");
		scrollRight.setBackground(bgColor);
		scrollRight.setActionCommand("scrollRight");
		scrollRight.addActionListener(this);
		buttonsPanel.add(scrollRight);

		JButton scrollLeft = new JButton("scroll");
		scrollLeft.setBackground(bgColor);
		scrollLeft.setActionCommand("scrollLeft");
		scrollLeft.addActionListener(this);
		buttonsPanel.add(scrollLeft);
	}

	void rebuildPanelLayout()
	{
		// System.out.println("rebuilding layout...");
		Rectangle bounds = frame.getBounds();
		frame.setVisible(false);

		// System.out.println(" removing all panels...");
		mainPanel.removeAll();

		// System.out.println(" adding them back in...");
		Iterator it = sFPanels.iterator();
		while (it.hasNext())
			mainPanel.add((SingleFeaturePanel) it.next());

		mainPanel.add(checkBoxesPanel);
		mainPanel.add(buttonsPanel);
		// mainPanel.add(infoPanel);

		frame.pack();
		frame.setVisible(true);
		frame.setBounds(bounds);

		// System.out.println(" refreshing gui...");
		// refreshGUI();
		refreshControls();

		// System.out.println("done rebuilding layout.");
	}

	void refreshGUI()
	{
		// System.out.println("refreshGUI()");
		Iterator it = sFPanels.iterator();
		while (it.hasNext())
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			sFP.repaint();
		}
	}

	void refreshControls()
	{
		// find selected panel (if any)
		SingleFeaturePanel theSFP = null;
		Iterator it = sFPanels.iterator();
		while (it.hasNext() && theSFP == null)
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
				theSFP = sFP;
		}

		if (theSFP == null)
		{
			// resest all controls
			showScaleCheckBox.setSelected(false);
			showSegTicksComboBox.setSelectedIndex(1);
			showPanelInfoCheckBox.setSelected(false);
			showTimelineComboBox.setSelectedIndex(0);

			return;
		}

		// update controls for this panel
		showScaleCheckBox.setSelected(theSFP.showScale);
		showSegTicksComboBox.setSelectedIndex(theSFP.segTickType);
		showPanelInfoCheckBox.setSelected(theSFP.showPanelInfo);
		showTimelineComboBox.setSelectedIndex(theSFP.timelineType);
		// System.out.println("set segTickType to: " + theSFP.segTickType);
	}

	void setShowScale(boolean show)
	{
		Iterator it = sFPanels.iterator();
		while (it.hasNext())
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
				sFP.setShowScale(show);
		}
		// System.out.println("setShowScale calling refreshGUI...");
		refreshGUI();
	}

	void setSegTickType(int segTickType)
	{
		Iterator it = sFPanels.iterator();
		while (it.hasNext())
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
				sFP.setSegTickType(segTickType);
		}
		// System.out.println("setSegTickType calling refreshGUI...");
		refreshGUI();
	}

	void setShowTimeline(int timelineType)
	{
		Iterator it = sFPanels.iterator();
		while (it.hasNext())
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
				sFP.setShowTimeline(timelineType);
		}
		// System.out.println("setShowTimeline calling refreshGUI...");
		refreshGUI();
	}

	void setShowPanelInfo(boolean show)
	{
		Iterator it = sFPanels.iterator();
		while (it.hasNext())
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
				sFP.setShowPanelInfo(show);
		}
		// System.out.println("setShowPanelInfo calling refreshGUI...");
		refreshGUI();
	}

	void deselectAllPanels()
	{
		int numPanels = sFPanels.size();

		for (int i = 0; i < numPanels; i++)
			((SingleFeaturePanel) sFPanels.elementAt(i)).setSelected(false);
	}

	// deletes the currently selected panel
	void deletePanel()
	{
		boolean deleted = false;

		Iterator it = sFPanels.iterator();
		while (it.hasNext())
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
			{
				it.remove();
				deleted = true;
			}
		}

		if (deleted)
			rebuildPanelLayout();
	}

	void shiftPanelUp()
	{
		boolean shifted = false;
		int currPos = -1;
		SingleFeaturePanel shifter = null;

		Iterator it = sFPanels.iterator();
		while (it.hasNext() && !shifted)
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
			{
				currPos = sFPanels.indexOf(sFP);
				shifter = sFP;
				shifted = true;
			}
		}

		if (currPos - 1 < 0)
			return;

		if (shifted)
		{
			sFPanels.remove(shifter);
			sFPanels.add(currPos - 1, shifter);

			rebuildPanelLayout();
		}
	}

	void shiftPanelDown()
	{
		boolean shifted = false;
		int currPos = -1;
		SingleFeaturePanel shifter = null;

		Iterator it = sFPanels.iterator();
		while (it.hasNext() && !shifted)
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
			{
				currPos = sFPanels.indexOf(sFP);
				shifter = sFP;
				shifted = true;
			}
		}

		if (currPos + 1 == sFPanels.size())
			return;

		if (shifted)
		{
			sFPanels.remove(shifter);
			sFPanels.add(currPos + 1, shifter);

			rebuildPanelLayout();
		}
	}

	void zoomIn()
	{
		Iterator it = sFPanels.iterator();
		while (it.hasNext())
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
			{
				sFP.zoomIn();
			}
		}
		refreshGUI();
	}

	void zoomOut()
	{
		Iterator it = sFPanels.iterator();
		while (it.hasNext())
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
			{
				sFP.zoomOut();
			}
		}
		refreshGUI();
	}

	void resetZoom()
	{
		Iterator it = sFPanels.iterator();
		while (it.hasNext())
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
			{
				sFP.resetZoom();
			}
		}
		refreshGUI();
	}

	void scrollLeft(int numSteps)
	{
		Iterator it = sFPanels.iterator();
		while (it.hasNext())
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
			{
				for (int i = 0; i < numSteps; i++)
					sFP.incrFirstChunkToDraw();
			}
		}
		refreshGUI();
	}

	void scrollRight(int numSteps)
	{
		Iterator it = sFPanels.iterator();
		while (it.hasNext())
		{
			SingleFeaturePanel sFP = (SingleFeaturePanel) it.next();
			if (sFP.isSelected())
			{
				for (int i = 0; i < numSteps; i++)
					sFP.decrFirstChunkToDraw();
			}
		}
		refreshGUI();
	}

	boolean addAPanel(TypeFileFeature tFF)
	{
		// System.out.println("sFPT addAPanel...");

		SingleFeaturePanel sFP = null;

		if (tFF.panelType.equals("BarGraph"))
			sFP = new SingleFeatureBarGraphPanel();
		else if (tFF.panelType.equals("LineGraph"))
			sFP = new SingleFeatureLineGraphPanel();
		else if (tFF.panelType.equals("ColorBars"))
			sFP = new SingleFeatureColorBarsPanel();
		else if (tFF.panelType.equals("Waveform"))
			sFP = new SingleFeatureWaveformPanel();
		else if (tFF.panelType.equals("Spectrum"))
			sFP = new SingleFeatureSpectrumPanel();
		else
		{
			System.out.println("don't know that type of panel!");
			return false;
		}

		if (sFP.initialize(tFF.featFile, tFF.featureName) == -1)
		{
			System.out.println("hmm, something wrong, bailing.");
			return false;
		}
		// sFP.setSize(600, 400);

		sFP.addMouseListener(this);

		sFPanels.add(sFP);

		deselectAllPanels();
		sFP.setSelected(true);

		rebuildPanelLayout();

		return true;
	}

	String getPanelType()
	{
		Object[] possibilities = { "BarGraph", "LineGraph", "ColorBars" };
		String s = (String) JOptionPane.showInputDialog(frame,
				"Select a panel type:", "Customized Dialog",
				JOptionPane.PLAIN_MESSAGE, null, possibilities, "BarGraph");
		return s;
	}

	Vector getFeatureFromFile(FeatFile fF)
	{
		Vector features = fF.featureDescriptions;
		int numFeatures = features.size();
		// System.out.println("numFeatures: " + numFeatures);

		// we've got some actual features
		if (numFeatures > 0)
		{
			Object[] choices = new Object[numFeatures + 4];

			for (int i = 0; i < numFeatures; i++)
			{
				String name = (String) features.elementAt(i);
				 System.out.println("i: " + i + " name: " + name);
				// we're splitting on "." but have to use an escape sequence!
				String[] chunks = name.split("\\.");
				choices[i] = chunks[chunks.length - 1];
			}
			choices[numFeatures] = "Length";
			choices[numFeatures + 1] = "Waveform";
			choices[numFeatures + 2] = "Spectrum";
			choices[numFeatures + 3] = "all";

			String s = (String) JOptionPane.showInputDialog(frame,
					"Select a feature:", "Customized Dialog",
					JOptionPane.PLAIN_MESSAGE, null, choices, "");
			Vector v = new Vector();

			if (s.equals("all"))
			{
				for (int i = 0; i < numFeatures; i++)
				{
					// System.out.println("adding " +
					// (String)features.elementAt(i) + " to vector.");
					v.add(choices[i]);
				}
				v.add("Length");
				v.add("Waveform");
				v.add("Spectrum");
			}
			else
				v.add(s);

			return v;
		}
		// features, probably a .seg file
		else
		{
			Object[] choices = new Object[4];

			choices[0] = "Length";
			choices[1] = "Waveform";
			choices[2] = "Spectrum";
			choices[3] = "all";

			String s = (String) JOptionPane.showInputDialog(frame,
					"Select a feature:", "Customized Dialog",
					JOptionPane.PLAIN_MESSAGE, null, choices, "");
			Vector v = new Vector();

			if (s.equals("all"))
			{
				v.add("Length");
				v.add("Waveform");
				v.add("Spectrum");
			}
			else
				v.add(s);

			return v;
		}
	}

	public Vector selectATFF()
	{
		String names[] = GUIUtils.FileSelector(GUIUtils.ANYMEAP, dataDirectory,
				frame);

		if (names[0] == null)
			return null;

		FeatFile fF = new FeatFile(names[0]);
		return processPassedFF(fF);
	}

	public Vector processPassedFF(FeatFile fF)
	{
		try
		{
			fF.readFile();
			// System.out.println("features size(): " +
			// fF.featureDescriptions.size());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}

		Vector selectedFeatures = getFeatureFromFile(fF);

		String panelType = null;

		// only valid for normal features!
		// we still need to kludge this below for Waveform & Spectrum
		boolean needPanelType = false;

		int numFeatures = selectedFeatures.size();

		// boy this is ugly!
		for (int i = 0; i < numFeatures; i++)
		{
			String featName = (String) selectedFeatures.elementAt(i);

			if (!featName.equals("Waveform") && !featName.equals("Spectrum"))
				needPanelType = true;
		}

		if (needPanelType)
			panelType = getPanelType();

		Vector v = new Vector();

		for (int i = 0; i < numFeatures; i++)
		{
			String featName = (String) selectedFeatures.elementAt(i);

			TypeFileFeature fAF = null;

			if (featName.equals("Waveform"))
				fAF = new TypeFileFeature("Waveform", fF, fF.filename, featName);
			else if (featName.equals("Spectrum"))
				fAF = new TypeFileFeature("Spectrum", fF, fF.filename, featName);
			else
				fAF = new TypeFileFeature(panelType, fF, fF.filename, featName);

			v.add(fAF);
		}
		return v;
	}

	public void mouseClicked(MouseEvent e)
	{
		SingleFeaturePanel sourcePanel = (SingleFeaturePanel) e.getComponent();

		int numPanels = sFPanels.size();

		for (int i = 0; i < numPanels; i++)
		{
			SingleFeaturePanel testPanel = (SingleFeaturePanel) sFPanels
					.elementAt(i);
			if (sourcePanel == testPanel)
			{
				sourcePanel.toggleSelected();
			}
		}

		for (int i = 0; i < numPanels; i++)
		{
			SingleFeaturePanel testPanel = (SingleFeaturePanel) sFPanels
					.elementAt(i);
			if (sourcePanel != testPanel)
			{

				if (sourcePanel.isSelected())
					testPanel.setSelected(false);
			}
		}
		refreshGUI();
		refreshControls();
	}

	public void mouseEntered(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void actionPerformed(ActionEvent arg0)
	{
		String command = arg0.getActionCommand();

		if (command.equals("newPanel"))
		{
			Vector aTFs = selectATFF();

			Iterator it = aTFs.iterator();
			while (it.hasNext())
				addAPanel((TypeFileFeature) it.next());
		}
		else if (command.equals("deletePanel"))
		{
			deletePanel();
		}
		else if (command.equals("shiftUp"))
		{
			shiftPanelUp();
		}
		else if (command.equals("shiftDown"))
		{
			shiftPanelDown();
		}
		else if (command.equals("scale"))
		{
			// System.out.println("got tick toggle...");
			JCheckBox source = (JCheckBox) arg0.getSource();
			setShowScale(source.isSelected());
		}
		else if (command.equals("timeLine"))
		{
			// System.out.println("got tick toggle...");
			JComboBox source = (JComboBox) arg0.getSource();
			setShowTimeline(source.getSelectedIndex());
		}
		else if (command.equals("segmentTicks"))
		{
			// System.out.println("got tick toggle...");
			// JCheckBox source = (JCheckBox)arg0.getSource();
			// setShowSegmentTicks(source.isSelected());

			JComboBox source = (JComboBox) arg0.getSource();
			setSegTickType(source.getSelectedIndex());
		}
		else if (command.equals("panelInfo"))
		{
			// System.out.println("got tick toggle...");
			JCheckBox source = (JCheckBox) arg0.getSource();
			setShowPanelInfo(source.isSelected());
		}
		else if (command.equals("zoomIn"))
		{
			zoomIn();
		}
		else if (command.equals("zoomOut"))
		{
			zoomOut();
		}
		else if (command.equals("resetZoom"))
		{
			resetZoom();
		}
		else if (command.equals("scrollRight"))
		{
			scrollRight(2);
		}
		else if (command.equals("scrollLeft"))
		{
			scrollLeft(2);
		}
	}

	public static void main(final String[] args)
	{
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				System.out.println("opening: " + args[0]);
				new SingleFeaturePanelTester(args[0]);
			}
		});
	}

}
