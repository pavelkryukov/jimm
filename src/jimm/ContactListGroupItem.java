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
 File: src/jimm/ContactListGroupItem.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm;


public class ContactListGroupItem extends ContactListItem
{


	// Persistent variables
	private int id;
	private String name;


	// Constructor
	public ContactListGroupItem(int id, String name)
	{
		this.id = id;
		this.name = new String(name);
	}


	// Returns the group item id
	public int getId()
	{
		return (this.id);
	}


	// Sets the group item id
	public void setId(int id)
	{
		this.id = id;
	}


	// Returns the group item name
	public String getName()
	{
		return (new String(this.name));
	}


	// Sets the group item name
	public void setName(String name)
	{
		this.name = new String(name);
	}


	// Checks whether some other object is equal to this one
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ContactListGroupItem)) return (false);
		ContactListGroupItem gi = (ContactListGroupItem) obj;
		return (this.id == gi.getId());
	}


}
