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
 File: src/DrawControls/VirtualList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

package DrawControls;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;

import DrawControls.ImageList;
import DrawControls.ListItem;
import DrawControls.VirtualListCommands;
//#sijapp cond.if target is "MOTOROLA"#
import DrawControls.LightControl;
//#sijapp cond.end#

//! This class is base class of owner draw list controls
/*!
    It allows you to create list with different colors and images. 
    Base class of VirtualDrawList if Canvas, so it draw itself when
    paint event is heppen. VirtualList have cursor controlled of
    user
*/

public abstract class VirtualList extends Canvas
{
	/*! Use dotted mode of cursor. If item of list 
	 is selected, dotted rectangle drawn around  it*/
	public final static int SEL_DOTTED = 2;

	/*! Does't show cursor at selected item. */
	public final static int SEL_NONE = 3;

	/*! Constant for medium sized font of caption and item text */
	public final static int MEDIUM_FONT = Font.SIZE_MEDIUM;

	/*! Constant for large sized font of caption and item text */
	public final static int LARGE_FONT = Font.SIZE_LARGE;

	/*! Constant for small sized font of caption and item text */
	public final static int SMALL_FONT = Font.SIZE_SMALL;

	// default values 
	protected static int defCapColor = 0xD0D0D0, defCapFontColor = 0xFF,
			defBackColor = 0xFFFFFF, defCursorMode = SEL_DOTTED,
			defFontSize = MEDIUM_FONT;
	
	// Set of fonts for quick selecting
	private Font normalFont, boldFont, italicFont;
	
	Font getQuickFont(int style)
	{
		switch (style)
		{
		case Font.STYLE_BOLD:   return boldFont;
		case Font.STYLE_PLAIN:  return normalFont;
		case Font.STYLE_ITALIC: return italicFont;
		}
		return Font.getFont(Font.FACE_SYSTEM, style, fontSize);
	}

	public static int getDefCapColor()
	{
		return defCapColor;
	}

	public static int getDefCapFontColor()
	{
		return defCapFontColor;
	}

	public static int getDefBackColor()
	{
		return defBackColor;
	}

	public static int getDefCursorMode()
	{
		return defCursorMode;
	}

	public static int getDefFontSize()
	{
		return defFontSize;
	}

	private VirtualListCommands vlCommands;

	public void setVLCommands(VirtualListCommands vlCommands)
	{
		this.vlCommands = vlCommands;
	}

	private boolean dontRepaint = false;

	private ImageList imageList = null;

	private String caption;

	protected int currItem = 0;
	
	protected ListItem paintedItem = new ListItem(); 

	private int width = 0, topItem = 0,
			fontHeightInt = -1, fontSize = MEDIUM_FONT, bkgrndColor = 0xFFFFFF,
			capColor = 0x0000D0, textColor = 0x000000, capTxtColor = 0xFFFFFF,
			cursorMode = SEL_DOTTED;

	//! Request number of list elements to be shown in list
	/*! You must return number of list elements in successtor of
	 VirtualList. Class calls method "getSize" each time before it drawn */
	abstract protected int getSize();

	//! Request of data of one list item
	/*! You must reload this method. With help of method "get" class finds out
	 data of each item. Method "get" is called each time when list item 
	 is drawn */
	abstract protected void get
	(
		int index,    //!< Number of requested list item 
		ListItem item //!< Data of list item. Fill this object with item data.
	);

	// public void setCapColor(int value)
	public void setCapColor(int value)
	{
		capColor = value;
		repaint();
	}
	
	// returns height of draw area in pixels  
	protected int getDrawHeight()
	{
		return getHeight()-getCapHeight();
	}

	//! Sets new font size and invalidates items
	public void setFontSize(int value)
	{
		fontSize = value;
		createSetOfFonts(fontSize);
		checkTopItem();
		repaint();
	}
	
	private void createSetOfFonts(int size)
	{
		normalFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,  fontSize); 
		boldFont   = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD,   fontSize);
		italicFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC, fontSize);
	}
	
	public int getFontSize()
	{
		return fontSize;
	}

	public int getTextColor()
	{
		return textColor;
	}

	public void setTextColor(int value)
	{
		textColor = value;
		repaint();
	}

	//! Returns number of visibled lines of text which fits in screen 
	public int getVisCount()
	{
		int capHeight = getCapHeight(); 
		int size = getSize();
		int y = getCapHeight();
		int counter = 0, i;
		int height = getDrawHeight();
		int topItem = this.topItem;
		
		if (size == 0) return 0;
		
		if (topItem < 0) topItem = 0;
		if (topItem >= size) topItem = size-1;
		
		for (i = topItem; i < (size-1); i++)
		{
			y += getItemHeight(i);
			if (y > height) return counter;
			counter++;
		}
		
		y = height;
		counter = 0;
		for (i = size-1; i >= 0; i--)
		{
			y -= getItemHeight(i);
			if (y < capHeight) break;
			counter++;
		}
		
		return counter;
	}

	//TODO: brief text
	public void setCursorMode(int value)
	{
		if (cursorMode == value) return;
		cursorMode = value;
		invalidate();
	}
	
	public int getCursorMode()
	{
		return cursorMode;
	}

	//! Set background color of items
	public void setBackgroundColor(int value //!< New color for background
	)
	{
		bkgrndColor = value;
		repaint();
	}

	//! Returns height of each item in list
	public int getItemHeight(int itemIndex)
	{
		int imgHeight, fontHeight = getFontHeight();
		if (imageList != null) imgHeight = imageList.getHeight() + 1;
		else imgHeight = 0;
		return (fontHeight > imgHeight) ? fontHeight : imgHeight;
	}

	// protected void invalidate()  
	protected void invalidate()
	{
		if (dontRepaint || !isShown()) return;
		repaint();
	}

	//! Set new default values for all new classes based on VirtualList
	/*! To create new class based on VirtualList you must to call constructor
	    only with caption argument. */
	public static void setDefaults
	(
		int capBackColor, //!< Caption background color
		int capTextColor, //!< Caption text color
		int backColor,    //!< Control back color
		int fontSize,     /*!< Control font size. This font size if used both for caption and text in tree nodes */
		int cursorMode    /*!< Cursor mode. Can be VirtualList.SEL_DOTTED or VirtualList.SEL_INVERTED */
	)
	{
		defCapColor = capBackColor;
		defCapFontColor = capTextColor;
		defBackColor = backColor;
		defFontSize = fontSize;
		defCursorMode = cursorMode;
	}

	//! Create new virtual list with default values  
	public VirtualList(String capt //!< Caption text of new virtual list
	)
	{
		super();
		this.caption = capt;
		this.capColor = defCapColor;
		this.capTxtColor = defCapFontColor;
		this.bkgrndColor = defBackColor;
		this.fontSize = defFontSize; 
		createSetOfFonts(this.fontSize);
		this.cursorMode = defCursorMode;
	}

	// public VirtualList
	public VirtualList
	(
		String capt,      //!< Caption text of new virtual list
		int capBackColor, //!< Caption background color
		int capTextColor, //!< Caption text color
		int backColor,    //!< Control back color
		int fontSize,     /*!< Control font size. This font size if used both for caption and text in tree nodes */
		int cursorMode    /*!< Cursor mode. Can be VirtualList.SEL_DOTTED or VirtualList.SEL_INVERTED */
	)
	{
		super();
		this.caption = capt;
		this.capColor = capBackColor;
		this.capTxtColor = capTextColor;
		this.bkgrndColor = backColor;
		
		this.fontSize = fontSize; 
		createSetOfFonts(this.fontSize);
		this.cursorMode = cursorMode;
	}

	//! Setting image list for items
	public void setImageList(ImageList list /*!< Reference to ImageList object or null 
	 if you don't need to show images in list */
	)
	{
		imageList = list;
		repaint();
	}

	//! Return current image list, used for tree node icons
	/*! If no image list stored, null is returned */
	public ImageList getImageList()
	{
		return imageList;
	}

	// protected void checkCurrItem()
	protected void checkCurrItem()
	{
		if (currItem < 0) currItem = 0;
		if (currItem >= getSize() - 1) currItem = getSize() - 1;
	}
	

	// protected void checkTopItem() - internal
	// check for position of top element of list and change it, if nesessary
	protected void checkTopItem()
	{
		int size = getSize();
		int visCount = getVisCount();
		
		if (size == 0)
		{
			topItem = 0;
			return;
		}
		
		if (currItem >= (topItem + visCount - 1)) topItem = currItem - visCount + 1;
		if (currItem < topItem) topItem = currItem;
		
		if ((size - topItem) <= visCount) topItem = (size > visCount) ? (size - visCount) : 0;
		if (topItem < 0) topItem = 0;
	}

	// Check does item with index visible
	protected boolean visibleItem(int index)
	{
		return (index >= topItem) && (index <= (topItem + getVisCount()));
	}

	private int lastCurrItem = 0, lastTopItem = 0;

	// private void storelastItemIndexes()
	protected void storelastItemIndexes()
	{
		lastCurrItem = currItem;
		lastTopItem = topItem;
	}

	// private void repaintIfLastIndexesChanged()
	protected void repaintIfLastIndexesChanged()
	{
		if ((lastCurrItem != currItem) || (lastTopItem != topItem))
		{
			invalidate();
			cursorMoved();
			if (vlCommands != null) vlCommands.onCursorMove(this);
		}
	}

	// protected void moveCursor(int step)
	protected void moveCursor(int step)
	{
		storelastItemIndexes();
		if (cursorMode == SEL_NONE) topItem += step;
		currItem += step;
		checkCurrItem();
		checkTopItem();
		repaintIfLastIndexesChanged();
	}

	// private void moveToBottom()
	private void moveToBottom()
	{
		storelastItemIndexes();
		int endIndex = getSize() - 1;
		currItem = endIndex;
		checkTopItem();
		repaintIfLastIndexesChanged();
	}

	// private void moveToTop()
	private void moveToTop()
	{
		storelastItemIndexes();
		currItem = topItem = 0;
		repaintIfLastIndexesChanged();
	}

	//! Is called when user press a key
	/*! You may reload it in successor to react on key event */
	protected void userPressKey(int keyCode)
	{
	}

	protected void itemSelected() {}
	
	protected void cursorMoved() {}

	// private keyReaction(int keyCode)
	private void keyReaction(int keyCode)
	{
		switch (getGameAction(keyCode))
		{
		case Canvas.DOWN:
			moveCursor(1);
			break;
		case Canvas.UP:
			moveCursor(-1);
			break;
		case Canvas.FIRE:
			itemSelected();
			if (vlCommands != null) vlCommands.onItemSelected(this);
			break;
		}

		switch (keyCode)
		{
		case KEY_NUM1:
			moveToTop();
			break;
		case KEY_NUM7:
			moveToBottom();
			break;

		case KEY_NUM3:
			moveCursor(-getVisCount());
			break;
		case KEY_NUM9:
			moveCursor(getVisCount());
			break;
                // #sijapp cond.if target is "MOTOROLA"#
	        case KEY_STAR: 
                        LightControl.changeState();
                        break;
                // #sijapp cond.end#
		}

	}

	// protected void keyPressed(int keyCode) 
	protected void keyPressed(int keyCode)
	{
                //#sijapp cond.if target is "MOTOROLA"#
                LightControl.flash(false);
                //#sijapp cond.end#
		keyReaction(keyCode);
		userPressKey(keyCode);
		if (vlCommands != null) vlCommands.onKeyPress(this, keyCode);
	}

	// protected void keyRepeated(int keyCode)
	protected void keyRepeated(int keyCode)
	{
		keyReaction(keyCode);
	}

	//! Set caption text for list
	public void setCaption(String capt)
	{
		caption = capt;
		repaint();
	}

	public void setTopItem(int index)
	{
		storelastItemIndexes();
		currItem = topItem = index;
		checkTopItem();
		repaintIfLastIndexesChanged();
	}

	// public void setCurrentItem(int index)
	public void setCurrentItem(int index)
	{
		storelastItemIndexes();
		currItem = index;
		checkTopItem();
		repaintIfLastIndexesChanged();
	}

	// public int getCurrIndex()
	public int getCurrIndex()
	{
		return currItem;
	}
	
	// Return height of caption in pixels
	private int getCapHeight()
	{
		return (caption == null) ? 0 : getQuickFont(Font.STYLE_BOLD).getHeight();
	}

	// private int drawCaption(Graphics g)
	private int drawCaption(Graphics g)
	{
		if (caption == null) return 0;

		int tmp_y, th;
		Font font = getQuickFont(Font.STYLE_BOLD);
		g.setFont(font);
		th = font.getHeight();
		g.setColor(capColor);
		g.fillRect(0, 0, width, th + 2);
		g.setColor(capTxtColor);
		g.drawString(caption, th / 3, 1, Graphics.TOP | Graphics.LEFT);
		g.setColor(0);
		tmp_y = th + 1;
		g.drawLine(0, tmp_y, width, tmp_y);
		g.setColor(bkgrndColor);
		tmp_y++;
		g.drawLine(0, tmp_y, width, tmp_y);
		return tmp_y + 1;
	}

	// private void drawDottedSelectedBgrnd(Graphics g, int x, int y, int w, int h, ListItem item)
	public void drawDottedSelectedBgrnd(
		Graphics g,
		int x,
		int y,
		int w,
		int h,
		int bkColor)
	{
		// Draw bkgrn
		g.setColor(bkColor);
		g.fillRect(x, y, w, h);
		
		// draw rect
		g.setStrokeStyle(Graphics.DOTTED);
		g.setColor(textColor);
		g.drawRect(x, y, w-1, h-1);
		
		// restore line style
		g.setStrokeStyle(Graphics.SOLID);
	}

	protected int getItemBkColor(int index, int lastColor)
	{
		return lastColor;
	}

	// private int drawItem(int index, Graphics g, int top_y, int th, ListItem item)
	private int drawItem(
		int index,
		Graphics g,
		int yCrd,
		int itemHeight,
		int fontHeight,
		int bkColor)
	{
		int w = 0;
		boolean isSelected = ((currItem == index) && (cursorMode != SEL_NONE));

		if (isSelected)
		{
			drawDottedSelectedBgrnd(g, 0, yCrd, width, itemHeight, bkgrndColor);
		}
		else
		{
			g.setColor(bkColor);
			g.fillRect(0, yCrd, width, itemHeight);
		}

		g.setStrokeStyle(Graphics.SOLID);
		drawItemData(g, isSelected, index, 2, yCrd, width - itemHeight / 3, yCrd
				+ itemHeight, fontHeight);

		return yCrd + itemHeight;
	}

	// private void drawScroller(Graphics g, int scrollerWidth)
	private void drawScroller(Graphics g, int scrollerWidth, int topY, int visCount)
	{
		int sliderSize, y1, y2, itemCount, position;
		boolean haveToShowScroller;
		int height = getDrawHeight();
		
		int fh = getFontHeight();
		position = topItem;
		itemCount = getSize();
		
		haveToShowScroller = ((itemCount > visCount) && (itemCount > 0));
	
		int color = transformColorLight(transformColorLight(bkgrndColor, 32), -32);
		if (color == 0) color = 0x808080;
		g.setColor(color);
		g.fillRect(width + 1, topY, scrollerWidth - 1, height - topY);
		g.setColor(transformColorLight(color, -64));
		g.drawLine(width, topY, width, height);
		if (haveToShowScroller)
		{
			sliderSize = (height-topY)*visCount/itemCount;
		    y1 = position * (height - sliderSize - topY) / (itemCount-visCount) + topY;
			y2 = y1 + sliderSize;
			g.setColor(color);
			g.fillRect(width + 2, y1 + 2, scrollerWidth - 3, y2 - y1 - 3);
			g.setColor(transformColorLight(color, -192));
			g.drawRect(width, y1, scrollerWidth - 1, y2 - y1 - 1);
			g.setColor(transformColorLight(color, 96));
			g.drawLine(width + 1, y1 + 1, width + 1, y2 - 2);
			g.drawLine(width + 1, y1 + 1, width + scrollerWidth - 2, y1 + 1);
		}
	}

	//! returns font height
	public int getFontHeight()
	{
		if (fontHeightInt != -1) return fontHeightInt;
		Font font = getQuickFont(Font.STYLE_PLAIN);
		fontHeightInt = font.getHeight();
		return fontHeightInt;
	}

	// private int drawItems(Graphics g, int top_y)
	private int drawItems(Graphics g, int top_y, int fontHeight)
	{
		int size, itemHeight, i, y = 0;
		int height = getDrawHeight();
		ListItem item = new ListItem();

		size = getSize();

		y = top_y;
		for (i = topItem; i < size; i++)
		{
			itemHeight = getItemHeight(i);
			int bkColor = getItemBkColor(i, bkgrndColor);
			y = drawItem(i, g, y, itemHeight, fontHeight, bkColor);
			if (y >= height) break;
		}
		if (y < height)
		{
			g.setColor(bkgrndColor);
			g.fillRect(0, y, width, height);
		}

		return y;
	}

	void init()
	{
	}

	void destroy()
	{
	}

	// protected int getScrollerWidth()
	protected int getScrollerWidth()
	{
		int width = getFontHeight() / 4;
		return width > 4 ? width : 4;
	}

	// change light of color 
	static public int transformColorLight(int color, int light)
	{
		int r = (color & 0xFF) + light;
		int g = ((color & 0xFF00) >> 8) + light;
		int b = ((color & 0xFF0000) >> 16) + light;
		if (r < 0) r = 0;
		if (r > 255) r = 255;
		if (g < 0) g = 0;
		if (g > 255) g = 255;
		if (b < 0) b = 0;
		if (b > 255) b = 255;
		return r | (g << 8) | (b << 16);
	}

	// private void paintAllOnGraphics(Graphics graphics)
	private void paintAllOnGraphics(Graphics graphics)
	{
		int scrollerWidth = getScrollerWidth();
		int visCount = getVisCount();
		width = getWidth() - scrollerWidth;
		int y = drawCaption(graphics);
		drawItems(graphics, y, getFontHeight());
		drawScroller(graphics, scrollerWidth, y, visCount);
	}

	static Image bDIimage = null;

	// protected void paint(Graphics g)
	protected void paint(Graphics g)
	{
		if (dontRepaint) return;

		if (isDoubleBuffered())
		{
			paintAllOnGraphics(g);
		}
		else
		{
			try
			{
				if (bDIimage == null) bDIimage = Image.createImage(getWidth(), getHeight());
				paintAllOnGraphics(bDIimage.getGraphics());
				g.drawImage(bDIimage, 0, 0, Graphics.TOP | Graphics.LEFT);
			}
			catch (Exception e)
			{
				paintAllOnGraphics(g);
			}
		}
	}

	// protected void drawItemData
	protected void drawItemData(
		Graphics g,
		boolean isSelected,
		int index,
		int x1,
		int y1,
		int x2,
		int y2,
		int fontHeight)
	{
		int imgWidth;

		get(index, paintedItem);
		
	    g.setFont(getQuickFont(paintedItem.fontStyle));
	    g.setColor(paintedItem.color);
		
		ImageList imageList = getImageList();
		if ((imageList != null) 
				&& (paintedItem.imageIndex >= 0)
				&& (paintedItem.imageIndex < imageList.size()))
		{
			imgWidth = imageList.getWidth() + 3;
			g.drawImage
			(
				imageList.elementAt(paintedItem.imageIndex),
				x1 + 1, 
				(y1 + y2 - imageList.getHeight()) / 2, 
				Graphics.TOP | Graphics.LEFT
			);
		}
		else imgWidth = 0;

		if (paintedItem.text != null) 
			g.drawString
			(
				paintedItem.text, 
				x1 + imgWidth, 
				(y1 + y2 - fontHeight) / 2, 
				Graphics.TOP | Graphics.LEFT
			);
	}

	public void lock()
	{
		dontRepaint = true;
	}

	protected void afterUnlock()
	{
	}

	public void unlock()
	{
		dontRepaint = false;
		afterUnlock();
		invalidate();
	}

	protected boolean getLocked()
	{
		return dontRepaint;
	}

}