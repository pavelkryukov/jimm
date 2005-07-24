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

//! Text list
/*! This class store text and data of items internally
    You may use it to show text with colorised lines :) */
public class TextList extends VirtualList
{
	private Vector items = new Vector();

	// protected int getSize()
	public int getSize()
	{
		return items.size();
	}

	public int getItemCount()
	{
		return items.size();
	}

	private int textSelColor = 0xE0E0E0;

	public void setTextSelColor(int value)
	{
		if (textSelColor == value) return;
		textSelColor = value;
		invalidate();
	}

	protected int getItemBkColor(int index, int lastColor)
	{
		int selIndex = getCurrIndex();
		int textIndex = (selIndex >= items.size()) ? -1 : ((ListItem) items
				.elementAt(selIndex)).bigTextIndex;
		if (textIndex == -1) return lastColor;
		int retColor = (((ListItem) items.elementAt(index)).bigTextIndex == textIndex) ? textSelColor : lastColor;
		return retColor;
	}

	// protected void get(int index, ListItem item)
	protected void get(int index, ListItem item)
	{
		ListItem listItem = (ListItem) items.elementAt(index);

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
		internAdd(text, color, imageIndex, Font.STYLE_PLAIN, -1);
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
		internAdd(text, color, imageIndex, fontStyle, -1);
		invalidate();
	}

	private void internAdd
	(
		String text,
		int color,
		int imageIndex,
		int fontStyle,
		int textIndex)
	{
		ListItem new_item = new ListItem();
		new_item.color = color;
		new_item.imageIndex = imageIndex;
		new_item.text = text;
		new_item.fontStyle = fontStyle;
		new_item.bigTextIndex = textIndex;
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
	public TextList(String capt //!< Caption of list
	)
	{
		super(capt);
	}

	// Overrides VirtualList.moveCursor
	protected void moveCursor(int step)
	{
		int size, changeCounter = 0, currTextIndex, i, halfSize = getVisCount()/2;

		switch (step)
		{
		case -1:
		case 1:
			currTextIndex = getCurrTextIndex();
			size = items.size();
			if (currTextIndex == -1)
			{
				super.moveCursor(step);
				return;
			}

			storelastItemIndexes();

			for (i = 0; i < halfSize;)
			{
				currItem += step;
				if ((currItem < 0) || (currItem >= size)) break;
				ListItem item = (ListItem) items.elementAt(currItem);
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
			super.moveCursor(step);
			return;
		}
	}

	// Returns lines of text which were added by 
	// methon addBigText in current selection
	synchronized public String[] getCurrText()
	{
		String[] result = null;
		int currTextIndex = getCurrTextIndex();
		if (currTextIndex != -1)
		{
			// How much lines in current text?
			int linesCount = 0;
			Enumeration allItems = items.elements();
			while (allItems.hasMoreElements())
				if (((ListItem) allItems.nextElement()).bigTextIndex == currTextIndex) linesCount++;

			// Fills the lines
			result = new String[linesCount];
			int size = items.size();
			for (int i = 0, j = 0; i < size; i++)
			{
				ListItem listItem = (ListItem) items.elementAt(i);
				if (listItem.bigTextIndex == currTextIndex) result[j++] = listItem.text;
			}
		}
		return result;
	}

	public int getCurrTextIndex()
	{
		int currItemIndex = getCurrIndex();
		if (currItemIndex < 0) return -1;
		return ((ListItem) items.elementAt(currItemIndex)).bigTextIndex;
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
		int cursorMode    //!< Cursor mode. Can be VirtualList.SEL_INVERTED, VirtualList.SEL_DOTTED, VirtualList.SEL_NONE
	)
	{
		super(capt, capBackColor, capTextColor, backColor, fontSize, cursorMode);
	}

	public void setTextColor(int value)
	{
		int count = items.size();
		for (int i = 0; i < count; i++)
		{
			ListItem listItem = (ListItem) items.elementAt(i);
			if ((listItem.color == 0x000000) || (listItem.color == 0xFFFFFF)) listItem.color = value;

		}
		super.setTextColor(value);
	}

	//! Add big multiline text. 
	/*! Text visial width can be larger then screen width.
	    Method addBigText automatically divides text to short lines 
	    and adds lines to text list */
	public void addBigText
	(
		String text,   //!< Text to add
		int color,     //!< Text color
		int fontStyle, //!< Text font style. See MID profile for details
		int textIndex  //!< Whole text index
	)
	{
		Font font;
		int textLen, curPos, lastWordEnd, startPos, width;
		char curChar;
		boolean lineBreak, wordEnd, textEnd;
		String testString;

		width = getWidth() - getScrollerWidth() - 3;
		startPos = 0;
		lastWordEnd = -1;
		font = createFont(fontStyle);
		textLen = text.length();
		for (curPos = 0; curPos < textLen;)
		{
			curChar = text.charAt(curPos);
			wordEnd = (curChar == ' ');
			lineBreak = (curChar == '\n') || (curChar == '\r'); // ???
			textEnd = (curPos == (textLen - 1));
			if (textEnd) curPos++;

			if (lineBreak || textEnd) // simply add line
			{
				testString = text.substring(startPos, curPos);
				if (font.stringWidth(testString) <= width)
				{
					internAdd(testString, color, -1, fontStyle, textIndex);
					curPos++;
					startPos = curPos;
					lastWordEnd = -1;
					continue;
				}
			}

			if (wordEnd || lineBreak || textEnd)
			{
				testString = text.substring(startPos, curPos);
				if (font.stringWidth(testString) > width)
				{
					if (lastWordEnd != -1) // several words in line
					{
						internAdd(text.substring(startPos, lastWordEnd), color, -1, fontStyle, textIndex);
						curPos = lastWordEnd + 1;
						startPos = curPos;
					}
					else
					// divide big word to several lines
					{
						for (; curPos >= 1; curPos--)
						{
							testString = text.substring(startPos, curPos);
							if (font.stringWidth(testString) <= width) break;
						}
						internAdd(testString, color, -1, fontStyle, textIndex);
						startPos = curPos;
					}
					lastWordEnd = -1;
					continue;
				}
			}
			if (wordEnd) lastWordEnd = curPos;
			curPos++;
		}
	}
}