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
File: src/jimm/comm/NewUinRegistrationAction.java
Version: ###VERSION###  Date: ###DATE###
Author(s): Andreas Rossbacher
*******************************************************************************/

package jimm.comm;

import java.util.Date;
import java.util.Random;

import jimm.JimmException;

public class NewUinRegistrationAction extends Action
{

   // Action states
   public static final int STATE_ERROR = -1;
   public static final int STATE_INIT_DONE = 1;
   public static final int STATE_CLI_REGISTRATION_REQUEST_SENT = 2;
   public static final int STATE_SRV_NEW_UIN_RCV  = 3;   

   // Timeout
   public static final int TIMEOUT = 10 * 1000; // milliseconds

   /** ************************************************************************* */

   // Action state
   private int state;

   // The password that will be used for the new UIN
   private String password;
   
   // Server host
   private String srvHost;

   // Server port
   private String srvPort;
   
   // The registration cookie
   private int regCookie;
   
   // The uin we got from the Server
   private String uinStr;
   
   // Last activity
   private Date lastActivity = new Date();
   
   // Constructor (requests a new uin)
   public NewUinRegistrationAction(String password, String srvHost, String srvPort)
   {
       this.srvHost = srvHost;
       this.srvPort = srvPort;
       this.password = password;
       this.uinStr = "";
   }

   // Returns true if the action can be performed
   public boolean isExecutable()
   {
       return (this.icq.isNotConnected());
   }

   // Returns true if this is an exclusive command
   public boolean isExclusive()
   {
       return (true);
   }
   
   // Retrun the UIN we got from the Server
   public String getUIN()
   {
       return(this.uinStr);
   }

   // Init action
   protected void init() throws JimmException
   {
           
       // Check parameters
       if (this.password.length() == 0)
       {
           this.state = NewUinRegistrationAction.STATE_ERROR;
           throw (new JimmException(117, 0));
       }

       // Open connection
       try
       {
           this.icq.c.connect(this.srvHost, this.srvPort);
       } catch (JimmException e)
       {
           this.state = NewUinRegistrationAction.STATE_ERROR;
           throw (e);
       }

       // Set STATE_INIT
       this.state = NewUinRegistrationAction.STATE_INIT_DONE;

       // Update activity timestamp
       this.lastActivity = new Date();    
       


   }

   // Forwards received packet, returns true if packet was consumed
    protected synchronized boolean forward(Packet packet) throws JimmException
    {

        // Catch JimmExceptions
        try
        {
            // Flag indicates whether packet has been consumed or not
            boolean consumed = false;

            // Watch out for STATE_INIT_DONE
            if (this.state == NewUinRegistrationAction.STATE_INIT_DONE)
            {
                // Watch out for SRV_CLI_HELLO packet
                if (packet instanceof ConnectPacket)
                {
                    ConnectPacket connectPacket = (ConnectPacket) packet;
                    if (connectPacket.getType() == ConnectPacket.SRV_CLI_HELLO)
                    {
                        // Send a CLI_REGISTRATION_REQUEST as reply

                        System.out.println("Pack CLI_REGISTRATION_REQUEST");

                        SnacPacket request;

                        byte[] paswdRaw = Util.stringToByteArray(this.password);

                        Random rand = new Random(System.currentTimeMillis());
                        regCookie = rand.nextInt();

                        // Byte array for the packet
                        int bufLength = 2 + 2 + 4 + 2 + 2 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 2 + paswdRaw.length + 4 + 4 + 2;
                        byte[] buf = new byte[bufLength];

                        int marker = 0;

                        //TLV Type and length
                        Util.putWord(buf, marker, 0x001);
                        Util.putWord(buf, marker + 2, bufLength - 4);
                        marker += 4;

                        Util.putDWord(buf, marker, 0x00000000);
                        marker += 4;

                        Util.putWord(buf, marker, 0x2800);
                        Util.putWord(buf, marker + 2, 0x0300);
                        marker += 4;

                        Util.putDWord(buf, marker, 0x00000000);
                        Util.putDWord(buf, marker + 4, 0x00000000);
                        marker += 8;

                        Util.putDWord(buf, marker, regCookie);
                        Util.putDWord(buf, marker + 4, regCookie);
                        marker += 8;

                        Util.putDWord(buf, marker, 0x00000000);
                        Util.putDWord(buf, marker + 4, 0x00000000);
                        Util.putDWord(buf, marker + 8, 0x00000000);
                        Util.putDWord(buf, marker + 12, 0x00000000);
                        marker += 12;

                        Util.putWord(buf, marker, paswdRaw.length, false);
                        System.arraycopy(paswdRaw, 0, buf, marker + 2, paswdRaw.length);
                        marker += 2 + paswdRaw.length;

                        Util.putDWord(buf, marker, regCookie);
                        marker += 4;

                        Util.putDWord(buf, marker, 0x00000000);
                        marker += 4;

                        Util.putWord(buf, marker, rand.nextInt());

                        // Send a CLI_REGISTRATION_REQUEST packet
                        request = new SnacPacket(SnacPacket.CLI_REGISTERUSER_FAMILY,
                                SnacPacket.CLI_REGISTERUSER_COMMAND, SnacPacket.CLI_REGISTERUSER_COMMAND, new byte[0],
                                buf);

                        this.icq.c.sendPacket(request);

                        // Move to next state
                        this.state = NewUinRegistrationAction.STATE_CLI_REGISTRATION_REQUEST_SENT;

                        // Packet has been consumed
                        consumed = true;

                    }
                }

            }
            // Watch out for SRV_NEW_UIN
            else if (this.state == NewUinRegistrationAction.STATE_CLI_REGISTRATION_REQUEST_SENT)
            {
                // Watch out for SRV_UPDATEACK packet type
                if (packet instanceof SnacPacket)
                {
                    SnacPacket snacPacket = (SnacPacket) packet;
                    if ((snacPacket.getFamily() == SnacPacket.SRV_NEWUIN_FAMILY)
                            && (snacPacket.getCommand() == SnacPacket.SRV_NEWUIN_COMMAND))
                    {
                        // Get data
                        byte[] buf = snacPacket.getData();

                        // Read the important data from the packet
                        int marker = 0;
                        if (Util.getWord(buf, marker) != 0x001) throw (new JimmException(170, 0, true));
                        marker += 10;
                        if (Util.getWord(buf, marker) != 0x2d00) throw (new JimmException(170, 1, true));
                        if (Util.getWord(buf, marker + 2) != 0x3000) throw (new JimmException(170, 2, true));
                        marker += 4 + 12;
                        
                        // Look if they sent back our cookie, to be sure we got the right UIN
                        if (Util.getDWord(buf, marker) != regCookie) throw (new JimmException(170, 3, true));
                        if (Util.getDWord(buf, marker + 24) != regCookie) throw (new JimmException(170, 4, true));
                        marker += 20;
                        
                        // Get the UIN as a long in Little Endian and save it in a string
                        long uin = Util.getDWord(buf, marker, false);

                        this.uinStr = String.valueOf(uin);
                        
                        // Disconnect from Server
                        this.icq.reset();
                        
                        consumed = true;
                        
                        this.state = NewUinRegistrationAction.STATE_SRV_NEW_UIN_RCV;

                    } else if ((snacPacket.getFamily() == SnacPacket.SRV_REGREFUSED_FAMILY)
                            && (snacPacket.getCommand() == SnacPacket.SRV_REGREFUSED_COMMAND)) 
                    { 
                        consumed = true;
                        this.state = NewUinRegistrationAction.STATE_ERROR;
                        throw (new JimmException(171, 0, true)); 
                    }

                }
            }

            // Return consumption flag
            return (consumed);
        }
        // Catch JimmExceptions
        catch (JimmException e)
        {

            // Forward exception
            throw (e);

        }

    }
   
   // Returns true if the action is completed
   public boolean isCompleted()
   {
       return (this.state == NewUinRegistrationAction.STATE_SRV_NEW_UIN_RCV);
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