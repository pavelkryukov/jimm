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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;


public class JimmLangFileTool
{

	// Variables
	public static String baseLANGFile = "src/lng/EN.lang";
	public static String compareLANGFile = "src/lng/DE.LANG";
	
	// The both files we wound like to compare
	private LGFile base, compare;
	
	// Array with error specific comments
	private String error[] = {"Generic errors",
			"Login specific errors",
			"Network communication specific exceptions (first half main connection second half peer)",
			"Parsing specific error",
			"Action errors",
			"Specific action errors",
			"Specific action errors",
			"Other errors",
			"Camera errors",
			"File transfer errors",
			"",
			"",
			"HTTP Connection errors"};

	public JimmLangFileTool()
	{
		base = new LGFile(baseLANGFile);
		compare = new LGFile(compareLANGFile);
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

	
	public void compare()
	{
		LGString lgs_base,lgs_compare;
		
		for(int i=0;i<base.size();i++)
		{
			// A hole subset is missing in the compare file, add subset from base file
			if(!compare.containsGroup(((LGFileSubset)base.get(i)).getId()))
			{
				LGFileSubset temp = ((LGFileSubset)base.get(i)).getClone();
				for(int j=0;j<temp.size();j++)
					((LGString)temp.get(j)).setTranslated(LGString.NOT_TRANSLATED);
				compare.add(i,temp);
			}
			// Only a few items are missing, find out which, add and tag them
			else
			{
				for(int k=0;k<((LGFileSubset)base.get(i)).size();k++)
				{
					lgs_base = (LGString)((LGFileSubset)base.get(i)).get(k);
					lgs_base.setTranslated(LGString.TRANSLATED);
					lgs_compare = ((LGFileSubset)compare.get(i)).containsKey(lgs_base.getKey());
					if(lgs_compare == null)
					{
						lgs_compare = lgs_base.getClone();
						lgs_compare.setTranslated(LGString.NOT_TRANSLATED);
						((LGFileSubset)compare.get(i)).add(k,lgs_compare);
					}
					else
						if(lgs_compare.getTranslated() != LGString.NOT_TRANSLATED)
							lgs_compare.setTranslated(LGString.TRANSLATED);
				}
			}
		}
	}

	
	public void loadBase(String filename) throws Exception
	{
		base = loadLANGFile(filename);
	}
	
	public void loadCompare(String filename) throws Exception
	{
		compare = loadLANGFile(filename);
	}
	
	public void save(String path) throws Exception
	{
		BufferedWriter file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), Charset.forName("UTF-8")));
		file.write("// Labels\n");
		LGFileSubset subset;
		LGString lgs;
		boolean print_end = false;
		for (int i = 0; i < compare.size(); i++)
		{
			subset = (LGFileSubset) compare.get(i);
			if (subset.getId().startsWith("TAR_") && !subset.getId().endsWith("_ELSE"))
			{
				file.write("// " + subset.getId().substring(4, subset.getId().length()) + " target special strings\n");
				file.write("//#sijapp cond.if target is \"" + subset.getId().substring(4, subset.getId().length()) + "\"#\n");
				try
				{
					if (((LGFileSubset) compare.get(i + 1)).getId().endsWith("_ELSE"))
						print_end = false;
					else
						print_end = true;
				} catch (Exception e)
				{
					print_end = true;
				}
			}
			else
				if (subset.getId().startsWith("MOD_") && !subset.getId().endsWith("_ELSE"))
				{
					file.write("// " + subset.getId().substring(4, subset.getId().length()) + " module strings\n");
					file.write("//#sijapp cond.if modules_" + subset.getId().substring(4, subset.getId().length()) + " is \"true\" #\n");
					print_end = true;
				}
				else
					if (subset.getId().endsWith("_ELSE"))
					{
						file.write("//#sijapp cond.else#\n");
						print_end = true;
					}
					else
						file.write("// General strings\n");
			for (int j = 0; j < subset.size(); j++)
			{
				lgs = (LGString) subset.get(j);
				if (lgs.getTranslated() != LGString.REMOVED && lgs.getTranslated() != LGString.NOT_TRANSLATED)
				{
					if (lgs.getKey().startsWith("error_"))
					{
						if (lgs.getKey().endsWith("0")) file.write("\n // " + error[Integer.parseInt(lgs.getKey().substring(6, 8)) - 10] + "\n");
					}
					file.write("\"" + lgs.getKey() + "\"\t");
					for (int k = lgs.getKey().length(); k < 22; k += 4)
						file.write("\t");
					file.write("\"" + lgs.getValue() + "\"\n");
				}
			}
			if (print_end)
			{
				print_end = false;
				file.write("//#sijapp cond.end#\n\n");
			}

		}
		file.close();
	}

	public LGFile loadLANGFile(String filename) throws Exception
	{
		String line;
		String group = null;
		LGFileSubset subset = new LGFileSubset();
		LGFileSubset general = new LGFileSubset("GENERAL");
		String name;
		
		if(filename.lastIndexOf("\\") != -1)
			name = filename.substring(filename.lastIndexOf("\\")+1,filename.length());
		else if(filename.lastIndexOf("/") != -1)
			name = filename.substring(filename.lastIndexOf("/")+1,filename.length());
		else
			name = filename;
			
		LGFile temp = new LGFile(name);
		
			BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(filename),Charset.forName("UTF-8")));
			while (file.ready())
			{
				line = file.readLine();
				if (line.lastIndexOf("sijapp") != -1)
				{
					if (line.lastIndexOf("modules") != -1)
						group = "MOD_" + line.substring(line.lastIndexOf("modules") + 8, line.lastIndexOf("is") - 1);
					else
						if (line.lastIndexOf("\"") != -1) 
							group = "TAR_" + line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));

					if (line.lastIndexOf("cond.else") != -1)
					{
						subset.setId(group);
						temp.add(subset.getClone());
						subset = new LGFileSubset();
						group = group + "_ELSE";
					}
					else
						if (line.lastIndexOf("cond.end") != -1)
						{
							subset.setId(group);
							temp.add(subset.getClone());
							subset = new LGFileSubset();
							group = null;
						}
				}
				else
				{
					if (LGString.parseLine(line) != null)
					{
						if (group == null)
							general.add(LGString.parseLine(line));
						else
							subset.add(LGString.parseLine(line));
					}
				}

			}
			temp.add(general);
			return temp;
	}


	static public void main(String[] argv)
	{
		JimmLangFileTool tool = new JimmLangFileTool();
		GUI ui = new GUI(tool);
		try
		{
			tool.loadBase(baseLANGFile);
			tool.loadCompare(compareLANGFile);
		} catch (Exception e)
		{
			JOptionPane.showMessageDialog(ui, "Error loading the file", "Error", JOptionPane.ERROR_MESSAGE);
		}
		tool.compare();
		ui.initialize();
	}

}
