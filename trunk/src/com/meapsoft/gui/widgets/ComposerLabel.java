package com.meapsoft.gui.widgets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.meapsoft.composers.Composer;
import com.meapsoft.featextractors.FeatureExtractor;
import com.meapsoft.gui.ComposePanel;
import com.meapsoft.gui.GUIUtils;
import com.meapsoft.gui.composers.ComposerSettingsPanel;

/**
 * A small data structure that maps labels to composers and the composer panels
 * Used in the Composer panel
 *
 * @author Mike Sorvillo (ms3311@columbia.edu)
 * and the MEAP team
 */
public class ComposerLabel extends JLabel 
{
    ////////////////////////////////////////////////////////////////////////
    // member variables
    ////////////////////////////////////////////////////////////////////////
	private String mName = "";
	private String mDescription = "";
	private ComposerSettingsPanel mSettingsPanel = null;
	
    ////////////////////////////////////////////////////////////////////////
    // accessors for these member variables
    ////////////////////////////////////////////////////////////////////////
    public String getName() 
    {
		return mName;
	}
    
	public String getDescription() 
	{
		return mDescription;
	}
	
	public ComposerSettingsPanel getSettingsPanel() 
	{
		if (mSettingsPanel == null)
			System.out.println("wtf? why is settings panel == null?");
		return mSettingsPanel;
	}

	////////////////////////////////////////////////////////////////////////
    // constructors
    ////////////////////////////////////////////////////////////////////////
	public ComposerLabel(String className, ComposePanel composeTab)
	{
		//call our parent with our name
		super(className);
		
		//System.out.println("in ComposerLabel className: " + className);
		
		//lots of dynamic instantiation, yay!
		try
		{
			//create the composer itself (with empty constructor)
			Composer comp = (Composer)(Class.forName("com.meapsoft.composers." + className).newInstance());
			
			//save the data here
			mName = comp.name();
			mDescription = comp.description();
			
			//System.out.println("mDescription: " + mDescription);
			
			//set our text here
			this.setText(mName);
			
			//create the panel associated with this composer
			String panelName = "com.meapsoft.gui.composers." + className + "Panel";
			
			try
			{
				//System.out.println("getting: " + panelName);
				//get this class
				Class panelClass = Class.forName(panelName);
				
				//System.out.println("got: " + panelName);
				
				//if we have a class, create a new instance here
				mSettingsPanel = (ComposerSettingsPanel)(panelClass.newInstance());
				
				//System.out.println("created instance of: " + panelName);
				
				//give the settings panel a reference to our parent tab
				mSettingsPanel.setParentTab(composeTab);
				
				//System.out.println("set parent tab for: " + panelName);
			}
			catch(ClassNotFoundException e)
			{
				System.out.println("Something went wrong sniffing out the  class info for: " +
						panelName);
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
