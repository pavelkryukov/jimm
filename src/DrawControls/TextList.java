/*
   Custom Controls for J2ME
   (c) 2004 Artyomov Denis (artyomov@inbox.ru)
   
   No license needs to use this software. You can use it as you want :)
   Any improvements are very welcome! :)
*/

package DrawControls;

import java.util.Vector;
import javax.microedition.lcdui.*;

import DrawControls.VirtualList;
import DrawControls.ListItem;

//! Text list
/*! This class store text and data of items internally
    You may use it to show text with colorised lines :) */
public class TextList extends VirtualList
{
  private Vector 
    items = new Vector();

  // protected int getSize()
  protected int getSize()
  {
    return items.size();
  }
  
  public int getItemCount()
  {
    return items.size();
  }
  
  // protected void get(int index, ListItem item)
  protected void get(int index, ListItem item)
  {
    ListItem listItem = (ListItem)items.elementAt(index);
    listItem.assignTo(item);
  }
  
  //! Remove all items form list
  public void clear()
  {
    items.removeAllElements();
    setCurrentItem(0);
  }

  //! Add new text item to list
  public void add
  (
    String text,   //!< Text of new item
    int color,     //!< Color of new item
    int imageIndex /*!< Index of image in images list. You must use 
                        setImageList to set images for list items */
  )
  {
    internAdd(text, color, imageIndex, Font.STYLE_PLAIN);
    invalidate();
  }
  
  //! Add new text item to list
  public void add
  (
    String text,    //!< Text of new item
    int color,      //!< Color of new item
    int imageIndex, /*!< Index of image in images list. You must use 
                         setImageList to set images for list items */
    int fontStyle   //!< Text font style. See MID profile for details
  )
  {
    internAdd(text, color, imageIndex, fontStyle);
    invalidate();
  }
  
  
  private void internAdd(String text, int color, int imageIndex, int fontStyle)
  {
    ListItem new_item = new ListItem();
    new_item.color      = color;
    new_item.imageIndex = imageIndex;
    new_item.text       = text;
    new_item.fontStyle  = fontStyle;
    items.addElement(new_item);
  }
  
  //! Add new black text item to list
  public void add
  (
    String text //!< Text of new item
  )
  {
    add(text, 0x0, -1);
  }
  
  //! Construct new text list with default values of colors, font size etc...
  public TextList
  (
    String capt //!< Caption of list
  )
  {
    super(capt);
  }
  
  
  //! Construct new text list 
  public TextList
  (
    String capt,      //!< Caption of list
    int capBackColor, //!< Background color of caption
    int capTextColor, //!< Text color of caption
    int backColor,    //!< Background color of list
    int fontSize,     /*!< Font size for list items and caption. 
                           Can be VirtualList.SMALL_FONT, VirtualList.MEDIUM_FONT 
                           or VirtualList.LARGE_FONT */
    int cursorMode    //!< Cursor mode. Can be VirtualList.SEL_INVERTED or VirtualList.SEL_DOTTED
  )
  {
    super(capt, capBackColor, capTextColor, backColor, fontSize, cursorMode);
  }
 
  //! Add big multiline text. 
  /*! Text visial width can be larger then screen width.
      Method addBigText automaticalli divide text to lines 
      and add lines to text list */
  public void addBigText
  (
    String text,  //!< Text to add
    int color,    //!< Text color
    int fontStyle //!< Text font style. See MID profile for details
  )
  {
    Font font = createFont(fontStyle);
    int textLen = text.length();
    int startPos, curPos, width, last_word_end, test_width;
    String test_line;
    
    width = getWidth()-getScrollerWidth()-3;
    
    startPos = 0;
    last_word_end = -1;
    for (curPos = 0; curPos < textLen; curPos++)
    {
      char currChar = text.charAt(curPos);
      boolean textEnd = (curPos == textLen-1);
      boolean lineBreak = ((currChar == '\n') || (currChar == '\r'));
      
      if ((currChar == ' ') || textEnd || lineBreak)
      {
        if (textEnd) curPos++;
        test_line = text.substring(startPos, curPos);
        test_width = font.stringWidth(test_line);
        
        if ((textEnd||lineBreak) && (test_width < width))
        {
           internAdd(test_line.trim(), color, -1, fontStyle);
           startPos = curPos;
           last_word_end = -1;
        }
        
        else if ((test_width > width) || lineBreak)
        {
          if (last_word_end != -1)
          {
            //System.out.println("1: "+text.substring(startPos, last_word_end));
          
            internAdd(text.substring(startPos, last_word_end).trim(), color, -1, fontStyle);
            curPos = last_word_end+1;
            if (!textEnd) for (; curPos < textLen; curPos++) { if (text.charAt(curPos) != ' ') break; }
          }
          else
          {
            for (; curPos >= startPos; curPos--)
            {
              test_line = text.substring(startPos, curPos);
              test_width = font.stringWidth(test_line);
              if (test_width <= width) 
              {
                //System.out.println("2: "+test_line); 
                internAdd(test_line.trim(), color, -1, fontStyle);
                break;
              }
            }
          }
          last_word_end = -1;
          startPos = curPos;
        }
        else last_word_end = curPos;
      }  
    }
    invalidate();
  }
}
