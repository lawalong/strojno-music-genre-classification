package hr.fer.su.mgc.classifier;

/**
 * Razred s popisom klasifikatora. Brojčana konstanta 
 * identificira korišteni klasifikator.
 * 
 * @author Zoki
 *
 */
public class ClassifierConstants {
	/**
	 * LogitBoost.
	 * Aditivna logistička regresija - boostanje kratkih stabala odluka
	 * temeljenih na metodi najmanjih kvadrata pogreški.
	 * 
	 */
	public static final int LogitBoost = 1;
	
	/**
	 * Sequential Minimal Optimizaition.
	 * Koristi SMO za učenje potpornog stroja, klasifikacija se radi pomoću
	 * više 1-na-1 klasifikacija. Samostalno normalizira podatke.
	 */
	public static final int SMO = 2;
	
	/**
	 * RotationForest.
	 * Bazni klasifikator je J48 stablo odluke.
	 * Provodi analizu glavnih komponenti.
	 */
	public static final int RotationForest = 3;
	
	/**
	 * Višeslojni perceptron.
	 * Provodi propagaciju uatrag i na taj način podešava težine mreže.
	 * Broj slojeva u skrivenom sloju ovisi o broju atributa i razreda.
	 */
	public static final int MultilayerPerceptron = 4;
}
