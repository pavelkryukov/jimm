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
 File: src/DrawControls/RichTextList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

package DrawControls;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

abstract class TitledControl extends Canvas
{
	private String title;
	boolean useStdTitle = false;
	private int currentOffset = 0, lastOffset, bkgrndColor = 0xFFFFFF;
	
	static int defFontHeight = Font.getDefaultFont().getHeight();
	
	public TitledControl(String title)
	{
		super();
		this.title = title; 
	}
	
	abstract protected int getDataHeight();
	abstract protected void paintClient
	(
		Graphics g, 
		int left, 
		int top,
		int right, 
		int bottom,
		int offset
	);
	
	private boolean locked = false;
	
	final public void lock()
	{
		locked = true;
	}
	
	final public void unlock()
	{
		locked = false;
		invalidate();
	}
	
	final protected void invalidate()
	{
		if (locked) return;
		repaint();
	}
	
	final protected int getCapGeight()
	{
		if (title == null) return 0;
		if (title.length() == 0) return 0;
		return defFontHeight+2;
	}
	
	final protected int getScrollerWidth()
	{
		return (defFontHeight/5) | 1;
	}
	
	final private void drawScroller(Graphics g)
	{
		int width = getWidth(),
		    scrWidth = getScrollerWidth(),
		    x1 = width-scrWidth,
			x = (width+x1)/2,
			y = getCapGeight()+1;
		
		g.setColor(bkgrndColor);
		g.fillRect(x1, y, scrWidth, getHeight()-y);
		g.setColor(0xFF);
		g.drawLine(x, y, x, getHeight());
		int clientHeight = getHeight()-getCapGeight();
		int dataHeight = getDataHeight()-clientHeight;
		if (dataHeight <= 0) return;
		int scrY = (clientHeight-defFontHeight)*currentOffset/dataHeight+getCapGeight();
		g.fillRect(x1, scrY, scrWidth, defFontHeight);
	}
	
	private void drawCaption(Graphics g)
	{
		if (title == null) return;
		if (title.length() == 0) return;
		int capHeight = getCapGeight();
		int width = getWidth();
		Font font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM); 
		g.setFont(font);
		g.setColor(bkgrndColor);
		g.fillRect(0, 0, width, capHeight);
		g.setColor(0xFF);
		g.drawLine(0, capHeight-1, width, capHeight-1);
		g.drawString(title, defFontHeight/4, 1, Graphics.TOP|Graphics.LEFT);
	}
	
	final public void setCaption(String value)
	{
		if ((title != null) && title.equals(value)) return;
		title = value;
		invalidate();
	}
	
	final protected void paint(Graphics g)
	{
		paintClient(g, 0, getCapGeight()+1, getWidth()-getScrollerWidth(), getHeight(), currentOffset);
		drawScroller(g);
		drawCaption(g);
	}
	
	final private void checkOffset()
	{
		if (currentOffset < 0) currentOffset = 0;
		int offsetMax = getDataHeight()-getHeight()+getCapGeight();
		if (currentOffset > offsetMax) currentOffset = offsetMax; 
		if (getDataHeight() < (getHeight()-getCapGeight())) currentOffset = 0;
	}
	
	final private void keyReact(int keyCode)
	{
		storeLastOffset();
		switch (getGameAction(keyCode))
		{
		case Canvas.UP:
			currentOffset -= defFontHeight;
			break;
		
		case Canvas.DOWN:
			currentOffset += defFontHeight;
			break;
		}
		checkOffset();
		repaintIfChanged();
	}
	
	protected void userPressKey(int keyCode) {}
	
	final protected void keyPressed(int keyCode)
	{
		keyReact(keyCode);
		userPressKey(keyCode);
	}
	
	final private void storeLastOffset()
	{
		lastOffset = currentOffset;
	}
	
	final private void repaintIfChanged()
	{
		if (lastOffset != currentOffset) invalidate();
	}
}

interface RichTextListElement
{
	int getHeight();
	int draw(int x, int y, Graphics g);
}

class TextElement implements RichTextListElement
{
	String text;
	private int 
		color = 0x000000, 
		size  = Font.SIZE_MEDIUM, 
		style = Font.STYLE_PLAIN; 
	
	public TextElement(String text, int color, int size, int style)
	{
		this.text = text;
		this.color = color;  
		this.size = size;  
		this.style = style;  
	}
	
	public int getHeight()
	{
		Font font = Font.getFont(Font.FACE_PROPORTIONAL, style, size);
		return font.getHeight();
	}
	
	public int draw(int x, int y, Graphics g)
	{
		Font font = Font.getFont(Font.FACE_PROPORTIONAL, style, size);
		g.setFont(font);
		g.setColor(color);
		g.drawString(text, x, y, Graphics.TOP|Graphics.LEFT);
		return font.stringWidth(text);
	}
}

class ImageElement implements RichTextListElement
{
	Image image;
	
	ImageElement(Image image)
	{
		this.image = image;
	}
	
	public int getHeight()
	{
		if (image == null) return 0;
		return image.getHeight();
	}
	
	public int draw(int x, int y, Graphics g)
	{
		if (image == null) return 0;
		g.drawImage(image, x, y, Graphics.TOP|Graphics.LEFT);
		return image.getWidth();
	}
}

class RichTextListLine
{
	Vector items = new Vector();
	
	int lastHeight = -1;
	
	int calcheight()
	{
		int i, count, height, maxHeight;
		
		if (lastHeight != -1) return lastHeight;
					
		count = items.size();
		maxHeight = 0;
		for (i = 0; i < count; i++)
		{
			height = ((RichTextListElement)items.elementAt(i)).getHeight();
			if (height > maxHeight) maxHeight = height; 
		}
		
		lastHeight = maxHeight; 
		return maxHeight;
	}
	
	void draw(int x, int y, Graphics g)
	{
		int totalHeight = calcheight();
		int i, count, height, width;
		
		count = items.size();
		for (i = 0; i < count; i++)
		{
			RichTextListElement element = (RichTextListElement)items.elementAt(i);
			height = element.getHeight();
			width = element.draw(x, y+totalHeight-height, g);
			x += width;
		}
	}
}

public class RichTextList extends TitledControl
{
	private Vector lines = new Vector();
	
	
	private int lastDataheight = -1;
	
	private int fontStyle = Font.STYLE_PLAIN,
	            fontSize  = Font.SIZE_MEDIUM,
		        fontColor = 0x00;
	
	public RichTextList(String cap)
	{
		super(cap);
	}

	final public RichTextList setFontStyle(int value)
	{
		fontStyle = value;
		return this;
	}
	
	final public RichTextList setFontColor(int value)
	{
		fontColor = value;
		return this;
	}
	
	final public RichTextList setFontSize(int value)
	{
		fontSize = value;
		return this;
	}
	
	final protected int getDataHeight()
	{
		if (lastDataheight != -1) return lastDataheight;
		lastDataheight = 0;  
		int count = lines.size();
		for (int i = 0; i < count; i++)
			lastDataheight += ((RichTextListLine)lines.elementAt(i)).calcheight();
		return lastDataheight;
	}
	
	final protected void paintClient(Graphics g, int left, int top, int right, int bottom, int offset)
	{
		int width = getWidth(), 
	        height = getHeight();
		int count, i, lineHeight, y;
		RichTextListLine line;
	
		g.setColor(0xFFFFFF);
		g.fillRect(0, 0, width, height);
	
		y = -offset+top;
		count = lines.size();
		for (i = 0; i < count; i++)
		{
			line = (RichTextListLine)lines.elementAt(i);
			lineHeight = line.calcheight();
			if ((y+defFontHeight) > top) line.draw(0, y, g);
			y += lineHeight;
			if (y > height) break;
		}
	}
	
	final private RichTextListLine getLastLine()
	{
		if (lines.size() == 0) lines.addElement( new RichTextListLine() );
		return (RichTextListLine)lines.lastElement();
	}
	
	final public RichTextList insertImage(Image img, boolean nextLine)
	{
		RichTextListLine line = getLastLine();
		line.items.addElement( new ImageElement(img) );
		line.lastHeight = -1;
		if (nextLine) lines.addElement( new RichTextListLine() );
		invalidate();
		return this;
	}
	
	final private void addtext(String text, boolean nextLine)
	{
		RichTextListLine line = getLastLine();

		line.items.addElement( new TextElement(text, fontColor, fontSize, fontStyle) );
		line.lastHeight = -1;
		if (nextLine) lines.addElement( new RichTextListLine() );
		lastDataheight = -1;
		invalidate();
	}
	
	final public RichTextList println(String text)
	{
		addtext(text, true);
		return this;
	}
	
	final public RichTextList print(String text)
	{
		addtext(text, false);
		return this;
	}
	
	final public RichTextList println(String text, int color, int style)
	{
		fontStyle = style;
		fontColor = color;
		addtext(text, true);
		return this;
	}
	
	final public RichTextList print(String text, int color, int style)
	{
		fontStyle = style;
		fontColor = color;
		addtext(text, false);
		return this;
	}
	
	final public void clear()
	{
		lines.removeAllElements();
		lastDataheight = -1;
	}
}
