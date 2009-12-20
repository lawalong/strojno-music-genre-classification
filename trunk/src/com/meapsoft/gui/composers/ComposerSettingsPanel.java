package com.meapsoft.gui.composers;

import javax.swing.JPanel;

import com.meapsoft.composers.Composer;
import com.meapsoft.disgraced.GUIUtils;
import com.meapsoft.gui.BasePanel;
import com.meapsoft.gui.ComposePanel;

/**
 * An abstract class for all ComposerPanels. Created to simplify
 * the architecture of the ComposerGUI. I didn't want to have 
 * a huge switch statement inside that GUI to get the settings for
 * each panel, so instead, You must implement an initComposer method 
 *  in each composer panel
 * @author  Mike
 */
public abstract class ComposerSettingsPanel extends JPanel
{
	protected ComposePanel mParentTab = null;		//who is our parent tab?
	protected Composer mComposer = null;			//the composer we will use

	public void setParentTab(ComposePanel panel)
	{
		mParentTab = panel;
	}
	
	public Composer getComposer()
	{
		return mComposer;
	}	
	
    protected String[] browseFile(String extension)
    {
        String names[] = GUIUtils.FileSelector(GUIUtils.OPEN, mParentTab.dataDirectory, 
        		mParentTab.mMainScreen);

        if(!names[1].endsWith(extension))
            GUIUtils.ShowDialog("Please select a ." + extension + " file!", 
                                GUIUtils.MESSAGE, mParentTab.mMainScreen);   
        
        return names;
    }
	
	public abstract int initComposer();
}
