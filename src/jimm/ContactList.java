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
 File: src/jimm/ContactList.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Artyomov Denis
 *******************************************************************************/

package jimm;

import jimm.DebugLog;

import jimm.Jimm;
import jimm.comm.Message;
import jimm.comm.PlainMessage;
import jimm.comm.SendMessageAction;
import jimm.comm.Util;
import jimm.comm.Icq;
import jimm.util.ResourceBundle;

import java.util.Hashtable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import javax.microedition.lcdui.*;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

//#sijapp cond.if target!="DEFAULT"#
import javax.microedition.media.PlayerListener;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.ToneControl;
import javax.microedition.media.control.VolumeControl;
import java.io.InputStream;
//#sijapp cond.end#

//#sijapp cond.if target is "RIM"#
//import net.rim.device.api.system.Alert;
//# import net.rim.device.api.system.LED;
//#sijapp cond.end#

import DrawControls.*;
import jimm.Options;

//////////////////////////////////////////////////////////////////////////////////
public class ContactList implements CommandListener, VirtualTreeCommands,
		VirtualListCommands
//#sijapp cond.if target!="DEFAULT"#
		, PlayerListener
//#sijapp cond.end#
{
	/* Status (all are mutual exclusive) TODO: move status to ContactItem */
	public static final int STATUS_EVIL = 0x00003000;
	public static final int STATUS_DEPRESSION = 0x00004000;
	public static final int STATUS_HOME = 0x00005000;
	public static final int STATUS_WORK = 0x00006000;
	public static final int STATUS_LUNCH = 0x00002001;
	public static final int STATUS_AWAY = 0x00000001;
	public static final int STATUS_CHAT = 0x00000020;
	public static final int STATUS_DND = 0x00000002;
	public static final int STATUS_INVISIBLE = 0x00000100;
	public static final int STATUS_INVIS_ALL = 0x00000200;
	public static final int STATUS_NA = 0x00000004;
	public static final int STATUS_OCCUPIED = 0x00000010;
	public static final int STATUS_OFFLINE = 0xFFFFFFFF;
	public static final int STATUS_NONE = 0x10000000;
	public static final int STATUS_ONLINE = 0x00000000;

	/* Sound notification typs */
	public static final int SOUND_TYPE_MESSAGE = 1;
	public static final int SOUND_TYPE_ONLINE = 2;
	public static final int SOUND_TYPE_TYPING = 3;

	public static Image statusEvilImg;
	public static Image statusDepressionImg;
	public static Image statusHomeImg;
	public static Image statusWorkImg;
	public static Image statusLunchImg;
	public static Image statusAwayImg;
	public static Image statusChatImg;
	public static Image statusDndImg;
	public static Image statusInvisibleImg;
	public static Image statusNaImg;
	public static Image statusOccupiedImg;
	public static Image statusOfflineImg;
	public static Image statusOnlineImg;
	public static Image eventPlainMessageImg;
	public static Image eventUrlMessageImg;
	public static Image eventSystemNoticeImg;
	public static Image eventSysActionImg;
	
	public static boolean playerFree = true;
	private static boolean needPlayOnlineNotif = false;
	private static boolean needPlayMessNotif = false;

	private static ContactList _this;

	//#sijapp cond.if modules_DEBUGLOG is "true" #
	private static Command debugListCommand = new Command("*Debug list*",
			Command.ITEM, 2);

	//#sijapp cond.end#

	/** ************************************************************************* */

	/* Version id numbers */
	static private int ssiListLastChangeTime = -1;

	static private int ssiNumberOfItems = 0;

	/* Update help variable */
	private static boolean haveToBeCleared;

	/* Contact items */
	private static Vector cItems;

	/* Group items */
	private static Vector gItems;

	private static boolean treeBuilt = false, treeSorted = false;

	/* Contains tree nodes by groip ids */
	private static Hashtable gNodes = new Hashtable();

	/* Tree object */
	private static VirtualTree tree;

	/* Images for icons */
	final public static ImageList imageList;
	final public static ImageList smallIcons;
	final public static ImageList cliImages;
	final public static ImageList xStatusImages;

	//
	private static int onlineCounter;

	/* Initializer */
	static
	{
		/* Construct image objects */
		smallIcons = new ImageList();
		imageList = new ImageList();
		cliImages = new ImageList();
		xStatusImages = new ImageList();
		
		try
		{
			/* reads and divides image "icons.png" to several icons */
			imageList.load("/icons.png", -1, -1, -1);
			ContactList.statusEvilImg = imageList.elementAt(8);
			ContactList.statusDepressionImg = imageList.elementAt(9);
			ContactList.statusHomeImg = imageList.elementAt(10);
			ContactList.statusWorkImg = imageList.elementAt(11);
			ContactList.statusLunchImg = imageList.elementAt(12);
			ContactList.statusAwayImg = imageList.elementAt(0);
			ContactList.statusChatImg = imageList.elementAt(1);
			ContactList.statusDndImg = imageList.elementAt(2);
			ContactList.statusInvisibleImg = imageList.elementAt(3);
			ContactList.statusNaImg = imageList.elementAt(4);
			ContactList.statusOccupiedImg = imageList.elementAt(5);
			ContactList.statusOfflineImg = imageList.elementAt(6);
			ContactList.statusOnlineImg = imageList.elementAt(7);
			ContactList.eventPlainMessageImg = imageList.elementAt(13);
			ContactList.eventUrlMessageImg = imageList.elementAt(14);
			ContactList.eventSystemNoticeImg = imageList.elementAt(15);
			ContactList.eventSysActionImg = imageList.elementAt(16);
			smallIcons.load("/sicons.png", -1, -1, -1);
		} catch (Exception e) {}
		
		try
		{
			cliImages.load("/clicons.png", -1, -1, -1);
		} catch (Exception e) {}
		
		try
		{
			xStatusImages.load("/xstatus.png", -1, -1, -1);
		} catch (Exception e) {}
	}

	/* Constructor */
	public ContactList()
	{
		_this = this;
		try
		{
			load();
		} catch (Exception e)
		{
			haveToBeCleared = false;
			cItems = new Vector();
			gItems = new Vector();
		}
		
//#sijapp cond.if modules_ANTISPAM="true"#		
		antiSpamLoadList();
//#sijapp cond.end#

		tree = new VirtualTree(null, false);
		tree.setVTCommands(this);
		tree.setVLCommands(this);
		tree.setCyclingCursor(true);

		tree.setFontSize((imageList.getHeight() < 16) ? VirtualList.SMALL_FONT
				: VirtualList.MEDIUM_FONT);
		tree.setStepSize(0);

		//#sijapp cond.if modules_TRAFFIC is "true" #
		updateTitle(Traffic.getSessionTraffic());
		//#sijapp cond.else #
		//#        updateTitle(0);
		//#sijapp cond.end#

		tree.setCommandListener(this);
	}
	
	/* *********************************************************** */
	final static public int SORT_BY_NAME = 1;
	final static public int SORT_BY_STATUS = 0;
	static private int sortType;

	public int vtCompareNodes(TreeNode node1, TreeNode node2)
	{
		Object obj1 = node1.getData();
		Object obj2 = node2.getData();
		ContactListItem item1 = (ContactListItem) obj1;
		ContactListItem item2 = (ContactListItem) obj2;

		int result = 0;
		switch (sortType)
		{
		case SORT_BY_NAME:
			result = item1.getSortText().compareTo(item2.getSortText());
			break;
		case SORT_BY_STATUS:
			int weight1 = item1.getSortWeight();
			int weight2 = item2.getSortWeight();
			if (weight1 == weight2)
				result = item1.getSortText().compareTo(item2.getSortText());
			else
				result = (weight1 < weight2) ? -1 : 1;
			break;
		}

		return result;
	}

	/* *********************************************************** */
	
	/* Returns reference to tree */
	static public VirtualList getVisibleContactListRef()
	{
		return tree;
	}

	/* Returns image list with status icons and status icons with red letter "C" */
	static public ImageList getImageList()
	{
		return imageList;
	}

	/* Returns the id number #1 which identifies (together with id number #2)
	 the saved contact list version */
	static public int getSsiListLastChangeTime()
	{
		return ssiListLastChangeTime;
	}

	/* Returns the id number #2 which identifies (together with id number #1)
	 the saved contact list version */
	static public int getSsiNumberOfItems()
	{
		return (ssiNumberOfItems);
	}

	// Returns number of contact items
	static public int getSize()
	{
		return cItems.size();
	}

	static private ContactItem getCItem(int index)
	{
		return (ContactItem) cItems.elementAt(index);
	}

	// Returns all contact items as array
	static public synchronized ContactItem[] getContactItems()
	{
		ContactItem[] cItems_ = new ContactItem[cItems
				.size()];
		ContactList.cItems.copyInto(cItems_);
		return (cItems_);
	}

	// Returns all group items as array
	static public ContactListGroupItem[] getGroupItems()
	{
		synchronized (_this)
		{
			ContactListGroupItem[] gItems_ = new ContactListGroupItem[gItems.size()];
			ContactList.gItems.copyInto(gItems_);
			return (gItems_);
		}
	}

	// Request display of the given alert and the main menu afterwards
	static public void activate(Alert alert)
	{
		ContactList.tree.activate(Jimm.display, alert);
	}
	
	static public void showStatusInCaption(int status)
	{
		tree.setCapImage(smallIcons.elementAt
		(
			JimmUI.getStatusImageIndex(status == -1 ? Icq.getCurrentStatus() : status)
		));
	}

	// Request display of the main menu
	static public void activate()
	{
		Jimm.aaUserActivity();
		showStatusInCaption(-1);
		
		//#sijapp cond.if modules_TRAFFIC is "true" #
		updateTitle(Traffic.getSessionTraffic());
		//#sijapp cond.else #
		updateTitle(0);
		//#sijapp cond.end#
		
		// show contact list
		tree.lock();
		buildTree();
		sortAll();
		tree.unlock();
		
		tree.removeAllCommands();
		tree.addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_LEFT_BAR);
		tree.addCommandEx(JimmUI.cmdSelect, VirtualList.MENU_TYPE_RIGHT_BAR);

		//#sijapp cond.if modules_DEBUGLOG is "true" #
		tree.addCommandEx(debugListCommand, VirtualList.MENU_TYPE_RIGHT);
		//#sijapp cond.end#
		
		ContactList.tree.activate(Jimm.display);
		JimmUI.setLastScreen(ContactList.tree);

		//#sijapp cond.if target isnot "DEFAULT" #
		// play sound notifications after connecting 
		if (needPlayOnlineNotif)
		{
			needPlayOnlineNotif = false;
			playSoundNotification(SOUND_TYPE_ONLINE);
		}

		if (needPlayMessNotif)
		{
			needPlayMessNotif = false;
			playSoundNotification(SOUND_TYPE_MESSAGE);
		}
		//#sijapp cond.end#		
	}

	// is called by options form when options changed
	static public void optionsChanged(boolean needToRebuildTree,
			boolean needToSortContacts)
	{
		if (needToRebuildTree) treeBuilt = false;
		if (needToSortContacts) treeSorted = false;
	}

	// Tries to load contact list from record store
	static private void load() throws Exception, IOException, RecordStoreException
	{
		// Initialize vectors
		ContactList.cItems = new Vector();
		ContactList.gItems = new Vector();

		// Check whether record store exists
		String[] recordStores = RecordStore.listRecordStores();
		boolean exist = false;
		for (int i = 0; i < recordStores.length; i++)
		{
			if (recordStores[i].equals("contactlist"))
			{
				exist = true;
				break;
			}
		}
		if (!exist)
			throw (new Exception());

		// Open record store
		RecordStore cl = RecordStore.openRecordStore("contactlist", false);

		try
		{
			// Temporary variables
			byte[] buf;
			ByteArrayInputStream bais;
			DataInputStream dis;

			// Get version info from record store
			buf = cl.getRecord(1);
			bais = new ByteArrayInputStream(buf);
			dis = new DataInputStream(bais);
			if (!(dis.readUTF().equals(Jimm.VERSION)))
				throw (new IOException());

			// Get version ids from the record store
			buf = cl.getRecord(2);
			bais = new ByteArrayInputStream(buf);
			dis = new DataInputStream(bais);
			ssiListLastChangeTime = dis.readInt();
			ssiNumberOfItems = dis.readUnsignedShort();

			// Read all remaining items from the record store
			int marker = 3;

			//#sijapp cond.if modules_DEBUGLOG is "true"#
			System.gc();
			long mem = Runtime.getRuntime().freeMemory();
			//#sijapp cond.end#

			while (marker <= cl.getNumRecords())
			{

				// Get type of the next item
				buf = cl.getRecord(marker++);
				bais = new ByteArrayInputStream(buf);
				dis = new DataInputStream(bais);

				// Loop until no more items are available
				//int load = 0;
				while (dis.available() > 0)
				{
					// Get item type
					byte type = dis.readByte();

					// Normal contact
					if (type == 0)
					{
						// Instantiate ContactItem object and add to vector
						ContactItem ci = new ContactItem();
						ci.loadFromStream(dis);
						ContactList.cItems.addElement(ci);
					}
					// Group of contacts
					else if (type == 1)
					{
						// Instantiate ContactListGroupItem object and add to vector
						ContactListGroupItem gi = new ContactListGroupItem();
						gi.loadFromStream(dis);
						ContactList.gItems.addElement(gi);
					}
				}
			}

			//#sijapp cond.if modules_DEBUGLOG is "true"#
			buf = null;
			dis = null;
			bais = null;
			System.gc();
			System.out.println("\n clload mem used: "
					+ (mem - Runtime.getRuntime().freeMemory()));
			//#sijapp cond.end#
		} finally
		{
			// Close record store
			cl.closeRecordStore();
		}
	}

	// Save contact list to record store
	static protected void save() throws IOException, RecordStoreException
	{
		// Try to delete the record store
		try
		{
			RecordStore.deleteRecordStore("contactlist");
		} catch (RecordStoreNotFoundException e)
		{
			// Do nothing
		}

		// Create new record store
		RecordStore cl = RecordStore.openRecordStore("contactlist", true);

		// Temporary variables
		ByteArrayOutputStream baos;
		DataOutputStream dos;
		byte[] buf;

		// Add version info to record store
		baos = new ByteArrayOutputStream();
		dos = new DataOutputStream(baos);
		dos.writeUTF(Jimm.VERSION);
		buf = baos.toByteArray();
		cl.addRecord(buf, 0, buf.length);

		// Add version ids to the record store
		baos.reset();
		dos.writeInt(ssiListLastChangeTime);
		dos.writeShort((short) ssiNumberOfItems);
		buf = baos.toByteArray();
		cl.addRecord(buf, 0, buf.length);

		// Initialize buffer
		baos.reset();

		// Iterate through all contact items
		int cItemsCount = cItems.size();
		int totalCount = cItemsCount + gItems.size();
		for (int i = 0; i < totalCount; i++)
		{
			if (i < cItemsCount)
				getCItem(i).saveToStream(dos);
			else
			{
				ContactListGroupItem gItem = (ContactListGroupItem) gItems
						.elementAt(i - cItemsCount);
				gItem.saveToStream(dos);
			}

			// Start new record if it exceeds 4000 bytes
			if ((baos.size() >= 4000) || (i == totalCount - 1))
			{
				// Save record
				buf = baos.toByteArray();
				cl.addRecord(buf, 0, buf.length);

				// Initialize buffer
				baos.reset();
			}
		}

		// Close record store
		cl.closeRecordStore();
	}
	
	// called before jimm start to connect to server
	public static void beforeConnect()
	{
		treeBuilt = treeSorted = false;
		haveToBeCleared = true;
		tree.clear();
		setStatusesOffline();
	}

	static public void setStatusesOffline()
	{
		onlineCounter = 0;
		for (int i = cItems.size() - 1; i >= 0; i--)
		{
			ContactItem item = getCItem(i);
			item.setOfflineStatus();
		}

		for (int i = gItems.size() - 1; i >= 0; i--)
			((ContactListGroupItem) gItems.elementAt(i)).setCounters(0, 0);
	}

	// Returns array of uins of unuthorized and temporary contacts
	public static String[] getUnauthAndTempContacts()
	{
		Vector data = new Vector();
		for (int i = cItems.size() - 1; i >= 0; i--)
		{
			if (getCItem(i).getBooleanValue(
					ContactItem.CONTACTITEM_NO_AUTH)
					|| getCItem(i).getBooleanValue(
							ContactItem.CONTACTITEM_IS_TEMP))
				data.addElement(getCItem(i).getStringValue(
						ContactItem.CONTACTITEM_UIN));
		}
		String result[] = new String[data.size()];
		data.copyInto(result);
		return result;
	}

	// Updates the client-side contact list (called when a new roster has been
	// received)
	static public void update(int flags, int versionId1_, int versionId2_, ContactListItem[] items, Vector privData)
	{
		synchronized (_this)
		{
			//#sijapp cond.if modules_DEBUGLOG is "true"#
			System.out.println("New roster. versionId1_=" + ssiListLastChangeTime
					+ ", versionId2=" + versionId2_ + ", flags=" + flags);
			System.out.println("Old versionId1=" + ssiListLastChangeTime
					+ ", versionId2=" + ssiNumberOfItems + ", updated="
					+ haveToBeCleared);
			System.out.println();
			//#sijapp cond.end #

			// Remove all Elemente form the old ContactList
			if (haveToBeCleared)
			{
				cItems.removeAllElements();
				gItems.removeAllElements();
				haveToBeCleared = false;
				ssiNumberOfItems = 0;
			}

			// Add new contact items and group items
			for (int i = 0; i < items.length; i++)
			{
				if (items[i] instanceof ContactItem)
				{
					cItems.addElement(items[i]);
				} else if (items[i] instanceof ContactListGroupItem)
				{
					gItems.addElement(items[i]);
				}
			}
			ssiNumberOfItems += versionId2_;
			
			// Privacy data
			if (privData != null)
			{
				Hashtable list = new Hashtable();
				
				for (int i = cItems.size()-1; i >= 0; i--) 
					list.put(((ContactItem)cItems.elementAt(i)).getStringValue(ContactItem.CONTACTITEM_UIN), cItems.elementAt(i));
				
				int size = privData.size();
				for (int i = 0; i < size; i += 2)
				{
					String uin = (String)privData.elementAt(i);
					int[] data = (int[]) privData.elementAt(i+1);
					
					ContactItem ci = (ContactItem)list.get(uin);
					if (ci == null) continue;

					ci.setIntValue(data[0], data[1]);
				}
			}

			// Save new contact list
			if (versionId1_ != 0)
			{
				ssiListLastChangeTime = versionId1_;
				safeSave();
				treeBuilt = false;

				// Which contacts already have chats?
				for (int i = getSize() - 1; i >= 0; i--)
				{
					ContactItem cItem = getCItem(i);
					cItem.setBooleanValue
					(
						ContactItem.CONTACTITEM_HAS_CHAT,
						ChatHistory.chatHistoryExists(cItem.getStringValue(ContactItem.CONTACTITEM_UIN))
					);
				}
			}
		}
	}

	public static void safeSave()
	{
		try
		{
			save();
		} catch (Exception e)
		{
		}
	}

	//==================================//
	//                                  //
	//    WORKING WITH CONTACTS TREE    //
	//                                  //  
	//==================================//

	// Sorts the contacts and calc online counters
	static private void sortAll()
	{
		if (treeSorted) return;
		sortType = Options.getInt(Options.OPTION_CL_SORT_BY);
		if (Options.getBoolean(Options.OPTION_USER_GROUPS))
		{
			// Sort groups
			tree.sortNode(null);
			
			// Sort contacts
			for (int i = 0; i < gItems.size(); i++)
			{
				ContactListGroupItem gItem = (ContactListGroupItem) gItems.elementAt(i);
				TreeNode groupNode = (TreeNode) gNodes.get(new Integer(gItem.getId()));
				tree.sortNode(groupNode);
				calcGroupData(groupNode, gItem);
			}
		} else
			tree.sortNode(null);
		treeSorted = true;
	}

	// Builds contacts tree (without sorting) 
	static private void buildTree()
	{
		int i, gCount, cCount;
		boolean use_groups = Options.getBoolean(Options.OPTION_USER_GROUPS);
		boolean only_online = Options.getBoolean(Options.OPTION_CL_HIDE_OFFLINE);

		cCount = cItems.size();
		gCount = gItems.size();
		if (treeBuilt || ((cCount == 0) && (gCount == 0))) return;

		tree.clear();
		tree.setShowButtons(use_groups);

		// add group nodes
		gNodes.clear();

		if (use_groups)
		{
			for (i = 0; i < gCount; i++)
			{
				ContactListGroupItem item = (ContactListGroupItem) gItems.elementAt(i);
				TreeNode groupNode = tree.addNode(null, item);
				gNodes.put(new Integer(item.getId()), groupNode);
			}
		}

		// add contacts
		for (i = 0; i < cCount; i++)
		{
			ContactItem cItem = getCItem(i);

			if (only_online
					&& (cItem.getIntValue(ContactItem.CONTACTITEM_STATUS) == STATUS_OFFLINE)
					&& !cItem.mustBeShownAnyWay())
				continue;

			if (use_groups)
			{
				int group = cItem.getIntValue(ContactItem.CONTACTITEM_GROUP);
				TreeNode groupNode = (TreeNode) gNodes.get(new Integer(group));
				tree.addNode(groupNode, cItem);
			} else
			{
				tree.addNode(null, cItem);
			}
		}

		treeSorted = false;
		treeBuilt = true;
	}

	// Returns reference to group with id or null if group not found
	public static ContactListGroupItem getGroupById(int id)
	{
		for (int i = gItems.size() - 1; i >= 0; i--)
		{
			ContactListGroupItem group = (ContactListGroupItem) gItems.elementAt(i);
			if (group.getId() == id) return group;
		}
		return null;
	}

	// Returns reference to contact item with uin or null if not found  
	static public ContactItem getItembyUIN(String uin)
	{
		int uinInt;
		try {
			uinInt = Integer.parseInt(uin);
		} catch (NumberFormatException ne){
			return null;
		}
		for (int i = cItems.size() - 1; i >= 0; i--)
		{
			ContactItem citem = getCItem(i);
			if (citem.getUIN() == uinInt)
				return citem;
		}
		return null;
	}

	static public ContactItem[] getGroupItems(int groupId)
	{
		Vector vect = new Vector();
		for (int i = 0; i < cItems.size(); i++)
		{
			ContactItem cItem = getCItem(i);
			if (cItem.getIntValue(ContactItem.CONTACTITEM_GROUP) == groupId)
				vect.addElement(cItem);
		}

		ContactItem[] result = new ContactItem[vect
				.size()];
		vect.copyInto(result);

		return result;
	}

	// Calculates online/total values for group
	static private void calcGroupData(TreeNode groupNode,
			ContactListGroupItem group)
	{
		if ((group == null) || (groupNode == null))
			return;

		ContactItem cItem;
		int onlineCount = 0;

		int count = groupNode.size();
		for (int i = 0; i < count; i++)
		{
			if (!(groupNode.elementAt(i).getData() instanceof ContactItem))
				continue; // TODO: must be removed
			cItem = (ContactItem) groupNode.elementAt(i).getData();
			if (cItem.getIntValue(ContactItem.CONTACTITEM_STATUS) != STATUS_OFFLINE)
				onlineCount++;
		}
		group.setCounters(onlineCount, count);
	}

	// Must be called after any changes in contacts
	public static void contactChanged(ContactItem item,
			boolean setCurrent, boolean needSorting)
	{
		if (!treeBuilt)
			return;

		boolean contactExistInTree = false, contactExistsInList, wasDeleted = false, haveToAdd = false, haveToDelete = false;
		TreeNode cItemNode = null;
		int i, count, groupId;

		int status = item
				.getIntValue(ContactItem.CONTACTITEM_STATUS);

		String uin = item
				.getStringValue(ContactItem.CONTACTITEM_UIN);

		// which group id ?
		groupId = item.getIntValue(ContactItem.CONTACTITEM_GROUP);

		boolean only_online = Options
				.getBoolean(Options.OPTION_CL_HIDE_OFFLINE);

		// Whitch group node?
		TreeNode groupNode = (TreeNode) gNodes.get(new Integer(groupId));
		if (groupNode == null)
			groupNode = tree.getRoot();

		// Does contact exists in tree?
		count = groupNode.size();
		for (i = 0; i < count; i++)
		{
			cItemNode = groupNode.elementAt(i);
			Object data = cItemNode.getData();
			if (!(data instanceof ContactItem))
				continue;
			if (!((ContactItem) data).getStringValue(
					ContactItem.CONTACTITEM_UIN).equals(uin))
				continue;
			contactExistInTree = true;
			break;
		}

		// Does contact exists in internal list?
		contactExistsInList = (cItems.indexOf(item) != -1);

		// Lock tree repainting
		tree.lock();

		haveToAdd = contactExistsInList && !contactExistInTree;
		if (only_online && !contactExistInTree)
			haveToAdd |= ((status != STATUS_OFFLINE) | item.mustBeShownAnyWay());

		haveToDelete = !contactExistsInList && contactExistInTree;
		if (only_online && contactExistInTree)
			haveToDelete |= ((status == STATUS_OFFLINE) && !item
					.mustBeShownAnyWay());

		// if have to add new contact
		if (haveToAdd && !contactExistInTree)
		{
			cItemNode = tree.addNode(groupNode, item);
		}

		// if have to delete contact
		else if (haveToDelete)
		{
			tree.removeNode(cItemNode);
			wasDeleted = true;
		}

		// sort group
		if (needSorting && !wasDeleted)
		{
			boolean isCurrent = (tree.getCurrentItem() == cItemNode), inserted = false;

			tree.deleteChild(groupNode, tree.getIndexOfChild(groupNode,
					cItemNode));

			int contCount = groupNode.size();
			sortType = Options.getInt(Options.OPTION_CL_SORT_BY);

			// TODO: Make binary search instead of linear before child insertion!!!
			for (int j = 0; j < contCount; j++)
			{
				TreeNode testNode = groupNode.elementAt(j);
				if (!(testNode.getData() instanceof ContactItem))
					continue;
				if (_this.vtCompareNodes(cItemNode, testNode) < 0)
				{
					tree.insertChild(groupNode, cItemNode, j);
					inserted = true;
					break;
				}
			}
			if (!inserted)
				tree.insertChild(groupNode, cItemNode, contCount);
			if (isCurrent)
				tree.setCurrentItem(cItemNode);
		}

		// if set current
		if (setCurrent)
			tree.setCurrentItem(cItemNode);

		// unlock tree and repaint
		tree.unlock();

		// change status for chat (if exists)
		item.setStatusImage();
	}

	/* lastUnknownStatus is used for adding contact item as sometimes online messages
	 is received before contact is added to internal list */
	private static int lastUnknownStatus = STATUS_NONE;

	// Updates the client-side contact list (called when a contact changes status)
	// DO NOT CALL THIS DIRECTLY FROM OTHER THREAND THAN MAIN!
	// USE RunnableImpl.updateContactList INSTEAD!
	static public synchronized void update(String uin, int status, int xStatus,
			byte[] internalIP, byte[] externalIP, int dcPort, int dcType,
			int icqProt, int authCookie, int signon, int online, int idle)
	{
		ContactItem cItem = getItembyUIN(uin);

		int trueStatus = Util.translateStatusReceived(status);

		if (cItem == null)
		{
			lastUnknownStatus = trueStatus;
			return;
		}

		long oldStatus = cItem.getIntValue(ContactItem.CONTACTITEM_STATUS);
		long oldXStatus = cItem.getIntValue(ContactItem.CONTACTITEM_XSTATUS);

		boolean statusChanged = (oldStatus != trueStatus) || (xStatus != oldXStatus);
		boolean wasOnline = (oldStatus != STATUS_OFFLINE);
		boolean nowOnline = (trueStatus != STATUS_OFFLINE);

		if (status == STATUS_OFFLINE)
		{
			//#sijapp cond.if target isnot "DEFAULT"#
			cItem.BeginTyping(false);
			//#sijapp cond.end#
		}

		// Online counters
		statusChanged(cItem, wasOnline, nowOnline, 0);

		// Set Status
		cItem.setIntValue(ContactItem.CONTACTITEM_STATUS, trueStatus);
		
		// Set x-status
		cItem.setIntValue(ContactItem.CONTACTITEM_XSTATUS, xStatus);

		// Update DC values
		//#sijapp cond.if (target="MIDP2" | target="MOTOROLA" | target="SIEMENS2") & modules_FILES="true"#
		if (dcType != -1)
		{
			cItem.setBytesArray(ContactItem.CONTACTITEM_INTERNAL_IP,
					internalIP);
			cItem.setBytesArray(ContactItem.CONTACTITEM_EXTERNAL_IP,
					externalIP);
			cItem.setIntValue(ContactItem.CONTACTITEM_DC_PORT,
					(int) dcPort);
			cItem.setIntValue(ContactItem.CONTACTITEM_DC_TYPE,
					dcType);
			cItem.setIntValue(ContactItem.CONTACTITEM_ICQ_PROT,
					icqProt);
			cItem.setIntValue(ContactItem.CONTACTITEM_AUTH_COOKIE,
					authCookie);
		}
		//#sijapp cond.end#

		// Update time values
		cItem.setIntValue(ContactItem.CONTACTITEM_SIGNON, signon);
		cItem.setIntValue(ContactItem.CONTACTITEM_ONLINE, online);
		cItem.setIntValue(ContactItem.CONTACTITEM_IDLE, idle);

		// Play sound notice if selected
		if ((trueStatus == STATUS_ONLINE) && statusChanged && !treeBuilt)
			needPlayOnlineNotif |= true;

		// Update visual list
		if (statusChanged)
			contactChanged(cItem, false, (wasOnline && !nowOnline)
					|| (!wasOnline && nowOnline));
		
		Object curScr = JimmUI.getCurrentScreen();
		if (tree.isActive())
		{
			String text = null;
			if (oldStatus != trueStatus) text = JimmUI.getStatusString(trueStatus);
			if ((xStatus != oldXStatus) && (xStatus >= 0)) text = ResourceBundle.getString(JimmUI.xStatusStrings[xStatus+1]);
			if (text != null) JimmUI.showCapText(curScr, cItem.getStringValue(ContactItem.CONTACTITEM_NAME)+": "+text, TimerTasks.TYPE_FLASH);
		}
	}

	//#sijapp cond.if target isnot "DEFAULT" #
	static public void checkAndPlayOnlineSound(String uin, int newStatus)
	{
		ContactItem cItem = getItembyUIN(uin);
		if (cItem == null)
			return;
		int trueStatus = Util.translateStatusReceived(newStatus);
		if ((trueStatus == STATUS_ONLINE)
				&& (cItem
						.getIntValue(ContactItem.CONTACTITEM_STATUS) != STATUS_ONLINE))
			playSoundNotification(SOUND_TYPE_ONLINE);
	}

	//#sijapp cond.end#    

	// Updates the client-side contact list (called when a contact changes status)
	static public synchronized void update(String uin, int status)
	{
		update(uin, status, -1, null, null, 0, 0, -1, 0, -1, -1, -1);
	}

	static private void statusChanged(ContactItem cItem,
			boolean wasOnline, boolean nowOnline, int tolalChanges)
	{
		boolean changed = false;

		// which group id ?
		int groupId = cItem
				.getIntValue(ContactItem.CONTACTITEM_GROUP);

		// which group ?
		ContactListGroupItem group = getGroupById(groupId);

		// Calc online counters
		if (wasOnline && !nowOnline)
		{
			onlineCounter--;
			if (group != null)
				group.updateCounters(-1, 0);
			changed = true;
		}

		if (!wasOnline && nowOnline)
		{
			onlineCounter++;
			if (group != null)
				group.updateCounters(1, 0);
			changed = true;
		}

		if (group != null)
		{
			group.updateCounters(0, tolalChanges);
			changed |= (tolalChanges != 0);
		}

		if (changed)
			RunnableImpl.updateContactListCaption();
	}

	//Updates the title of the list
	static public void updateTitle(int traffic)
	{
		String text = onlineCounter + "/" + cItems.size();
		if (traffic != 0) text += " " + traffic + ResourceBundle.getString("kb");
		//#sijapp cond.if target is "SIEMENS2"#
		//#		if( Options.getBoolean(Options.OPTION_FULL_SCREEN) )
		//#		{
		//#			String accuLevel = System.getProperty("MPJC_CAP");
		//#			if( accuLevel != null )
		//#				text += " " + accuLevel + "%";
		//#		}
		//#sijapp cond.end#
		tree.setCaption(text);
	}

	// Removes a contact list item
	static public void removeContactItem(ContactItem cItem)
	{
		synchronized (_this)
		{
			// Remove given contact item
			ContactList.cItems.removeElement(cItem);

			// Update visual list
			contactChanged(cItem, false, false);

			// Update online counters
			statusChanged(
					cItem,
					cItem.getIntValue(ContactItem.CONTACTITEM_STATUS) != STATUS_OFFLINE,
					false, -1);

			// Save list
			safeSave();
		}
	}

	// Adds a contact list item
	static public void addContactItem(ContactItem cItem)
	{
		synchronized (_this)
		{
			if (!cItem.getBooleanValue(ContactItem.CONTACTITEM_ADDED))
			{
				// does contact already exists or temporary ?
				ContactItem oldItem = getItembyUIN(cItem
						.getStringValue(ContactItem.CONTACTITEM_UIN));
				if (oldItem != null)
				{
					removeContactItem(oldItem);
					lastUnknownStatus = oldItem
							.getIntValue(ContactItem.CONTACTITEM_STATUS);
				}

				// Add given contact item
				cItems.addElement(cItem);
				cItem.setBooleanValue(ContactItem.CONTACTITEM_ADDED,
						true);

				// Check is chat availible 
				cItem
						.setBooleanValue(
								ContactItem.CONTACTITEM_HAS_CHAT,
								ChatHistory
										.chatHistoryExists(cItem
												.getStringValue(ContactItem.CONTACTITEM_UIN)));

				// Set contact status (if already received)
				if (lastUnknownStatus != STATUS_NONE)
				{
					cItem.setIntValue(ContactItem.CONTACTITEM_STATUS,
							lastUnknownStatus);
					lastUnknownStatus = STATUS_NONE;
				}

				// Update visual list
				contactChanged(cItem, true, true);

				// Update online counters
				statusChanged(
						cItem,
						false,
						cItem
								.getIntValue(ContactItem.CONTACTITEM_STATUS) != STATUS_OFFLINE,
						1);

				// Save list
				safeSave();
			}
		}
	}

	// Adds new group
	static public void addGroup(ContactListGroupItem gItem)
	{
		synchronized (_this)
		{
			gItems.addElement(gItem);
			if (!Options.getBoolean(Options.OPTION_USER_GROUPS))
				return;
			TreeNode groupNode = tree.addNode(null, gItem);
			gNodes.put(new Integer(gItem.getId()), groupNode);
			safeSave();
		}
	}

	// removes existing group 
	static public void removeGroup(ContactListGroupItem gItem)
	{
		synchronized (_this)
		{
			for (int i = cItems.size()-1; i >= 0; i--)
			{
				ContactItem cItem = getCItem(i);
				if (cItem.getIntValue(ContactItem.CONTACTITEM_GROUP) == gItem.getId())
				{
					if (cItem.getIntValue(ContactItem.CONTACTITEM_STATUS) != STATUS_OFFLINE)
						onlineCounter--;
					cItems.removeElementAt(i);
				}
			}
			Integer groupId = new Integer(gItem.getId());
			if (Options.getBoolean(Options.OPTION_USER_GROUPS))
			{
				TreeNode node = (TreeNode) gNodes.get(groupId);
				tree.deleteChild(tree.getRoot(), tree.getIndexOfChild(tree.getRoot(), node));
				gNodes.remove(groupId);
			}
			gItems.removeElement(gItem);
			safeSave();
		}
	}

	static public ContactItem createTempContact(String uin)
	{
		synchronized (_this)
		{
			ContactItem cItem = getItembyUIN(uin);

			if (cItem != null) return cItem;

			try
			{
				cItem = new ContactItem(0, 0, uin, uin, false, true);
			} catch (Exception e)
			{
				// Message from non-icq contact
				return null;
			}
			cItems.addElement(cItem);
			cItem.setBooleanValue(ContactItem.CONTACTITEM_IS_TEMP, true);
			return cItem;
		}
	}

	/* Adds the given message to the message queue of the contact item
	 identified by the given UIN */
	static public void addMessage(Message message)
	{
		synchronized (_this)
		{
			String uin = message.getSndrUin();
			
			ContactItem cItem = getItembyUIN(uin);
			
//#sijapp cond.if modules_ANTISPAM="true"#			
			if (cItem == null 
				|| cItem.getBooleanValue(ContactItem.CONTACTITEM_NO_AUTH|ContactItem.CONTACTITEM_IS_TEMP) // This is hack. Don't do like this
				|| cItem.getIntValue(ContactItem.CONTACTITEM_GROUP) == 0) 
			{
				if (message.needCheckForSpam()) 
				{
					boolean checked = antiSpamCheckContactFor(uin, message.getText());
					if (!checked) return;
				}
			}
//#sijapp cond.end#
			
			/* Create a temporary contact entry if no contact entry could be found
			 do we have a new temp contact */
			if (cItem == null)
				cItem = createTempContact(uin);

			/* Add message to chat */
			ChatHistory.addMessage(cItem, message);

			/* Notify splash canvas */
			SplashCanvas.messageAvailable();

			/* Notify user */
			if (!treeBuilt) needPlayMessNotif |= true;
//#sijapp cond.if target isnot "DEFAULT" #
			else playSoundNotification(SOUND_TYPE_MESSAGE);
//#sijapp cond.end #

			/* Flag contact as having chat */
			cItem.setBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT, true);

			/* Increment messages count for group */
			ContactListGroupItem gItem = getGroupById(cItem
					.getIntValue(ContactItem.CONTACTITEM_GROUP));
			if (gItem != null)
				gItem.changeMessCount(+1);

			/* Update tree */
			contactChanged(cItem, true, false);
		}
	}

	//#sijapp cond.if target!="DEFAULT"#

	public static boolean testSoundFile(String source)
	{
		playerFree = true;
		Player player = createPlayer(source);
		boolean ok = (player != null);
		if (player != null)
			player.close();
		playerFree = true;
		return ok;
	}

	//#sijapp cond.end#    

	//#sijapp cond.if target!="DEFAULT"#
	// Reaction to player events. (Thanks to Alexander Barannik for idea!)
	public void playerUpdate(final Player player, final String event,
			Object eventData)
	{
		if (event.equals(PlayerListener.END_OF_MEDIA))
		{
			player.close();
			playerFree = true;
		}
	}

	/* Creates player for file 'source' */
	static private Player createPlayer(String source)
	{
		if (!playerFree)
			return null;

		String url, mediaType;
		Player p;

		url = source.toLowerCase();

		/* What is media type? */
		if (url.endsWith("mp3")) 
			mediaType = "audio/mpeg";
		else if (url.endsWith("amr"))
			mediaType = "audio/amr";
		else if (url.endsWith("jts"))
			mediaType = "audio/x-tone-seq";
		else if (url.endsWith("mid") || url.endsWith("midi"))
			mediaType = "audio/midi";
		else
			mediaType = "audio/X-wav";

		try
		{
			Class cls = new Object().getClass();
			InputStream is = cls.getResourceAsStream(source);
			if (is == null)
				is = cls.getResourceAsStream("/" + source);
			if (is == null)
				return null;
			p = Manager.createPlayer(is, mediaType);
			playerFree = false;
			p.addPlayerListener(_this);
		} catch (Exception e)
		{
			return null;
		}
		return p;
	}
	//#sijapp cond.end#

	//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
	// sets volume for player
	static private void setVolume(Player p, int value)
	{
		try
		{
			p.realize();
			VolumeControl c = (VolumeControl) p.getControl("VolumeControl");
			if (c != null)
			{
				c.setLevel(value);
				p.prefetch();
			}
		} catch (Exception e)
		{
		}
	}

	//#sijapp cond.end#

	//#sijapp cond.if target isnot "DEFAULT"#
	synchronized static public void BeginTyping(String uin, boolean type)
	{
		ContactItem item = getItembyUIN(uin);
		if (item == null) return;

		// If the user does not have it add the typing capability
		if (!item.hasCapability(Icq.CAPF_TYPING)) item.addCapability(Icq.CAPF_TYPING);
		item.BeginTyping(type);
		
		if (type) playSoundNotification(ContactList.SOUND_TYPE_TYPING);
		
		if (ChatHistory.chatHistoryShown(uin)) ChatHistory.getChatHistoryAt(uin).BeginTyping(type);
		else tree.repaint();
	}
	//#sijapp cond.end#

	//#sijapp cond.if target isnot  "DEFAULT"#		
	// Play a sound notification
	static public void playSoundNotification(int notType)
	{
		synchronized (_this)
		{
			if (!treeBuilt)
				return;

			int vibraKind = Options.getInt(Options.OPTION_VIBRATOR);
			if (vibraKind == 2)
				vibraKind = SplashCanvas.locked() ? 1 : 0;
			if ((vibraKind > 0) && (notType == SOUND_TYPE_MESSAGE))
			{
				Jimm.display.vibrate(500);
			}

			if (Options.getBoolean(Options.OPTION_SILENT_MODE) == true)
				return;

			//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#

			int not_mode = 0;

			switch (notType)
			{
			case SOUND_TYPE_MESSAGE:
				not_mode = Options.getInt(Options.OPTION_MESS_NOTIF_MODE);
				break;

			case SOUND_TYPE_ONLINE:
				not_mode = Options.getInt(Options.OPTION_ONLINE_NOTIF_MODE);
				break;

			case SOUND_TYPE_TYPING:
				not_mode = Options.getInt(Options.OPTION_TYPING_MODE) - 1;
				break;
			}

			switch (not_mode)
			{
			case 1:
				try
				{
					switch (notType)
					{
					case SOUND_TYPE_MESSAGE:
						Manager.playTone(ToneControl.C4, 500, Options
								.getInt(Options.OPTION_MESS_NOTIF_VOL));
						break;
					case SOUND_TYPE_ONLINE:

					case SOUND_TYPE_TYPING:
						Manager.playTone(ToneControl.C4 + 7, 500, Options
								.getInt(Options.OPTION_ONLINE_NOTIF_VOL));
					}

				} catch (Exception e)
				{
					//Do nothing
				}
				break;

			case 2:
				try
				{
					Player p;

					if (notType == SOUND_TYPE_MESSAGE)
					{
						//Siemens 65-75 bugfix
						//#sijapp cond.if target is "SIEMENS2"#
						//#	        			Player p1 = createPlayer("silence.wav");
						//#        				setVolume(p1,100);
						//#        				p1.start();
						//#        				p1.close();
						//#        				playerFree = true;
						//#sijapp cond.end#
						p = createPlayer(Options
								.getString(Options.OPTION_MESS_NOTIF_FILE));
						if (p == null)
							return;
						setVolume(p, Options
								.getInt(Options.OPTION_MESS_NOTIF_VOL));
					} else if (notType == SOUND_TYPE_ONLINE)
					{
						// Siemens 65-75 bugfix
						//#sijapp cond.if target is "SIEMENS2"#
						//#        				Player p1 = createPlayer("silence.wav");
						//#        				setVolume(p1,100);
						//#        				p1.start();
						//#	        			p1.close();
						//#    	    			playerFree = true;
						//#sijapp cond.end#
						p = createPlayer(Options
								.getString(Options.OPTION_ONLINE_NOTIF_FILE));
						if (p == null)
							return;
						setVolume(p, Options
								.getInt(Options.OPTION_ONLINE_NOTIF_VOL));
					} else
					{
						// Siemens 65-75 bugfix
						//#sijapp cond.if target is "SIEMENS2"#
						//#        				Player p1 = createPlayer("silence.wav");
						//#        				setVolume(p1,100);
						//#        				p1.start();
						//#        				p1.close();
						//#        				playerFree = true;
						//#sijapp cond.end#
						p = createPlayer(Options
								.getString(Options.OPTION_TYPING_FILE));
						if (p == null)
							return;
						setVolume(p, Options.getInt(Options.OPTION_TYPING_VOL));
					}

					p.start();
				} catch (Exception me)
				{
					// Do nothing
				}

				break;

			}

			//#sijapp cond.end#
		}

	}

	//#sijapp cond.end#

	//#sijapp cond.if target isnot "DEFAULT"#    
	static public boolean changeSoundMode(boolean activate)
	{
		boolean newValue = !Options.getBoolean(Options.OPTION_SILENT_MODE);
		Options.setBoolean(Options.OPTION_SILENT_MODE, newValue);
		Options.safe_save();
		Alert alert = new Alert(null, ResourceBundle
				.getString(newValue ? "#sound_is_off" : "#sound_is_on"), null,
				null);
		alert.setTimeout(1000);
		
		if (activate) tree.activate(Jimm.display, alert);
		else 
		{
			Jimm.display.setCurrent(alert, Jimm.display.getCurrent());
			Jimm.setBkltOn(false);
		}
		return newValue;
	}

	//#sijapp cond.end#

	static ContactItem lastChatItem = null;

	public void vtGetItemDrawData(TreeNode src, ListItem dst)
	{
		ContactListItem item = (ContactListItem) src.getData();
		dst.text = item.getText();
		dst.leftImage = imageList.elementAt(item.getLeftImageIndex(src.getExpanded()));
		dst.rightImage = 
			Options.getBoolean(Options.OPTION_CL_CLIENTS) ? 
			cliImages.elementAt(item.getRightImageIndex()) : 
			null;
			
		dst.secondLeftImage =
			Options.getBoolean(Options.OPTION_XSTATUSES) ? 
			xStatusImages.elementAt(item.getSecondLeftImageIndex()) :
			null;
		
		dst.color = item.getTextColor();
		dst.fontStyle = item.getFontStyle();
	}

	public void vlCursorMoved(VirtualList sender)  {}
	public void vlItemClicked(VirtualList sender)  {}

	public void vlKeyPress(VirtualList sender, int keyCode, int type)
	{
		TreeNode node = tree.getCurrentItem();
		ContactItem item = ((node != null) && (node.getData() instanceof ContactItem)) ? (ContactItem) node
				.getData()
				: null;
		JimmUI.execHotKey(item, keyCode, type);
		
		if (type == VirtualList.KEY_PRESSED) Jimm.aaUserActivity();
	}

	// shows next or previos chat 
	static synchronized protected String showNextPrevChat(boolean next)
	{
		int index = cItems.indexOf(lastChatItem);
		if (index == -1)
			return null;
		int di = next ? 1 : -1;
		int maxSize = cItems.size();

		for (int i = index + di;; i += di)
		{
			if (i < 0)
				i = maxSize - 1;
			if (i >= maxSize)
				i = 0;
			if (i == index)
				break;

			ContactItem cItem = getCItem(i);
			if (cItem
					.getBooleanValue(ContactItem.CONTACTITEM_HAS_CHAT))
			{
				lastChatItem = cItem;
				cItem.activate();
				return cItem.getStringValue(ContactItem.CONTACTITEM_UIN);
			}
		}
		return null;
	}

	// Returns number of unread messages 
	static protected int getUnreadMessCount()
	{
		int count = cItems.size();
		int result = 0;
		for (int i = 0; i < count; i++)
			result += getCItem(i).getUnreadMessCount();
		return result;
	}

	static public ContactItem[] getItems(ContactListGroupItem group)
	{
		Vector data = new Vector();
		int gid = group.getId();
		int size = getSize();
		for (int i = 0; i < size; i++)
		{
			ContactItem item = getCItem(i);
			if (item.getIntValue(ContactItem.CONTACTITEM_GROUP) == gid)
				data.addElement(item);
		}
		ContactItem[] result = new ContactItem[data
				.size()];
		data.copyInto(result);
		return result;
	}

	// Command listener
	public void commandAction(Command c, Displayable d)
	{
		Jimm.aaUserActivity();
		
		// Activate main menu
		if (c == JimmUI.cmdMenu)
		{
			MainMenu.activate();
		}

		// Contact item has been selected
		else if (c == JimmUI.cmdSelect)
		{
			TreeNode node = tree.getCurrentItem();
			if (node == null) return;
			ContactListItem item = (ContactListItem) node.getData();
			
			if (item instanceof ContactItem)
			{
				// Activate the contact item menu
				//#sijapp cond.if target is "RIM"#
				//#			LED.setState(LED.STATE_OFF);
				//#sijapp cond.end#

				lastChatItem = (ContactItem) item;
				lastChatItem.activate();
			}
			
			if (item instanceof ContactListGroupItem)
			{
				boolean newExpanded = !node.getExpanded();
				if (!newExpanded)
				{
					ContactListGroupItem gItem = (ContactListGroupItem)item;
					ContactItem[] cItems = getItems(gItem);
					int unreadCounter = 0;
					for (int i = 0; i < cItems.length; i++) unreadCounter += cItems[i].getUnreadMessCount();
					gItem.setMessCount(unreadCounter);
				}
				tree.setExpandFlag(node, newExpanded);
			}
		}

		//#sijapp cond.if modules_DEBUGLOG is "true" #
		else if (c == debugListCommand)
		{
			DebugLog.activate();
		}
		//#sijapp cond.end#
	}
	
	static private boolean checkIfIdExists(int id)
	{
		for (int i = cItems.size()-1; i >= 0; i--)
		{
			ContactItem ci = getCItem(i);
			if (ci.getIntValue(ContactItem.CONTACTITEM_ID) == id) return true;
			if (ci.getIntValue(ContactItem.CONTACTITEM_INV_ID) == id) return true;
			if (ci.getIntValue(ContactItem.CONTACTITEM_VIS_ID) == id) return true;
			if (ci.getIntValue(ContactItem.CONTACTITEM_IGN_ID) == id) return true;
		}
		
		for (int i = gItems.size()-1; i >= 0; i--)
		{
			ContactListGroupItem gi = (ContactListGroupItem)gItems.elementAt(i);
			if (gi.getId() == id) return true;
		}
	
		return false;
	}
	
	static public int generateNewIdForBuddy()
	{
		int range = 0x6FFF;
		Random rand = new Random(System.currentTimeMillis());
		int randint = 0;
		
		do
		{
			randint = rand.nextInt();
			if (randint < 0) randint = randint * (-1);
			randint = randint % range + 4096;
		} 
		while ( checkIfIdExists(randint) );
		
		return randint;
	}

//#sijapp cond.if modules_ANTISPAM="true" #	
	static private final String ANTISPAM_RMS_NAME = "antispam_";
	static private final int ANTISPAM_RMS_VERS = 1;
	
	static private final Hashtable antiSpamList = new Hashtable();
	static private final Object antiSpamTrue = new Object();
	static private final Object antiSpamAnsSent = new Object();
	
	static public boolean antiSpamCheckContactFor(String uin, String message)
	{
		if (Options.getBoolean(Options.OPTION_ANTI_SPAM) == false) return true;
		
		Object state = antiSpamList.get(uin);
		
		if (state == antiSpamTrue) return true;
		
		if (state == antiSpamAnsSent)
		{
			if (message != null && Options.getString(Options.OPTION_ANTI_SPAM_ANS).equals(message.trim()))
			{
				antiSpamSendMess(uin, ResourceBundle.getString("antispam_good"));
				antiSpamList.put(uin, antiSpamTrue);
				antiSpamSaveList();
			}
			return false;
		}
		else
		{
			String messText = ResourceBundle.getString("antispam_mess")+Options.getString(Options.OPTION_ANTI_SPAM_QUESTION);
			boolean ok = antiSpamSendMess(uin, messText);
			if (ok) antiSpamList.put(uin, antiSpamAnsSent);
		}
		
		return false;
	}
	
	static private boolean antiSpamSendMess(String uin, String text)
	{
		ContactItem ci = new ContactItem();
		ci.setStringValue(ContactItem.CONTACTITEM_UIN, uin);
		PlainMessage plainMsg = new PlainMessage(Options.getString(Options.OPTION_UIN), ci, Message.MESSAGE_TYPE_NORM, Util.createCurrentDate(false), text);
		try
		{
			Icq.requestAction(new SendMessageAction(plainMsg));
		} catch (JimmException e)
		{
			return false;
		}
		return true;
	}
	
	static public void antiSpamSaveList()
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			
			dos.writeInt(ANTISPAM_RMS_VERS);
			
			int size = 0;
			Enumeration e = antiSpamList.keys();
			while (e.hasMoreElements())
				if (antiSpamList.get(e.nextElement()) == antiSpamTrue) size++;
			
			System.out.println("antiSpamSaveList, size="+size);
			dos.writeInt(size);
			
			e = antiSpamList.keys();
			while (e.hasMoreElements())
			{
				String uin = (String)e.nextElement();
				if (antiSpamList.get(uin) == antiSpamTrue) dos.writeUTF(uin);
			}
			
			RecordStore rs = RecordStore.openRecordStore(ANTISPAM_RMS_NAME, true);
			while (rs.getNumRecords() < 1) rs.addRecord(null, 0, 0);
			
			dos.flush();
			byte[] buf = baos.toByteArray();
			rs.setRecord(1, buf, 0, buf.length);

			dos.close();
			baos.close();
			rs.closeRecordStore();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	static public void antiSpamLoadList()
	{
		try
		{
			antiSpamList.clear();
			
			RecordStore account = RecordStore.openRecordStore(ANTISPAM_RMS_NAME, false);
			byte[] buf = account.getRecord(1);
			account.closeRecordStore();
			
			ByteArrayInputStream bais = new ByteArrayInputStream(buf);
			DataInputStream dis = new DataInputStream(bais);
			
			int vers = dis.readInt();
			
			if (vers != ANTISPAM_RMS_VERS) return;
			
			int count = dis.readInt();
			for (int i = 0; i < count; i++)
			{
				String uin = dis.readUTF();
				antiSpamList.put(uin, antiSpamTrue);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
//#sijapp cond.end#
	
}
