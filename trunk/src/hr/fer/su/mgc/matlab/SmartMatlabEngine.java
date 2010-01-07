package hr.fer.su.mgc.matlab;

import java.io.File;
import java.io.IOException;

import hr.fer.su.mgc.matlab.MatlabEngine.UnsupportedOSException;

public abstract class SmartMatlabEngine {

	public static SmartMatlabEngine getInstance(String matlabWorkDir)
			throws UnsupportedOSException {

		// Check for OS type...
		String osType = System.getProperty("os.name");
		if (osType.startsWith("Linux"))
			return new SmartLinuxMatlabEngine(matlabWorkDir);
		else if (osType.startsWith("Windows"))
			return new SmartWindowsMatlabEngine(matlabWorkDir);
		else
			throw new UnsupportedOSException(osType + 
					" not supported. Only Windows and Linux are currently supported.");
	}
	
	public abstract void open() throws Exception, IOException, MatlabException;
	
	public abstract double[] extractSongFeatures(File song) throws Exception, MatlabException;
	
	public abstract File runScript(String scriptName, String[] args) throws Exception;
	
	public abstract void close();

}
