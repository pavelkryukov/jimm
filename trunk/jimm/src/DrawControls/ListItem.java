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
 File: src/DrawControls/ListItem.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/


package DrawControls;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;

//! Data for list item
/*! All members of class are made as public 
    in order to easy access. 
 */
public class ListItem
{
	public String text; //!< Text of node

	Image image; // Used for TextList in SEL_NONE mode

	private int imageWidth, imageHeigth;

	public int fontStyle, //!< Font style
			color,        //!< Color of node text
			imageIndex;   //!< Index of node image. Must be -1 for disabling image

	ListItem()
	{
		color = imageIndex = 0;
		fontStyle = Font.STYLE_PLAIN;
	}

	ListItem(String text, int color, int imageIndex, int fontStyle)
	{
		this.text = text;
		this.color = color;
		this.imageIndex = imageIndex;
		this.fontStyle = fontStyle;
	}

	ListItem(Image image, String text, int imageWidth, int imageHeigth)
	{
		this.image = image;
		this.text = text;
		this.imageWidth = imageWidth;
		this.imageHeigth = imageHeigth;
	}

	//! Set all member to default values
	public void clear()
	{
		text = "";
		image = null;
		color = 0;
		imageIndex = -1;
	}

	//! Copy data of class to another object
	public void assignTo(ListItem dest //!< Destination object to copy data
	)
	{
		dest.text = text;
		dest.color = color;
		dest.imageIndex = imageIndex;
		dest.fontStyle = fontStyle;
		dest.imageWidth = imageWidth;
		dest.imageHeigth = imageHeigth;
	}

	int getHeight(int fontSize)
	{
		if (image != null) return imageHeigth;
		if (text == null) return 0;
		Font font = Font.getFont(Font.FACE_SYSTEM, fontStyle, fontSize);
		return font.getHeight();
	}

	int getWidth(int fontSize)
	{
		if (image != null) return imageWidth;
		if (text == null) return 0;
		Font font = Font.getFont(Font.FACE_SYSTEM, fontStyle, fontSize);
		return font.stringWidth(text);
	}
}