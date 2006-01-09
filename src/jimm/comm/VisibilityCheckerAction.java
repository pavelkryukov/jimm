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

public class VisibilityCheckerAction extends Action implements CommandListener {
    // #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
    // #sijapp cond.if modules_FILES is "true"#
    // DC variables
	//boolean statusChange = true;
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
	private ContactListContactItem item;
	private byte[] uinRaw;
	private boolean completed;
	private boolean showResult;
	private boolean statusReceived;
	private long status;
	public VisibilityCheckerAction(ContactListContactItem citem) {
		item = citem;
		showResult = true;
		uinRaw = Util.stringToByteArray(item.getStringValue(ContactListContactItem.CONTACTITEM_UIN));
	}
	public boolean isExecutable() {
		return Icq.isConnected();
	}
	public boolean isExclusive() {
		return false;
	}
	protected void init() throws JimmException {
		byte[] buf = new byte[5 + uinRaw.length];
		int marker = 0;
		Util.putDWord(buf, marker, 0x00000005);
		marker += 4;
		Util.putByte(buf, marker, uinRaw.length);
		marker += 1;
		System.arraycopy(uinRaw, 0, buf, marker, uinRaw.length);
		marker += uinRaw.length;
		Icq.c.sendPacket(new SnacPacket(0x0002, 0x0015, 0x00000005, new byte[0], buf));
	}

	protected boolean forward(Packet packet) throws JimmException
	{
		boolean consumed = false;
		int marker = 0;
		byte[] buf;
		if (!statusReceived) {
		if (packet instanceof SnacPacket)
		{
			SnacPacket p = (SnacPacket) packet;
			if ((p.getFamily() == 0x0002) && (p.getCommand() == 0x0006))
			{
					buf = p.getData();
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
									status = Util.translateStatusReceived(Util.getDWord(tlvData, 0));
								break;
							case 0x0005:
									capabilities = Util.parseCapabilities(item.getStringValue(ContactListContactItem.CONTACTITEM_UIN), tlvData);
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
									//statusChange = false;
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
				consumed = true;
			}
			else if ((p.getFamily() == 0x0002) && (p.getCommand() == 0x0001))
			{
					status = ContactList.STATUS_OFFLINE;
				consumed = true;
			}
				if (consumed) statusReceived = completed = true;
			}
		}
		return consumed;
	}
	public boolean isCompleted() {
		if (completed && showResult) {
				TextList results = new TextList(null);
				results.setCursorMode(TextList.SEL_NONE);
				JimmUI.setColorScheme(results);
				// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
				results.setTitle(ResourceBundle.getString("invisible_check"));
				results.setFullScreenMode(false);
				results.setFontSize(Font.SIZE_MEDIUM);
				//#sijapp cond.else#
				results.setCaption(ResourceBundle.getString("invisible_check"));
				results.setFontSize(Font.SIZE_SMALL);
				//#sijapp cond.end#
			String str_begin = ResourceBundle.getString("status") + " " + item.getStringValue(ContactListContactItem.CONTACTITEM_NAME) + ":\n";
				Image st_image = ContactList.getImageList().elementAt(ContactListContactItem.getStatusImageIndex(status));
				String str_end = " (" + ContactListContactItem.getStatusString(status) + ")";
				results
						.addBigText(str_begin, Options.getSchemeColor(Options.CLRSCHHEME_TEXT), Font.STYLE_PLAIN, -1)
						.addImage(st_image, null, st_image.getWidth(), st_image.getHeight(), -1)
						.addBigText(str_end, Options.getSchemeColor(Options.CLRSCHHEME_TEXT), Font.STYLE_PLAIN, -1);
				results.addCommand(new Command("OK", Command.OK, 1));
				results.setCommandListener(this);
				Jimm.display.setCurrent(results);
				// #sijapp cond.if (target="MIDP2" | target="MOTOROLA" | target="SIEMENS2") & modules_FILES="true"#
			//if (!statusChange)
				Util.detectUserClient(item.getStringValue(ContactListContactItem.CONTACTITEM_UIN), dwFT1, dwFT2, dwFT3,capabilities,icqProt);
			RunnableImpl.updateContactList(item.getStringValue(ContactListContactItem.CONTACTITEM_UIN), status, capabilities,internalIP, null, dcPort,dcType,icqProt,authCookie,signon,online,idle);
				// #sijapp cond.else#
			RunnableImpl.updateContactList(item.getStringValue(ContactListContactItem.CONTACTITEM_UIN), status, capabilities, null, null, 0, 0, 0, 0, signon, online, idle);
				// #sijapp cond.end#
		}
		return completed;
		}
	public long getStatus() {
		return status;
	}
	public boolean isError() {
		return false;
	}
	public void commandAction(Command c, Displayable d) {
		ContactList.activate();
	}
}