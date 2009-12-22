package hr.fer.su.mgc.test;

import hr.fer.su.mgc.Config;
import hr.fer.su.mgc.matlab.SmartLinuxMatlabEngine;
import hr.fer.su.mgc.matlab.MatlabEngine.MatlabException;

import java.io.File;
import java.io.IOException;

public class MatlabSmartTester {
	
	public static void main(String[] args) throws IOException, MatlabException, Exception {
		
		Config.init();
		
		SmartLinuxMatlabEngine engine = new SmartLinuxMatlabEngine("./matlab");
		
		engine.open();
		
		for(double var : engine.extractSongFeatures(new File("./dataset/jazz/jazz.000001"))) {
			System.out.println(var);
		}
		
		engine.close();
		
	}

}
