package hr.fer.su.mgc.matlab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class WindowsMatlabEngine extends MatlabEngine {
	
	private int tmpDataCounter;
	private File tmpDataFile;
	
	public WindowsMatlabEngine(String matlabStartDir) {
		super(matlabStartDir);
		tmpDataCounter = 0;
	}

	
	public void open() {
		// In Windows implementation we do nothing...	
	}
	
	public String evalString(String str) throws Exception {
		
		genTmpFile();
		
		// str = "fprintf(fopen(" + tmpDataFile + "), num2str([1:30]););";
		
		str = "tmp_file = '" + tmpDataFile.getAbsolutePath() + "'; " + str + ";";
		
		// Set user path if necessary...
		if(matlabWorkDir != null) 
			if(matlabWorkDir.exists())
				str = "cd " + matlabWorkDir.getAbsolutePath() + "; ";
			else throw new Exception("Start dir " + 
					matlabWorkDir.getAbsolutePath() + " does not exist!");
		
		matlabProcess = Runtime.getRuntime().exec(
				"matlab -nojvm -r \"" + str + "\"");
		
		
		while(true) {
			try {
				if(matlabProcess.waitFor() != 0) return "";
				else break;
			} catch (InterruptedException x) { continue; }
		}
		
		BufferedReader tmpReader = new BufferedReader(new FileReader(tmpDataFile));
		String retVal = tmpReader.readLine();
		
		tmpReader.close();
		if(tmpDataFile.exists()) tmpDataFile.delete();
		
		return retVal;
	}
	
	public void close() {
		// In Windows implementation we do nothing...
	}
	
	private void genTmpFile() throws IOException {
		String name = "mgc_temp_" + (++tmpDataCounter) + ".data";
		tmpDataFile = new File(
				System.getProperty("java.io.tmpdir") + File.separator + name);
	}

}
