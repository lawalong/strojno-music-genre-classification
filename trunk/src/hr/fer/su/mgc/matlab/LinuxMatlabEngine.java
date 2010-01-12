package hr.fer.su.mgc.matlab;

import hr.fer.su.mgc.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class LinuxMatlabEngine extends MatlabEngine {

	private BufferedReader reader;
	private BufferedWriter writer;
	private BufferedReader errReader;
	private String errorMessage;
	private char[] outputBuffer;
	private StringBuffer outputStringBuffer;
	private static final int BUFFERSIZE = 65536;
	
	private boolean openedflag;

	public LinuxMatlabEngine(String matlabStartDir) {
		super(matlabStartDir);
		outputBuffer = new char[BUFFERSIZE];
		errorMessage = "";
		outputStringBuffer = new StringBuffer();
	}

	public void open() throws Exception, IOException, MatlabException {
		try {
			matlabProcess = Runtime.getRuntime().exec(
					Config.getProperty("MATLAB_ROOT") + "/bin/matlab -nojvm -nosplash");
			reader = new BufferedReader(
				new InputStreamReader(matlabProcess.getInputStream()));
			errReader = new BufferedReader(
				new InputStreamReader(matlabProcess.getErrorStream()));
			writer = new BufferedWriter(
				new OutputStreamWriter(matlabProcess.getOutputStream()));
		
			// Wait for the Matlab process to respond.
			receive();
			
			// Set compact formating...
			evalString("format('compact');");
			
			// Set user path...
			if(matlabWorkDir != null) 
				if(matlabWorkDir.exists())
					evalString("cd " + matlabWorkDir.getAbsolutePath());
				else throw new Exception("Start dir " + 
						matlabWorkDir.getAbsolutePath() + " does not exist!");
			
			openedflag = true;
			
		} catch (IOException ex) {
			System.err.println("ERROR: Matlab could not be opened. " + ex.getMessage());
			throw (ex);
		}
	}
	
	public boolean started() {
		return openedflag;
	}

	private void send(String str) throws IOException {
		try {
			str += "\n";
			writer.write(str, 0, str.length());
			writer.flush();

		} catch (IOException ex) {
			System.err.println("ERROR: IOException occured while sending data to the"
					+ " Matlab process.");
			throw (ex);
		}
	}

	private boolean receive() throws IOException {
		int charsRead = 0; boolean errorOcurred = false;
		char[] errBuffer = new char[outputBuffer.length];
		try {
			while (true) {
				
				charsRead = reader.read(outputBuffer, 0, BUFFERSIZE);
				
				if (charsRead == -1) {
					try {
						while(!reader.ready()) Thread.sleep(100);
					} catch (InterruptedException Ignorable) { }
					continue;
				}
				
				outputStringBuffer.append(outputBuffer, 0, charsRead);
				
				// Any errors?
				if(!errorOcurred) {
					if(errReader.ready()) {
						charsRead = errReader.read(errBuffer, 0, BUFFERSIZE);
						if(charsRead != -1) {
							errorMessage = new String(errBuffer, 0, charsRead);
							errorOcurred = true;
						}
					}
				}
				
				if(outputStringBuffer.lastIndexOf(">> ") != -1) break;
			}
		} catch (IOException ex) {
			System.err.println("ERROR: IOException occured while receiving data from"
					+ " the Matlab process.");
			throw (ex);
		}
		
		return !errorOcurred;
	}
	

	public String evalString(String str) throws IOException, MatlabException {
		send(str);
		if(!receive()) {
			outputStringBuffer = new StringBuffer();
			try {
				throw new MatlabException(errorMessage.substring(4, errorMessage.length()-4));
			} catch (IndexOutOfBoundsException ex) {
				throw new MatlabException(errorMessage);
			}
		}
		
		String result;

		try {
			result = outputStringBuffer.
				substring(0, outputStringBuffer.length()-4); // Remove "\n>> "
		} catch (StringIndexOutOfBoundsException ex) {
			result = outputStringBuffer.
				substring(0, outputStringBuffer.length()-3); // Remove ">> "
		}
		
		outputStringBuffer = new StringBuffer();
		return result;
	}
	
	public void close() {
		try {
			reader.close();
			errReader.close();
			writer.close();
			matlabProcess.destroy();
			openedflag = false;
		} catch (Throwable Ignorable) { }
	}


}
