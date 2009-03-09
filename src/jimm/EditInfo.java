/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-08  Jimm Project

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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.*;

import jimm.comm.Icq;
import jimm.comm.RequestInfoAction;
import jimm.comm.SaveInfoAction;
import jimm.comm.Util;
import jimm.util.ResourceBundle;

public class EditInfo extends Form implements CommandListener
{
	private String[] userInfo;
	private TextField _NickNameItem;
	private TextField _FirstNameItem;
	private TextField _LastNameItem;
	private TextField _EmailItem;
	private TextField _BdayItem;
	private TextField _CityItem;
	private TextField _cPhoneItem;
	private TextField _hPhoneItem;
	private TextField _wPhoneItem;
	private ChoiceGroup _SexItem;
	private ChoiceGroup[] chInterests = new ChoiceGroup[RequestInfoAction.INTERESTS_COUNT];
	private TextField[] tfInteresrs = new TextField[RequestInfoAction.INTERESTS_COUNT];
	private Hashtable indexByName = new Hashtable();
	private Hashtable ctrlIndexByIndex = new Hashtable();

	public EditInfo(String[] userInfo)
	{
		super(ResourceBundle.getString("editform"));
		
		_NickNameItem  = new TextField(ResourceBundle.getString("nick"),       userInfo[JimmUI.UI_NICK],       20, TextField.ANY);
		_FirstNameItem = new TextField(ResourceBundle.getString("firstname"),  userInfo[JimmUI.UI_FIRST_NAME], 20, TextField.ANY);
		_LastNameItem  = new TextField(ResourceBundle.getString("lastname"),   userInfo[JimmUI.UI_LAST_NAME],  20, TextField.ANY);
		_EmailItem     = new TextField(ResourceBundle.getString("email"),      userInfo[JimmUI.UI_EMAIL],      50, TextField.EMAILADDR);
		_BdayItem      = new TextField(ResourceBundle.getString("birth_day"),  userInfo[JimmUI.UI_BDAY],       15, TextField.ANY);
		_CityItem      = new TextField(ResourceBundle.getString("city"),       userInfo[JimmUI.UI_CITY],       50, TextField.ANY);
		_cPhoneItem    = new TextField(ResourceBundle.getString("cell_phone"), userInfo[JimmUI.UI_CPHONE],     20, TextField.ANY);
		_hPhoneItem    = new TextField(ResourceBundle.getString("home_phone"), userInfo[JimmUI.UI_PHONE],      20, TextField.ANY);
		_wPhoneItem    = new TextField(ResourceBundle.getString("work_phone"), userInfo[JimmUI.UI_W_PHONE],    20, TextField.ANY);
		
		_SexItem = new ChoiceGroup(ResourceBundle.getString("gender"), Choice.EXCLUSIVE);
		_SexItem.append(ResourceBundle.getString("none"), null);
		_SexItem.append(ResourceBundle.getString("female"), null);
		_SexItem.append(ResourceBundle.getString("male"), null);
		_SexItem.setSelectedIndex(Util.stringToGender(userInfo[JimmUI.UI_GENDER]), true);
		
		// Interests
		final String empryStr = new String();
		String[] interestsArray = new String[Icq.interests.size()+1];
		
		Enumeration interKeys = Icq.interests.keys();
		int index = 0;
		interestsArray[index++] = empryStr; 
		while (interKeys.hasMoreElements())
		{
			String interKey = (String)interKeys.nextElement();
			String interValue = (String)Icq.interests.get(interKey);
			interestsArray[index++] = interValue; 
			indexByName.put(interValue, interKey);
		}
		
		Util.quicksort(interestsArray, 1, interestsArray.length-1);
		
		for (int i = 0; i < interestsArray.length; i++)
		{
			String interIndex = (String)indexByName.get(interestsArray[i]);
			if (interIndex != null) ctrlIndexByIndex.put(interIndex, Integer.toString(i));
		}
		
		for (int i = 0; i < RequestInfoAction.INTERESTS_COUNT; i++)
		{
			String cap = ResourceBundle.getString("interests")+" "+(i+1);
			
			chInterests[i] = new ChoiceGroup(cap, Choice.POPUP, interestsArray, null);
			String interKey = userInfo[RequestInfoAction.indexes[2*i]];
			if (interKey != null)
			{
				String InterKeyPos = (String)ctrlIndexByIndex.get(interKey);
				int pos = Util.strToIntDef(InterKeyPos, -1);
				if (pos != -1) chInterests[i].setSelectedIndex(pos, true);
			}
			String interValue = userInfo[RequestInfoAction.indexes[2*i+1]];
			tfInteresrs[i] = new TextField(cap, (interValue == null) ? empryStr : interValue, 256, TextField.ANY); 
		}
		
		// Add controls into form
		append(_NickNameItem);
		append(_FirstNameItem);
		append(_LastNameItem);
		append(_SexItem);
		append(_EmailItem);
		append(_BdayItem);
		append(_CityItem);
		append(_cPhoneItem);
		append(_hPhoneItem);
		append(_wPhoneItem);
		
		for (int i = 0; i < RequestInfoAction.INTERESTS_COUNT; i++)
		{
			append(chInterests[i]);
			append(tfInteresrs[i]);
		}
		
		addCommand(JimmUI.cmdSave);
		addCommand(JimmUI.cmdCancel);
		setCommandListener(this);
	}

	public static void showEditForm(String[] userInfo)
	{
		EditInfo editInfoForm = new EditInfo(userInfo);
		editInfoForm.userInfo = userInfo;
		Jimm.display.setCurrent(editInfoForm);
		Jimm.setBkltOn(true);
	}

	public void commandAction(Command c, Displayable d)
	{
		if (c == JimmUI.cmdCancel)
		{
			JimmUI.backToLastScreen();
			Jimm.setBkltOn(true);
		}

		else if (c == JimmUI.cmdSave)
		{
			userInfo[JimmUI.UI_NICK]       = _NickNameItem.getString();
			userInfo[JimmUI.UI_EMAIL]      = _EmailItem.getString();
			userInfo[JimmUI.UI_BDAY]       = _BdayItem.getString();
			userInfo[JimmUI.UI_FIRST_NAME] = _FirstNameItem.getString();
			userInfo[JimmUI.UI_LAST_NAME]  = _LastNameItem.getString();
			userInfo[JimmUI.UI_CITY]       = _CityItem.getString();
			userInfo[JimmUI.UI_CPHONE]     = _cPhoneItem.getString();
			userInfo[JimmUI.UI_PHONE]      = _hPhoneItem.getString();
			userInfo[JimmUI.UI_W_PHONE]    = _wPhoneItem.getString();
			userInfo[JimmUI.UI_GENDER]     = Util.genderToString(_SexItem.getSelectedIndex());
			
			for (int i = 0; i < RequestInfoAction.INTERESTS_COUNT; i++)
			{
				ChoiceGroup ch = chInterests[i];
				int selIndex = ch.getSelectedIndex();
				String InterType = (selIndex == -1) ? null : ch.getString(selIndex);
				userInfo[RequestInfoAction.indexes[2*i]] = (InterType != null) ? (String)indexByName.get(InterType) : null;
				String interValue = tfInteresrs[i].getString();
				if (interValue != null && interValue.length() == 0) interValue = null;
				userInfo[RequestInfoAction.indexes[2*i+1]] = interValue;
			}

			SaveInfoAction action = new SaveInfoAction(userInfo);
			try
			{
				Icq.requestAction(action);
			} catch (JimmException e)
			{
				JimmException.handleException(e);
				if (e.isCritical()) return;
			}

			SplashCanvas.addTimerTask("saveinfo", action, false);
		}
	}
}