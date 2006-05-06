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
 File: src/DrawControls/ImageList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Andreas Rossbacher
 *******************************************************************************/

package DrawControls;

import java.util.Vector;
import java.lang.String;
import java.lang.Integer;
import java.io.IOException;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
import javax.microedition.lcdui.game.Sprite;
//#sijapp cond.end#

//! Class for dividing one big image to several small with equal size
/*!
    This class allow you to reduce images number, stored at res folder. 
    It can be uses only if all images have equal height and width. 
    For example, if you want use 10 images with size 16 x 16, you can
    store one 160 x 16 image and divide it with help of this class
    
    \par Example
    \code
    ImageList images = new ImageList();
    images.load("/big160x16.png", 16);
    
    // now you can retrive second image: 
    Image img1 = images.elementAt(1);
    
    \endcode
*/

public class ImageList
{
	private Vector items = new Vector();

	private int width = 0, height = 0;

	//! Return image by index
	public Image elementAt(int index //!< Index of requested image in the list
	)
	{
		return (Image) items.elementAt(index);
	}

	public void setImage(Image image, int index)
	{
		items.setElementAt(Image.createImage(image), index);
	}

	//! Return number of stored images
	public int size()
	{
		return items.size();
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

	//! Remove all images from list
	public void removeAllElements()
	{
		items.removeAllElements();
	}

	//! Load and divide big image to several small and store it in object
	public void load
	(
		String resName, //!< Name of image in resouce
		int width,      //!< Width of result images
		int height,     //!< Height of result images
		int count) throws IOException
	{
		Image resImage = Image.createImage(resName);
		int imgHeight = resImage.getHeight();
		int imgWidth = resImage.getWidth();

		if (width == -1) width = imgHeight;
		if (height == -1) height = imgHeight;

		this.width = width;
		this.height = height;

		for (int y = 0; y < imgHeight; y += height)
		{
			for (int x = 0; x < imgWidth; x += width)
			{
				Image newImage;
				// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
				newImage = Image
						.createImage(Image
								.createImage(resImage, x, y, width, height, Sprite.TRANS_NONE));
				// #sijapp cond.else#
				newImage = Image.createImage(width, height);
				newImage.getGraphics().drawImage(resImage, -x, -y, Graphics.TOP
						| Graphics.LEFT);
				// #sijapp cond.end#
				Image imImage = Image.createImage(newImage);
				items.addElement(imImage);
			}
		}
	}

	public void load(String firstLine, String extention, int from, int to)
			throws IOException
	{
		Image image = null;

		for (int i = from; i <= to; i++)
		{
			image = Image.createImage(firstLine + Integer.toString(i) + "."
					+ extention);
			items.addElement(image);
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
	}
}