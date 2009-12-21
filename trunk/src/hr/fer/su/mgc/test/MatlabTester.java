package hr.fer.su.mgc.test;

import hr.fer.su.mgc.matlab.LinuxMatlabEngine;
import hr.fer.su.mgc.matlab.MatlabEngine.MatlabException;

import java.io.IOException;

public class MatlabTester {

	public static void main(String[] args) {

		LinuxMatlabEngine engine = new LinuxMatlabEngine();

		try {
			engine.open();
			String result = engine.evalString("A = gallery('lehmer',10)");
			System.out.println(result);
			result = engine.evalString("A = gallery('lehmer',10)");
			System.out.println(result);
			result = engine.evalString("ewqw");
			System.out.println(result);
			result = engine.evalString("A = gallery('lehmer',2)");
			System.out.println(result);
			engine.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MatlabException e) {
			System.err.println(e.getMessage());
		} finally {
			engine.close();
		}

	}

}
