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
 File: src/jimm/comm/UpdateContactListAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import java.util.Date;
import java.util.Vector;

import jimm.ContactListContactItem;
import jimm.ContactListGroupItem;
import jimm.ContactListItem;
import jimm.Jimm;
import jimm.JimmException;

public class UpdateContactListAction extends Action
{

    // Action states
    public static final int STATE_ERROR = -1;
    public static final int STATE_INIT = 0;
    public static final int STATE_CLI_ROSTERMODIFY_SENT = 1;;
    public static final int STATE_CLI_ADDEND_SENT = 2;
    public static final int STATE_SRV_UPDATEACK_RCVD = 3;
    public static final int STATE_SRV_REPLYED_AUTH = 4;
    
    // Timeout
    public static final int TIMEOUT = 10 * 1000; // milliseconds

    /** ************************************************************************* */

    // Contact item
    private ContactListContactItem cItem;
    
    // Group item
    private ContactListGroupItem gItem;

    // Action state
    private int state;

    // Action types
    private boolean add;
    private boolean modify;
    
    // Type of error happend
    private int error;

    // Last activity
    private Date lastActivity = new Date();
    
    // Byte arrays for ID and name
    byte[] idRaw;
    byte[] nameRaw;

    // Constructor (removes or adds given contact item)
    public UpdateContactListAction(ContactListItem cItem, boolean add, boolean modify)
    {
        this.add = add;
        this.modify = modify;
        if (cItem instanceof ContactListContactItem)
        {
            this.cItem = (ContactListContactItem) cItem;
            this.gItem = null;
            this.idRaw = Util.stringToByteArray(this.cItem.getUin());
            this.nameRaw = Util.stringToByteArray(this.cItem.getName());
        }
        else
        {
            this.cItem = null;
            this.gItem = (ContactListGroupItem) cItem;
            this.idRaw = Util.stringToByteArray(this.gItem.getName());
            this.nameRaw = null;
        }
        this.error = 0;
    }
        

    // Returns true if the action can be performed
    public boolean isExecutable()
    {
        return (this.icq.isConnected());
    }

    // Returns true if this is an exclusive command
    public boolean isExclusive()
    {
        return (false);
    }

    // Init action
    protected void init() throws JimmException
    {
        SnacPacket packet;

        // Send a buddy add or delete packet if this update deals with a contact
        if (cItem != null && !modify)
        {
            byte[] buf;
            int marker = 0;

            // Pack and send CLI_BUDDYLIST_REMOVE or CLI_BUDDYLIST_ADD packet
            buf = new byte[1 + idRaw.length];

            Util.putByte(buf, marker, idRaw.length);
            System.arraycopy(idRaw, 0, buf, 1, idRaw.length);

            if (!add)
                packet = new SnacPacket(SnacPacket.CLI_BUDDYLIST_REMOVE_FAMILY, SnacPacket.CLI_BUDDYLIST_REMOVE_COMMAND, SnacPacket.CLI_BUDDYLIST_REMOVE_COMMAND, new byte[0], buf);
            else
                packet = new SnacPacket(SnacPacket.CLI_BUDDYLIST_ADD_FAMILY, SnacPacket.CLI_BUDDYLIST_ADD_COMMAND, SnacPacket.CLI_BUDDYLIST_ADD_COMMAND, new byte[0], buf);

            this.icq.c.sendPacket(packet);
        }

        // Send a CLI_ADDSTART packet
        packet = new SnacPacket(SnacPacket.CLI_ADDSTART_FAMILY, SnacPacket.CLI_ADDSTART_COMMAND, SnacPacket.CLI_ADDSTART_COMMAND, new byte[0], new byte[0]);
        this.icq.c.sendPacket(packet);

        if (modify)
            packet = new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY, SnacPacket.CLI_ROSTERUPDATE_COMMAND, Util.getCounter(), new byte[0], this.packRoosterItem(null, null, null));
        else
            if (add)
            {
                // Send CLI_ROSTERADDpacket
                packet = new SnacPacket(SnacPacket.CLI_ROSTERADD_FAMILY, SnacPacket.CLI_ROSTERADD_COMMAND, Util.getCounter(), new byte[0], this.packRoosterItem(null, null, null));
            }
            else
            {
                // Send a CLI_ROSTERDELETE packet
                packet = new SnacPacket(SnacPacket.CLI_ROSTERDELETE_FAMILY, SnacPacket.CLI_ROSTERDELETE_COMMAND, SnacPacket.CLI_ROSTERDELETE_COMMAND, new byte[0], this.packRoosterItem(null, null, null));
            }

        this.icq.c.sendPacket(packet);

        // Set state
        this.state = UpdateContactListAction.STATE_CLI_ROSTERMODIFY_SENT;
    }

    // Forwards received packet, returns true if packet was consumed
    protected synchronized boolean forward(Packet packet) throws JimmException
    {
        // Catch JimmExceptions
        try
        {
            // Flag indicates whether packet has been consumed or not
            boolean consumed = false;

            // Watch out for STATE_CLI_ROSTERMODIFY_SENT
            if (this.state == UpdateContactListAction.STATE_CLI_ROSTERMODIFY_SENT)
            {
                // Watch out for SRV_UPDATEACK packet type
                if (packet instanceof SnacPacket)
                {
                    SnacPacket snacPacket = (SnacPacket) packet;
                    if ((snacPacket.getFamily() == SnacPacket.SRV_UPDATEACK_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_UPDATEACK_COMMAND))
                    {
                        // Check error code, see ICQv8 specification
                        switch (Util.getWord(snacPacket.getData(), 0))
                        {
                        case 0x002:
                            error = 2;
                            throw (new JimmException(154, 0, true));                            
                        case 0x003:
                            error = 3;
                            throw (new JimmException(155, 0, true));
                        case 0x00A:
                            error = 10;
                            throw (new JimmException(156, 0, true));
                        case 0x00C:
                            error = 12;
                            throw (new JimmException(157, 0, true));
                        case 0x00D:
                            error = 13;
                            throw (new JimmException(158, 0, true));
                        case 0x00E:
                            cItem.setBoolValue(ContactListContactItem.VALUE_NO_AUTH, true);
                            Jimm.jimm.getContactListRef().addContactItem(this.cItem);
                            this.state = UpdateContactListAction.STATE_SRV_REPLYED_AUTH;
                            break;
                        default:
                            
                            // Send an ROOSTER_UPDATE packet if contact/group was added/deleted
                            if (!this.modify)
                            {
                                Vector cItemsRemaining = null;
                                
                                // Get all group items as aray
                                ContactListGroupItem[] gItems = Jimm.jimm.getContactListRef().getGroupItems();
                                ContactListGroupItem gItem = null;

                                if (this.cItem != null)
                                {
                                    cItem.setBoolValue(ContactListContactItem.VALUE_IS_TEMP, false);

                                    // Get all contact items as aray
                                    ContactListContactItem[] cItems = Jimm.jimm.getContactListRef().getContactItems();

                                    // Get group of contact item to be removed or added to
                                    for (int i = 0; i < gItems.length; i++)
                                    {
                                        if (gItems[i].getId() == this.cItem.getGroup())
                                        {
                                            gItem = gItems[i];
                                            break;
                                        }
                                    }
                                    if (gItem == null) { throw (new JimmException(154, 1, true)); }

                                    // Get all contact items in this group
                                    // Either this is the whole group plus the new item (adding) or
                                    // it is the old list without the to be removed item
                                    cItemsRemaining = new Vector();

                                    for (int i = 0; i < cItems.length; i++)
                                    {
                                        if ((gItem.getId() == cItems[i].getGroup()) && ((this.cItem != cItems[i]) || cItem.returnBoolValue(ContactListContactItem.VALUE_IS_TEMP)))
                                        {
                                            cItemsRemaining.addElement(cItems[i]);
                                        }
                                    }
                                    if (cItem.returnBoolValue(ContactListContactItem.VALUE_IS_TEMP))
                                    {
                                        cItemsRemaining.addElement(cItem);
                                    }
                                }
                                // Send CLI_ROSTERUPDATE packet
                                packet = new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY, SnacPacket.CLI_ROSTERUPDATE_COMMAND,SnacPacket.CLI_ROSTERUPDATE_COMMAND, new byte[0], this.packRoosterItem(cItemsRemaining, gItem,gItems));
                                this.icq.c.sendPacket(packet);
                            }
                        }
                        // Send a CLI_ADDEND packet
                        packet = new SnacPacket(SnacPacket.CLI_ADDEND_FAMILY, SnacPacket.CLI_ADDEND_COMMAND, SnacPacket.CLI_ADDEND_COMMAND,new byte[0], new byte[0]);
                        this.icq.c.sendPacket(packet);

                        // Move to next state
                        if (!this.modify)
                            this.state = UpdateContactListAction.STATE_CLI_ADDEND_SENT;
                        else
                            this.state = UpdateContactListAction.STATE_SRV_UPDATEACK_RCVD;
                        
                        // Packet has been consumed
                        consumed = true;
                    }
                }
            }
            // Watch out for STATE_CLI_ADDEND_SENT
            else if (this.state == UpdateContactListAction.STATE_CLI_ADDEND_SENT)
            {

                // Watch out for SRV_UPDATEACK packet type
                if (packet instanceof SnacPacket)
                {
                    SnacPacket snacPacket = (SnacPacket) packet;
                    if ((snacPacket.getFamily() == SnacPacket.SRV_UPDATEACK_FAMILY) && (snacPacket.getCommand() == SnacPacket.SRV_UPDATEACK_COMMAND))
                    {
                        // Check error code, see ICQv8 specification
                        if (Util.getWord(snacPacket.getData(), 0) != 0x0000) { throw (new JimmException(154, 0, true)); }

                        // Delete or add contact or group item from internal list
                        if (cItem != null)
                            if (!add)
                                Jimm.jimm.getContactListRef().removeContactItem(this.cItem);
                            else
                                Jimm.jimm.getContactListRef().addContactItem(this.cItem);
                        else
                            if (!add)
                                Jimm.jimm.getContactListRef().removeGroup(this.gItem);
                            else
                                Jimm.jimm.getContactListRef().addGroup(this.gItem);

                        // Move to next state
                        this.state = UpdateContactListAction.STATE_SRV_UPDATEACK_RCVD;

                        // Packet has been consumed
                        consumed = true;
                    }
                }
            }

            // Update activity timestamp
            this.lastActivity = new Date();

            // Return consumption flag
            return (consumed);
        }
        // Catch JimmExceptions
        catch (JimmException e)
        {
            // Send a CLI_ADDEND packet
            packet = new SnacPacket(SnacPacket.CLI_ADDEND_FAMILY,SnacPacket.CLI_ADDEND_COMMAND,SnacPacket.CLI_ADDEND_COMMAND, new byte[0], new byte[0]);
            this.icq.c.sendPacket(packet);
            
            // Update activity timestamp
            this.lastActivity = new Date();

            // Set error state if exception is critical
            if (e.isCritical() || (error != 0)) 
                this.state = ConnectAction.STATE_ERROR;
            
            while (!Jimm.jimm.getContactListRef().getVisibleContactListRef().isShown())
                try
                {
                    this.wait(100);
                } catch (InterruptedException e1)
                {
                    // Do nothing
                }
            // Forward exception
            throw (e);
        }
    }

    // Returns type of the error that happend
    public int getErrorType(){
        return error;
    }
    
    // Returns true if the action is completed
    public boolean isCompleted()
    {
        return (this.state == UpdateContactListAction.STATE_SRV_UPDATEACK_RCVD || this.state == UpdateContactListAction.STATE_SRV_REPLYED_AUTH);
    }

    // Returns true if an error has occured
    public boolean isError()
    {
        if ((this.state != ConnectAction.STATE_ERROR) && (this.lastActivity.getTime() + UpdateContactListAction.TIMEOUT < System.currentTimeMillis()))
        {
            JimmException.handleException(new JimmException(154, 3));
            this.state = ConnectAction.STATE_ERROR;
        }
        return (this.state == ConnectAction.STATE_ERROR);
    }
    
    // Pack Rooster update/add/delete packet content
    public byte[] packRoosterItem(Vector cItemsRemaining, ContactListGroupItem gItem, ContactListGroupItem[] gItems)
    {
        // Calculate length depending on type of packet
        int length;
        if (!modify)
            if (this.state == STATE_INIT)
                if (!this.add)
                    if (cItem != null)
                        length = 2 + idRaw.length + 8 + 4 + nameRaw.length; // Delete cItem
                    else
                        length = 2 + idRaw.length + 8; // Delete gItem
                else if (cItem != null)
                    	if (cItem.returnBoolValue(ContactListContactItem.VALUE_NO_AUTH))
                    	    length = 2 + idRaw.length + 6 + 6 + 4 + nameRaw.length; // Add cItem(noAuth)
                    	else
                    	    length = 2 + idRaw.length + 6 + 2 + 4 + nameRaw.length; // Add cItem(auth)
                else
                    length = 2 + idRaw.length + 8; // Add gItem
            else if (cItem != null)
            {
                idRaw = Util.stringToByteArray(gItem.getName());
                length = 2 + idRaw.length + 8 + 4 + cItemsRemaining.size() * 2; // Update group header for contact
            } else
            {
                idRaw = new byte[0];
                length = 2 + 8 + 4 + gItems.length * 2 + 2; // Update group header for group
            }
        else
            length = length = 2 + idRaw.length + 6 + 2 + 4 + nameRaw.length; // Modify name        

        // Byte array for the packet to be constructed
        byte[] buf = new byte[length];

        // Marker for the packet construction
        int marker = 0;

        Util.putWord(buf, marker, idRaw.length);
        System.arraycopy(idRaw, 0, buf, marker + 2, idRaw.length);
        marker += 2 + idRaw.length;

        if (this.state == STATE_INIT)
        {
            if (cItem != null)
            {
                Util.putWord(buf, marker, this.cItem.getGroup());
                marker += 2;
                Util.putWord(buf, marker, this.cItem.getId());
            } else
            {
                Util.putWord(buf, marker, this.gItem.getId());
                marker += 2;
                Util.putDWord(buf, marker, 0x0000);
                marker += 2;
                Util.putWord(buf, marker, 0x0001);
            }
            marker += 2;
            Util.putWord(buf, marker, 0x0000);
            marker += 2;
        } else
        {
            if (cItem != null)
                Util.putWord(buf, marker, gItem.getId());
            else
                Util.putWord(buf, marker, 0x0000);
            marker += 2;

            Util.putWord(buf, marker, 0x0000);
            marker += 2;
            Util.putWord(buf, marker, 0x0001);
            marker += 2;
        }

        if (this.state == STATE_INIT)
        {
            if (cItem != null)
            {
                if (add && cItem.returnBoolValue(ContactListContactItem.VALUE_NO_AUTH))
                {

                    // Add length of TLVs and 0x066 packet
                    Util.putWord(buf, marker, 8 + nameRaw.length);
                    marker += 2;
                    Util.putWord(buf, marker, 0x0066);
                    marker += 2;
                    Util.putWord(buf, marker, 0x0000);
                    marker += 2;
                }
                else
                {
                    // Add only length of TLVs
                    Util.putWord(buf, marker, 4 + nameRaw.length);
                    marker += 2;
                }
                Util.putWord(buf, marker, 0x0131);
                Util.putWord(buf, marker + 2, nameRaw.length);
                System.arraycopy(nameRaw, 0, buf, marker + 4, nameRaw.length);
                marker += 4 + nameRaw.length;
            }
        } 
        else
        {
            if (cItem != null)
                Util.putWord(buf, marker, 4 + cItemsRemaining.size() * 2);
            else
                Util.putWord(buf, marker, 4 + gItems.length * 2 + 2);
            marker += 2;

            Util.putWord(buf, marker, 0x00C8);
            if (cItem != null)
                Util.putWord(buf, marker + 2, cItemsRemaining.size() * 2);
            else
                Util.putWord(buf, marker + 2, gItems.length * 2 + 2);
            marker += 4;

            if (cItem != null)
                for (int i = 0; i < cItemsRemaining.size(); i++)
                {
                    Util.putWord(buf, marker, ((ContactListContactItem) cItemsRemaining.elementAt(i)).getId());
                    marker += 2;
                }
            else
            {
                for (int i = 0; i < gItems.length; i++)
                {
                    Util.putWord(buf, marker, gItems[i].getId());
                    marker += 2;
                }
                Util.putWord(buf, marker, this.gItem.getId());
                marker += 2;
            }
        }
        return buf;
    }
}