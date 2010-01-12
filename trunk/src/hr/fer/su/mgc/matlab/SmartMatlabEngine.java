package hr.fer.su.mgc.matlab;

import java.io.File;
import java.io.IOException;

import hr.fer.su.mgc.matlab.MatlabEngine.UnsupportedOSException;

public abstract class SmartMatlabEngine {
	
	protected File matlabWorkDir;
	
	protected int tmpDataCounter;
	
	protected File tmpDataFile;
	
	public SmartMatlabEngine(String matlabWorkDir) {
		this.matlabWorkDir = new File(matlabWorkDir);
		tmpDataCounter = 0;
	}
	
	
	protected void genTmpFile(String outputFileExtension) throws IOException {
		String name = "mgc_temp_" + (++tmpDataCounter) + "." + outputFileExtension;
		tmpDataFile = new File(
				System.getProperty("java.io.tmpdir") + File.separator + name);
	}
	
	protected void genTmpFile() throws IOException {
		genTmpFile("data");
	}
	
	public abstract void open() throws Exception, IOException, MatlabException;
	
	/**
	 * @return true if matlab engine is open.
	 */
	public abstract boolean started();
	
	public abstract double[] extractSongFeatures(File song) throws Exception, MatlabException;
	
	public abstract File runScript(String scriptName, String[] args, String outputFileExtension) throws Exception;
	
	public abstract void close();
	
	public File runScript(String scriptName, String[] args) throws Exception {
		return runScript(scriptName, args, "arff");
	}
	
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

}
