package hr.fer.su.mgc.classifier;

import hr.fer.su.mgc.classifier.exceptions.DataNotFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.LogitBoost;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;

public class ClassifierAdapter implements IClassifier, Serializable {
	private static final long serialVersionUID = 703168465236462289L;
	
	private String[] genres;
	
	public String[] getGenres() {
		return genres;
	}

	public void setGenres(String[] genres) {
		this.genres = genres;
	}

	private Instances trainSet;
	
	private Classifier classifier;
	
	/**
	 * Konstruktor klasifikatora kernel SVM (SMO).
	 * Sequential minimal optimization.
	 *  
	 * @param type vrsta klasifikatora koji se koristi (popis u ClassifierConstants)
	 * @throws Exception ukoliko podešavanje opcija ne uspije.
	 */
	public ClassifierAdapter(Integer type) throws Exception {
		switch(type){
		case ClassifierConstants.LogitBoost: 	classifier = initLogitBoost(); 	break;
		case ClassifierConstants.SMO: 			classifier = initSMO(); 		break;
		
		}
	}
	
	private LogitBoost initLogitBoost() throws Exception {
		LogitBoost lb = new LogitBoost();
		lb.setOptions(Utils.splitOptions(
				"-P 100 -F 0 -R 1 -L -1.7976931348623157E308 -H 1.0 -S 1 " +
				"-I 10 -W weka.classifiers.trees.DecisionStump"));
		return lb;
	}
	
	private SMO initSMO() throws Exception {
		SMO smo = new SMO();
		smo.setOptions(Utils.splitOptions(
				"-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -M -V -1 -W 1 -K \"" +
		"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 2.0\""));
		return smo;
	}
	
	@Override
	public void buildModel() throws Exception {
		if(trainSet == null) throw new DataNotFoundException("Train set was not loaded.");
		classifier.buildClassifier(trainSet);
	}
	
	public Evaluation crossValidate(int folds) throws Exception {
		Evaluation eval = new Evaluation(trainSet);
		eval.crossValidateModel(classifier, trainSet, folds, new Random(1));
		return eval;
	}
	
	public Evaluation validate(File testFile) throws Exception {
		
		Instances testSet;
		try {
			DataSource ds = new DataSource(testFile.getAbsolutePath());
			testSet = ds.getDataSet();
			if (testSet.classIndex() == -1)
				   testSet.setClassIndex(testSet.numAttributes() - 1);
		} catch (Exception e) {
			throw new DataNotFoundException("Test data could not not be read.");
		}
		
		if(testSet == null) throw new DataNotFoundException("Test set cannot be null.");
		Evaluation eval = new Evaluation(trainSet);
		eval.evaluateModel(classifier, testSet);
		return eval;
	}
	

	@Override
	public List<double[]> classifyInstances(File unclassified) throws DataNotFoundException {
		try {
			Instances inst = new Instances(new BufferedReader(
					new FileReader(unclassified)));
			//add new label (class)
			inst.setClassIndex(inst.numAttributes() - 1);
			//classify
			List<double[]> result = new ArrayList<double[]>();
			for (int i = 0; i < inst.numInstances(); i++) {
				   double[] clsLabel = classifier.distributionForInstance(inst.instance(i));
				   result.add(clsLabel);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataNotFoundException("Could not find data for classification.");
		}
		
	}
	
	
	public void setTrainData(File dataFile) throws DataNotFoundException {
		try {
			setTrainData(new DataSource(dataFile.getAbsolutePath()));
		} catch (Exception e) {
			throw new DataNotFoundException("Train data could not not be read.");
		}
	}
	
	public void setTrainData(DataSource dataSource) throws DataNotFoundException {
		try {
			// Update genres...
			String genreString = dataSource.getDataSet().attribute("class").toString();
			genreString = genreString.substring(
					genreString.indexOf('{')+1, genreString.lastIndexOf('}'));
			String[] parts = genreString.split(",");
			for(String part : parts) part = part.trim();
			genres = parts;
			
			trainSet = dataSource.getDataSet();
			if (trainSet.classIndex() == -1)
				   trainSet.setClassIndex(trainSet.numAttributes() - 1);
		} catch (Exception e) {
			throw new DataNotFoundException("Train data could not not be read.");
		}
	}

}
