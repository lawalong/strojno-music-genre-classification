package hr.fer.su.mgc;

import hr.fer.su.mgc.swing.MGCSwingMain;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javazoom.jl.decoder.JavaLayerException;

public class MGC {

	/**
	 * Main frame reference...
	 */
	protected static JFrame mainFrameRef;
	

	public static void main(String[] args) throws JavaLayerException, UnsupportedAudioFileException, IOException, InterruptedException {
		
		Config.init();

		loadGUI();
		
	}
	
	
	private static void loadGUI() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainFrameRef = new MGCSwingMain();
				mainFrameRef.setVisible(true);
				// mainFrame.setExtendedState(mainFrame.getExtendedState() |
				// JFrame.MAXIMIZED_BOTH);
			}
		});
	}


}
