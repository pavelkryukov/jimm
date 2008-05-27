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
import java.lang.Math;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import DrawControls.ListItem;
import DrawControls.VirtualListCommands;

class VirtualCanvas extends Canvas implements Runnable
{
	VirtualList currentControl;
	private Timer repeatTimer = new Timer();
	private TimerTask timerTask;
	private int lastKeyKode;
	private Display display;
	
	
	public void setDisplay(Display display)
	{
		this.display = display;
	}
	
	public Display getDisplay()
	{
		return display;
	}
	
	public VirtualCanvas()
	{
		//#sijapp cond.if target="MIDP2" | target="MOTOROLA" | target="SIEMENS2"#
		setFullScreenMode(true);
		//#sijapp cond.elseif target="RIM"#
		setFullScreenMode(false);
		//#sijapp cond.end#
	}
	
	protected void paint(Graphics g)
	{
		if (currentControl != null) currentControl.paint(g);
	}
	
	protected void showNotify()
	{
		cancelKeyRepeatTask();
		//#sijapp cond.if target="MIDP2" | target="MOTOROLA" | target="SIEMENS2"#
		setFullScreenMode(true);
		//#sijapp cond.elseif target="RIM"#
		setFullScreenMode(false);
		//#sijapp cond.end#
		if (currentControl != null) currentControl.showNotify();
	}
	
	protected void hideNotify()
	{
		cancelKeyRepeatTask();
//#sijapp cond.if target!="RIM"#
		currentControl.resetUiState();
//#sijapp cond.end#		
		currentControl.onHide();
	}
	
	public void run()
	{
		if (timerTask == null) return;
		currentControl.keyRepeated(lastKeyKode);
	}

	protected void keyPressed(int keyCode)
	{
		cancelKeyRepeatTask();
		if (currentControl != null) currentControl.keyPressed(keyCode);
		lastKeyKode = keyCode;
		timerTask = new TimerTask() {
			public void run()
			{
				display.callSerially(VirtualCanvas.this);
			}
		};
		repeatTimer.schedule(timerTask, 500, 50);
	}

	protected void keyReleased(int keyCode)
	{
		if (currentControl != null) currentControl.keyReleased(keyCode);
		cancelKeyRepeatTask();
	}
	
	void cancelKeyRepeatTask()
	{
		if (timerTask != null) timerTask.cancel();
		lastKeyKode = 0;
		timerTask = null;
	}
	
	//#sijapp cond.if target is "MIDP2"#
	protected void pointerDragged(int x, int y)
	{
		if (currentControl != null) currentControl.pointerDragged(x, y); 
	}
	
	protected void pointerPressed(int x, int y)
	{
		if (currentControl != null) currentControl.pointerPressed(x, y); 
	}
	
	protected void pointerReleased(int x, int y)
	{
		if (currentControl != null) currentControl.pointerReleased(x, y);
	}
	//#sijapp cond.end#
}

public abstract class VirtualList
{
	private static VirtualCanvas virtualCanvas = new VirtualCanvas();  
	
	/*! Use dotted mode of cursor. If item of list 
	 is selected, dotted rectangle drawn around  it*/
	public final static int CURSOR_MODE_ENABLED = 2;

	/*! Does't show cursor at selected item. */
	public final static int CURSOR_MODE_DISABLED = 3;

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
	private static Font capAndMenuFont;
	
	// Commands to react to VL events
	private VirtualListCommands vlCommands;

	// Caption of VL
	private String caption;
	
	static private String bottomText;

	// Used by "Invalidate" method to prevent invalidate when locked 
	private boolean dontRepaint = false;

	// Index for current item of VL
	protected int currItem = 0;
	
	protected boolean cyclingCursor = false;

	// Used for passing params of items when painting 
	final static protected ListItem paintedItem;

	// Used to catch changes to repaint data
	private int lastCurrItem = 0, lastTopItem = 0;

	private boolean fullScreen = false;

	private Image capImage;
	
	private static final int KEY_CODE_LEFT_MENU = 1000001;
	private static final int KEY_CODE_RIGHT_MENU = 1000002;
	private static final int KEY_CODE_BACK_BUTTON = 1000003;
	private static final int KEY_CODE_UNKNOWN = 1000004;
	
	private static int curMenuItemIndex; 
	
	private static boolean mirrorMenu = false;
	protected int borderWidth = 0;
	protected int curFrameWidth = 1;
	private int topItem = 0; // Index of top visilbe item 
	private int fontSize = MEDIUM_FONT; // Current font size of VL
	private int bkgrndColor = 0xFFFFFF; // bk color of VL
	private int cursorColor = 0x808080; // Used when drawing focus rect.
	private int cursorFrameColor = 0xFF; // Used when drawing focus rect.
	private int textColor = 0x000000; // Default text color.
	private int capBkCOlor = 0xC0C0C0;
	private int capTxtColor = 0x00; // Color of caprion text
	private int cursorMode = CURSOR_MODE_ENABLED; // Cursor mode

	static
	{
		capAndMenuFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
		int width = capAndMenuFont.getHeight() / 3;
		scrollerWidth = width > 4 ? width : 4;
		paintedItem = new ListItem();
	}

	/**
	 * Sets mirror-style for menues. If value is true, left and right menus is changed over 
	 * @param value values for mirror-style of menus
	 */
	public static void setMirrorMenu(boolean value)
	{
		mirrorMenu = value;
	}
	
	/**
	 * Sets display reference for all VirtualList's instances. You have to set display
	 * before usage of VirtualList objects
	 * @param display reference to midlet's display
	 */
	static public void setDisplay(Display display)
	{
		virtualCanvas.setDisplay(display);
	}
	
	public void setFullScreen(boolean value)
	{
		if (fullScreen == value) return;
		fullScreen = value;
		if (isActive()) virtualCanvas.repaint();
	}
	
	public static void setFullScreenForCurrent(boolean value)
	{
		if (virtualCanvas.currentControl != null) 
			virtualCanvas.currentControl.setFullScreen(value);
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
		
		this.cursorMode = CURSOR_MODE_ENABLED;
		initVirtualList();
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
		this.cursorMode = cursorMode;
		initVirtualList();
	}
	
	private void initVirtualList()
	{
		createSetOfFonts(this.fontSize);
		int fontHeight = normalFont.getHeight();
		curFrameWidth = (fontHeight > 16) ? 2 : 1;
		borderWidth = fontHeight/6+1;
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
		int menuBartHeight;
//#sijapp cond.if target!="RIM"#		
		menuBartHeight = getMenuBarHeight();
//#sijapp cond.else#
		menuBartHeight = 0;
//#sijapp cond.end#
		
		return getHeightInternal() - getCapHeight() - menuBartHeight;
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
	
	public void setCyclingCursor(boolean value)
	{
		cyclingCursor = value;
	}
	
	public int getGameAction(int keyCode)
	{
		return virtualCanvas.getGameAction(keyCode);
	}
	
	public void repaint()
	{
		if (isActive()) virtualCanvas.repaint();
	}

	public void setCapImage(Image image)
	{
		if (capImage == image) return;
		capImage = image;
		invalidate(0, 0, getWidth(), getCapHeight());
	}

	public void setVLCommands(VirtualListCommands vlCommands)
	{
		this.vlCommands = vlCommands;
	}
	
	public static VirtualList getCurrent()
	{
		return virtualCanvas.isShown() ? virtualCanvas.currentControl : null; 
	}

	public void setColors(int capTxt, int capbk, int bkgrnd, int cursor, int text, int crsFrame)
	{
		capBkCOlor = capbk;
		capTxtColor = capTxt;
		bkgrndColor = bkgrnd;
		cursorColor = cursor;
		textColor = text;
		cursorFrameColor = crsFrame;
		if (isActive()) virtualCanvas.repaint();
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
	
	public int getBkgrndColor()
	{
		return bkgrndColor;
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
	public void setMode(int value)
	{
		if (cursorMode == value) return;
		cursorMode = value;
		invalidate();
	}

	public int getCursorMode()
	{
		return cursorMode;
	}
	
	public boolean isActive()
	{
		return (virtualCanvas.currentControl == this) && virtualCanvas.isShown();
	}
	
	private void doActivate(Display display, Alert alert)
	{
		if (isActive()) return;
		
		if (virtualCanvas.currentControl != null)
		{
//#sijapp cond.if target="RIM"#
			for (int i = virtualCanvas.currentControl.commands.size()-1; i >= 0; i--)
				virtualCanvas.removeCommand((Command)virtualCanvas.currentControl.commands.elementAt(i));
//#sijapp cond.end#
			virtualCanvas.currentControl.onHide();
		}
		
//#sijapp cond.if target="RIM"#
		for (int i = commands.size()-1; i >= 0; i--)
			virtualCanvas.addCommand((Command)commands.elementAt(i));
//#sijapp cond.else#
		resetUiState();
//#sijapp cond.end#
		
		virtualCanvas.currentControl = this;
		virtualCanvas.cancelKeyRepeatTask();
		
		virtualCanvas.setCommandListener(commandListener);
		
		if (alert != null) display.setCurrent(alert, virtualCanvas);
		else display.setCurrent(virtualCanvas);

		repaint();
		onShow();
//#sijapp cond.if target="MOTOROLA" | target="MIDP2"#
		setBackLightOn();
//#sijapp cond.end#
	}
	
	public void activate(Display display)
	{
		doActivate(display, null);
	}
	
	public void activate(Display display, Alert alert)
	{
		doActivate(display, alert);
	}

	protected void showNotify()
	{
		virtualCanvas.setCommandListener(commandListener);
		forcedHeight = forcedWidth = -1;
//#sijapp cond.if target!="RIM"#		
		uiState = UI_STATE_NORMAL;
//#sijapp cond.end#		
		onShow();
	}

	private static int maxInt(int value1, int value2)
	{
		return (value1 > value2) ? value1 : value2;
	}

	//! Returns height of each item in list
	public int getItemHeight(int itemIndex)
	{
		int imgHeight = 0, fontHeight = getFontHeight();
		paintedItem.clear();
		get(itemIndex, paintedItem);
		if (paintedItem.leftImage != null) 
			imgHeight = maxInt(imgHeight, paintedItem.leftImage.getHeight());
		else if (paintedItem.secondLeftImage != null) 
			imgHeight = maxInt(imgHeight, paintedItem.secondLeftImage.getHeight());
		else if (paintedItem.rightImage != null) 
			imgHeight = maxInt(imgHeight, paintedItem.secondLeftImage.getHeight());
		else 
			imgHeight = 0;
		return (fontHeight > imgHeight) ? fontHeight : imgHeight;
	}

	// protected void invalidate()  
	protected void invalidate()
	{
		if (dontRepaint) return;
		if (isActive()) virtualCanvas.repaint();
	}
	
	protected void invalidate(int x1, int y1, int x2, int y2)
	{
		if (dontRepaint) return;
		if (isActive()) virtualCanvas.repaint(x1, y1, x2-x1, y2-y1);
	}

	// protected void checkCurrItem()
	protected void checkCurrItem()
	{
		int size = getSize();
		if (cyclingCursor)
		{
			if (currItem < 0) currItem = size - 1;
			else if (currItem >= size) currItem = 0;
		}
		else
		{
			if (currItem < 0) currItem = 0;
			else if (currItem >= size) currItem = size-1;
		}
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
		if ((lastCurrItem != currItem) || (lastTopItem != topItem)) invalidate();
		if ((lastCurrItem != currItem) && (vlCommands != null)) vlCommands.vlCursorMoved(this);
	}

	// protected void moveCursor(int step)
	protected void moveCursor(int step, boolean moveTop)
	{
		storelastItemIndexes();
		if (moveTop && (cursorMode == CURSOR_MODE_DISABLED)) topItem += step;
		currItem += step;
		checkCurrItem();
		checkTopItem();
		repaintIfLastIndexesChanged();
	}

	protected boolean itemSelected()
	{
		return executeCommand(findMenuByType(Command.OK));
	}
	
	// private keyReaction(int keyCode)
	private void keyReaction(int keyCode, int type)
	{
//#sijapp cond.if target!="RIM"#
		boolean menuItemsVisible = false;
		int lastMenuIndex = curMenuItemIndex;
		Vector menuItemsData = null, clickedMenuItems = null;
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
				
		int lastUIState = uiState;
//#sijapp cond.end#		
		
		switch (getExtendedGameAction(keyCode))
		{
//#sijapp cond.if target!="RIM"#		
		case KEY_CODE_LEFT_MENU:
			if (type == KEY_PRESSED) clickedMenuItems = leftMenuPressed();
			break;
			
		case KEY_CODE_RIGHT_MENU:
			if (type == KEY_PRESSED) clickedMenuItems = rightMenuPressed();
			break;
//#sijapp cond.end#			
			
		case KEY_CODE_BACK_BUTTON:
			if (type == KEY_PRESSED)
			{
//#sijapp cond.if target!="RIM"#				
				switch (uiState)
				{

				case UI_STATE_RIGHT_MENU_VISIBLE:
				case UI_STATE_LEFT_MENU_VISIBLE:
					uiState = UI_STATE_NORMAL;
					invalidate();
					break;
				default:
					Command backMenu = findMenuByType(Command.BACK);
					if (backMenu != null && executeCommand(backMenu)) return;
					break;
				}
//#sijapp cond.end#				
				Command backMenu = findMenuByType(Command.BACK);
				if (backMenu != null && executeCommand(backMenu)) return;
			}
			break;

		case Canvas.DOWN:
//#sijapp cond.if target!="RIM"#			
			if (menuItemsVisible) moveSelectedMenuItem(1, menuItemsData.size(), false);
			else moveCursor(1, false);
//#sijapp cond.else#
			moveCursor(1, false);
//#sijapp cond.end#			
			break;
			
		case Canvas.UP:
//#sijapp cond.if target!="RIM"#			
			if (menuItemsVisible) moveSelectedMenuItem(-1, menuItemsData.size(), false);
			else moveCursor(-1, false);
//#sijapp cond.else#
			moveCursor(-1, false);
//#sijapp cond.end#			
			break;
			
		case Canvas.FIRE:
			if (type == KEY_PRESSED)
			{
//#sijapp cond.if target!="RIM"#				
				if ((keyCode == KEY_CODE_LEFT_MENU) || (keyCode == KEY_CODE_RIGHT_MENU)) return;
				if (menuItemsVisible)
				{
					uiState = UI_STATE_NORMAL;
					executeCommand((Command)menuItemsData.elementAt(curMenuItemIndex));
					invalidate();
				}
				else
				{
					boolean executed = itemSelected();
					if (!executed && (vlCommands != null)) vlCommands.vlItemClicked(this);
				}
//#sijapp cond.else#
				boolean executed = itemSelected();
				if (!executed && (vlCommands != null)) vlCommands.vlItemClicked(this);
//#sijapp cond.end#				
			}
			break;
		}
		
//#sijapp cond.if target!="RIM"#		
		initPopupMenuItems(clickedMenuItems);
		
		if ((menuItemsVisible && (lastMenuIndex != curMenuItemIndex)) || (lastUIState != uiState))
		{
			invalidate();
			return;
		}
//#sijapp cond.end#
		
		if (type == KEY_PRESSED)
		{
			switch (keyCode)
			{
			case Canvas.KEY_NUM1:
				storelastItemIndexes();
				currItem = topItem = 0;
				repaintIfLastIndexesChanged();
				break;

			case Canvas.KEY_NUM7:
				storelastItemIndexes();
				int endIndex = getSize() - 1;
				currItem = endIndex;
				checkTopItem();
				repaintIfLastIndexesChanged();
				break;

			case Canvas.KEY_NUM3:
				moveCursor(-getVisCount(), false);
				break;

			case Canvas.KEY_NUM9:
				moveCursor(getVisCount(), false);
				break;

//#sijapp cond.if target="MOTOROLA"#
			case Canvas.KEY_STAR:
				getDisplay().flashBacklight(backLightIsOn ? 1 : Integer.MAX_VALUE);
				backLightIsOn = !backLightIsOn;
				break;
//#sijapp cond.end#
			}
		}
	}
	
	protected Display getDisplay()
	{
		return virtualCanvas.getDisplay();
	}
	
	protected Canvas getCanvas()
	{
		return virtualCanvas;
	}

	public void doKeyreaction(int keyCode, int type)
	{
		switch (type)
		{
		case KEY_PRESSED:
//#sijapp cond.if target="MOTOROLA" | target="MIDP2"#
			setBackLightOn();
//#sijapp cond.end#
			keyReaction(keyCode, type);
			break;
		case KEY_REPEATED:
			keyReaction(keyCode, type);
			break;
		}

		if (vlCommands != null) vlCommands.vlKeyPress(this, keyCode, type);
	}
	
	// Return game action or extended codes
	// Thanks for Aspro for source examples
	private int getExtendedGameAction(int keyCode)
	{
		try
		{
			int gameAct = virtualCanvas.getGameAction(keyCode);
			switch (gameAct)
			{
			case Canvas.UP:
			case Canvas.DOWN:
			case Canvas.LEFT:
			case Canvas.RIGHT:
				return gameAct;
			}
		}
		catch (Exception e) {}
		
		String strCode = null;

		try
		{
			strCode = virtualCanvas.getKeyName(keyCode).toLowerCase();
		}
		catch (IllegalArgumentException e) {}
		
		if (strCode != null)
		{
			if ("soft1".equals(strCode) || "soft 1".equals(strCode) || "soft_1".equals(strCode) || "softkey 1".equals(strCode) || "sk2(left)".equals(strCode)
					|| strCode.startsWith("left soft")) return KEY_CODE_LEFT_MENU;

			if ("soft2".equals(strCode) || "soft 2".equals(strCode) || "soft_2".equals(strCode) || "softkey 4".equals(strCode) || "sk1(right)".equals(strCode)
					|| strCode.startsWith("right soft")) return KEY_CODE_RIGHT_MENU;

			if ("on/off".equals(strCode) || "back".equals(strCode)) return KEY_CODE_BACK_BUTTON;
			
//#sijapp cond.if target="RIM"#			
			if ("trackball".equals(strCode) || "enter".equals(strCode)) return Canvas.FIRE;
//#sijapp cond.end#			
		}

		switch (keyCode)
		{
//#sijapp cond.if target is "MIDP2"#
		case -6:
//#sijapp cond.end#
		case -21:
		case 21:
		case 105:
		case -202:
		case 113:
		case 57345:
			return KEY_CODE_LEFT_MENU;
			
//#sijapp cond.if target is "MIDP2"#
		case -7:
//#sijapp cond.end#
		case -22:
		case 22:
		case 106:
		case -203:
		case 112:
		case 57346:
			return KEY_CODE_RIGHT_MENU;

//#sijapp cond.if target isnot "SIEMENS2"#
		case -11:
			return KEY_CODE_BACK_BUTTON;
//#sijapp cond.end#
		}

		try
		{
			int gameAct = virtualCanvas.getGameAction(keyCode);
			if (gameAct > 0) return gameAct;
		}
		catch (Exception e) {}

		return KEY_CODE_UNKNOWN;
	}

	protected void keyPressed(int keyCode)
	{
		doKeyreaction(keyCode, KEY_PRESSED);
	}
	
	private CommandListener commandListener;
	public void setCommandListener(CommandListener l)
	{
		commandListener = l;
		if (isActive()) virtualCanvas.setCommandListener(commandListener);
	}
	
	protected boolean executeCommand(Command command)
	{
		if ((commandListener != null) && (command != null))
		{
			commandListener.commandAction(command, null);
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

	protected boolean pointerPressedOnUtem(int index, int x, int y, int mode)
	{
		return false;
	}

	static int abs(int value)
	{
		return (value < 0) ? -value : value;
	}

	protected void pointerPressed(int x, int y)
	{
		// is pointing on scroller
		if (x >= (getWidthInternal() - 3 * scrollerWidth))
		{
			if ((srcollerY1 <= y) && (y < srcollerY2))
			{
				lastPointerYCrd = y;
				lastPointerTopItem = topItem;
				return;
			}
		}
		lastPointerTopItem = -1;
		
		int mode = DMS_CLICK;
		long time = System.currentTimeMillis();
		
		if (((time - lastPointerTime) < 500) && 
				(abs(x - lastPointerXCrd) < 10) && 
				(abs(y - lastPointerYCrd) < 10)) mode = DMS_DBLCLICK;
		
		if (bDIimage == null) bDIimage = Image.createImage(getWidthInternal(), getHeightInternal());
		paintAllOnGraphics(bDIimage.getGraphics(), mode, x, y);
		
		lastPointerXCrd = x;
		lastPointerYCrd = y;
		lastPointerTime = time;
	}
	
	protected void pointerReleased(int x, int y)
	{
		
	}

	//#sijapp cond.end#
	
	static public void setBottomText(String text)
	{
		if ((bottomText != null) && bottomText.equals(text)) return;
		bottomText = text;
		if ((virtualCanvas != null) && virtualCanvas.isShown()) virtualCanvas.repaint();
	}

	//! Set caption text for list
	public void setCaption(String capt)
	{
		if ((caption != null) && (caption.equals(capt))) return;
		caption = capt;
//#sijapp cond.if target="MIDP2" | target="MOTOROLA" | target="SIEMENS2" | target="RIM"#
		if (!fullScreen) invalidate(0, 0, getWidth(), getCapHeight());
//#sijapp cond.else#
		invalidate(0, 0, getWidth(), getCapHeight());
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
	protected final int getCapHeight()
	{
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2" | target="RIM"#
		if (fullScreen) return curFrameWidth;
		//#sijapp cond.end#
		int capHeight = 0;
		if (caption != null) capHeight = capAndMenuFont.getHeight() + 2;
		if (capImage != null)
		{
			int imgHeight = capImage.getHeight() + 2;
			if (imgHeight > capHeight) capHeight = imgHeight;
		}

		return capHeight + 1;
	}
	
	protected void afterDrawCaption(Graphics g, int height) {}

	// private int drawCaption(Graphics g)
	protected int drawCaption(Graphics g, int mode, int curX, int curY)
	{
		if (caption == null) return 0;
		
		int width = getWidthInternal();
		
		//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		if (fullScreen)
		{
			if (mode == DMS_DRAW)
			{
				g.setColor(bkgrndColor);
				g.fillRect(0, 0, width, curFrameWidth);
			}
			return curFrameWidth;
		}
		//#sijapp cond.end#
		
		if (mode != DMS_DRAW) return getCapHeight();

		
		g.setFont(capAndMenuFont);
		int height = getCapHeight();
		drawRect(g, capBkCOlor, transformColorLight(capBkCOlor, -64), 0, 0, width, height);

		g.setColor(transformColorLight(capBkCOlor, -128));
		g.drawLine(0, height - 1, width, height - 1);

		int x = 2;

		if (capImage != null)
		{
			g.drawImage(capImage, x, (height - capImage.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
			x += capImage.getWidth() + 1;
		}

		g.setColor(capTxtColor);
		g.drawString(caption, x, (height - capAndMenuFont.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
		
		afterDrawCaption(g, height);
		
		return height;
	}

	protected boolean isItemSelected(int index)
	{
		return ((currItem == index) && (cursorMode != CURSOR_MODE_DISABLED));
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
		int color = transformColorLight(bkgrndColor, -16);
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
			color = capBkCOlor;
			g.setColor(color);
			g.fillRect(width+1, srcollerY1+1, scrollerWidth-2, srcollerY2-srcollerY1-2);
			g.setColor(mergeColors(color, 0, 70));
			g.drawRect(width, srcollerY1, scrollerWidth - 1, srcollerY2 - srcollerY1 - 1);
			g.setColor(transformColorLight(color, 96));
			g.drawLine(width+1, srcollerY1+1, width+1, srcollerY2-3);
			g.drawLine(width+1, srcollerY1+1, width+scrollerWidth-2, srcollerY1+1);
		}
	}

	static protected void drawRect(Graphics g, int color1, int color2, int x1, int y1, int x2, int y2)
	{
		int r1 = ((color1 & 0xFF0000) >> 16);
		int g1 = ((color1 & 0x00FF00) >> 8);
		int b1 = (color1 & 0x0000FF);
		int r2 = ((color2 & 0xFF0000) >> 16);
		int g2 = ((color2 & 0x00FF00) >> 8);
		int b2 = (color2 & 0x0000FF);
		
		int count = (y2-y1)/3;
		if (count < 0) count = -count;
		if (count < 8) count = 8;
		
		y2++;
		x2++;

		for (int i = 0; i < count; i++)
		{
			int crd1 = i * (y2 - y1) / count + y1;
			int crd2 = (i + 1) * (y2 - y1) / count + y1;
			if (crd1 == crd2) continue;
			g.setColor(i * (r2 - r1) / (count-1) + r1, i * (g2 - g1) / (count-1) + g1, i * (b2 - b1) / (count-1) + b1);
			g.fillRect(x1, crd1, x2-x1, crd2-crd1);
		}
	}

	//! returns font height
	public int getFontHeight()
	{
		return getQuickFont(Font.STYLE_PLAIN).getHeight();
	}
	
	static private int[] curXVals = new int[2];  
	protected void getCurXVals(int[] values)
	{
		values[0] = curFrameWidth+1;
		values[1] = getWidthInternal() - scrollerWidth - curFrameWidth;
	}

	protected final boolean drawItems
	(
		Graphics g, 
		int topY, 
		int fontHeight, 
		int menuBarHeight, 
		int mode, 
		int mouseX, 
		int mouseY, 
		int clipY1, 
		int clipY2
	)
	{
		int grCursorY1 = -1, grCursorY2 = -1;
		int height = getDrawHeight();
		int size = getSize();
		int i, y;
		int itemWidth = getWidthInternal() - scrollerWidth;
		int bottomY = topY+height;
		
		if ((mode == DMS_DRAW) && (!crdIntersect(topY, bottomY, clipY1, clipY2))) return false;
		
		if (mode == DMS_DRAW)
		{
			//System.out.println("VirtualList.drawItems");
			
			getCurXVals(curXVals);
			int curX1 = curXVals[0];
			int curX2 = curXVals[1];
			
			// Fill background
			g.setColor(bkgrndColor);
			int realY1 = Math.max(topY, clipY1);
			int realY2 = Math.min(bottomY, clipY2); 
			g.fillRect(0, realY1, itemWidth, realY2-realY1);

			// Draw cursor
			y = topY;
			for (i = topItem; i < size; i++)
			{
				int itemHeight = getItemHeight(i);
				if (isItemSelected(i))
				{
					if (grCursorY1 == -1) grCursorY1 = y;
					grCursorY2 = y + itemHeight;
				}
				y += itemHeight;
				if (y >= bottomY) break;
			}

			if ((grCursorY1 != -1) && crdIntersect(grCursorY1-3, grCursorY2+2, clipY1, clipY2))
			{
				grCursorY1--;
				g.setColor(cursorColor);
				g.fillRect(curX1, grCursorY1, curX2-curX1, grCursorY2-grCursorY1);
				g.setColor(cursorFrameColor);
				boolean isCursorUpper = (topItem >= 1) ? isItemSelected(topItem - 1) : false;
				
				if (!isCursorUpper)
					g.fillRect(curX1-curFrameWidth+1, grCursorY1-curFrameWidth+1, curX2-curX1+2*curFrameWidth-2, curFrameWidth);
				
				g.fillRect(curX1-curFrameWidth, grCursorY1-curFrameWidth+2, curFrameWidth, grCursorY2-grCursorY1+2*curFrameWidth-3);
				
				g.fillRect(curX2, grCursorY1-curFrameWidth+2, curFrameWidth, grCursorY2-grCursorY1+2*curFrameWidth-3);
				
				g.fillRect(curX1-curFrameWidth+1, grCursorY2, curX2-curX1+2*curFrameWidth-2, curFrameWidth);
				
				g.setColor(mergeColors(cursorFrameColor, cursorColor, 50));
				if (!isCursorUpper)
				{
					g.fillRect(curX1, grCursorY1+1, 1, 1);
					g.fillRect(curX1, grCursorY2-1, 1, 1);
				}
				
				g.fillRect(curX2-1, grCursorY1+1, 1, 1);
				g.fillRect(curX2-1, grCursorY2-1, 1, 1);
			}
		}

		// Draw items
		paintedItem.clear();
		y = topY;
		for (i = topItem; i < size; i++)
		{
			int itemHeight = getItemHeight(i);
			if (g != null) g.setStrokeStyle(Graphics.SOLID);
			
			int x1 = borderWidth;
			int x2 = itemWidth-2;
			int y1 = y;
			int y2 = y + itemHeight;
			if ((mode == DMS_DRAW || mode == DMS_CUSTOM)/* && */)
			{
				if (crdIntersect(y1, y2, clipY1, clipY2))
					drawItemData(g, i, x1, y1, x2, y2, fontHeight, mode);
			}
			
			//#sijapp cond.if target is "MIDP2"#
			else
			{
				if ((y1 < mouseY) && (mouseY < y2) && (x1 < mouseX) && (mouseX < x2))
				{
					switch (mode)
					{
					case DMS_CLICK:
						if (currItem != i)
						{
							currItem = i;
							if (vlCommands != null) vlCommands.vlCursorMoved(this);
							invalidate();
						}
						break;
						
					case DMS_DBLCLICK:
						itemSelected();
						break;
					}

					pointerPressedOnUtem(i, mouseX-x1, mouseY-y1, mode);
					return true;
				}
			}
			//#sijapp cond.end#
			
			y += itemHeight;
			if (y >= bottomY) break;
		}

		return false;
	}
	
	static protected boolean crdContains(int crd, int crd1, int crd2)
	{
		return (crd1 <= crd) && (crd <= crd2);
	}
	
	static protected boolean crdIntersect(int a1, int a2, int b1, int b2)
	{
		if ((a1 == -1) || (a2 == -1) || (b1 == -1) || (b2 == -1)) return true;
		return crdContains(a1, b1, b2) || crdContains(a2, b1, b2) || crdContains(b1, a1, a2) || crdContains(b2, a1, a2);  
	}

	void destroy()
	{
	}

	// change light of color 
	static protected int transformColorLight(int color, int light)
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
	
	static protected int mergeColors(int color1, int color2, int value)
	{
		int r1 = (color1 & 0xFF);
		int g1 = ((color1 & 0xFF00) >> 8);
		int b1 = ((color1 & 0xFF0000) >> 16);
		int r2 = (color2 & 0xFF);
		int g2 = ((color2 & 0xFF00) >> 8);
		int b2 = ((color2 & 0xFF0000) >> 16);
		int r = value*(r2-r1)/100+r1;
		int g = value*(g2-g1)/100+g1;
		int b = value*(b2-b1)/100+b1;
		if (r < 0) r = 0;
		if (r > 255) r = 255;
		if (g < 0) g = 0;
		if (g > 255) g = 255;
		if (b < 0) b = 0;
		if (b > 255) b = 255;
		return (r) | (g << 8) | (b << 16);
	}
	
	static public int getInverseColor(int color)
	{
		int r = (color & 0xFF);
		int g = ((color & 0xFF00) >> 8);
		int b = ((color & 0xFF0000) >> 16);
		return ((r+g+b) > 3*127) ? 0 : 0xFFFFFF;
	}

	public void paintAllOnGraphics(Graphics graphics)
	{
		paintAllOnGraphics(graphics, DMS_DRAW, -1, -1);
	}
	
	public void paintAllOnGraphics(Graphics graphics, int mode, int mouseX, int mouseY)
	{
		int y;
		int height = getHeightInternal();
		int capHeight = getCapHeight();
		int visCount = getVisCount();
		int menuBarHeight;
		boolean clicked;

//#sijapp cond.if target!="RIM"#
		menuBarHeight = getMenuBarHeight();
//#sijapp cond.else#		
		menuBarHeight = 0;
//#sijapp cond.end#
		
		switch (mode)
		{
		case DMS_DRAW:
			int clipY1 = graphics.getClipY();
			int clipY2 = clipY1+graphics.getClipHeight();
			y = (clipY1 <= capHeight) ? drawCaption(graphics, mode, mouseX, mouseY) : capHeight;
			drawItems(graphics, y, getFontHeight(), menuBarHeight, mode, mouseX, mouseY, clipY1, clipY2);
			drawScroller(graphics, y, visCount, menuBarHeight);
//#sijapp cond.if target!="RIM"#			
			if (menuBarHeight != 0)
			{
				int barY = height-menuBarHeight;
				if (clipY2 >= barY)
					drawMenuBar(graphics, menuBarHeight, mode, mouseX, mouseY);
			}
			drawMenuItems(graphics, menuBarHeight, mode, mouseX, mouseY);
//#sijapp cond.end#			
			break;
			
//#sijapp cond.if target is "MIDP2"#
		case DMS_CLICK:
		case DMS_DBLCLICK:
			y = capHeight;
			
			if (menuBarHeight != 0)
			{
				clicked = drawMenuBar(graphics, menuBarHeight, mode, mouseX, mouseY);
				if (clicked) return;
			}
			
			clicked = drawMenuItems(graphics, menuBarHeight, mode, mouseX, mouseY);
			if (clicked) return;
			clicked = drawItems(graphics, y, getFontHeight(), menuBarHeight, mode, mouseX, mouseY, -1, -1);
			if (clicked) return;
			break;
//#sijapp cond.end#
		}

	}

	static private Image bDIimage = null;

	// protected void paint(Graphics g)
	protected void paint(Graphics g)
	{
		if (dontRepaint) return;

		if (virtualCanvas.isDoubleBuffered())
		{
			paintAllOnGraphics(g, DMS_DRAW, -1, -1);
		}
		else
		{
			try
			{
				if (bDIimage == null) bDIimage = Image.createImage(getWidthInternal(), getHeightInternal());
				paintAllOnGraphics(bDIimage.getGraphics(), DMS_DRAW, -1, -1);
				g.drawImage(bDIimage, 0, 0, Graphics.TOP | Graphics.LEFT);
			}
			catch (Exception e)
			{
				paintAllOnGraphics(g, DMS_DRAW, -1, -1);
			}
		}
	}

	// protected void drawItemData
	protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int fontHeight, int paintMode)
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
		return (forcedHeight == -1) ? virtualCanvas.getHeight() : forcedHeight;
	}

	protected int getWidthInternal()
	{
		return (forcedWidth == -1) ? virtualCanvas.getWidth() : forcedWidth;
	}
	
	public int getWidth()
	{
		return virtualCanvas.getWidth();
	}
	
	public int getHeight()
	{
		return virtualCanvas.getHeight();
	}
	
	protected void onShow() {}
	protected void onHide() {}
	
	///////////////////////////////
	//                           //
	//        EXTENDED UI        //
    //                           //
	///////////////////////////////
	
	public static final int MENU_TYPE_LEFT_BAR = 1;
	public static final int MENU_TYPE_RIGHT_BAR = 2;
	public static final int MENU_TYPE_LEFT = 3;
	public static final int MENU_TYPE_RIGHT = 4;
	
	protected static final int DMS_DRAW = 1;
	protected static final int DMS_CLICK = 2;
	protected static final int DMS_DBLCLICK = 3;
	protected static final int DMS_CUSTOM = 4;
	
	protected Command findMenuByType(int type)
	{
//#sijapp cond.if target="RIM"#		
		for (int i = commands.size()-1; i >= 0; i--)
		{
			Command cmd = (Command)commands.elementAt(i); 
			if (cmd.getCommandType() == type) return cmd;
		}
//#sijapp cond.else#
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
//#sijapp cond.end#		
		
		return null;
	}
	
	public void addCommandEx(Command cmd, int type)
	{
//#sijapp cond.if target="RIM"#
		commands.addElement(cmd);
		if (virtualCanvas.currentControl == this) virtualCanvas.addCommand(cmd);
//#sijapp cond.else#		
		if (mirrorMenu)
		{
			switch (type)
			{
			case MENU_TYPE_LEFT_BAR:  type = MENU_TYPE_RIGHT_BAR; break;
			case MENU_TYPE_RIGHT_BAR: type = MENU_TYPE_LEFT_BAR;  break;
			case MENU_TYPE_LEFT:      type = MENU_TYPE_RIGHT;     break;
			case MENU_TYPE_RIGHT:     type = MENU_TYPE_LEFT;      break;
			}
		}
		
		switch (type)
		{
		case MENU_TYPE_LEFT_BAR:
			leftMenu = cmd;
			invalidate();
			break;
			
		case MENU_TYPE_RIGHT_BAR:
			rightMenu = cmd;
			invalidate();
			break;
			
		case MENU_TYPE_LEFT:
			if (leftMenuItems.indexOf(cmd) == -1)
			{
				leftMenuItems.addElement(cmd);
				leftMenuItemsSorted = false;
			}
			break;
			
		case MENU_TYPE_RIGHT:
			if (rightMenuItems.indexOf(cmd) == -1)
			{
				rightMenuItems.addElement(cmd);
				rightMenuItemsSorted = false;
			}
			break;
		}
		
//#sijapp cond.end#		
	}
	
	public void removeCommandEx(Command cmd)
	{
//#sijapp cond.if target="RIM"#		
		commands.removeElement(cmd);
		if (virtualCanvas.currentControl == this) virtualCanvas.removeCommand(cmd);
//#sijapp cond.else#		
		if (cmd == leftMenu)
		{
			leftMenu = null;
			leftMenuItems.removeAllElements();
			invalidate();
			return;
		} 
		else if (cmd == rightMenu)
		{
			rightMenu = null;
			rightMenuItems.removeAllElements();
			invalidate();
			return;
		}
		
		leftMenuItems.removeElement(cmd);
		rightMenuItems.removeElement(cmd);
//#sijapp cond.end#		
	}
	
	public void removeAllCommands()
	{
//#sijapp cond.if target="RIM"#
		if (virtualCanvas.currentControl == this)
			for (int i = commands.size()-1; i >= 0; i--) 
				virtualCanvas.removeCommand((Command)commands.elementAt(i));
		commands.removeAllElements();
//#sijapp cond.else#		
		leftMenu = null;
		rightMenu = null;
		leftMenuItems.removeAllElements();
		rightMenuItems.removeAllElements();
//#sijapp cond.end#
	}

//#sijapp cond.if target="RIM"#
	private Vector commands = new Vector();
//#sijapp cond.else#
	
	private static final int UI_STATE_NORMAL = 0;
	private static final int UI_STATE_LEFT_MENU_VISIBLE = 1;
	private static final int UI_STATE_RIGHT_MENU_VISIBLE = 2;
	
	private int uiState;
	
	private Command leftMenu;
	private Command rightMenu;
	private Vector leftMenuItems = new Vector();
	private Vector rightMenuItems = new Vector();
	private boolean leftMenuItemsSorted = true;
	private boolean rightMenuItemsSorted = true;
	
	void resetUiState()
	{
		uiState = UI_STATE_NORMAL; 
	}
	
	static private int visibleItemsMenuCount;
	static private int topMenuItem;
	
	private Vector leftMenuPressed()
	{
		Vector items = null;
		if (leftMenu != null)
		{
			if (leftMenuItems.size() == 0)
			{
				if ( executeCommand(leftMenu) ) return null;
			}
			else 
			{
				if (uiState == UI_STATE_LEFT_MENU_VISIBLE) uiState = UI_STATE_NORMAL;
				else
				{
					if (!leftMenuItemsSorted)
					{
						sortMenuItems(leftMenuItems);
						leftMenuItemsSorted = true;
					}
					uiState = UI_STATE_LEFT_MENU_VISIBLE;
					items = leftMenuItems;
				}
			}
		}
		
		return items;
	}
	
	private Vector rightMenuPressed()
	{
		Vector items = null;
		if (rightMenu != null)
		{
			if (rightMenuItems.size() == 0)
			{
				if (executeCommand(rightMenu)) return null;
			}
			else
			{
				if (uiState == UI_STATE_RIGHT_MENU_VISIBLE) uiState = UI_STATE_NORMAL;
				else
				{
					if (!rightMenuItemsSorted)
					{
						sortMenuItems(rightMenuItems);
						rightMenuItemsSorted = true;
					}
					uiState = UI_STATE_RIGHT_MENU_VISIBLE;
					items = rightMenuItems;
				}
			}
		}
		return items;
	}
	
	private void initPopupMenuItems(Vector items)
	{
		if (items == null) return;
		curMenuItemIndex = items.size()-1;
		int menuItemsCount = items.size();
		int menuHeight = getMenuHeight(menuItemsCount);
		int drawHeight = getDrawHeight();
		if (menuHeight > drawHeight)
		{
			visibleItemsMenuCount = drawHeight/capAndMenuFont.getHeight();
			topMenuItem = menuItemsCount-visibleItemsMenuCount;
		}
		else
		{
			visibleItemsMenuCount = menuItemsCount;
			topMenuItem = 0;
		}
	}
	
	private static void moveSelectedMenuItem(int offset, int size, boolean moveOnlyView)
	{
		if (!moveOnlyView)
		{
			curMenuItemIndex += offset;
			if (curMenuItemIndex >= size) curMenuItemIndex = 0;
			if (curMenuItemIndex < 0) curMenuItemIndex = size-1;
			if (curMenuItemIndex >= topMenuItem+visibleItemsMenuCount) 
				topMenuItem = curMenuItemIndex-visibleItemsMenuCount+1;
			if (curMenuItemIndex < topMenuItem) 
				topMenuItem = curMenuItemIndex;
		}
		else
		{
			topMenuItem += offset; 
			if (topMenuItem < 0) topMenuItem = 0;
			if (topMenuItem >= size-visibleItemsMenuCount) topMenuItem = size-visibleItemsMenuCount;
		}
	}
	
	public static void drawFramedString (Graphics g, String text, int left, int top, int style, int textColor, int frameColor){
		g.setColor(frameColor);
		g.drawString(text, left-1, top, style);
		g.drawString(text, left+1, top, style);
		g.drawString(text, left, top-1, style);
		g.drawString(text, left, top+1, style);
		g.setColor(textColor);
		g.drawString(text, left, top, style);
	}

	private boolean drawMenuBar(Graphics g, int height, int style, int curX, int curY)
	{
		int y1 = getHeightInternal()-height;
		int y2 = getHeightInternal();
		int width = getWidthInternal();
		int layer = height/4;
		
		if (style == DMS_DBLCLICK) return false;
		
		if (style == DMS_DRAW)
		{
			if (fullScreen)
			{
				g.setColor(bkgrndColor);
				g.fillRect(0, y1, width, y2-y1);
				return false;
			}
			else drawRect(g, capBkCOlor, transformColorLight(capBkCOlor, -80), 0, y1, width, y2);
		}
		
		g.setFont(capAndMenuFont);
		
		int textY = (y1+y2-capAndMenuFont.getHeight())/2+2;
		
		boolean menuItemsVisible = false;
		if (leftMenu != null)
		{
			//#sijapp cond.if target is "MIDP2"#
			if ((style == DMS_CLICK) && ptInRect(curX, curY, 0, y1, getWidthInternal()/2, y2))
			{
				Vector items = leftMenuPressed();
				initPopupMenuItems(items);
				invalidate();
				return true;
			}
			//#sijapp cond.end#
			
			if (uiState == UI_STATE_LEFT_MENU_VISIBLE)
			{
				menuItemsVisible = true;
				drawRect(g, transformColorLight(capBkCOlor, -64), transformColorLight(capBkCOlor, -32), 0, y1, width/2, y2);
			}
			String text = leftMenu.getLabel();
			g.setColor(capTxtColor);
			g.drawString(text, layer, textY, Graphics.TOP|Graphics.LEFT);
		}
		
		if (rightMenu != null)
		{
			//#sijapp cond.if target is "MIDP2"#
			if ((style == DMS_CLICK) && ptInRect(curX, curY, getWidthInternal()/2, y1, getWidthInternal(), y2))
			{
				Vector items = rightMenuPressed();
				initPopupMenuItems(items);
				invalidate();
				return true;
			}
			//#sijapp cond.end#
			
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
				width-layer-capAndMenuFont.stringWidth(text), 
				textY, 
				Graphics.TOP|Graphics.LEFT
			);
		}
		
		if (!menuItemsVisible && (bottomText != null))
		{

			drawFramedString
			(
			 	g, bottomText,
				(width-capAndMenuFont.stringWidth(bottomText))/2, 
				textY, 
				Graphics.TOP|Graphics.LEFT,
				capTxtColor, getInverseColor(capTxtColor)

			);
		}
		
		g.setColor(transformColorLight(capBkCOlor, -128));
		g.drawLine(0, y1, width, y1);
		return false; 
	}
	
	private boolean exMenuExists()
	{
		return (leftMenu != null) || (rightMenu != null);
	}
	
	protected final int getMenuBarHeight()
	{
		if (fullScreen) return curFrameWidth;
		return exMenuExists() ? capAndMenuFont.getHeight()+3 : curFrameWidth;
	}

	private boolean drawMenuItems(Graphics g, int menuBarHeight, int style, int curX, int curY)
	{
		switch (uiState)
		{
		case UI_STATE_LEFT_MENU_VISIBLE:
			return drawMenuItems(g, leftMenuItems, getHeightInternal()-menuBarHeight, Graphics.LEFT, style, curX, curY);
			
		case UI_STATE_RIGHT_MENU_VISIBLE:
			return drawMenuItems(g, rightMenuItems, getHeightInternal()-menuBarHeight, Graphics.RIGHT, style, curX, curY);
		}
		return false;
	}
	
	private static int getMenuHeight(int count)
	{
		int fontHeight = capAndMenuFont.getHeight();
		return fontHeight+fontHeight*count;
	}
	
	private static boolean ptInRect(int ptX, int ptY, int x1, int y1, int x2, int y2)
	{
		return (x1 <= ptX) && (ptX < x2) && (y1 <= ptY) && (ptY < y2); 
	}
	
	private boolean paint3points(Graphics g, int x1, int y1, int x2, int y2, int mode, int curX, int curY, int moveOffset, int menuItemsCount)
	{
		switch (mode)
		{
		case DMS_DRAW: 
			g.setColor(textColor);
			int size = 2;
			int y = (y1+y2-size)/2;
			for (int i = -1; i <= 1; i++)
			{
				int x = (x1+x2)/2-i*(2*size+1);
				g.fillRect(x, y, size, size);
			}
			break;
			
		//#sijapp cond.if target is "MIDP2"#			
		case DMS_CLICK:
		case DMS_DBLCLICK:
			if (ptInRect(curX, curY, x1, y1, x2, y2))
			{
				moveSelectedMenuItem(moveOffset, menuItemsCount, true);
				invalidate();
				return true;
			}
			break;
		//#sijapp cond.end#
		}
		return false;
	}
	
	private boolean drawMenuItems(Graphics g, Vector items, int bottom, int horizAlign, int mode, int curX, int curY)
	{
		int fontHeight = capAndMenuFont.getHeight(); 
		int layer = fontHeight/3;
		int vert_layer = fontHeight/2;
		
		int itemsCount = items.size();
		
		// calculate width and height
		int width = 0;
		int height = getMenuHeight(visibleItemsMenuCount);
		for (int i = 0; i < itemsCount; i++)
		{
			Command cmd = (Command)items.elementAt(i);
			int txtWidth = capAndMenuFont.stringWidth(cmd.getLabel());
			if (txtWidth > width) width = txtWidth;
		}
		width += layer*2;
		if (width > getWidth()-4) width = getWidth()-4;
		
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
		
		// Draw background
		if (mode == DMS_DRAW)
		{
			drawRect(g, transformColorLight(bkgrndColor, 24), transformColorLight(bkgrndColor, -24), x, y, x+width, y+height);
		}
		
		// Draw up button
		if (topMenuItem != 0)
		{
			boolean ok = paint3points(g, x, y, x+width, y+vert_layer, mode, curX, curY, -1, itemsCount);
			if (ok) return true;
		}
		
		if (topMenuItem+visibleItemsMenuCount != itemsCount)
		{
			boolean ok = paint3points(g, x, y+height-vert_layer, x+width, y+height, mode, curX, curY, +1, itemsCount);
			if (ok) return true;
		}
		
		// Draw items
		g.setFont(capAndMenuFont);
		
		int itemY = y+vert_layer;
		
		for (int i = topMenuItem, j = 0; j < visibleItemsMenuCount; i++, j++)
		{
			if (i == curMenuItemIndex)
			{
				if (mode == DMS_DRAW)
				{
					g.setColor(getInverseColor(bkgrndColor));
					g.fillRect(x, itemY-1, width+1, fontHeight+2);
				}
			}
			itemY += fontHeight;
		}
		
		itemY = y+vert_layer;

		for (int i = topMenuItem, j = 0; j < visibleItemsMenuCount; i++, j++)
		{
			Command cmd = (Command)items.elementAt(i);
			switch (mode)
			{
			case DMS_DRAW:
				g.setColor((i == curMenuItemIndex) ? bkgrndColor : textColor);
				g.drawString(cmd.getLabel(), x+layer, itemY, Graphics.LEFT|Graphics.TOP);
				break;
				
			//#sijapp cond.if target is "MIDP2"#
			case DMS_CLICK:
				if (ptInRect(curX, curY, x, itemY, x+width, itemY+fontHeight))
				{
					uiState = UI_STATE_NORMAL;
					invalidate();
					executeCommand(cmd);
					return true;
				}
				break;
			//#sijapp cond.end#
			}
			itemY += fontHeight;
		}
		
		
		// Draw rectangle
		if (mode == DMS_DRAW)
		{
			g.setColor(textColor);
			g.drawRect(x, y, width, height);
		}
		
		return false;
	}
	
	static private void sortMenuItems(Vector items)
	{
		int size = items.size()-1;
		boolean swaped;
		do
		{
			swaped = false; 
			for (int i = 0; i < size; i++)
			{
				Command cmd1 = (Command)items.elementAt(i);
				Command cmd2 = (Command)items.elementAt(i+1);
				if (cmd1.getPriority() < cmd2.getPriority())
				{
					items.setElementAt(cmd2, i);
					items.setElementAt(cmd1, i+1);
					swaped = true;
				}
			}
		}
		while (swaped);
	}
	
//#sijapp cond.end#	
	
//#sijapp cond.if target="MOTOROLA" | target="MIDP2"#
	private static boolean manualBackLight = false;
	private static int backLightTimeOut = 5;
	private static boolean backLightIsOn = false;
	
	public static void setBackLightData(boolean manualBackLight, int backLightTimeOut)
	{
		VirtualList.manualBackLight = manualBackLight;
		VirtualList.backLightTimeOut = backLightTimeOut;
	}
	
	private static void setBackLightOn()
	{
		if (!manualBackLight) return;
		virtualCanvas.getDisplay().flashBacklight(1000*backLightTimeOut);
		//System.out.println("VirtualList.setBackLightOn()");
	}
	
//#sijapp cond.end #
}