package hr.fer.su.mgc.classifier.testing;

import hr.fer.su.mgc.Config;
import hr.fer.su.mgc.classifier.ClassifierAdapter;
import hr.fer.su.mgc.classifier.ClassifierConstants;
import hr.fer.su.mgc.matlab.SmartMatlabEngine;

import java.io.File;

public class TrainTestWithMatlab {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Config.init();
		SmartMatlabEngine engine = SmartMatlabEngine.getInstance("matlab");
		engine.open();
		File samples = engine
				.runScript(
						"extractFeatures",
						new String[] {
								"'../dataset'",
								"'test'",
								"{'blues', 'classical', 'country', 'disco', 'hiphop', 'jazz', 'metal', 'pop', 'reggae', 'rock'}" });
		engine.close();
		ClassifierAdapter smo = new ClassifierAdapter(
				ClassifierConstants.SMO);
		smo.setTrainData(samples);
		smo.buildModel();
	}

}
