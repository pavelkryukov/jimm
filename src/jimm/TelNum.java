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
 File: src/jimm/TelNum.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Mikitevich Ivan
 *******************************************************************************/
//#sijapp cond.if modules_PIM is "true" #
package jimm;

/**
 *
 * @author Rad1st
 */
public class TelNum {

    private String name;
    private String tel;
    private int value;

    public TelNum(String _n, String _t, int _v) {
	this.name = _n;
	this.tel = _t;
	this.value = _v;
    }

    public String getName() {
	return name;
    }

    public String getTel() {
	return tel;
    }

    public int getValue() {
	return value;
    }

    public void setTel(String _t) {
	this.tel = _t;
    }
}
//#sijapp cond.end#
