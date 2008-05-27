/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-07  Jimm Project

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
 File: src/DrawControls/ImageList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Andreas Rossbacher
 *******************************************************************************/

package DrawControls;

import java.util.Vector;
import java.lang.String;
import java.lang.Integer;
import java.io.IOException;

import javax.microedition.lcdui.*;
//#sijapp cond.if target="MIDP2" | target="MOTOROLA" | target="SIEMENS2" | target="RIM"#
import javax.microedition.lcdui.game.Sprite;
//#sijapp cond.end#

public class ImageList
{
	private Image[] items;

	private int width = 0, height = 0;

	//! Return image by index
	public Image elementAt(int index)
	{
		if (items == null) return null;
		return (index < 0 || index >= items.length) ? null : items[index];
	}

	public void setImage(Image image, int index)
	{
		items[index] = image;
	}

	//! Return number of stored images
	public int size()
	{
		return (items == null) ? 0 : items.length;
	}

	//! Return width of each image
	public int getWidth()
	{
		return width;
	}

	//! Return hright of each image
	public int getHeight()
	{
		return height;
	}
	
	public Image[] getImages()
	{
		return items;
	}

	//! Remove all images from list
	public void removeAllElements()
	{
		items = null;
	}

	//! Load and divide big image to several small and store it in object
	public void load(String resName, //!< Name of image in resouce
		int width, //!< Width of result images
		int height, //!< Height of result images
		int count) throws IOException
	{
		Image resImage = Image.createImage(resName);
		int imgHeight = resImage.getHeight();
		int imgWidth = resImage.getWidth();
		Vector images = new Vector(); 

		if (width == -1) width = imgHeight;
		if (height == -1) height = imgHeight;

		this.width = width;
		this.height = height;

		for (int y = 0; y < imgHeight; y += height)
		{
			for (int x = 0; x < imgWidth; x += width)
			{
				Image newImage;
//#sijapp cond.if target="MIDP2" | target="MOTOROLA" | target="SIEMENS2" | target="RIM"#
				newImage = Image.createImage(Image.createImage(resImage, x, y, width, height, Sprite.TRANS_NONE));
//#sijapp cond.else#
				newImage = Image.createImage(width, height);
				newImage.getGraphics().drawImage(resImage, -x, -y, Graphics.TOP| Graphics.LEFT);
//#sijapp cond.end#
				Image imImage = Image.createImage(newImage);
				images.addElement(imImage);
			}
		}
		items = new Image[images.size()];
		images.copyInto(items);
	}

	public void load(String firstLine, String extention, int from, int to) throws IOException
	{
		items = null;
		Image image = null;
		Vector images = new Vector();

		for (int i = from; i <= to; i++)
		{
			image = Image.createImage(firstLine + Integer.toString(i) + "." + extention);
			images.addElement(image);
		}
		if (image != null)
		{
			height = image.getHeight();
			width = image.getWidth();
		}
		else
		{
			height = width = 0;
		}
		items = new Image[images.size()];
		images.copyInto(items);
	}
}