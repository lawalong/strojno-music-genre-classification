package hr.fer.su.mgc.utils;

public class MathUtils {
	
	public static double max(double[] array) throws Exception {
		if(array == null || array.length == 0)
			throw new Exception("Array is null or empty.");
		double max = array[0]; int i;
		for(i = 1; i < array.length; i++)
			if(max < array[i]) max = array[i];
		return max;
	}

}
