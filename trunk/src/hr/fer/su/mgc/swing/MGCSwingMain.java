package hr.fer.su.mgc.swing;

import hr.fer.su.mgc.Config;
import hr.fer.su.mgc.audio.AudioFile;
import hr.fer.su.mgc.swing.image.ImageUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.FontUIResource;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.skin.SubstanceBusinessBlueSteelLookAndFeel;

import com.nilo.plaf.nimrod.NimRODLookAndFeel;

public class MGCSwingMain extends JFrame {
	private static final long serialVersionUID = -489245112812832880L;
	
	/** 
	 * Main frame reference..
	 */
	public static Frame frameRef;
	
	/**
	 * Map that contains used actions.
	 */
	private Map<String, Action> actions;
	
	/**
	 * Audio file chooser reference.
	 */
	private JFileChooser fileChooser;
	
	private AudioFile audioFile;
	
	public JLabel tagLabel;
	public JLabel timeLabel;
	public JTextField inputFieldL;
	public JTextField inputFieldC;
	public JSlider playerSlider;
	
	
	// Learner vars...
	
	/**
	 * Dataset dir.
	 */
	private File dataset;
	
	private JComboBox algCombo;
	
	private JComboBox crossValCombo;
	
	private JProgressBar progressBar;

	/**
	 * Output.
	 */
	private JTextArea output;
	
	
	public MGCSwingMain() {
		
		frameRef = this;
		
		LookAndFeel lf = new SubstanceBusinessBlueSteelLookAndFeel();
		
		try {
			UIManager.setLookAndFeel(lf);
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception Ignorable) { }
		
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(100, 100);
		setSize(screenSize.width - 200, screenSize.height - 200);
		setMaximizedBounds(GraphicsEnvironment.
				getLocalGraphicsEnvironment().getMaximumWindowBounds());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO: Do something...
			}
		});
		setTitle("Music Genre Classifier");
		
		actions = createActions();
		ImageUtils.loadIconsAndComponents();
		
		initGUI();
	}
	
	/**
	 * GUI Initialization...
	 */
	@SuppressWarnings("serial")
	private void initGUI() {
		final MGCSwingMain mainRef = this;
		
		// Classifier vars...
		
		tagLabel = new JLabel();
		timeLabel = new JLabel();
		inputFieldC = new JTextField();
		playerSlider = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
		
		// Learner vars...
		inputFieldL = new JTextField();
		
		
		createMenus();
		
		setLayout(new BorderLayout());
		final JPanel rootPanel = new JPanel(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		add(rootPanel);

		NamedBorderedPanel outputPanel = new NamedBorderedPanel("Output", 8, 0, 0, 0) {
			@Override
			public void init() {
				output = new JTextArea();
				output.setEditable(false);
				output.setBackground(rootPanel.getBackground());
				this.panel.setLayout(new BorderLayout());
				this.panel.add(new JScrollPane(output), BorderLayout.CENTER);
				this.setPreferredSize(new Dimension(0, 150));
				this.setMaximumSize(new Dimension(5000, 150));
			}
		};
		rootPanel.add(outputPanel, BorderLayout.SOUTH);
		
		JTabbedPane tabPane = new JTabbedPane();
		rootPanel.add(tabPane, BorderLayout.CENTER);
		JPanel classifierPanel = new JPanel(new BorderLayout());
		JPanel learnersPanel = new JPanel(new BorderLayout());
		tabPane.add("Classifier", classifierPanel);
		tabPane.add("Learning", learnersPanel);
		
		
		// Init learner...
		
		JPanel learnerNorth = new JPanel();
		learnerNorth.setLayout(new BoxLayout(learnerNorth, BoxLayout.Y_AXIS));
		learnersPanel.add(learnerNorth, BorderLayout.NORTH);
		
		JPanel datasetPanel = new JPanel();
		learnerNorth.add(datasetPanel, BorderLayout.NORTH);
		datasetPanel.setLayout(new BoxLayout(datasetPanel, BoxLayout.Y_AXIS));
		datasetPanel.setBorder(BorderFactory.createTitledBorder("Dataset Learner"));
		
		JPanel browsePanel = new JPanel(new BorderLayout(8, 0));
		browsePanel.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
		browsePanel.add(new JLabel("Dataset Path: "), BorderLayout.WEST);
		browsePanel.add(inputFieldL, BorderLayout.CENTER);
		
		Action browseAction = new AbstractAction("Browse") {
			public void actionPerformed(ActionEvent event) {
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fileChooser.showOpenDialog(mainRef) == JFileChooser.APPROVE_OPTION) {
					dataset = fileChooser.getSelectedFile();
					inputFieldL.setText(dataset.getAbsolutePath());
				}
			}
		};
		browseAction.putValue(Action.SHORT_DESCRIPTION, "Select dataset path.");
		
		JButton browseButton = new JButton(browseAction);
		browsePanel.add(browseButton, BorderLayout.EAST);
		datasetPanel.add(browsePanel);
		
		
		JPanel learnParams = new JPanel(new BorderLayout());
		datasetPanel.add(learnParams);
		learnParams.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
		JPanel learnParamsLeft = new JPanel();
		learnParams.add(learnParamsLeft, BorderLayout.CENTER);
		learnParamsLeft.setLayout(new BoxLayout(learnParamsLeft, BoxLayout.X_AXIS));
		
		learnParamsLeft.add(new JLabel("Learning algorithm: "));
		algCombo = new JComboBox(new String[] {"LogitBoost", "SMO"});
		algCombo.setMaximumSize(new Dimension(100, 30));
		algCombo.setSelectedIndex(1);
		learnParamsLeft.add(algCombo);
		learnParamsLeft.add(new JLabel("  Cross-Validation: "));
		crossValCombo = new JComboBox(new String[] {"OFF", "2", "5", "10"});
		crossValCombo.setMaximumSize(new Dimension(100, 30));
		crossValCombo.setSelectedIndex(0);
		learnParamsLeft.add(crossValCombo);

		learnParams = new JPanel(new BorderLayout(8, 0));
		datasetPanel.add(learnParams);
		learnParams.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
		
		progressBar = new JProgressBar(0, 1050);
//		progressBar.setIndeterminate(true);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		learnParams.add(progressBar, BorderLayout.CENTER);
		
		Action learnDataset = new AbstractAction("Learn") {
			public void actionPerformed(ActionEvent event) {
				if(audioFile != null) {
					try {
						// TODO
					} catch (Exception e) {
						JOptionPane.showMessageDialog(mainRef, "No line is available!", 
								"Audio playback error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		learnDataset.putValue(Action.SHORT_DESCRIPTION, "Start learning.");
		JButton learnButton = new JButton(learnDataset);
		learnParams.add(learnButton, BorderLayout.EAST);
		
		
		
		// Init classifier...
		
		JPanel classifierNorth = new JPanel();
		classifierNorth.setLayout(new BoxLayout(classifierNorth, BoxLayout.Y_AXIS));
		classifierPanel.add(classifierNorth, BorderLayout.NORTH);
		
		JPanel selectHypPanel = new JPanel(new BorderLayout());
		JPanel selectHypPanelInner = new JPanel();
		selectHypPanelInner.setLayout(new BoxLayout(selectHypPanelInner, BoxLayout.X_AXIS));
		selectHypPanelInner.add(new JLabel("<html><font size=+1>Select hypothesis: </font></html>"));
		JComboBox hypCombo = new JComboBox(Config.getAllHypothesesNames());
		selectHypPanelInner.add(hypCombo);
		selectHypPanel.add(selectHypPanelInner, BorderLayout.WEST);
		classifierNorth.add(selectHypPanel, BorderLayout.NORTH);
		
		JPanel playerPanel = new JPanel();
		classifierNorth.add(playerPanel, BorderLayout.NORTH);
		playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
		playerPanel.setBorder(BorderFactory.createTitledBorder("Player"));
		
		browsePanel = new JPanel(new BorderLayout(8, 0));
		browsePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		browsePanel.add(inputFieldC, BorderLayout.CENTER);
		
		browseAction = new AbstractAction("Browse") {
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
						audioFile = new AudioFile(
								selectedFile.getAbsolutePath(), mainRef);
						inputFieldC.setText(selectedFile.getName());
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
		browseAction.putValue(Action.SHORT_DESCRIPTION, "Browse for audio file...");
		
		browseButton = new JButton(browseAction);
		browsePanel.add(browseButton, BorderLayout.EAST);
		playerPanel.add(browsePanel);
		
		
		JPanel tagsPanel = new JPanel(new BorderLayout());
		tagsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		tagsPanel.add(tagLabel, BorderLayout.WEST);
		tagsPanel.add(timeLabel, BorderLayout.EAST);
		playerPanel.add(tagsPanel);
		
		JPanel sliderPanel = new JPanel(new BorderLayout());
		sliderPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
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
		playerPanel.add(sliderPanel);
		
		
		JPanel buttonsPanel = new JPanel(new BorderLayout());
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		final JPanel buttonsPanelLeft = new JPanel(new GridLayout(1, 3, 8, 0));
		
		
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
		
		buttonsPanel.add(buttonsPanelLeft, BorderLayout.WEST);
		
		playerPanel.add(buttonsPanel);
	}
	
	/**
	 * Menus Creation.
	 */
	private void createMenus() {
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenu menuFile = new JMenu("File");
		
		menuFile.addSeparator();
		
		JMenuItem exitMenuItem = new JMenuItem(actions.get("Exit"));
		// exitMenuItem.setIcon(ImageUtils.getIcon("exitSim"));
		menuFile.add(exitMenuItem);
		
		menuBar.add(menuFile);
		
		
		
		JMenu menuLNF = new JMenu("l'n'f");
		
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(actions.get("systemLNF"));
		rbMenuItem.setMnemonic(KeyEvent.VK_S);
		group.add(rbMenuItem);
		menuLNF.add(rbMenuItem);
		
		rbMenuItem = new JRadioButtonMenuItem(actions.get("nimrodLNF"));
		rbMenuItem.setMnemonic(KeyEvent.VK_N);
		group.add(rbMenuItem);
		menuLNF.add(rbMenuItem);
		
		rbMenuItem = new JRadioButtonMenuItem(actions.get("substanceLNF"));
		rbMenuItem.setSelected(true);
		rbMenuItem.setMnemonic(KeyEvent.VK_B);
		group.add(rbMenuItem);
		menuLNF.add(rbMenuItem);
		
		menuBar.add(menuLNF);
		
		
		JMenu menuHelp = new JMenu("Help");
		
		JMenuItem aboutMenuItem = new JMenuItem(actions.get("helpAbout"));
		menuHelp.add(aboutMenuItem);
		
		menuBar.add(menuHelp);

		setJMenuBar(menuBar);
	}
	
	
	/**
	 * Creation of actions.
	 */
	@SuppressWarnings("serial")
	private Map<String, Action> createActions() {
		actions = new HashMap<String, Action>();
		
		fileChooser = new JFileChooser(".");
		
		Action action;
		
		
		// Menus actions...

		action = new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "Exit application");
		action.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
		actions.put("Exit", action);
		
		action = new AbstractAction("About MGC") {
			public void actionPerformed(ActionEvent event) {
				// TODO
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "About Music Genre Classifier");
		action.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		actions.put("helpAbout", action);
		
		
		action = new AbstractAction("System") {
			public void actionPerformed(ActionEvent event) {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					SwingUtilities.updateComponentTreeUI(frameRef);
				} catch (Throwable thr) {
					JOptionPane.showMessageDialog(frameRef, 
							"Error changing look and feel. " + thr.getLocalizedMessage(), 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "Set default look and feel.");
		action.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		actions.put("systemLNF", action);
		
		action = new AbstractAction("Nimrod") {
			public void actionPerformed(ActionEvent event) {
				try {
					NimRODLookAndFeel lf = new NimRODLookAndFeel();
					UIManager.setLookAndFeel(lf);
					SwingUtilities.updateComponentTreeUI(frameRef);
				} catch (Throwable thr) {
					JOptionPane.showMessageDialog(frameRef, 
							"Error changing look and feel. " + thr.getLocalizedMessage(), 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "Set Nimrod look and feel");
		action.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
		actions.put("nimrodLNF", action);
		
		action = new AbstractAction("Substance") {
			public void actionPerformed(ActionEvent event) {
				try {
					SubstanceLookAndFeel lf = 
						new SubstanceBusinessBlueSteelLookAndFeel();
					UIManager.setLookAndFeel(lf);
					SwingUtilities.updateComponentTreeUI(frameRef);
				} catch (Throwable thr) {
					JOptionPane.showMessageDialog(frameRef, 
							"Error changing look and feel. " + thr.getLocalizedMessage(), 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "Set Substance look and feel");
		action.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
		actions.put("substanceLNF", action);
		
		return actions;
	}

	/**
	 * Sets the default font for all Swing components.<br>
	 * example:
	 * setUIFont (new FontUIResource("Serif", Font.ITALIC, 12));
	 * @param f desired font
	 */
	public static void setUIFont(FontUIResource f) {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource)
				UIManager.put(key, f);
		}
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MGCSwingMain().setVisible(true);
				// mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
			}
		});
	}
	
	
	public static void writeOut(String message, 
			boolean errorFlag, MGCSwingMain frameRef) {
		if(errorFlag) {
			System.err.println(message);
			frameRef.output.append("ERROR: " + message + "\n");
		} else {
			System.out.println(message);
			frameRef.output.append(message + "\n");
		}
	}

}
