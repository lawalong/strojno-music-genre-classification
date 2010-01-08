/**
 * 
 */
package hr.fer.su.mgc.test;

import hr.fer.su.mgc.Config;
import hr.fer.su.mgc.classifier.ClassifierAdapter;
import hr.fer.su.mgc.classifier.ClassifierConstants;
import hr.fer.su.mgc.features.FeatureExtractor;

import java.io.File;
import java.util.List;

import weka.classifiers.Evaluation;

/**
 * @author Tomek
 */
public class FeatureExtractorTester {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Config.init();
		
		// žanrovi:
		String[] genres = new String[]{"blues","classical","country",
				"disco","hiphop","jazz","metal","pop","reggae","rock"};
		
		FeatureExtractor featureExtractor = new FeatureExtractor(genres);
		
		File songFile = new File("dataset/blues/blues.00036.au");
		
		File song = featureExtractor.extractSongFeatures(new File[] {songFile});
		
		System.out.println("asd");
		
		//podesiti putanju do uzoraka
		File samples = new File("data/allFeatures.arff");
		
		ClassifierAdapter smo = new ClassifierAdapter(ClassifierConstants.LogitBoost);
		smo.setTrainData(samples);
		Evaluation eval = smo.buildModel(2); // 10-fold krosvalidacija
		//ispisuje općenite statistike testiranja
		System.out.println(eval.toSummaryString());
		
		List<double[]> res = smo.classifyInstances(song);
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
