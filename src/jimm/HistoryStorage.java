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
 Author(s): Artyomov Denis, Igor Palkin
 *******************************************************************************/

//#sijapp cond.if modules_HISTORY is "true" #
package jimm;

import java.util.*;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Font;
import javax.microedition.rms.RecordStore;
import jimm.util.ResourceBundle;
import java.io.*;
import javax.microedition.lcdui.*;
//#sijapp cond.if target="RIM" | target="MOTOROLA" | target="MIDP2"#
import javax.microedition.io.Connector;
//#sijapp cond.elseif target="SIEMENS2"#
//# import javax.microedition.io.Connector;
//# import com.siemens.mp.io.file.*;
//#sijapp cond.end#

import DrawControls.ListItem;
import DrawControls.VirtualList;
import DrawControls.TextList;
import DrawControls.VirtualListCommands;

import jimm.comm.Util;
import jimm.ContactItem;

// Class to cache one line in messages list
// All fields are public to easy and fast access
class CachedRecord
{
	String shortText, text, date, from;

	byte type; // 0 - incoming message, 1 - outgoing message

//#sijapp cond.if target != "DEFAULT"#
	boolean contains_url;
//#sijapp cond.end#
}

// Visual messages history list
class HistoryStorageList extends VirtualList implements CommandListener,
		VirtualListCommands
//#sijapp cond.if target != "DEFAULT"#
		, Runnable
//#sijapp cond.end#
{
	// commands for message text
	private static Command cmdMsgBack = new Command(ResourceBundle
			.getString("back"), Command.BACK, 1);

	private static Command cmdMsgNext = new Command(ResourceBundle
			.getString("next"), Command.ITEM, 2);

	private static Command cmdMsgPrev = new Command(ResourceBundle
			.getString("prev"), Command.ITEM, 3);

	private static Command cmdMsgCopyText = new Command(ResourceBundle
			.getString("copy_text"), Command.ITEM, 4);

	// commands for messages list
	private static Command cmdSelect = new Command(ResourceBundle
			.getString("select"), Command.OK, 1);

	private static Command cmdBack = new Command(ResourceBundle
			.getString("back"), Command.BACK, 2);

	private static Command cmdClear = new Command(ResourceBundle.getString(
			"clear", ResourceBundle.FLAG_ELLIPSIS), Command.ITEM, 4);

	private static Command cmdFind = new Command(ResourceBundle.getString(
			"find", ResourceBundle.FLAG_ELLIPSIS), Command.ITEM, 3);

	private static Command cmdInfo = new Command(ResourceBundle
			.getString("history_info"), Command.ITEM, 6);

	private static Command cmdCopytext = new Command(ResourceBundle
			.getString("copy_text"), Command.ITEM, 5);

	private static Command cmdExport = new Command(ResourceBundle
			.getString("export"), Command.ITEM, 7);

	private static Command cmdExportAll = new Command(ResourceBundle
			.getString("exportall"), Command.ITEM, 8);

//#sijapp cond.if target != "DEFAULT"#
	private static Command cmdGotoURL = new Command(ResourceBundle.getString("goto_url"), Command.ITEM, 9);
//#sijapp cond.end#
	
	//commands for url list
	private static Command cmdurlSelect = new Command(ResourceBundle
			.getString("select"), Command.OK, 1);

	private static Command cmdurlBack = new Command(ResourceBundle
			.getString("back"), Command.BACK, 2);

	static TextList messText;

	private final static int SELECTOR_DEL_HISTORY = 2;

	// list UIN
	private static String currUin = new String(), currName = new String();

	// Controls for finding text
	private static Form frmFind;

	private static Command cmdFindOk;

	private static Command cmdFindCancel;

	private static TextField tfldFind;

	private static ChoiceGroup chsFind;

	// Constructor
	public HistoryStorageList()
	{
		super(null);
		
		addCommandEx(cmdBack, VirtualList.MENU_TYPE_LEFT_BAR);
//#sijapp cond.if target!="RIM"#		
		addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_RIGHT_BAR);
//#sijapp cond.end#

		addCommandEx(cmdSelect, VirtualList.MENU_TYPE_RIGHT);
		addCommandEx(cmdClear, VirtualList.MENU_TYPE_RIGHT);
		addCommandEx(cmdFind, VirtualList.MENU_TYPE_RIGHT);
		addCommandEx(cmdInfo, VirtualList.MENU_TYPE_RIGHT);
		addCommandEx(cmdCopytext, VirtualList.MENU_TYPE_RIGHT);
		addCommandEx(cmdExport, VirtualList.MENU_TYPE_RIGHT);
		addCommandEx(cmdExportAll, VirtualList.MENU_TYPE_RIGHT);
		//addCommand(cmdGotoURL);
		setCommandListener(this);
		setVLCommands(this);
		JimmUI.setColorScheme(this, false, -1, true);
	}

	// VirtualList command impl.
	public void vlCursorMoved(VirtualList sender)
	{
		// user select some history storage line
		if (sender == this)
		{
			CachedRecord record = HistoryStorage.getCachedRecord(currUin,
					getCurrIndex());

			if (record == null)
				return;
//#sijapp cond.if target != "DEFAULT"#
			removeCommandEx(cmdGotoURL);
			if (record.contains_url)
				addCommandEx(cmdGotoURL, VirtualList.MENU_TYPE_RIGHT);
//#sijapp cond.end#
			setCaption(record.from + " " + record.date);
		}
	}

	// VirtualList command impl.
	public void vlKeyPress(VirtualList sender, int keyCode, int type)
	{
		if ((sender == messText) && (type == VirtualList.KEY_PRESSED))
		{
			switch (getGameAction(keyCode))
			{
			case Canvas.LEFT:
				moveInList(-1);
				break;
			case Canvas.RIGHT:
				moveInList(1);
				break;
			}
		}
	}

	// Moves on next/previous message in list and shows message text
	private void moveInList(int offset)
	{
		moveCursor(offset, false);
		showMessText();
	}

	// VirtualList command impl.
	public void vlItemClicked(VirtualList sender)
	{
		if (sender == this)
		{
			showMessText();
		}
	}

//#sijapp cond.if target != "DEFAULT"#
	private Object fileSystem; 
	private boolean cp1251;
	private String exportUin;
	private String directory;
	private TextList messTextList;

	public void export(String uin)
	{
		exportUin = uin;
		try
		{
			FileSystem2 fb = new FileSystem2();
			fb.browse(null, this, true);
			fileSystem = fb; 
		} catch (Exception e)
		{
		}
	}
	
	private static final int MODE_EXPORT_HISTORY = 1;
	private static final int MODE_SHOW_EH_MESSAGE = 2;
	private int currentMode;
	private String currMessage;

	public void run()
	{
		switch (currentMode)
		{
		case MODE_EXPORT_HISTORY:
			System.out.println("Start history export!");
			if (exportUin == null) startExport(null);
			else startExport(new ContactItem[]{ ContactList.getItembyUIN(exportUin) });
			break;
			
		case MODE_SHOW_EH_MESSAGE:
			messTextList = new TextList(currName);
			JimmUI.setColorScheme(messTextList, false, -1, true);
			messTextList.setFontSize(Font.SIZE_LARGE);
			messTextList.setMode(VirtualList.CURSOR_MODE_DISABLED);
			messTextList.addBigText(currMessage, messTextList.getTextColor(), Font.STYLE_PLAIN, -1);
			messTextList.setCommandListener(this);
			messTextList.addCommandEx(JimmUI.cmdOk, VirtualList.MENU_TYPE_RIGHT_BAR);
			messTextList.activate(Jimm.display);
			break;
		}
	}

	private void exportUinToStream(ContactItem item, OutputStream os)
			throws IOException
	{
		String endl = "\r\n";
		CachedRecord record;
		String uin = item
				.getStringValue(ContactItem.CONTACTITEM_UIN);
		int max = HistoryStorage.getRecordCount(uin);
		if (max > 0)
		{
			String nick = (item.getStringValue(
					ContactItem.CONTACTITEM_NAME).length() > 0) ? item
					.getStringValue(ContactItem.CONTACTITEM_NAME)
					: uin;
			SplashCanvas.setMessage(nick);
			SplashCanvas.setProgress(0);
			StringBuffer str_buf = new StringBuffer().append(endl).append(
					'\t').append(
					ResourceBundle.getString("message_history_with")).append(
					nick).append(" (").append(uin).append(')').append(endl).append('\t')
					.append(ResourceBundle.getString("export_date")).append(
							Util.getDateString(false)).append(endl).append(endl);
			os.write(Util.stringToByteArray(str_buf.toString(), !cp1251));
			for (int i = 0; i < max; i++)
			{
				record = HistoryStorage.getRecord(uin, i);
				os.write(Util.stringToByteArray(" "
						+ ((record.type == 0) ? nick : ResourceBundle
								.getString("me")) + " (" + record.date
						+ "):\r\n", !cp1251));
				String curr_msg_text = record.text.trim();
				StringBuffer msg_str_buf = new StringBuffer(curr_msg_text.length());
				for (int k = 0; k < curr_msg_text.length(); k++)
				{
					char cc = curr_msg_text.charAt(k);
					if (cc == '\r') continue;
					else if (cc == '\n') msg_str_buf.append(endl);
					else msg_str_buf.append(cc);
				}
				msg_str_buf.append(endl);
				os.write(Util.stringToByteArray(msg_str_buf.toString(), !cp1251));
				os.flush();
				SplashCanvas.setProgress((100 * i) / max);
			}
		}
	}

	private void exportUinToFile(ContactItem item, String filename) throws IOException
	{
		if (HistoryStorage.getRecordCount(item.getStringValue(ContactItem.CONTACTITEM_UIN)) > 0)
		{
			FileSystem2 fs = null;
			OutputStream os = null;
			try
			{
				fs = (FileSystem2)fileSystem;
				fs.openFile(filename, Connector.READ_WRITE);
				os = fs.openOutputStream();
				if (!cp1251) os.write(new byte[]{ (byte) 0xef, (byte) 0xbb, (byte) 0xbf });
				exportUinToStream(item, os);
			}
			finally
			{
				if (os != null) try { os.close(); } catch (Exception e) {}
				if (fs != null) try { fs.close(); } catch (Exception e) {}
			}
		}
	}

	public void startExport(ContactItem[] citems)
	{
		cp1251 = Options.getBoolean(Options.OPTION_CP1251_HACK);
		SplashCanvas.setMessage(ResourceBundle.getString("exporting",
				ResourceBundle.FLAG_ELLIPSIS));
		SplashCanvas.setProgress(0);
		Jimm.display.setCurrent(Jimm.jimm.getSplashCanvasRef());
		Jimm.setBkltOn(true);
		
		if (citems == null)
			citems = ContactList.getContactItems();
		
		try
		{
			for (int i = 0; i < citems.length; i++)
				exportUinToFile(
						citems[i],
						directory
								+ "jimm_hist_"
								+ citems[i]
										.getStringValue(ContactItem.CONTACTITEM_UIN)
								+ ".txt");
			
			
			((FileSystem2)fileSystem).close();
			currMessage = ResourceBundle.getString("export_complete");
		}
		catch (Exception e)
		{
			currMessage = e.toString()+e.getMessage();
		}
		fileSystem = null;
		
		System.out.println("currMessage="+currMessage);
		
		// Show success (or error message) in main thread
		currentMode = MODE_SHOW_EH_MESSAGE;
		Jimm.display.callSerially(this);
	}

	private static TextList URLList;

	private void gotoURL()
	{
		CachedRecord rec = HistoryStorage.getCachedRecord(currUin,
				getCurrIndex());
		Vector urls = Util.parseMessageForURL(rec.text + "\n");
		if (urls == null)
			return;

		if (urls.size() == 1)
			try
			{
				Jimm.jimm.platformRequest((String) urls.elementAt(0));
			} catch (Exception e)
			{
			}
		else
		{
			URLList = JimmUI.getInfoTextList(ResourceBundle.getString("goto_url"), false);
			URLList.addCommandEx(cmdurlSelect, VirtualList.MENU_TYPE_RIGHT_BAR);
			URLList.addCommandEx(cmdurlBack, VirtualList.MENU_TYPE_LEFT_BAR);
			URLList.setCommandListener(this);
			for (int i = 0; i < urls.size(); i++)
			{
				URLList.addBigText((String) urls.elementAt(i), getTextColor(),
						Font.STYLE_PLAIN, i).doCRLF(i);
			}
			JimmUI.showInfoTextList(URLList);
		}
	}

//#sijapp cond.end#

	public void commandAction(Command c, Displayable d)
	{
		Jimm.aaUserActivity();
		
//#sijapp cond.if target != "DEFAULT"#		
		if (JimmUI.isControlActive(messTextList))
		{
			messTextList = null;
			HistoryStorage.showHistoryList(currUin, currName);
			return;
		}
		
		if (fileSystem != null)
		{
			if (c == JimmUI.cmdOk)
			{
				FileSystem2 fs = (FileSystem2)fileSystem;
				directory = fs.getValue();
				currentMode = MODE_EXPORT_HISTORY;
				(new Thread(this)).start();
			}
			else
			{
				HistoryStorage.showHistoryList(currUin, currName);
				fileSystem = null;
			}
			return;
		}
		
		if (c == cmdurlBack)
		{
			URLList = null;
			HistoryStorage.showHistoryList(currUin, currName);
		}

		else if (c == cmdurlSelect)
		{
			try
			{
				Jimm.jimm.platformRequest(URLList.getCurrText(0, false));
			} catch (Exception e)
			{

			}
		}

		else if (c == cmdGotoURL)
			gotoURL();
		
		// Export history to txt file
		if (c == cmdExport)
			export(currUin);
		if (c == cmdExportAll)
			export(null);
//#sijapp cond.end#

		// back to contact list
		if (c == cmdBack)
		{
			HistoryStorage.clearCache();
			messText = null;
			JimmUI.backToLastScreen();
		}

		// select message
		else if (c == cmdSelect)
		{
			showMessText();
		}

		// Clear messages
		else if (c == cmdClear)
		{
			JimmUI.showSelector("clear_history", JimmUI.stdSelector, this,
					SELECTOR_DEL_HISTORY, true);
		}

		// "Clear history"
		else if (JimmUI.getCurScreenTag() == SELECTOR_DEL_HISTORY)
		{
			if (c == JimmUI.cmdOk)
			{
				switch (JimmUI.getLastSelIndex())
				{
				case 0: // Current
					HistoryStorage.clearHistory(currUin);
					break;

				case 1: // All except current
					HistoryStorage.clear_all(currUin);
					break;

				case 2: // All
					HistoryStorage.clear_all(null);
					break;
				}
			}
			activate(Jimm.display);
			invalidate();
		}

		// Copy text from messages list
		else if ((c == cmdCopytext) || (c == cmdMsgCopyText))
		{
			int index = getCurrIndex();
			if (index == -1)
				return;
			CachedRecord record = HistoryStorage
					.getCachedRecord(currUin, index);
			if (record == null)
				return;
			JimmUI.setClipBoardText((record.type == 0), record.date,
					record.from, record.text);
		}

		// back to messages list
		else if (c == cmdMsgBack)
		{
			activate(Jimm.display);
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

		// find command
		else if (c == cmdFind)
		{
			if (frmFind == null)
			{
				frmFind = new Form(ResourceBundle.getString("find"));
				cmdFindOk = new Command(ResourceBundle.getString("find"),
						Command.ITEM, 1);
				cmdFindCancel = new Command(ResourceBundle.getString("back"),
						Command.BACK, 2);
				tfldFind = new TextField(ResourceBundle
						.getString("text_to_find"), "", 64, TextField.ANY);

				chsFind = new ChoiceGroup(ResourceBundle.getString("options"),
						Choice.MULTIPLE);
				chsFind
						.append(ResourceBundle.getString("find_backwards"),
								null);
				chsFind.append(ResourceBundle.getString("find_case_sensitiv"),
						null);
				chsFind.setSelectedIndex(0, true);

				frmFind.addCommand(cmdFindOk);
				frmFind.addCommand(cmdFindCancel);
				frmFind.append(tfldFind);
				frmFind.append(chsFind);
				frmFind.setCommandListener(this);
			}
			Jimm.display.setCurrent(frmFind);
			Jimm.setBkltOn(true);
		}

		// user select ok command in find screen
		else if (c == cmdFindOk)
		{
			HistoryStorage.find(currUin, tfldFind.getString(), chsFind
					.isSelected(1), chsFind.isSelected(0));
		}

		else if (c == cmdFindCancel)
		{
			activate(Jimm.display);
		}

		// commands info
		else if (c == cmdInfo)
		{
			RecordStore rs = HistoryStorage.getRS();

			try
			{
				Alert alert = new Alert(ResourceBundle
						.getString("history_info"), (new StringBuffer())
						.append(ResourceBundle.getString("hist_cur")).append(
								": ").append(getSize()).append("\n").append(
								ResourceBundle.getString("hist_size")).append(
								": ").append(rs.getSize() / 1024).append("\n")
						.append(ResourceBundle.getString("hist_avail")).append(
								": ").append(rs.getSizeAvailable() / 1024)
						.append("\n").toString(), null, AlertType.INFO);
				alert.setTimeout(Alert.FOREVER);
				Jimm.display.setCurrent(alert);
				Jimm.setBkltOn(false);
			} catch (Exception e)
			{
			}
		}
	} // end 'commandAction'

	// Show text message of current message of messages list
	void showMessText()
	{
		if (this.getCurrIndex() >= this.getSize())
			return;
		if (messText == null)
		{
			messText = new TextList(null);
			messText.setMode(VirtualList.CURSOR_MODE_DISABLED);
			messText.setCommandListener(this);
			
			messText.addCommandEx(cmdMsgBack, VirtualList.MENU_TYPE_LEFT_BAR);
			
//#sijapp cond.if target!="RIM"#
			messText.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_RIGHT_BAR);
//#sijapp cond.end#
			
			messText.addCommandEx(cmdMsgNext, VirtualList.MENU_TYPE_RIGHT);
			messText.addCommandEx(cmdMsgPrev, VirtualList.MENU_TYPE_RIGHT);
			messText.addCommandEx(cmdMsgCopyText, VirtualList.MENU_TYPE_RIGHT);
			messText.setVLCommands(this);
			JimmUI.setColorScheme(messText, false, -1, true);
		}

		CachedRecord record = HistoryStorage.getRecord(currUin, this
				.getCurrIndex());

		messText.clear();
		messText.addBigText(record.date + ":", messText.getTextColor(),
				Font.STYLE_BOLD, -1);
		messText.doCRLF(-1);

//#sijapp cond.if modules_SMILES_STD="true" | modules_SMILES_ANI="true" #
		Emotions.addTextWithEmotions(messText, record.text, Font.STYLE_PLAIN, messText.getTextColor(), -1);
//#sijapp cond.else#
		messText.addBigText(record.text, messText.getTextColor(), Font.STYLE_PLAIN, -1);
//#sijapp cond.end#

//#sijapp cond.if target != "DEFAULT"#
		messText.removeCommandEx(cmdGotoURL);
		if (record.contains_url)
			messText.addCommandEx(cmdGotoURL, VirtualList.MENU_TYPE_RIGHT);
//#sijapp cond.end#

		messText.doCRLF(-1);
		messText.setCaption(record.from);

		messText.activate(Jimm.display);
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
		currUin = currUin_;
		currName = currName_;
	}

	// returns size of messages history list
	protected int getSize()
	{
		return HistoryStorage.getRecordCount(currUin);
	}

	// returns messages history list item data
	protected void get(int index, ListItem item)
	{
		CachedRecord record = HistoryStorage.getCachedRecord(currUin, index);
		if (record == null)
			return;
		item.text = record.shortText;
		item.color = (record.type == 0) ? getTextColor() : Options
				.getSchemeColor(Options.CLRSCHHEME_OUTGOING, -1);
	}
}

// History storage implementation
public class HistoryStorage
{
	//===================================//
	//                                   //
	//    Data storage implementation    //
	//                                   //
	//===================================//

	static final public int CLEAR_EACH_DAY = 0;

	static final public int CLEAR_EACH_WEEK = 1;

	static final public int CLEAR_EACH_MONTH = 2;

	static final private int VERSION = 1;

	static final private String prefix = "hist";

	private static RecordStore recordStore;

	private static HistoryStorageList list;

	private static String currCacheUin = new String();

	private static Hashtable cachedRecords;

	final static private int TEXT_START_INDEX = 1;

	public HistoryStorage()
	{
		try
		{
			RecordStore.deleteRecordStore("history");
		} catch (Exception e)
		{

		}
	}

	// Add message text to contact history
	static synchronized public void addText(String uin, // uin sended text  
			String text, // text to save
			byte type, // type of message 0 - incoming, 1 - outgouing
			String from, // text sender
			long time // date of message
	)
	{
		byte[] buffer, textData;
		int textLen;
		boolean lastLine = false;

		RecordStore recordStore = null;

		if (list != null)
		{
			if (list.getSize() == 0)
				lastLine = true;
			else if (list.getCurrIndex() == (list.getSize() - 1))
				lastLine = true;
		}

		boolean isCurrenty = currCacheUin.equals(uin);

		try
		{
			recordStore = isCurrenty ? HistoryStorage.recordStore : RecordStore
					.openRecordStore(getRSName(uin), true);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream das = new DataOutputStream(baos);
			das.writeUTF(from);
			das.writeUTF(text);
			das.writeUTF(Util.getDateString(false, time));
			textData = baos.toByteArray();
			textLen = textData.length;

			buffer = new byte[textLen + TEXT_START_INDEX];
			Util.putDWord(buffer, 0, recordStore.getNextRecordID());
			buffer[0] = type;
			System.arraycopy(textData, 0, buffer, TEXT_START_INDEX, textLen);

			recordStore.addRecord(buffer, 0, buffer.length);

			if (!isCurrenty && (recordStore != null))
			{
				recordStore.closeRecordStore();
				recordStore = null;
			}
		} catch (Exception e)
		{
		}

		if ((list != null) && HistoryStorageList.getCurrUin().equals(uin))
		{
			list.repaint();
			if (lastLine)
				list.setCurrentItem(list.getSize() - 1);
		}
	}

	// Returns reference for record store
	static RecordStore getRS()
	{
		return recordStore;
	}

	// Returns record store name for UIN
	static private String getRSName(String uin)
	{
		return prefix + uin;
	}

	// Opens record store for UIN
	static private void openUINRecords(String uin)
	{
		if (currCacheUin.equals(uin))
			return;

		try
		{
			if (recordStore != null)
			{
				recordStore.closeRecordStore();
				recordStore = null;
				System.gc();
			}
			recordStore = RecordStore.openRecordStore(getRSName(uin), true);
		} catch (Exception e)
		{
			recordStore = null;
			return;
		}
		currCacheUin = uin;

		if (cachedRecords == null)
			cachedRecords = new Hashtable();
	}

	// Returns record count for UIN
	static public int getRecordCount(String uin)
	{
		openUINRecords(uin);
		int result;
		try
		{
			result = recordStore.getNumRecords();
		} catch (Exception e)
		{
			result = 0;
		}

		return result;
	}

	// Returns full data of stored message
	static synchronized public CachedRecord getRecord(String uin, int recNo)
	{
		openUINRecords(uin);
		byte[] data;
		CachedRecord result = new CachedRecord();

		try
		{
			data = recordStore.getRecord(recNo + 1);
			result.type = data[0];
			ByteArrayInputStream bais = new ByteArrayInputStream(data,
					TEXT_START_INDEX, data.length - TEXT_START_INDEX);
			DataInputStream dis = new DataInputStream(bais);
			result.from = dis.readUTF();
			result.text = dis.readUTF();
			result.date = dis.readUTF();
//#sijapp cond.if target != "DEFAULT"#
			if (Util.parseMessageForURL(result.text) != null)
				result.contains_url = true;
			else
				result.contains_url = false;
//#sijapp cond.end#
		} 
		catch (Exception e)
		{
			result.text = result.date = result.from = "error";
//#sijapp cond.if target != "DEFAULT"#			
			result.contains_url = false;
//#sijapp cond.end#
			return null;
		}

		return result;
	}

	// returns cached short text of the message for history list
	static public CachedRecord getCachedRecord(String uin, int recNo)
	{
		int maxLen = 20;
		CachedRecord cachedRec = (CachedRecord) cachedRecords.get(new Integer(
				recNo));
		if (cachedRec != null)
			return cachedRec;

		cachedRec = getRecord(uin, recNo);
		if (cachedRec == null)
			return null;
		if (cachedRec.text.length() > maxLen)
			cachedRec.shortText = cachedRec.text.substring(0, maxLen) + "...";
		else
			cachedRec.shortText = cachedRec.text;

		cachedRec.shortText = cachedRec.shortText.replace('\n', ' ');
		cachedRec.shortText = cachedRec.shortText.replace('\r', ' ');

		cachedRecords.put(new Integer(recNo), cachedRec);

		return cachedRec;
	}

	// Shows history list on mobile phone screen
	static public void showHistoryList(String uin, String nick)
	{
		if (list == null)
		{
			String caption = ResourceBundle.getString("history");
			list = new HistoryStorageList();
			list.setCaption(caption);
		}

		HistoryStorageList.setCurrUin(uin, nick);

		list.lock();

		if (list.getSize() != 0)
			list.setCurrentItem(list.getSize() - 1);
		list.unlock();
		list.activate(Jimm.display);
	}

	// Clears messages history for UIN
	static synchronized public void clearHistory(String uin)
	{
		try
		{
			openUINRecords(uin);
			recordStore.closeRecordStore();
			recordStore = null;
			System.gc();
			RecordStore.deleteRecordStore(getRSName(uin));
			if (cachedRecords != null)
				cachedRecords.clear();
			currCacheUin = new String();
		} catch (Exception e)
		{
		}
	}

	// Clears cache before hiding history list
	static public void clearCache()
	{
		if (cachedRecords != null)
		{
			cachedRecords.clear();
			cachedRecords = null;
		}

		list = null;

		currCacheUin = new String();
	}

	static synchronized private boolean find_intern(String uin, String text,
			boolean case_sens, boolean back)
	{
		int index = list.getCurrIndex();
		if ((index < 0) || (index >= list.getSize()))
			return false;
		if (!case_sens)
			text = text.toLowerCase();
		int size = getRecordCount(uin);

		for (;;)
		{
			if ((index < 0) || (index >= size))
				break;
			CachedRecord record = getRecord(uin, index);
			String search_text = case_sens ? record.text : record.text
					.toLowerCase();
			if (search_text.indexOf(text) != -1)

			{
				list.setCurrentItem(index);
				list.activate(Jimm.display);
				return true;
			}

			if (back)
				index--;
			else
				index++;
		}
		return false;
	}

	// find text
	static void find(String uin, String text, boolean case_sens, boolean back)
	{
		if (list == null)
			return;
		boolean result = find_intern(uin, text, case_sens, back);
		if (result == true)
			return;

		Alert alert = new Alert(ResourceBundle.getString("find"),
				(new StringBuffer()).append(text).append("\n").append(
						ResourceBundle.getString("not_found")).toString(),
				null, AlertType.INFO);

		alert.setTimeout(Alert.FOREVER);
		list.activate(Jimm.display, alert);
	}

	// Clears all records for all uins
	static synchronized void clear_all(String except)
	{
		String exceptRMS = (except == null) ? null : getRSName(except);

		try
		{
			if (recordStore != null)
			{
				recordStore.closeRecordStore();
				recordStore = null;
				System.gc();
				currCacheUin = new String();
			}

			String[] stores = RecordStore.listRecordStores();

			for (int i = 0; i < stores.length; i++)
			{
				String store = stores[i];
				if (store.indexOf(prefix) == -1)
					continue;
				if (exceptRMS != null)
					if (exceptRMS.equals(store))
						continue;
				RecordStore.deleteRecordStore(store);
			}
		} catch (Exception e)
		{

		}
	}
}

//#sijapp cond.end#

