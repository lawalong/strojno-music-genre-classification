package hr.fer.su.mgc.matlab;

import java.io.File;
import java.io.IOException;

public abstract class MatlabEngine {
	
	protected File matlabStartDir;
	
	protected Process matlabProcess;
	
	public static MatlabEngine getInstance(String matlabStartDir) throws UnsupportedOSException {
		
		// Check for os type...
		String osType = System.getProperty("os.name");
		if(osType.startsWith("Linux"))
			return new LinuxMatlabEngine(matlabStartDir);
		else if(osType.startsWith("Windows"))
			return new WindowsMatlabEngine(matlabStartDir);
		else throw new UnsupportedOSException(
				osType + " not supported. Only Linux and Windows are currently supported.");
	}
	
	public MatlabEngine(String matlabStartDir) {
		this.matlabStartDir = new File(matlabStartDir);
	}
	
	public abstract void open() throws Exception, IOException, MatlabException;
	
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
	
	public class MatlabException extends Exception {
		private static final long serialVersionUID = 1687200595982655087L;
		
		public MatlabException(String errorMessage) {
			super(errorMessage);
		}
	}


}
