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
 File: src/jimm/Emoticons.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

package jimm;

//#sijapp cond.if modules_SMILES_STD="true" | modules_SMILES_ANI="true" #

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.microedition.lcdui.*;

import java.io.*;

import jimm.comm.Util;
import jimm.util.ResourceBundle;
import DrawControls.*;
import DrawControls.device.Device;

public class Emoticons implements VirtualListCommands, CommandListener
{
	private static Emoticons _this;
	private static Vector imagesData = new Vector();
	private static Vector timeData = new Vector();
	private static Vector findedEmotions = new Vector();
	private static boolean used;
	private static boolean animated;
	private static int[] selEmotionsIndexes, textCorrIndexes;
	private static String[] selEmotionsWord;
	private static String[] selEmotionsSmileNames;
	private static String[] textCorrWords;
	private static boolean[] emoFinded;
	private static String setName;

	public Emoticons()
	{
		used = false;
		_this = this;
		load();
	}
	
	static void load()
	{
		int iconsSize = -1;
		animated = false;
		String lang = Options.getString(Options.OPTION_UI_LANGUAGE).toLowerCase();
		if (lang == null || lang.length() == 0) lang = "en";
		ImageList imageList = new ImageList(); 
		Vector textCorr = new Vector();
		Vector selEmotions = new Vector();
		Vector timeingsValues = new Vector();
		InputStream stream;
		DataInputStream dos;
		Hashtable transl = new Hashtable();
		
//#sijapp cond.if target="MIDP2" | target="SIEMENS2"#
		imageList.setScale(Options.getInt(Options.OPTION_IMG_SCALE));
//#sijapp cond.end#
		
		//#sijapp cond.if modules_DEBUGLOG is "true"#
		System.gc();
		long mem = Runtime.getRuntime().freeMemory();
		//#sijapp cond.end#
		
		String fileLine = null;
		
		try
		{
			stream = _this.getClass().getResourceAsStream("/ani_emotions/animate.txt");
			if (stream != null)
			{
				dos = new DataInputStream(stream);
				String[] lineItems;
				String[] nameAndExt;
				String[] imgAndTime;
				String fileName;
				int imgWidth;
				int index;
				byte[] timeDataArray;
				for (;;)
				{
					fileLine = readLineFromStream(dos);
					if (fileLine == null) break;
					
					lineItems = Util.explode(fileLine, ' ');
					
					if (lineItems.length >= 3)
					{
						fileName = lineItems[0];
						imgWidth = Integer.parseInt(lineItems[1]);
						
						nameAndExt = Util.explode(fileName, '.');
						
						index = Integer.parseInt(nameAndExt[0]);
						
						imageList.load("/ani_emotions/"+fileName, imgWidth, -1, -1, Jimm.getPhoneVendor() == Device.PHONE_NOKIA);
						imagesData.setSize(index+1);
						imagesData.setElementAt(imageList.getImages(), index);
						
						timeingsValues.removeAllElements();
						for (int i = 2; i < lineItems.length; i++)
						{
							imgAndTime = Util.explode(lineItems[i], ',');
							timeingsValues.addElement(new Byte((byte)(0xFF&Integer.parseInt(imgAndTime[0]))));
							timeingsValues.addElement(new Byte((byte)(0xFF&Integer.parseInt(imgAndTime[1]))));
						}
						
						timeData.setSize(index+1);
						if (timeingsValues.size() > 2)
						{
							timeDataArray = new byte[timeingsValues.size()];
							for (int i = 0; i < timeDataArray.length; i++)
								timeDataArray[i] = ((Byte)timeingsValues.elementAt(i)).byteValue();
							timeData.setElementAt(timeDataArray, index);
							timeDataArray = null;
						}
					}
				}
				
				animated = true;
			}
			else animated = false;
			
			String translFile = "/smiles."+lang+".txt";
			loadTranslation(animated ? "/ani_emotions"+translFile : translFile, transl);
			
			// Load file "smiles.txt"
			stream = _this.getClass().getResourceAsStream(animated ? "/ani_emotions/smiles.txt" : "/smiles.txt");
			if (stream == null) return;

			dos = new DataInputStream(stream);
			
			fileLine = readLineFromStream(dos);
			
			// Read icon size
			if (!animated)
			{
				iconsSize = Integer.parseInt(fileLine);
				setName = null;
			}
			
			// Read emotions set name
			else
			{
				String[] emoData = Util.explode(fileLine, '|');
				String[] langAndName;
				if (emoData.length >= 1)
				{
					setName = emoData[0];
					for (int i = 1; i < emoData.length; i++)
					{
						langAndName = Util.explode(emoData[i], ',');
						if (langAndName.length == 2 && lang.equals(langAndName[0].toLowerCase()))
						{
							setName = langAndName[1];
							break;
						}
					}
				}
			}

			String[] lineItems;
			Integer currIndex;
			String smileName;
			String translation; 
			String word;
			for (;;)
			{
				fileLine = readLineFromStream(dos);
				if (fileLine == null) break;
				
				lineItems = Util.explode(fileLine, ' ');
				
				if (lineItems.length >= 3)
				{
					if (lineItems[0].charAt(0) == '#') continue;
					currIndex = Integer.valueOf(lineItems[0]);
					smileName = lineItems[1];
					translation = (String)transl.get(smileName.toLowerCase()); 
					smileName = (translation != null) ? translation : lineItems[1].replace('_', ' ');
					for (int i = 2; i < lineItems.length; i++)
					{
						word = lineItems[i];
						if (word.length() == 0) continue;
						insertTextCorr(textCorr, word.toLowerCase(), currIndex);
						if (word.indexOf('_') != -1 && word.length() > 3) 
							insertTextCorr(textCorr, word.replace('_', ' ').toLowerCase(), currIndex);
						if (i == 2) selEmotions.addElement(new Object[] { currIndex, word, smileName });
					}
				}
			}

			stream.close();

			// Read images
			if (!animated)
			{
				imageList.load("/smiles.png", iconsSize, iconsSize, -1, Jimm.getPhoneVendor() == Device.PHONE_NOKIA);
				for (int i = 0; i < imageList.size(); i++)
					imagesData.addElement(new Image[] { imageList.elementAt(i) });
				timeData.setSize(imagesData.size());
			}
			
			used = true;
		}
		catch (OutOfMemoryError e)
		{
			imageList.removeAllElements();
			imagesData.removeAllElements();
			timeData.removeAllElements();
			findedEmotions.removeAllElements();
			
			imageList = null;
			imagesData = null;
			timeData = null;
			findedEmotions = null;
			selEmotionsIndexes = null;
			textCorrIndexes = null;
			selEmotionsWord = null;
			selEmotionsSmileNames = null;
			textCorrWords = null;
			emoFinded = null;
			
			System.gc();
			
			Jimm.setLoadError("No memory to load emotions images!");
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Jimm.setLoadError("Error while loading emotions: "+e.toString());
			return;
		}
		
		// Write emotions data from vectors to arrays
		int size = selEmotions.size();
		selEmotionsIndexes = new int[size];
		selEmotionsWord = new String[size];
		selEmotionsSmileNames = new String[size];
		Object[] data;
		for (int i = 0; i < size; i++)
		{
			data = (Object[]) selEmotions.elementAt(i);
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
			data = (Object[]) textCorr.elementAt(i);
			textCorrWords[i] = (String) data[0];
			textCorrIndexes[i] = ((Integer) data[1]).intValue();
		}
		data = null;

		//#sijapp cond.if modules_DEBUGLOG is "true"#
		selEmotions.removeAllElements();
		selEmotions = null;
		textCorr.removeAllElements();
		textCorr = null;
		dos = null;
		stream = null;
		System.gc();
		DebugLog.addText("Emoticons used: "+(mem - Runtime.getRuntime().freeMemory()));
		//#sijapp cond.end#
	}

	static private void loadTranslation(String file, Hashtable transl) throws IOException
	{
		InputStream stream = _this.getClass().getResourceAsStream(file);
		if (stream == null) return;

		DataInputStream dos = new DataInputStream(stream);
		
		String line;
		String[] pair;
		for (;;)
		{
			line = readLineFromStream(dos);
			if (line == null) break;
			
			pair = Util.explode(line, '=');
			if (pair.length < 2) continue;
			if (pair[1].length() == 0) continue;
			transl.put(pair[0].toLowerCase(), pair[1]);
		}

		stream.close();
	}

	// Add smile text and index to textCorr in decreasing order of text length 
	static void insertTextCorr(Vector textCorr, String word, Integer index)
	{
		Object[] data = new Object[] { word, index };
		int wordLen = word.length();
		int size = textCorr.size();
		int insIndex = 0;
		Object[] cvtData;
		int cvlDataWordLen;
		for (; insIndex < size; insIndex++)
		{
			cvtData = (Object[]) textCorr.elementAt(insIndex);
			cvlDataWordLen = ((String) cvtData[0]).length();
			if (cvlDataWordLen <= wordLen)
			{
				textCorr.insertElementAt(data, insIndex);
				return;
			}
		}
		cvtData = null;
		textCorr.addElement(data);
	}

	// Reads simple word from stream. Used in Emoticons(). 
	// Returns "true" if break was found after word
	static String readLineFromStream(DataInputStream stream)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		boolean wasRead = false;
		try
		{
			for (;;)
			{
				int chr = stream.readByte();
				if (chr == -1) break;
				wasRead = true;
				if (chr == '\n') break;
				if (chr != '\r') bytes.write(chr);
			}
		}
		catch (Exception e) {}
		return wasRead ? Util.byteArrayToString(bytes.toByteArray(), true) : null;
	}

	static private void findEmotionInText(String text, String emotion,
			int index, int startIndex, int recIndex)
	{
		if (!emoFinded[recIndex]) return;
		int findedIndex, len = emotion.length();
		findedIndex = text.indexOf(emotion, startIndex);
		if (findedIndex == -1)
		{
			emoFinded[recIndex] = false;
			return;
		}
		findedEmotions.addElement(new int[] { findedIndex, len, index });
	}

	static public void addTextWithEmotions(TextList textList, String text,
			int fontStyle, int textColor, int bigTextIndex)
	{
		if (!used || !Options.getBoolean(Options.OPTION_USE_SMILES))
		{
			textList.addBigText(text, textColor, fontStyle, bigTextIndex);
			return;
		}
		
		String loweredText = text.toLowerCase();

		for (int i = emoFinded.length - 1; i >= 0; i--)
			emoFinded[i] = true;

		int startIndex = 0;
		int size;
		int count;
		int imgIndex;
		Image[] imgSeq;
		byte[] timeSeq;
		for (;;)
		{
			findedEmotions.removeAllElements();

			size = textCorrWords.length;
			for (int i = 0; i < size; i++)
				findEmotionInText(loweredText, textCorrWords[i], textCorrIndexes[i], startIndex, i);
			
			if (findedEmotions.isEmpty()) break;
			
			count = findedEmotions.size();
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
				textList.addBigText(text.substring(startIndex, minIndex), textColor, fontStyle, bigTextIndex);

			imgIndex = minArray[2];
			imgSeq = (Image[])imagesData.elementAt(imgIndex);
			timeSeq = (byte[])timeData.elementAt(imgIndex);
			
			textList.addAniImage(imgSeq, timeSeq, text.substring(minIndex, minIndex + minArray[1]), bigTextIndex);

			startIndex = minIndex + minArray[1];
		}
		imgSeq = null;
		timeSeq = null;

		int lastIndex = text.length();

		if (lastIndex != startIndex)
			textList.addBigText(text.substring(startIndex, lastIndex), textColor, fontStyle, bigTextIndex);
	}

	///////////////////////////////////
	//                               // 
	//   UI for emotion selection    //
	//                               //
	///////////////////////////////////

	private static Command cmdOk = new Command(ResourceBundle.getString("select"), Command.OK, 1);
	private static Command cmdCancel = new Command(ResourceBundle.getString("cancel"), Jimm.cmdBack, 2);

	private static String emotionText;
	private static Selector selector;
	private static int caretPos;
	private static TextBox textBox;
	private static TimerTask aniTask;
	
	static public void aniEmoTimer()
	{
		if (selector == null) return;
		Selector.counter += 2;
		Selector._this.repaint();
	}

	static public void selectEmotion(TextBox textBox)
	{
		Emoticons.caretPos = textBox.getCaretPosition();
		Emoticons.textBox = textBox;

		selector = new Selector();
		JimmUI.setColorScheme(selector, false, -1, true);

		selector.addCommandEx(cmdOk, VirtualList.MENU_TYPE_RIGHT_BAR);
		selector.addCommandEx(cmdCancel, VirtualList.MENU_TYPE_LEFT_BAR);
		selector.setCommandListener(_this);

		selector.activate(Jimm.display);
		if (animated)
		{
			aniTask = new TimerTasks(TimerTasks.TYPE_SMILES_SEL_ANI);
			new Timer().schedule(aniTask, 200, 200);
		}
	}

	public void commandAction(Command c, Displayable d)
	{
		Jimm.aaUserActivity();
		if (c == cmdOk) select();
		else if (c == cmdCancel)
		{
			Jimm.display.setCurrent(textBox);
			Jimm.setBkltOn(true);
		}
	}

	public void vlKeyPress(VirtualList sender, int keyCode, int type)
	{
	}

	public void vlCursorMoved(VirtualList sender)
	{
	}

	public void vlItemClicked(VirtualList sender)
	{
		select();
	}

	static private void select()
	{
		textBox.insert(" " + Emoticons.getSelectedEmotion() + " ", caretPos);
		Jimm.display.setCurrent(textBox);
		Jimm.setBkltOn(true);
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
		static private int cols, rows, itemHeight, itemWidth, curCol, lastRow;
		static int counter;

		static Selector _this;

		static
		{
			curCol = 0;
			lastRow = 0;
		}

		Selector()
		{
			super(null);
			_this = this;
			setVLCommands(this);
			
			int drawWidth = getWidth()-2*borderWidth-scrollerWidth;

			setMode(CURSOR_MODE_ENABLED);
			
			itemWidth = itemHeight = 0;
			Image[] images;
			int w;
			int h;
			for (int i = 0; i < imagesData.size(); i++)
			{
				images = (Image[])imagesData.elementAt(i);
				if (images == null) continue;
				w = images[0].getWidth();
				h = images[0].getHeight();
				if (w > itemWidth) itemWidth = w;
				if (h > itemHeight) itemHeight = h;
			}
			images = null;
			
			int layer = (itemWidth < 30) ? 5 : 0;
			itemWidth += layer;
			itemHeight += layer;

			cols = drawWidth / itemWidth;
			rows = (selEmotionsIndexes.length + cols - 1) / cols;

			setCurrentItem(lastRow);

			showCurrSmileName();
		}
		
		protected void getCurXVals(int[] values)
		{
			values[0] = borderWidth+curCol*itemWidth;
			values[1] = borderWidth+(curCol+1)*itemWidth;
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

		protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int paintMode)
		{
			int xa, xb;
			int startIdx = cols * index;
			int imagesCount = imagesData.size();
			int smileIdx;
			int imgIndex;
			byte[] timeSeq;
			Image img;

			xa = x1;
			for (int i = 0; i < cols; i++, startIdx++)
			{
				if (startIdx >= selEmotionsIndexes.length)
					break;
				smileIdx = selEmotionsIndexes[startIdx];

				xb = xa + itemWidth;

				if (smileIdx < imagesCount)
				{
					if (animated)
					{
						timeSeq = (byte[])timeData.elementAt(smileIdx);
						imgIndex = (timeSeq == null) ? 0 : 0xFF&(int)timeSeq[counter%timeSeq.length];
						
					}
					else imgIndex = 0;
					
					img = ((Image[])imagesData.elementAt(smileIdx))[imgIndex];
					g.drawImage(img, (xa+xb-img.getWidth()+1)/2, (y1+y2-img.getHeight()+1)/2,
							Graphics.TOP | Graphics.LEFT);
				}

				xa = xb;
			}
			img = null;
		}
		
		protected void onHide() 
		{
			System.out.println("Emoticons.onHide()");
			if (aniTask != null)
			{
				aniTask.cancel();
				aniTask = null;
			}
			selector = null;
		}

		static private void showCurrSmileName()
		{
			lastRow = _this.getCurrIndex();
			int selIdx = lastRow * cols + curCol;
			if (selIdx >= selEmotionsSmileNames.length) return;
			emotionText = selEmotionsWord[selIdx];
			String emoName = selEmotionsSmileNames[selIdx];
			_this.setCaption(setName != null ? emoName+" ["+setName+"]" : emoName);
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

		public void vlKeyPress(VirtualList sender, int keyCode, int type)
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
					} else
					{
					    curCol = (selEmotionsIndexes.length - 1) % cols;
					    curRow = rowCount - 1;
					}
					break;

				case Canvas.RIGHT:
					if (curCol < (cols - 1))
						curCol++;
					else if (curRow < rowCount-1)
					{
						curCol = 0;
						curRow++;
					} else
					{
					    curCol = 0;
					    curRow = 0;
					}
					break;
				}

				setCurrentItem(curRow);

				int index = curCol + getCurrIndex() * cols;
				if (index >= selEmotionsIndexes.length-1)
					curCol = (selEmotionsIndexes.length - 1) % cols;

				if (lastCol != curCol)
				{
					invalidate();
					showCurrSmileName();
				}
			}
		}

		public void vlCursorMoved(VirtualList sender)
		{
			showCurrSmileName();
		}

		public void vlItemClicked(VirtualList sender)
		{
			select();
		}
	}
}

//#sijapp cond.end#
