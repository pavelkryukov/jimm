/*
   Custom Controls for J2ME
   (c) 2004 Artyomov Denis (artyomov@inbox.ru)
   
   No license needs to use this software. You can use it as you want :)
   Any improvements are very welcome! :)
*/

package DrawControls;

import java.util.Vector;
import java.lang.String;
import java.lang.Integer;
import java.io.IOException;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
//#sijapp cond.if target is "MIDP2"#
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
  private Vector 
    items = new Vector();
  
  private int 
    width = 0,
    height = 0;
  
  //! Return image by index
  public Image elementAt
  (
    int index //!< Index of requested image in the list
  )
  {
    return (Image)items.elementAt(index);
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
    int width       //!< Width of of result images
  ) throws IOException
  {
    Image resImage = Image.createImage(resName);
    height = resImage.getHeight();
    int size = resImage.getWidth()/width;
    for (int i = 0; i < size; i++)
    {
//      #sijapp cond.if target is "MIDP2" | target is "SIEMENS"#
        Image newImage = Image.createImage(Image.createImage(resImage, i * width, 0, width, height, Sprite.TRANS_NONE));
//      #sijapp cond.else#
        Image newImage = Image.createImage(width, height);
        newImage.getGraphics().drawImage(resImage, -width*i, 0, Graphics.TOP|Graphics.LEFT);
//      #sijapp cond.end#     
      items.addElement( Image.createImage(newImage) );
    }
    this.width = width;
  }
  
  public void load
  (
    String firstLine,
	String extention,
	int from,
	int to
  ) throws IOException
  {
    Image image = null;
  
    for (int i = from; i <= to; i++)
	{
	  image = Image.createImage(firstLine+Integer.toString(i)+"."+extention);
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