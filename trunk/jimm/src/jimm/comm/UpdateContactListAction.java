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
 File: src/jimm/comm/UpdateContactListAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/

package jimm.comm;

import java.util.Date;
import java.util.Vector;

import jimm.ContactListContactItem;
import jimm.ContactListGroupItem;
import jimm.Jimm;
import jimm.JimmException;

public class UpdateContactListAction extends Action
{

    // Action states
    public static final int STATE_ERROR = -1;
    public static final int STATE_CLI_ROSTERDELETE_SENT = 1;
    public static final int STATE_CLI_ROSTERADD_SENT = 2;
    public static final int STATE_CLI_ADDEND_SENT = 3;
    public static final int STATE_SRV_UPDATEACK_RCVD = 4;
    public static final int STATE_SRV_REPLYED_AUTH = 5;
    

    // Timeout
    public static final int TIMEOUT = 10 * 1000; // milliseconds

    /** ************************************************************************* */

    // Contact item
    private ContactListContactItem cItem;

    // Action state
    private int state;

    // Action type
    private boolean add;
    
    // Type of error happend
    private int error;

    // Last activity
    private Date lastActivity = new Date();

    // Constructor (removes given contact item)
    public UpdateContactListAction(ContactListContactItem cItem, boolean add)
    {
        this.add = add;
        this.cItem = cItem;
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

        error = 0;
        int marker = 0;
        
        SnacPacket packet;
        
        byte[] uinRaw = Util.stringToByteArray(this.cItem.getUin());
        byte[] nameRaw = Util.stringToByteArray(this.cItem.getName());
        
        byte[] buf = new byte[1+uinRaw.length];
        
        Util.putByte(buf, marker, uinRaw.length);
        System.arraycopy(uinRaw, 0, buf, 1, uinRaw.length);
        
        if (!add)
            packet = new SnacPacket(SnacPacket.CLI_BUDDYLIST_REMOVE_FAMILY, SnacPacket.CLI_BUDDYLIST_REMOVE_COMMAND,SnacPacket.CLI_BUDDYLIST_REMOVE_COMMAND, new byte[0], buf);
        else	
            packet = new SnacPacket(SnacPacket.CLI_BUDDYLIST_ADD_FAMILY, SnacPacket.CLI_BUDDYLIST_ADD_COMMAND,SnacPacket.CLI_BUDDYLIST_ADD_COMMAND, new byte[0], buf);
        
        this.icq.c.sendPacket(packet);
        
        // Send a CLI_ADDSTART packet
        packet = new SnacPacket(SnacPacket.CLI_ADDSTART_FAMILY, SnacPacket.CLI_ADDSTART_COMMAND,SnacPacket.CLI_ADDSTART_COMMAND, new byte[0], new byte[0]);

        this.icq.c.sendPacket(packet);

        if (!add)
        {
            
            // Pack CLI_ROSTERDELETE packet
            System.out.println("Pack CLI_ROSTERDELETEpacket");

            buf = new byte[2 + uinRaw.length + 8 + 4 + nameRaw.length];
            
            marker = 0;
            
            Util.putWord(buf, marker, uinRaw.length);
            System.arraycopy(uinRaw, 0, buf, marker + 2, uinRaw.length);
            marker += 2 + uinRaw.length;
            Util.putWord(buf, marker, this.cItem.getGroup());
            marker += 2;
            Util.putWord(buf, marker, this.cItem.getId());
            marker += 2;
            Util.putWord(buf, marker, 0x0000);
            marker += 2;
            Util.putWord(buf, marker, 4 + nameRaw.length);
            marker += 2;
            Util.putWord(buf, marker, 0x0131);
            Util.putWord(buf, marker + 2, nameRaw.length);
            System.arraycopy(nameRaw, 0, buf, marker + 4, nameRaw.length);
            marker += 4 + nameRaw.length;
            // Send a CLI_ROSTERDELETE packet
            packet = new SnacPacket(SnacPacket.CLI_ROSTERDELETE_FAMILY, SnacPacket.CLI_ROSTERDELETE_COMMAND,
                    SnacPacket.CLI_ROSTERDELETE_COMMAND, new byte[0], buf);
        } else
        {
            // Pack CLI_ROSTERADDpacket
            System.out.println("Pack CLI_ROSTERADDpacket");
            
            if (cItem.noAuth())
                buf = new byte[2 + uinRaw.length + 6 + 6 + 4 + nameRaw.length];
            else
                buf = new byte[2 + uinRaw.length + 6 + 2 + 4 + nameRaw.length];
            
            marker = 0;
            
            Util.putWord(buf, marker, uinRaw.length);
            System.arraycopy(uinRaw, 0, buf, marker + 2, uinRaw.length);
            marker += 2 + uinRaw.length;
            Util.putWord(buf, marker, this.cItem.getGroup());
            marker += 2;
            Util.putWord(buf, marker, this.cItem.getId());
            marker += 2;
            Util.putWord(buf, marker, 0x0000);
            marker += 2;
            if (cItem.noAuth())
            {
                // Add length of TLVs and 0x066 packet
                Util.putWord(buf, marker, 8 + nameRaw.length);
                marker += 2;

                Util.putWord(buf, marker, 0x0066);
                marker += 2;
                Util.putWord(buf, marker, 0x0000);
                marker += 2;
            } else
            {
                // Add only length of TLVs
                Util.putWord(buf, marker, 4 + nameRaw.length);
                marker += 2;
            }
            Util.putWord(buf, marker, 0x0131);
            marker += 2;
            Util.putWord(buf, marker, nameRaw.length);
            marker += 2;
            System.arraycopy(nameRaw, 0, buf, marker, nameRaw.length);

            // Send CLI_ROSTERADDpacket
            packet = new SnacPacket(SnacPacket.CLI_ROSTERADD_FAMILY, SnacPacket.CLI_ROSTERADD_COMMAND, SnacPacket.CLI_ROSTERADD_COMMAND,
                    new byte[0], buf);
        }
        this.icq.c.sendPacket(packet);

        // Set state
        if (!add)
            this.state = UpdateContactListAction.STATE_CLI_ROSTERDELETE_SENT;
        else
            this.state = UpdateContactListAction.STATE_CLI_ROSTERADD_SENT;

    }

    // Forwards received packet, returns true if packet was consumed
    protected boolean forward(Packet packet) throws JimmException
    {

        // Catch JimmExceptions
        try
        {

            // Flag indicates whether packet has been consumed or not
            boolean consumed = false;

            // Watch out for STATE_CLI_ROSTERDELETE_SENT or
            // STATE_CLI_ROSTERADD_SENT
            if (this.state == UpdateContactListAction.STATE_CLI_ROSTERDELETE_SENT
                    || this.state == UpdateContactListAction.STATE_CLI_ROSTERADD_SENT)
            {

                // Watch out for SRV_UPDATEACK packet type
                if (packet instanceof SnacPacket)
                {
                    SnacPacket snacPacket = (SnacPacket) packet;
                    if ((snacPacket.getFamily() == SnacPacket.SRV_UPDATEACK_FAMILY)
                            && (snacPacket.getCommand() == SnacPacket.SRV_UPDATEACK_COMMAND))
                    {

                        // Check error code, see ICQv8 specification
                        System.out.println("DEL/ADD ACC Rep: 0x"+ Integer.toHexString(Util.getWord(snacPacket.getData(), 0)));               
                        SnacPacket packet2;
                        switch (Util.getWord(snacPacket.getData(), 0)){
                        
                        case 0x002:
                            System.out.println("CLI_ADDEND");
                            // Send a CLI_ADDEND packet
                            packet2 = new SnacPacket(SnacPacket.CLI_ADDEND_FAMILY,
                                    SnacPacket.CLI_ADDEND_COMMAND,SnacPacket.CLI_ADDEND_COMMAND, new byte[0], new byte[0]);
                            this.icq.c.sendPacket(packet2);
                            error = 2;
                            this.state = ConnectAction.STATE_ERROR;
                            throw (new JimmException(154, 0, true));
                        case 0x003:
                            System.out.println("CLI_ADDEND");
                            // Send a CLI_ADDEND packet
                            packet2 = new SnacPacket(SnacPacket.CLI_ADDEND_FAMILY,
                                    SnacPacket.CLI_ADDEND_COMMAND,SnacPacket.CLI_ADDEND_COMMAND, new byte[0], new byte[0]);
                            this.icq.c.sendPacket(packet2);
                            error = 3;
                            this.state = ConnectAction.STATE_ERROR;
                            throw (new JimmException(155, 0, true));
                            
                        case 0x00A:
                            System.out.println("CLI_ADDEND");
                            // Send a CLI_ADDEND packet
                            packet2 = new SnacPacket(SnacPacket.CLI_ADDEND_FAMILY,
                                    SnacPacket.CLI_ADDEND_COMMAND,SnacPacket.CLI_ADDEND_COMMAND, new byte[0], new byte[0]);
                            this.icq.c.sendPacket(packet2);
                            error = 10;
                            this.state = ConnectAction.STATE_ERROR;
                            throw (new JimmException(156, 0, true));
                            
                        case 0x00C:
                            System.out.println("CLI_ADDEND");
                            // Send a CLI_ADDEND packet
                            packet2 = new SnacPacket(SnacPacket.CLI_ADDEND_FAMILY,
                                    SnacPacket.CLI_ADDEND_COMMAND,SnacPacket.CLI_ADDEND_COMMAND, new byte[0], new byte[0]);
                            this.icq.c.sendPacket(packet2);
                            error = 12;
                            this.state = ConnectAction.STATE_ERROR;
                            throw (new JimmException(157, 0, true));
                            
                        case 0x00D:
                            System.out.println("CLI_ADDEND");
                            // Send a CLI_ADDEND packet
                            packet2 = new SnacPacket(SnacPacket.CLI_ADDEND_FAMILY,
                                    SnacPacket.CLI_ADDEND_COMMAND,SnacPacket.CLI_ADDEND_COMMAND, new byte[0], new byte[0]);
                            this.icq.c.sendPacket(packet2);
                            error = 13;
                            this.state = ConnectAction.STATE_ERROR;
                            throw (new JimmException(158, 0, true));
                            
                        case 0x00E:
                            cItem.setNoAuth(true);
                            System.out.println("Added");
                            Jimm.jimm.getContactListRef().addContactItem(this.cItem);
                            this.state = UpdateContactListAction.STATE_SRV_REPLYED_AUTH;
                            break;
                        
                        default:
                            cItem.setTemporary(false);

                        // Get all contact items and group items as aray
                        ContactListContactItem[] cItems = Jimm.jimm.getContactListRef().getContactItems();
                        ContactListGroupItem[] gItems = Jimm.jimm.getContactListRef().getGroupItems();

                        // Get group of contact item to be removed or added to
                        ContactListGroupItem gItem = null;
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
                        // Either this is the whole group plus the new item
                        // (adding or
                        // it is the old list without the to be removed item
                        Vector cItemsRemaining = new Vector();

                        for (int i = 0; i < cItems.length; i++)
                        {
                            if ((gItem.getId() == cItems[i].getGroup())
                                    && ((this.cItem != cItems[i]) || cItem.isTemporary()))
                            {
                                cItemsRemaining.addElement(cItems[i]);
                            }
                        }
                        if (cItem.isTemporary())
                        {
                            cItemsRemaining.addElement(cItem);
                        }

                        // Pack CLI_ROSTERUPDATE packet
                        byte[] nameRaw = Util.stringToByteArray(gItem.getName());
                        byte[] buf = new byte[2 + nameRaw.length + 8 + 4 + cItemsRemaining.size() * 2];
                        int marker = 0;
                        Util.putWord(buf, marker, nameRaw.length);
                        System.arraycopy(nameRaw, 0, buf, marker + 2, nameRaw.length);
                        marker += 2 + nameRaw.length;
                        Util.putWord(buf, marker, gItem.getId());
                        marker += 2;
                        Util.putWord(buf, marker, 0x0000);
                        marker += 2;
                        Util.putWord(buf, marker, 0x0001);
                        marker += 2;
                        Util.putWord(buf, marker, 4 + cItemsRemaining.size() * 2);
                        marker += 2;
                        Util.putWord(buf, marker, 0x00C8);
                        Util.putWord(buf, marker + 2, cItemsRemaining.size() * 2);
                        marker += 4;
                        for (int i = 0; i < cItemsRemaining.size(); i++)
                        {
                            Util.putWord(buf, marker, ((ContactListContactItem) cItemsRemaining.elementAt(i)).getId());
                            marker += 2;
                        }

                        System.out.println("Send CLI_ROSTERUPDATE");
                        // Send CLI_ROSTERUPDATE packet
                        SnacPacket packet1 = new SnacPacket(SnacPacket.CLI_ROSTERUPDATE_FAMILY,
                                SnacPacket.CLI_ROSTERUPDATE_COMMAND,SnacPacket.CLI_ROSTERUPDATE_COMMAND, new byte[0], buf);
                        this.icq.c.sendPacket(packet1);

                        // Move to next state
                        this.state = UpdateContactListAction.STATE_CLI_ADDEND_SENT;
                        }
                        
                        System.out.println("CLI_ADDEND");
                        // Send a CLI_ADDEND packet
                        packet2 = new SnacPacket(SnacPacket.CLI_ADDEND_FAMILY,
                                SnacPacket.CLI_ADDEND_COMMAND,SnacPacket.CLI_ADDEND_COMMAND, new byte[0], new byte[0]);
                        this.icq.c.sendPacket(packet2);
                                                    
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
                    if ((snacPacket.getFamily() == SnacPacket.SRV_UPDATEACK_FAMILY)
                            && (snacPacket.getCommand() == SnacPacket.SRV_UPDATEACK_COMMAND))
                    {

                        // Check error code, see ICQv8 specification
                        System.out.println("UPDATE ACC Rep: 0x"
                                + Integer.toHexString(Util.getWord(snacPacket.getData(), 0)));
                        if (Util.getWord(snacPacket.getData(), 0) != 0x0000) { throw (new JimmException(154, 0, true)); }

                        // Delete or add contact item from internal list
                        if (!add)
                        {
                            System.out.println("Removed");
                            Jimm.jimm.getContactListRef().removeContactItem(this.cItem);
                        } else
                        {
                            System.out.println("Added");
                            Jimm.jimm.getContactListRef().addContactItem(this.cItem);
                        }

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

            // Update activity timestamp
            this.lastActivity = new Date();

            // Set error state if exception is critical
            if (e.isCritical()) this.state = ConnectAction.STATE_ERROR;

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

}