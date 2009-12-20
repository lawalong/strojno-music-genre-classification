/*
 * SoundFileFilter.java
 *
 * Created on October 8, 2007, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.meapsoft.gui.filters;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Mike
 */
public class SoundFileFilter extends FileFilter
{
    
    /** Creates a new instance of SoundFileFilter */
    public SoundFileFilter()
    {
    }

    public boolean accept(File f)
    {
        //if we're a directory, we should show it to navigate
        if(f.isDirectory ())
            return true;
        
        String extension = getExtension(f);
        if(extension != null)
        {
            if (extension.equals("wav") || extension.equals("WAV"))
                    return true;            
        }
        
        //by default, return false
        return false;
    }

    public String getDescription()
    {
        return "Sound Files (*.wav)";
    }

    
    //returns the file extension of a file
    //this should really be part of the File class...
    public String getExtension(File kFile)
    {
        String ext = null;
        String s = kFile.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) 
        {
            ext = s.substring(i+1).toLowerCase();
        }
        
        return ext;
    }
    
}
