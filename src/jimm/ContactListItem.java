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
 File: src/jimm/ContactListItem.java
 Version: 0.3.1  Date: 2004/12/25
 Author(s): Manuel Linsmayer, Artyomov Denis
 *******************************************************************************/


package jimm;


public abstract class ContactListItem
{
	// Checks whether some other object is equal to this one
	public abstract boolean equals(Object obj);

	// returns text of list item. Is used for visual tree
	public abstract String getText();
	
	// returns image index of tree node. Is used for visual tree
	public abstract int getImageIndex();
	
	public abstract int getTextColor();
}
