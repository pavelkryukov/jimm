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
 File: src/jimm/comm/VisibilityCheckerAction.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andrey Kazakov
 *******************************************************************************/

package jimm.comm;

import jimm.util.ResourceBundle;
import javax.microedition.lcdui.*;
import DrawControls.*;
import jimm.*;

public class VisibilityCheckerAction extends Action implements CommandListener
{
    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    // #sijapp cond.if modules_FILES is "true"#
    // DC variables
	boolean statusChange = true;
	byte[] tmpCaps;
    byte[] internalIP = new byte[4];
    long dcPort = 0;
    int dcType = -1;
    int icqProt = 0;
    long authCookie = 0;
    // #sijapp cond.end#
    // #sijapp cond.end#
    
    int dwFT1=0, dwFT2=0, dwFT3=0;
    int capabilities = 0;
    
    int idle = -1;
    long online = -1;
    long signon = -1;
    
	private byte[] uin;
	private String nick;
	private boolean completed;
	private boolean showResult;
	private long status;

	public VisibilityCheckerAction(
		String _uin,
		String _nick,
		boolean _showResult)
	{
		this.uin = Util.stringToByteArray(_uin);
		this.nick = _nick;
		this.showResult = _showResult;
	}

	public VisibilityCheckerAction(String _uin, String _nick)
	{
		this(_uin, _nick, true);
	}

	public VisibilityCheckerAction(String _uin)
	{
		this(_uin, _uin, true);
	}

	public boolean isExecutable()
	{
		return Icq.isConnected();
	}

	public boolean isExclusive()
	{
		return false;
	}

	protected void init() throws JimmException
	{
		byte[] buf = new byte[5 + this.uin.length];
		int marker = 0;
		Util.putDWord(buf, marker, 0x00000005);
		marker += 4;
		Util.putByte(buf, marker, this.uin.length);
		marker += 1;
		System.arraycopy(this.uin, 0, buf, marker, this.uin.length);
		marker += this.uin.length;
		Jimm.jimm.getIcqRef().c.sendPacket(new SnacPacket(0x0002, 0x0015, 0x00000005, new byte[0], buf));
	}

	protected boolean forward(Packet packet) throws JimmException
	{
		boolean consumed = false;
		if (packet instanceof SnacPacket)
		{
			SnacPacket p = (SnacPacket) packet;
			if ((p.getFamily() == 0x0002) && (p.getCommand() == 0x0006))
			{
				byte[] buf = p.getData();
				int marker = 0;
				marker += Util.getByte(buf, marker) + 3;
				int tlvCount = Util.getWord(buf, marker);
				marker += 2;
				for (int i = 0; i < tlvCount; i++)
				{
					try
					{
						int tlvType = Util.getWord(buf, marker);
						byte[] tlvData = Util.getTlv(buf, marker);
						marker += 2 + 2 + tlvData.length;
						switch (tlvType) {
							case 0x0006:
								this.status = Util.translateStatusReceived(Util.getDWord(tlvData, 0));
								break;
							case 0x0005:
								capabilities = Util.parseCapabilities(new String(uin), tlvData);
								break;
							// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
							// #sijapp cond.if modules_FILES is "true"#
							case 0x000c:
		                        // dcMarker
		                        int dcMarker = 0;
		                        
		                        // Get internal IP
		                        System.arraycopy(tlvData,dcMarker,internalIP,0,4);
		                        dcMarker += 4;
		                        
		                        // Get tcp port
		                        dcPort = Util.getDWord(tlvData,dcMarker);
		                        dcMarker += 4;
		                        
		                        // Get DC type
		                        dcType = Util.getByte(tlvData,dcMarker);
		                        dcMarker ++;
		                        
		                        // Get protocol version
		                        icqProt = Util.getWord(tlvData,dcMarker);
		                        dcMarker += 2;
		                        
		                        // Get auth cookie
		                        authCookie = Util.getDWord(tlvData,dcMarker);
		                        dcMarker +=12;
		                        
		                        // Get data for client detection
		                        dwFT1 = (int) Util.getDWord(tlvData,dcMarker);
		                        dcMarker += 4;
		                        dwFT2 = (int) Util.getDWord(tlvData,dcMarker);
		                        dcMarker += 4;
		                        dwFT3 = (int) Util.getDWord(tlvData,dcMarker);
		                        statusChange = false;
								break;
							// #sijapp cond.end#
							// #sijapp cond.end#
							case 0x0003:
								signon = Util.byteArrayToLong(tlvData); //signon time
								break;
							case 0x000f:
								online = Util.byteArrayToLong(tlvData); //online time
								break;
						}
					}
					catch (Exception e)
					{
						break;
					}
				}
				this.completed = true;
				consumed = true;
			}
			else if ((p.getFamily() == 0x0002) && (p.getCommand() == 0x0001))
			{
				this.completed = true;
				this.status = ContactList.STATUS_OFFLINE;
				consumed = true;
			}
		}
		return consumed;
	}

	public boolean isCompleted()
	{
		if (completed)
		{
			if (showResult)
			{
				TextList results = new TextList(null);
				results.setCursorMode(TextList.SEL_NONE);
				JimmUI.setColorScheme(results);
				String uin = new String(this.uin);
				// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
				results.setTitle(ResourceBundle.getString("invisible_check"));
				results.setFullScreenMode(false);
				results.setFontSize(Font.SIZE_MEDIUM);
				//#sijapp cond.else#
				results.setCaption(ResourceBundle.getString("invisible_check"));
				results.setFontSize(Font.SIZE_SMALL);
				//#sijapp cond.end#
				String str_begin = ResourceBundle.getString("status") + " " + nick + ":\n";
				Image st_image = ContactList.getImageList().elementAt(ContactListContactItem.getStatusImageIndex(status));
				String str_end = " (" + ContactListContactItem.getStatusString(status) + ")";
				results
						.addBigText(str_begin, Options.getSchemeColor(Options.CLRSCHHEME_TEXT), Font.STYLE_PLAIN, -1)
						.addImage(st_image, null, st_image.getWidth(), st_image.getHeight(), -1)
						.addBigText(str_end, Options.getSchemeColor(Options.CLRSCHHEME_TEXT), Font.STYLE_PLAIN, -1);
				results.addCommand(new Command("OK", Command.OK, 1));
				results.setCommandListener(this);
				Jimm.display.setCurrent(results);
				// Update contact list
				if (status != ContactList.STATUS_OFFLINE)
					ContactList.incOnlineCount();
				// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
				// #sijapp cond.if modules_FILES is "true"#
				if ( !statusChange )
					Util.detectUserClient(uin, dwFT1, dwFT2, dwFT3,capabilities,icqProt);
				ContactList.update(uin, status, capabilities,internalIP,dcPort,dcType,icqProt,authCookie,signon,online,idle);
				// #sijapp cond.else#
				ContactList.update(uin, status, capabilities,signon,online,idle);
				// #sijapp cond.end#
				// #sijapp cond.else#
				ContactList.update(uin, status, capabilities,signon,online,idle);
				// #sijapp cond.end#

			}
			return true;
		}
		else
		{
			return false;
		}
	}

	public long getStatus()
	{
		return this.status;
	}

	public boolean isError()
	{
		return false;
	}

	public void commandAction(Command c, Displayable d)
	{
		ContactList.activate();
	}
}