package com.meapsoft.gui.widgets;

import javax.swing.JCheckBox;

import com.meapsoft.featextractors.FeatureExtractor;

/**
 * A small data structure containing the feature extractor checkbox, the associated feature extractor
 * and its weight value
 *
 * @author Mike Sorvillo (ms3311@columbia.edu)
 * and the MEAP team
 */
public class FeatureExtractorCheckBox extends JCheckBox
{
    ////////////////////////////////////////////////////////////////////////
    // member variables
    ////////////////////////////////////////////////////////////////////////
	private String mName = "";
	private String mDescription = "";
	private float mWeight = 1;
	private FeatureExtractor mExtractor = null;
	
    ////////////////////////////////////////////////////////////////////////
    // getters and setters
    ////////////////////////////////////////////////////////////////////////
	public float getWeight() 
	{
		return mWeight;
	}

	public void setWeight(float weight) 
	{
		mWeight = weight;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public String getDescription() 
	{
		return mDescription;
	}
	
	public FeatureExtractor getExtractor() 
	{
		return mExtractor;
	}
	
    ////////////////////////////////////////////////////////////////////////
    // constructors
    ////////////////////////////////////////////////////////////////////////
	public FeatureExtractorCheckBox(String name)
	{
		//call our parent with our name
		super(name);
		
		//save our name
		mName = name;
		
		//lets create the feature extractor here
		try
		{
			mExtractor = (FeatureExtractor)(Class.forName("com.meapsoft.featextractors." + mName).newInstance());
			
			//get the description
			mDescription = mExtractor.description();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//set the selected to be false
		this.setSelected(false);
	}



	
}
