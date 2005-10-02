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
 File: src/DrawControls/TextList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/


package DrawControls;

import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.*;

import DrawControls.VirtualList;
import DrawControls.ListItem;

class TextLine
{
	final Vector items = new Vector();
	int height = -1;
  	int bigTextIndex = -1;	
	
	ListItem elementAt(int index)
	{
		return (ListItem) items.elementAt(index); 
	}
	
	int getHeight(int fontSize)
	{
		if (height == -1)
		{
			height = fontSize;
			for (int i = items.size()-1; i >= 0; i--)
			{
				int currHeight = elementAt(i).getHeight(fontSize); 
				if (currHeight > height) height = currHeight; 
			}
		}
		return height;
	}
	
	int getWidth(int fontSize)
	{
		int width = 0;
		for (int i = items.size()-1; i >= 0; i--) width += elementAt(i).getWidth(fontSize);  
		return width;
	}
	
	void setItemColor(int value)
	{
		for (int i = items.size()-1; i >= 0; i--)
		{
			ListItem listItem = elementAt(i);
			if ((listItem.color == 0x000000) || (listItem.color == 0xFFFFFF)) listItem.color = value;
		}
	}
	
	void paint(int xpos, int ypos, Graphics g, int fontSize, VirtualList vl)
	{
		int count = items.size(), 
			intemHeight = getHeight(fontSize);
		
		for (int i = 0; i < count; i++)
		{
			ListItem item = elementAt(i);
			int drawYPos = ypos+intemHeight-item.getHeight(fontSize);
			if (item.image != null)
			{
				g.drawImage(item.image, xpos, drawYPos, Graphics.TOP|Graphics.LEFT);
			}
			else if (item.text != null)
			{
				g.setColor(item.color);
				g.setFont(vl.getQuickFont(item.fontStyle));
				g.drawString(item.text, xpos, drawYPos, Graphics.TOP|Graphics.LEFT);
			}
			xpos += item.getWidth(fontSize);
		}
	}
}

//! Text list
/*! This class store text and data of lines internally
    You may use it to show text with colorised lines :) */
public class TextList extends VirtualList
{
	// Vector of lines. Each line contains cols. Col can be text or image
	private Vector lines = new Vector();
	private int textSelColor = 0xE0E0E0;

	// protected int getSize()
	public int getSize()
	{
		if (lines.isEmpty()) return 0;
		int size = lines.size();
		return ((TextLine)lines.lastElement()).items.isEmpty() ? size-1 : size;
	}

	private TextLine getLine(int index)
	{
		return (TextLine)lines.elementAt(index);
	}

	public void setTextSelColor(int value)
	{
		if (textSelColor == value) return;
		textSelColor = value;
		invalidate();
	}

	protected int getItemBkColor(int index, int lastColor)
	{
		int selIndex = getCurrIndex();
		int textIndex = (selIndex >= lines.size()) ?
				-1 : getLine(selIndex).bigTextIndex;
		if (textIndex == -1) return lastColor;
		int retColor = (getLine(index).bigTextIndex == textIndex) ? textSelColor : lastColor;
		return retColor;
	}

	// protected void get(int index, ListItem item)
	protected void get(int index, ListItem item)
	{
		TextLine listItem = getLine(index);
		if (listItem.items.isEmpty()) item.clear();
		else listItem.elementAt(0).assignTo(item);
	}

	//! Remove all lines form list
	public void clear()
	{
		lines.removeAllElements();
		setCurrentItem(0);
	}

	//! Add new text item to list
	public void add
	(
		String text,   //!< Text of new item
		int color,     //!< Color of new item
		int imageIndex /*!< Index of image in images list. You must use 
	                        setImageList to set images for list lines */
	)
	{
		internAdd(text, color, imageIndex, Font.STYLE_PLAIN, -1, true);
		invalidate();
	}

	//! Add new text item to list
	public void add
	(
		String text,    //!< Text of new item
		int color,      //!< Color of new item
		int imageIndex, /*!< Index of image in images list. You must use 
		                     setImageList to set images for list lines */
		int fontStyle   //!< Text font style. See MID profile for details
	)
	{
		internAdd(text, color, imageIndex, fontStyle, -1, true);
		invalidate();
	}

	private void internAdd
	(
		String text,
		int color,
		int imageIndex,
		int fontStyle,
		int textIndex,
		boolean doCRLF)
	{
		ListItem new_item = new ListItem();
		new_item.color = color;
		new_item.imageIndex = imageIndex;
		new_item.text = text;
		new_item.fontStyle = fontStyle;

		if (lines.isEmpty()) lines.addElement(new TextLine());
		TextLine textLine = (TextLine) lines.lastElement();
		textLine.items.addElement(new_item);
		textLine.bigTextIndex = textIndex;
		if (doCRLF) lines.addElement(new TextLine());
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
	public TextList(String capt //!< Caption of list
	)
	{
		super(capt);
	}
	
	public int getItemHeight(int itemIndex)
	{
		if (getCursorMode() != SEL_NONE) return super.getItemHeight(itemIndex);
		if (itemIndex >= lines.size()) return 1;
		return getLine(itemIndex).getHeight( getFontSize() );
	}
	
	// Overrides VirtualList.drawItemData
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
		if (getCursorMode() != SEL_NONE)
		{
			super.drawItemData(g, isSelected, index, x1, y1, x2, y2, fontHeight);
			return;
		}
		
		TextLine line = getLine(index);
		line.paint(1, y1, g, getFontSize(), this);
	}
	

	// Overrides VirtualList.moveCursor
	protected void moveCursor(int step, boolean moveTop)
	{
		int size, changeCounter = 0, currTextIndex, i, halfSize = getVisCount()/2;

		switch (step)
		{
		case -1:
		case 1:
			currTextIndex = getCurrTextIndex();
			size = lines.size();
			if (currTextIndex == -1)
			{
				super.moveCursor(step, true);
				return;
			}

			storelastItemIndexes();

			for (i = 0; i < halfSize;)
			{
				currItem += step;
				if ((currItem < 0) || (currItem >= size)) break;
				TextLine item = getLine(currItem);
				if (currTextIndex != item.bigTextIndex)
				{
					currTextIndex = item.bigTextIndex;
					changeCounter++;
					if ((changeCounter == 2) || (!visibleItem(currItem) && (i > 0)))
					{
						currItem -= step;
						break;
					}
				}
				
				if (!visibleItem(currItem) || (changeCounter != 0)) i++;
			}

			checkCurrItem();
			checkTopItem();
			repaintIfLastIndexesChanged();
			break;

		default:
			super.moveCursor(step, moveTop);
			return;
		}
	}

	// Returns lines of text which were added by 
	// methon addBigText in current selection
	public String getCurrText(int offset)
	{
		int offsetCounter = 0;
		StringBuffer result = new StringBuffer();
		int currTextIndex = getCurrTextIndex();
		
		// Fills the lines
		int size = lines.size();
		for (int i = 0; i < size; i++)
		{
			TextLine line = getLine(i);
			if (line.bigTextIndex == currTextIndex)
			{
				if (offset != offsetCounter)
				{
					offsetCounter++;
					continue;
				}
				int count = line.items.size(); 
				for (int k = 0; k < count; k++) result.append(line.elementAt(k).text);
				result.append(' ');
			}
		}
		
		return (result.length() == 0) ? null : result.toString().trim();
	}

	public int getCurrTextIndex()
	{
		int currItemIndex = getCurrIndex();
		if ((currItemIndex < 0) || (currItemIndex >= lines.size())) return -1;
		return getLine(currItemIndex).bigTextIndex;
	}

	//! Construct new text list 
	public TextList
	(
		String capt,      //!< Caption of list
		int capTextColor, //!< Text color of caption
		int backColor,    //!< Background color of list
		int fontSize,     /*!< Font size for list lines and caption. 
		                       Can be VirtualList.SMALL_FONT, VirtualList.MEDIUM_FONT 
		                       or VirtualList.LARGE_FONT */
		int cursorMode    //!< Cursor mode. Can be VirtualList.SEL_INVERTED, VirtualList.SEL_DOTTED, VirtualList.SEL_NONE
	)
	{
		super(capt, capTextColor, backColor, fontSize, cursorMode);
	}

	public void setTextColor(int value)
	{
		Enumeration allLines = lines.elements();
		while (allLines.hasMoreElements())
			((TextLine) allLines.nextElement()).setItemColor(value);
		super.setTextColor(value);
	}
	
	public TextList doCRLF()
	{
		TextLine newLine = new TextLine();
		newLine.bigTextIndex = lines.isEmpty() ? -1 : ((TextLine) lines.lastElement()).bigTextIndex; 
		lines.addElement(newLine);
		return this;
	}
	
	public TextList addImage(Image image, String altarnateText)
	{
		if (lines.isEmpty()) lines.addElement(new TextLine());
		TextLine textLine = (TextLine) lines.lastElement();
		
		if ((textLine.getWidth(getFontSize())+image.getWidth()) > getTrueWidth())
		{
			doCRLF();
			textLine = (TextLine) lines.lastElement();
		}
		
		ListItem item = new ListItem();
		item.image = image;
		item.text = altarnateText;
		textLine.items.addElement(item);
		return this;
	}

	private int getTrueWidth()
	{
		return getWidth()-scrollerWidth-3;
	}
	
	private void addBigTextInternal
	(
		String text,  
		int color,    
		int fontStyle,
		int textIndex,
		int trueWidth
	)
	{
		Font font;
		int textLen, curPos, lastWordEnd, startPos, width, testStringWidth = 0;
		char curChar;
		boolean lineBreak, wordEnd, textEnd, divideLineToWords;
		String testString = null;

		font = getQuickFont(fontStyle);
		
		// Width of free space in last line 
		width = lines.isEmpty() 
		        ? trueWidth 
		        : trueWidth-((TextLine)lines.lastElement()).getWidth( getFontSize() );
		
		// Start pos of new line
		startPos = 0;
		
		// Pos of last word end
		lastWordEnd = -1;
		
		textLen = text.length();
		for (curPos = 0; curPos < textLen;)
		{
			curChar = text.charAt(curPos);
			wordEnd = (curChar == ' ');
			lineBreak = (curChar == '\n') || (curChar == '\r'); // ???
			textEnd = (curPos == (textLen - 1));
			divideLineToWords = false;
			if (textEnd) curPos++;
			
			if (lineBreak || textEnd || wordEnd)
			{
				testString = text.substring(startPos, curPos);
				testStringWidth = font.stringWidth(testString);
			}
			
			// simply add line
			if ((lineBreak || textEnd) && (testStringWidth <= width)) 
			{
				//System.out.println("*1*");
				internAdd(testString, color, -1, fontStyle, textIndex, lineBreak);
				width = trueWidth;
				curPos++;
				startPos = curPos;
				lastWordEnd = -1;
				continue;
			}
			
			if ((lineBreak || textEnd || wordEnd) && (testStringWidth > width))
			{
				if ((testStringWidth < trueWidth) && (lastWordEnd != -1))
				{
					//System.out.println("*3*");
					divideLineToWords = true;
				}
				
				// Insert new line and try again
				else if (trueWidth != width)
				{
					//System.out.println("*2*");
					doCRLF();
					curPos = startPos;
					width = trueWidth;
					lastWordEnd = -1;
					continue;
				}
			}
			
			if ((lineBreak || textEnd || wordEnd) && (testStringWidth > trueWidth) && (!divideLineToWords))
			{
				// divide big word to several lines
				if (lastWordEnd == -1)
				{
					//System.out.println("*4*");
					for (; curPos >= 1; curPos--)
					{
						testString = text.substring(startPos, curPos);
						if (font.stringWidth(testString) <= width) break;
					}
					internAdd(testString, color, -1, fontStyle, textIndex, true);
					width = trueWidth;
					startPos = curPos;
					lastWordEnd = -1;
					continue;
				}
				
				// several words in line
				else
				{
					//System.out.println("*5*");
					divideLineToWords = true;
				}
			}
			
			if (divideLineToWords)
			{
				String insString = text.substring(startPos, lastWordEnd);
				internAdd(insString, color, -1, fontStyle, textIndex, true);
				curPos = lastWordEnd + 1;
				startPos = curPos;
				width = trueWidth;
				lastWordEnd = -1;
				continue;
			}
			
			if (wordEnd) lastWordEnd = curPos;
			curPos++;
		}
	}
	
	//! Add big multiline text. 
	/*! Text visial width can be larger then screen width.
	    Method addBigText automatically divides text to short lines 
	    and adds lines to text list */
	public TextList addBigText
	(
		String text,   //!< Text to add
		int color,     //!< Text color
		int fontStyle, //!< Text font style. See MID profile for details
		int textIndex  //!< Whole text index
	)
	{
		addBigTextInternal(text, color, fontStyle, textIndex, getTrueWidth());
		return this;
	}
	
    static public int getLineNumbers(String s, int width, int fontSize, int fontStyle, int textColor)
    {
        TextList paintList = new TextList(null);
        paintList.setFontSize(fontSize);
        paintList.addBigTextInternal(s, textColor, fontStyle, -1, width);
        
        return (paintList.getSize());
    }
    
	static public void showText
	(
		Graphics g, 
		String s, 
		int x, int y, int width, int height, 
		int fontSize, int fontStyle, int textColor
	)
	{
		TextList paintList = new TextList(null);
		paintList.setFontSize(fontSize);
		paintList.addBigTextInternal(s, textColor, fontStyle, -1, width);
		
		int line, textHeight = 0;
		int linesCount = paintList.getSize();
		for (line = 0; line < linesCount; line++) textHeight += paintList.getLine(line).getHeight(fontSize);
		int top = y+(height-textHeight)/2;
		for (line = 0; line < linesCount; line++)
		{
			paintList.getLine(line).paint(x, top, g, fontSize, paintList);
			top += paintList.getLine(line).getHeight(fontSize);
		}
	}
}