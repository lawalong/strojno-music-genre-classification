package hr.fer.su.mgc.test;

import hr.fer.su.mgc.Config;


public class Tester {

	public static void main(String[] args) throws Exception {
		
		Config.init();
		
		System.out.println(Config.getProperty("MATLAB_ROOT"));

	}

}
