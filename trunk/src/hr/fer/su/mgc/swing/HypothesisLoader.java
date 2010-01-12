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

public class HypothesisLoader extends JPanel {
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
	public HypothesisLoader(final MGCSwingMain mainRef, final JFileChooser fileChooser) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		
		// Top panel...
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		add(panel);
		
		String[] tmp = Config.getAllHypothesesNames();
		String[] hyps = new String[tmp.length+1];
		for(int i = 0; i < tmp.length; i++) hyps[i+1] = tmp[i];
		hyps[0] = "[FILE]";
		
		hypCombo = new JComboBox(hyps);
		panel.add(hypCombo);
		
		Action browseAction = new AbstractAction("Load") {
			public void actionPerformed(ActionEvent event) {
				String selection = hypCombo.getSelectedItem().toString();
				if(selection.equals("[FILE]")) {
					if (fileChooser.showOpenDialog(mainRef) == JFileChooser.APPROVE_OPTION) {
						File selectedFile = fileChooser.getSelectedFile();
						try {
							classifier = Config.loadHypothesis(selectedFile);
							displayLabel.setText(selectedFile.getName());
							initGenresPanel();
						} catch (Exception e) {
							String message = "Error loading hypothesis " + 
								selectedFile.getName() + ". " + e.getLocalizedMessage();
							JOptionPane.showMessageDialog(mainRef, message, 
									"Error loading hypothesis", JOptionPane.ERROR_MESSAGE);
							mainRef.writeOut(message, true);
						}
					}
					
				} else {
					try {
						classifier = Config.getHypothesis(selection);
						displayLabel.setText(selection);
						initGenresPanel();
					} catch (Exception e) {
						String message = "Error loading hypothesis " + 
							selection + ". " + e.getLocalizedMessage();
						JOptionPane.showMessageDialog(mainRef, message, 
							"Error loading hypothesis", JOptionPane.ERROR_MESSAGE);
						mainRef.writeOut(message, true);
					}
				}
			}
		};
		
		JButton loadButton = new JButton(browseAction);
		panel.add(loadButton);
		
		panel.add(new JLabel("Hypothesis:"));
		
		displayLabel = new JLabel("None<");
		panel.add(displayLabel);

		hypCombo.setSelectedIndex(0);
		
		// Load default hypothesis...
		if(hyps.length > 1) {
			try {
				classifier = Config.getHypothesis(hyps[1]);
				displayLabel.setText(hyps[1]);
				hypCombo.setSelectedIndex(1);
				initGenresPanel();
			} catch (Exception e) {
				System.err.println("Error loading classifer: " + hyps[1]);
			}
		}
		
		
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
		}

	}
	
	public boolean hypothesisLoaded() {
		return classifier != null;
	}

}
