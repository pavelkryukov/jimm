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
 File: src/jimm/comm/DirectConnectionAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
// #sijapp cond.if modules_FILES is "true"#

package jimm.comm;

import java.util.Date;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;

import jimm.ContactList;
import jimm.ContactListContactItem;
import jimm.Jimm;
import jimm.JimmException;
import jimm.Options;
import jimm.util.ResourceBundle;

public class DirectConnectionAction extends Action
{

    // Action states (for filetransfer)
    public static final int STATE_ERROR = -1;
    public static final int STATE_CLI_FILE_INIT_DONE = 1;
    public static final int STATE_CLI_FILE_START_DONE = 2;
    public static final int STATE_CLI_FILE_SENT = 3;

    /****************************************************************************/

    // Current action state
    private int state;

    // File transfer object
    private FileTransferMessage ft;

    // Packet counter
    private int packets;
    
    // Timestamp
    private long timestamp;
    
    // Cancel bool
    private boolean cancel;

    // Constructor
    public DirectConnectionAction(FileTransferMessage _ft)
    {
    	super(true, true);
        this.ft = _ft;
        packets = 0;
        timestamp = 0;
        cancel = false;
    }

    // Init action
    protected void init() throws JimmException
    {

        // Make a new peer connection and connect to the adress and port we got from the FileTransferRequest
        Icq.peerC = icq.new PeerConnection();
        Icq.peerC.connect(Util.ipToString(ft.getRcvr().getIPValue(ContactListContactItem.CONTACTITEM_INTERNAL_IP)) + ":" + ft.getRcvr().getIntValue(ContactListContactItem.CONTACTITEM_DC_PORT));
        
        // Send a DC init packet
        byte[] dcpacket = new byte[48];

        int marker = 0;

        // Put command
        Util.putByte(dcpacket, marker, 0xff);
        marker++;

        // Put ICQ version in LE
        Util.putDWord(dcpacket, marker, 8, false);
        marker += 2;

        // Length of the following data
        Util.putWord(dcpacket, marker, 0x2b00);
        marker += 2;

        // UIN this packet ist sent to
        Util.putDWord(dcpacket, marker, Long.parseLong(ft.getRcvrUin()), false);
        marker += 4;

        // Unknown
        Util.putWord(dcpacket, marker, 0x0000);
        marker += 2;

        // port we are listening on
        Util.putDWord(dcpacket, marker, Icq.peerC.getLocalPort(), false);
        marker += 4;

        // UIN of the sender
        Util.putDWord(dcpacket, marker, Long.parseLong(Options.getString(Options.OPTION_UIN)), false);
        marker += 4;

        // our internal IP
        System.arraycopy(dcpacket, marker, Icq.peerC.getLocalIP(), 0, 4);
        marker += 4;

        // our external IP 
        Util.putDWord(dcpacket, marker, 0xa9fe0000);
        marker += 4;

        // TCP connection flags
        Util.putByte(dcpacket, marker, 0x04);
        marker++;

        // other (same) port we are listening on
        Util.putDWord(dcpacket, marker, Icq.peerC.getLocalPort(), false);
        marker += 4;

        // connection cookie
        Util.putDWord(dcpacket, marker, ft.getRcvr().getIntValue(ContactListContactItem.CONTACTITEM_AUTH_COOKIE), false);
        marker += 4;

        // some unknown stuff
        Util.putDWord(dcpacket, marker, 0x50000000);
        marker += 4;

        // some unknown stuff
        Util.putDWord(dcpacket, marker, 0x03000000);
        marker += 4;

        // some unknown stuff
        Util.putDWord(dcpacket, marker, 0x00000000);

        DCPacket initPacket = new DCPacket(dcpacket);
        Icq.peerC.sendPacket(initPacket);
    }

    // Forwards received packet, returns true if packet was consumed
    protected boolean forward(Packet packet) throws JimmException
    {
        boolean consumed = false;

        if (packet instanceof DCPacket)
        {
            // System.out.println("Got a dc packet");
            DCPacket dcPacket = (DCPacket) packet;

            byte[] buf = dcPacket.getDCContent();

            // System.out.println(Util.toHexString(buf));

            // Got peer ACK for DC
            if ((buf.length >= 4) && (Util.getDWord(buf, 0) == 0x01000000))
            {
                // System.out.println("Got a dc init ack packet");

                // We also have to ACK the connection
                buf = new byte[4];
                Util.putDWord(buf, 0, 0x01000000);

                DCPacket initPacket = new DCPacket(buf);
                Icq.peerC.sendPacket(initPacket);

                // System.out.println("Sent DC ACK");
                // System.out.println(Util.toHexString(buf));

                // And now we send out a file transfer init
                buf = new byte[17 + 2 + Options.getString(Options.OPTION_UIN).length() + 1];

                int marker = 0;

                // Put the command
                Util.putByte(buf, marker, 0x00);
                marker++;

                // Put unknown 4 zero bytes
                Util.putDWord(buf, marker, 0x00000000);
                marker += 4;

                // Put number of files we want to send (one only at the moment)
                Util.putDWord(buf, marker, 1, false);
                marker += 4;

                // if (this.ft == null) System.out.println("ft was null this cannot happen");

                // Put number of bytes which will be sent
                Util.putDWord(buf, marker, this.ft.getSize(), false);
                marker += 4;

                // Put speed (no pause)
                Util.putDWord(buf, marker, 64, false);
                marker += 4;

                // Put nick (or uin in this case)
                //First the size in one byte
                Util.putByte(buf, marker, Options.getString(Options.OPTION_UIN).length() + 1);
                // Then one zero byte
                Util.putByte(buf, marker + 1, 0x00);
                marker += 2;
                // Then the string itself
                byte[] nick = Util.stringToByteArray(Options.getString(Options.OPTION_UIN));
                System.arraycopy(nick, 0, buf, marker, nick.length);
                marker += nick.length;
                Util.putByte(buf, marker, 0x00);
                marker++;

                // System.out.println(Util.toHexString(buf));

                // Send the packet
                DCPacket initFTPacket = new DCPacket(buf);
                Icq.peerC.sendPacket(initFTPacket);
                // System.out.println("Sent FT init packet");

                this.state = STATE_CLI_FILE_INIT_DONE;

                consumed = true;
            } else if (this.state == STATE_CLI_FILE_INIT_DONE && (Util.getByte(buf, 0) == 0x01))
            {
                // System.out.println("Got peer ACK for FT");
                // System.out.println("Now start the transfer");

                buf = new byte[14 + 2 + this.ft.getFilename().length() + 1 + 3];

                int marker = 0;

                // Put the command
                Util.putByte(buf, marker, 0x02);
                marker++;

                // File or directory (we only do files)
                Util.putByte(buf, marker, 0x00);
                marker++;

                // Put the filename
                // First one byte with the length vollowed by one zero byte
                Util.putByte(buf, marker, this.ft.getFilename().length() + 1);
                Util.putByte(buf, marker + 1, 0x00);
                marker += 2;
                // Then put the name itself
                byte[] filename = Util.stringToByteArray(this.ft.getFilename());
                System.arraycopy(filename, 0, buf, marker, filename.length);
                marker += filename.length;
                Util.putByte(buf, marker, 0x00);
                marker++;

                // Put another string but it is all zero so it is three zero bytes
                Util.putWord(buf, marker, 0x0100);
                Util.putByte(buf, marker + 2, 0x00);
                marker += 3;

                // Put size of the file to transfer
                Util.putDWord(buf, marker, this.ft.getSize(), false);
                marker += 4;

                // Put 4 zero bytes
                Util.putDWord(buf, marker, 0);
                marker += 4;

                // Put the speed again
                Util.putDWord(buf, marker, 0x00000064, false);
                marker += 4;

                // Send the packet
                DCPacket startFTPacket = new DCPacket(buf);
                Icq.peerC.sendPacket(startFTPacket);
 
                this.state = STATE_CLI_FILE_START_DONE;

                consumed = true;
            } else if (this.state == STATE_CLI_FILE_START_DONE && (Util.getByte(buf, 0) == 0x03) && (Util.getByte(buf, 13) == 0x01))
            {

                // Variables needed for transfer
                DCPacket dataPacket;
                Date date = new Date();
                this.timestamp = date.getTime();
        
                // Send out the file in 2048 byte blocks
                while (ft.segmentAvail(packets) && !cancel)
                {
                	System.out.println("Continue...");
                    // Send the packet
                    dataPacket = new DCPacket(ft.getFileSegmentPacket(packets));
                    Icq.peerC.sendPacket(dataPacket);
                    packets++;
                }
                date = new Date();
                this.timestamp = date.getTime()-this.timestamp;
                

                try
                {
                    Thread.sleep(5000);
                }
                catch (Exception e) {}
                
                // Close the connection
                Icq.peerC.close();
                Thread.yield();
                Icq.peerC = null;

                ft.getRcvr().setFTM(null);

                // Connection closed 
                // System.out.println("File done/Conn closed");
                consumed = true;

                if (!cancel)
                    this.state = STATE_CLI_FILE_SENT;
            }
        }
        return (consumed);
    }

    // Returns a number between 0 and 100 (inclusive) which indicates the current progress
    public int getProgress()
    {
        int percent;
        try
        {
            percent = ((packets * 100) / (ft.getSize() >>> 11));
        }
        catch(Exception e)
        {
            percent = 0;
        }
        return (percent);
    }
    
    // Return the time the filetransfer took
    public String getSpeed()
    {
        if (this.timestamp != 0)
            return(String.valueOf(this.ft.getSize()/this.timestamp)+"."+String.valueOf((this.ft.getSize()%this.timestamp)/(this.timestamp/10)));
        else
            return("0.0");
    }

    // Returns true if the action is completed
    public boolean isCompleted()
    {
        return (this.state == STATE_CLI_FILE_SENT);
    }

    // Returns true if an error has occured
    public boolean isError()
    {        
        if ((this.state == STATE_ERROR) || this.cancel)
        {
            return (true);
        }
        else
            return (false);
    }
    
    public void onEvent(int eventType)
    {
    	switch (eventType)
    	{
    	case ON_CANCEL:
    		cancel = true;
			ContactList.activate();
    		break;
    		
    	case ON_ERROR:
			Alert err = new Alert(ResourceBundle.getString("filetransfer"), ResourceBundle.getString("filetransfer")+" "+ResourceBundle.getString("was")+" "+ResourceBundle.getString("not")+" "+ResourceBundle.getString("successful")+"!",null, AlertType.WARNING);
			Jimm.display.setCurrent(err, ContactList.getVisibleContactListRef());
    		break;
    		
    	case ON_COMPLETE:
			Alert ok = new Alert(ResourceBundle.getString("filetransfer"),ResourceBundle.getString("filetransfer")+" "+ResourceBundle.getString("was")+" "+ResourceBundle.getString("successful")+".\n"+ResourceBundle.getString("speed")+": "+getSpeed()+" "+ResourceBundle.getString("kbs"),null, AlertType.INFO);
			ok.setTimeout(2000);
			Jimm.display.setCurrent(ok, ContactList.getVisibleContactListRef());
    		break;
    	}
    }

}

//#sijapp cond.end#
//#sijapp cond.end#