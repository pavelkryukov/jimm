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

//#sijapp cond.if modules_SMILES is "true" #

import java.util.Vector;
import javax.microedition.lcdui.*;

import java.io.*;

import jimm.util.ResourceBundle;
import DrawControls.*;

public class Emotions implements VirtualListCommands, CommandListener
{
	private static Emotions _this;
	
	final private static ImageList images = new ImageList();

	final private static Vector findedEmotions = new Vector();

	private static boolean used;

	private static int[] selEmotionsIndexes, textCorrIndexes;

	private static String[] selEmotionsWord, selEmotionsSmileNames,
			textCorrWords;

	private static boolean[] emoFinded;

	public Emotions()
	{
		int iconsSize;
		used = false;
		_this = this;

		Vector textCorr = new Vector();
		Vector selEmotions = new Vector();

		//#sijapp cond.if modules_DEBUGLOG is "true"#
		System.gc();
		long mem = Runtime.getRuntime().freeMemory();
		//#sijapp cond.end#

		// Load file "smiles.txt"
		InputStream stream = this.getClass().getResourceAsStream("/smiles.txt");
		if (stream == null)
			return;

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
					} catch (EOFException eofExcept)
					{
						eof = true;
					}

					String word = new String(strBuffer).trim();

					// Add pair (word, integer) to textCorr
					if (word.length() != 0)
						insertTextCorr(textCorr, word, currIndex);

					// Add triple (index, word, name) to selEmotions  
					if (i == 0)
						selEmotions.addElement(new Object[]
						{ currIndex, word, smileName });

					if (clrf || eof)
						break;
				}
				if (eof)
					break;
			}

			stream.close();

			// Read images
			images.load("/smiles.png", iconsSize, iconsSize, -1);
		} catch (Exception e)
		{
			return;
		}

		// Write emotions data from vectors to arrays
		int size = selEmotions.size();
		selEmotionsIndexes = new int[size];
		selEmotionsWord = new String[size];
		selEmotionsSmileNames = new String[size];
		for (int i = 0; i < size; i++)
		{
			Object[] data = (Object[]) selEmotions.elementAt(i);
			selEmotionsIndexes[i] = ((Integer) data[0]).intValue();
			selEmotionsWord[i] = (String) data[1];
			selEmotionsSmileNames[i] = (String) data[2];
		}

		size = textCorr.size();
		textCorrWords = new String[size];
		textCorrIndexes = new int[size];
		emoFinded = new boolean[size];
		for (int i = 0; i < size; i++)
		{
			Object[] data = (Object[]) textCorr.elementAt(i);
			textCorrWords[i] = (String) data[0];
			textCorrIndexes[i] = ((Integer) data[1]).intValue();
		}

		//#sijapp cond.if modules_DEBUGLOG is "true"#
		selEmotions.removeAllElements();
		selEmotions = null;
		textCorr.removeAllElements();
		textCorr = null;
		dos = null;
		stream = null;
		System.gc();
		System.out.println("Emotions used: "
				+ (mem - Runtime.getRuntime().freeMemory()));
		//#sijapp cond.end#

		used = true;
	}

	// Add smile text and index to textCorr in decreasing order of text length 
	static void insertTextCorr(Vector textCorr, String word, Integer index)
	{
		Object[] data = new Object[]
		{ word, index };
		int wordLen = word.length();
		int size = textCorr.size();
		int insIndex = 0;
		for (; insIndex < size; insIndex++)
		{
			Object[] cvtData = (Object[]) textCorr.elementAt(insIndex);
			int cvlDataWordLen = ((String) cvtData[0]).length();
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
	static boolean readStringFromStream(StringBuffer buffer,
			DataInputStream stream) throws IOException, EOFException
	{
		byte chr;
		buffer.setLength(0);
		for (;;)
		{
			chr = stream.readByte();
			if ((chr == ' ') || (chr == '\n') || (chr == '\t'))
				break;
			if (chr == '_')
				chr = ' ';
			if (chr >= ' ')
				buffer.append((char) chr);
		}
		return (chr == '\n');
	}

	static private void findEmotionInText(String text, String emotion,
			int index, int startIndex, int recIndex)
	{
		if (!emoFinded[recIndex])
			return;
		int findedIndex, len = emotion.length();
		findedIndex = text.indexOf(emotion, startIndex);
		if (findedIndex == -1)
		{
			emoFinded[recIndex] = false;
			return;
		}
		findedEmotions.addElement(new int[]
		{ findedIndex, len, index });
	}

	static public void addTextWithEmotions(TextList textList, String text,
			int fontStyle, int textColor, int bigTextIndex)
	{
		if (!used || !Options.getBoolean(Options.OPTION_USE_SMILES))
		{
			textList.addBigText(text, textColor, fontStyle, bigTextIndex);
			return;
		}

		for (int i = emoFinded.length - 1; i >= 0; i--)
			emoFinded[i] = true;

		int startIndex = 0;
		for (;;)
		{
			findedEmotions.removeAllElements();

			int size = textCorrWords.length;
			for (int i = 0; i < size; i++)
			{
				findEmotionInText(text, textCorrWords[i], textCorrIndexes[i],
						startIndex, i);
			}

			if (findedEmotions.isEmpty())
				break;
			int count = findedEmotions.size();
			int minIndex = 100000, data[] = null, minArray[] = null;
			for (int i = 0; i < count; i++)
			{
				data = (int[]) findedEmotions.elementAt(i);
				if (data[0] < minIndex)
				{
					minIndex = data[0];
					minArray = data;
				}
			}

			if (startIndex != minIndex)
				textList.addBigText(text.substring(startIndex, minIndex),
						textColor, fontStyle, bigTextIndex);

			textList.addImage(images.elementAt(minArray[2]), text.substring(
					minIndex, minIndex + minArray[1]), bigTextIndex);

			startIndex = minIndex + minArray[1];
		}

		int lastIndex = text.length();

		if (lastIndex != startIndex)
			textList.addBigText(text.substring(startIndex, lastIndex),
					textColor, fontStyle, bigTextIndex);
	}

	///////////////////////////////////
	//                               // 
	//   UI for emotion selection    //
	//                               //
	///////////////////////////////////

	private static Command cmdOk = new Command(ResourceBundle.getString("select"), Command.OK, 1);
	private static Command cmdCancel = new Command(ResourceBundle.getString("cancel"), Command.BACK, 2);

	private static String emotionText;
	private static Selector selector;
	private static int caretPos;
	private static Object lastScreen;
	private static TextBox textBox;

	static public void selectEmotion(TextBox textBox, Object screen)
	{
		lastScreen = screen;
		Emotions.caretPos = textBox.getCaretPosition();
		Emotions.textBox = textBox;
		//selList = new TextList(null);
		selector = new Selector();
		JimmUI.setColorScheme(selector, false);

		selector.addCommandEx(cmdOk, VirtualList.MENU_TYPE_LEFT_BAR);
		selector.addCommandEx(cmdCancel, VirtualList.MENU_TYPE_RIGHT_BAR);
		selector.setCommandListener(_this);

		selector.activate(Jimm.display);
	}

	public void commandAction(Command c, Displayable d)
	{
		if (c == cmdOk) select();
		else if (c == cmdCancel) JimmUI.selectScreen(lastScreen);
	}

	public void onKeyPress(VirtualList sender, int keyCode, int type)
	{
	}

	public void onCursorMove(VirtualList sender)
	{
	}

	public void onItemSelected(VirtualList sender)
	{
		select();
	}

	static private void select()
	{
		textBox.insert(" " + Emotions.getSelectedEmotion() + " ", caretPos);
		JimmUI.selectScreen(lastScreen);
	}

	static public String getSelectedEmotion()
	{
		return emotionText;
	}

	static public boolean isMyOkCommand(Command command)
	{
		return (command == cmdOk);
	}

	/////////////////////////
	//                     //
	//    class Selector   //
	//                     //
	/////////////////////////

	static class Selector extends VirtualList implements VirtualListCommands
	{
		static private int cols, rows, itemHeight, curCol;

		static private Selector _this;

		Selector()
		{
			super(null);
			_this = this;
			setVLCommands(this);

			int drawWidth = getWidth() - scrollerWidth - 2;

			setCursorMode(SEL_NONE);

			int imgHeight = images.getHeight();

			itemHeight = imgHeight + 5;

			cols = drawWidth / itemHeight;
			rows = (selEmotionsIndexes.length + cols - 1) / cols;
			curCol = 0;

			showCurrSmileName();
		}

		//#sijapp cond.if target is "MIDP2"#
		protected boolean pointerPressedOnUtem(int index, int x, int y, int mode)
		{
			int lastCol = curCol;
			curCol = x / itemHeight;
			if (curCol < 0)
				curCol = 0;
			if (curCol >= cols)
				curCol = cols - 1;
			if (lastCol != curCol)
			{
				showCurrSmileName();
				invalidate();
			}

			return false;
		}

		//#sijapp cond.end#

		protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int fontHeight)
		{
			int xa, xb;
			int startIdx = cols * index;
			int imagesCount = images.size();
			boolean isSelected = (index == getCurrIndex());
			xa = x1;
			for (int i = 0; i < cols; i++, startIdx++)
			{
				if (startIdx >= selEmotionsIndexes.length)
					break;
				int smileIdx = selEmotionsIndexes[startIdx];

				xb = xa + itemHeight;

				if (smileIdx < imagesCount)
					g.drawImage(images.elementAt(smileIdx), xa + 3, y1 + 3,
							Graphics.TOP | Graphics.LEFT);

				if (isSelected && (i == curCol))
				{
					g.setColor(this.getTextColor());
					g.setStrokeStyle(Graphics.DOTTED);
					g.drawRect(xa, y1, itemHeight - 1, y2 - y1 - 1);
				}
				xa = xb;
			}
		}

		static private void showCurrSmileName()
		{
			int selIdx = _this.getCurrIndex() * cols + curCol;
			if (selIdx >= selEmotionsSmileNames.length)
				return;
			emotionText = selEmotionsWord[selIdx];
			_this.setCaption(selEmotionsSmileNames[selIdx]);
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

		public void onKeyPress(VirtualList sender, int keyCode, int type)
		{
			if (type == VirtualList.KEY_PRESSED)
			{
				int lastCol = curCol;
				int curRow = getCurrIndex();
				int rowCount = getSize();
				switch (getGameAction(keyCode))
				{
				case Canvas.LEFT:
					if (curCol != 0)
						curCol--;
					else if (curRow != 0)
					{
						curCol = cols - 1;
						curRow--;
					}
					break;

				case Canvas.RIGHT:
					if (curCol < (cols - 1))
						curCol++;
					else if (curRow <= rowCount)
					{
						curCol = 0;
						curRow++;
					}
					break;
				}

				setCurrentItem(curRow);

				int index = curCol + getCurrIndex() * cols;
				if (index >= selEmotionsIndexes.length)
					curCol = (selEmotionsIndexes.length - 1) % cols;

				if (lastCol != curCol)
				{
					invalidate();
					showCurrSmileName();
				}
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

//#sijapp cond.end#
