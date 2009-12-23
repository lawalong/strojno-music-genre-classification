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
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.FontUIResource;

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
	public JTextField inputField;
	public JSlider playerSlider;

	
	
	public MGCSwingMain() {
		
		frameRef = this;
		
		NimRODLookAndFeel NimRODLF = new NimRODLookAndFeel();

		// NimRODLookAndFeel.setCurrentTheme(SSMUMain.makeNimrodTheme());
		
		try {
			UIManager.setLookAndFeel(NimRODLF);
		} catch (Exception Ignorable) { }
		
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception Ignorable) { }
		
		@SuppressWarnings("unused")
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(200, 200);
		setSize(800, 300);
		setMaximizedBounds(GraphicsEnvironment.
				getLocalGraphicsEnvironment().getMaximumWindowBounds());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Do something...
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
		
		tagLabel = new JLabel();
		timeLabel = new JLabel();
		inputField = new JTextField();
		playerSlider = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
		
		createMenus();
		
		setLayout(new BorderLayout());
		JPanel rootPanel = new JPanel(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		add(rootPanel);
		
		JTabbedPane tabPane = new JTabbedPane();
		rootPanel.add(tabPane, BorderLayout.CENTER);
		JPanel classifierPanel = new JPanel(new BorderLayout());
		JPanel learnersPanel = new JPanel(new BorderLayout());
		tabPane.add("Classifier", classifierPanel);
		tabPane.add("Learning", learnersPanel);
		
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
		
		JPanel browsePanel = new JPanel(new BorderLayout(8, 0));
		browsePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		browsePanel.add(inputField, BorderLayout.CENTER);
		
		Action browseAction = new AbstractAction("Browse") {
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
						inputField.setText(selectedFile.getName());
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
		
		JButton browseButton = new JButton(browseAction);
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
		
		
		// Menus actions...

		Action fileExit = new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		};
		fileExit.putValue(Action.SHORT_DESCRIPTION, "Exit application");
		fileExit.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
		actions.put("Exit", fileExit);
		
		Action helpAbout = new AbstractAction("About MGC") {
			public void actionPerformed(ActionEvent event) {
				// TODO
			}
		};
		helpAbout.putValue(Action.SHORT_DESCRIPTION, "About Music Genre Classifier");
		helpAbout.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		actions.put("helpAbout", helpAbout);
		
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

}
