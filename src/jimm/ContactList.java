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
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm;

import jimm.comm.Message;
import jimm.util.ResourceBundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Graphics;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

// #sijapp cond.if target is "SIEMENS"#
import com.siemens.mp.game.Sound;
import com.siemens.mp.game.Vibrator;
import com.siemens.mp.game.Light;
import com.siemens.mp.media.Manager;
import com.siemens.mp.media.MediaException;
import com.siemens.mp.media.Player;
// #sijapp cond.end#

//#sijapp cond.if target is "MIDP2"#
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.ToneControl;
import java.io.InputStream;
//#sijapp cond.end#

public class ContactList implements CommandListener
{

    // Status (all are mutual exclusive)
    public static final long STATUS_AWAY = 0x00000001;
    public static final long STATUS_CHAT = 0x00000020;
    public static final long STATUS_DND = 0x00000002;
    public static final long STATUS_INVISIBLE = 0x00000100;
    public static final long STATUS_NA = 0x00000004;
    public static final long STATUS_OCCUPIED = 0x00000010;
    public static final long STATUS_OFFLINE = 0xFFFFFFFF;
    public static final long STATUS_ONLINE = 0x00000000;

    // Sound notification typs
    public static final int SOUND_TYPE_MESSAGE = 1;
    public static final int SOUND_TYPE_ONLINE = 2;
    
    // Image objects
    static Image images[];

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

    // Initializer
    static
    {
        // Construct image objects
        try
        {
            Image iconsImage, dImage;
            iconsImage = Image.createImage("/icons.png");
            int imagenum = iconsImage.getWidth() >> 4;
            images = new Image[imagenum];
            for (int i = 0; i < imagenum; i++)
            {
                dImage = Image.createImage(16, 16);
                dImage.getGraphics().drawImage(iconsImage, -i << 4, 0, Graphics.TOP | Graphics.LEFT);
                images[i] = Image.createImage(dImage);
            }
            ContactList.statusAwayImg = images[0];
            ContactList.statusChatImg = images[1];
            ContactList.statusDndImg = images[2];
            ContactList.statusInvisibleImg = images[3];
            ContactList.statusNaImg = images[4];
            ContactList.statusOccupiedImg = images[5];
            ContactList.statusOfflineImg = images[6];
            ContactList.statusOnlineImg = images[7];
            ContactList.eventPlainMessageImg = images[8];
            ContactList.eventUrlMessageImg = images[9];
            ContactList.eventSystemNoticeImg = images[10];
            ContactList.eventSysActionImg = images[11];
        } catch (IOException e)
        {
            // Do nothing
        }
    }

    // Main menu command
    private static Command mainMenuCommand = new Command(ResourceBundle.getString("jimm.res.Text", "menu"),
            Command.ITEM, 1);

    /** ************************************************************************* */

    // Version id numbers
    private long versionId1;

    private int versionId2;

    // Contact items
    private Vector cItems;

    // Have there been changes since last visible list update?
    private boolean changed;

    // Group items
    private Vector gItems;

    // Contact list
    private List contactList;

    // How many are online
    private int onlineCount;

    // Reference to currently selected contact item
    private ContactListContactItem currSelCItem;

    // Constructor
    public ContactList()
    {

        // Try to load contact list from record store

        try
        {
            this.load();
        } catch (Exception e)
        {
            this.versionId1 = -1;
            this.versionId2 = -1;
            this.cItems = new Vector();
            this.gItems = new Vector();
        }
        onlineCount = 0;
        changed = true;
        // Initialize contact list
        this.contactList = new List(ResourceBundle.getString("jimm.res.Text", "contact_list"), Choice.IMPLICIT);
        //		#sijapp cond.if modules_TRAFFIC is "true" #
        this.updateTitle(Jimm.jimm.getTrafficRef().getSessionTraffic(true));
        //		#sijapp cond.else #
        this.updateTitle(0);
        //		#sijapp cond.end#
        this.contactList.addCommand(ContactList.mainMenuCommand);
        this.contactList.setCommandListener(this);

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
    
    // Return the contactList
    public List getContactList()
    {
        return contactList;
    }

    // Request display of the given alert and the main menu afterwards
    public void activate(Alert alert)
    {
        Jimm.display.setCurrent(alert, this.contactList);
    }

    // Request display of the main menu
    public void activate()
    {
        //System.out.println("Show the contact list");
        //		#sijapp cond.if modules_TRAFFIC is "true" #
        Jimm.jimm.getContactListRef().updateTitle(Jimm.jimm.getTrafficRef().getSessionTraffic(true));
        //		#sijapp cond.else #
        this.updateTitle(0);
        //		#sijapp cond.end#
        if (changed)
        {
            this.refreshVisibleList(true);
        }
        Jimm.display.setCurrent(this.contactList);
    }

    // Tries to load contact list from record store
    private void load() throws Exception, IOException, RecordStoreException
    {

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
        // Close record store
        cl.closeRecordStore();

    }

    // Save contact list to record store
    private void save() throws IOException, RecordStoreException
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
    }

    // Changes only the place of the changed element in the list
    private synchronized int sortElement(int index)
    {

        //System.out.println("SortElement: " + index);

        // Insertion sort (sort first by status - online/offline/temprary -,
        // then by nick)
        ContactListContactItem v = (ContactListContactItem) this.cItems.elementAt(index);
        this.cItems.removeElementAt(index);
        //this.contactList.delete(index);

        int i = 0;
        while (((ContactListContactItem) this.cItems.elementAt(i)).compareTo(v) < 0)
        {
            i++;
            if (i == this.cItems.size()) break;
        }
        this.cItems.insertElementAt((ContactListItem) v, i);

        return i;
    }

    // Sorts the contact list completely (used after rooster update)
    // Insertion sort (sort first by status - online/offline/temprary -, then by
    // nick)
    public synchronized void sortAll()
    {

        // System.out.println("\n");
        // System.out.println("sort all");

        int j;
        ContactListContactItem v, w;
        for (int i = 1; i < this.cItems.size(); i++)
        {
            j = i;
            v = (ContactListContactItem) this.cItems.elementAt(i);
            while ((w = (ContactListContactItem) this.cItems.elementAt(j - 1)).compareTo(v) > 0)
            {
                this.cItems.setElementAt(w, j);
                if (--j == 0) break;
            }
            if (i != j)
            {
                this.cItems.setElementAt(v, j);
            }
        }
        changed = true;
    }

    // Updates the client-side conact list (called when a new roster has been
    // received)
    public synchronized void update(long versionId1, int versionId2, ContactListItem[] items)
    {

        //System.out.println("\n");
        //System.out.println("update: new rooster");

        // Save selected contact entry
        this.saveListPosition();

        // Save version numbers
        this.versionId1 = versionId1;
        this.versionId2 = versionId2;

        // Remove all Elemente form the old ContactList
        cItems.removeAllElements();

        // Add new contact items and group items
        for (int i = 0; i < items.length; i++)
        {
            //System.out.println(i);
            if (items[i] instanceof ContactListContactItem)
            {
                //Sort the new item in the contact list
                int j;
                for (j = 0; i < cItems.size(); j++)
                {
                    if (((ContactListContactItem) this.cItems.elementAt(j))
                            .compareTo((ContactListContactItem) items[i]) >= 0)
                    {
                        break;
                    }
                }
                this.cItems.insertElementAt(items[i], j);
            } else if (items[i] instanceof ContactListGroupItem)
            {
                if (!this.gItems.contains(items[i]))
                {
                    this.gItems.addElement(items[i]);
                }
            }
        }

        // Save new contact list
        try
        {
            this.save();
        } catch (Exception e)
        {
            // Do nothing
        }

    }

    // Updates the client-side contact list (called when roster is up to date)
    public synchronized void update()
    {

        System.out.println("\n");
        System.out.println("update: rooster up to date");

        // Save selected contact entry
        this.saveListPosition();
    }

    // Updates the client-side contact list (called when coming back from
    // Message Display)
    public synchronized void update(String uin)
    {

        System.out.println("\n");
        System.out.println("update: back form msg display");

        // Save selected contact entry
        this.saveListPosition();

        // Find postion in list
        int i;
        ContactListContactItem cItem;
        for (i = 0; i < this.cItems.size(); i++)
        {
            cItem = (ContactListContactItem) this.cItems.elementAt(i);
            if (cItem.getUin().equals(uin)) break;
        }
        // Update list
        this.refreshList(true, false, i);
    }

    // Updates the client-side contact list (called when a contact changes
    // status)
    public synchronized void update(String uin, long status, int capabilities)
    {

        System.out.println("\n");
        System.out.println("update: status change");
        //Do we have an offline to online change?
        boolean wasoffline = false;
        boolean onoffchange = false;

        // Save selected contact entry
        this.saveListPosition();

        // Update status
        ContactListContactItem cItem;
        int i;
        for (i = 0; i < this.cItems.size(); i++)
        {
            cItem = (ContactListContactItem) this.cItems.elementAt(i);
            if (cItem.getUin().equals(uin))
            {
                //Do we have on offline<->online change?
                if (cItem.getStatus() == STATUS_OFFLINE)
                {
                    onlineCount++;
                    wasoffline = true;
                    onoffchange = true;
                }
                if (status == STATUS_OFFLINE)
                {
                    onlineCount--;
                    wasoffline = true;
                }
                cItem.setStatus(status);
                cItem.setCapabilities(capabilities);
                break;
            }
        }

        // Update list only if the item is in our list (was in the rooster)
        if (i < this.cItems.size())
        {
            // Play sound notice if selected
            if (onoffchange)
                this.playSoundNotivication(SOUND_TYPE_ONLINE);
            // Update visual list (sorting only if it was on online offline or
            // vice versa change
            this.refreshList(!wasoffline, false, i);
        }
    }

    // Updates the client-side contact list (called when a contact changes
    // status)
    public synchronized void update(String uin, long status)
    {
        this.update(uin, status, ContactListContactItem.CAP_NO_INTERNAL);
    }

    // Adds a "c" to the contact list image to show that there is an active chat
    private Image addC(Image img)
    {
        Image copy = Image.createImage(16, 16);
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
        img = Image.createImage(copy);
        return img;
    }

    //Updates the title of the list
    public void updateTitle(int traffic)
    {
        if (traffic != 0)
            contactList.setTitle(ResourceBundle.getString("jimm.res.Text", "contact_list") + " - " + traffic
                    + ResourceBundle.getString("jimm.res.Text", "kb") + " - "
                    + Jimm.jimm.getSplashCanvasRef().getDateString(true));
        else
            contactList.setTitle(ResourceBundle.getString("jimm.res.Text", "contact_list") + " - "
                    + Jimm.jimm.getSplashCanvasRef().getDateString(true));
    }

    // Removes a contact list item
    public synchronized void removeContactItem(ContactListContactItem cItem)
    {

        // System.out.println("\n");
        System.out.println("RemoveItem");
        System.out.println(cItem.toString());

        // Save selected contact entry
        this.saveListPosition();

        // Remove given contact item
        this.cItems.removeElement(cItem);

        // Update visual list
        this.refreshList(true, false, Integer.MAX_VALUE);
        this.refreshVisibleList(true);

    }

    // Adds a contact list item
    public synchronized void addContactItem(ContactListContactItem cItem)
    {
        // System.out.println("\n");
        System.out.println("AddItem");
        if (!cItem.added())
        {
            System.out.println(cItem.toString());
            // Save selected contact entry
            this.saveListPosition();
            // Add given contact item
            cItem.setAdded(true);
            this.cItems.addElement(cItem);
            // Resort List
            this.sortElement(cItems.size() - 1);
            // Update visual list
            this.refreshList(true, false, Integer.MAX_VALUE);
            this.refreshVisibleList(true);
        }
    }

    // Adds the given message to the message queue of the contact item
    // identified by the given UIN
    public synchronized void addMessage(Message message)
    {
        // Search for contact entry and add message to message queue
        // System.out.println("\n");
        // System.out.println("addMessage");
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
            cItem.setTemporary(true);
            cItem.addMessage(message);
            this.cItems.addElement(cItem);
            i++;
            temp = true;
        }

        // Refresh the contact list
        if (temp)
            this.refreshList(false, true, i - 1);
        else if (!cItem.chatShown()) this.refreshList(true, true, i);
        // Notify splash canvas
        Jimm.jimm.getSplashCanvasRef().messageAvailable();
        // Notify user
        this.playSoundNotivication(SOUND_TYPE_MESSAGE);
    }

    // Play a sound notification
    private void playSoundNotivication(int notType)
    {
        // #sijapp cond.if target is "SIEMENS"#
        Light.setLightOn();
        if (Jimm.jimm.getOptionsRef().isVibrator())
        {
            Vibrator.triggerVibrator(500);
        }
        switch (Jimm.jimm.getOptionsRef().getNotificationMode(notType))
        {
        case 1:
            Sound.playTone(1000, 1000);
            break;
        case 2:
            try
            {
                Player p = Manager.createPlayer(Jimm.jimm.getOptionsRef().getSoundFileName(notType));
                p.start();
            } catch (IOException e)
            {
                e.printStackTrace();
            } catch (MediaException e)
            {
                e.printStackTrace();
            }
            break;

        }
        Light.setLightOff();
        // #sijapp cond.end#
        // #sijapp cond.if target is "MIDP2"#
        switch (Jimm.jimm.getOptionsRef().getNotificationMode(notType))
        {
        case 1:
            try
            {
                switch(notType)
                {
                case SOUND_TYPE_MESSAGE:
                    Manager.playTone(ToneControl.C4, 500, 100);
                    break;
                case SOUND_TYPE_ONLINE:
                    Manager.playTone(ToneControl.C4+7, 500, 100);
                }
                
            } catch (MediaException e)
            {
                // Do nothing
            }
            break;
        case 2:
            try
            {
                InputStream is = getClass().getResourceAsStream(Jimm.jimm.getOptionsRef().getSoundFileName(notType));
                Player p = Manager.createPlayer(is, "audio/X-wav");
                p.start();
            } catch (IOException ioe)
            {
            } catch (MediaException me)
            {
                // Do nothing
            }

            break;

        }
        // #sijapp cond.end#

    }
    
    // Save reference to currently selected contact item
    private void saveListPosition()
    {

        // System.out.println("\n");
        // System.out.println("saveListPosition");
        if (this.contactList.size() > 0)
        {
            this.currSelCItem = (ContactListContactItem) this.cItems.elementAt(this.contactList.getSelectedIndex());
        }
    }

    // Returns an reference to the visible Contact List
    public List getVisibleContactListRef()
    {
        return this.contactList;
    }

    // Clears the reference to the currently selected contact item
    public void resetListPosition()
    {

        // System.out.println("\n");
        // System.out.println("resetListPosition");
        this.currSelCItem = null;
        this.refreshList(true, false, Integer.MAX_VALUE);
    }

    // Retruns the image for the given status
    public Image whichImage(ContactListContactItem cItem)
    {

        // Get status image
        Image img = null;
        if (cItem.isunasweredAuthRequest())
            img = eventSysActionImg;
        else if (cItem.isSysNoticeAvailable() || cItem.noAuth())
        {
            img = eventSystemNoticeImg;
        } else if (cItem.isTemporary())
        {
            img = null;
        }

        if (!(cItem.isPlainMessageAvailable() || cItem.isUrlMessageAvailable() || cItem.isSysNoticeAvailable() || cItem
                .noAuth()))
        {
            long status = cItem.getStatus();
            if (status == STATUS_AWAY)
                img = statusAwayImg;
            else if (status == STATUS_CHAT)
                img = statusChatImg;
            else if (status == STATUS_DND)
                img = statusDndImg;
            else if (status == STATUS_INVISIBLE)
                img = statusInvisibleImg;
            else if (status == STATUS_NA)
                img = statusNaImg;
            else if (status == STATUS_OCCUPIED)
                img = statusOccupiedImg;
            else if (status == STATUS_OFFLINE)
                img = statusOfflineImg;
            else if (status == STATUS_ONLINE) img = statusOnlineImg;

            // Add an "c" the the Status icon determining we have an open chat
            // session for that one.
            if (cItem.hasChat() && (img != null))
            {
                img = this.addC(img);
            }
        }
        return img;
    }

    // Refreshes the visible contact list
    public void refreshVisibleList(boolean evenIfNotShown)
    {

        // System.out.println("refreshVisibleList call");

        //	Refreh the visible List of Conctacts only if its showing or the
        // update is forced
        if (Jimm.jimm.getContactListRef().getVisibleContactListRef().isShown() || evenIfNotShown)
        {

            // System.out.println("refreshVisibleList doit");

            // Dtermine the number of visible items
            int repeatfor = onlineCount;
            if (!Jimm.jimm.getOptionsRef().isClHideOffline()) repeatfor = this.cItems.size();

            ContactListContactItem cItem;
            Image img;

            //Interate over the number of visible items
            for (int i = 0; i < repeatfor; i++)
            {

                // Get the item we want form the Vector
                cItem = (ContactListContactItem) this.cItems.elementAt(i);

                // Get event image, if event is enabled
                if (cItem.isPlainMessageAvailable())
                {
                    img = eventPlainMessageImg;
                } else if (cItem.isUrlMessageAvailable())
                {
                    img = eventUrlMessageImg;
                } else if (cItem.isunasweredAuthRequest())
                    img = eventSysActionImg;
                else if (cItem.isSysNoticeAvailable())
                {
                    img = eventSystemNoticeImg;
                } else
                {
                    img = whichImage(cItem);
                }

                //System.out.println("Invisble size: "+this.cItems.size());
                //System.out.println("Visible size: "+this.contactList.size());

                // Add/update list item
                this.contactList.set(i, cItem.getName(), img);
            }
            changed = false;
        }

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

    // Refreshes the contact list
    // noSort: ContactList will not be resorted
    // message: is called because a message should be added
    // position: position of the cItem which should be updated
    public void refreshList(boolean noSort, boolean message, int position)
    {

        // System.out.println("jimm:ContactList:refreshList");

        // How long should the visible list be? this.contactList.size() for not
        // hiding
        // offline contacts and this.onlineCount if hiding offline contacts
        int clLength = this.onlineCount;
        if (!Jimm.jimm.getOptionsRef().isClHideOffline())
        {
            clLength = this.cItems.size();
        }

        // Add empty elements to visual contact list, if required
        while (this.contactList.size() < clLength)
        {
            this.contactList.append("", null);
        }

        // Delete elements from visual contact list, if required
        while (this.contactList.size() > clLength)
        {
            this.contactList.delete(this.contactList.size() - 1);
        }

        //	Sorts the element if requested
        if (!noSort)
        {
            position = sortElement(position);
            changed = true;
            // Refreshes the visible contact list
            this.refreshVisibleList(false);
        }

        // New focus
        boolean focused = false;
        ContactListContactItem cItem;
        if (message)
        {

            //System.out.println("Message");
            cItem = (ContactListContactItem) this.cItems.elementAt(position);
            Image img = null;

            // Get event image, if event is enabled
            if (cItem.isPlainMessageAvailable())
                img = eventPlainMessageImg;
            else if (cItem.isUrlMessageAvailable()) img = eventUrlMessageImg;
            if (cItem.isunasweredAuthRequest())
                img = eventSysActionImg;
            else if (cItem.isSysNoticeAvailable())
            {
                img = eventSystemNoticeImg;
            }

            // Add an "c" the the Status icon determining we have an open chat
            // session for that one.
            if (cItem.hasChat())
            {
                img = this.addC(img);
            }

            // Add/update list item
            this.contactList.set(position, cItem.getName(), img);

            // Change selected item
            if (!focused)
            {
                focused = true;
                this.contactList.setSelectedIndex(position, true);
                this.currSelCItem = cItem;
            }
        } else
        {

            // Only change image if we have a postion for it and the new status
            // it not offline and were
            // are not hiding offline contacts
            if (position != Integer.MAX_VALUE)
            {
                cItem = (ContactListContactItem) this.cItems.elementAt(position);
                if (!((cItem.getStatus() == STATUS_OFFLINE) && Jimm.jimm.getOptionsRef().isClHideOffline()))
                {
                    //System.out.println("jimm:ContactList:refreshList: Image
                    // change at:" + position);
                    // Update list item
                    this.contactList.set(position, cItem.getName(), whichImage(cItem));
                }
            }
        }

        // Focus item
        //	System.out.println("Change focus");
        if (contactList.size() > 0)
        {
            if (!focused && (this.currSelCItem != null) && (this.cItems.indexOf(this.currSelCItem) != -1))
            {
                if ((this.cItems.elementAt(this.contactList.getSelectedIndex()) != this.currSelCItem)
                        && !Jimm.jimm.getOptionsRef().isClHideOffline())
                {
                    this.contactList.setSelectedIndex(this.cItems.indexOf(this.currSelCItem), true);
                }
            } else if (!focused && (this.contactList.size() > 0))
            {
                this.contactList.setSelectedIndex(0, true);
                this.currSelCItem = (ContactListContactItem) this.cItems.elementAt(0);
            }
        }
        //printContactList(true);
        //printContactList(false);
    }

    // Command listener
    public void commandAction(Command c, Displayable d)
    {

        // Activate main menu
        if (c == ContactList.mainMenuCommand)
        {
            Jimm.jimm.getMainMenuRef().activate();
        }
        // Contact item has been selected
        else if (c == List.SELECT_COMMAND)
        {

            // Get selected contact item
            ContactListContactItem currCItem = (ContactListContactItem) this.cItems.elementAt(this.contactList
                    .getSelectedIndex());

            // Activate the contact item menu
            currCItem.activateMenu();

        }

    }

}