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

import jimm.util.ResourceBundle;
import DrawControls.*;

public class Emotions implements VirtualListCommands, CommandListener
{
	final private ImageList images = new ImageList();
	final private Vector findedEmotions = new Vector();
	
	private void findEmotionInText(String text, String emotion, Integer index, int startIndex)
	{
		int findedIndex, len = emotion.length();
		
		findedIndex = text.indexOf(emotion, startIndex);
		if (findedIndex == -1) return;
		findedEmotions.addElement( new int[] {findedIndex, len, index.intValue()} );
	}
	
	static final private Object[] knownEmotions = 
	{
			":-))))", new Integer(3),
			
			":-)))",  new Integer(2),
			":))))",  new Integer(3),
			
			"]:-<",   new Integer(14),
			"]:->",   new Integer(15),
			":-))",   new Integer(1),
			":)))",   new Integer(2),
			":-[[",   new Integer(18),
			"OOPS",   new Integer(22),
			":-PP",   new Integer(30),
			";-PP",   new Integer(30),
			"LOVE",   new Integer(21),
			
			":-\\",   new Integer(13),
			":-)",    new Integer(0),
			":))",    new Integer(1),
			"LOL",    new Integer(3),
			":-D",    new Integer(3),
			";-)",    new Integer(4),
			":-]",    new Integer(6),
			";-]",    new Integer(7),
			"8-)",    new Integer(9),
			"8-P",    new Integer(10),
			":-o",    new Integer(11),
			":-O",    new Integer(11),
			"WOW",    new Integer(10),
			":-/",    new Integer(12),
			":-@",    new Integer(16),
			":-[",    new Integer(17),
			";-[",    new Integer(19),
			":\")",   new Integer(20),
			":-L",    new Integer(21),
			";-L",    new Integer(21),
			":-.",    new Integer(22),
			":-(",    new Integer(23),
			";-<",    new Integer(23),
			":-<",    new Integer(24),
			";-(",    new Integer(25),
			":-|",    new Integer(26),
			";-|",    new Integer(27),
			":-S",    new Integer(28),
			":->",    new Integer(29),
			";->",    new Integer(29),
			":-P",    new Integer(30),
			";-P",    new Integer(30),
			";-D",    new Integer(31),
			"*G*",    new Integer(31),
			
			":)",     new Integer(0),
			";)",     new Integer(4),
			"^^",     new Integer(5),
			":]",     new Integer(6),
			";]",     new Integer(7),
			"(:",     new Integer(8),
			":D",     new Integer(3)
	};
	
	public void addTextWithEmotions(TextList textList, String text, int fontStyle, int textColor, int bigTextIndex)
	{
		int startIndex = 0;
		for (;;)
		{
			findedEmotions.removeAllElements();
			
			for (int i = 0; i < knownEmotions.length; i += 2)
			{
				findEmotionInText
				(
					text,
					(String)knownEmotions[i],
					(Integer)knownEmotions[i+1], startIndex
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
	
	public void load(String resName, int height)
	{
		try
		{
			images.load(resName, height);
		}
		catch (Exception e) {}
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
	private TextList selList;
	private String emotionText; 
	
	private static final Object[] selEmotionsStrings = 
	{
		new Integer(0),  ":-)",   "Smile",
		new Integer(1),  ":-))",  "More smile",
		new Integer(2),  ":-))",  "Most smile",
		new Integer(3),  "LOL",   "LOL",
		new Integer(4),  ";-)",   "Ironic",
		new Integer(5),  "^^",    "Sly",
		new Integer(6),  ":-]",   "Content",
		new Integer(7),  ";-]",   "Ironic content",
		new Integer(8),  "(:",    "Embarrassed",
		new Integer(9),  "8-)",   "Boss",
		new Integer(10), "8-P",   "Super",
		new Integer(11), ":-O",   "Scared",
		new Integer(12), ":-/",   "Discontent",
		new Integer(13), ":-\\",  "Suspirious",
		new Integer(14), "]:-<",  "Devil",
		new Integer(15), "]:->",  "Malicious devil",
		new Integer(16), ":-@",   "Shouter",
		new Integer(17), ":-[",   "Disappointed",
		new Integer(18), ":-[[",  "Angry",
		new Integer(19), ";-[",   "Furious",
		new Integer(20), ":\")",  "Shy",
		new Integer(21), ":-L",   "Love",
		new Integer(22), ":-.",   "Ooops",
		new Integer(23), ":-(",   "Sad",
		new Integer(24), ":-<",   "Very sad",
		new Integer(25), ";-(",   "Crying",
		new Integer(26), ":-|",   "Worried",
		new Integer(27), ";-|",   "Upset",
		new Integer(28), ":-S",   "Spooked",
		new Integer(29), ":->",   "Miser",
		new Integer(30), ":-P",   "Amused",
		new Integer(31), ";-D",   "Big green"
	};
	
	public void selectEmotion(CommandListener selectionListener, Displayable lastDisplay)
	{
		this.selectionListener = selectionListener;
		this.lastDisplay       = lastDisplay;
		selList = new TextList(null);
		
		// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		selList.setTitle(ResourceBundle.getString("emotions"));
		selList.setFullScreenMode(false);
		// #sijapp cond.else#
		selList.setCaption(ResourceBundle.getString("emotions"));
		// #sijapp cond.end#
		
		selList.addCommand(cmdOk);
		selList.addCommand(cmdCancel);
		selList.setImageList(images);
		selList.setVLCommands(this);
		selList.setCommandListener(this);
		
		for (int i = 0; i < selEmotionsStrings.length; i += 3)
		{
			selList.add
			(
				(String)selEmotionsStrings[i+2],
				0,
				((Integer)selEmotionsStrings[i]).intValue()
			);
		}
		
		Jimm.display.setCurrent(selList);
	}
	
	public void commandAction(Command c, Displayable d)
	{
		if (c == cmdOk) select();
		else if (c == cmdCancel)
		{
			Jimm.display.setCurrent(lastDisplay);
			selList = null;
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
		int index = selList.getCurrIndex();
		emotionText = (String)selEmotionsStrings[index*3+1];
		Jimm.display.setCurrent(lastDisplay);
		selList = null;
		System.gc();
		selectionListener.commandAction(cmdOk, selList);
	}
	
	public String getSelectedEmotion()
	{
		return emotionText;
	}
	
	public boolean isMyOkCommand(Command command)
	{
		return (command == cmdOk);
	}
}
