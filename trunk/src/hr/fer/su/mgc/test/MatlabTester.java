package hr.fer.su.mgc.test;

import java.io.IOException;

import hr.fer.su.mgc.matlab.MatlabEngine;
import hr.fer.su.mgc.matlab.MatlabEngine.MatlabException;

public class MatlabTester {

	public static void main(String[] args) {

		MatlabEngine engine = new MatlabEngine();

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
