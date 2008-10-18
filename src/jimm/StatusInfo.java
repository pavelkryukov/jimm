/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-08  Jimm Project

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
 File: src/jimm/StatusInfo.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/
package jimm;

import javax.microedition.lcdui.Image;

public class StatusInfo
{
	public static final int TYPE_STATUS   = 1;
	public static final int TYPE_X_STATUS = 2;
	
	public static final int FLAG_IN_MENU    = 1 << 0;
	public static final int FLAG_HAVE_DESCR = 1 << 1;
	public static final int FLAG_STD        = 1 << 2;
	
	private int     type;
	private int     value;
	private String  text;
	private Image   image;
	private int     flags;
	
	StatusInfo(int type, int value, String text, Image image, int flags)
	{
		this.type  = type;
		this.value = value;
		this.text  = text;
		this.image = image;
		this.flags = flags;
	}
	
	public int getType() 
	{
		return type; 
	}

	public int getValue()
	{
		return value;
	}

	public String getText()
	{
		return text;
	}

	public Image getImage()
	{
		return image;
	}

	public int getFlags()
	{
		return flags;
	}
	
	public boolean testFlag(int mask)
	{
		return (flags&mask) != 0;
	}

}
