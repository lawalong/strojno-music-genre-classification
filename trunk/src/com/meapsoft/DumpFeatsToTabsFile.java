package com.meapsoft;

public class DumpFeatsToTabsFile
{

	public DumpFeatsToTabsFile(String filename)
	{
		try
		{
			FeatFile inFF = new FeatFile(filename);
			inFF.readFile();

			inFF.dumpFeatsToTabsFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	public static void main(String[] args)
	{
		new DumpFeatsToTabsFile(args[0]);
	}

}
