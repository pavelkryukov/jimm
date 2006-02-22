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
import jimm.comm.Util;
import jimm.comm.Icq;
import jimm.util.ResourceBundle;

import java.util.Hashtable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Date;

import javax.microedition.lcdui.*;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

//#sijapp cond.if target is "SIEMENS1"#
import com.siemens.mp.game.Vibrator;
import com.siemens.mp.game.Light;
import com.siemens.mp.media.Manager;
import com.siemens.mp.media.MediaException;
import com.siemens.mp.media.Player;
import com.siemens.mp.media.control.ToneControl;
import com.siemens.mp.media.control.VolumeControl;
import java.io.InputStream;
// #sijapp cond.end#

//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
import javax.microedition.media.PlayerListener;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.ToneControl;
import javax.microedition.media.control.VolumeControl;
import java.io.InputStream;
//#sijapp cond.end#

//#sijapp cond.if target is "RIM"#
//import net.rim.device.api.system.Alert;
import net.rim.device.api.system.LED;
//#sijapp cond.end#

import DrawControls.*;
import jimm.Options;

//////////////////////////////////////////////////////////////////////////////////
public class ContactList implements CommandListener, VirtualTreeCommands, VirtualListCommands
//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
                                    , PlayerListener
//#sijapp cond.end#
{
    // Status (all are mutual exclusive) TODO: move status to ContactListContactItem
    public static final int STATUS_AWAY      = 0x00000001;
    public static final int STATUS_CHAT      = 0x00000020;
    public static final int STATUS_DND       = 0x00000002;
    public static final int STATUS_INVISIBLE = 0x00000100;
	public static final int STATUS_INVIS_ALL = 0x00000200;
    public static final int STATUS_NA        = 0x00000004;
    public static final int STATUS_OCCUPIED  = 0x00000010;
    public static final int STATUS_OFFLINE   = 0xFFFFFFFF;
    public static final int STATUS_NONE      = 0x10000000;
    public static final int STATUS_ONLINE    = 0x00000000;

    // Sound notification typs
    public static final int SOUND_TYPE_MESSAGE = 1;
    public static final int SOUND_TYPE_ONLINE  = 2;

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

    public static boolean playerFree	= true;
    
    private static boolean needPlayOnlineNotif = false; 
    private static boolean needPlayMessNotif = false;
    private static ContactList _this;
    
	
    // Main menu command
    private static Command mainMenuCommand; 
    private static Command selectCommand;
    //#sijapp cond.if modules_DEBUGLOG is "true" #
    private static Command debugListCommand = new Command("*Debug list*", Command.ITEM, 2);
    //#sijapp cond.end#
    

    /** ************************************************************************* */

    // Version id numbers
    static private int ssiListLastChangeTime = -1;
    static private int ssiNumberOfItems = 0;

    // Update help variable
    private static boolean haveToBeCleared;

    // Contact items
    private static Vector cItems;

    // Group items
    private static Vector gItems;
    
    private static boolean treeBuilt = false, treeSorted = false;
	
    // Contains tree nodes by groip ids
	private static Hashtable gNodes = new Hashtable();
	
	// Tree object
	private static VirtualTree tree;

	// Images for icons
	final public static ImageList imageList;
	final public static ImageList smallIcons;

	//
	private static int onlineCounter;

    // Initializer
    static
    {
        //#sijapp cond.if target is "MOTOROLA" #
        mainMenuCommand    = new Command(ResourceBundle.getString("menu_button"),  Command.SCREEN, 3);
        //#sijapp cond.else #    
        mainMenuCommand    = new Command(ResourceBundle.getString("menu"),         Command.SCREEN, 3);
        //#sijapp cond.end #
        
        selectCommand = new Command(ResourceBundle.getString("select"), Command.OK, 1);
    	
        // Construct image objects
        smallIcons = new ImageList();
        imageList = new ImageList();
        try
        {
        	// reads and divides image "icons.png" to several icons
			imageList.load("/icons.png", -1, -1, -1);
            ContactList.statusAwayImg        = imageList.elementAt(0);
            ContactList.statusChatImg        = imageList.elementAt(1);
            ContactList.statusDndImg         = imageList.elementAt(2);
            ContactList.statusInvisibleImg   = imageList.elementAt(3);
            ContactList.statusNaImg          = imageList.elementAt(4);
            ContactList.statusOccupiedImg    = imageList.elementAt(5);
            ContactList.statusOfflineImg     = imageList.elementAt(6);
            ContactList.statusOnlineImg      = imageList.elementAt(7);
            ContactList.eventPlainMessageImg = imageList.elementAt(8);
            ContactList.eventUrlMessageImg   = imageList.elementAt(9);
            ContactList.eventSystemNoticeImg = imageList.elementAt(10);
            ContactList.eventSysActionImg    = imageList.elementAt(11);
            smallIcons.load("/sicons.png", -1, -1, -1);
        } 
        catch (IOException e)
        {
            // Do nothing
        }
    }

    // Constructor
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
		
     
		tree = new VirtualTree(null, false);
		tree.setVTCommands(this);
		tree.setVLCommands(this);
		
        // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
		tree.setFullScreenMode(false);
        // #sijapp cond.end#
		
		tree.setImageList(imageList);
		tree.setFontSize((imageList.getHeight() < 16) ? VirtualList.SMALL_FONT : VirtualList.MEDIUM_FONT);
		tree.setStepSize( -tree.getFontHeight()/2 );
		
        // #sijapp cond.if modules_TRAFFIC is "true" #
		updateTitle(Traffic.getSessionTraffic(true));
        // #sijapp cond.else #
        updateTitle(0);
        // #sijapp cond.end#
        tree.addCommand(ContactList.mainMenuCommand);
		tree.addCommand(selectCommand);
	
        // #sijapp cond.if modules_DEBUGLOG is "true" #
		tree.addCommand(debugListCommand);
        // #sijapp cond.end#
		
        tree.setCommandListener(this);
    }
    
    ///////////////////////////////////////////////////////////////////////////
	final static public int SORT_BY_NAME   = 1;
	final static public int SORT_BY_STATUS = 0;
	static private int sortType;
	
	static int getNodeWeight(TreeNode node)
	{
		ContactListContactItem cItem;
		Object obj;

		obj = node.getData();
		if ( !(obj instanceof ContactListContactItem) ) return 10;
		cItem = (ContactListContactItem)obj;
		if (cItem.getIntValue(ContactListContactItem.CONTACTITEM_STATUS) != ContactList.STATUS_OFFLINE) return 0;
		if (cItem.getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP) && 
			cItem.getIntValue(ContactListContactItem.CONTACTITEM_STATUS) == ContactList.STATUS_OFFLINE) return 20;
	
		return 10;
	}

	public int compareNodes(TreeNode node1, TreeNode node2)
	{
		ContactListContactItem item1, item2;
		Object obj1, obj2;
		int result = 0;
	
		obj1 = node1.getData();
		obj2 = node2.getData();
		
		item1 = (ContactListContactItem)obj1;
		item2 = (ContactListContactItem)obj2;
		
		switch (sortType)
		{
		case SORT_BY_NAME: 
			result = item1.getLowerText().compareTo( item2.getLowerText() );
			break;
		case SORT_BY_STATUS:
			int weight1 = getNodeWeight(node1);
			int weight2 = getNodeWeight(node2);
			if (weight1 == weight2) result = item1.getLowerText().compareTo( item2.getLowerText() );		
			else result = (weight1 < weight2) ? -1 : 1; 
			break;
		}
		
		return result;
	}
	///////////////////////////////////////////////////////////////////////////
    
    // Returns reference to tree 
    static public Displayable getVisibleContactListRef()
    {
        return tree;
    }
	
	// Returns image list with status icons and status icons with red letter "C"  
    static public ImageList getImageList()
	{
		return imageList;
	}

    // Returns the id number #1 which identifies (together with id number #2)
    // the saved contact list version
	static public int getSsiListLastChangeTime()
    {
        return ssiListLastChangeTime;
    }

    // Returns the id number #2 which identifies (together with id number #1)
    // the saved contact list version
	static public int getSsiNumberOfItems()
    {
        return (ssiNumberOfItems);
    }
    
    // Returns number of contact items
    static public int getSize()
    {
    	return cItems.size();
    }
    
    static private ContactListContactItem getCItem(int index)
    {
    	return (ContactListContactItem)cItems.elementAt(index);
    }
    
    // Returns all contact items as array
    static public synchronized ContactListContactItem[] getContactItems()
    {
        ContactListContactItem[] cItems_ = new ContactListContactItem[cItems.size()];
        ContactList.cItems.copyInto(cItems_);
        return (cItems_);
    }
    
    // Returns all group items as array
    static public synchronized ContactListGroupItem[] getGroupItems()
    {
        ContactListGroupItem[] gItems_ = new ContactListGroupItem[gItems.size()];
        ContactList.gItems.copyInto(gItems_);
        return (gItems_);
    }

    // Request display of the given alert and the main menu afterwards
    static public void activate(Alert alert)
    {
        Jimm.display.setCurrent(alert, ContactList.tree);
        //#sijapp cond.if target is "MOTOROLA"#
        LightControl.flash(false);
        //#sijapp cond.end#
    }

    // Request display of the main menu
    static public void activate()
	{
    	tree.setCapImage(smallIcons.elementAt(JimmUI.getStatusImageIndex(Jimm.jimm.getIcqRef().getCurrentStatus())));
		//#sijapp cond.if modules_TRAFFIC is "true" #
		updateTitle(Traffic.getSessionTraffic(true));
		//#sijapp cond.else #
		updateTitle(0);
		//#sijapp cond.end#
		// show contact list
		tree.lock();
		buildTree();
		sortAll();
		tree.unlock();
		Jimm.display.setCurrent(ContactList.tree);
		//#sijapp cond.if target is "MOTOROLA"#
		LightControl.flash(false);
		//#sijapp cond.end#

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
	}
    
    // is called by options form when options changed
    static public void optionsChanged(boolean needToRebuildTree, boolean needToSortContacts)
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
        if (!exist) throw (new Exception());
        
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
            if (!(dis.readUTF().equals(Jimm.VERSION))) throw (new IOException());

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
                    	
                        // Instantiate ContactListContactItem object and add to vector
                        ContactListContactItem ci = new ContactListContactItem();
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
            System.out.println("\n clload mem used: "+(mem-Runtime.getRuntime().freeMemory()));
            //#sijapp cond.end#
		}
        finally
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
        dos.writeShort((short)ssiNumberOfItems);
        buf = baos.toByteArray();
        cl.addRecord(buf, 0, buf.length);

        // Initialize buffer
        baos.reset();

        // Iterate through all contact items
        int cItemsCount = cItems.size();
        int totalCount = cItemsCount+gItems.size();
        for (int i = 0; i < totalCount; i++)
        {
        	if (i < cItemsCount) getCItem(i).saveToStream(dos);
        	else
        	{
        		ContactListGroupItem gItem = (ContactListGroupItem)gItems.elementAt(i-cItemsCount);
        		gItem.saveToStream(dos);
        	}
        	
            // Start new record if it exceeds 4000 bytes
            if ((baos.size() >= 4000) || (i == totalCount-1))
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
    static protected void beforeConnect()
    {
    	treeBuilt = treeSorted = false;
    	haveToBeCleared = true;
    	tree.clear();
		setStatusesOffline();
    }
    
    static public void setStatusesOffline()
    {
    	onlineCounter = 0;
    	for (int i = cItems.size()-1; i >= 0; i--)
    		getCItem(i).setIntValue(ContactListContactItem.CONTACTITEM_STATUS,ContactList.STATUS_OFFLINE);
    	
    	for (int i = gItems.size()-1; i >= 0; i--)
    		((ContactListGroupItem)gItems.elementAt(i)).setCounters(0, 0);
    }
    
	// Returns array of uins of unuthorized and temporary contacts
	public static String[] getUnauthAndTempContacts() 
	{
		Vector data = new Vector(); 
		for (int i = cItems.size()-1; i >= 0; i--)
		{
			if (getCItem(i).getBooleanValue(ContactListContactItem.CONTACTITEM_NO_AUTH) ||
					getCItem(i).getBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP))
				data.addElement( getCItem(i).getStringValue(ContactListContactItem.CONTACTITEM_UIN) );
		}
		String result[] = new String[data.size()];
		data.copyInto(result);
		return result;
	}
    
    // Updates the client-side conact list (called when a new roster has been
    // received)
    static public synchronized void update(int flags, int versionId1_, int versionId2_, ContactListItem[] items)
    {
    	//#log("some_debug_text")
    	
    	//#sijapp cond.if modules_DEBUGLOG is "true"#
    	System.out.println("New roster. versionId1_="+ssiListLastChangeTime+", versionId2="+versionId2_+", flags="+flags);
    	System.out.println("Old versionId1="+ssiListLastChangeTime+", versionId2="+ssiNumberOfItems+", updated="+haveToBeCleared);
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
            if (items[i] instanceof ContactListContactItem)
            {
            	cItems.addElement(items[i]);
            } else if (items[i] instanceof ContactListGroupItem)
            {
            	gItems.addElement(items[i]);
            }
        }
        ssiNumberOfItems += versionId2_;
        
        // Save new contact list
        if (versionId1_ != 0)
        {
        	ssiListLastChangeTime = versionId1_;
        	safeSave();
        	treeBuilt = false;
        	
        	// Which contacts already have chats?
        	for (int i = getSize()-1; i >= 0; i--)
        	{
        		ContactListContactItem cItem = getCItem(i); 
        		cItem.setBooleanValue
        		(
        			ContactListContactItem.CONTACTITEM_HAS_CHAT,
        			ChatHistory.chatHistoryExists(cItem.getStringValue(ContactListContactItem.CONTACTITEM_UIN))
        		);
        	}
        }	
    }
    
    public static void safeSave()
    {
    	try
    	{
    		save();
    	}
    	catch (Exception e) 
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
            for (int i = 0; i < gItems.size(); i++)
    		{
    		    ContactListGroupItem gItem = (ContactListGroupItem)gItems.elementAt(i);
    		    TreeNode groupNode = (TreeNode)gNodes.get( new Integer(gItem.getId()) );
    		    tree.sortNode(groupNode);
    		    calcGroupData(groupNode, gItem);
    		}
    	}
    	else tree.sortNode(null);
    	treeSorted = true;
    }
    
    // Builds contacts tree (without sorting) 
    static private void buildTree()
	{
	    int i, gCount, cCount;
	    boolean use_groups  = Options.getBoolean(Options.OPTION_USER_GROUPS),
		        only_online = Options.getBoolean(Options.OPTION_CL_HIDE_OFFLINE);
			    
		cCount = cItems.size();
		if (treeBuilt || (cCount == 0)) return;
		
		tree.clear();
		tree.setShowButtons(use_groups);
		
		// add group nodes
		gNodes.clear();
		
		if (use_groups)
		{
			gCount = gItems.size();
			for (i = 0; i < gCount; i++)
			{
				ContactListGroupItem item = (ContactListGroupItem)gItems.elementAt(i);
				TreeNode groupNode = tree.addNode(null, item);
				gNodes.put(new Integer(item.getId()), groupNode);
			}
		}
		
		// add contacts
		for (i = 0; i < cCount; i++)
		{
			ContactListContactItem cItem = getCItem(i);
			
			if (only_online && 
			    (cItem.getIntValue(ContactListContactItem.CONTACTITEM_STATUS) == STATUS_OFFLINE) &&
				 !cItem.mustBeShownAnyWay()) continue;
			
			if (use_groups)
			{
			    ContactListGroupItem group = getGroupById( cItem.getIntValue(ContactListContactItem.CONTACTITEM_GROUP) );
		  	    TreeNode groupNode = (TreeNode)gNodes.get( new Integer( cItem.getIntValue(ContactListContactItem.CONTACTITEM_GROUP) ) );
		  		tree.addNode(groupNode, cItem);
			}
			else
			{
				tree.addNode(null, cItem);
			}
		}    
	
		treeSorted = false;
		treeBuilt = true;
	}

	// Returns reference to group with id or null if group not found
	private static ContactListGroupItem getGroupById(int id)
	{
		for (int i = gItems.size()-1; i >= 0; i--)
		{
			ContactListGroupItem group = (ContactListGroupItem) gItems.elementAt(i);
			if (group.getId() == id) return group;
		}
		return null;
	}
   
	// Returns reference to contact item with uin or null if not found  
	static public ContactListContactItem getItembyUIN(String uin)
    {
		int uinInt = Integer.parseInt(uin);
    	for (int i = cItems.size()-1; i >= 0; i--)
    	{
    		ContactListContactItem citem = getCItem(i); 
    	    if (citem.getUIN() == uinInt) return citem;
    	}
    	return null;
    }
    
    // Calculates online/total values for group
    static private void calcGroupData(TreeNode groupNode, ContactListGroupItem group)
    {
        if ((group == null) || (groupNode == null)) return;
        
        ContactListContactItem cItem;
        int onlineCount = 0;
        
        int count = groupNode.size();
        for (int i = 0; i < count; i++)
        {
        	if (!(groupNode.elementAt(i).getData() instanceof ContactListContactItem)) continue; // TODO: must be removed
            cItem = (ContactListContactItem)groupNode.elementAt(i).getData();
            if (cItem.getIntValue(ContactListContactItem.CONTACTITEM_STATUS) != STATUS_OFFLINE) onlineCount++;
        }
        group.setCounters(onlineCount, count);
    }
    
    // Must be called after any changes in contacts
    public static void contactChanged
    (
    	ContactListContactItem item, 
		boolean setCurrent,
		boolean needSorting
    )
    {
    	if (!treeBuilt) return;
    	
    	boolean contactExistInTree = false,
		        contactExistsInList,
				wasDeleted = false,
				haveToAdd = false,
				haveToDelete = false;
    	TreeNode cItemNode = null;
    	int i, count, groupId;
    	
    	int status = item.getIntValue(ContactListContactItem.CONTACTITEM_STATUS);
    	
    	String uin = item.getStringValue(ContactListContactItem.CONTACTITEM_UIN);
    	
    	// which group id ?
    	groupId = item.getIntValue(ContactListContactItem.CONTACTITEM_GROUP);
    	
	    // which group ?
	    ContactListGroupItem group = getGroupById(groupId);
	    
		boolean only_online = Options.getBoolean(Options.OPTION_CL_HIDE_OFFLINE);
    	
    	// Whitch group node?
    	TreeNode groupNode = (TreeNode)gNodes.get( new Integer(groupId) );
    	if (groupNode == null) groupNode = tree.getRoot();
    	
    	// Does contact exists in tree?
  		count = groupNode.size();
   		for (i = 0; i < count; i++)
   		{
   			cItemNode = groupNode.elementAt(i);
   			Object data = cItemNode.getData();
   			if ( !(data instanceof ContactListContactItem) ) continue; 
   			if ( !((ContactListContactItem)data).getStringValue(ContactListContactItem.CONTACTITEM_UIN).equals(uin) ) continue;
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
    		haveToDelete |= ((status == STATUS_OFFLINE) && !item.mustBeShownAnyWay());
    	
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
    		boolean isCurrent = (tree.getCurrentItem() == cItemNode),
			        inserted = false;
    		
    		tree.deleteChild( groupNode, tree.getIndexOfChild(groupNode, cItemNode) );
    		
    		int contCount = groupNode.size();
    		sortType = Options.getInt(Options.OPTION_CL_SORT_BY);
    		
    		// TODO: Make binary search instead of linear before child insertion!!!
    		for (int j = 0; j < contCount; j++)
    		{
    			TreeNode testNode = groupNode.elementAt(j);
    			if ( !(testNode.getData() instanceof ContactListContactItem) ) continue;
    			if (_this.compareNodes(cItemNode, testNode) < 0)
    			{
    				tree.insertChild(groupNode, cItemNode, j);
    				inserted = true;
    				break;
    			}
    		}
    		if (!inserted) tree.insertChild(groupNode, cItemNode, contCount);
    		if (isCurrent) tree.setCurrentItem(cItemNode);
    	}
    	
    	// if set current
    	if (setCurrent) tree.setCurrentItem(cItemNode);
    	
    	// unlock tree and repaint
    	tree.unlock();
    	
    	// change status for chat (if exists)
    	item.setStatusImage();
    }
    
    private static int lastUnknownStatus = STATUS_NONE;
    
    // Updates the client-side contact list (called when a contact changes status)
    // DO NOT CALL THIS DIRECTLY FROM OTHER THREAND THAN MAIN!
    // USE RunnableImpl.updateContactList INSTEAD!
    static public synchronized void update
    (
    	String uin,
    	int status,
    	byte[] internalIP,
    	byte[] externalIP,
    	int dcPort,
    	int dcType,
    	int icqProt,
        int authCookie,
        int signon,
        int online,
        int idle
    )
    {
        ContactListContactItem cItem = getItembyUIN(uin);
        
        int trueStatus = Util.translateStatusReceived(status);
        
        if (cItem == null)
        {
        	lastUnknownStatus = trueStatus;
        	return;
        }
        
        long oldStatus = cItem.getIntValue(ContactListContactItem.CONTACTITEM_STATUS);
    	
    	boolean statusChanged  = (oldStatus != trueStatus);
        boolean wasOnline  = (oldStatus != STATUS_OFFLINE);
        boolean nowOnline  = (trueStatus != STATUS_OFFLINE);
        
        if (status == STATUS_OFFLINE) cItem.setIntValue(ContactListContactItem.CONTACTITEM_CAPABILITIES, 0);
        
        // Online counters
        statusChanged(cItem, wasOnline, nowOnline, 0);
        
        // Set Status
        cItem.setIntValue(ContactListContactItem.CONTACTITEM_STATUS, trueStatus);
        
        if (treeBuilt && statusChanged) ContactListContactItem.statusChanged(uin, trueStatus);

        // Update DC values
        //#sijapp cond.if (target="MIDP2" | target="MOTOROLA" | target="SIEMENS2") & modules_FILES="true"#
        if (dcType != -1)
        {
            cItem.setIPValue  (ContactListContactItem.CONTACTITEM_INTERNAL_IP, internalIP);
            cItem.setIPValue  (ContactListContactItem.CONTACTITEM_EXTERNAL_IP, externalIP); 
            cItem.setIntValue (ContactListContactItem.CONTACTITEM_DC_PORT,     (int)dcPort);
            cItem.setIntValue (ContactListContactItem.CONTACTITEM_DC_TYPE,     dcType);
            cItem.setIntValue (ContactListContactItem.CONTACTITEM_ICQ_PROT,    icqProt);
            cItem.setIntValue(ContactListContactItem.CONTACTITEM_AUTH_COOKIE, authCookie);
        }
        //#sijapp cond.end#
        
        // Update time values
		cItem.setIntValue (ContactListContactItem.CONTACTITEM_SIGNON,signon);
		cItem.setIntValue (ContactListContactItem.CONTACTITEM_ONLINE,online);
		cItem.setIntValue (ContactListContactItem.CONTACTITEM_IDLE,idle);

        // Play sound notice if selected
        if ((trueStatus == STATUS_ONLINE) && statusChanged)
        {
            if ( treeBuilt ) playSoundNotification(SOUND_TYPE_ONLINE);
            else needPlayOnlineNotif |= true;
        }
        
        // Update visual list
        if (statusChanged) contactChanged(cItem, false, (wasOnline && !nowOnline) || (!wasOnline && nowOnline)); 
    }

    // Updates the client-side contact list (called when a contact changes status)
    static public synchronized void update(String uin, int status)
    {
        update(uin, status, null, null, 0,0,-1,0,-1,-1,-1);
    }
    
    static private void statusChanged(ContactListContactItem cItem, boolean wasOnline, boolean nowOnline, int tolalChanges)
    {
    	boolean changed = false;
    	
    	// which group id ?
    	int groupId = cItem.getIntValue(ContactListContactItem.CONTACTITEM_GROUP);
    	
	    // which group ?
	    ContactListGroupItem group = getGroupById(groupId);
        
        // Calc online counters
        if (wasOnline && !nowOnline)
        {
        	onlineCounter--;
        	if (group != null) group.updateCounters(-1, 0);
        	changed = true;
        }
        
        if (!wasOnline && nowOnline)
        {
        	onlineCounter++;
        	if (group != null) group.updateCounters(1, 0);
        	changed = true;
        }
        
        if (group != null)
        {
        	group.updateCounters(0, tolalChanges);
        	changed |= (tolalChanges != 0);
        }	
        
        if (changed) RunnableImpl.updateContactListCaption();
    }
    
    //Updates the title of the list
	static public void updateTitle(int traffic)
	{
		String text = onlineCounter + "/" + cItems.size();
		String sep = " - ";
		if (traffic != 0)
		{
			if (text.length() > 4) sep = "-";
			text += sep + traffic + ResourceBundle.getString("kb") + sep + Util.getDateString(true);
		}
		else
		{
			text += sep + Util.getDateString(true);
		}

		tree.setCaption(text);
	}

	// Removes a contact list item
	static public synchronized void removeContactItem(
		ContactListContactItem cItem)
	{
		// Remove given contact item
		ContactList.cItems.removeElement(cItem);

		// Update visual list
		contactChanged(cItem, false, false);

		// Update online counters
		statusChanged(cItem, cItem.getIntValue(ContactListContactItem.CONTACTITEM_STATUS) != STATUS_OFFLINE, false, -1);

		// Save list
		safeSave();
	}

	// Adds a contact list item
	static public synchronized void addContactItem(ContactListContactItem cItem)
	{
		if (!cItem.getBooleanValue(ContactListContactItem.CONTACTITEM_ADDED))
		{
			// does contact already exists or temporary ?
			ContactListContactItem oldItem = getItembyUIN(cItem.getStringValue(ContactListContactItem.CONTACTITEM_UIN));
			if (oldItem != null)
			{
				removeContactItem(oldItem);
				lastUnknownStatus = oldItem.getIntValue(ContactListContactItem.CONTACTITEM_STATUS);
			}

			// Add given contact item
			cItems.addElement(cItem);
			cItem.setBooleanValue(ContactListContactItem.CONTACTITEM_ADDED, true);

			// Check is chat availible 
    		cItem.setBooleanValue
    		(
    			ContactListContactItem.CONTACTITEM_HAS_CHAT,
    			ChatHistory.chatHistoryExists(cItem.getStringValue(ContactListContactItem.CONTACTITEM_UIN))
    		);
    		
    		// Set contact status (if already received)
    		if (lastUnknownStatus != STATUS_NONE)
    		{
    			cItem.setIntValue(ContactListContactItem.CONTACTITEM_STATUS, lastUnknownStatus);
    			lastUnknownStatus = STATUS_NONE;
    		}
    		
    		// Request contact status
    		else
    		{
    			Icq.addLocalContacts(new String[] { cItem.getStringValue(ContactListContactItem.CONTACTITEM_UIN) } );
    		}
    		
			// Update visual list
			contactChanged(cItem, true, true);
    		
			// Update online counters
			statusChanged(cItem, false, cItem.getIntValue(ContactListContactItem.CONTACTITEM_STATUS) != STATUS_OFFLINE, 1);

			// Save list
			safeSave();
		}
	}

	// Adds new group
	static public synchronized void addGroup(ContactListGroupItem gItem)
	{
		gItems.addElement(gItem);
		if (!Options.getBoolean(Options.OPTION_USER_GROUPS)) return;
		TreeNode groupNode = tree.addNode(null, gItem);
		gNodes.put(new Integer(gItem.getId()), groupNode);
		safeSave();
	}

	// removes existing group 
	static public synchronized void removeGroup(ContactListGroupItem gItem)
	{
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

    // Adds the given message to the message queue of the contact item
    // identified by the given UIN
    static public synchronized void addMessage(Message message)
    {
        ContactListContactItem cItem = getItembyUIN(message.getSndrUin());
        
        boolean temp = false;
        
        // Add message to contact
        if (cItem != null) cItem.addMessage(message);
        
        // Create a temporary contact entry if no contact entry could be found
        // do we have a new temp contact
        else
        {
        	try
        	{
        		cItem = new ContactListContactItem(0, 0, message.getSndrUin(), message.getSndrUin(), false, true);
        	}
        	catch (Exception e)
        	{
        		// Message from non-icq contact
        		return;
        	}
            cItems.addElement(cItem);
            cItem.setBooleanValue(ContactListContactItem.CONTACTITEM_IS_TEMP,true);
            cItem.addMessage(message);
            temp = true;
            Icq.addLocalContacts(new String[] { message.getSndrUin() });
        }

        // Notify splash canvas
        SplashCanvas.messageAvailable();
        
        // Notify user
        if ( !treeBuilt ) needPlayMessNotif |= true;
        else playSoundNotification(SOUND_TYPE_MESSAGE);
        
        cItem.setBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT,true);
        
        // Update tree
        contactChanged(cItem, true, false);
    }

    //#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#    
    
    // Reaction to player events. (Thanks to Alexander Barannik for idea!)
    public void playerUpdate(final Player player, final String event, Object eventData)
    {
    	// queue a call to updateEvent in the user interface event queue
    	RunnableImpl.callSerially(RunnableImpl.TYPE_CLOSE_PLAYER, player, event);
    	playerFree = true;
    }

	// Creates player for file 'source'
    static private Player createPlayer(String source)
	{
		String ext, mediaType;
		Player p;
		
		// What is file extention?
		int point = source.lastIndexOf('.');
		if (point != -1) ext = source.substring(point+1, source.length()).toLowerCase();
		else ext = "wav";
		
		// What is media type?
		if (ext.equals("mp3")) mediaType = "audio/mpeg";
		else if (ext.equals("mid") || ext.equals("midi")) mediaType = "audio/midi";
		else if (ext.equals("amr"))  mediaType = "audio/amr";
		else mediaType = "audio/X-wav";
	
		try
		{
			InputStream is = _this.getClass().getResourceAsStream(source);
			if (is == null) is = _this.getClass().getResourceAsStream("/"+source);
			if (is == null) return null;
			if (playerFree)
			{
				p = Manager.createPlayer(is, mediaType);
				playerFree = false;
				p.addPlayerListener(_this);
			}
			else
			return null;
		
		}
		catch (Exception e)
		{
			return null;
		}
		return p;
	}
	
	//#sijapp cond.end#
	
	//#sijapp cond.if target is"SIEMENS1"#
	static private Player createPlayer(String source)
	{
		Player p;
		
		try
		{
			p = Manager.createPlayer(source);
		}
		catch (Exception e)
		{
			return null;
		}
		return p;
	}
	//#sijapp cond.end#
	
	
	//#sijapp cond.if target is "MIDP2" | target is"SIEMENS1" | target is "MOTOROLA" | target is "SIEMENS2"#
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
		}
		catch (Exception e)
		{
		}
	}
	
	//#sijapp cond.end#

    // Play a sound notification
    synchronized static private void playSoundNotification(int notType)
    {
    	if (!treeBuilt) return;
    	
        // #sijapp cond.if target is "SIEMENS1" | target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        
        // #sijapp cond.if target is "SIEMENS1"#
        Light.setLightOn();
        // #sijapp cond.end#
        
        int vibraKind = Options.getInt(Options.OPTION_VIBRATOR);
        if(vibraKind == 2) vibraKind = SplashCanvas.locked()?1:0;
        if ((vibraKind > 0) && (notType == SOUND_TYPE_MESSAGE))
        {
            // #sijapp cond.if target is "SIEMENS1"#
            Vibrator.triggerVibrator(500);
            // #sijapp cond.else#
            Jimm.display.vibrate(500);
            // #sijapp cond.end#
        }
        
        int not_mode = 0;
        
        switch (notType)
		{
		case SOUND_TYPE_MESSAGE:
			not_mode = Options.getInt(Options.OPTION_MESS_NOTIF_MODE);
			break;
			
		case SOUND_TYPE_ONLINE:
			not_mode = Options.getInt(Options.OPTION_ONLINE_NOTIF_MODE);
			break;
		}
            
        switch (not_mode)
        {
        case 1:
            try
            {
                switch(notType)
                {
                case SOUND_TYPE_MESSAGE:
                    Manager.playTone(ToneControl.C4, 500, Options.getInt(Options.OPTION_MESS_NOTIF_VOL));
                    break;
                case SOUND_TYPE_ONLINE:
                    Manager.playTone(ToneControl.C4+7, 500, Options.getInt(Options.OPTION_ONLINE_NOTIF_VOL));
                }

            } catch (MediaException e)
            {
                // Do nothing
            }
            break;
        case 2:
            try
            {
                Player p;
                
                if (notType == SOUND_TYPE_MESSAGE)
                {
                	p = createPlayer( Options.getString(Options.OPTION_MESS_NOTIF_FILE) );
                	if (p == null) return;
                    setVolume(p, Options.getInt(Options.OPTION_MESS_NOTIF_VOL));
                }
                else
                {
                	p = createPlayer( Options.getString(Options.OPTION_ONLINE_NOTIF_FILE) );
                	if (p == null) return;
                    setVolume(p, Options.getInt(Options.OPTION_ONLINE_NOTIF_VOL)); 
                }
                
                p.start();
            }
            catch (Exception me)
            {
                // Do nothing
            }

            break;

        }
        // #sijapp cond.if target is "SIEMENS1"#
        Light.setLightOff();
        // #sijapp cond.end#
        
        // #sijapp cond.end#
        
        // #sijapp cond.if target is "RIM"#
        if (Options.getBoolean(Options.OPTION_VIBRATOR))
        {
						// had to use full path since import already contains another Alert object
            net.rim.device.api.system.Alert.startVibrate(500);
        }
        int mode_rim;
        if (notType == SOUND_TYPE_MESSAGE)
            mode_rim = Options.getInt(Options.OPTION_MESS_NOTIF_MODE);
        else
            mode_rim = Options.getInt(Options.OPTION_ONLINE_NOTIF_MODE);
        switch (mode_rim)
        {
        case 1:
            // array is note in Hz, duration in ms.
			short[] tune = new short[] { 349, 250, 0, 10, 523, 250 };
            net.rim.device.api.system.Alert.startAudio(tune, 50);
            net.rim.device.api.system.Alert.startBuzzer(tune, 50);
            break;
				}
        // net.rim.device.api.system.Alert.stopAudio();
        // net.rim.device.api.system.Alert.stopBuzzer();
        // #sijapp cond.end#

    }
    
    static ContactListContactItem lastChatItem = null;
    
    public void VTGetItemDrawData(TreeNode src, ListItem dst)
	{
		ContactListItem item = (ContactListItem)src.getData();
		dst.text       = item.getText();
		dst.imageIndex = item.getImageIndex();
		dst.color      = item.getTextColor();
		dst.fontStyle  = item.getFontStyle(); 
	}
	
	public void VTnodeClicked(TreeNode node)
	{
		if (node == null) return;
		ContactListItem item = (ContactListItem)node.getData();
		if (item instanceof ContactListContactItem)
		{
			// Activate the contact item menu
			//#sijapp cond.if target is "RIM"#
			LED.setState(LED.STATE_OFF);
			//#sijapp cond.end#
			
			lastChatItem = (ContactListContactItem)item; 
			lastChatItem.activate(true);
		}
		else if (item instanceof ContactListGroupItem)
		{
			tree.setExpandFlag(node, !node.getExpanded());
		}
	}
	
	public void onCursorMove(VirtualList sender) {}
	public void onItemSelected(VirtualList sender) {}
	public void onKeyPress(VirtualList sender, int keyCode,int type)
	{
		TreeNode node = tree.getCurrentItem();
		ContactListContactItem item = 
			((node != null) && (node.getData() instanceof ContactListContactItem))
				?
			(ContactListContactItem) node.getData()
				:
			null;
		JimmUI.execHotKey(item, keyCode, type);
	}

	// shows next or previos chat 
	static synchronized protected String showNextPrevChat(boolean next)
	{
		int index = cItems.indexOf(lastChatItem);
		if (index == -1) return null;
		int di = next ? 1 : -1;
		int maxSize = cItems.size();
		
		for (int i = index+di;; i += di)
		{
			if (i < 0) i = maxSize-1;
			if (i >= maxSize) i = 0;
			if (i == index) break;
			
			ContactListContactItem cItem = getCItem(i); 
			if ( cItem.getBooleanValue(ContactListContactItem.CONTACTITEM_HAS_CHAT) )
			{
				lastChatItem = cItem;
				cItem.activate(false);
				return cItem.getStringValue(ContactListContactItem.CONTACTITEM_UIN);
			}
		}
		return null;
	}
	
	// Returns number of unread messages 
	static protected int getUnreadMessCount()
	{
		int count = cItems.size();
		int result = 0;
		for (int i = 0; i < count; i++) result += getCItem(i).getUnreadMessCount();
		return result;
	}
	
    // Command listener
    public void commandAction(Command c, Displayable d)
    {
        // Activate main menu
        if (c == mainMenuCommand)
        {
            MainMenu.activate();
        }
        
        // Contact item has been selected
        else if (c == selectCommand)
        {
        	VTnodeClicked(tree.getCurrentItem());
        }
        
//#sijapp cond.if modules_DEBUGLOG is "true" #
        else if (c == debugListCommand)
        {
            DebugLog.activate();
        }
//#sijapp cond.end#
    }

}

