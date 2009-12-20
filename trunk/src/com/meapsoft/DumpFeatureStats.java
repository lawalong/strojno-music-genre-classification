package com.meapsoft;

public class DumpFeatureStats
{

	DumpFeatureStats(String filename)
	{		
		FeatFile fF = new FeatFile(filename);
		try
		{
			fF.readFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		System.out.println(fF.getFeatureStats());		
	}
	
	
	public static void main(String[] args)
	{
		new DumpFeatureStats(args[0]);
	}

}
