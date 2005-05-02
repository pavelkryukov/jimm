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
 File: src/DrawControls/LightControl.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Dmitry Tunin
 *******************************************************************************/
//#sijapp cond.if target is "MOTOROLA"#

package DrawControls;
import jimm.Jimm;

public class LightControl
{
private static boolean LightOn =true;
  
   public static void On()
   {
	Jimm.display.flashBacklight(Integer.MAX_VALUE);
                     LightOn=true;
    }
    public static void Off()
    {
	Jimm.display.flashBacklight(1);
	Jimm.display.flashBacklight(0);
	LightOn=false;
    }
    public static void ChangeState()
   {              
               if (LightOn)
               {	      
	Off();
                }
                else
                {
                      On();
                 }
    }
}
//#sijapp cond.end#