package hr.fer.su.mgc.matlab;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

public class SmartLinuxMatlabEngine extends SmartMatlabEngine {
	
	private MatlabEngine engine;
	
	public SmartLinuxMatlabEngine(String matlabWorkDir) {
		super(matlabWorkDir);
		engine = new LinuxMatlabEngine(matlabWorkDir);
	}
	
	public void open() throws Exception, IOException, MatlabException {
		engine.open();
	}
	
	
	public boolean started() {
		return engine.started();
	}
	
	public double[] extractSongFeatures(File song) throws Exception, MatlabException {
		if(!song.exists()) 
			throw new Exception(song.getAbsolutePath() + " does not exist!");
		StringTokenizer st = 
			new StringTokenizer(engine.evalString(
					"disp(num2str(exSongFeatures('" + song.getAbsolutePath() + "')))"), " ");
		
		double[] result = new double[st.countTokens()];
		int counter = 0;
		while(st.hasMoreTokens()) result[counter++] = Double.valueOf(st.nextToken());
		
		return result;
	}
	
	
	public void close() {
		engine.close();
	}
	
	
	public File runScript(String scriptName, String[] args,
				String outputFileExtension) throws Exception {
		
		genTmpFile(outputFileExtension);
		
		String command = "";
		
		command += "temp_file = fopen('" + tmpDataFile.getAbsolutePath() + "', 'w'); ";
		command += scriptName + "(temp_file";
		for(String arg : args) command += ", " + arg;
		command += "); ";
		command += "fclose(temp_file); ";
		
		engine.evalString(command);
		
		return tmpDataFile;
	}

}
