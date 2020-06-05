package src.gui;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

public class FileIcon extends FileView
{
    public String getName(File f) {
	    return null; 
             
    }

    public String getDescription(File f) {
	    return null; 
	
    }

    public String getTypeDescription(File f)
    {
        String extension = getExtensionName(f);
        if(extension.equals("tr"))
            return "Trace File";
        if(extension.equals("sql"))
        	return "SQL Script";
        return "";
    }

    public Icon getIcon(File f)
    {
        String extension = getExtensionName(f);
        if(extension.equals("tr"))
            return new ImageIcon("icons/trace.png");
        if(extension.equals("sql"))
        	return new ImageIcon("icons/sql.png");
        return null;
	}

    public Boolean isTraversable(File f) {
    	return null; 
    }
    
    public String getExtensionName(File f)
    {
     	String extension ="";
	    String fileName = f.getName();
        int index = fileName.lastIndexOf('.');

        if (index > 0 && index < fileName.length()-1) {
            extension = fileName.substring(index+1).toLowerCase();
        }
        return extension;
    } 
} 
