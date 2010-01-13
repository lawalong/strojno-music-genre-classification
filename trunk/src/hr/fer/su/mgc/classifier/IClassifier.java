package hr.fer.su.mgc.classifier;

import hr.fer.su.mgc.classifier.exceptions.DataNotFoundException;

import java.io.File;
import java.util.List;

import weka.classifiers.Evaluation;

/** 
 * Sučelje klasifikatora.
 * U stvari predstavlja različite adaptere prema Weki,
 * svaki bivajući jedan od klasifikatora.
 * 
 * @author Zoki
 *
 */
public interface IClassifier {
	
	/**
	 * Postavlja podatke za učenje. 
	 * Ukoliko se želi napraviti krovalidacija, potrebno je postaviti 
	 * zastavicu pomoću enableCrossvalidation().
	 * 
	 * @param dataFile pokazivač na file tipa .arff (standardni weka ulaz)
	 * @throws DataNotFoundException ako uslijede problemi s čitanjem datoteke
	 */
	public void setTrainData(File dataFile) throws DataNotFoundException;
	
	/**
	 * Pokreće postupak učenja.
	 * @return rezultate testiranja (weka.classifiers.Evaluation)
	 * @throws Exception ako nešto pođe po zlu prilikom izgradnje modela
	 */
	public void buildModel() throws Exception;
	
	/**
	 * Vrši klasifikaciju pjesama.
	 * 
	 * @param pokazivač na datoteku s nerazvrstanim pjesmama (značajkama)
	 * @return lista distribucija po žanrovima 
	 * @throws DataNotFoundException ako uslijede problemi s čitanjem datoteke
	 */
	public List<double[]> classifyInstances(File unclassified) throws DataNotFoundException;
	
	public Evaluation crossValidate(int folds) throws Exception;
	
	public Evaluation validate(File testFile) throws Exception;
	
}
