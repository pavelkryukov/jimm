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
File: src/jimm/Options.java
Version: ###VERSION###  Date: ###DATE###
Author(s): Andreas Rossbacher
*******************************************************************************/

package jimm.comm;

import java.util.Date;

import jimm.Jimm;
import jimm.JimmException;
import jimm.Search;
import jimm.util.ResourceBundle;

public class SearchAction extends Action
{
    
    // States of search mission
    public static final int STATE_ERROR                  = -1;
    public static final int STATE_UIN_SEARCH_SENT        = 1;
    public static final int STATE_EMAIL_SEARCH_SENT      = 2;
    public static final int STATE_WHITEPAGES_SEARCH_SENT = 3;
    public static final int STATE_FIRSTRESULT_RECEIVED   = 4;
    public static final int STATE_LASTRESULT_RECEIVED    = 5;
    public static final int STATE_SEARCH_FINISHED        = 6;
    
    // TLVs used in LE format
    public static final int TLV_TYPE_UIN                 = 0x3601; // long (4 byte)
    public static final int TLV_TYPE_NICK                = 0x5401; // String (2 byte length + string)
    public static final int TLV_TYPE_FIRSTNAME           = 0x4001; // String (2 byte length + string)
    public static final int TLV_TYPE_LASTNAME            = 0x4A01; // String (2 byte length + string)
    public static final int TLV_TYPE_EMAIL               = 0x5E01; // String (2 byte length + string + 1 byte email code)
    public static final int TLV_TYPE_CITY                = 0x9A01; // String (2 byte length + string)
    public static final int TLV_TYPE_KEYWORD             = 0x2602; // String (2 byte length + string)
    public static final int TLV_TYPE_ONLYONLINE          = 0x3002; // UINT8 (1 byte:  1 - search online, 0 - search all)
    
    // Search action was called by
    public static final int CALLED_BY_SEARCHUSER         = 0;
    public static final int CALLED_BY_ADDUSER            = 1;
    
    // Timeout
    public static final int TIMEOUT = 10 * 1000; // milliseconds
    
    /****************************************************************************/

    // Action state
    private int state;
    
    // Search object as container for request and results
    private Search cont;
    
    // Search action was called by
    private int calledBy;
    
    // Last activity
    private Date lastActivity = new Date();
    
    // Excpetion handeled;
    private boolean handeld;
    
    public SearchAction(Search _cont,int _calledBy){
        cont = _cont;
        calledBy = _calledBy;
        handeld = false;
    }

    // Returns true if the action can be performed
    public boolean isExecutable()
    {
        return (this.icq.isConnected());
    }

    // Returns true if this is an exclusive command
    public boolean isExclusive()
    {
        return false;
    }
    
    // Init action
    protected void init() throws JimmException
    {
        // Snac packet
        ToIcqSrvPacket packet;
        
        // Marker for packet building
        int marker = 0;
        
        
        String[] search = cont.getSearchRequest();
        
        // Basic length is 2
        int length = 2;
        
        // Add 8 if there is an uin to search for
        if (search[0].length() != 0)
            length+= 8;
        
        // For each item which should be added to the search request 
        // we need 6 byte + length of the string + 1 zero byte for the end.
        for (int i = 1; i < 7; i++)
        {
            if (search[i].length() != 0) 
                length += 6 + search[i].length() + 1;
        }
        // Search offline/online TLV
        length+=5;
            
        byte[] buf = new byte[length];
        
        Util.putWord(buf,marker,0x5f05);
        marker+=2;
        
        byte[] tmp;
        
        if (search[0].length() != 0)
        {
            Util.putWord(buf, marker, TLV_TYPE_UIN);
            marker += 2;
            
            Util.putWord(buf, marker, 0x0004, false);
            marker += 2;
            
            Util.putDWord(buf, marker, Integer.parseInt(cont.getSearchRequest()[0]), false);
            marker += 4;
        }
        for (int i = 1; i < 7; i++)
        {
            if (search[i].length() != 0)
            {
                
                // Write T of TLV
                switch (i)
                {
                case 1:
                    Util.putWord(buf, marker, TLV_TYPE_NICK);
                    break;
                case 2:
                    Util.putWord(buf, marker, TLV_TYPE_FIRSTNAME);
                    break;
                case 3:
                    Util.putWord(buf, marker, TLV_TYPE_LASTNAME);
                    break;
                case 4:
                    Util.putWord(buf, marker, TLV_TYPE_EMAIL);
                    break;
                case 5:
                    Util.putWord(buf, marker, TLV_TYPE_CITY);
                    break;
                case 6:
                    Util.putWord(buf, marker, TLV_TYPE_KEYWORD);
                }
                marker += 2;
                
                // Create byte array from seach string
                tmp = Util.stringToByteArray(search[i]);

                // Write L of TLV
                Util.putWord(buf, marker, tmp.length +3, false);
                marker += 2;
                
                // Write V of TLV
                
                // First write the length of the string + 1 for zero byte
                Util.putWord(buf, marker, tmp.length + 1, false);
                marker += 2;
                // Second write the string
                System.arraycopy(tmp, 0, buf, marker, tmp.length);
                marker += tmp.length;
                // Third write zero byte
                Util.putByte(buf, marker, 0x00);
                marker += 1;
            }
        }
        Util.putWord(buf, marker, TLV_TYPE_ONLYONLINE);
        marker+=2;
        Util.putWord(buf,marker,1,false);
        marker+=2;
        if (search[7].equals("1"))
            Util.putByte(buf,marker,1);
        else
            Util.putByte(buf,marker,0);
        marker+=1;

        packet = new ToIcqSrvPacket(-1,SnacPacket.CLI_TOICQSRV_COMMAND,0x0002,Jimm.jimm.getIcqRef().getUin(),0x07D0,new byte[0], buf);
    
        this.icq.c.sendPacket(packet);
        
        this.state = STATE_UIN_SEARCH_SENT;
        
    }
    

    // Forwards received packet, returns true if packet was consumed
    protected synchronized boolean forward(Packet packet) throws JimmException
    {
        // Flag indicates whether packet has been consumed or not
        boolean consumed = false;

        if (this.state == STATE_UIN_SEARCH_SENT || this.state == STATE_FIRSTRESULT_RECEIVED)
        {
            // Watch out for SRV_FROMICQSRV packet type
            if (packet instanceof FromIcqSrvPacket)
            {
                FromIcqSrvPacket fromIcqServerPacket = (FromIcqSrvPacket) packet;

                    int marker = 0;
                    
                    byte[] data = fromIcqServerPacket.getData();

                    if (Util.getWord(data,marker) == 0xa401)
                        this.state = STATE_FIRSTRESULT_RECEIVED;
                    if (Util.getWord(data,marker) == 0xae01)
                        this.state = STATE_LASTRESULT_RECEIVED;
                    
                    marker += 2;
                    if (Util.getByte(data,marker)== 0x0A)
                    {
                        // Get UIN
                        marker += 3;
                        long uin = Util.getDWord(data,marker,false);
                        
                        // Get nick
                        marker += 4;
                        
                        String[] strings = new String[4];
                        
                        // Get the strings
                        // #0 Nick
                        // #1 Firstname
                        // #2 Lastname
                        // #3 EMail
                        for (int i=0;i<4;i++)
                        {
                            strings[i] = Util.byteArrayToString(data,marker+2,Util.getWord(data,marker,false));
                            marker += 2+Util.getWord(data,marker,false);
                        }
                        
                        // Get auth flag
                        String auth;
                        if (Util.getByte(data,marker) == 0)
                            auth = ResourceBundle.getString("req");
                        else
                            auth = ResourceBundle.getString("reqno");
                        marker += 1;
                        
                        // Get status
                        int status = Util.getWord(data,marker,false);
                        marker += 2;
                        
                        // Get gender
                        String gender;
                        switch (Util.getByte(data,marker)){
                        case 0: gender = ""; break;
                        case 1: gender = ResourceBundle.getString("female"); break;
                        case 2: gender = ResourceBundle.getString("male"); break;
                        default: gender = "";
                        }
                        marker += 1;
                        
                        // Get age
                        int age = Util.getWord(data,marker,false);
                        
                        cont.addResult(String.valueOf(uin),strings[0],strings[1]+" "+strings[2],strings[3],auth,status,gender,age);
                        
                        if (this.state == STATE_LASTRESULT_RECEIVED)
                        {
                            marker += 2;
                            long foundleft = Util.getDWord(data,marker,false);
                            System.out.println("foundleft: "+foundleft);
                            this.state = STATE_SEARCH_FINISHED;
                        }
                    }
                    else
                    {
                        this.state = ConnectAction.STATE_ERROR;
                    }
                }
            }
        
        // Update activity timestamp
        this.lastActivity = new Date();
        
        return consumed;
    }
    
    // Activates the result screen
    public void activateResult()
    {
    	this.cont.getSearchForm().activate(true);
    }
    
    // Activates the result screen
    public void activateSearchForm()
    {
    	this.cont.getSearchForm().activate(false);
    }
    
    // Return if exception already handeled
    public boolean excepHandled()
    {
    	return handeld;
    }

    // Returns true if the action is completed
    public boolean isCompleted()
    {
        return (this.state == STATE_SEARCH_FINISHED);
    }

    // Returns true if an error has occured
    public synchronized boolean isError()
    {
        if ((this.state != ConnectAction.STATE_ERROR) && (this.lastActivity.getTime() + SearchAction.TIMEOUT < System.currentTimeMillis()))
        {
            if (calledBy == CALLED_BY_SEARCHUSER)
            	cont.getSearchForm().activate(false);
            else if (calledBy == CALLED_BY_ADDUSER)
            	Jimm.display.setCurrent(Jimm.jimm.getMainMenuRef().addUser);
            Thread.yield();
            if (this.state == STATE_FIRSTRESULT_RECEIVED)
            {
            	JimmException.handleException(new JimmException(159, 0, true));
            	handeld = true;
            }
            else
            {
            	JimmException.handleException(new JimmException(159, 1, true));
            	handeld = true;
            }
            this.state = ConnectAction.STATE_ERROR;
        }
        return (this.state == ConnectAction.STATE_ERROR);
    }
}
