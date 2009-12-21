package hr.fer.su.mgc.matlab;

import java.io.IOException;

public abstract class MatlabEngine {
	
	protected Process matlabProcess;
	
	public static MatlabEngine getInstance() throws UnsupportedOSException {
		
		// Check for os type...
		String osType = System.getProperty("os.name");
		if(osType.startsWith("Linux"))
			return new LinuxMatlabEngine();
		else if(osType.startsWith("Windows"))
			return new WindowsMatlabEngine();
		else throw new UnsupportedOSException(
				osType + " not supported. Only Linux and Windows are currently supported.");
	}
	
	public MatlabEngine() {

	}
	
	public abstract void open() throws IOException, MatlabException;
	
	public abstract String evalString(String str) throws IOException, MatlabException;
	
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
