package hr.fer.su.mgc;

import hr.fer.su.mgc.matlab.MatlabException;
import hr.fer.su.mgc.matlab.SmartMatlabEngine;
import hr.fer.su.mgc.swing.MGCSwingMain;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class MGC {
	
	private static SmartMatlabEngine matlabEngine;

	public static SmartMatlabEngine getMatlabEngine() {
		return matlabEngine;
	}
	
	public static void initMatlabEngine() throws IOException, MatlabException, Exception {
		matlabEngine = SmartMatlabEngine.getInstance("matlab");
		
		matlabEngine.open();
		
		// Add shutdown hook...
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if(matlabEngine != null) matlabEngine.close();
			}
		});

	}
	
	public static void closeMatlabEngine() {
		matlabEngine.close();
	}


	/**
	 * Main frame reference...
	 */
	protected static JFrame mainFrameRef;
	

	public static void main(String[] args) throws Exception {
		
		Config.init();
		
		initMatlabEngine();

		loadGUI();
	}
	
	
	private static void loadGUI() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					mainFrameRef = new MGCSwingMain();
					mainFrameRef.setVisible(true);
					// mainFrame.setExtendedState(mainFrame.getExtendedState() |
					// JFrame.MAXIMIZED_BOTH);
				}
			});
		} catch (Throwable Ignorable) { }
	}
}
