/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-07  Jimm Project

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
 File: src/jimm/JimmUI.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Igor Palkin
 *******************************************************************************/
package jimm;

import java.io.ByteArrayOutputStream;
import javax.microedition.lcdui.*;

import jimm.comm.Icq;
import jimm.comm.SaveInfoAction;
import jimm.comm.ToIcqSrvPacket;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

public class EditInfo extends Form implements CommandListener
{
	private TextField _NickNameItem = new TextField(ResourceBundle
			.getString("nick"), null, 15, TextField.ANY);

	private TextField _FirstNameItem = new TextField(ResourceBundle
			.getString("firstname"), null, 15, TextField.ANY);

	private TextField _LastNameItem = new TextField(ResourceBundle
			.getString("lastname"), null, 15, TextField.ANY);

	private TextField _EmailItem = new TextField(ResourceBundle
			.getString("email"), null, 50, TextField.EMAILADDR);

	private TextField _BdayItem = new TextField(ResourceBundle
			.getString("birth_day"), null, 15, TextField.ANY);

	private TextField _CityItem = new TextField(ResourceBundle
			.getString("city"), null, 15, TextField.ANY);

	private ChoiceGroup _SexItem = new ChoiceGroup(ResourceBundle
			.getString("gender"), Choice.EXCLUSIVE);

	private Command _CmdCancel = new Command(
			ResourceBundle.getString("cancel"), Command.CANCEL, 0);

	private Command _CmdSave = new Command(ResourceBundle.getString("save"),
			Command.OK, 1);

	private Displayable _PreviousForm;

	private static String[] userInfo;

	public EditInfo(Displayable currentForm)
	{
		super(ResourceBundle.getString("editform"));
		_PreviousForm = currentForm;
		_SexItem.append(ResourceBundle.getString("female"), null);
		_SexItem.append(ResourceBundle.getString("male"), null);
		append(_NickNameItem);
		append(_FirstNameItem);
		append(_LastNameItem);
		append(_SexItem);
		append(_EmailItem);
		append(_BdayItem);
		append(_CityItem);
		addCommand(_CmdSave);
		addCommand(_CmdCancel);
		setCommandListener(this);
	}

	public static void showEditForm(String[] userInfo, Displayable previousForm)
	{
		EditInfo.userInfo = userInfo;
		EditInfo editInfoForm = new EditInfo(previousForm);
		editInfoForm._SexItem.setSelectedIndex(Util
				.stringToGender(userInfo[JimmUI.UI_GENDER]) - 1, true);
		editInfoForm._NickNameItem.setString(userInfo[JimmUI.UI_NICK]);
		editInfoForm._EmailItem.setString(userInfo[JimmUI.UI_EMAIL]);
		editInfoForm._BdayItem.setString(userInfo[JimmUI.UI_BDAY]);
		editInfoForm._FirstNameItem.setString(userInfo[JimmUI.UI_FIRST_NAME]);
		editInfoForm._LastNameItem.setString(userInfo[JimmUI.UI_LAST_NAME]);
		editInfoForm._CityItem.setString(userInfo[JimmUI.UI_CITY]);

		Jimm.display.setCurrent(editInfoForm);
		Jimm.setBkltOn(true);
	}

	public void commandAction(Command c, Displayable d)
	{
		if (c == _CmdCancel)
		{
			Jimm.display.setCurrent(_PreviousForm);
			Jimm.setBkltOn(true);
		}

		if (c == _CmdSave)
		{
			userInfo[JimmUI.UI_NICK] = _NickNameItem.getString();
			userInfo[JimmUI.UI_EMAIL] = _EmailItem.getString();
			userInfo[JimmUI.UI_BDAY] = _BdayItem.getString();
			userInfo[JimmUI.UI_FIRST_NAME] = _FirstNameItem.getString();
			userInfo[JimmUI.UI_LAST_NAME] = _LastNameItem.getString();
			userInfo[JimmUI.UI_CITY] = _CityItem.getString();
			userInfo[JimmUI.UI_GENDER] = Util.genderToString(_SexItem
					.getSelectedIndex() + 1);

			//
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Util.writeWord(stream, ToIcqSrvPacket.CLI_SET_FULLINFO, false);

			Util.writeAsciizTLV(SaveInfoAction.FIRSTNAME_TLV_ID, stream,
					userInfo[JimmUI.UI_FIRST_NAME], false);

			SaveInfoAction action = new SaveInfoAction(userInfo);
			try
			{
				Icq.requestAction(action);
			} catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical())
					return;
			}

			SplashCanvas.addTimerTask("saveinfo", action, false);
		}
	}
}