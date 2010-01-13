package hr.fer.su.mgc.matlab;

import java.io.File;
import java.io.IOException;

public abstract class MatlabEngine {
	
	protected File matlabWorkDir;
	
	protected Process matlabProcess;
	
	public static MatlabEngine getInstance(String matlabWorkDir) throws UnsupportedOSException {
		
		// Check for os type...
		String osType = System.getProperty("os.name");
		if(osType.startsWith("Linux"))
			return new LinuxMatlabEngine(matlabWorkDir);
//		else if(osType.startsWith("Windows"))
//			return new WindowsMatlabEngine(matlabWorkDir);
		else throw new UnsupportedOSException(
				osType + " not supported. Only Linux is currently supported.");
	}
	
	public MatlabEngine(String matlabStartDir) {
		this.matlabWorkDir = new File(matlabStartDir);
	}
	
	public abstract void open() throws Exception, IOException, MatlabException;
	
	/**
	 * @return true if matlab engine is open.
	 */
	public abstract boolean started();
	
	public abstract String evalString(String str) throws Exception, IOException, MatlabException;
	
	public abstract void close();
	
	
	public static class UnsupportedOSException extends Exception {
		private static final long serialVersionUID = -470933512761582862L;
		
		public UnsupportedOSException() {
			super();
		}
		
		public UnsupportedOSException(String errorMessage) {
			super(errorMessage);
		}
		
	}


}
