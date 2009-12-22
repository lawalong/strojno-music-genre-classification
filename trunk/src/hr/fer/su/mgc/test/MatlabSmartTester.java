package hr.fer.su.mgc.test;

import hr.fer.su.mgc.Config;
import hr.fer.su.mgc.matlab.MatlabException;
import hr.fer.su.mgc.matlab.SmartMatlabEngine;

import java.io.File;

public class MatlabSmartTester {
	
	public static void main(String[] args) throws Exception, MatlabException {
		
		Config.init();
		
		SmartMatlabEngine engine = SmartMatlabEngine.getInstance("./matlab");
		
		engine.open();
		
		for(double var : engine.extractSongFeatures(new File("dataset/jazz/jazz.00001.au"))) {
			System.out.println(var);
		}
		
		engine.close();
		
	}

}
