/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-04  Jimm Project

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
import jimm.util.ResourceBundle;

import java.util.Hashtable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.*;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

//#sijapp cond.if target is "SIEMENS"#
import com.siemens.mp.game.Vibrator;
import com.siemens.mp.game.Light;
import com.siemens.mp.media.Manager;
import com.siemens.mp.media.MediaException;
import com.siemens.mp.media.Player;
import com.siemens.mp.media.control.ToneControl;
import com.siemens.mp.media.control.VolumeControl;
import java.io.InputStream;
// #sijapp cond.end#

//#sijapp cond.if target is "MIDP2"#
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

/*
abstract class UserManagementBase implements CommandListener
{
    private static Form 
		form = new Form(null);

    private static Command 
    	sendCommand = new Command(ResourceBundle.getString("exec"), Command.ITEM, 1),
    	backCommand = new Command(ResourceBundle.getString("back"), Command.BACK, 2);
    
    static
    {
        form.addCommand(sendCommand);
        form.addCommand(backCommand);
    }    
    
    protected abstract void select();
    protected abstract void addControls(Form form);
    
    protected static void activateContactList()
    {
        Jimm.jimm.getContactListRef().activate();
    }  
    
    public void commandAction(Command c, Displayable d)
    {
        if (c == backCommand) activateContactList();
        else if (c == sendCommand) select();
    }
    
    UserManagementBase(String title)
    {
        form.setTitle(title);
    }
    
    void go()
    {
        while (form.size() != 0) form.delete(0);
        System.gc();
        addControls(form);
        form.setCommandListener(this);
        Jimm.display.setCurrent(form);
    }
}

////////////////////////////////////////////////////////////////////////////////////

// Class for adding new user
class AddUserForm extends UserManagementBase
{
    private TextField 
    	uinTextField = new TextField(ResourceBundle.getString("uin"), "", 16, TextField.NUMERIC),
    	nameTextField = new TextField(ResourceBundle.getString("name"), "", 32, TextField.ANY);

    protected void select()
    {
        // Display splash canvas
        SplashCanvas wait2 = Jimm.jimm.getSplashCanvasRef();
        wait2.setMessage(ResourceBundle.getString("wait"));
        wait2.setProgress(0);
        Jimm.display.setCurrent(wait2);

        Search search = new Search();
        search.setSearchRequest(uinTextField.getString(), "", "", "", "", "", "", false);
        SearchAction act = new SearchAction(search, SearchAction.CALLED_BY_ADDUSER);

        try
        {
            Jimm.jimm.getIcqRef().requestAction(act);

        } catch (JimmException e)
        {
            JimmException.handleException(e);
            if (e.isCritical()) return;
        }

        // Start timer
        Jimm.jimm.getTimerRef().schedule(new SplashCanvas.SearchTimerTask(act), 1000, 1000);
    }
    
    protected void addControls(Form form)
    {
        form.append(uinTextField);
        form.append(nameTextField);
    }
    
    AddUserForm()
    {
        super(ResourceBundle.getString("add_user"));
    }
}

//////////////////////////////////////////////////////////////////////////////////

// Class for adding new group
class AddGroupForm extends UserManagementBase
{
    private TextField 
		uinTextField = new TextField(ResourceBundle.getString("group_name"), "", 32, TextField.ANY);
   
    protected void select()
    {
    }
    
    protected void addControls(Form form)
    {
        form.append(uinTextField);
    }
    
    public AddGroupForm()
    {
        super(ResourceBundle.getString("add_group"));
    }
}

//////////////////////////////////////////////////////////////////////////////////

// Class for removing group 
class RemoveGroupForm extends UserManagementBase
{
    private ChoiceGroup 
    	choice = new ChoiceGroup(ResourceBundle.getString("group_name"), ChoiceGroup.EXCLUSIVE);
 
    protected void select()
    {
        ContactListGroupItem[] gItems = Jimm.jimm.getContactListRef().getGroupItems();
        int index = choice.getSelectedIndex();
        
        if (index < 0)
        {
            activateContactList();
            return;
        }

        if (gItems[index].getTotalCount() != 0)
        {
            Alert errorMsg = new Alert
            (
                    ResourceBundle.getString("warning"), 
                    ResourceBundle.getString("group_is_not_empty"),
                    null, 
                    AlertType.ERROR
            );
    		errorMsg.setTimeout(10000);
    		
    		Jimm.jimm.getContactListRef().activate(errorMsg);
        }
    }
    
    protected void addControls(Form form)
    {
        form.append(choice);
        ContactListGroupItem[] gItems = Jimm.jimm.getContactListRef().getGroupItems();
        for (int i = 0; i < gItems.length; i++) choice.append(gItems[i].getName(), null); 
    }

    RemoveGroupForm()
    {
        super(ResourceBundle.getString("remove_group"));
    }
}

*/

//////////////////////////////////////////////////////////////////////////////////

// Comparer for node sorting only by name
class SimpleNodeComparer implements TreeNodeComparer
{
	public int compareNodes(TreeNode node1, TreeNode node2)
	{
		ContactListContactItem item1, item2;
		Object obj1, obj2;
	
		obj1 = node1.getData();
		obj2 = node2.getData();
		// TODO: test obj & obj2  
		
		item1 = (ContactListContactItem)obj1;
		item2 = (ContactListContactItem)obj2;
		
		return item1.getText().compareTo( item2.getText() );
	}
}


// Comparer for node sorting by status and by name
class NodeComparer implements TreeNodeComparer
{
	static int getNodeWeight(TreeNode node)
	{
		ContactListContactItem cItem;
		Object obj;

		obj = node.getData();
		if ( !(obj instanceof ContactListContactItem) ) return 10;
		cItem = (ContactListContactItem)obj;
		if (cItem.getStatus() != ContactList.STATUS_OFFLINE) return 0;
		if (cItem.returnBoolValue(ContactListContactItem.VALUE_IS_TEMP)) return 20;
	
		return 10;
	}

	public int compareNodes(TreeNode node1, TreeNode node2)
	{
		ContactListContactItem item1, item2;
		Object obj1, obj2;
	
		obj1 = node1.getData();
		obj2 = node2.getData();
		// TODO: test obj & obj2  
		
		item1 = (ContactListContactItem)obj1;
		item2 = (ContactListContactItem)obj2;
		
		int weight1 = getNodeWeight(node1);
		int weight2 = getNodeWeight(node2);
		
		if (weight1 == weight2)
		{
			return item1.getText().compareTo( item2.getText() );		
		}
	
		return (weight1 < weight2) ? -1 : 1;
	}
}

//////////////////////////////////////////////////////////////////////////////////

// Class for tree implementation
class Tree extends VirtualTree
{
	protected void getItemDrawData(TreeNode src, ListItem dst)
	{
		ContactListItem item = (ContactListItem)src.getData();
		dst.text = item.getText();
		dst.imageIndex = item.getImageIndex();
		dst.color = item.getTextColor();
	}

	public Tree(String caption)
	{
		super(caption, false);
	}
	
	protected void nodeClicked(TreeNode node)
	{
	    Jimm.jimm.getContactListRef().itemClicked();
	}
}


//////////////////////////////////////////////////////////////////////////////////
public class ContactList implements CommandListener 
//#sijapp cond.if target is "MIDP2"#
                                    , PlayerListener
//#sijapp cond.end#
{
    // Status (all are mutual exclusive) TODO: move status to ContactListContactItem
    public static final long STATUS_AWAY      = 0x00000001;
    public static final long STATUS_CHAT      = 0x00000020;
    public static final long STATUS_DND       = 0x00000002;
    public static final long STATUS_INVISIBLE = 0x00000100;
    public static final long STATUS_NA        = 0x00000004;
    public static final long STATUS_OCCUPIED  = 0x00000010;
    public static final long STATUS_OFFLINE   = 0xFFFFFFFF;
    public static final long STATUS_ONLINE    = 0x00000000;

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
	
    // Main menu command
    private static Command 
    	mainMenuCommand    = new Command(ResourceBundle.getString("menu"),         Command.SCREEN, 3),
		selectCommand      = new Command(ResourceBundle.getString("select"),       Command.ITEM, 1),
		newUserCommand     = new Command(ResourceBundle.getString("add_user"),     Command.SCREEN, 2),
		searchUserCommand  = new Command(ResourceBundle.getString("search_user"),  Command.SCREEN, 2),
		newGroupCommand    = new Command(ResourceBundle.getString("add_group"),    Command.SCREEN, 2),
		removeUserCommand  = new Command(ResourceBundle.getString("remove_user"),  Command.SCREEN, 2),
		removeGroupCommand = new Command(ResourceBundle.getString("remove_group"), Command.SCREEN, 2);
    
//#sijapp cond.if modules_DEBUGLOG is "true" #
    private static Command 
    	debugListCommand = new Command("*Debug list*", Command.ITEM, 2);
//#sijapp cond.end#
    

    /** ************************************************************************* */

    // Version id numbers
    private long versionId1;

    private int versionId2;
    
    // Update help variable
    private boolean updated;

    // Contact items
    private Vector cItems;

    // Group items
    private Vector gItems;
    
    private boolean treeBuilded = false, treeSorted = false;
    //private boolean contactsChanged;
	
    // Contains tree nodes by groip ids
	Hashtable gNodes = new Hashtable();
	
	// Tree object
	Tree tree;
	
    public Displayable getVisibleContactListRef()
    {
        return tree;
    }
	
	private static int imagesCount;
	
	public static int getImagesCount()
	{
		return imagesCount;
	}
	
	// Images for icons
	private static ImageList imageList;
	
	
	public static ImageList getImageList()
	{
		return imageList;
	}

    // The current online status
    private long onlineStatus;

    // Initializer
    static
    {
        // Construct image objects
        try
        {
        	imageList = new ImageList();
			imageList.load("/icons.png", 16);
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
            
            int from = imageList.size();
            imageList.load("/icons.png", 16);
    		int to = imageList.size();
            for (int i = from; i < to; i++)
            	imageList.setImage
				(
					addC
					(
							imageList.elementAt(i), 
							imageList.getWidth(),
							imageList.getHeight()
					),
					i
				);
            imagesCount = from;
        } 
        catch (IOException e)
        {
            // Do nothing
        }
    }

    // Constructor
    public ContactList()
    {
        onlineStatus = STATUS_ONLINE;
        
        try
        {
            this.load();
        } catch (Exception e)
        {
            this.versionId1 = -1;
            this.versionId2 = -1;
            this.updated = false;
            this.cItems = new Vector();
            this.gItems = new Vector();
            DebugLog.addText("Exception while loading list: "+e.toString());        
        }
		
		tree = new Tree(null);
		
//#sijapp cond.if target is "MIDP2"#
		tree.setFullScreenMode(false);
//#sijapp cond.end#
		
		tree.setImageList(imageList);
		tree.setStepSize( -tree.getItemHeight()/2 );
		
        //		#sijapp cond.if modules_TRAFFIC is "true" #
        this.updateTitle(Jimm.jimm.getTrafficRef().getSessionTraffic(true));
        //		#sijapp cond.else #
        this.updateTitle(0);
        //		#sijapp cond.end#
        this.tree.addCommand(ContactList.mainMenuCommand);
		this.tree.addCommand(selectCommand);
		//this.tree.addCommand(newUserCommand);
		//this.tree.addCommand(searchUserCommand);
		//this.tree.addCommand(removeUserCommand);
		//this.tree.addCommand(newGroupCommand);
		//this.tree.addCommand(removeGroupCommand);
	
//#sijapp cond.if modules_DEBUGLOG is "true" #
		this.tree.addCommand(debugListCommand);
//#sijapp cond.end#

		
        this.tree.setCommandListener(this);
    }

    // Returns the id number #1 which identifies (together with id number #2)
    // the saved contact list version
    public long getVersionId1()
    {
        return (this.versionId1);
    }

    // Returns the id number #2 which identifies (together with id number #1)
    // the saved contact list version
    public int getVersionId2()
    {
        return (this.versionId2);
    }
    
    // Returns the current status
    public long getOnlineStatus()
    {
        return (this.onlineStatus);
    }

    // Returns all contact items as array
    public synchronized ContactListContactItem[] getContactItems()
    {
        ContactListContactItem[] cItems = new ContactListContactItem[this.cItems.size()];
        this.cItems.copyInto(cItems);
        return (cItems);
    }
    
    // Returns all group items as array
    public synchronized ContactListGroupItem[] getGroupItems()
    {
        ContactListGroupItem[] gItems = new ContactListGroupItem[this.gItems.size()];
        this.gItems.copyInto(gItems);
        return (gItems);
    }

    // Request display of the given alert and the main menu afterwards
    public void activate(Alert alert)
    {
        Jimm.display.setCurrent(alert, this.tree);
    }

    // Request display of the main menu
    public void activate()
    {
    	//DebugLog.addText("Contact list activated");
    	
        //System.out.println("Show the contact list");
        //		#sijapp cond.if modules_TRAFFIC is "true" #
        Jimm.jimm.getContactListRef().updateTitle(Jimm.jimm.getTrafficRef().getSessionTraffic(true));
        //		#sijapp cond.else #
        this.updateTitle(0);
        //		#sijapp cond.end#
        
        // show contact list
        tree.lock();
        buildTree();
        sortAll();
        tree.unlock();
        Jimm.display.setCurrent(this.tree);

        // play sound notifications after connecting 
        if (needPlayOnlineNotif)
        {
        	needPlayOnlineNotif = false;
        	playSoundNotivication(SOUND_TYPE_ONLINE);
        }
        
        if (needPlayMessNotif)
        {
        	needPlayMessNotif = false;
        	playSoundNotivication(SOUND_TYPE_MESSAGE);
        }
    }
    
    // is called by options form when options changed
    public void optionsChanged(boolean needToRebuildTree, boolean needToSortContacts)
    {
    	if (needToRebuildTree) treeBuilded = false;
    	if (needToSortContacts) treeSorted = false;
    }
    
    // Tries to load contact list from record store
    private void load() throws Exception, IOException, RecordStoreException
    {
        //DebugLog.addText("Loading contact list...");
        
        // Initialize vectors
        this.cItems = new Vector();
        this.gItems = new Vector();

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
            this.versionId1 = dis.readLong();
            this.versionId2 = dis.readInt();
            
            // Read all remaining items from the record store
            int marker = 3;
            System.gc();
            long mem = Runtime.getRuntime().freeMemory();
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
                    int type = dis.readInt();

                    // Normal contact
                    if (type == 0)
                    {

                        // Get id, group id, UIN and name from the record store
                        int id = dis.readInt();
                        int group = dis.readInt();
                        String uin = dis.readUTF();
                        String name = dis.readUTF();

                        // Instantiate ContactListContactItem object and add to
                        // vector
                        ContactListContactItem ci = new ContactListContactItem(id, group, uin, name, false, true);
                        this.cItems.addElement(ci);
                    }
                    // Group of contacts
                    else if (type == 1)
                    {
                        // Get id and name from the record store
                        int id = dis.readInt();
                        String name = dis.readUTF();

                        // Instantiate ContactListGroupItem object and add to vector
                        ContactListGroupItem gi = new ContactListGroupItem(id, name);
                        this.gItems.addElement(gi);
                    }
                }
            }
            System.gc();
            System.out.println("clload mem used: "+(mem-Runtime.getRuntime().freeMemory()));
		}
        finally
		{
        	// Close record store
        	cl.closeRecordStore();  
		}
        
        
        //DebugLog.addText("Contact list loaded...");
    }

    // Save contact list to record store
    private void save() throws IOException, RecordStoreException
    {
        //DebugLog.addText("Saving contact list...");
        
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
        byte[] buf;
        ByteArrayOutputStream baos;
        DataOutputStream dos;

        // Add version info to record store
        baos = new ByteArrayOutputStream();
        dos = new DataOutputStream(baos);
        dos.writeUTF(Jimm.VERSION);
        buf = baos.toByteArray();
        cl.addRecord(buf, 0, buf.length);

        // Add version ids to the record store
        baos = new ByteArrayOutputStream();
        dos = new DataOutputStream(baos);
        dos.writeLong(this.versionId1);
        dos.writeInt(this.versionId2);
        buf = baos.toByteArray();
        cl.addRecord(buf, 0, buf.length);

        // Initialize buffer
        baos = new ByteArrayOutputStream();
        dos = new DataOutputStream(baos);

        // Iterate through all contact items
        for (int i = 0; i < this.cItems.size(); i++)
        {
            ContactListContactItem cItem = (ContactListContactItem) this.cItems.elementAt(i);

            // Add next contact item
            dos.writeInt(0);
            dos.writeInt(cItem.getId());
            dos.writeInt(cItem.getGroup());
            dos.writeUTF(cItem.getUin());
            dos.writeUTF(cItem.getName());

            // Start new record if it exceeds 4096 bytes
            if (baos.size() >= 4096)
            {

                // Save record
                buf = baos.toByteArray();
                cl.addRecord(buf, 0, buf.length);

                // Initialize buffer
                baos = new ByteArrayOutputStream();
                dos = new DataOutputStream(baos);
            }

        }

        // Iterate through all group items
        for (int i = 0; i < this.gItems.size(); i++)
        {
            ContactListGroupItem gItem = (ContactListGroupItem) this.gItems.elementAt(i);

            // Add next group item
            dos.writeInt(1);
            dos.writeInt(gItem.getId());
            dos.writeUTF(gItem.getName());

            // Start new record if it exceeds 4096 bytes
            if (baos.size() >= 4096)
            {
                // Save record
                buf = baos.toByteArray();
                cl.addRecord(buf, 0, buf.length);

                // Initialize buffer
                baos = new ByteArrayOutputStream();
                dos = new DataOutputStream(baos);
            }
        }
        // Save pending record
        if (baos.size() > 0)
        {
            // Save record
            buf = baos.toByteArray();
            cl.addRecord(buf, 0, buf.length);
        }
        // Close record store
        cl.closeRecordStore();
                
        //DebugLog.addText("Contact list saved!");
    }
    
    // called before jimm start to connect to server
    protected void beforeConnect()
    {
    	tree.clear();
    	treeBuilded = treeSorted = false;
    	int count = cItems.size();
    	for (int i = 0; i < count; i++) 
    		((ContactListContactItem)cItems.elementAt(i)).setStatus(ContactList.STATUS_OFFLINE);
    }
    
    // Updates the client-side conact list (called when a new roster has been
    // received)
    public synchronized void update(int flags, long versionId1, int versionId2, ContactListItem[] items)
    {
        System.out.println("update: new rooster");
        System.out.println("Flags: "+flags);
        System.out.println("Updated: "+this.updated);
        
        //DebugLog.addText("update: new rooster");

        // Remove all Elemente form the old ContactList
        if (!updated)
        {
            System.out.println("Clear ContactList");
            cItems.removeAllElements();
            gItems.removeAllElements();
            this.updated = false;
        }
        
        if (flags == 0)
            this.versionId1 = versionId1;
        
        if (! this.updated)
            this.versionId2 = versionId2;
        else
            this.versionId2 = this.versionId2+versionId2;
        
        System.out.println("Ver 1: "+this.versionId1);
        System.out.println("Ver 2: "+this.versionId2);

        // Add new contact items and group items
        for (int i = 0; i < items.length; i++)
        {
            if (items[i] instanceof ContactListContactItem)
            {
                this.cItems.addElement(items[i]);
            } else if (items[i] instanceof ContactListGroupItem)
            {
                this.gItems.addElement(items[i]);
            }
        }
        treeBuilded = false;
        
        // Save new contact list
        if (flags == 0)
        {
            try
            {
                System.out.println("List saved");
                this.save();
            } catch (Exception e)
            {
                DebugLog.addText("Exception while saving list: " + e.toString());
            }
        }
        if (flags == 1)
            this.updated = true;
        else 
            this.updated = false;
    }
    
    //==================================//
    //                                  //
    //    WORKING WITH CONTACTS TREE    //
    //                                  //  
    //==================================//
    
    // Sorts the contacts and calc online counters
    private void sortAll()
    {
    	if (treeSorted) return;
    	if (Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_USER_GROUPS))
    	{
            for (int i = 0; i < gItems.size(); i++)
    		{
    		    ContactListGroupItem gItem = (ContactListGroupItem)gItems.elementAt(i);
    		    TreeNode groupNode = (TreeNode)gNodes.get( new Integer(gItem.getId()) );
    		    tree.sortNode( groupNode, createNodeComparer() );
    		    calcGroupData(groupNode, gItem);
    		}
    	}
    	else tree.sortNode( null, createNodeComparer() );
    	treeSorted = true;
    	
    	//DebugLog.addText("Tree sorted");
    }
    
    // creates node comparer for node sorting 
    static private TreeNodeComparer createNodeComparer()
    {
        switch ( Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_CL_SORT_BY) )
        {
        case 0:
        	return new NodeComparer();
 	
        case 1:
        	return new SimpleNodeComparer();
        }
        return null;
    }
    
    // Builds contacts tree (without sorting) 
	private void buildTree()
	{
	    int i, gCount, cCount;
	    boolean use_groups  = Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_USER_GROUPS),
		        only_online = Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_CL_HIDE_OFFLINE);
			    
		cCount = cItems.size();
		if (treeBuilded || (cCount == 0)) return;
		
		//DebugLog.addText("Start to build tree...");
		
		tree.clear();
		System.gc();
		
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
			ContactListContactItem cItem = (ContactListContactItem)cItems.elementAt(i);
			
			if (only_online && 
			    (cItem.getStatus() == STATUS_OFFLINE) &&
				 !cItem.mustBeShownAnyWay()) continue;
			
			if (use_groups)
			{
			    ContactListGroupItem group = getGroupById( cItem.getGroup() );
		  	    TreeNode groupNode = (TreeNode)gNodes.get( new Integer( cItem.getGroup() ) );
		  		tree.addNode(groupNode, cItem);
			}
			else
			{
				tree.addNode(null, cItem);
			}
		}    
	
		treeSorted = false;
		treeBuilded = true;
		
		//DebugLog.addText("Tree builded");
	}

	// Returns reference to group with id or null if group not found
	ContactListGroupItem getGroupById(int id)
	{
	    int count = gItems.size();
	    for (int i = 0; i < count; i++)
	    {
	      if (((ContactListGroupItem)gItems.elementAt(i)).getId() == id) 
	          return (ContactListGroupItem)gItems.elementAt(i); 
	    }
	    return null;
	}
   
    public ContactListContactItem getItembyUIN(String uin)
    {
    	int count = cItems.size();
    	for (int i = 0; i < count; i++)
    	{
    	    if (((ContactListContactItem)cItems.elementAt(i)).getUin().equals(uin)) 
    	        return (ContactListContactItem)cItems.elementAt(i);
    	}
    	return null;
    }
    
    // Calculates online/total values for group
    private void calcGroupData(TreeNode groupNode, ContactListGroupItem group)
    {
        if ((group == null) || (groupNode == null)) return;
        
        ContactListContactItem cItem;
        int onlineCount = 0;
        
        int count = groupNode.size();
        for (int i = 0; i < count; i++)
        {
        	if (!(groupNode.elementAt(i).getData() instanceof ContactListContactItem)) continue; // TODO: must be removed
            cItem = (ContactListContactItem)groupNode.elementAt(i).getData();
            if (cItem.getStatus() != STATUS_OFFLINE) onlineCount++;
        }
        group.setCounters(onlineCount, count);
    }
    
    // Must be called after any changes in contacts
    private void contactChanged
    (
    	ContactListContactItem item, 
		boolean setCurrent, 
		boolean needSorting,
		boolean needCalcGroupData
    )
    {
    	boolean contactExistInTree = false,
		        contactExistsInList,
		        fullyChanged = false,
				wasDeleted = false,
				haveToAdd = false,
				haveToDelete = false;
    	TreeNode cItemNode = null;
    	int i, count, groupId;
    	
    	int debugValue = 0;
    	
    	if (!treeBuilded) return;
    	
    	try
		{
    	debugValue = 1;
    	String uin = item.getUin();
    	
    	// which group id ?
    	debugValue = 2;
    	groupId = item.getGroup();
    	
	    // which group ?
    	debugValue = 3;
	    ContactListGroupItem group = getGroupById(groupId);
	    
	    debugValue = 4;
		boolean only_online = Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_CL_HIDE_OFFLINE);
    	
    	// Whitch group node?
		debugValue = 5;
    	TreeNode groupNode = (TreeNode)gNodes.get( new Integer(groupId) );
    	if (groupNode == null) groupNode = tree.getRoot();
    	
    	// Does contact exists in tree?
    	debugValue = 6;
  		count = groupNode.size();
   		for (i = 0; i < count; i++)
   		{
   			cItemNode = groupNode.elementAt(i);
   			Object data = cItemNode.getData();
   			if ( !(data instanceof ContactListContactItem) ) continue; 
   			if ( !((ContactListContactItem)data).getUin().equals(uin) ) continue;
   			contactExistInTree = true;
   			break;
   		}
    	
    	// Does contact exists in internal list?
   		debugValue = 7;
    	contactExistsInList = (cItems.indexOf(item) != -1);
    	
    	// Lock tree repainting
    	debugValue = 8;
    	tree.lock();
    	
    	debugValue = 9;
    	haveToAdd = contactExistsInList && !contactExistInTree;
    	if (only_online && !contactExistInTree) 
    		haveToAdd |= ((item.getStatus() != STATUS_OFFLINE) | item.mustBeShownAnyWay()); 
    	
    	debugValue = 10;
    	haveToDelete = !contactExistsInList && contactExistInTree;
    	if (only_online && contactExistInTree) 
    		haveToDelete |= ((item.getStatus() == STATUS_OFFLINE) && !item.mustBeShownAnyWay());
    	
    	// if have to add new contact
    	
    	if (haveToAdd)
    	{
    		debugValue = 11;
    		cItemNode = tree.addNode(groupNode, item);
    	    fullyChanged = !item.returnBoolValue(ContactListContactItem.VALUE_IS_TEMP);
    	}
    	
    	// if have to delete contact
    	else if (haveToDelete)
    	{
    		debugValue = 12;
    		
    		//DebugLog.addText("Deleting node");
    		tree.removeNode(cItemNode);
    		wasDeleted = true;
    	}
    	
    	// sort group
    	if (needSorting && !wasDeleted)
    	{
    		
    		boolean isCurrent = (tree.getCurrentItem() == cItemNode),
			        inserted = false;
    		
    		debugValue = 13;
    		tree.deleteChild( groupNode, tree.getIndexOfChild(groupNode, cItemNode) );
    		
    		debugValue = 14;
    		int contCount = groupNode.size();
    		TreeNodeComparer comparer = createNodeComparer();
    		
    		debugValue = 15;
    		for (int j = 0; j < contCount; j++)
    		{
    			debugValue = 16;
    			TreeNode testNode = groupNode.elementAt(j);
    			if ( !(testNode.getData() instanceof ContactListContactItem) ) continue;
    			if (comparer.compareNodes(cItemNode, testNode) < 0)
    			{
    				debugValue = 17;
    				tree.insertChild(groupNode, cItemNode, j);
    				inserted = true;
    				break;
    			}
    		}
    		debugValue = 18;
    		if (!inserted) tree.insertChild(groupNode, cItemNode, contCount);
    		debugValue = 19;
    		if (isCurrent) tree.setCurrentItem(cItemNode);
    	}
    	
    	// if set current
    	debugValue = 20;
    	if (setCurrent) tree.setCurrentItem(cItemNode);
    	
    	// if calc group online/total data
    	debugValue = 21;
    	if (fullyChanged || needCalcGroupData || wasDeleted) calcGroupData(groupNode, group);
    	
    	// unlock tree and repaint
    	debugValue = 22;
    	tree.unlock();
    	
		}
    	catch (Exception e) // TODO: remove try {} catch {} !
		{
    		//DebugLog.addText("contactChanged (Exception): "+e.toString()+" "+Integer.toString(debugValue));
    		tree.unlock();
		}
    }
	
    // Updates the client-side contact list (called when roster is up to date)
	public synchronized void update()
	{
	    //DebugLog.addText("update: rooster up to date");
	    //treeBuilded = false;
	}
    
    // Updates the client-side contact list (called when coming back from
    // Message Display)
    public synchronized void update(String uin)
    {
        System.out.println("update: back form msg display");
        System.out.println("THIS IS EMPTY METHOD!!!");
    }
    
    boolean 
		needPlayOnlineNotif = false, 
		needPlayMessNotif = false; 
    
    // Updates the client-side contact list (called when a contact changes
    // status)
    //  #sijapp cond.if target is "MIDP2"#
    public synchronized void update(String uin, long status, int capabilities,
            byte[] internalIP, long dcPort, int dcType, int icqProt,
            long authCookie)
    {
        ContactListContactItem cItem = getItembyUIN(uin);
        if (cItem == null) return; // error ???
	
    	long trueStatus = Util.translateStatusReceived(status);
    	boolean statusChanged = (cItem.getStatus() != trueStatus);
        boolean wasNotOffline = (cItem.getStatus() != STATUS_OFFLINE);
        boolean nowNotOffline = (trueStatus != STATUS_OFFLINE);
        
        // Set Status
        cItem.setStatus(status);
        cItem.setCapabilities(capabilities);

        // Update DC values
        if (dcType != -1)
        {
            cItem.setDCValues(internalIP, Long.toString(dcPort), dcType,
                    icqProt, authCookie);
        }

        // Play sound notice if selected
        if ((trueStatus == STATUS_ONLINE) && statusChanged)
        {
            if ( treeBuilded ) 
            	this.playSoundNotivication(SOUND_TYPE_ONLINE);
            else
            	needPlayOnlineNotif |= true;
        }

        // Update visual list
        if (statusChanged) contactChanged
        (
                cItem, 
                false, 
                (wasNotOffline && !nowNotOffline) || (!wasNotOffline && nowNotOffline),
                true
        );
    }

    // #sijapp cond.else#
    public synchronized void update(String uin, long status, int capabilities)
    {
        //System.out.println("update: status change");

        ContactListContactItem cItem = getItembyUIN(uin);
        if (cItem == null)
            return; // error ???
        
        long trueStatus = Util.translateStatusReceived(status);
        boolean statusChanged = (cItem.getStatus() != trueStatus);
        boolean wasNotOffline = (cItem.getStatus() != STATUS_OFFLINE);
        boolean nowNotOffline = (trueStatus != STATUS_OFFLINE);
        
        // Set Status
        cItem.setStatus(status);
        cItem.setCapabilities(capabilities);

        // Play sound notice if selected
        if ((trueStatus == STATUS_ONLINE) && statusChanged)
        {
            if ( treeBuilded ) 
            	this.playSoundNotivication(SOUND_TYPE_ONLINE);
            else
            	needPlayOnlineNotif |= true;
        }

        // Update visual list
        if (statusChanged) contactChanged
        (
                cItem, 
                false, 
                (wasNotOffline && !nowNotOffline) || (!wasNotOffline && nowNotOffline),
                true
        );
    }
    // #sijapp cond.end#

    // Updates the client-side contact list (called when a contact changes
    // status)
    public synchronized void update(String uin, long status)
    {
        //System.out.println("update(String uin, long status)");
        //  #sijapp cond.if target is "MIDP2"#
        this.update(uin, status, ContactListContactItem.CAP_NO_INTERNAL,new byte[0],0,0,-1,0);
        //  #sijapp cond.else#
        this.update(uin, status, ContactListContactItem.CAP_NO_INTERNAL);
        //  #sijapp cond.end#
    }
    
    
    // Adds a "c" to the image
    private static Image addC(Image img, int width, int height)
    {
        Image copy = Image.createImage(width, height);
        Graphics g = copy.getGraphics();
        g.drawImage(img, 0, 0, Graphics.TOP | Graphics.LEFT);
        g.setColor(255, 0, 0);
        g.drawLine(3, 1, 5, 1);
        g.drawLine(2, 2, 6, 2);
        g.drawLine(1, 3, 1, 6);
        g.drawLine(2, 3, 2, 7);
        g.drawLine(3, 7, 6, 7);
        g.drawLine(3, 8, 5, 8);
        g.drawLine(5, 6, 6, 6);
        g.drawLine(5, 3, 6, 3);
        return copy;
    }

    //Updates the title of the list
    public void updateTitle(int traffic)
    {
    	String text;
        if (traffic != 0)
            text = ResourceBundle.getString("contact_list") + " - " + traffic
                    + ResourceBundle.getString("kb") + " - "
                    + Jimm.jimm.getSplashCanvasRef().getDateString(true);
        else
        	text = ResourceBundle.getString("contact_list") + " - "
                    + Jimm.jimm.getSplashCanvasRef().getDateString(true);
        
//#sijapp cond.if target is "MIDP2"#
		tree.setTitle(text);
//#sijapp cond.else#
		tree.setCaption(text);
//#sijapp cond.end#
    }

    // Removes a contact list item
    public synchronized void removeContactItem(ContactListContactItem cItem)
    {
    	//DebugLog.addText("removeContactItem "+cItem.getUin());

        // Remove given contact item
        this.cItems.removeElement(cItem);

        // Update visual list
        contactChanged(cItem, false, false, true);
    }

    // Adds a contact list item
    public synchronized void addContactItem(ContactListContactItem cItem)
    {
        //DebugLog.addText("addContactItem "+cItem.getUin()+
        //		" temp="+new Boolean(cItem.returnBoolValue(ContactListContactItem.VALUE_IS_TEMP)).toString());
        if (!cItem.returnBoolValue(ContactListContactItem.VALUE_ADDED))
        {
        	// does contact already exists or temporary ?
        	ContactListContactItem oldItem = getItembyUIN(cItem.getUin());
        	if (oldItem != null) removeContactItem(oldItem);
        	
            // Add given contact item
        	this.cItems.addElement(cItem);
            cItem.setBoolValue(ContactListContactItem.VALUE_ADDED, true);
            
            // Update visual list
            contactChanged(cItem, true, true, true);
            
            // copy old chat history number if contact was temporary 
            if (oldItem != null) cItem.copyChatHistory(oldItem);
        }
    }

    // Adds the given message to the message queue of the contact item
    // identified by the given UIN
    public synchronized void addMessage(Message message)
    {
        // Search for contact entry and add message to message queue

        //DebugLog.addText("addMessage");
        boolean listed = false;
        ContactListContactItem cItem = null;
        int i;
        for (i = 0; i < this.cItems.size(); i++)
        {
            cItem = (ContactListContactItem) this.cItems.elementAt(i);
            if (cItem.getUin().equals(message.getSndrUin()))
            {
                cItem.addMessage(message);
                listed = true;
                break;
            }
        }
        // Create a temporary contact entry if no contact entry could be found
        // do we have a new temp contact (we must do refreshList then first)
        boolean temp = false;
        if (!listed)
        {
            cItem = new ContactListContactItem(0, 0, message.getSndrUin(), message.getSndrUin(), false, true);
            cItem.setBoolValue(ContactListContactItem.VALUE_IS_TEMP,true);
            cItem.addMessage(message);
            this.cItems.addElement(cItem);
            i++;
            temp = true;
        }

        // Notify splash canvas
        Jimm.jimm.getSplashCanvasRef().messageAvailable();
        
        // Notify user
        if ( !treeBuilded ) needPlayMessNotif |= true;
        else this.playSoundNotivication(SOUND_TYPE_MESSAGE);
        
        // Update tree
        contactChanged(cItem, true, false, false);
    }
    
// #sijapp cond.if target is "SIEMENS" | target is"MIDP2"#
// #sijapp cond.if target is"MIDP2"#    
    
    // Reaction to player events. (Thanks to Alexander Barannik for idea!)
    public void playerUpdate(final Player player, final String event, Object eventData)
    {
    	// queue a call to updateEvent in the user interface event queue
    	Jimm.display.callSerially(new Runnable() {
    	public void run()
    	{
        	if (event == END_OF_MEDIA)
        	{
        		player.close();
        	}    	
    	}
    	});    	
    }
    
// #sijapp cond.end#    
	
	// Creates player for file 'source'
	private Player createPlayer(String source)
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
			InputStream is = getClass().getResourceAsStream(source);
			if (is == null) is = getClass().getResourceAsStream("/"+source);
			if (is == null)
			{
				//DebugLog.addText("Create player: source "+source+"not found");
				return null;
			}
			p = Manager.createPlayer(is, mediaType);
//#sijapp cond.if target is"MIDP2"#			
			p.addPlayerListener(this);
//#sijapp cond.end#			
		}
		catch (Exception e)
		{
			//DebugLog.addText("Create player exception: "+e.toString());
			return null;
		}
		return p;
	}
	
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
			//DebugLog.addText("setVolume "+e.toString());
		}
	}
	
// #sijapp cond.end#

    // Play a sound notification
    private void playSoundNotivication(int notType)
    {
    	if (!treeBuilded) return;
    	
        // #sijapp cond.if target is "SIEMENS" | target is "MIDP2"#
        
        // #sijapp cond.if target is "SIEMENS"#
        Light.setLightOn();
        // #sijapp cond.end#
        
        if (Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_VIBRATOR) && (notType == SOUND_TYPE_MESSAGE))
        {
            // #sijapp cond.if target is "SIEMENS"#
            Vibrator.triggerVibrator(500);
            // #sijapp cond.else#
            Jimm.display.vibrate(500);
            // #sijapp cond.end#
        }
        
        int not_mode;
        if (notType == SOUND_TYPE_MESSAGE)
            not_mode = Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_MESSAGE_NOTIFICATION_MODE);
        else
            not_mode = Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_ONLINE_NOTIFICATION_MODE);
            
        switch (not_mode)
        {
        case 1:
            try
            {
                switch(notType)
                {
                case SOUND_TYPE_MESSAGE:
                    Manager.playTone(ToneControl.C4, 500, Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_MESSAGE_NOTIFICATION_VOLUME));
                    break;
                case SOUND_TYPE_ONLINE:
                    Manager.playTone(ToneControl.C4+7, 500, Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_ONLINE_NOTIFICATION_VOLUME));
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
                	p = createPlayer( Jimm.jimm.getOptionsRef().getStringOption(Options.OPTION_MESSAGE_NOTIFICATION_SOUNDFILE) );
                	if (p == null) return;
                    setVolume(p, Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_MESSAGE_NOTIFICATION_VOLUME));
                }
                else
                {
                	p = createPlayer( Jimm.jimm.getOptionsRef().getStringOption(Options.OPTION_ONLINE_NOTIFICATION_SOUNDFILE) );
                	if (p == null) return;
                    setVolume(p, Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_ONLINE_NOTIFICATION_VOLUME)); 
                }
                
                p.start();
            }
            catch (Exception me)
            {
                // Do nothing
                //System.out.println(me.toString());
            }

            break;

        }
        // #sijapp cond.if target is "SIEMENS"#
        Light.setLightOff();
        // #sijapp cond.end#
        
        // #sijapp cond.end#
        
        // #sijapp cond.if target is "RIM"#
        LED.setConfiguration(500, 250, LED.BRIGHTNESS_50);
        LED.setState(LED.STATE_BLINKING);
        if (Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_VIBRATOR))
        {
						// had to use full path since import already contains another Alert object
            net.rim.device.api.system.Alert.startVibrate(500);
        }
        int mode_rim;
        if (notType == SOUND_TYPE_MESSAGE)
            mode_rim = Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_MESSAGE_NOTIFICATION_MODE);
        else
            mode_rim = Jimm.jimm.getOptionsRef().getIntOption(Options.OPTION_ONLINE_NOTIFICATION_MODE);
        switch (mode_rim)
        {
        case 1:
						// array is note in Hz, duration in ms.
						short[] tune = new short[] { 349, 250, 0, 10, 523, 250 };
            net.rim.device.api.system.Alert.startAudio(tune, 50);
            net.rim.device.api.system.Alert.startBuzzer(tune, 50);
            break;
				}
        // LED.setState(LED.STATE_OFF);
        // net.rim.device.api.system.Alert.stopAudio();
        // net.rim.device.api.system.Alert.stopBuzzer();
        // #sijapp cond.end#

    }
    

    //	  public void printContactList(boolean visible){
    //
    //	    if (visible){
    //	    //System.out.println("Visible CList("+contactList.size()+"):");
    //	    if (contactList.size() > 0){
    //	      for (int i=0;i<this.contactList.size();i++){
    //	        //System.out.println(this.contactList.getImage(i).toString()+" "+
    // this.contactList.getString(i));
    //	        System.out.println(this.contactList.getString(i));
    //	      }
    //	    }
    //	    System.out.println("\n");
    //	    }
    //	    else{
    //	      System.out.println("Invisible CList("+cItems.size()+"):");
    //	      if (cItems.size() > 0){
    //	      ContactListContactItem cItem;
    //	        for (int i=0;i<this.cItems.size();i++){
    //	        cItem = (ContactListContactItem) this.cItems.elementAt(i);
    //	          //System.out.println(cItem.getStatus()+" "+cItem.getName());
    //	        System.out.println(cItem.getName());
    //	        }
    //	      }
    //	    System.out.println("\n");
    //	    }
    //	  }

    
    ContactListContactItem lastChatItem = null;
   
	// Is called when user click to contact
	void itemClicked()
	{
		TreeNode node = tree.getCurrentItem();
		if (node == null) return;
		ContactListItem item = (ContactListItem)node.getData();
		if (item instanceof ContactListContactItem)
		{
			// Activate the contact item menu
			//#sijapp cond.if target is "RIM"#
			LED.setState(LED.STATE_OFF);
			//#sijapp cond.end#
			
			lastChatItem = (ContactListContactItem)item; 
			lastChatItem.activateMenu();
		}
		else if (item instanceof ContactListGroupItem)
		{
			tree.setExpandFlag(node, !node.getExpanded());
		}
		
	}
	
	// shows next or previos chat 
	synchronized protected void showNextPrevChat(boolean next)
	{
		int index = cItems.indexOf(lastChatItem);
		if (index == -1) return;
		int di = next ? 1 : -1;
		int maxSize = cItems.size();
		
		for (int i = index+di;; i += di)
		{
			if (i < 0) i = maxSize-1;
			if (i >= maxSize) i = 0;
			if (i == index) break;
			
			ContactListContactItem cItem = (ContactListContactItem)cItems.elementAt(i); 
			if ( cItem.returnBoolValue(ContactListContactItem.VALUE_HAS_CHAT) )
			{
				lastChatItem = cItem;
				cItem.activateMenu();
				break;
			}
		}
	}
	

    // Command listener
    public void commandAction(Command c, Displayable d)
    {
        // Activate main menu
        if (c == mainMenuCommand)
        {
            Jimm.jimm.getMainMenuRef().activate();
        }
        
        // Contact item has been selected
        else if (c == selectCommand)
        {
        	itemClicked();
        }

        /*
        // "new user" menu item selected
        else if (c == newUserCommand)
        {
            (new AddUserForm()).go();
        }
    	
        // "search user" menu item selected
        else if (c == searchUserCommand)
        {
            Search search = new Search();
            search.getSearchForm().activate(false);
        }
        
        // "add group" menu item selected
        else if (c == newGroupCommand)
        {
            (new AddGroupForm()).go();
        }
        
        // "remove group" menu item selected
        else if (c == removeGroupCommand)
        {
            (new RemoveGroupForm()).go();
        }
        */
        
//#sijapp cond.if modules_DEBUGLOG is "true" #
        else if (c == debugListCommand)
        {
            DebugLog.activate();
        }
//#sijapp cond.end#
    }

}
