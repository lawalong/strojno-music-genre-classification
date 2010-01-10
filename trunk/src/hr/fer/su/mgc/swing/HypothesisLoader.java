package hr.fer.su.mgc.swing;

import hr.fer.su.mgc.Config;
import hr.fer.su.mgc.classifier.ClassifierAdapter;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class HypothesisLoader extends JPanel {
	private static final long serialVersionUID = -803860326803039716L;
	
	private ClassifierAdapter classifier;
	
	private JComboBox hypCombo;
	
	private JLabel displayLabel;
	
	@SuppressWarnings("serial")
	public HypothesisLoader(final MGCSwingMain mainRef, final JFileChooser fileChooser) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
		
		String[] tmp = Config.getAllHypothesesNames();
		String[] hyps = new String[tmp.length+1];
		for(int i = 0; i < tmp.length; i++) hyps[i+1] = tmp[i];
		hyps[0] = "[FILE]";
		
		hypCombo = new JComboBox(hyps);
		add(hypCombo);
		
		Action browseAction = new AbstractAction("Load") {
			public void actionPerformed(ActionEvent event) {
				String selection = hypCombo.getSelectedItem().toString();
				if(selection.equals("[FILE]")) {
					if (fileChooser.showOpenDialog(mainRef) == JFileChooser.APPROVE_OPTION) {
						File selectedFile = fileChooser.getSelectedFile();
						try {
							classifier = Config.loadHypothesis(selectedFile);
							displayLabel.setText(selectedFile.getName());
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
		add(loadButton);
		
		add(new JLabel("Hypothesis:"));
		
		displayLabel = new JLabel("None<");
		add(displayLabel);

		hypCombo.setSelectedIndex(0);
		
		// Load default hypothesis...
		if(hyps.length > 1) {
			try {
				classifier = Config.getHypothesis(hyps[1]);
				displayLabel.setText(hyps[1]);
				hypCombo.setSelectedIndex(1);
			} catch (Exception e) {
				System.err.println("Error loading classifer: " + hyps[1]);
			}
		}
	}
	
	public boolean fileLoaded() {
		return classifier != null;
	}
	

	public ClassifierAdapter getClassifier() {
		return classifier;
	}

	public JLabel getDisplayLabel() {
		return displayLabel;
	}

}
