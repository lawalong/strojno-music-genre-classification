package hr.fer.su.mgc.swing;

import hr.fer.su.mgc.audio.AudioFile;
import hr.fer.su.mgc.classifier.ClassifierAdapter;
import hr.fer.su.mgc.conv.ConversionException;
import hr.fer.su.mgc.conv.MGCconv;
import hr.fer.su.mgc.features.FeatureExtractor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PlayerPanel extends JPanel {
	private static final long serialVersionUID = -1226334005551881012L;
	
	protected AudioFile audioFile;
	
	public AudioFile getAudioFile() {
		return audioFile;
	}
	
	protected JLabel tagLabel;

	public JLabel getTagLabel() {
		return tagLabel;
	}

	protected JLabel timeLabel;
	
	public JLabel getTimeLabel() {
		return timeLabel;
	}
	
	protected JSlider playerSlider;

	public JSlider getPlayerSlider() {
		return playerSlider;
	}

	private JLabel displayLabel;
	
	public JLabel getDisplayLabel() {
		return displayLabel;
	}

	
	@SuppressWarnings("serial")
	public PlayerPanel(final MGCSwingMain mainRef, final JFileChooser fileChooser, 
			int top, int left, int bottom, int right) {
		super(new BorderLayout());
		
		tagLabel = new JLabel();
		timeLabel = new JLabel();
		playerSlider = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
		
		setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createTitledBorder("Audio Loader"));
		add(mainPanel, BorderLayout.CENTER);
		
		
		// Load panel init...
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		mainPanel.add(panel);
		
		Action action = new AbstractAction("Load") {
			public void actionPerformed(ActionEvent event) {
				int status = fileChooser.showOpenDialog(mainRef);

				if (status == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					try {
						if(audioFile != null) {
							tagLabel.setText("");
							timeLabel.setText("");
							audioFile.stop();
						}
						audioFile = new AudioFile(selectedFile.getAbsolutePath(), mainRef);
						displayLabel.setText(selectedFile.getName());
					} catch (UnsupportedAudioFileException e) {
						JOptionPane.showMessageDialog(mainRef, "Unsupported Audio File!", 
								"Audio loading error", JOptionPane.ERROR_MESSAGE);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(mainRef, "IO Error: " + e.getLocalizedMessage(), 
								"Audio loading error", JOptionPane.ERROR_MESSAGE);
					}

				}
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "Load an audio file...");
		JButton button = new JButton(action);
		panel.add(button);
		
		panel.add(new JLabel("Audio:"));
		
		displayLabel = new JLabel("<html><b>None</b></html>");
		panel.add(displayLabel);
		
		
		// Tags panel init...
		
		JPanel tagsPanel = new JPanel(new BorderLayout());
		tagsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		tagsPanel.add(tagLabel, BorderLayout.WEST);
		tagsPanel.add(timeLabel, BorderLayout.EAST);
		mainPanel.add(tagsPanel);
		
		
		// Slider panel init...
		
		JPanel sliderPanel = new JPanel(new BorderLayout());
		sliderPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		mainPanel.add(sliderPanel);
		
		playerSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		//Turn on labels at major tick marks.
		playerSlider.setMajorTickSpacing(10000);
		playerSlider.setMinorTickSpacing(2000);
		playerSlider.setPaintTicks(true);
		playerSlider.setPaintLabels(true);
		
		Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
		for(int i = playerSlider.getMinimum(); 
			i <= playerSlider.getMaximum(); i += playerSlider.getMajorTickSpacing()) {
			
			int secs = i/1000, mins = 0;
			while(secs >= 60) { mins++; secs -= 60; }
			
			labelTable.put(i, new JLabel(String.valueOf(mins) + 
					":" + ((secs < 10) ? "0" + secs : secs)));
		}
		playerSlider.setLabelTable(labelTable);

		sliderPanel.add(playerSlider, BorderLayout.CENTER);
		
		
		// Init buttons...
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		mainPanel.add(buttonsPanel);
		
		JPanel buttonsPanelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		buttonsPanelLeft.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		buttonsPanel.add(buttonsPanelLeft);
		
		JPanel buttonsPanelRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
		buttonsPanelRight.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		buttonsPanel.add(buttonsPanelRight);
		
		Action playerPlay = new AbstractAction("Play") {
			public void actionPerformed(ActionEvent event) {
				if(audioFile != null) {
					try {
						audioFile.play(mainRef);
					} catch (UnsupportedAudioFileException e) {
						JOptionPane.showMessageDialog(mainRef, "Unsupported Audio File!", 
								"Audio loading error", JOptionPane.ERROR_MESSAGE);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(mainRef, "IO Error: " + e.getLocalizedMessage(), 
								"Audio playback error", JOptionPane.ERROR_MESSAGE);
					} catch (LineUnavailableException e) {
						JOptionPane.showMessageDialog(mainRef, "No line is available!", 
								"Audio playback error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		playerPlay.putValue(Action.SHORT_DESCRIPTION, "Start playback");
		JButton playButton = new JButton(playerPlay);
		buttonsPanelLeft.add(playButton);

		
		Action playerPause = new AbstractAction("Pause") {
			public void actionPerformed(ActionEvent event) {
				if(audioFile != null) {
					audioFile.pause();
				}
			}
		};
		playerPause.putValue(Action.SHORT_DESCRIPTION, "Pause playback");
		JButton pauseButton = new JButton(playerPause);
		buttonsPanelLeft.add(pauseButton);
		
		
		Action playerStop = new AbstractAction("Stop") {
			public void actionPerformed(ActionEvent event) {
				if(audioFile != null) {
					audioFile.stop();
				}
			}
		};
		playerStop.putValue(Action.SHORT_DESCRIPTION, "Stop playback");
		JButton stopButton = new JButton(playerStop);
		buttonsPanelLeft.add(stopButton);
		
		Action classify = new AbstractAction("Classify") {
			public void actionPerformed(ActionEvent event) {
				if(audioFile != null && mainRef.getHypLoader().hypothesisLoaded()) {
					new ClassificationThread().start();
				}
			}
			
			class ClassificationThread extends Thread {

				@Override
				public void run() {
					try {
						ClassifierAdapter classifier = mainRef.getHypLoader().getClassifier();
						final String[] genres = classifier.getGenres();
						
						mainRef.writeOut("CLASSIFICATION: Starting classificaton of " +
								audioFile.getAudioFile().getName(), false);
						
						FeatureExtractor featureExtractor = new FeatureExtractor(genres);
						
						long time = System.currentTimeMillis();
						
						File tempFile;
						try {
							tempFile = MGCconv.convertForClassification(audioFile.getAudioFile());
						} catch (ConversionException cex) {
							String message = 
								"CLASSIFICATION ERROR: " + cex.getLocalizedMessage();
							JOptionPane.showMessageDialog(mainRef, message, 
									"Classification Error", JOptionPane.ERROR_MESSAGE);
							mainRef.writeOut(message, true);
							return;
						}
						
						mainRef.writeOut("CLASSIFICATION: Prepared audio for classification in " + 
								((System.currentTimeMillis()-time)/1000f) + " seconds.", false);
						
						time = System.currentTimeMillis();
						
						File song = featureExtractor.extractSongFeatures(new File[] {tempFile});
						
						mainRef.writeOut("CLASSIFICATION: Extracted features from audio in " + 
								((System.currentTimeMillis()-time)/1000f) + " seconds.", false);
						
						if(tempFile.exists()) tempFile.delete();
						
						time = System.currentTimeMillis();

						final double[] result = classifier.classifyInstances(song).get(0);
						
						if(song.exists()) song.delete();
						
						mainRef.writeOut("CLASSIFICATION: Classification completed in " + 
								((System.currentTimeMillis() - time)/1000f) + " seconds.", false);
						
						StringBuilder sb = new StringBuilder();
						double max = 0; int ind = -1;
						for (int i = 0; i < result.length; ++i) {
							sb.append(String.format("%.3f ", result[i]));
							if (max < result[i]) {
								max = result[i];
								ind = i;
							}
						}
						
						mainRef.writeOut("CLASSIFICATION: => " + sb.toString() + "-> " + genres[ind], false);
						
						final int tempInd = ind;
						SwingUtilities.invokeLater(new Runnable() {
						    public void run() {
								long time = System.currentTimeMillis();
								
								mainRef.updateCharts(genres, result, tempInd);

								mainRef.writeOut("Updating charts took " + 
										((System.currentTimeMillis() - time)/1000f) + " seconds.", false);
						    }
						});
						
					} catch (Throwable e) {
						String message = 
							"Error ocurred trying to classify audio file. " + e.getLocalizedMessage();
						JOptionPane.showMessageDialog(mainRef, message, 
								"Classification Error", JOptionPane.ERROR_MESSAGE);
						mainRef.writeOut(message, true);
					}
					super.run();
				}
				
			}
		};
		classify.putValue(Action.SHORT_DESCRIPTION, "Classify audio genre.");
		JButton classifyButton = new JButton(classify);
		buttonsPanelRight.add(classifyButton);
		
	}

}
