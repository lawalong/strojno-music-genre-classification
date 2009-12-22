package hr.fer.su.mgc.matlab;

import hr.fer.su.mgc.matlab.MatlabEngine.MatlabException;

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
	
	public double[] extractSongFeatures(File song) throws IOException, MatlabException, Exception {
		StringTokenizer st = 
			new StringTokenizer(engine.evalString("disp(num2str([1:0.1:30]))"), " ");
		
		double[] result = new double[st.countTokens()];
		int counter = 0;
		while(st.hasMoreTokens()) result[counter++] = Double.valueOf(st.nextToken());
		
		return result;
	}
	
	
	public void close() {
		engine.close();
	}

}
