/*
   Custom Controls for J2ME
   (c) 2004 Artyomov Denis (artyomov@inbox.ru)
   
   No license needs to use this software. You can use it as you want :)
   Any improvements are very welcome! :)
*/

package DrawControls;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;

import DrawControls.ImageList;
import DrawControls.ListItem;

//! This class is base class of owner draw list controls
/*!
    It allows you to create list with different colors and images. 
    Base class of VirtualDrawList if Canvas, so it draw itself when
    paint event is heppen. VirtualDrawList have cursor controlled of
    user
*/
public abstract class VirtualList extends Canvas
{
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

  private boolean 
    dontPaintCaption = false;
    
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
    if (imageList != null) imgHeight = imageList.getHeight()+2;
    else imgHeight = 0;
    return (fontHeight > imgHeight) ? fontHeight : imgHeight;
  }
    
  // protected void invalidate()  
  final protected void invalidate()
  {
    if ( !isShown() ) return;
    dontPaintCaption = true;
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
    defCapColor = capBackColor;
    defCapFontColor = capTextColor;
    defBackColor = backColor;
    defFontSize = fontSize;
    defCursorMode = cursorMode;
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
  
  // private void checkTopItem() - internal
  // check for position of top element of list and change it, if nesessary
  private void checkTopItem()
  {
    if (cursorMode == SEL_NONE)
    {
      if ((getSize()-topItem) <= visCount) topItem = getSize()-visCount;
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
    if ((lastCurrItem != currItem) || (lastTopItem != topItem)) invalidate();
  }
  
  // private void moveUp()
  private void moveUp()
  {
    storelastItemIndexes();
    if (cursorMode == SEL_NONE) 
    {
      topItem--;
      checkTopItem();
    }
    else
    {
      currItem--;
      checkCurrItem();
      checkTopItem();
    }
    repaintIfLastIndexesChanged();
  }
  
  // private void moveDown()
  private void moveDown()
  {
    storelastItemIndexes();
    if (cursorMode == SEL_NONE) 
    {
      topItem++;
      checkTopItem();
    }  
    else
    {
      currItem++;
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
  
  // private keyReaction(int keyCode)
  private void keyReaction(int keyCode)
  {
    switch ( getGameAction(keyCode) )
    {
case Canvas.DOWN:  moveDown();     break;
case Canvas.UP:    moveUp();       break;
case Canvas.LEFT:  moveToTop();    break;
case Canvas.RIGHT: moveToBottom(); break;
case Canvas.FIRE:  itemSelected(); break;
    }
  }
  
  // protected void keyPressed(int keyCode) 
  protected void keyPressed(int keyCode)
  {
    keyReaction(keyCode);
    userPressKey(keyCode);
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
    int tmp_y, th;
    Font font;
  
    font = createFont(Font.STYLE_BOLD);
    g.setFont(font);
    if (dontPaintCaption) return font.getHeight()+3;
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
    
    switch (cursorMode)
    {
case SEL_DOTTED:
      g.setColor(item.color);
      g.drawRect(x, y, w-1, h-1);
      break;

case SEL_NONE:
      //g.setColor(0x808080); // TODO: make color as varible
      //g.drawLine(x, y, w-1, y);
      break;
      
    }
    
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
    boolean isSelected = (currItem == index);
    
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
case SEL_NONE:
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
    
    visCount = (height-y)/getItemHeight();
    width = getWidth()-scrollerWidth;
    drawItems( graphics, y, getFontHeight() );
    drawScroller(graphics, scrollerWidth, y);
  }
  
  static Image bDIimage = null;

  // final protected void paint(Graphics g)
  final protected void paint(Graphics g)
  {
    if ( isDoubleBuffered() ) 
    {
      paintAllOnGraphics(g);
    }  
    else 
    {
      if (bDIimage == null) bDIimage = Image.createImage(getWidth(), getHeight());
      dontPaintCaption = false;
      paintAllOnGraphics( bDIimage.getGraphics() );
      g.drawImage(bDIimage, 0, 0, Graphics.TOP|Graphics.LEFT);
    }
    dontPaintCaption = false;
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
    if ((imageList != null) && (item.imageIndex != -1))
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

}
