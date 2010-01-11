package hr.fer.su.mgc.classifier.testing;

import hr.fer.su.mgc.classifier.ClassifierAdapter;
import hr.fer.su.mgc.classifier.ClassifierConstants;

import java.io.File;
import java.util.List;

import weka.classifiers.Evaluation;

public class TestingClassifiers {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		//žanrovi:
		String[] genres = new String[]{"blues","classical","country",
				"disco","hiphop","jazz","metal","pop","reggae","rock"};
		
		
		//podesiti putanju do uzoraka
		File samples = new File("data/allFeatures.arff");
		
		ClassifierAdapter smo = new ClassifierAdapter(ClassifierConstants.LogitBoost);
		smo.setTrainData(samples);
		Evaluation eval = smo.buildModel(null); // 10-fold krosvalidacija
		//ispisuje općenite statistike testiranja
		System.out.println(eval.toSummaryString());
		
		File unlabeled = new File("D:/Data/testUnlabeled.arff");
		List<double[]> res = smo.classifyInstances(unlabeled);
		System.out.println("\nPerforming instance test:");
		for (double[] df: res) {
			double max = 0;
			int ind = -1;
			for(int i = 0; i < df.length; ++i){
				System.out.printf("%.3f",(float)df[i]);
				System.out.print(" ");
				if(max < df[i]) {
					max = df[i];
					ind = i;
				}
			}
			System.out.println(" -> "+genres[ind]);
		}
	}

}
