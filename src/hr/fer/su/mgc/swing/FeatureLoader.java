package hr.fer.su.mgc.swing;

import hr.fer.su.mgc.Config;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import weka.core.converters.ConverterUtils.DataSource;

public class FeatureLoader extends JPanel {
	private static final long serialVersionUID = -803860326803039716L;
	
	private DataSource features;
	
	public DataSource getFeatures() {
		return features;
	}

	private JComboBox featCombo;
	
	private JLabel displayLabel;
	
	public JLabel getDisplayLabel() {
		return displayLabel;
	}
	
	private JPanel genresPanel;
	
	@SuppressWarnings("serial")
	public FeatureLoader(final MGCSwingMain mainRef) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Top panel...
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		add(panel);
		
		final JPanel featSelector = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		panel.add(featSelector);
		
		String[] featureNames = initFeatures();
		
		featCombo = new JComboBox(featureNames);
		featSelector.add(featCombo);
		
		
		Action action = new AbstractAction("Refresh") {
			public void actionPerformed(ActionEvent event) {
				Config.initFeatures();
				featSelector.remove(featCombo);
				featCombo = new JComboBox(initFeatures());
				featSelector.add(featCombo);
				featSelector.revalidate();
			}
		};
		JButton button = new JButton(action);
		panel.add(button);
		
		action = new AbstractAction("Load") {
			public void actionPerformed(ActionEvent event) {
				String selection = featCombo.getSelectedItem().toString();
				if(selection.equals("[FILE]")) {
					if (mainRef.getFileChooser().showOpenDialog(mainRef) == JFileChooser.APPROVE_OPTION) {
						File selectedFile = mainRef.getFileChooser().getSelectedFile();
						try {
							features = Config.loadFeatures(selectedFile);
							displayLabel.setText("<html><b>" + selectedFile.getName() + "</b></html>");
							initGenresPanel();
						} catch (Throwable e) {
							String message = "Error loading features " + 
								selectedFile.getName() + ". " + e.getLocalizedMessage();
							JOptionPane.showMessageDialog(mainRef, message, 
									"Error loading features", JOptionPane.ERROR_MESSAGE);
							mainRef.writeOut(message, true);
						}
					}
					
				} else {
					try {
						features = Config.loadFeatures(selection);
						displayLabel.setText("<html><b>" + selection + "</b></html>");
						initGenresPanel();
					} catch (Exception e) {
						String message = "Error loading features " + 
							selection + ". " + e.getLocalizedMessage();
						JOptionPane.showMessageDialog(mainRef, message, 
							"Error loading features", JOptionPane.ERROR_MESSAGE);
						mainRef.writeOut(message, true);
					}
				}
			}
		};
		
		JButton loadButton = new JButton(action);
		panel.add(loadButton);
		
		
		panel.add(new JLabel("Loaded Features:"));
		
		displayLabel = new JLabel("<html><b>None</b></html>");
		panel.add(displayLabel);

		featCombo.setSelectedIndex(0);
		
		// Load default hypothesis...
		if(featureNames.length > 1) {
			try {
				features = Config.loadFeatures(featureNames[1]);
				displayLabel.setText("<html><b>" + featureNames[1] + "</b></html>");
				featCombo.setSelectedIndex(1);
				initGenresPanel();
			} catch (Exception e) {
				System.err.println("Error loading classifer: " + featureNames[1]);
			}
		}
	}
	
	private String[] initFeatures() {
		String[] tmp = Config.getAllFeatureNames();
		String[] featureNames = new String[tmp.length+1];
		for(int i = 0; i < tmp.length; i++) featureNames[i+1] = tmp[i];
		featureNames[0] = "[FILE]";
		return featureNames;
	}
	
	private void initGenresPanel() {
		if(genresPanel != null) remove(genresPanel);
		if(features != null) {
			genresPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
			add(genresPanel);

			// Update genres...
			try {
				StringBuilder sb = new StringBuilder();
				
				String genreString = features.getDataSet().attribute("class").toString();
				
				genreString = genreString.substring(genreString.indexOf('{') + 1,
						genreString.lastIndexOf('}'));
				String[] parts = genreString.split(",");
				for (String part : parts) part = part.trim();
				String[] genres = parts;
				
				if(genres.length > 0) sb.append(genres[0]);
				for(int i = 1; i < genres.length; i++) sb.append(", " + genres[i]);
				
				genresPanel.add(new JLabel("<html><b>Genres: </b>" + sb.toString() + "</html>"));
			} catch (Exception e) {
				genresPanel.add(new JLabel("<html><b>Genres: </b>" + 
						"Error extracting genres from feature file?!" + "</html>"));
			}

		}

	}
	
	public boolean featuresLoaded() {
		return features != null;
	}

}
