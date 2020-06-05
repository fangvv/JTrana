package src.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class MYFileFilter extends FileFilter
{
    public String ext;
    
    public MYFileFilter(String ext)
    {
        this.ext = ext;
    }
    
    public boolean accept(File file)
    {
        if (file.isDirectory())
            return true;
        
        String fileName = file.getName();
        int index = fileName.lastIndexOf('.');

        if (index > 0 && index < fileName.length()-1) {
            String extension = fileName.substring(index+1).toLowerCase();
            if (extension.equals(ext))
                return true;
        }
        return false;
    }
    
    public String getDescription(){
        if (ext.equals("tr"))
            return "Trace File (*.tr)";
        if (ext.equals("txt"))
        	return "Text File (*.txt)";
        if (ext.equals("doc"))
        	return "Doc File (*.doc)";
        if (ext.equals("png"))
        	return "Image File (*.png)";
        if (ext.equals("jpeg"))
        	return "Image File (*.jpeg)";
        if (ext.equals("sql"))
        	return "SQL Script (*.sql)";
        return "";
    }
}