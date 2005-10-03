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
 File: src/jimm/Emotions.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

package jimm;

import java.util.Vector;
import javax.microedition.lcdui.*;

import java.io.*;

import jimm.util.ResourceBundle;
import DrawControls.*;

public class Emotions implements VirtualListCommands, CommandListener
{
	final private ImageList images = new ImageList();
	final private Vector findedEmotions = new Vector();
	boolean used;
	final private Vector textCorr = new Vector();
	final private Vector selEmotions = new Vector();
	
	public Emotions()
	{
		int iconsSize;
		used = false;

		// Load file "smiles.txt"
		InputStream stream = this.getClass().getResourceAsStream("/smiles.txt");
		if (stream == null) return;
		
		DataInputStream dos = new DataInputStream(stream); 

		try
		{
			StringBuffer strBuffer = new StringBuffer();
			boolean eof = false, clrf = false;
			
			// Read icon size
			readStringFromStream(strBuffer, dos);
			iconsSize = Integer.parseInt(strBuffer.toString());
			
			for (;;)
			{
				// Read smile index
				readStringFromStream(strBuffer, dos);
				Integer currIndex = Integer.valueOf(strBuffer.toString());
				
				// Read smile name				
				readStringFromStream(strBuffer, dos);
				String smileName = strBuffer.toString();
				
				// Read smile strings
				for (int i = 0;; i++)
				{
					try
					{
						clrf = readStringFromStream(strBuffer, dos);
					}
					catch (EOFException eofExcept)
					{
						eof = true;
					}
					
					String word = new String(strBuffer).trim();
				
					// Add pair (word, integer) to textCorr
					if (word.length() != 0) insertTextCorr(word, currIndex);
					
					// Add triple (index, word, name) to selEmotions  
					if (i == 0) selEmotions.addElement(new Object[] {currIndex, word, smileName});
					
					if (clrf || eof) break;
				}
				if (eof) break;
			}
			
			// Read images
			images.load("/smiles.png", iconsSize, iconsSize, -1);
		}
		catch (Exception e)
		{
			//DebugLog.addText(e.toString());
			return;
		}
		
		used = true;
	}
	
	// Add smile text and index to textCorr in decreasing order of text length 
	void insertTextCorr(String word, Integer index)
	{
		Object[] data = new Object[] {word, index};
		int wordLen = word.length();
		int size = textCorr.size();
		int insIndex = 0;
		for (; insIndex < size; insIndex++)
		{
			Object[] cvtData = (Object[])textCorr.elementAt(insIndex);
			int cvlDataWordLen = ((String)cvtData[0]).length();
			if (cvlDataWordLen <= wordLen)
			{
				textCorr.insertElementAt(data, insIndex);
				return;
			}
		}
		textCorr.addElement(data);
	}

	// Reads simple word from stream. Used in Emotions(). 
	// Returns "true" if break was found after word
	static boolean readStringFromStream(StringBuffer buffer, DataInputStream stream) throws IOException, EOFException
	{
		byte chr;
		buffer.setLength(0);
		for (;;)
		{
			chr = stream.readByte();
			if ((chr == ' ') || (chr == '\n') || (chr == '\t')) break;
			if (chr == '_') chr = ' ';
			if (chr >= ' ') buffer.append((char)chr);
		}
		return (chr == '\n');
	}
	
	private void findEmotionInText(String text, String emotion, Integer index, int startIndex)
	{
		int findedIndex, len = emotion.length();
		
		findedIndex = text.indexOf(emotion, startIndex);
		if (findedIndex == -1) return;
		findedEmotions.addElement( new int[] {findedIndex, len, index.intValue()} );
	}
	
	public void addTextWithEmotions(TextList textList, String text, int fontStyle, int textColor, int bigTextIndex)
	{
		if (!used || !Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_USE_SMILES))
		{
			textList.addBigText(text, textColor, fontStyle, bigTextIndex);
			return;
		}
		
		int startIndex = 0;
		for (;;)
		{
			findedEmotions.removeAllElements();
			
			int size = textCorr.size();
			for (int i = 0; i < size; i++)
			{
				Object[] array = (Object[])textCorr.elementAt(i); 
				findEmotionInText
				(
					text,
					(String)array[0],
					(Integer)array[1], 
					startIndex
				);  
			}
			
			if (findedEmotions.isEmpty()) break;
			int count = findedEmotions.size();
			int minIndex = 100000, data[] = null, minArray[] = null;
			for (int i = 0; i < count; i++)
			{
				data = (int[])findedEmotions.elementAt(i);
				if (data[0] < minIndex)
				{
					minIndex = data[0];
					minArray = data;
				}
			}
			
			if (startIndex != minIndex)
				textList.addBigText(text.substring(startIndex, minIndex), textColor, fontStyle, bigTextIndex);
			
			textList.addImage(images.elementAt(minArray[2]), text.substring(minIndex, minIndex+minArray[1]));
			
			startIndex = minIndex+minArray[1];
		}
		
		int lastIndex = text.length();
		
		if (lastIndex != startIndex) 
			textList.addBigText(text.substring(startIndex, lastIndex), textColor, fontStyle, bigTextIndex);
	}
	
	
	///////////////////////////////////
	//                               // 
	//   UI for emotion selection    //
	//                               //
	///////////////////////////////////
	
	private Displayable lastDisplay;
	private CommandListener selectionListener;
	static private Command cmdOk = new Command(ResourceBundle.getString("select"), Command.OK, 1); 
	static private Command cmdCancel = new Command(ResourceBundle.getString("cancel"), Command.BACK, 2); 
	private String emotionText; 
	
	private Selector selector;

	public void selectEmotion(CommandListener selectionListener, Displayable lastDisplay)
	{
		this.selectionListener = selectionListener;
		this.lastDisplay       = lastDisplay;
		//selList = new TextList(null);
		selector = new Selector();
		JimmUI.setColorScheme(selector);
		
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		selector.setFullScreenMode(false);
		// #sijapp cond.end#
		
		selector.addCommand(cmdOk);
		selector.addCommand(cmdCancel);
		selector.setCommandListener(this);
		
		Jimm.display.setCurrent(selector);
	}
	
	public void commandAction(Command c, Displayable d)
	{
		if (c == cmdOk) select();
		else if (c == cmdCancel)
		{
			Jimm.display.setCurrent(lastDisplay);
			selector = null;
			System.gc();
		}
	}
	
	public void onKeyPress(VirtualList sender, int keyCode) {}
	public void onCursorMove(VirtualList sender) {}
	public void onItemSelected(VirtualList sender) 
	{
		select();
	}

	private void select()
	{
		Jimm.display.setCurrent(lastDisplay);
		System.gc();
		selectionListener.commandAction(cmdOk, selector);
	}
	
	public String getSelectedEmotion()
	{
		return emotionText;
	}
	
	public boolean isMyOkCommand(Command command)
	{
		return (command == cmdOk);
	}
	
	
	
	/////////////////////////
	//                     //
	//    class Selector   //
	//                     //
	/////////////////////////
		
	private class Selector extends VirtualList implements VirtualListCommands
	{
		private int cols, rows, itemHeight, curCol; 
		
		Selector()
		{
			super(null);
			setVLCommands(this);
			
			int drawWidth = getWidth()-scrollerWidth-2;
			int drawHeight = getDrawHeight();
			
			setCursorMode(SEL_NONE);
			
			int imgHeight = images.getHeight();
			int imgWidth = images.getWidth();
			
			itemHeight = imgHeight+5;
			
			cols = drawWidth/itemHeight;
			rows = (selEmotions.size()+cols-1)/cols;
			curCol = 0;
			
			showCurrSmileName();
		}
		
		protected void drawItemData
		(
			Graphics g, 
		    boolean isSelected, 
		    int index, 
		    int x1, int y1, int x2, int y2,
		    int fontHeight
		)
		{
			int xa, xb;
			int startIdx = cols*index;
			xa = x1;
			for (int i = 0; i < cols; i++, startIdx++)
			{
				if (startIdx >= selEmotions.size()) break;
				Object[] data = (Object[])selEmotions.elementAt(startIdx);
				int smileIdx = ((Integer)data[0]).intValue(); 
				
				xb = xa+itemHeight;
				
				g.drawImage(images.elementAt(smileIdx), xa+3, y1+3, Graphics.TOP|Graphics.LEFT);
				
				if (isSelected && (i == curCol))
				{
					g.setColor(this.getTextColor());
					g.setStrokeStyle(Graphics.DOTTED);
					g.drawRect(xa, y1, itemHeight-1, y2-y1-1);
				}
				xa = xb;
			}
		}
		
		private void showCurrSmileName()
		{
			int selIdx = getCurrIndex()*cols+curCol;
			if (selIdx >= selEmotions.size()) return;
			Object[] data = (Object[])selEmotions.elementAt(selIdx);
			emotionText = (String)data[1];			
			// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
			setTitle((String)data[2]);
			// #sijapp cond.else#
			setCaption((String)data[2]);
			// #sijapp cond.end#			
		}
		
		public int getItemHeight(int itemIndex)
		{
			return itemHeight;
		}
		
		protected int getSize()
		{
			return rows;
		}
		
		protected void get(int index, ListItem item)
		{
			
		}
		
		public void onKeyPress(VirtualList sender, int keyCode) 
		{
			int lastCol = curCol;
			switch (getGameAction(keyCode))
			{
			case LEFT:
				if (curCol != 0) curCol--;
				break;
			case RIGHT:	
				if (curCol < (cols-1)) curCol++;
				break;
			}
			if (lastCol != curCol)
			{
				repaint();
				showCurrSmileName();
			}
		}
		
		public void onCursorMove(VirtualList sender) 
		{
			showCurrSmileName();
		}
		
		public void onItemSelected(VirtualList sender) 
		{
			select();
		}
	}
}
