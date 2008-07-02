/*******************************************************************************
 Library of additional graphical screens for J2ME applications
 Copyright (C) 2003-08  Jimm Project

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
********************************************************************************
 File: src/DrawControls/TextList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
*******************************************************************************/

package DrawControls;

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.microedition.lcdui.*;

import DrawControls.VirtualList;
import DrawControls.ListItem;

class TextItem
{
	byte[] imgNumsAndTimes;
	Image[] image;
	String text;
	int fontAndColor = 0;
	short aniStepOrWidth;
	short timeCounterOrHeight;
	
	int getHeight(int fontSize)
	{
		if (image != null) return image[0].getHeight();
		if (text == null) return 0;
		if (timeCounterOrHeight == 0)
		{
			Font font = Font.getFont(Font.FACE_SYSTEM, (fontAndColor >> 24)&0xFF, fontSize);
			timeCounterOrHeight = (short)font.getHeight();
		}
		return timeCounterOrHeight;
	}
	
	int getWidth(int fontSize)
	{
		if (image != null) return image[0].getWidth();
		if (text == null) return 0;
		if (aniStepOrWidth == 0)
		{
			Font font = Font.getFont(Font.FACE_SYSTEM, (fontAndColor >> 24)&0xFF, fontSize);
			aniStepOrWidth = (short)font.stringWidth(text);
		}
		return aniStepOrWidth;
	}
	
	int getColor()
	{
		return fontAndColor&0xFFFFFF;
	}
	
	void setColor(int value)
	{
		fontAndColor = (fontAndColor&0xFF000000) | (value&0x00FFFFFF); 
	}
	
	int getFontStyle()
	{
		return (fontAndColor&0xFF000000) >> 24;
	}
	
	void setFontStyle(int value)
	{
		fontAndColor = (fontAndColor&0x00FFFFFF)|((value&0xFF) << 24);
	}
	
	boolean replaceImage(Image from, Image to)
	{
		if (image == null) return false;
		if (image[0] == from)
		{
			image[0] = to;
			return true;
		}
		return false;
	}
}

class TextLine
{
	private Vector items = new Vector();

	int height = -1;

	int bigTextIndex = -1;

	char last_charaster;

	TextItem elementAt(int index)
	{
		return (TextItem)items.elementAt(index);
	}
	
	void add(TextItem item)
	{
		items.addElement(item);
	}

	int getHeight(int fontSize)
	{
		if (height == -1)
		{
			height = fontSize;
			int currHeight;
			for (int i = items.size() - 1; i >= 0; i--)
			{
				currHeight = elementAt(i).getHeight(fontSize);
				if (currHeight > height) height = currHeight;
			}
		}
		return height;
	}

	int getWidth(int fontSize)
	{
		int width = 0;
		for (int i = items.size() - 1; i >= 0; i--)
			width += elementAt(i).getWidth(fontSize);
		return width;
	}

	void setItemColor(int value)
	{
		TextItem listItem;
		for (int i = items.size() - 1; i >= 0; i--)
		{
			listItem = elementAt(i);
			listItem.setColor(value);
		}
		listItem = null;
	}

	void paint(int xpos, int ypos, Graphics g, int fontSize, VirtualList vl, boolean nextAniStep)
	{
		int count = items.size();
		int itemHeight = getHeight(fontSize);

		TextItem item;
		int drawYPos;
		int imgIndex;
		Image img;
		for (int i = 0; i < count; i++)
		{
			item = elementAt(i);
			drawYPos = ypos + (itemHeight - item.getHeight(fontSize))/2;
			if (item.image != null)
			{
				imgIndex = (item.imgNumsAndTimes == null) ? 0 : item.imgNumsAndTimes[2*item.aniStepOrWidth];
				img = item.image[imgIndex];
				if (g != null) g.drawImage(img, xpos, drawYPos, Graphics.TOP | Graphics.LEFT);
				
				if (nextAniStep && item.image.length > 1)
				{
					item.timeCounterOrHeight++;
					if (item.timeCounterOrHeight > item.imgNumsAndTimes[2*item.aniStepOrWidth+1])
					{
						item.timeCounterOrHeight = 0;
						item.aniStepOrWidth++;
						if (item.aniStepOrWidth >= item.imgNumsAndTimes.length/2) item.aniStepOrWidth = 0;
						vl.getCanvas().repaint(xpos, drawYPos, img.getWidth(), img.getHeight());
					}
				}
			}
			
			else if (item.text != null && !nextAniStep && g != null)
			{
				g.setColor(item.getColor());
				g.setFont(vl.getQuickFont(item.getFontStyle()));
				g.drawString(item.text, xpos, drawYPos, Graphics.TOP | Graphics.LEFT);
			}
			
			xpos += item.getWidth(fontSize);
		}
	}
	
	int size()
	{
		return items.size();
	}
	
	void readText(StringBuffer buffer)
	{
		for (int i = 0; i < items.size(); i++) buffer.append(elementAt(i).text);
	}
	
	boolean replaceImages(Image from, Image to)
	{
		boolean replaced = false;
		for (int i = items.size()-1; i >= 0; i--) 
			replaced |= elementAt(i).replaceImage(from, to);
		return replaced;
	}
	
}

//! Text list
/*! This class store text and data of lines internally
 You may use it to show text with colorised lines :) */
public class TextList extends VirtualList implements Runnable
{
	private boolean alwaysShowCursor;
	private boolean animated;
	private static TimerTask aniTimerTask;
	private static Timer aniTimer = new Timer();
	private Vector lines = new Vector(); // Vector of lines. Each line contains cols. Col can be text or image
	
	
	//! Construct new text list 
	public TextList
	(
		String capt, //!< Caption of list
		int capTextColor, //!< Text color of caption
		int backColor, //!< Background color of list
		int fontSize, /*!< Font size for list lines and caption. 
					 Can be VirtualList.SMALL_FONT, VirtualList.MEDIUM_FONT 
					 or VirtualList.LARGE_FONT */
		int cursorMode //!< Cursor mode. Can be VirtualList.SEL_INVERTED, VirtualList.SEL_DOTTED, VirtualList.SEL_NONE
	)
	{
		super(capt, capTextColor, backColor, fontSize, cursorMode);
	}
	
	//! Construct new text list with default values of colors, font size etc...
	public TextList(String capt)
	{
		super(capt);
	}
	
	// protected int getSize()
	public int getSize()
	{
		if (lines.isEmpty()) return 0;
		int size = lines.size();
		return (((TextLine) lines.lastElement()).size() == 0) ? size - 1 : size;
	}
	
	public void setAlwaysShowCursor(boolean value)
	{
		alwaysShowCursor = value;
	}

	private TextLine getLine(int index)
	{
		return (TextLine) lines.elementAt(index);
	}

	protected boolean isItemSelected(int index)
	{
		int selIndex = getCurrIndex();
		int textIndex = (selIndex >= lines.size() || selIndex < 0) ? -1 : getLine(selIndex).bigTextIndex;
		if (textIndex == -1) return false;
		return (getLine(index).bigTextIndex == textIndex);
	}

	// protected void get(int index, ListItem item)
	protected void get(int index, ListItem item)
	{
		TextLine listItem = getLine(index);
		item.clear();
		if (listItem.size() == 0) return;
		
		TextItem titem = listItem.elementAt(0);
		item.text = titem.text;
		item.color = titem.getColor();
		item.fontStyle = titem.getFontStyle();
	}

	//! Remove all lines form list
	public void clear()
	{
		lines.removeAllElements();
		setCurrentItem(0);
		animated = false;
		resetAnimationTask();
		invalidate();
	}

	//! Add new text item to list
	public void add(String text, //!< Text of new item
		int color, //!< Color of new item
		int imageIndex /*!< Index of image in images list. You must use 
	 setImageList to set images for list lines */
	)
	{
		internAdd(text, color, imageIndex, Font.STYLE_PLAIN, -1, true, '\0');
		invalidate();
	}

	//! Add new text item to list
	public void add(String text, //!< Text of new item
		int color, //!< Color of new item
		int imageIndex, /*!< Index of image in images list. You must use 
					 setImageList to set images for list lines */
		int fontStyle //!< Text font style. See MID profile for details
	)
	{
		internAdd(text, color, imageIndex, fontStyle, -1, true, '\0');
		invalidate();
	}

	private void internAdd(String text, int color, int imageIndex, int fontStyle, int textIndex, boolean doCRLF, char last_charaster)
	{
		TextItem newItem = new TextItem();
		
		newItem.text = text;
		newItem.setColor(color);
		newItem.setFontStyle(fontStyle);

		if (lines.isEmpty()) lines.addElement(new TextLine());
		TextLine textLine = (TextLine) lines.lastElement();
		textLine.add(newItem);
		textLine.bigTextIndex = textIndex;
		if (doCRLF)
		{
			textLine.last_charaster = last_charaster;
			TextLine newLine = new TextLine();
			newLine.bigTextIndex = textIndex;
			lines.addElement(newLine);
		}
	}

	//! Add new black text item to list
	public void add(String text //!< Text of new item
	)
	{
		add(text, this.getTextColor(), -1);
	}

	public int getItemHeight(int itemIndex)
	{
		if (getCursorMode() != CURSOR_MODE_DISABLED) return super.getItemHeight(itemIndex);
		if (itemIndex >= lines.size()) return 1;
		return getLine(itemIndex).getHeight(getFontSize());
	}

	// Overrides VirtualList.drawItemData
	protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int paintMode)
	{
		if (getCursorMode() != CURSOR_MODE_DISABLED)
		{
			super.drawItemData(g, index, x1, y1, x2, y2, paintMode);
			return;
		}

		TextLine line = getLine(index);
		line.paint(borderWidth, y1, g, getFontSize(), this, paintMode == DMS_CUSTOM);
	}

	// Overrides VirtualList.moveCursor
	protected void moveCursor(int step, boolean moveTop)
	{
		int size, currTextIndex, lastCurItem;
		int halfSize = (getDrawHeight()/getFontHeight())/2;

		switch (step)
		{
		case -1:
		case 1:
			lastCurItem = currItem;
			currTextIndex = getCurrTextIndex();
			size = getSize();
			
			storelastItemIndexes();
			
			// Find next item
			TextLine item;
			for (; halfSize > 0; halfSize--)
			{
				currItem += step;
				if ((currItem < 0) || (currItem >= size)) break;
				item = getLine(currItem);
				if ((currTextIndex != item.bigTextIndex) && (item.bigTextIndex != -1))
				{
					currTextIndex = item.bigTextIndex;
					break;
				}
			}
			
			// Find next item last line
			if ((halfSize != 0) && (currItem >= 0) && (currItem < size))
			{
				for (; halfSize > 0; halfSize--)
				{
					currItem += step;
					if ((currItem < 0) || (currItem >= size))
					{
						currItem -= step;
						break;
					}
					item = getLine(currItem);
					if (currTextIndex != item.bigTextIndex)
					{
						currItem -= step;
						break;
					}
				}
			}
			item = null;

			checkCurrItem();
			checkTopItem();
			checkTopItem();
			
			if (alwaysShowCursor && getLine(currItem).bigTextIndex == -1) 
				currItem = lastCurItem;
			
			repaintIfLastIndexesChanged();
			break;

		default:
			super.moveCursor(step, moveTop);
			return;
		}
	}
	
	public void selectFirstTextIndex()
	{
		int size = lines.size();
		TextLine line;
		for (int i = 0; i < size; i++)
		{
			line = getLine(i);
			if (line.bigTextIndex == -1) continue;
			setCurrentItem(i);
			break;
		}
		line = null;
	}
	
	public String getTextByIndex(int offset, boolean wholeText, int textIndex)
	{
		StringBuffer result = new StringBuffer();
		
		// Fills the lines
		int size = lines.size();
		TextLine line;
		for (int i = 0; i < size; i++)
		{
			line = getLine(i);
			if (wholeText || (textIndex == -1) || (line.bigTextIndex == textIndex))
			{
				line.readText(result);
				if (line.last_charaster != '\0')
				{
					if (line.last_charaster == '\n') result.append("\n");
					else result.append(line.last_charaster);
				}
			}
		}
		line = null;
		
		if (result.length() == 0) return null;
		String resultText = result.toString();
		int len = resultText.length();
		if (offset > len) return null;
		return resultText.substring(offset, len);
	}
	
	public void selectTextByIndex(int textIndex)
	{
		if (textIndex == -1) return;
		int size = lines.size();
		for (int i = 0; i < size; i++)
		{
			if (getLine(i).bigTextIndex == textIndex)
			{
				setCurrentItem(i);
				break;
			}
		}
	}

	// Returns lines of text which were added by 
	// methon addBigText in current selection
	public String getCurrText(int offset, boolean wholeText)
	{
		return getTextByIndex(offset, wholeText, getCurrTextIndex());
	}

	public int getCurrTextIndex()
	{
		int currItemIndex = getCurrIndex();
		if ((currItemIndex < 0) || (currItemIndex >= lines.size())) return -1;
		return getLine(currItemIndex).bigTextIndex;
	}

	public void setColors(int capTxt, int capbk, int bkgrnd, int cursor, int text, int crsFrame, int cursorAlpha, int menuAlpha)
	{
		if (getTextColor() != text)
		{
			Enumeration allLines = lines.elements();
			while (allLines.hasMoreElements())
				((TextLine) allLines.nextElement()).setItemColor(text);
		}
		super.setColors(capTxt, capbk, bkgrnd, cursor, text, crsFrame, cursorAlpha, menuAlpha);
	}

	public TextList doCRLF(int blockTextIndex)
	{
		if (lines.size() != 0) ((TextLine) lines.lastElement()).last_charaster = '\n';
		TextLine newLine = new TextLine();
		newLine.bigTextIndex = blockTextIndex;
		lines.addElement(newLine);
		return this;
	}
	
	public TextList addAniImage(Image[] image, byte[] imgNumsAndTimes, String altarnateText, int blockTextIndex)
	{
		if (lines.isEmpty()) lines.addElement(new TextLine());
		TextLine textLine = (TextLine) lines.lastElement();
		textLine.bigTextIndex = blockTextIndex;
		
		if ((textLine.getWidth(getFontSize()) + image[0].getWidth()) > getTextAreaWidth())
		{
			doCRLF(blockTextIndex);
			textLine = (TextLine) lines.lastElement();
		}

		TextItem newItem = new TextItem();
		newItem.image = image;
		newItem.imgNumsAndTimes = imgNumsAndTimes;
		newItem.text = altarnateText;
		textLine.add(newItem);
		
		boolean lastAnimated = animated; 
		animated |= (image.length > 1);
		if (lastAnimated != animated) startAnimationTask();
		
		return this;
	}

	public TextList addImage(Image image, String altarnateText, int blockTextIndex)
	{
		return addAniImage(new Image[] {image}, null, altarnateText, blockTextIndex);
	}

	private int getTextAreaWidth()
	{
		return getWidthInternal() - scrollerWidth - borderWidth*2;
	}

	private static String replace(String text, String from, String to)
	{
		int fromSize = from.length();
		int pos;
		for (;;)
		{
			pos = text.indexOf(from);
			if (pos == -1) break;
			text = text.substring(0, pos) + to + text.substring(pos + fromSize, text.length());
		}
		return text;
	}

	private void addBigTextInternal(String text, int color, int fontStyle, int textIndex, int trueWidth)
	{
		Font font;
		int textLen, curPos, lastWordEnd, startPos, width, testStringWidth = 0;
		char curChar;
		boolean lineBreak, wordEnd, textEnd, divideLineToWords;
		String testString = null;

		if (text == null)
			return;
		// Replace '\r\n' charasters with '\n'
		text = replace(text, "\r\n", "\n");

		// Replace '\r' charasters with '\n'
		text = replace(text, "\r", "\n");

		font = getQuickFont(fontStyle);

		// Width of free space in last line 
		width = lines.isEmpty() ? trueWidth : trueWidth - ((TextLine) lines.lastElement()).getWidth(getFontSize());

		// Start pos of new line
		startPos = 0;

		// Pos of last word end
		lastWordEnd = -1;

		textLen = text.length();
		String insString;
		for (curPos = 0; curPos < textLen;)
		{
			curChar = text.charAt(curPos);
			wordEnd = (curChar == ' ');
			lineBreak = (curChar == '\n');
			textEnd = (curPos == (textLen - 1));
			divideLineToWords = false;
			if (textEnd && (!lineBreak)) curPos++;

			if (lineBreak || textEnd || wordEnd)
			{
				testString = text.substring(startPos, curPos);
				testStringWidth = font.stringWidth(testString);
			}

			// simply add line
			if ((lineBreak || textEnd) && (testStringWidth <= width))
			{
				internAdd(testString, color, -1, fontStyle, textIndex, lineBreak, lineBreak ? '\n' : ' ');
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
					divideLineToWords = true;
				}

				// Insert new line and try again
				else if ((trueWidth != width) && (lastWordEnd == -1))
				{
					doCRLF(textIndex);
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
					for (; curPos >= 1; curPos--)
					{
						testString = text.substring(startPos, curPos);
						if (font.stringWidth(testString) <= width) break;
					}
					internAdd(testString, color, -1, fontStyle, textIndex, true, '\0');
					width = trueWidth;
					startPos = curPos;
					lastWordEnd = -1;
					continue;
				}

				// several words in line
				else
				{
					divideLineToWords = true;
				}
			}

			if (divideLineToWords)
			{
				insString = text.substring(startPos, lastWordEnd);
				internAdd(insString, color, -1, fontStyle, textIndex, true, ' ');
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
	public TextList addBigText(String text, //!< Text to add
		int color, //!< Text color
		int fontStyle, //!< Text font style. See MID profile for details
		int textIndex //!< Whole text index
	)
	{
		addBigTextInternal(text, color, fontStyle, textIndex, getTextAreaWidth());
		invalidate();
		return this;
	}
	
	public boolean replaceImages(int textIndex, Image from, Image to)
	{
		boolean replaced = false;
		TextLine textLine;
		for (int i = getSize()-1; i >= 0; i--)
		{
			textLine = (TextLine) lines.elementAt(i);
			if (textLine.bigTextIndex != textIndex) continue;
			replaced |= textLine.replaceImages(from, to);
		}
		textLine = null;
		return replaced;
	}

	// TODO: full rewrite code below!
	static public int getLineNumbers(String s, int width, int fontSize, int fontStyle, int textColor)
	{
		TextList paintList = new TextList(null);
		paintList.setFontSize(fontSize);
		paintList.addBigTextInternal(s, textColor, fontStyle, -1, width);

		return (paintList.getSize());
	}

	static public void showText(Graphics g, String s, int x, int y, int width, int height, int fontSize, int fontStyle, int textColor)
	{
		TextList paintList = new TextList(null);
		paintList.setFontSize(fontSize);
		paintList.addBigTextInternal(s, textColor, fontStyle, -1, width);

		int line, textHeight = 0;
		int linesCount = paintList.getSize();
		for (line = 0; line < linesCount; line++)
			textHeight += paintList.getLine(line).getHeight(fontSize);
		int top = y + (height - textHeight) / 2;
		for (line = 0; line < linesCount; line++)
		{
			paintList.getLine(line).paint(x, top, g, fontSize, paintList, false);
			top += paintList.getLine(line).getHeight(fontSize);
		}
	}
	
	protected void onShow() 
	{
		if (animated)
		{
			startAnimationTask();
		}
		else if (!animated && (aniTimerTask != null))
		{
			aniTimerTask.cancel();
			aniTimerTask = null;
		}
	}
	
	public void run()
	{
		int menuBarHeight;
//#sijapp cond.if target="RIM" | target="DEFAULT"#
		menuBarHeight = 0;
//#sijapp cond.else#		
		menuBarHeight = getMenuBarHeight();
//#sijapp cond.end#
		
		drawItems(null, getCapHeight(), menuBarHeight, DMS_CUSTOM, -1, -1, -1, -1);
	}
	
	protected void onHide() 
	{
		resetAnimationTask();
	}
	
	private void startAnimationTask()
	{
		if (aniTimerTask != null) aniTimerTask.cancel();
		
		aniTimerTask = new TimerTask() 
		{
			public void run()
			{
				getDisplay().callSerially(TextList.this);
			}
		};
		
		aniTimer.schedule(aniTimerTask, 100, 100);
		
		//System.out.println("startAnimationTask");
	}
	
	private void resetAnimationTask()
	{
		if (aniTimerTask != null)
		{
			aniTimerTask.cancel();
			aniTimerTask = null;
			//System.out.println("resetAnimationTask");
		}
	}
	
	public void addTextItem(String string, Image image, int index, int fontStyle)
	{
		if (fontStyle == -1) fontStyle = Font.STYLE_PLAIN;
		if (image != null)
		{
			addImage(image, null, index);
			addBigText(" ", getTextColor(), fontStyle, index);
		}
		addBigText(string, getTextColor(), fontStyle, index);
		doCRLF(index);
	}
	
	public int getLastTextIndex()
	{
		int size = getSize();
		if (size == 0) return -1;
		return getLine(size-1).bigTextIndex; 
	}
	
}