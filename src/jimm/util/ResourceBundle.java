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
Author(s): Manuel Linsmayer
*******************************************************************************/

package jimm.util;

import java.util.Hashtable;
import java.io.InputStream;
import java.io.DataInputStream;

public class ResourceBundle
{
	// List of available language packs
	public static String[] LANG_AVAILABLE =
	{
		// #sijapp cond.if lang_EN is "true" #
		"EN",
		// #sijapp cond.end #
	    // #sijapp cond.if lang_BR is "true" #
		"BR",
		// #sijapp cond.end #
		// #sijapp cond.if lang_BG is "true" #
		"BG",
		// #sijapp cond.end #
		// #sijapp cond.if lang_CZ is "true" #
		"CZ",
		// #sijapp cond.end #
		// #sijapp cond.if lang_DE is "true" #
		"DE",
		// #sijapp cond.end #
		// #sijapp cond.if lang_ES is "true" #
		"ES",
		// #sijapp cond.end #
		// #sijapp cond.if lang_HE is "true" #
		"HE",
		// #sijapp cond.end #
		// #sijapp cond.if lang_IT is "true" #
		"IT",
		// #sijapp cond.end #
		// #sijapp cond.if lang_LT is "true" #
		"LT",
		// #sijapp cond.end #
		// #sijapp cond.if lang_PL is "true" #
		"PL",
		// #sijapp cond.end #
		// #sijapp cond.if lang_RU is "true" #
		"RU",
		// #sijapp cond.end #
		// #sijapp cond.if lang_SE is "true" #
		"SE",
		// #sijapp cond.end #
		// #sijapp cond.if lang_SR is "true" #
		"SR",
		// #sijapp cond.end #
		// #sijapp cond.if lang_UA is "true" #
		"UA",
		// #sijapp cond.end #
	};


	// Current language
	private static String currUiLanguage = ResourceBundle.LANG_AVAILABLE[0];

	// Get user interface language/localization for current session
	public static String getCurrUiLanguage()
	{
		return (new String(ResourceBundle.currUiLanguage));
	}


	// Set user interface language/localization for current session
	public static void setCurrUiLanguage(String currUiLanguage)
	{
		if (ResourceBundle.currUiLanguage.equals(currUiLanguage)) return;
		for (int i = 0; i < ResourceBundle.LANG_AVAILABLE.length; i++)
		{
			if (ResourceBundle.LANG_AVAILABLE[i].equals(currUiLanguage))
			{
				ResourceBundle.currUiLanguage = new String(currUiLanguage);
				loadLang();
				return;
			}
		}
	}
	
	static private void loadLang()
	{
		try
		{
			resources = new Hashtable();
			InputStream istream = resources.getClass().getResourceAsStream("/"+ResourceBundle.currUiLanguage+".lng");
			DataInputStream dos = new DataInputStream(istream);
			int size = dos.readShort();
			for (int j = 0; j < size; j++) resources.put(dos.readUTF(), dos.readUTF());
		} catch (Exception e)
		{ }
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
}

