package hr.fer.su.mgc.matlab;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

public class SmartLinuxMatlabEngine extends SmartMatlabEngine {
	
	private MatlabEngine engine;
	
	public SmartLinuxMatlabEngine(String matlabWorkDir) {
		engine = new LinuxMatlabEngine(matlabWorkDir);
	}
	
	public void open() throws Exception, IOException, MatlabException {
		engine.open();
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

	@Override
	public File runScript(String scriptName, String[] args) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
