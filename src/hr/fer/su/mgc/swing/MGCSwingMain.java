package hr.fer.su.mgc.swing;

import hr.fer.su.mgc.Config;
import hr.fer.su.mgc.classifier.ClassifierAdapter;
import hr.fer.su.mgc.classifier.ClassifierConstants;
import hr.fer.su.mgc.swing.image.ImageUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.skin.SubstanceBusinessBlueSteelLookAndFeel;

import com.nilo.plaf.nimrod.NimRODLookAndFeel;

public class MGCSwingMain extends JFrame {
	private static final long serialVersionUID = -489245112812832880L;
	
	/** 
	 * Main frame reference..
	 */
	protected static Frame frameRef;
	
	/**
	 * Map that contains used actions.
	 */
	protected Map<String, Action> actions;
	
	/**
	 * Audio file chooser reference.
	 */
	protected JFileChooser fileChooser;
	
	public JFileChooser getFileChooser() {
		return fileChooser;
	}

	/**
	 * PlayerPanel reference.
	 */
	protected PlayerPanel playerPanel;

	public PlayerPanel getPlayerPanel() {
		return playerPanel;
	}
	
	/**
	 * ClassifierLoader referece.
	 */
	protected ClassifierLoader classifierLoader;
	
	protected ClassifierLoader classifierLoaderL;
	
	protected FeatureLoader featureLoader;

	public ClassifierLoader getClassifierLoader() {
		return classifierLoader;
	}
	
	protected JPanel classifierNorth;
	
	protected JPanel learnerNorth;
	
	protected JPanel chartPanel;

	public JTextField inputFieldL;
	
	protected JScrollPane classifierScroll;
	
	protected JScrollPane learnerScroll;

	/**
	 * Output.
	 */
	private JTextArea output;
	
	
	
	// Learning tab components...
	
	protected JPanel classifierBuilder;
	
	
	
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
				System.exit(0);
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
		
		
		initClassifierTab(tabPane);
		
		initLearningTab(tabPane);
		
	}
	
	@SuppressWarnings("serial")
	private void initClassifierTab(JTabbedPane tabPane) {
		final MGCSwingMain mainRef = this;
		
		JPanel classifierPanel = new JPanel(new BorderLayout());
		classifierScroll = new JScrollPane(classifierPanel);
		classifierScroll.getVerticalScrollBar().setUnitIncrement(16);
		tabPane.add("Classifier", classifierScroll);

		classifierNorth = new JPanel();
		classifierNorth.setLayout(new BoxLayout(classifierNorth, BoxLayout.Y_AXIS));
		classifierPanel.add(classifierNorth, BorderLayout.NORTH);

		NamedBorderedPanel classLoaderPanel = new NamedBorderedPanel("Classifier Loader", 16, 8, 8, 8) {
			@Override
			public void init() {
				this.panel.setLayout(new FlowLayout(FlowLayout.LEFT));
				classifierLoader = new ClassifierLoader(mainRef, false, true);
				this.panel.add(classifierLoader);
			}
		};
		
		classifierNorth.add(classLoaderPanel);
		
		playerPanel = new PlayerPanel(this, 8, 8, 8, 8);
		classifierNorth.add(playerPanel);
	}

	@SuppressWarnings("serial")
	private void initLearningTab(JTabbedPane tabPane) { // TODO
		final MGCSwingMain mainRef = this;
		
		JPanel learnerPanel = new JPanel(new BorderLayout());
		learnerScroll = new JScrollPane(learnerPanel);
		learnerScroll.getVerticalScrollBar().setUnitIncrement(16);
		tabPane.add("Learning", learnerScroll);

		learnerNorth = new JPanel();
		learnerNorth.setLayout(new BoxLayout(learnerNorth, BoxLayout.Y_AXIS));
		learnerPanel.add(learnerNorth, BorderLayout.NORTH);

		NamedBorderedPanel panel = new NamedBorderedPanel("Feature Loader", 16, 8, 8, 8) {
			@Override
			public void init() {
				this.panel.setLayout(new FlowLayout(FlowLayout.LEFT));
				featureLoader = new FeatureLoader(mainRef);
				this.panel.add(featureLoader);
			}
		};
		
		learnerNorth.add(panel);
		
		
		classifierBuilder = new NamedBorderedPanel("Classifier Builder", 8, 8, 8, 8) {
			
			protected JComboBox classTypesCombo;
			
			@Override
			public void init() {
				this.panel.setLayout(new BorderLayout());
				
				JPanel panelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
				JPanel panelCenter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
				this.panel.add(panelLeft, BorderLayout.WEST);
				this.panel.add(panelCenter, BorderLayout.CENTER);
				
				panelLeft.add(new JLabel("<html><b>Build Classifier:</b></html>"));
				
				classTypesCombo = new JComboBox(new String[] {"LogitBoost", "SMO"});
				panelLeft.add(classTypesCombo);
				
				Action action = new AbstractAction("Build") {
					public void actionPerformed(ActionEvent event) {
						
						if(!featureLoader.featuresLoaded()) {
							String message = "No loaded features!";
							JOptionPane.showMessageDialog(mainRef, message, 
									"Error building classifier", JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						try {
							String selection = classTypesCombo.getSelectedItem().toString();
							ClassifierAdapter classifier;
							if(selection.equals("LogitBoost")) {
								classifier = new ClassifierAdapter(ClassifierConstants.LogitBoost);
							} else if(selection.equals("SMO")) {
								classifier = new ClassifierAdapter(ClassifierConstants.SMO);
							} else return;
							classifier.setTrainData(featureLoader.getFeatures());
							classifier.buildModel();
							
							// Update Classifier loader...
							classifierLoaderL.loadCustomClassifier(classifier);
							
						} catch (Exception e) {
							String message = "Error building classifier! " + e.getLocalizedMessage();
							JOptionPane.showMessageDialog(mainRef, message, 
									"Error building classifier", JOptionPane.ERROR_MESSAGE);
							mainRef.writeOut(message, true);
							return;
						}
					}
				};
				panelCenter.add(new JButton(action));
			}
		};
		learnerNorth.add(classifierBuilder);
		
		
		NamedBorderedPanel classLoaderPanel = new NamedBorderedPanel("Classifier Loader", 8, 8, 8, 8) {
			@Override
			public void init() {
				this.panel.setLayout(new FlowLayout(FlowLayout.LEFT));
				classifierLoaderL = new ClassifierLoader(mainRef, true, false);
				this.panel.add(classifierLoaderL);
			}
		};
		learnerNorth.add(classLoaderPanel);
		
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
					reInitFileChooser();
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
					reInitFileChooser();
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
					reInitFileChooser();
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
	
	private void reInitFileChooser() {
		fileChooser = new JFileChooser(fileChooser.getCurrentDirectory());
	}
	
	public void writeOut(String message, boolean errorFlag) {
		if(errorFlag) {
			System.err.println(message);
			output.append(message + "\n");
		} else {
			System.out.println(message);
			output.append(message + "\n");
		}
		
		output.setCaretPosition(output.getDocument().getLength());

	}
	
	public void updateCharts(String[] genres, double[] result, int index) {
		if(genres.length != result.length) {
			writeOut("Error generating charts. " +
					"Genres and Result must have equal length.", true);
			return;
		}
		
		int i;
		
		// Create Pie Chart ...
		
		DefaultPieDataset pieDataset = new DefaultPieDataset();
		for(i = 0; i < genres.length; i++)
			pieDataset.setValue(genres[i], result[i]);
		
		JFreeChart chart = ChartFactory.createPieChart(genres[index], pieDataset, true, true, false);
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setCircular(true);
		plot.setBackgroundAlpha(0.1f);
		final JPanel piePanel = new ChartPanel(chart);
		
        // Create Bar Chart ...
		
		DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
		for(i = 0; i < genres.length; i++)
			barDataset.addValue(result[i], genres[i], "");
		
		chart = ChartFactory.createBarChart(
				genres[index], "Žanrovi", "Postotak", barDataset, PlotOrientation.VERTICAL, true, true, false);
		
		chart.getPlot().setBackgroundAlpha(0.1f);
		final JPanel barPanel = new ChartPanel(chart, true);
		
		if(chartPanel != null) classifierNorth.remove(chartPanel);
		
		chartPanel = new NamedBorderedPanel("Classification results", 8, 4, 16, 4) {
			private static final long serialVersionUID = 6228148692395703978L;

			@Override
			public void init() {
				this.panel.setLayout(new BorderLayout());
				JPanel innerPanel = new JPanel(new GridLayout(1, 2, 16, 0));
				innerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
				this.panel.add(innerPanel, BorderLayout.CENTER);

				innerPanel.add(piePanel);
				innerPanel.add(barPanel);
			}
		};
		
		classifierNorth.add(chartPanel);
		
		classifierNorth.revalidate();
		
		classifierScroll.getVerticalScrollBar().setValue(
				classifierScroll.getVerticalScrollBar().getMaximum());
	}
	
	public void removeCharts() {
		if(chartPanel != null) {
			classifierNorth.remove(chartPanel);
			chartPanel = null;
		}
		
		classifierNorth.invalidate();
		classifierNorth.repaint();
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
	
	
	public static void main(String[] args) throws IOException {
		Config.init();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				new MGCSwingMain().setVisible(true);
				// mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
			}
		});
	}

}
