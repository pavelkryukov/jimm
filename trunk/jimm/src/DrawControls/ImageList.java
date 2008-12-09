/*******************************************************************************
 Library of additional graphical screens for J2ME applications
 Copyright (C) 2003-08  Jimm Project

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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

//#sijapp cond.if target!="DEFAULT"#
import javax.microedition.lcdui.game.Sprite;
//#sijapp cond.end#

public class ImageList
{
	private Image[] items;
	private int scale = -1;
	static private boolean useAlpha;

	private int width = 0, height = 0;

	//! Return image by index
	public Image elementAt(int index)
	{
		if (items == null) return null;
		return (index < 0 || index >= items.length) ? null : items[index];
	}
	
	public void setScale(int value)
	{
		scale = value;
	}
	
	static public void setUseAlpha(boolean value)
	{
		useAlpha = value;
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

	//! Return height of each image
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
	
//#sijapp cond.if target="MIDP2"#
	private static Image fixAlphaChannel(Image image)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		int len = height*width;
		int[] argbData = new int[len];  
		image.getRGB(argbData, 0, width, 0, 0, width, height);
		
		int transpColor = -1;
		for (int x = 0; x < width; x++) 
			if ((argbData[x]&0xFF000000) == 0x00000000)
			{
				transpColor = argbData[x]&0x00FFFFFF;
				break;
			}
		if (transpColor == -1 || transpColor == 0 || transpColor == 0x00FFFFFF) return image;
		
		for (int i = 0; i < len; i++) 
			if ((argbData[i]&0x00FFFFFF) == transpColor) argbData[i] = transpColor;
		
		return Image.createRGBImage(argbData, width, height, true);
	}
//#sijapp cond.end#

	//! Load and divide big image to several small and store it in object
	public void load
	(
		String resName, //!< Name of image in resouce
		int width, //!< Width of result images
		int height, //!< Height of result images
		int count,
		boolean fixAlphaCh
	) throws IOException
	{
		Image resImage = Image.createImage(resName);
		int imgHeight = resImage.getHeight();
		int imgWidth = resImage.getWidth();
		Vector images = new Vector(); 

		if (width == -1) width = imgHeight;
		if (height == -1) height = imgHeight;

		this.width = width;
		this.height = height;
		
//#sijapp cond.if target!="DEFAULT"#
		int[] imgRgbData = new int[imgHeight*imgWidth];
		resImage.getRGB(imgRgbData, 0, imgWidth, 0, 0, imgWidth, imgHeight);
		resImage = null;
//#sijapp cond.end#		

		Image newImage;
		for (int y = 0; y < imgHeight; y += height)
		{
			for (int x = 0; x < imgWidth; x += width)
			{
//#sijapp cond.if target!="DEFAULT"#
				//if (fixAlphaCh)
				//	newImage = Image.createImage(Image.createImage(resImage, x, y, width, height, Sprite.TRANS_NONE));
				//else
					newImage = cutImage(imgRgbData, x, y, width, height, imgWidth);
//#sijapp cond.else#
				newImage = Image.createImage(width, height);
				newImage.getGraphics().drawImage(resImage, -x, -y, Graphics.TOP| Graphics.LEFT);
				newImage = Image.createImage(newImage); // make image immutable 
//#sijapp cond.end#
				
//#sijapp cond.if target="MIDP2"#
				//if (fixAlphaCh) newImage = fixAlphaChannel(newImage);
				if ((scale != -1) && (scale != 100)) 
					newImage = resizeImage(newImage, scale*newImage.getWidth()/100, scale*newImage.getHeight()/100, useAlpha);
//#sijapp cond.end#
				images.addElement(newImage);
			}
		}
		items = new Image[images.size()];
		images.copyInto(items);
		images = null;
	}

	public void load(String firstLine, String extention, int from, int to) throws IOException
	{
		items = null;
		Image image = null;
		Vector images = new Vector();

		for (int i = from; i <= to; i++)
		{
			image = Image.createImage(firstLine + Integer.toString(i) + "." + extention);
//#sijapp cond.if target="MIDP2"#				
			if ((scale != -1) && (scale != 100))
				image = resizeImage(image, scale*image.getWidth()/100, scale*image.getHeight()/100, useAlpha);
//#sijapp cond.end#
			
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
		images = null;
	}
	
//#sijapp cond.if target!="DEFAULT"#	

	static public Image cutImage(int[] imgRgbData, int x, int y, int width, int height, int origWidth)
	{
		int[] tmp = new int[width*height];
		//img.getRGB(tmp, 0, width, x, y, width, height);
		int bottomY = y+height;
		int dstPtr = 0;
		for (int crd = y; crd < bottomY; crd++)
		{
			int srcPtr = x+crd*origWidth;
			System.arraycopy(imgRgbData, srcPtr, tmp, dstPtr, width);
			dstPtr += width;
		}
		return Image.createRGBImage(tmp, width, height, true);
	}
		
	static public Image resizeImage(Image img, int newWidth, int newHeight, boolean useAlpha)
	{
		int width = img.getWidth();
		int width1 = width-1;
		int height = img.getHeight();
		int height1 = height-1;
		int[] oldImage = new int[width*height];
	
		img.getRGB(oldImage, 0, width, 0, 0, width, height);
		
		int[] newImage = new int[newWidth*newHeight];
	
		int r00=0, g00=0, b00=0, a00=0, r01=0, g01=0, b01=0, a01=0, r10=0, g10=0, b10=0, a10=0, r11=0, g11=0, b11=0, a11=0; 
		int oxPrev = -1, oyPrev = -1;
		int ox = 0;
		int xcnt = 0;
		for (int x = 0; x < newWidth; x++)
		{
			int oy = 0;
			int ycnt = 0;
			for (int y = 0; y < newHeight; y++)
			{
				if ((oxPrev != ox) || (oyPrev != oy))
				{
					int rgb = oldImage[ox+oy*width];
					r00 = rgb & 0xFF;
					g00 = (rgb >> 8) & 0xFF;
					b00 = (rgb >> 16) & 0xFF;
					a00 = (rgb >> 24) & 0xFF;
					if (oy < height1)
					{
						rgb = oldImage[ox+(oy+1)*width];
						r01 = rgb & 0xFF;
						g01 = (rgb >> 8) & 0xFF;
						b01 = (rgb >> 16) & 0xFF;
						a01 = (rgb >> 24) & 0xFF;
					}
					else { r01 = r00; g01 = g00; b01 = b00; a01 = a00; }
					if (ox < width1)
					{
						rgb = oldImage[ox+1+oy*width];
						r10 = rgb & 0xFF;
						g10 = (rgb >> 8) & 0xFF;
						b10 = (rgb >> 16) & 0xFF;
						a10 = (rgb >> 24) & 0xFF;
					}
					else { r10 = r00; g10 = g00; b10 = b00; a10 = a00; }
					if (oy < height1 && ox < width1)
					{
						rgb = oldImage[ox+1+(oy+1)*width];
						r11 = rgb & 0xFF;
						g11 = (rgb >> 8) & 0xFF;
						b11 = (rgb >> 16) & 0xFF;
						a11 = (rgb >> 24) & 0xFF;
					}
					else { r11 = r00; g11 = g00; b11 = b00; a11 = a00; }
					
					oxPrev = ox;
					oyPrev = oy;
				}
				
				int cf1 = (newHeight-ycnt);
				int r1 = r00*cf1/newHeight + r01*ycnt/newHeight;
				int g1 = g00*cf1/newHeight + g01*ycnt/newHeight;
				int b1 = b00*cf1/newHeight + b01*ycnt/newHeight;
				int a1 = a00*cf1/newHeight + a01*ycnt/newHeight;
				int r2 = r10*cf1/newHeight + r11*ycnt/newHeight;
				int g2 = g10*cf1/newHeight + g11*ycnt/newHeight;
				int b2 = b10*cf1/newHeight + b11*ycnt/newHeight;
				int a2 = a10*cf1/newHeight + a11*ycnt/newHeight;
				
				int cf2 = (newWidth-xcnt);
				int r = r1*cf2/newWidth + r2*xcnt/newWidth;
				int g = g1*cf2/newWidth + g2*xcnt/newWidth;
				int b = b1*cf2/newWidth + b2*xcnt/newWidth;
				int a = a1*cf2/newWidth + a2*xcnt/newWidth;
				
				if (r > 255) r = 255;
				if (r < 0) r = 0;
				if (g > 255) g = 255;
				if (g < 0) g = 0;
				if (b > 255) b = 255;
				if (b < 0) b = 0;
				if (a > 255) a = 255;
				if (a < 0) a = 0;
				
				if (!useAlpha) a = (a < 64) ? 0 : 255;
				
				newImage[x+y*newWidth] = r | (g << 8) | (b << 16) | (a << 24);
				
				ycnt += height;
				if (ycnt > newHeight) { oy++; ycnt -= newHeight; }
			}
			
			xcnt += width;
			if (xcnt >= newWidth) { ox++; xcnt -= newWidth; }
		}

		return Image.createRGBImage(newImage, newWidth, newHeight, true);
	}
//#sijapp cond.end#	
}