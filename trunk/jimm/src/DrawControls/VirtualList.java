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
 File: src/DrawControls/VirtualList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis, Igor Palkin
 *******************************************************************************/

package DrawControls;

import javax.microedition.lcdui.*;
import java.util.Vector;

import DrawControls.ImageList;
import DrawControls.ListItem;
import DrawControls.VirtualListCommands;

//#sijapp cond.if target is "MOTOROLA"#
//# import jimm.TimerTasks;
//# import jimm.Jimm;
//# import jimm.Options;
//# import com.motorola.funlight.*;
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

	// Key event type
	public final static int KEY_PRESSED = 1;

	public final static int KEY_REPEATED = 2;

	public final static int KEY_RELEASED = 3;

	// Set of fonts for quick selecting
	private Font normalFont, boldFont, italicFont;

	// Width of scroller line
	protected final static int scrollerWidth;

	// Font for drawing caption
	private static Font capFont;
	
	// Commands to react to VL events
	private VirtualListCommands vlCommands;

	// Caption of VL
	private String caption;

	// Used by "Invalidate" method to prevent invalidate when locked 
	private boolean dontRepaint = false;

	// Images for VL
	private ImageList imageList = null;

	// Index for current item of VL
	protected int currItem = 0;

	// Used for passing params of items whan painting 
	final static protected ListItem paintedItem;

	// Used to catch changes to repaint data
	private int lastCurrItem = 0, lastTopItem = 0;

	private static VirtualList current;

	private static boolean fullScreen = false;

	private Image capImage;
	
	private static int KEY_CODE_LEFT_MENU;
	private static int KEY_CODE_RIGHT_MENU;
	private static int KEY_CODE_BACK_BUTTON;
	
	private static int curMenuItemIndex; 
	

	private int topItem = 0, // Index of top visilbe item 
			fontSize = MEDIUM_FONT, // Current font size of VL
			bkgrndColor = 0xFFFFFF, // bk color of VL
			cursorColor = 0x808080, // Used when drawing focus rect.
			textColor = 0x000000, // Default text color.
			capBkCOlor = 0xC0C0C0, capTxtColor = 0x00, // Color of caprion text
			cursorMode = SEL_DOTTED; // Cursor mode

	static
	{
		//#sijapp cond.if target="MIDP2"#
		capFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
		//#sijapp cond.else#
		//# 		capFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
		//#sijapp cond.end#
		int width = capFont.getHeight() / 4;
		scrollerWidth = width > 4 ? width : 4;
		paintedItem = new ListItem();
		
		//#sijapp cond.if target="SIEMENS2"#
		KEY_CODE_LEFT_MENU = -1;
		KEY_CODE_RIGHT_MENU = -4;
		KEY_CODE_BACK_BUTTON = -12;
		//#sijapp cond.else#
		KEY_CODE_LEFT_MENU = -6;
		KEY_CODE_RIGHT_MENU = -7;
		KEY_CODE_BACK_BUTTON = -11;
		//#sijapp cond.end#
	}

	static public void setFullScreen(boolean value)
	{
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		if (fullScreen == value) return;
		fullScreen = value;
		if (current != null)
		{
			current.setFullScreenMode(fullScreen);
			if (!fullScreen) current.setTitle(current.caption);
		}
		//#sijapp cond.end#
	}

	//! Create new virtual list with default values  
	public VirtualList(String capt //!< Caption text of new virtual list
	)
	{
		super();
		setCaption(capt);
		//#sijapp cond.if target is "SIEMENS2"# 
		//#		this.fontSize = Font.SIZE_SMALL;
		//#sijapp cond.else#
		this.fontSize = Font.SIZE_MEDIUM;
		//#sijapp cond.end#
		createSetOfFonts(this.fontSize);
		this.cursorMode = SEL_DOTTED;
	}

	// public VirtualList
	public VirtualList(String capt, //!< Caption text of new virtual list
		int capTextColor, //!< Caption text color
		int backColor, //!< Control back color
		int fontSize, /*!< Control font size. This font size if used both for caption and text in tree nodes */
		int cursorMode /*!< Cursor mode. Can be VirtualList.SEL_DOTTED or VirtualList.SEL_INVERTED */
	)
	{
		super();
		setCaption(capt);
		this.capTxtColor = capTextColor;
		this.bkgrndColor = backColor;

		this.fontSize = fontSize;
		createSetOfFonts(this.fontSize);
		this.cursorMode = cursorMode;
	}

	//! Request number of list elements to be shown in list
	/*! You must return number of list elements in successtor of
	 VirtualList. Class calls method "getSize" each time before it drawn */
	abstract protected int getSize();

	//! Request of data of one list item
	/*! You have to reload this method. With help of method "get" class finds out
	 data of each item. Method "get" is called each time when list item 
	 is drawn */
	abstract protected void get(int index, //!< Number of requested list item 
		ListItem item //!< Data of list item. Fill this object with item data.
	);

	Font getQuickFont(int style)
	{
		switch (style)
		{
		case Font.STYLE_BOLD:
			return boldFont;
		case Font.STYLE_PLAIN:
			return normalFont;
		case Font.STYLE_ITALIC:
			return italicFont;
		}
		return Font.getFont(Font.FACE_SYSTEM, style, fontSize);
	}

	// returns height of draw area in pixels  
	protected int getDrawHeight()
	{
		return getHeightInternal() - getCapHeight() - getMenuBarHeight();
	}

	//! Sets new font size and invalidates items
	public void setFontSize(int value)
	{
		if (fontSize == value) return;
		fontSize = value;
		createSetOfFonts(fontSize);
		checkTopItem();
		invalidate();
	}

	public void setCapImage(Image image)
	{
		if (capImage == image) return;
		capImage = image;
		invalidate();
	}

	public void setVLCommands(VirtualListCommands vlCommands)
	{
		this.vlCommands = vlCommands;
	}

	public void setColors(int capTxt, int capbk, int bkgrnd, int cursor, int text)
	{
		capBkCOlor = capbk;
		capTxtColor = capTxt;
		bkgrndColor = bkgrnd;
		cursorColor = cursor;
		textColor = text;
		repaint();
	}

	private void createSetOfFonts(int size)
	{
		normalFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSize);
		boldFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, fontSize);
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

	//! Returns number of visibled lines of text which fits in screen 
	public int getVisCount()
	{
		int size = getSize();
		int y = 0;
		int counter = 0, i;
		int height = getDrawHeight();
		int topItem = this.topItem;

		if (size == 0) return 0;

		if (topItem < 0) topItem = 0;
		if (topItem >= size) topItem = size - 1;

		for (i = topItem; i < (size - 1); i++)
		{
			y += getItemHeight(i);
			if (y > height) return counter;
			counter++;
		}

		y = height;
		counter = 0;
		for (i = size - 1; i >= 0; i--)
		{
			y -= getItemHeight(i);
			if (y < 0) break;
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

	protected void showNotify()
	{
		current = this;
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		
		if (exMenuExists()) setFullScreenMode(true);
		else
		{
			setFullScreenMode(fullScreen);
			if (!fullScreen) setTitle(caption);
		}
		//#sijapp cond.end #
		forcedHeight = forcedWidth = -1;
		uiState = UI_STATE_NORMAL;
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
		if (dontRepaint) return;
		repaint();
	}

	// Setting image list for items
	public void setImageList(ImageList list)
	{
		imageList = list;
		invalidate();
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
			if (vlCommands != null) vlCommands.onCursorMove(this);
		}
	}

	// protected void moveCursor(int step)
	protected void moveCursor(int step, boolean moveTop)
	{
		storelastItemIndexes();
		if (moveTop && (cursorMode == SEL_NONE)) topItem += step;
		currItem += step;
		checkCurrItem();
		checkTopItem();
		repaintIfLastIndexesChanged();
	}

	protected void itemSelected()
	{
	}

	// private keyReaction(int keyCode)
	private void keyReaction(int keyCode)
	{
		boolean menuItemsVisible = false;
		int lastManuIndex = curMenuItemIndex;
		Vector menuItemsData = null;
		switch (uiState)
		{
		case UI_STATE_LEFT_MENU_VISIBLE:
			menuItemsVisible = true;
			menuItemsData = leftMenuItems;
			break;
			
		case UI_STATE_RIGHT_MENU_VISIBLE:
			menuItemsVisible = true;
			menuItemsData = rightMenuItems;
			break;
		}
		
		try
		{
			switch (getGameAction(keyCode))
			{
			case Canvas.DOWN:
				if (menuItemsVisible)
				{
					curMenuItemIndex++;
					if (curMenuItemIndex >= menuItemsData.size()) curMenuItemIndex = menuItemsData.size()-1; 
				}
				else moveCursor(1, false);
				break;
				
			case Canvas.UP:
				if (menuItemsVisible)
				{
					curMenuItemIndex--;
					if (curMenuItemIndex < 0) curMenuItemIndex = 0; 
				}
				else moveCursor(-1, false);
				break;
				
			case Canvas.FIRE:
				if ((keyCode == KEY_CODE_LEFT_MENU) || (keyCode == KEY_CODE_RIGHT_MENU)) return;
				if (menuItemsVisible)
				{
					uiState = UI_STATE_NORMAL;
					executeCommand((Command)menuItemsData.elementAt(curMenuItemIndex));
					invalidate();
				}
				else
				{
					Command defaultCommand = findMenuByType(Command.OK);
					if (defaultCommand != null)
					{
						executeCommand(defaultCommand);
						return;
					}
					
					itemSelected();
					if (vlCommands != null) vlCommands.onItemSelected(this);
				}
				break;
			}
		}
		catch (Exception e) // getGameAction throws exception on motorola
		{ // when opening flipper
			return;
		}
		
		if (menuItemsVisible && (lastManuIndex != curMenuItemIndex))
		{
			invalidate();
			return;
		}

		switch (keyCode)
		{
		case KEY_NUM1:
			storelastItemIndexes();
			currItem = topItem = 0;
			repaintIfLastIndexesChanged();
			break;

		case KEY_NUM7:
			storelastItemIndexes();
			int endIndex = getSize() - 1;
			currItem = endIndex;
			checkTopItem();
			repaintIfLastIndexesChanged();
			break;

		case KEY_NUM3:
			moveCursor(-getVisCount(), false);
			break;

		case KEY_NUM9:
			moveCursor(getVisCount(), false);
			break;

		//#sijapp cond.if target is "MOTOROLA"#
		//#		case KEY_STAR: 
		//#		setBkltOn(!bklt_on);
		//#		break;
		//#sijapp cond.end#
		}

	}

	public void doKeyreaction(int keyCode, int type)
	{
		switch (type)
		{
		case KEY_PRESSED:
			//#sijapp cond.if target="MOTOROLA"#
			//#			if (!Options.getBoolean(Options.OPTION_LIGHT_MANUAL))
			//#				flashBklt(Options.getInt(Options.OPTION_LIGHT_TIMEOUT)*1000);
			//#sijapp cond.end#
			keyReaction(keyCode);
			break;
		case KEY_REPEATED:
			keyReaction(keyCode);
			break;
		}

		if (vlCommands != null) vlCommands.onKeyPress(this, keyCode, type);
	}

	protected void keyPressed(int keyCode)
	{
		int lastUIState = uiState;
		
		if (keyCode == KEY_CODE_LEFT_MENU)
		{
			if (leftMenu != null)
			{
				if (leftMenuItems.size() == 0)
				{
					if ( executeCommand(leftMenu) ) return;
				}
				else 
				{
					uiState = UI_STATE_LEFT_MENU_VISIBLE;
					curMenuItemIndex = leftMenuItems.size()-1;
				}
			}
		}
		else if (keyCode == KEY_CODE_RIGHT_MENU)
		{
			if (rightMenu != null)
			{
				if (rightMenuItems.size() == 0)
				{
					if ( executeCommand(rightMenu) ) return;
				}
				else 
				{
					uiState = UI_STATE_RIGHT_MENU_VISIBLE;
					curMenuItemIndex = rightMenuItems.size()-1;
				}
			}
		}
		else if (keyCode == KEY_CODE_BACK_BUTTON)
		{
			switch (uiState)
			{
			case UI_STATE_RIGHT_MENU_VISIBLE:
			case UI_STATE_LEFT_MENU_VISIBLE:
				uiState = UI_STATE_NORMAL;
				invalidate();
				break;

			default:
				Command backMenu = findMenuByType(Command.BACK);
				if (backMenu != null)
				{
					if (executeCommand(backMenu)) return;
				}
				break;
			}
		}
			
		if (lastUIState != uiState)
		{
			invalidate();
			return;
		}
		
		doKeyreaction(keyCode, KEY_PRESSED);
	}
	
	private CommandListener commandListener;
	public void setCommandListener(CommandListener l)
	{
		commandListener = l;
		super.setCommandListener(l);
	}
	
	private boolean executeCommand(Command command)
	{
		if (commandListener != null)
		{
			commandListener.commandAction(command, this);
			return true;
		}
		return false;
	}

	protected void keyRepeated(int keyCode)
	{
		doKeyreaction(keyCode, KEY_REPEATED);
	}

	protected void keyReleased(int keyCode)
	{
		doKeyreaction(keyCode, KEY_RELEASED);
	}

	//#sijapp cond.if target is "MIDP2"#
	private static long lastPointerTime = 0;

	private static int lastPointerYCrd = -1;

	private static int lastPointerXCrd = -1;

	private static int lastPointerTopItem = -1;

	protected void pointerDragged(int x, int y)
	{
		if (lastPointerTopItem == -1) return;
		int height = getHeightInternal() - getCapHeight();
		int itemCount = getSize();
		int visCount = getVisCount();
		if (itemCount == visCount) return;
		storelastItemIndexes();
		topItem = lastPointerTopItem + (itemCount) * (y - lastPointerYCrd) / height;
		if (topItem < 0) topItem = 0;
		if (topItem > (itemCount - visCount)) topItem = itemCount - visCount;
		repaintIfLastIndexesChanged();
	}

	protected boolean pointerPressedOnUtem(int index, int x, int y)
	{
		return false;
	}

	static int abs(int value)
	{
		return (value < 0) ? -value : value;
	}

	protected void pointerPressed(int x, int y)
	{
		int itemY1 = getCapHeight();

		// is pointing on scroller
		if (x >= (getWidthInternal() - 3 * scrollerWidth))
		{
			if ((srcollerY1 <= y) && (y < srcollerY2))
			{
				lastPointerYCrd = y;
				lastPointerTopItem = topItem;
			}
			return;
		}

		// is pointing on data area
		lastPointerTopItem = -1;

		int size = getSize();
		for (int i = topItem; i < size; i++)
		{
			int height = getItemHeight(i);
			int itemY2 = itemY1 + height;
			if ((itemY1 <= y) && (y < itemY2))
			{
				setCurrentItem(i);

				if (pointerPressedOnUtem(i, x, y) == false)
				{
					long time = System.currentTimeMillis();
					if (((time - lastPointerTime) < 500) && (abs(x - lastPointerXCrd) < 10) && (abs(y - lastPointerYCrd) < 10))
					{
						itemSelected();
						if (vlCommands != null) vlCommands.onItemSelected(this);
					}
					lastPointerTime = time;
				}
				break;
			}
			itemY1 = itemY2;
		}

		lastPointerXCrd = x;
		lastPointerYCrd = y;
	}

	//#sijapp cond.end#

	//! Set caption text for list
	public void setCaption(String capt)
	{
		if (caption != null) if (caption.equals(capt)) return;
		caption = capt;

		//#sijapp cond.if target="MIDP2" | target="MOTOROLA" | target="SIEMENS2"#
		if (fullScreen || exMenuExists()) invalidate();
		else setTitle(capt);
		//#sijapp cond.else#
		//# 	invalidate();
		//#sijapp cond.end#
	}

	public String getCaption()
	{
		return caption;
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
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		if (!fullScreen && !exMenuExists()) return 0;
		//#sijapp cond.end#
		int capHeight = 0;
		if (caption != null) capHeight = capFont.getHeight() + 2;
		if (capImage != null)
		{
			int imgHeight = capImage.getHeight() + 2;
			if (imgHeight > capHeight) capHeight = imgHeight;
		}

		return capHeight + 1;
	}

	// private int drawCaption(Graphics g)
	protected int drawCaption(Graphics g)
	{
		if (caption == null) return 0;
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		if (!fullScreen && !exMenuExists()) return 0;
		//#sijapp cond.end#

		int width = getWidthInternal();
		g.setFont(capFont);
		int height = getCapHeight();
		drawRect(g, capBkCOlor, transformColorLight(capBkCOlor, -32), 0, 0, width, height);

		g.setColor(transformColorLight(capBkCOlor, -128));
		g.drawLine(0, height - 1, width, height - 1);

		int x = 2;

		if (capImage != null)
		{
			g.drawImage(capImage, x, (height - capImage.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
			x += capImage.getWidth() + 1;
		}

		g.setColor(capTxtColor);
		g.drawString(caption, x, (height - capFont.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
		return height;
	}

	protected boolean isItemSelected(int index)
	{
		return ((currItem == index) && (cursorMode != SEL_NONE));
	}

	// private int drawItem(int index, Graphics g, int top_y, int th, ListItem item)
	private int drawItem(int index, Graphics g, int yCrd, int itemWidth, int itemHeight, int fontHeight)
	{
		drawItemData(g, index, 1, yCrd, itemWidth-2, yCrd + itemHeight, fontHeight);
		return yCrd + itemHeight;
	}

	private static int srcollerY1 = -1;

	private static int srcollerY2 = -1;

	// Draw scroller is items doesn't fit in VL area 
	private void drawScroller(Graphics g, int topY, int visCount, int menuBarHeight)
	{
		int width = getWidthInternal() - scrollerWidth;
		int height = getHeightInternal()-menuBarHeight;
		int itemCount = getSize();
		boolean haveToShowScroller = ((itemCount > visCount) && (itemCount > 0));
		int color = transformColorLight(transformColorLight(bkgrndColor, 32), -32);
		if (color == 0) color = 0x808080;
		g.setStrokeStyle(Graphics.SOLID);
		g.setColor(color);
		g.fillRect(width + 1, topY, scrollerWidth - 1, height - topY);
		g.setColor(transformColorLight(color, -64));
		g.drawLine(width, topY, width, height);
		if (haveToShowScroller)
		{
			int sliderSize = (height - topY) * visCount / itemCount;
			if (sliderSize < 7) sliderSize = 7;
			srcollerY1 = topItem * (height - sliderSize - topY) / (itemCount - visCount) + topY;
			srcollerY2 = srcollerY1 + sliderSize;
			g.setColor(color);
			g.fillRect(width + 2, srcollerY1 + 2, scrollerWidth - 3, srcollerY2 - srcollerY1 - 3);
			g.setColor(transformColorLight(color, -192));
			g.drawRect(width, srcollerY1, scrollerWidth - 1, srcollerY2 - srcollerY1 - 1);
			g.setColor(transformColorLight(color, 96));
			g.drawLine(width + 1, srcollerY1 + 1, width + 1, srcollerY2 - 2);
			g.drawLine(width + 1, srcollerY1 + 1, width + scrollerWidth - 2, srcollerY1 + 1);
		}
	}

	static private void drawRect(Graphics g, int color1, int color2, int x1, int y1, int x2, int y2)
	{
		int r1 = ((color1 & 0xFF0000) >> 16);
		int g1 = ((color1 & 0x00FF00) >> 8);
		int b1 = (color1 & 0x0000FF);
		int r2 = ((color2 & 0xFF0000) >> 16);
		int g2 = ((color2 & 0x00FF00) >> 8);
		int b2 = (color2 & 0x0000FF);
		int count = 8;
		y2++;
		x2++;

		for (int i = 0; i < count; i++)
		{
			int crd1 = i * (y2 - y1) / count + y1;
			int crd2 = (i + 1) * (y2 - y1) / count + y1;
			g.setColor(i * (r2 - r1) / (count-1) + r1, i * (g2 - g1) / (count-1) + g1, i * (b2 - b1) / (count-1) + b1);
			g.fillRect(x1, crd1, x2-x1, crd2-crd1);
		}
	}

	//! returns font height
	public int getFontHeight()
	{
		return getQuickFont(Font.STYLE_PLAIN).getHeight();
	}

	// private int drawItems(Graphics g, int top_y)
	private int drawItems(Graphics g, int top_y, int fontHeight, int menuBarHeight)
	{
		int grCursorY1 = -1, grCursorY2 = -1;
		int height = getHeightInternal();
		int size = getSize();
		int i, y;
		int itemWidth = getWidthInternal() - scrollerWidth;

		// Fill background
		g.setColor(bkgrndColor);
		g.fillRect(0, top_y, itemWidth, height - top_y);

		// Draw cursor
		y = top_y;
		for (i = topItem; i < size; i++)
		{
			int itemHeight = getItemHeight(i);
			if (isItemSelected(i))
			{
				if (grCursorY1 == -1) grCursorY1 = y;
				grCursorY2 = y + itemHeight - 1;
			}
			y += itemHeight;
			if (y >= height) break;
		}

		if (grCursorY1 != -1)
		{
			g.setStrokeStyle(Graphics.DOTTED);
			g.setColor(cursorColor);
			boolean isCursorUpper = (topItem >= 1) ? isItemSelected(topItem - 1) : false;
			if (!isCursorUpper) g.drawLine(1, grCursorY1, itemWidth - 2, grCursorY1);
			g.drawLine(0, grCursorY1 + 1, 0, grCursorY2 - 1);
			g.drawLine(itemWidth - 1, grCursorY1 + 1, itemWidth - 1, grCursorY2 - 1);
			g.drawLine(1, grCursorY2, itemWidth - 2, grCursorY2);
		}

		// Draw items
		paintedItem.clear();
		y = top_y;
		for (i = topItem; i < size; i++)
		{
			int itemHeight = getItemHeight(i);
			g.setStrokeStyle(Graphics.SOLID);
			y = drawItem(i, g, y, itemWidth, itemHeight, fontHeight);
			if (y >= height) break;
		}

		return y;
	}

	void init()
	{
	}

	void destroy()
	{
	}

	// change light of color 
	static private int transformColorLight(int color, int light)
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
	
	static private int getInverseColor(int color)
	{
		int r = (color & 0xFF);
		int g = ((color & 0xFF00) >> 8);
		int b = ((color & 0xFF0000) >> 16);
		return ((r+g+b) > 3*127) ? 0 : 0xFFFFFF;
	}

	public void paintAllOnGraphics(Graphics graphics)
	{
		int visCount = getVisCount();
		int y = drawCaption(graphics);
		int menuBarHeight = getMenuBarHeight();
		drawItems(graphics, y, getFontHeight(), menuBarHeight);
		if (menuBarHeight != 0) drawMenuBar(graphics, menuBarHeight);
		drawScroller(graphics, y, visCount, menuBarHeight);
		drawMenuItems(graphics, menuBarHeight);
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
				if (bDIimage == null) bDIimage = Image.createImage(getWidthInternal(), getHeightInternal());
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
	protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int fontHeight)
	{
		paintedItem.clear();
		get(index, paintedItem);
		
		int x = paintedItem.horizOffset+x1;
		
		// Draw first left image
		if (paintedItem.leftImage != null)
		{
			g.drawImage
			(
				paintedItem.leftImage, 
				x, 
				(y1 + y2 - paintedItem.leftImage.getHeight()) / 2, 
				Graphics.TOP | Graphics.LEFT
			);
			x += (paintedItem.leftImage.getWidth()+1);
		}
		
		// Draw second left image
		if (paintedItem.secondLeftImage != null)
		{
			g.drawImage
			(
				paintedItem.secondLeftImage, 
				x, 
				(y1 + y2 - paintedItem.secondLeftImage.getHeight()) / 2, 
				Graphics.TOP | Graphics.LEFT
			);
			x += (paintedItem.secondLeftImage.getWidth()+1);
		}

		// Draw text of item
		if (paintedItem.text != null)
		{
			g.setFont(getQuickFont(paintedItem.fontStyle));
			g.setColor(paintedItem.color);
			g.drawString(paintedItem.text, x+1, (y1 + y2 - fontHeight) / 2, Graphics.TOP | Graphics.LEFT);
		}
		
		// Draw right image
		if (paintedItem.rightImage != null)
		{
			g.drawImage
			(
				paintedItem.rightImage, 
				x2-paintedItem.rightImage.getWidth(), 
				(y1 + y2 - paintedItem.rightImage.getHeight()) / 2, 
				Graphics.TOP | Graphics.LEFT
			);
		}
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

	private int forcedWidth = -1;

	private int forcedHeight = -1;

	public void setForcedSize(int width, int height)
	{
		forcedWidth = width;
		forcedHeight = height;
	}

	protected int getHeightInternal()
	{
		return (forcedHeight == -1) ? getHeight() : forcedHeight;
	}

	protected int getWidthInternal()
	{
		return (forcedWidth == -1) ? getWidth() : forcedWidth;
	}
	
	///////////////////////////////
	//                           //
	//        EXTENDED UI        //
    //                           //
	///////////////////////////////
	
	public static final int MENU_TYPE_LEFT_BAR = 1;
	public static final int MENU_TYPE_RIGHT_BAR = 2;
	public static final int MENU_TYPE_LEFT = 3;
	public static final int MENU_TYPE_RIGHT = 4;
	
	private static final int UI_STATE_NORMAL = 0;
	private static final int UI_STATE_LEFT_MENU_VISIBLE = 1;
	private static final int UI_STATE_RIGHT_MENU_VISIBLE = 2;
	
	private int uiState;
	
	// Font for painting menu bar
	private static Font menuBarFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
	private static Font menuItemsFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
	
	private Command leftMenu;
	private Command rightMenu;
	private Vector leftMenuItems = new Vector();
	private Vector rightMenuItems = new Vector();
	
	private void drawMenuBar(Graphics g, int height)
	{
		int y1 = getHeightInternal()-height;
		int y2 = getHeightInternal();
		int width = getWidthInternal();
		int layer = height/4;
		boolean defaultMenu = false;
		
		drawRect(g, transformColorLight(capBkCOlor, -32), capBkCOlor, 0, y1, width, y2);
		
		g.setFont(menuBarFont);
		
		int textY = (y1+y2-menuBarFont.getHeight())/2+2;
		
		boolean menuItemsVisible = false;
		if (leftMenu != null)
		{
			if (uiState == UI_STATE_LEFT_MENU_VISIBLE)
			{
				menuItemsVisible = true;
				drawRect(g, transformColorLight(capBkCOlor, -64), transformColorLight(capBkCOlor, -32), 0, y1, width/2, y2);
			}
			String text = leftMenu.getLabel();
			g.setColor(capTxtColor);
			g.drawString(text, layer, textY, Graphics.TOP|Graphics.LEFT);
			if (leftMenu.getCommandType() == Command.OK) defaultMenu = true;   
		}
		
		if (rightMenu != null)
		{
			String text = rightMenu.getLabel();
			if (uiState == UI_STATE_RIGHT_MENU_VISIBLE)
			{
				menuItemsVisible = true;
				drawRect(g, transformColorLight(capBkCOlor, -64), transformColorLight(capBkCOlor, -32), width/2, y1, width, y2);
			}
			
			g.setColor(capTxtColor);
			g.drawString
			(
				text, 
				width-layer-menuBarFont.stringWidth(text), 
				textY, 
				Graphics.TOP|Graphics.LEFT
			);
			if (rightMenu.getCommandType() == Command.OK) defaultMenu = true;
		}
		
		if (defaultMenu && !menuItemsVisible)
		{
			String text = "v";
			g.setColor(capTxtColor);
			g.drawString
			(
				text, 
				(width-menuBarFont.stringWidth(text))/2, 
				textY, 
				Graphics.TOP|Graphics.LEFT
			);
		}
		
		g.setColor(transformColorLight(capBkCOlor, -128));
		g.drawLine(0, y1, width, y1);
	}
	
	private Command findMenuByType(int type)
	{
		if ((leftMenu != null) && (leftMenu.getCommandType() == type)) return leftMenu;
		
		if ((rightMenu != null) && (rightMenu.getCommandType() == type)) return rightMenu;
		
		for (int i = leftMenuItems.size()-1; i >= 0; i--)
		{
			Command cmd = (Command)leftMenuItems.elementAt(i); 
			if (cmd.getCommandType() == type) return cmd; 
		}
		
		for (int i = rightMenuItems.size()-1; i >= 0; i--)
		{
			Command cmd = (Command)rightMenuItems.elementAt(i); 
			if (cmd.getCommandType() == type) return cmd; 
		}
		
		return null; 
	}
	
	private boolean exMenuExists()
	{
		return (leftMenu != null) || (rightMenu != null);
	}
	
	private int getMenuBarHeight()
	{
		return exMenuExists() ? menuBarFont.getHeight()+3 : 0;
	}
	
	public void addCommandEx(Command cmd, int type)
	{
		switch (type)
		{
		case MENU_TYPE_LEFT_BAR:
			leftMenu = cmd;
			break;
			
		case MENU_TYPE_RIGHT_BAR:
			rightMenu = cmd;
			break;
			
		case MENU_TYPE_LEFT:
			leftMenuItems.addElement(cmd);
			break;
			
		case MENU_TYPE_RIGHT:
			rightMenuItems.addElement(cmd);
			break;
		}
		invalidate();
	}
	
	public void removeCommandEx(Command cmd)
	{
		if (cmd == leftMenu)
		{
			leftMenu = null;
			leftMenuItems.removeAllElements();
		}
		
		if (cmd == rightMenu)
		{
			rightMenu = null;
			rightMenuItems.removeAllElements();
		}
		
		leftMenuItems.removeElement(cmd);
		rightMenuItems.removeElement(cmd);
	}
	
	private void drawMenuItems(Graphics g, int menuBarHeight)
	{
		switch (uiState)
		{
		case UI_STATE_LEFT_MENU_VISIBLE:
			drawManuItems(g, leftMenuItems, getHeightInternal()-menuBarHeight, Graphics.LEFT);
			break;
			
		case UI_STATE_RIGHT_MENU_VISIBLE:
			drawManuItems(g, rightMenuItems, getHeightInternal()-menuBarHeight, Graphics.RIGHT);
			break;
		}
	}
	
	private void drawManuItems(Graphics g, Vector items, int bottom, int horizAlign)
	{
		int fontHeight = menuItemsFont.getHeight(); 
		int layer = fontHeight/3;
		
		// calculate width and height
		int width = 0;
		int height = layer*2;
		for (int i = items.size()-1; i >= 0; i--)
		{
			Command cmd = (Command)items.elementAt(i);
			int txtWidth = menuItemsFont.stringWidth(cmd.getLabel());
			if (txtWidth > width) width = txtWidth;
			height += fontHeight;
		}
		width += layer*2;
		
		int y = bottom-height;
		int x = 0;
		switch (horizAlign)
		{
		case Graphics.LEFT:
			x = 2;
			break;
		case Graphics.RIGHT:
			x = getWidthInternal()-width-2;
			break;
		}
		
		// Draw rectangle
		drawRect(g, transformColorLight(bkgrndColor, -12), transformColorLight(bkgrndColor, -32), x, y, x+width, y+height);
		g.setColor(transformColorLight(bkgrndColor, -128));
		g.drawRect(x, y, width, height);
		
		// Draw items
		g.setFont(menuItemsFont);
		
		int itemY = y+layer;
		
		for (int i = 0; i < items.size(); i++)
		{
			if (i == curMenuItemIndex)
			{
				g.setColor(capTxtColor);
				g.fillRect(x, itemY-1, width+1, fontHeight+2);
			}
			itemY += fontHeight;
		}
		
		itemY = y+layer;
		for (int i = 0; i < items.size(); i++)
		{
			Command cmd = (Command)items.elementAt(i);
			g.setColor((i == curMenuItemIndex) ? getInverseColor(capTxtColor) : capTxtColor);
			g.drawString(cmd.getLabel(), x+layer, itemY, Graphics.LEFT|Graphics.TOP);
			itemY += fontHeight;
		}
	}

	//#sijapp cond.if target="MOTOROLA"#
	//#	private static boolean bklt_on = true;
	//#	private static java.util.Timer switchoffTimer;
	//#
	//#	public static void setBkltOn(boolean on)
	//#	{
	//#		if (on != bklt_on)
	//#		{
	//#			bklt_on = on;
	//#			Jimm.display.flashBacklight(bklt_on ? Integer.MAX_VALUE : 1);
	//#		}
	//#	}
	//#	public static void flashBklt(int msec)
	//#	{
	//#		try
	//#		{
	//#			setBkltOn(true);
	//#	
	//#			if (switchoffTimer != null)
	//#			{
	//#				switchoffTimer.cancel();
	//#			}
	//#
	//#			(switchoffTimer = new java.util.Timer()).schedule(new jimm.TimerTasks(jimm.TimerTasks.VL_SWITCHOFF_BKLT), msec);
	//#		}
	//#		catch (Exception e) {}
	//#	}
	//#	protected void hideNotify()
	//#	{
	//#		if (!Options.getBoolean(Options.OPTION_LIGHT_MANUAL) & !(Jimm.display.getCurrent() instanceof Canvas))
	//#		{
	//#			if (switchoffTimer != null) switchoffTimer.cancel();
	//#			setBkltOn(true);
	//#		}
	//#	}
	//#
	//#	public static final int BKLT_TYPE_BLINKING = 1;
	//#	public static final int BKLT_TYPE_LIGHTING = 2;
	//#
	//#	private static java.util.Timer ledTimer;
	//#	private static Region[] currentRegions;
	//#	public static void setLEDmode(int type, int duration, int color)
	//#	{
	//#		int t = Jimm.funlight_device_type;
	//#		if ((t == -1) | !Options.getBoolean(Options.OPTION_FLASH_BACKLIGHT))
	//#		{
	//#			return;
	//#		}
	//#		disableLED();
	//#		Region[] regions = null;
	//#		switch (t)
	//#		{
	//#			case Jimm.FUNLIGHT_DEVICE_E390:
	//#				regions = new Region[]
	//#				{
	//#					FunLight.getRegion(3),
	//#					FunLight.getRegion(4)
	//#				};
	//#				break;
	//#			case Jimm.FUNLIGHT_DEVICE_E380:
	//#				regions = new Region[]
	//#				{
	//#					FunLight.getRegion(4),
	//#					null
	//#				};
	//#				break;
	//#		}
	//#		currentRegions = regions;
	//#		switch (type)
	//#		{
	//#			case BKLT_TYPE_LIGHTING:
	//#				regions[0].setColor(color);
	//#				if (regions[1] != null)
	//#				{
	//#					regions[1].setColor(color);
	//#					regions[1].getControl();
	//#				}
	//#				regions[0].getControl();
	//#				if (duration >= 200)
	//#				{
	//#					(ledTimer = new java.util.Timer()).schedule(new jimm.TimerTasks(jimm.TimerTasks.VL_SWITCHOFF_LED), duration);
	//#				}
	//#				break;
	//#			default:
	//#				regions[0].setColor(color);
	//#				if (regions[1] != null) regions[1].setColor(color);
	//#				int tries = duration / 250;
	//#				(ledTimer = new java.util.Timer())
	//#					.schedule(new jimm.TimerTasks(jimm.TimerTasks.VL_LED_CHANGE_STATE, regions, tries), 0, 250);
	//#				break;
	//#		}
	//#	}
	//#	public static void disableLED()
	//#	{
	//#		if (ledTimer != null)
	//#		{
	//#			ledTimer.cancel();
	//#			ledTimer = null;
	//#		}
	//#
	//#		if (currentRegions != null)
	//#		{
	//#			currentRegions[0].releaseControl();
	//#			if (currentRegions[1] != null) currentRegions[1].releaseControl();
	//#			currentRegions = null;
	//#		}
	//#	}
	//#sijapp cond.end#	
}