package hr.fer.su.mgc.swing;

import hr.fer.su.mgc.Config;
import hr.fer.su.mgc.classifier.ClassifierAdapter;

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

public class ClassifierLoader extends JPanel {
	private static final long serialVersionUID = -803860326803039716L;
	
	private ClassifierAdapter classifier;
	
	public ClassifierAdapter getClassifier() {
		return classifier;
	}
	
	private JComboBox hypCombo;
	
	private JLabel displayLabel;
	
	public JLabel getDisplayLabel() {
		return displayLabel;
	}
	
	private JPanel genresPanel;
	
	@SuppressWarnings("serial")
	public ClassifierLoader(final MGCSwingMain mainRef, boolean showSaveButton, boolean loadDefault) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Top panel...
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		add(panel);
		
		final JPanel hypSelector = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		panel.add(hypSelector);
		
		String[] hyps = initHyps();
		
		hypCombo = new JComboBox(hyps);
		hypSelector.add(hypCombo);
		
		
		Action action = new AbstractAction("Refresh") {
			public void actionPerformed(ActionEvent event) {
				Config.initClassifiers();
				Config.getAllClassifierNames();
				hypSelector.remove(hypCombo);
				hypCombo = new JComboBox(initHyps());
				hypSelector.add(hypCombo);
				hypSelector.revalidate();
			}
		};
		JButton button = new JButton(action);
		panel.add(button);
		
		action = new AbstractAction("Load") {
			public void actionPerformed(ActionEvent event) {
				String selection = hypCombo.getSelectedItem().toString();
				if(selection.equals("[FILE]")) {
					if (mainRef.getFileChooser().showOpenDialog(mainRef) == JFileChooser.APPROVE_OPTION) {
						File selectedFile = mainRef.getFileChooser().getSelectedFile();
						try {
							classifier = Config.loadClassifier(selectedFile);
							displayLabel.setText("<html><b>" + selectedFile.getName() + "</b></html>");
							initGenresPanel();
						} catch (Throwable e) {
							String message = "Error loading classifier " + 
								selectedFile.getName() + ". " + e.getLocalizedMessage();
							JOptionPane.showMessageDialog(mainRef, message, 
									"Error loading classifier", JOptionPane.ERROR_MESSAGE);
							mainRef.writeOut(message, true);
						}
					}
					
				} else {
					try {
						classifier = Config.getClassifier(selection);
						displayLabel.setText("<html><b>" + selection + "</b></html>");
						initGenresPanel();
					} catch (Exception e) {
						String message = "Error loading classifier " + 
							selection + ". " + e.getLocalizedMessage();
						JOptionPane.showMessageDialog(mainRef, message, 
							"Error loading classifier", JOptionPane.ERROR_MESSAGE);
						mainRef.writeOut(message, true);
					}
				}
			}
		};
		
		JButton loadButton = new JButton(action);
		panel.add(loadButton);
		
		if(showSaveButton) {
			action = new AbstractAction("Save") {
				public void actionPerformed(ActionEvent event) {
					if(classifier != null) {
						JFileChooser fc = new JFileChooser("classifiers/");
						if (fc.showSaveDialog(mainRef) == JFileChooser.APPROVE_OPTION) {
							File fileToSave = fc.getSelectedFile();
							try {
								Config.saveClassifier(classifier, fileToSave);
								displayLabel.setText("<html><b>" + fileToSave.getName() + "</b></html>");
							} catch (Exception e) {
								String message = "Error saving classifier " + e.getLocalizedMessage();
								JOptionPane.showMessageDialog(mainRef, message, 
									"Error saving classifier", JOptionPane.ERROR_MESSAGE);
								mainRef.writeOut(message, true);
							}
						}
					}
				}
			};
			
			JButton saveButton = new JButton(action);
			panel.add(saveButton);
		}
		
		
		panel.add(new JLabel("Classifier:"));
		
		displayLabel = new JLabel("<html><b>None</b></html>");
		panel.add(displayLabel);

		hypCombo.setSelectedIndex(0);
		
		// Load default hypothesis...
		if(hyps.length > 1 && loadDefault) {
			try {
				classifier = Config.getClassifier(hyps[1]);
				displayLabel.setText("<html><b>" + hyps[1] + "</b></html>");
				hypCombo.setSelectedIndex(1);
				initGenresPanel();
			} catch (Exception e) {
				System.err.println("Error loading classifer: " + hyps[1]);
			}
		}
	}
	
	public void loadCustomClassifier(ClassifierAdapter classifier) {
		this.classifier = classifier;
		displayLabel.setText("<html><b>" + "New Classifier" + "</b></html>");
		initGenresPanel();
	}
	
	private String[] initHyps() {
		String[] tmp = Config.getAllClassifierNames();
		String[] hyps = new String[tmp.length+1];
		for(int i = 0; i < tmp.length; i++) hyps[i+1] = tmp[i];
		hyps[0] = "[FILE]";
		return hyps;
	}
	
	private void initGenresPanel() {
		if(genresPanel != null) remove(genresPanel);
		if(classifier != null) {
			genresPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
			add(genresPanel);
			
			StringBuilder sb = new StringBuilder();
			String[] genres = classifier.getGenres();
			if(genres.length > 0) sb.append(genres[0]);
			for(int i = 1; i < genres.length; i++) sb.append(", " + genres[i]);
			
			genresPanel.add(new JLabel("<html><b>Genres: </b>" + sb.toString() + "</html>"));
			genresPanel.revalidate();
		}

	}
	
	public boolean classifierLoaded() {
		return classifier != null;
	}

}
