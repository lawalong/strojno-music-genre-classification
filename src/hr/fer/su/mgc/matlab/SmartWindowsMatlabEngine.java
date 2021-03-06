package hr.fer.su.mgc.matlab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class SmartWindowsMatlabEngine extends SmartMatlabEngine {
	
	private Process matlabProcess;
	
	public void open() {
		// In Windows implementation we do nothing...	
	}
	
	public boolean started() {
		return true;
	}
	
	
	public SmartWindowsMatlabEngine(String matlabWorkDir) {
		super(matlabWorkDir);
	}
	
	
	public double[] extractSongFeatures(File song) throws IOException, MatlabException, Exception {
		
		genTmpFile();
		
		String command = "";
		
		// Set user path if necessary...
		if(matlabWorkDir != null)
			if(matlabWorkDir.exists())
				command = "cd " + matlabWorkDir.getAbsolutePath() + "; ";
			else throw new Exception("Start dir " + 
					matlabWorkDir.getAbsolutePath() + " does not exist!");
		
		command += "temp_file = fopen('" + tmpDataFile.getAbsolutePath() + "', 'w'); ";
		command += "fprintf(temp_file, '%s', num2str(exSongFeatures('" + song.getAbsolutePath() + "'))); ";
		command += "fclose(temp_file); quit(); ";
		
		matlabProcess = Runtime.getRuntime().exec(
				"matlab -wait -nojvm -automation -r \"" + command + "\"");
		
		while(true) {
			try {
				if(matlabProcess.waitFor() != 0) 
					throw new MatlabException("Some error ocurred in matlab...");
				else break;
			} catch (InterruptedException x) { continue; }
		}
		
		// Read data file...
		BufferedReader tmpReader = new BufferedReader(new FileReader(tmpDataFile));
		String retVal = tmpReader.readLine(); // We expect all in one line...
		
		tmpReader.close();
		if(tmpDataFile.exists()) tmpDataFile.delete();
		
		StringTokenizer st = new StringTokenizer(retVal, " ");
		
		double[] result = new double[st.countTokens()];
		int counter = 0;
		while(st.hasMoreTokens()) result[counter++] = Double.valueOf(st.nextToken());
		
		return result;
	}
	
	
	public void close() {
		// In Windows implementation we do nothing...
	}

	
	public File runScript(String scriptName, String[] args, String outputFileExtension) throws Exception {
		
		genTmpFile(outputFileExtension);
		
		String command = "";
		
		// Set user path if necessary...
		if(matlabWorkDir != null)
			if(matlabWorkDir.exists())
				command = "cd " + matlabWorkDir.getAbsolutePath() + "; ";
			else throw new Exception("Start dir " + 
					matlabWorkDir.getAbsolutePath() + " does not exist!");
		
		command += "temp_file = fopen('" + tmpDataFile.getAbsolutePath() + "', 'w'); ";
		command += scriptName + "(temp_file";
		for(String arg : args) command += ", " + arg;
		command += "); ";
		command += "fclose(temp_file); quit(); ";
		
		matlabProcess = Runtime.getRuntime().exec(
				"matlab -wait -nojvm -automation -r \"" + command + "\"");
		
		while(true) {
			try {
				if(matlabProcess.waitFor() != 0) 
					throw new MatlabException("Some error ocurred in matlab...");
				else break;
			} catch (InterruptedException x) { continue; }
		}
		
		return tmpDataFile;
	}

}
