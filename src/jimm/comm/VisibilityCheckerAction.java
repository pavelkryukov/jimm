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

import jimm.Jimm;
import jimm.JimmException;
import jimm.ContactList;
import jimm.ContactListContactItem;
import jimm.JimmUI;
import jimm.util.ResourceBundle;
import javax.microedition.lcdui.*;
import DrawControls.*;
import jimm.Options;

public class VisibilityCheckerAction extends Action implements CommandListener
{
	protected byte[] uin;
	private String strUin;

	protected String nick;

	protected boolean completed;

	protected boolean showResult;

	protected long status;

	public VisibilityCheckerAction(
		String _uin,
		String _nick,
		boolean _showResult)
	{
		strUin = _uin;
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
		return !(Icq.isNotConnected());
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
						marker += 2;
						int tlvLen = Util.getWord(buf, marker);
						marker += 2;
						if ((tlvType == 0x0006) && (tlvLen == 4)) this.status = Util.translateStatusReceived(Util.getDWord(buf, marker));
						marker += tlvLen;
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
		if (this.completed)
		{
			if (this.showResult)
			{
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
				StringBuffer str_begin = new StringBuffer();
				str_begin.append(ResourceBundle.getString("status")).append(" ").append(this.nick).append(":\n");
				Image st_image = ContactList.getImageList().elementAt(ContactListContactItem.getStatusImageIndex(this.status));
				StringBuffer str_end = new StringBuffer();
				str_end.append(" (").append(ContactListContactItem.getStatusString(this.status)).append(")");
				results
						.addBigText(str_begin.toString(), Options.getSchemeColor(Options.CLRSCHHEME_TEXT), Font.STYLE_PLAIN, -1)
						.addImage(st_image, null, st_image.getWidth(), st_image.getHeight(), -1)
						.addBigText(str_end.toString(), Options.getSchemeColor(Options.CLRSCHHEME_TEXT), Font.STYLE_PLAIN, -1);
				results.addCommand(new Command("OK", Command.OK, 1));
				results.setCommandListener(this);
				Jimm.display.setCurrent(results);
				
				ContactList.update(strUin, this.status);
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