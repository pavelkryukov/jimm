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
//#sijapp cond.if target is "MOTOROLA"#
import jimm.Jimm;
//#sijapp cond.end#
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;

import DrawControls.ImageList;
import DrawControls.ListItem;
import DrawControls.VirtualListCommands;

//! This class is base class of owner draw list controls
/*!
    It allows you to create list with different colors and images. 
    Base class of VirtualDrawList if Canvas, so it draw itself when
    paint event is heppen. VirtualList have cursor controlled of
    user
*/
public abstract class VirtualList extends Canvas
{
 //#sijapp cond.if target is "MOTOROLA"#
  /*! Turn off or on backlight*/
  private boolean LightOn =true;
  public void LightState()
  {              
               if (LightOn)
               {
                              Jimm.display.flashBacklight(0);
	          LightOn=false;
                }
                else
                {
                               Jimm.display.flashBacklight(Integer.MAX_VALUE);
                                LightOn=true;
                 }
   }
  //#sijapp cond.end#


  /*! Use inverted mode of cursor */
  public final static int SEL_INVERTED = 1;
  
  /*! Use dotted mode of cursor. If item of list 
      is selected, dotted rectangle drawn around  it*/
  public final static int SEL_DOTTED   = 2;
  
  /*! Does't show cursor at selected item. */
  public final static int SEL_NONE     = 3;
  
  /*! Constant for medium sized font of caption and item text */
  public final static int MEDIUM_FONT  = Font.SIZE_MEDIUM;
  
  /*! Constant for large sized font of caption and item text */
  public final static int LARGE_FONT   = Font.SIZE_LARGE;
  
  /*! Constant for small sized font of caption and item text */
  public final static int SMALL_FONT   = Font.SIZE_SMALL;
  
  // default values 
  protected static int
    defCapColor     = 0xD0D0D0,
    defCapFontColor = 0xFF,
    defBackColor    = 0xFFFFFF,
    defCursorMode   = SEL_DOTTED,
    defFontSize     = MEDIUM_FONT;

  public static int getDefCapColor()     { return defCapColor; }
  public static int getDefCapFontColor() { return defCapFontColor; }
  public static int getDefBackColor()    { return defBackColor; }
  public static int getDefCursorMode()   { return defCursorMode; }
  public static int getDefFontSize()     { return defFontSize; }
  
  private VirtualListCommands vlCommands;
  
  public void setVLCommands(VirtualListCommands vlCommands)
  {
  	this.vlCommands = vlCommands;
  }

  private boolean 
    dontRepaint      = false;
    
  private ImageList 
    imageList = null;
  
  private String 
    caption;
   
  private int 
    width    = 0,
    height   = 0,
    topItem  = 0, 
    currItem = 0, 
    visCount = 0,
    fontHeightInt = -1,
    fontSize = MEDIUM_FONT,
    bkgrndColor = 0xFFFFFF,
    capColor    = 0x0000D0,
    capTxtColor = 0xFFFFFF,
    cursorMode  = SEL_INVERTED;
    
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
  final public void setCapColor(int value)
  {
    capColor = value;
    repaint();
  }
  
  //! Sets new font size and invalidates items
  final public void setFontSize(int value)
  {
    fontSize = value;
	checkTopItem();
    repaint();
  }
  
  //TODO: brief text
  public void setCursorMode(int value)
  {
    if (cursorMode == value) return;
	cursorMode = value;
	invalidate();
  }
  
  //! Set background color of items
  final public void setBackgroundColor
  (
    int value //!< New color for background
  )
  {
    bkgrndColor = value;
    repaint(); 
  }
  
  //! Returns height of each item in list
  public int getItemHeight()
  {
    int imgHeight, fontHeight = getFontHeight();
    if (imageList != null) imgHeight = imageList.getHeight()+1;
    else imgHeight = 0;
    return (fontHeight > imgHeight) ? fontHeight : imgHeight;
  }
    
  // protected void invalidate()  
  final protected void invalidate()
  {
    if ( dontRepaint || !isShown() ) return;
    repaint();
  }
  
  //! Set new default values for all new classes based on VirtualList
  /*! To create new class based on VirtualList you must to call constructor
      only with caption argument. */
  public final static void setDefaults
  (
    int capBackColor, //!< Caption background color
    int capTextColor, //!< Caption text color
    int backColor,    //!< Control back color
    int fontSize,     /*!< Control font size. This font size if used both 
                           for caption and text in tree nodes */
    int cursorMode    /*!< Cursor mode. Can be VirtualList.SEL_DOTTED 
                           or VirtualList.SEL_INVERTED */
  )
  {
    defCapColor     = capBackColor;
    defCapFontColor = capTextColor;
    defBackColor    = backColor;
    defFontSize     = fontSize;
    defCursorMode   = cursorMode;
  }

  //! Create new virtual list with default values  
  public VirtualList
  (
    String capt //!< Caption text of new virtual list
  )
  {
    super();
    this.caption     = capt;
    this.capColor    = defCapColor;
    this.capTxtColor = defCapFontColor;
    this.bkgrndColor = defBackColor;
    this.fontSize    = defFontSize;
    this.cursorMode  = defCursorMode;
  }
  
  // public VirtualList
  public VirtualList
  (
    String capt,      //!< Caption text of new virtual list
    int capBackColor, //!< Caption background color
    int capTextColor, //!< Caption text color
    int backColor,    //!< Control back color
    int fontSize,     /*!< Control font size. This font size if used both 
                           for caption and text in tree nodes */
    int cursorMode    /*!< Cursor mode. Can be VirtualList.SEL_DOTTED 
                           or VirtualList.SEL_INVERTED */
  )
  {
    super();
    this.caption     = capt;
    this.capColor    = capBackColor;
    this.capTxtColor = capTextColor;
    this.bkgrndColor = backColor;
    this.fontSize    = fontSize;
    this.cursorMode  = cursorMode;
  }
  
  //! Setting image list for items
  public void setImageList
  (
    ImageList list /*!< Reference to ImageList object or null 
                        if you don't need to show images in list */
  )
  {
    imageList = list;
    repaint();
  }
  
  //! Return current image list, used for tree node icons
  /*! If no image list storad, null is returned */
  public ImageList getImageList()
  {
    return imageList;
  }

  // protected void checkCurrItem()
  protected void checkCurrItem()
  {
    if (currItem < 0) currItem = 0;
    if (currItem >= getSize()-1) currItem = getSize()-1;
  }
  
  // protected void checkTopItem() - internal
  // check for position of top element of list and change it, if nesessary
  protected void checkTopItem()
  {
  	int size = getSize();
  	
  	if (visCount == 0) return;
  	
  	if (size == 0)
  	{
  		topItem = 0;
  		return;
  	}
  	
    if (cursorMode == SEL_NONE)
    {
      if ((size-topItem) <= visCount) topItem = size-visCount;
    }
    else 
    {
      if (currItem >= (topItem+visCount-1)) topItem = currItem-visCount+1;
      if (currItem < topItem) topItem = currItem;
    }  
    
    if (topItem < 0) topItem = 0;
  }
  
  private int lastCurrItem = 0, lastTopItem = 0;
  
  // private void storelastItemIndexes()
  private void storelastItemIndexes()
  {
    lastCurrItem = currItem;
    lastTopItem = topItem;
  }
  
  // private void repaintIfLastIndexesChanged()
  private void repaintIfLastIndexesChanged()
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
    if (cursorMode == SEL_NONE) 
    {
      topItem += step;
      checkTopItem();
    }
    else
    {
      currItem += step;
      checkCurrItem();
      checkTopItem();
    }
    repaintIfLastIndexesChanged();
  }
  
  // private void moveToBottom()
  private void moveToBottom()
  {
    storelastItemIndexes();
    int endIndex = getSize()-1;
    if (cursorMode == SEL_NONE) topItem = endIndex;
    else currItem = endIndex;
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
  protected void userPressKey(int keyCode) {}
  
  protected void itemSelected() { }
  protected void cursorMoved() { }
  
  // private keyReaction(int keyCode)
  private void keyReaction(int keyCode)
  {
    switch ( getGameAction(keyCode) )
    {
    case Canvas.DOWN:  moveCursor(1);  break;
    case Canvas.UP:    moveCursor(-1); break;
    case Canvas.FIRE:  
    	itemSelected();
    	if (vlCommands != null) vlCommands.onItemSelected(this);
    	break;
    }
    
    switch (keyCode)
	{
	case KEY_NUM1:  moveToTop(); break;
	case KEY_NUM7:  moveToBottom(); break;
 //#sijapp cond.if target is "MOTOROLA"#
                      case KEY_STAR:  LightState(); break;
 //#sijapp cond.end#


	}
  }
  
  // protected void keyPressed(int keyCode) 
  protected void keyPressed(int keyCode)
  {
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
  final public void setCaption
  (
    String capt
  )
  {
    caption = capt;
    repaint();
  }

  final public void setTopItem(int index)
  {
    if (cursorMode != SEL_NONE) return;
    storelastItemIndexes();
    topItem = index;
    checkTopItem();
    repaintIfLastIndexesChanged();
  }

  // public void setCurrentItem(int index)
  final public void setCurrentItem(int index)
  {
    storelastItemIndexes();
    currItem = index;
    checkTopItem();
    repaintIfLastIndexesChanged();
  }
  
  // final public int getCurrIndex()
  final public int getCurrIndex()
  {
    return currItem;
  }
  
  // private int drawCaption(Graphics g)
  private int drawCaption(Graphics g)
  {
  	if (caption == null) return 0;
  	
    int tmp_y, th;
    Font font;
  
    font = createFont(Font.STYLE_BOLD);
    g.setFont(font);
    th = font.getHeight();
    g.setColor(capColor);
    g.fillRect(0, 0, width, th+2);
    g.setColor(capTxtColor);
    g.drawString(caption, th/3, 1, Graphics.TOP|Graphics.LEFT);
    g.setColor(0);
    tmp_y = th+1;
    g.drawLine(0, tmp_y, width, tmp_y);
    g.setColor(bkgrndColor);
    tmp_y++;
    g.drawLine(0, tmp_y, width, tmp_y);
    return tmp_y+1;
  }
  
  // private void drawDottedSelectedBgrnd(Graphics g, int x, int y, int w, int h, ListItem item)
  private int drawDottedSelectedBgrnd(Graphics g, int x, int y, int w, int h, ListItem item)
  {
    g.setColor(bkgrndColor);
    g.fillRect(x, y, w, h);
    g.setStrokeStyle(Graphics.DOTTED);
    
    g.setColor(item.color);
    g.drawRect(x, y, w-1, h-1);
    return item.color;
  }
  
  // private void drawInvertedSelectedBgrnd(Graphics g, int x, int y, int w, int h, ListItem item)
  private int drawInvertedSelectedBgrnd(Graphics g, int x, int y, int w, int h, ListItem item)
  {
    int k1 = 1;
    int k2 = width-1;
    g.setColor(item.color);
    g.fillRect(k1, y, k2-k1, h);
    g.setColor(bkgrndColor);
    g.fillRect(0, y, k1, h);
    g.fillRect(k2, y, width-k2, h);
    g.fillRect(k1, y, 1, 1);
    g.fillRect(k1, y+h-1, 1, 1);
    g.fillRect(k2-1, y, 1, 1);
    g.fillRect(k2-1, y+h-1, 1, 1);
    return bkgrndColor;
  }
  
  // private int drawItem(int index, Graphics g, int top_y, int th, ListItem item)
  private int drawItem(int index, Graphics g, int top_y, int itemHeight, ListItem item, int fontHeight)
  {
    int y = 0, x = 0, w = 0, textColor = 0;
    boolean isSelected = ((currItem == index) && (cursorMode != SEL_NONE));
    
    item.clear();
    get(index, item);
    y = top_y+(index-topItem)*itemHeight;
    
    if (lastFontStyle != item.fontStyle)
    {
      g.setFont( createFont(item.fontStyle) );
      lastFontStyle = item.fontStyle;
    }
    
    if (isSelected)
    {
      switch (cursorMode)
      {
case SEL_INVERTED:       
        textColor = drawInvertedSelectedBgrnd(g, 0, y, width, itemHeight, item);
        break;
        
case SEL_DOTTED:
        textColor = drawDottedSelectedBgrnd(g, 0, y, width, itemHeight, item);
        break;
      }  
    }
    else
    {
      g.setColor(bkgrndColor);
      g.fillRect(0, y, width, itemHeight);
      textColor = item.color;
    }  
    
    switch (cursorMode)
    {
case SEL_INVERTED: x = itemHeight/3; break;
case SEL_DOTTED:   x = 2;            break;
    }
    
    g.setColor(textColor);
    g.setStrokeStyle(Graphics.SOLID);
    drawItemData(g, item, isSelected, index, x, y, width-itemHeight/3, y+itemHeight, fontHeight);
    
    return y+itemHeight;
  }
  
  // private void drawScroller(Graphics g, int scrollerWidth)
  private void drawScroller(Graphics g, int scrollerWidth, int topY)
  {
    int sliderSize = getItemHeight(),
        y1, y2, itemCount,
        position;
        
    position  = (cursorMode == SEL_NONE) ? topItem : currItem;
    itemCount = (cursorMode == SEL_NONE) ? getSize()-visCount : getSize()-1;
    
    if (itemCount < 1) itemCount = 1;
    y1 = position*(height-sliderSize-topY)/itemCount+topY;
    y2 = y1+sliderSize;
    g.setColor(defCapColor);
    g.fillRect(width, topY, scrollerWidth, y1);
    g.fillRect(width, y2, scrollerWidth, height-y2);
    g.setColor(0x00);
    g.fillRect(width, y1, scrollerWidth, y2-y1);
  }
  
  //! returns font height
  public int getFontHeight()
  {
    if (fontHeightInt != -1) return fontHeightInt;
    Font font = createFont(Font.STYLE_PLAIN);
    fontHeightInt = font.getHeight();
    return fontHeightInt;
  }
  
  // used for fast drawing items with diff. font styles
  int lastFontStyle;
  
  // private int drawItems(Graphics g, int top_y)
  private int drawItems(Graphics g, int top_y, int fontHeight)
  {
    int size, itemHeight, i, y = 0;
    ListItem item = new ListItem();
    
    itemHeight = getItemHeight();
    size = getSize();
    
    lastFontStyle = -1;
    y = top_y;
    for (i = topItem; i < size; i++)
    {
      y = drawItem(i, g, top_y, itemHeight, item, fontHeight);
      if (y >= height) break;
    }
    if (y < height)
    {
      g.setColor(bkgrndColor);
      g.fillRect(0, y, width, height);
    }
    
    return y;
  }
  
  void init() {}

  void destroy() {  }
  
  // final protected Font createFont(int style)
  final protected Font createFont(int style)
  {
    return Font.getFont(Font.FACE_PROPORTIONAL, style, fontSize);
  }
  
  // final protected int getScrollerWidth()
  final protected int getScrollerWidth()
  {
    return getFontHeight()/5;
  }
  
  // private void paintAllOnGraphics(Graphics graphics)
  private void paintAllOnGraphics(Graphics graphics)
  {
    int scrollerWidth = getScrollerWidth();
    height = getHeight();
    width = getWidth();
    int y = drawCaption(graphics);
    boolean haveToCheckTopItem = (visCount == 0); 
    
    boolean needCheck = (visCount == 0); 
    
    visCount = (height-y)/getItemHeight();
    
    if (haveToCheckTopItem) checkTopItem();
    
    if (needCheck)
    {
    	checkCurrItem();
    	checkTopItem();
    }
    
    width = getWidth()-scrollerWidth;
    drawItems( graphics, y, getFontHeight() );
    drawScroller(graphics, scrollerWidth, y);
  }
  
  static Image bDIimage = null;

  // final protected void paint(Graphics g)
  final protected void paint(Graphics g)
  {
    if (dontRepaint) return;
  
    if ( isDoubleBuffered() ) 
    {
      paintAllOnGraphics(g);
    }  
    else 
    {
      try
      {
        if (bDIimage == null) bDIimage = Image.createImage(getWidth(), getHeight());
        paintAllOnGraphics( bDIimage.getGraphics() );
        g.drawImage(bDIimage, 0, 0, Graphics.TOP|Graphics.LEFT);
      }
      catch (Exception e)
      {
        paintAllOnGraphics(g);
      }
    }
  }

  // protected void drawItemData
  protected void drawItemData
  (
    Graphics g, 
    ListItem item, 
    boolean isSelected, 
    int index, 
    int x1, int y1, int x2, int y2,
    int fontHeight
  )
  {
    int imgWidth;
  
    ImageList imageList = getImageList();
    if ((imageList != null) && (item.imageIndex >= 0) && (item.imageIndex < imageList.size()))
    {
      imgWidth = imageList.getWidth()+3;
      g.drawImage
      (
        imageList.elementAt(item.imageIndex), 
        x1+1, 
        (y1+y2-imageList.getHeight())/2, 
        Graphics.TOP|Graphics.LEFT
      );
    }
    else imgWidth = 0;
    
    if (item.text != null)
      g.drawString(item.text, x1+imgWidth, (y1+y2-fontHeight)/2, Graphics.TOP|Graphics.LEFT);
  }
  
  public void lock()
  {
    dontRepaint = true;
  }
  
  protected void afterUnlock() {} 
  
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
