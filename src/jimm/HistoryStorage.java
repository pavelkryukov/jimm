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
 File: src/jimm/HistoryStorage.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

//#sijapp cond.if modules_HISTORY is "true" #

package jimm;

import java.util.Hashtable;
import java.lang.StringBuffer;
import java.lang.System;
import java.lang.Exception;
import javax.microedition.lcdui.Alert;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordFilter;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordComparator;
import jimm.util.ResourceBundle;
import java.io.*;
import javax.microedition.lcdui.*;

import DrawControls.ListItem;
import DrawControls.VirtualList;
import DrawControls.TextList;
import DrawControls.VirtualListCommands;

import jimm.comm.Util;
import jimm.DebugLog;

// Class to cache one line in messages list
// All fields are public to easy and fast access
final class CachedRecord
{
	public String shortText, text, date, from;
	byte type; // 1 - incoming message, 0 - outgoing message
}

// Visual messages history list
final class HistoryStorageList extends VirtualList
                         implements CommandListener, VirtualListCommands
{
	// commands for message text
	private static Command cmdMsgBack = new Command(ResourceBundle.getString("back"),   Command.BACK,   1);
	private static Command cmdMsgNext = new Command(ResourceBundle.getString("next"),   Command.ITEM,   2);
	private static Command cmdMsgPrev = new Command(ResourceBundle.getString("prev"),   Command.ITEM,   3);
	
	// commands for messages list
	private static Command cmdSelect  = new Command(ResourceBundle.getString("select"), Command.SCREEN, 1);
	private static Command cmdBack    = new Command(ResourceBundle.getString("back"),   Command.BACK,   2);
	private static Command cmdClear   = new Command(ResourceBundle.getString("clear"),  Command.ITEM,   4); 
	private static Command cmdFind    = new Command(ResourceBundle.getString("find"),   Command.ITEM,   3);
	private static Command cmdInfo    = new Command(ResourceBundle.getString("history_info"), Command.ITEM,   5);
	
	private static TextList messText;
	
	// list UIN
	private static String currUin  = new String(), 
	                      currName = new String();
	
	// Constructor
	public HistoryStorageList()
	{
		super(null);
		addCommand(cmdSelect);
		addCommand(cmdBack);
		addCommand(cmdClear);
		addCommand(cmdFind);
		addCommand(cmdInfo);
		setCommandListener(this);
		setVLCommands(this);
	}
	
	// VirtualList command impl.
	public void onCursorMove(VirtualList sender)
	{
		// user select some history storage line
		if (sender == this)
		{
			CachedRecord record = Jimm.jimm.getHistory().getCachedRecord(currUin, getCurrIndex());
			
			if (record == null) return;
			
			//#sijapp cond.if target is "MIDP2"#
			setTitle(record.from+" "+record.date);
			//#sijapp cond.else#
			setCaption(record.from+" "+record.date);
			//#sijapp cond.end#
		}
	}
	
	// VirtualList command impl.
	public void onKeyPress(VirtualList sender, int keyCode)
	{
		if (sender == messText)
		{
			switch ( getGameAction(keyCode) )
			{
			case Canvas.LEFT: moveInList(-1);  break;
			case Canvas.RIGHT: moveInList(1); break;
			}
			
		}
	}
	
	// Moves on next/previous message in list and shows message text
	private void moveInList(int offset)
	{
		moveCursor(offset);
		showMessText();
	}
	
	// VirtualList command impl.
	public void onItemSelected(VirtualList sender)
	{
		if (sender == this)
		{
			showMessText();
		}
	}
	
	public void commandAction(Command c, Displayable d)
	{
		// back to contact list
		if (c == cmdBack)
		{
			Jimm.jimm.getHistory().clearCache();
			messText = null;
			System.gc();
			Jimm.jimm.getContactListRef().activate();
		}
		
		// select message
		else if (c == cmdSelect)
		{
			showMessText();
		}
		
		// Clear messages
		else if (c == cmdClear)
		{
			Jimm.jimm.getHistory().clearHistory(currUin);
			repaint();
		}
		
		// back to messages list
		else if (c == cmdMsgBack)
		{
			Jimm.display.setCurrent(this);
		}
		
		// next message command
		else if (c == cmdMsgNext)
		{
			moveInList(1);
		}
		
		// previous message command 
		else if (c == cmdMsgPrev)
		{
			moveInList(-1);
		}
		
		// commands info
		else if (c == cmdInfo)
		{
			StringBuffer str = new StringBuffer(); 
			RecordStore rs = Jimm.jimm.getHistory().getRS();
			
			try
			{
				str.append(ResourceBundle.getString("hist_cur")).append(": ").append(getSize()).append("\n");
				str.append(ResourceBundle.getString("hist_rc")).append(": ").append(rs.getNumRecords()).append("\n");
				str.append(ResourceBundle.getString("hist_size")).append(": ").append(rs.getSize()/1024).append("\n");
				str.append(ResourceBundle.getString("hist_avail")).append(": ").append(rs.getSizeAvailable()/1024).append("\n");
			}
			catch (Exception e)
			{
				
			}
			
			Alert alert = new Alert
			(
				ResourceBundle.getString("history_info"),
				str.toString(),
				null,
				AlertType.INFO 
			);
			alert.setTimeout(Alert.FOREVER);
			Jimm.display.setCurrent(alert);
		}
	}
	
	// Show text message of current message of messages list
	void showMessText()
	{
		if (this.getCurrIndex() >= this.getSize()) return;
		if (messText == null)
		{
			messText = new TextList(null);
			//#sijapp cond.if target is "MIDP2"#
			messText.setFullScreenMode(false);
			//#sijapp cond.end#
			messText.setCursorMode(TextList.SEL_NONE);
			messText.setCommandListener(this);
			messText.addCommand(cmdMsgBack);
			messText.addCommand(cmdMsgNext);
			messText.addCommand(cmdMsgPrev);
			messText.setVLCommands(this);
		}
		
		CachedRecord record = Jimm.jimm.getHistory().getRecord(currUin, this.getCurrIndex()); 
		
		messText.clear();
		messText.addBigText(record.date+":", 0, Font.STYLE_BOLD);
		messText.addBigText(record.text, 0, Font.STYLE_PLAIN);
		//#sijapp cond.if target is "MIDP2"#
		messText.setTitle(record.from);
		//#sijapp cond.else#
		messText.setCaption(record.from);
		//#sijapp cond.end#
	
		Jimm.display.setCurrent(messText);
		messText.repaint();
	}
	
	// returns UIN of list
	static String getCurrUin()
	{
		return currUin;
	}
	
	// sets UIN for list
	static void setCurrUin(String currUin_, String currName_)
	{
		currUin  = currUin_;
		currName = currName_;
	}
	
	// returns size of messages history list
	protected int getSize()
	{
		return Jimm.jimm.getHistory().getRecordCount(currUin);
	}
	  
	// returns messages history list item data
	protected void get(int index, ListItem item)
	{
		CachedRecord record = Jimm.jimm.getHistory().getCachedRecord(currUin, index);
		if (record == null) return;
		item.text = record.shortText;
		item.color = (record.type == 0) ? 0x000000 : 0x0000FF;
	}
}

// History storage implementation
final public class HistoryStorage implements RecordFilter, 
                                             RecordComparator
{
	//=============================//
	//                             //
	//   impl. for RecordFilter    //
	//                             //
	//=============================//
	
	private long cmpUin;
	public boolean matches(byte[] candidate)
	{
		return (cmpUin == Util.getDWord(candidate, 0));
	}

	//=============================//
	//                             //
	//   impl. for RecordFilter    //
	//                             //
	//=============================//
	
	public int compare(byte[] rec1, byte[] rec2)
	{
		long value1 = Util.getDWord(rec1, 4);
		long value2 = Util.getDWord(rec2, 4);
		if (value1 < value2) return PRECEDES;
		if (value1 > value2) return FOLLOWS;
		return EQUIVALENT;
	}
	
	//===================================//
	//                                   //
	//    Data storage implementation    //
	//                                   //
	//===================================//
	
	private static RecordStore recordStore;
	private static HistoryStorageList list;
	private String currCacheUin = new String();
	private RecordEnumeration recordsEnum;
	private int currRecIndex = -1, currRecId;
	private Hashtable cachedRecords;
	
	final static private int TEXT_START_INDEX = 9; 
	
	static
	{
		try
		{
			// opens record store
			recordStore = RecordStore.openRecordStore("history", true);
			
			// TODO: Check version
		}
		catch (Exception e)
		{
		
		}
	}
	
	// Convert String UIN to long value
	private long uinToLong(String uin)
	{
		long result;
		try
		{
			result = Long.parseLong(uin);
		}
		catch (Exception e)
		{
			result = -1;
		}
		return result;
	}
	
	// Add message text to contact history
	public void addText(String uin, String text, byte type, String from)
	{
		try
		{
		byte[] buffer, textData;
		int textLen;
		boolean lastLine = false;
		
		if (list != null)
		{
			if (list.getSize() == 0) lastLine = true;
			else if (list.getCurrIndex() == (list.getSize()-1)) lastLine = true;
		}
		
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			DataOutputStream das = new DataOutputStream(baos);
			das.writeUTF(from);
			das.writeUTF(text);
			das.writeUTF(Util.getDateString(false));
			textData = baos.toByteArray();
			textLen = textData.length;
			
			buffer = new byte[textLen+TEXT_START_INDEX];
			Util.putDWord(buffer, 0, uinToLong(uin));
			Util.putDWord(buffer, 4, recordStore.getNextRecordID());
			buffer[8] = type;
			System.arraycopy(textData, 0, buffer, TEXT_START_INDEX, textLen);
			
			recordStore.addRecord(buffer, 0, buffer.length);
		}
		catch (Exception e) 
		{
			DebugLog.addText( "Add text-1: "+e.toString() );
		}
		
		if ( (list != null) && HistoryStorageList.getCurrUin().equals(uin) )
		{
			list.repaint();
			if (lastLine) list.setCurrentItem(list.getSize()-1);
		}
		
		} catch (Exception e) 
		{
			DebugLog.addText( "Add text-2: "+e.toString() );
		}
		
	}
	
	RecordStore getRS()
	{
		return recordStore;
	}
	
	// enumerates all history records for UIN
	private void enumerateRecords(String uin)
	{
		if (cachedRecords == null) cachedRecords = new Hashtable();
		cachedRecords.clear();
		currRecIndex = -1;
		try
		{
			cmpUin = uinToLong(uin);
			recordsEnum = recordStore.enumerateRecords(this, this, true);
			if (recordsEnum.numRecords() > 0)
			{
				currRecId = recordsEnum.previousRecordId();
				currRecIndex = recordsEnum.numRecords()-1;
			}
			else currRecId = currRecIndex = -1;
		}
		catch (Exception e)
		{
			return;
		}
		
		currCacheUin = uin;
	}
	
	// Returns record count for UIN
	public int getRecordCount(String uin)
	{
		if (!currCacheUin.equals(uin)) enumerateRecords(uin);
		return recordsEnum.numRecords();
	}
	
	// Returns full data of stored message
	public CachedRecord getRecord(String uin, int recNo)
	{
		if (!currCacheUin.equals(uin)) enumerateRecords(uin);
		byte[] data;
		CachedRecord result = null;
		
		try
		{
			if (currRecIndex == -1)
			{
				if (recordsEnum.numRecords() == 0) return null;
				else
				{
					currRecId = recordsEnum.previousRecordId();
					currRecIndex = recordsEnum.numRecords()-1;
				}
			}
			
			result = new CachedRecord();

			while (recNo != currRecIndex)
			{
				if (recNo < currRecIndex) 
				{
					currRecId = recordsEnum.previousRecordId();
					currRecIndex--;
				}
				else
				{
					currRecId = recordsEnum.nextRecordId();
					currRecIndex++;
				}
			}
			data = recordStore.getRecord(currRecId);
			result.type = data[8];
			ByteArrayInputStream bais = new ByteArrayInputStream(data, TEXT_START_INDEX, data.length-TEXT_START_INDEX);
			DataInputStream dis = new DataInputStream(bais);
			result.from = dis.readUTF();
			result.text = dis.readUTF();
			result.date = dis.readUTF();
		}
		catch (Exception e)
		{
			return null;
		}
		
		return result;
	}
	
	// returns cached short text of the message for history list
	public CachedRecord getCachedRecord(String uin, int recNo)
	{
		int maxLen = 20;
		CachedRecord cachedRec = (CachedRecord)cachedRecords.get(new Integer(recNo)); 
		if (cachedRec != null) return cachedRec;
		
		cachedRec = getRecord(uin, recNo);
		if (cachedRec == null) return null;
		if (cachedRec.text.length() > maxLen) 
			cachedRec.shortText = cachedRec.text.substring(0, maxLen)+"...";
		else
			cachedRec.shortText = cachedRec.text;
		
		cachedRecords.put(new Integer(recNo), cachedRec);
		
		return cachedRec;
	}
	
	// Shows history list on mobile phone screen
	public void showHistoryList(String uin, String nick)
	{
		if (list == null)
		{
			String caption = ResourceBundle.getString("history");
			list = new HistoryStorageList();
			//#sijapp cond.if target is "MIDP2"#
			list.setTitle(caption);
			list.setFullScreenMode(false);
			//#sijapp cond.else#
			list.setCaption(caption);
			//#sijapp cond.end#
		}
		
		HistoryStorageList.setCurrUin(uin, nick);
		
		list.lock();
		if (list.getSize() != 0) list.setCurrentItem(list.getSize()-1);
		list.unlock();
		Jimm.display.setCurrent(list);
	}
	
	// Clears messages history for UIN
	synchronized public void clearHistory(String uin)
	{
		try
		{
			if (!currCacheUin.equals(uin)) enumerateRecords(uin);
			recordsEnum.keepUpdated(false);
			recordsEnum.rebuild();
			recordsEnum.reset();
			cachedRecords = null;
			while (recordsEnum.hasNextElement())
			{
				recordStore.deleteRecord( recordsEnum.nextRecordId() );
			}
			recordsEnum.destroy();
			recordsEnum = null;
			if (cachedRecords != null) cachedRecords.clear();
			enumerateRecords(uin);
		}
		catch (Exception e)
		{
			DebugLog.addText("Error in HistoryStorage.clearHistory: "+e.toString());
		}
	}
	
	// Clears cache before hiding history list
	public void clearCache()
	{
		if (recordsEnum != null)
		{
			recordsEnum.destroy();
			recordsEnum = null;
		}
		
		if (cachedRecords != null)
		{
			cachedRecords.clear();
			cachedRecords = null;
		}
		
		list = null;
		
		currCacheUin = "";
		currRecIndex = -1;
	}
	
}

//#sijapp cond.end#
