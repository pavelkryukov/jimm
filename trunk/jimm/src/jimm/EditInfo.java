/*******************************************************************************
Jimm - Mobile Messaging - J2ME ICQ clone
Copyright (C) 2003-06  Jimm Project

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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.ChoiceGroup;

import jimm.comm.Icq;
import jimm.comm.SaveInfoAction;
import jimm.comm.ToIcqSrvPacket;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

public class EditInfo extends Form implements CommandListener
{
	private static WeakReference _FormRef = new WeakReference(null);
	
	private TextField _UINItem = new TextField( ResourceBundle.getString("uin"), null, 15, TextField.UNEDITABLE );
	private TextField _NickNameItem = new TextField( ResourceBundle.getString("nick"), null, 15, TextField.ANY );
	private TextField _FirstNameItem = new TextField( ResourceBundle.getString("firstname"), null, 15, TextField.ANY );
	private TextField _LastNameItem = new TextField( ResourceBundle.getString("lastname"), null, 15, TextField.ANY );
	private TextField _EmailItem = new TextField( ResourceBundle.getString("email"), null, 50, TextField.EMAILADDR );
	private DateField _BdayItem = new DateField( ResourceBundle.getString("age"), DateField.DATE, TimeZone.getTimeZone("GMT") );
	private TextField _CityItem = new TextField( ResourceBundle.getString("city"), null, 15, TextField.ANY );
	private ChoiceGroup _SexItem = new ChoiceGroup(ResourceBundle.getString("gender"), ChoiceGroup.EXCLUSIVE);
	
	private Command _CmdCancel = new Command(ResourceBundle.getString("cancel"), Command.CANCEL, 0);
	private Command _CmdSave = new Command(ResourceBundle.getString("save"), Command.OK, 1);
	private Displayable _PreviousForm;
	
	public EditInfo(Displayable currentForm) 
	{
		super(ResourceBundle.getString("editform"));
		_PreviousForm = currentForm;
		_SexItem.append(ResourceBundle.getString("female"), null);
		_SexItem.append(ResourceBundle.getString("male"), null);
		append(_UINItem);
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
		EditInfo editInfoForm = (EditInfo)_FormRef.get();
		if( editInfoForm == null )
		{
			editInfoForm = new EditInfo(previousForm);
			_FormRef = new WeakReference(editInfoForm);
		}
		editInfoForm._SexItem.setSelectedIndex( Util.stringToGender(userInfo[JimmUI.UI_GENDER])-1, true );
		editInfoForm._UINItem.setString( userInfo[JimmUI.UI_UIN] );
		editInfoForm._NickNameItem.setString(userInfo[JimmUI.UI_NICK]);
		editInfoForm._EmailItem.setString(userInfo[JimmUI.UI_EMAIL]);
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		DebugLog.addText(userInfo[JimmUI.UI_BDAY]);
		String[] date = Util.explode( userInfo[JimmUI.UI_BDAY], '.' );
		c.set( Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]) );
		c.set( Calendar.MONTH, Integer.parseInt(date[1]) );
		c.set( Calendar.YEAR, Integer.parseInt(date[2]) );
		editInfoForm._BdayItem.setDate(c.getTime() );
		DebugLog.addText("showEditForm - 4");
		editInfoForm._FirstNameItem.setString(userInfo[JimmUI.UI_FIRST_NAME]);
		editInfoForm._LastNameItem.setString(userInfo[JimmUI.UI_LAST_NAME]);
		editInfoForm._CityItem.setString( userInfo[JimmUI.UI_CITY] );
		
		Jimm.display.setCurrent( editInfoForm );
	}

	public void commandAction(Command c, Displayable d) 
	{
		if( c == _CmdCancel )
			Jimm.display.setCurrent(_PreviousForm);
		
		if( c == _CmdSave )
		{
			String[] lastInfo = Util.getLastUserInfo();
			lastInfo[JimmUI.UI_NICK] = _NickNameItem.getString();
			lastInfo[JimmUI.UI_EMAIL] = _EmailItem.getString();
			Date bDate = _BdayItem.getDate();
			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			calendar.setTime(bDate);
			lastInfo[JimmUI.UI_BDAY] = calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH) + "." + calendar.get(Calendar.YEAR);
			lastInfo[JimmUI.UI_FIRST_NAME] = _FirstNameItem.getString();
			lastInfo[JimmUI.UI_LAST_NAME] = _LastNameItem.getString();
			lastInfo[JimmUI.UI_CITY] = _CityItem.getString();
			lastInfo[JimmUI.UI_GENDER] = Util.genderToString(_SexItem.getSelectedIndex()+1);
			
			//
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Util.writeWord(stream ,ToIcqSrvPacket.CLI_SET_FULLINFO, false);

			Util.writeAsciizTLV(SaveInfoAction.FIRSTNAME_TLV_ID, stream, lastInfo[JimmUI.UI_FIRST_NAME], false);
			
			ToIcqSrvPacket packet = new ToIcqSrvPacket(0,Options.getString(Options.OPTION_UIN), ToIcqSrvPacket.CLI_META_SUBCMD, new byte[0], stream.toByteArray());
			
			SaveInfoAction action = new SaveInfoAction(lastInfo);
			try
			{
				Icq.requestAction(action);
			}
			catch(Exception e){}
			SplashCanvas.addTimerTask("saveinfo", action, false);
		}
	}
}