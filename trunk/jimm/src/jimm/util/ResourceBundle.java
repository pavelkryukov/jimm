/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-05  Jimm Project

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
 File: src/jimm/util/ResourceBundle.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Artyomov Denis
 *******************************************************************************/

package jimm.util;

import java.util.Hashtable;
import java.io.InputStream;
import java.io.DataInputStream;

public class ResourceBundle
{
	// List of available language packs
	public static String[] langAvailable;
	
	static
	{
		try
		{
			InputStream istream = new Object().getClass().getResourceAsStream("/langlist.lng");
			DataInputStream dos = new DataInputStream(istream);
			int size = dos.readShort();
			langAvailable = new String[size];
			for (int i = 0; i < size; i++) langAvailable[i] = dos.readUTF();
			istream.close();
		} catch (Exception e)
		{ }
	}

	// Current language
	private static String currUiLanguage = ResourceBundle.langAvailable[0];

	// Get user interface language/localization for current session
	public static String getCurrUiLanguage()
	{
		return (new String(ResourceBundle.currUiLanguage));
	}


	// Set user interface language/localization for current session
	public static void setCurrUiLanguage(String currUiLanguage)
	{
		if (ResourceBundle.currUiLanguage.equals(currUiLanguage)) return;
		for (int i = 0; i < ResourceBundle.langAvailable.length; i++)
		{
			if (ResourceBundle.langAvailable[i].equals(currUiLanguage))
			{
				ResourceBundle.currUiLanguage = new String(currUiLanguage);
				loadLang();
				return;
			}
		}
	}
	
	static private void loadLang()
	{
		InputStream istream;
		
		try
		{
			resources = new Hashtable();
			istream = resources.getClass().getResourceAsStream("/"+ResourceBundle.currUiLanguage+".lng");
			DataInputStream dos = new DataInputStream(istream);
			int size = dos.readShort();
			for (int j = 0; j < size; j++) resources.put(dos.readUTF(), dos.readUTF());
			istream.close();
		} 
		catch (Exception e) { }
	}

	// Get string from active language pack
	public static synchronized String getString(String key)
	{
		if (resources == null) loadLang();
		String value = (String) resources.get(key);
		if (value != null)
		{
			return (value);
		}
		else
		{
			return (key);
		}
	}

	// Resource hashtable
	static private Hashtable resources = null;
	
	final static public int FLAG_ELLIPSIS = 1 << 0;  
	
	public static synchronized String getString(String key, int flags)
	{
		String result = getString(key);
		
		if ((flags&FLAG_ELLIPSIS) != 0) result = result+"..."; 
		
		return result;
	}
	
}

