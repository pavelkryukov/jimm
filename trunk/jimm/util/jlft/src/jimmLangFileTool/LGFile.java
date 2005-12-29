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
 File: src/jimmLangFileTool/LGFile.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimmLangFileTool;

import java.util.Vector;

public class LGFile extends Vector
{

	
	private static final long serialVersionUID = 1L;
	private String name;


	public LGFile(String _name)
	{
		super();
		name = _name;
	}
	
	public boolean containsGroup(String key)
	{
		boolean value = false;
		
		for(int i=0;i<super.size();i++)
		{
			if(super.get(i) instanceof LGFileSubset)
				if(((LGFileSubset)super.get(i)).getId().equals(key))
					value = true;
		}
		return value;
	}
	
	public void printContent()
	{
		LGFileSubset subset;
		LGString lgs;
		for(int i=0;i<super.size();i++)
		{
			subset = (LGFileSubset)super.get(i);
			System.out.println(subset.getId());
			for(int j=0;j<subset.size();j++)
			{
				lgs = (LGString)subset.get(j);
				if(lgs.isTranslated() == LGString.NOT_TRANSLATED || lgs.isTranslated() == LGString.NOT_IN_BASE_FILE)
					System.out.println(lgs.toString());
			}
				
		}
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return Returns the entrysize.
	 */
	public int getEntrysize()
	{
		int entries = super.size();
		for(int i=0;i<super.size();i++)
		{
			entries += ((LGFileSubset)super.get(i)).size();
		}
		return entries;
	}
}
