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
 File: src/jimm/comm/Encoding.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Sergey Chernov, Andrey B. Ivlev
 *******************************************************************************/


// #sijapp cond.if cp1251 is "true" #


package jimm.comm;


public class Encoding
{


	// Converts an Unicode string into CP1251 byte array
	public static byte[] stringToByteArray1251(String s)
	{
		byte abyte0[] = s.getBytes();
		for(int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			switch(c)
			{
				case 1025:
					abyte0[i] = -88;
					break;
				case 1105:
					abyte0[i] = -72;
					break;
				default:
					char c1 = c;
					if(c1 >= '\u0410' && c1 <= '\u044F')
					{
						abyte0[i] = (byte)((c1 - 1040) + 192);
					}
					break;
			}
		}
		return abyte0;
	}


	// Converts an CP1251 byte array into an Unicode string
	public static String byteArray1251ToString(byte abyte0[], int i, int j)
	{
		String s = new String(abyte0, i, j);
		StringBuffer stringbuffer = new StringBuffer(j);
		for(int k = 0; k < j; k++)
		{
			int l = abyte0[k + i] & 0xff;
			switch(l)
			{
			case 168:
				stringbuffer.append('\u0401');
				break;
			case 184:
				stringbuffer.append('\u0451');
				break;
			default:
				if(l >= 192 && l <= 255)
				{
					stringbuffer.append((char)((1040 + l) - 192));
				}
				else
				{
					stringbuffer.append(s.charAt(k));
				}
				break;
			}
		}
		return stringbuffer.toString();
	}


}


// #sijapp cond.end #
