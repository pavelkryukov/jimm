/*
   Custom Controls for J2ME
   (c) 2004 Artyomov Denis (artyomov@inbox.ru)
   
   No license needs to use this software. You can use it as you want :)
   Any improvements are very welcome! :)
*/


package DrawControls;

import javax.microedition.lcdui.Font;

//! Data for tree node
/*! All member of class are made as public 
    in order to easy access. 
*/
public class ListItem
{
  public String 
    text;        //!< Text of node
    
  public int 
    fontStyle,
    color,       //!< Color of node text
    imageIndex;  //!< Index of node image. Must be -1 for disabling image
    
  ListItem()
  {
    color = imageIndex = 0;
    fontStyle = Font.STYLE_PLAIN;
  }
    
  ListItem(String text, int color, int imageIndex)
  {
    this.text = text;
    this.color = color;
    this.imageIndex = imageIndex;
    fontStyle = Font.STYLE_PLAIN;
  }
  
  //! Set all member to default values
  public void clear()
  {
    text = "";
    color = 0;
    imageIndex = -1;
  }
  
  //! Copy data of class to another object
  public void assignTo
  (
    ListItem dest //!< Destination object to copy data
  )
  {
    dest.text       = text;
    dest.color      = color;
    dest.imageIndex = imageIndex;
    dest.fontStyle  = fontStyle;
  }
}
