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
 File: src/jimm/comm/Util.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Sergey Chernov, Andrey B. Ivlev
 *******************************************************************************/


package jimm.comm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.lang.*;

import jimm.ContactListContactItem;
import jimm.ContactListGroupItem;
import jimm.DebugLog;
import jimm.Jimm;
import jimm.ContactList;
import jimm.Options;


public class Util
{


	// Password encryption key
	public static final byte[] PASSENC_KEY = {(byte) 0xF3, (byte) 0x26, (byte) 0x81, (byte) 0xC4,
	                                          (byte) 0x39, (byte) 0x86, (byte) 0xDB, (byte) 0x92,
	                                          (byte) 0x71, (byte) 0xA3, (byte) 0xB9, (byte) 0xE6,
	                                          (byte) 0x53, (byte) 0x7A, (byte) 0x95, (byte) 0x7C};


	// Online status (set values)
	public static final int SET_STATUS_AWAY = 0x0001;
	public static final int SET_STATUS_CHAT = 0x0020;
	public static final int SET_STATUS_DND = 0x0013;
	public static final int SET_STATUS_INVISIBLE = 0x0100;
	public static final int SET_STATUS_NA = 0x0005;
	public static final int SET_STATUS_OCCUPIED = 0x0011;
	public static final int SET_STATUS_ONLINE = 0x0000;
	
	// Counter variable
	private static int counter = 0;

	public synchronized static int getCounter()
	{
	    counter++;
	    return (counter);
	    
	}
	
	// Called to get a date String
    public static String getDateString(boolean onlyTime)
    {
        return(getDateString(onlyTime,new Date()));
    }
	
	// Called to get a date String
    public static String getDateString(boolean onlyTime, Date value)
    {
        Calendar time = Calendar.getInstance();
        String datestr = new String("failed");

        // Get time an apply time zone correction
        Date date = new Date();
    	// #sijapp cond.if target is "SIEMENS2" #
        date.setTime(value.getTime() + TimeZone.getDefault().getRawOffset() + (TimeZone.getDefault().useDaylightTime() ? (60 * 60 * 1000) : 0 )); 
        // #sijapp cond.end #
        time.setTime(date);

        // Construct the string for the display
        datestr = Util.makeTwo(time.get(Calendar.HOUR_OF_DAY)) + ":" + Util.makeTwo(time.get(Calendar.MINUTE));

        if (!onlyTime)
        {
            datestr = Util.makeTwo(time.get(Calendar.DAY_OF_MONTH)) + "." + Util.makeTwo(time.get(Calendar.MONTH) + 1) + "."
                    + String.valueOf(time.get(Calendar.YEAR)) + " " + datestr;
        }
        return datestr;
    }

	public static String toHexString(byte[] b) {
		StringBuffer sb = new StringBuffer(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			//	look up high nibble char
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);

			//	look up low nibble char
			sb.append(hexChar[b[i] & 0x0f]);
			sb.append(" ");
			if ((i != 0) && ((i % 15) == 0))
			    sb.append("\n");
		}
		return sb.toString();
	}

	//	table to convert a nibble to a hex char.
	private static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	
	// Extracts the byte from the buffer (buf) at position off
	public static int getByte(byte[] buf, int off)
	{
		int val;
		val = ((int) buf[off]) & 0x000000FF;
		return (val);
	}


	// Puts the specified byte (val) into the buffer (buf) at position off
	public static void putByte(byte[] buf, int off, int val)
	{
		buf[off] = (byte) (val & 0x000000FF);
	}


	// Extracts the word from the buffer (buf) at position off using the specified byte ordering (bigEndian)
	public static int getWord(byte[] buf, int off, boolean bigEndian)
	{
		int val;
		if (bigEndian)
		{
			val = (((int) buf[off]) << 8) & 0x0000FF00;
			val |= (((int) buf[++off])) & 0x000000FF;
		}
		else   // Little endian
		{
			val = (((int) buf[off])) & 0x000000FF;
			val |= (((int) buf[++off]) << 8) & 0x0000FF00;
		}
		return (val);
	}


	// Extracts the word from the buffer (buf) at position off using big endian byte ordering
	public static int getWord(byte[] buf, int off)
	{
		return (Util.getWord(buf, off, true));
	}


	// Puts the specified word (val) into the buffer (buf) at position off using the specified byte ordering (bigEndian)
	public static void putWord(byte[] buf, int off, int val, boolean bigEndian)
	{
		if (bigEndian)
		{
			buf[off] = (byte) ((val >> 8) & 0x000000FF);
			buf[++off] = (byte) ((val)    & 0x000000FF);
		}
		else   // Little endian
		{
			buf[off] = (byte) ((val) & 0x000000FF);
			buf[++off] = (byte) ((val >> 8) & 0x000000FF);
		}
	}


	// Puts the specified word (val) into the buffer (buf) at position off using big endian byte ordering
	public static void putWord(byte[] buf, int off, int val)
	{
		Util.putWord(buf, off, val, true);
	}


	// Extracts the double from the buffer (buf) at position off using the specified byte ordering (bigEndian)
	public static long getDWord(byte[] buf, int off, boolean bigEndian)
	{
		long val;
		if (bigEndian)
		{
			val = (((long) buf[off]) << 24) & 0xFF000000;
			val |= (((long) buf[++off]) << 16) & 0x00FF0000;
			val |= (((long) buf[++off]) << 8) & 0x0000FF00;
			val |= (((long) buf[++off])) & 0x000000FF;
		}
		else   // Little endian
		{
			val = (((long) buf[off])) & 0x000000FF;
			val |= (((long) buf[++off]) << 8) & 0x0000FF00;
			val |= (((long) buf[++off]) << 16) & 0x00FF0000;
			val |= (((long) buf[++off]) << 24) & 0xFF000000;
		}
		return (val);
	}


	// Extracts the double from the buffer (buf) at position off using big endian byte ordering
	public static long getDWord(byte[] buf, int off)
	{
		return (Util.getDWord(buf, off, true));
	}


	// Puts the specified double (val) into the buffer (buf) at position off using the specified byte ordering (bigEndian)
	public static void putDWord(byte[] buf, int off, long val, boolean bigEndian)
	{
		if (bigEndian)
		{
			buf[off] = (byte) ((val >> 24) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 16) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 8) & 0x00000000000000FF);
			buf[++off] = (byte) ((val) & 0x00000000000000FF);
		}
		else   // Little endian
		{
			buf[off] = (byte) ((val) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 8) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 16) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 24) & 0x00000000000000FF);
		}
	}


	// Puts the specified double (val) into the buffer (buf) at position off using big endian byte ordering
	public static void putDWord(byte[] buf, int off, long val)
	{
		Util.putDWord(buf, off, val, true);
	}


	// getTlv(byte[] buf, int off) => byte[]
	public static byte[] getTlv(byte[] buf, int off)
	{
		if (off + 4 > buf.length) return (null);   // Length check (#1)
		int length = Util.getWord(buf, off + 2);
		if (off + 4 + length > buf.length) return (null);   // Length check (#2)
		byte[] value = new byte[length];
		System.arraycopy(buf, off + 4, value, 0, length);
		return (value);
	}

	
	// Extracts a string from the buffer (buf) starting at position off, ending at position off+len
	public static String byteArrayToString(byte[] buf, int off, int len, boolean utf8)
	{
	
		// Length check
		if (buf.length < off + len)
		{
			return (null);
		}

		// Remove \0's at the end
		while ((len > 0) && (buf[off + len - 1] == 0x00))
		{
			len--;
		}

		// Read string in UTF-8 format
		if (utf8)
		{
			try
			{
				byte[] buf2 = new byte[len + 2];
				Util.putWord(buf2, 0, len);
				System.arraycopy(buf, off, buf2, 2, len);
				ByteArrayInputStream bais = new ByteArrayInputStream(buf2);
				DataInputStream dis = new DataInputStream(bais);
				return (dis.readUTF());
			}
			catch (Exception e)
			{
				// do nothing
			}
		}

		// CP1251 or default character encoding?
		if (Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_CP1251_HACK))
		{
			return (Encoding.byteArray1251ToString(buf, off, len));
		}
		else
		{
			return (new String(buf, off, len));
		}
		
	}


	// Extracts a string from the buffer (buf) starting at position off, ending at position off+len
	public static String byteArrayToString(byte[] buf, int off, int len)
	{
		return (Util.byteArrayToString(buf, off, len, false));
	}


	// Converts the specified buffer (buf) to a string
	public static String byteArrayToString(byte[] buf, boolean utf8)
	{
		return (Util.byteArrayToString(buf, 0, buf.length, utf8));
	}


	// Converts the specified buffer (buf) to a string
	public static String byteArrayToString(byte[] buf)
	{
		return (Util.byteArrayToString(buf, 0, buf.length, false));
	}


	// Converts the specified string (val) to a byte array
	public static byte[] stringToByteArray(String val, boolean utf8)
	{

		// Write string in UTF-8 format
		if (utf8)
		{
			try
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(baos);
				dos.writeUTF(val);
				return (baos.toByteArray());
			}
			catch (Exception e)
			{
				// Do nothing
			}
		}

		// CP1251 or default character encoding?
		if (Jimm.jimm.getOptionsRef().getBooleanOption(Options.OPTION_CP1251_HACK))
		{
			return (Encoding.stringToByteArray1251(val));
		}
		else
		{
			return (val.getBytes());
		}
		
	}


	// Converts the specified string (val) to a byte array
	public static byte[] stringToByteArray(String val)
	{
		return (Util.stringToByteArray(val, false));
	}


	// Converts the specified string to UCS-2BE
	public static byte[] stringToUcs2beByteArray(String val)
	{
		byte[] ucs2be = new byte[val.length() * 2];
		for (int i = 0; i < val.length(); i++)
		{
			Util.putWord(ucs2be, i * 2, (int) val.charAt(i));
		}
		return (ucs2be);
	}


	// Extract a UCS-2BE string from the specified buffer (buf) starting at position off, ending at position off+len
	public static String ucs2beByteArrayToString(byte[] buf, int off, int len)
	{

		// Length check
		if ((off + len > buf.length) || (buf.length % 2 != 0))
		{
			return (null);
		}

		// Convert
		StringBuffer sb = new StringBuffer();
		for (int i = off; i < off+len; i += 2)
		{
			sb.append((char) Util.getWord(buf, i));
		}
		return (sb.toString());

	}


	// Extracts a UCS-2BE string from the specified buffer (buf)
	public static String ucs2beByteArrayToString(byte[] buf)
	{
		return (Util.ucs2beByteArrayToString(buf, 0, buf.length));
	}


	// Replaces all CRLF occurences in the string (val) with CR
	public static String crlfToCr(String val)
	{
		char[] dst = new char[val.length()];
		int dstLen = 0, i;
		for (i = 0; i < (val.length() - 1); i++)   // 0 to next to last
		{
			if ((val.charAt(i) == '\r') && (val.charAt(i + 1) == '\n'))
			{
				dst[dstLen++] = val.charAt(i++);
			}
			else if (val.charAt(i + 1) == '\r')
			{
				dst[dstLen++] = val.charAt(i);
			}
			else
			{
				dst[dstLen++] = val.charAt(i++);
				dst[dstLen++] = val.charAt(i);
			}
		}
		if (i < val.length())
		{
			dst[dstLen++] = val.charAt(i);
		}
		return (new String(dst, 0, dstLen));
	}
	
	public static String removeClRfAndTabs(String val)
	{
		int len = val.length();
		char[] dst = new char[len];
		for (int i = 0; i < len; i++)
		{
			char chr = val.charAt(i);
			if ((chr == '\n') || (chr == '\r') || (chr == '\t')) chr = ' ';
			dst[i] = chr; 
		}
		return new String(dst, 0, len);  
	}


	// Compare to byte arrays (return true if equals, false otherwise)
	public static boolean byteArrayEquals(byte[] buf1, int off1, byte[] buf2, int off2, int len)
	{

		// Length check
		if ((off1 + len > buf1.length) || (off2 + len > buf2.length))
		{
			return (false);
		}

		// Compare bytes, stop at first mismatch
		for (int i = 0; i < len; i++)
		{
			if (buf1[off1 + i] != buf2[off2 + i])
			{
				return (false);
			}
		}

		// Return true if this point is reached
		return (true);

	}


	// DeScramble password
	public static byte[] decipherPassword(byte[] buf)
	{
		byte[] ret = new byte[buf.length];
		for (int i = 0; i < buf.length; i++)
		{
			ret[i] = (byte) (buf[i] ^ Util.PASSENC_KEY[i % 16]);
		}
		return (ret);
	}


	// translateStatus(long status) => void
	public static long translateStatusReceived(long status)
	{
		if (status == ContactList.STATUS_OFFLINE) return (ContactList.STATUS_OFFLINE);
		if ((status & ContactList.STATUS_DND) != 0) return (ContactList.STATUS_DND);
		if ((status & ContactList.STATUS_INVISIBLE) != 0) return (ContactList.STATUS_INVISIBLE);
		if ((status & ContactList.STATUS_OCCUPIED) != 0) return (ContactList.STATUS_OCCUPIED);
		if ((status & ContactList.STATUS_NA) != 0) return (ContactList.STATUS_NA);
		if ((status & ContactList.STATUS_AWAY) != 0) return (ContactList.STATUS_AWAY);
		if ((status & ContactList.STATUS_CHAT) != 0) return (ContactList.STATUS_CHAT);
		return (ContactList.STATUS_ONLINE);
	}


	// Get online status set value
	public static int translateStatusSend(long status)
	{
		if (status == ContactList.STATUS_AWAY) return (Util.SET_STATUS_AWAY);
		if (status == ContactList.STATUS_CHAT) return (Util.SET_STATUS_CHAT);
		if (status == ContactList.STATUS_DND) return (Util.SET_STATUS_DND);
		if (status == ContactList.STATUS_INVISIBLE) return (Util.SET_STATUS_INVISIBLE);
		if (status == ContactList.STATUS_NA) return (Util.SET_STATUS_NA);
		if (status == ContactList.STATUS_OCCUPIED) return (Util.SET_STATUS_OCCUPIED);
		return (Util.SET_STATUS_ONLINE);
	}


	//  If the numer has only one digit add a 0
	public static String makeTwo(int number)
	{
		if (number < 10)
		{
			return ("0" + String.valueOf(number));
		}
		else
		{
			return (String.valueOf(number));
		}
	}
	
	// Byte array IP to String
	public static String ipToString(byte[] ip)
	{
	    String strIP = new String();
	    int tmp;
	    
	    for (int i=0;i<3;i++)
	    {
	        tmp = (int) ip[i] & 0xFF;
	        strIP = strIP + String.valueOf(tmp)+ ".";
	    }
	    tmp = (int) ip[3] & 0xFF;
        strIP = strIP + String.valueOf(tmp);
	    
	    return strIP;
	}
	
	// String IP to byte array
    public static byte[] ipToByteArray(String ip)
    {
        byte[] arrIP = new byte[4];
        int i;

        for (int j = 0; j < 3; j++)
        {

            for (i = 0; i < 3; i++)
            {
                if (ip.charAt(i) == '.') break;
            }
	
            arrIP[j] = (byte)Integer.parseInt(ip.substring(0, i));
            ip = ip.substring(i + 1);
            
        }
        
        arrIP[3] = (byte)Integer.parseInt(ip);

        return arrIP;
    }
    
    // Create a random id which is not used yet
    public static int createRandomId()
    {
		// Max value is probably 0x7FFF, lowest value is unknown.
		// We use range 0x1000-0x7FFF.
        // From miranda source
        
        int range  = 0x6FFF;
        
        ContactListGroupItem[] gItems = Jimm.jimm.getContactListRef().getGroupItems();
        ContactListContactItem[] cItems = Jimm.jimm.getContactListRef().getContactItems();
        int randint;
        boolean found;

        Random rand = new Random(System.currentTimeMillis());
        randint = rand.nextInt();
        if (randint < 0)
            randint = randint * (-1);
        randint = randint % range + 4096;
        
        //DebugLog.addText("rand: 0x"+Integer.toHexString(randint));
        
        do
        {
            found = false;
            for (int i = 0; i < gItems.length; i++)
            {
                if (gItems[i].getId() == randint)
                {
                    randint = rand.nextInt() + 4096 % range;
                    found = true;
                    break;
                }
            }
            if (!found) 
                for (int j = 0; j < cItems.length; j++)
                {
                    if (cItems[j].getId() == randint)
	                {
                        randint = rand.nextInt() % range + 4096;
	                    found = true;
	                    break;
	                }
                }
        } while (found == true);

        return randint;
    }
  
	// #sijapp cond.if modules_TRAFFIC is "true" #
	// Returns String value of cost value
	public static String intToDecimal(int value)
	{
		String costString = "";
		String afterDot = "";
		try
		{
			if (value != 0) {
				costString = Integer.toString(value / 1000) + ".";
				afterDot = Integer.toString(value % 1000);
				while (afterDot.length() != 3)
				{
					afterDot = "0" + afterDot;
				}
				while ((afterDot.endsWith("0")) && (afterDot.length() > 2))
				{
					afterDot = afterDot.substring(0, afterDot.length() - 1);
				}
				costString = costString + afterDot;
				return costString;
			}
			else
			{
				return new String("0.0");
			}
		}
		catch (Exception e)
		{
			return new String("0.0");
		}
	}
	
	// Check is data array utf-8 string
	public static boolean isDataUTF8(byte[] array, int start, int lenght)
	{
		if (lenght == 0) return false;
		if (array.length < (start + lenght)) return false;
		
		for (int i = start, len = lenght; len > 0;)
		{
			int seqLen = 0;
			byte bt = array[i++];
			len--;
			
			if      ((bt&0xE0) == 0xC0) seqLen = 1;
			else if ((bt&0xF0) == 0xE0) seqLen = 2;
			else if ((bt&0xF8) == 0xF0) seqLen = 3;
			else if ((bt&0xFC) == 0xF8) seqLen = 4;
			else if ((bt&0xFE) == 0xFC) seqLen = 5;
			
			if (seqLen == 0)
			{
				if ((bt&0x80) == 0x80) return false;
				else continue;
			}
			
			for (int j = 0; j < seqLen; j++)
			{
				if (len == 0) return false;
				bt = array[i++];
				if ((bt&0xC0) != 0x80) return false;
				len--;
			}
			if (len == 0) break;
		}
		return true;
	}


	// Extracts the number value form String
	public static int decimalToInt(String string)
	{
		int value = 0;
		byte i = 0;
		char c = new String(".").charAt(0);
		try
		{
			for (i = 0; i < string.length(); i++)
			{
				if (c != string.charAt(i))
				{
					break;
				}
			}
			if (i == string.length()-1)
			{
				value = Integer.parseInt(string) * 1000;
				return (value);
			}
			else
			{
				while (c != string.charAt(i))
				{
					i++;
				}
				value = Integer.parseInt(string.substring(0, i)) * 1000;
				string = string.substring(i + 1, string.length());
				while (string.length() > 3)
				{
					string = string.substring(0, string.length() - 1);
				}
				while (string.length() < 3)
				{
					string = string + "0";
				}
				value = value + Integer.parseInt(string);
				return value;
			}
		}
		catch (Exception e)
		{
			return (0);
		}
	}
	// #sijapp cond.end#


}

