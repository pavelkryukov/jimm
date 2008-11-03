/*******************************************************************************
 JimmLangFileTool - Simple Java GUI for editing/comparing Jimm language files
 Copyright (C) 2005  Jimm Project

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 ********************************************************************************
 File: src/jimmLangFileTool/JimmLangFileTool.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimmLangFileTool;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JOptionPane;


public class JimmLangFileTool
{

	public static final String VERSION = "0.5";

	public static boolean DEBUG = false;

	// Variables
	public static String baseLANGFile = "src/lng/EN.lang";
	
	// The both files we wound like to compare
	private LGFile base, compare;
	
	public JimmLangFileTool()
	{
		if(new File(baseLANGFile).exists()){
			base = new LGFile(baseLANGFile);
		}
	}

	
	/**
	 * @return Returns the base.
	 */
	public LGFile getBase()
	{
		return base;
	}

	
	/**
	 * @return Returns the compare.
	 */
	public LGFile getCompare()
	{
		return compare;
	}

	
	/**
	 * @param base The base to set.
	 */
	public void setBase(LGFile base)
	{
		this.base = base;
	}


	/**
	 * @param compare The compare to set.
	 */
	public void setCompare(LGFile compare)
	{
		this.compare = compare;
	}


	public void compare()
	{
		if (base != null && compare != null) {
			LGString lgs_base, lgs_compare;
			LGFileSubset comp_group;
			for (int i = 0; i < base.size(); i++) {
				// A hole subset is missing in the compare file, add subset from
				// base file
				comp_group = compare.containsGroup(base.get(i).getId());
				if (comp_group == null) {
					if(JimmLangFileTool.DEBUG){
						System.out.println(base.get(i).getId());
					}
					LGFileSubset temp = (base.get(i)).getClone();
					for (int j = 0; j < temp.size(); j++) {
						temp.get(j).setTranslated(LGString.NOT_TRANSLATED);
					}
					compare.add(i, temp);
				}
				// Only a few items are missing, find out which, add and tag them
				else {
					for (int k = 0; k < (base.get(i)).size(); k++) {
						lgs_base = base.get(i).get(k);
						lgs_base.setTranslated(LGString.TRANSLATED);
						Vector<LGString> lgs_compareVector = comp_group.containsKey(lgs_base.getKey());
						lgs_compare = lgs_compareVector.size() > 0 ? lgs_compareVector.elementAt(0) : null;
						if (lgs_compare == null) {
							lgs_compare = lgs_base.getClone();
							lgs_compare.setTranslated(LGString.NOT_TRANSLATED);
							if(k>comp_group.size()-1){
								comp_group.add(lgs_compare);
							}
							else {
								comp_group.add(k, lgs_compare);
							}
						} else if (lgs_compare.getTranslated() != LGString.NOT_TRANSLATED) {
							lgs_compare.setTranslated(LGString.TRANSLATED);
						}
					}
				}
			}
			// Check if all Subsets form compare are in base
			for(int i=0;i<compare.size();i++){
				if(base.containsGroup(compare.get(i).getId()) == null){
					compare.get(i).setNotInBase(true);
				}
			}
		}
	}


	static public void main(String[] argv)
	{
		JimmLangFileTool tool = new JimmLangFileTool();
		GUI ui = new GUI(tool);
		try
		{
			LGFile lLgFile = LGFile.load((argv.length > 0 && argv[0] != null) ? argv[0] : baseLANGFile);
			Hashtable<String,LGString> lDuplicates = lLgFile.checkForDuplicates();
			if(lDuplicates.size() > 0){
				String lDupString = "";
				Enumeration<String> lDupKeys = lDuplicates.keys();
				while(lDupKeys.hasMoreElements()){
					lDupString+=lDuplicates.get(lDupKeys.nextElement()).getKey()+"\n";
				}
				JOptionPane.showMessageDialog(ui, "Duplicate found in base file!\n"+lDupString+"\n Please remve these from the base file before continuing! ", "Error", JOptionPane.ERROR_MESSAGE);
			}
			else {
				tool.setBase(lLgFile);
			}
		} catch (Exception e)
		{
			JOptionPane.showMessageDialog(ui, "Error loading the file", "Error", JOptionPane.ERROR_MESSAGE);
		}
		tool.compare();
		ui.initialize();
	}

}
