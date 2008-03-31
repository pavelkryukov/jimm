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
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Sergey Chernov, Andrey B. Ivlev,
            Artyomov Denis, Igor Palkin
 *******************************************************************************/

package jimm.comm;

import java.io.*;
import java.util.*;

import jimm.ContactListContactItem;
import jimm.ContactListGroupItem;
import jimm.ContactList;
import jimm.Options;
import jimm.util.ResourceBundle;

public class Util
{

	public static void PrintCapabilities(String caption, byte[] caps)
	{
		for (int n = 0; n < caps.length; n += 16)
		{
			if (caps.length - n < 15)
				continue;
			byte[] b = new byte[16];
			System.arraycopy(caps, n, b, 0, 16);

			String bytes = new String();
			for (int i = 0; i < b.length; i++)
				bytes += Integer.toHexString(b[i] & 0xFF);
			System.out.println(caption + bytes);
		}
	}

	// Password encryption key
	public static final byte[] PASSENC_KEY = explodeToBytes(
			"F3,26,81,C4,39,86,DB,92,71,A3,B9,E6,53,7A,95,7C", ',', 16);

	// Online status (set values)
	public static final int SET_STATUS_AWAY = 0x0001;

	public static final int SET_STATUS_CHAT = 0x0020;

	public static final int SET_STATUS_DND = 0x0013;

	public static final int SET_STATUS_INVISIBLE = 0x0100;

	public static final int SET_STATUS_NA = 0x0005;

	public static final int SET_STATUS_OCCUPIED = 0x0011;

	public static final int SET_STATUS_ONLINE = 0x0000;

	public static final int SET_STATUS_EVIL = 0x3000;

	public static final int SET_STATUS_DEPRESSION = 0x4000;

	public static final int SET_STATUS_HOME = 0x5000;

	public static final int SET_STATUS_WORK = 0x6000;

	public static final int SET_STATUS_LUNCH = 0x2001;

	// Counter variable
	private static int counter = 0;

	public synchronized static int getCounter()
	{
		counter++;
		return (counter);

	}
	
	public static String toHexString(byte[] b)
	{
		StringBuffer sb = new StringBuffer(b.length * 2);
		for (int i = 0; i < b.length; i++)
		{
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
	private static char[] hexChar =
	{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
			'e', 'f' };

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
		} else
		// Little endian
		{
			val = (((int) buf[off])) & 0x000000FF;
			val |= (((int) buf[++off]) << 8) & 0x0000FF00;
		}
		return (val);
	}

	static public DataInputStream getDataInputStream(byte[] array, int offset)
	{
		return new DataInputStream(new ByteArrayInputStream(array, offset,
				array.length - offset));
	}

	static public int getWord(DataInputStream stream, boolean bigEndian)
			throws IOException
	{
		return bigEndian ? stream.readUnsignedShort() : ((int) stream
				.readByte() & 0x00FF)
				| (((int) stream.readByte() << 8) & 0xFF00);
	}

	static public String readAsciiz(DataInputStream stream) throws IOException
	{
		int len = Util.getWord(stream, false);
		if (len == 0)
			return new String();
		byte[] buffer = new byte[len];
		stream.readFully(buffer);
		return Util.byteArrayToString(buffer);
	}

	static public void writeWord(ByteArrayOutputStream stream, int value,
			boolean bigEndian)
	{
		if (bigEndian)
		{
			stream.write(((value & 0xFF00) >> 8) & 0xFF);
			stream.write(value & 0xFF);
		} else
		{
			stream.write(value & 0xFF);
			stream.write(((value & 0xFF00) >> 8) & 0xFF);
		}
	}

	static public void writeByteArray(ByteArrayOutputStream stream, byte[] array)
	{
		try
		{
			stream.write(array);
		} catch (Exception e)
		{
			System.out.println("Util.writeByteArray: " + e.toString());
		}
	}

	static public void writeDWord(ByteArrayOutputStream stream, int value,
			boolean bigEndian)
	{
		if (bigEndian)
		{
			stream.write(((value & 0xFF000000) >> 24) & 0xFF);
			stream.write(((value & 0xFF0000) >> 16) & 0xFF);
			stream.write(((value & 0xFF00) >> 8) & 0xFF);
			stream.write(value & 0xFF);
		} else
		{
			stream.write(value & 0xFF);
			stream.write(((value & 0xFF00) >> 8) & 0xFF);
			stream.write(((value & 0xFF0000) >> 16) & 0xFF);
			stream.write(((value & 0xFF000000) >> 24) & 0xFF);
		}
	}

	static public void writeByte(ByteArrayOutputStream stream, int value)
	{
		stream.write(value);
	}

	static public void writeLenAndString(ByteArrayOutputStream stream,
			String value, boolean utf8)
	{
		byte[] raw = Util.stringToByteArray(value, utf8);
		writeWord(stream, raw.length, true);
		stream.write(raw, 0, raw.length);
	}

	static public void writeLenLEAndStringAsciiz(ByteArrayOutputStream stream,
			String value)
	{
		byte[] raw = Util.stringToByteArray(value, false);
		writeWord(stream, raw.length + 1, false);
		writeByteArray(stream, raw);
		writeByte(stream, 0);
	}

	static public void writeAsciizTLV(int type, ByteArrayOutputStream stream,
			String value, boolean bigEndian)
	{
		writeWord(stream, type, bigEndian);
		byte[] raw = Util.stringToByteArray(value);
		writeWord(stream, raw.length + 3, false);
		writeWord(stream, raw.length + 1, false);
		stream.write(raw, 0, raw.length);
		stream.write(0);
	}

	static public void writeAsciizTLV(int type, ByteArrayOutputStream stream,
			String value)
	{
		writeAsciizTLV(type, stream, value, true);
	}

	static public void writeTLV(int type, ByteArrayOutputStream stream,
			ByteArrayOutputStream data)
	{
		writeTLV(type, stream, data, true);
	}

	static public void writeTLV(int type, ByteArrayOutputStream stream,
			ByteArrayOutputStream data, boolean bigEndian)
	{
		byte[] raw = data.toByteArray();
		writeWord(stream, type, bigEndian);
		writeWord(stream, raw.length, false);
		stream.write(raw, 0, raw.length);
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
			buf[++off] = (byte) ((val) & 0x000000FF);
		} else
		// Little endian
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
		} else
		// Little endian
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
		} else
		// Little endian
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

	// Puts the specified double (val) into the buffer (buf) at position off using the specified byte ordering (bigEndian)
	public static void putQWord(byte[] buf, int off, long val, boolean bigEndian)
	{
		if (bigEndian)
		{
			buf[off] = (byte) ((val >> 56) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 48) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 40) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 32) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 24) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 16) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 8) & 0x00000000000000FF);
			buf[++off] = (byte) ((val) & 0x00000000000000FF);
		} else
		// Little endian
		{
			buf[off] = (byte) ((val) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 8) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 16) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 24) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 32) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 40) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 48) & 0x00000000000000FF);
			buf[++off] = (byte) ((val >> 56) & 0x00000000000000FF);
		}
	}

	// Puts the specified double (val) into the buffer (buf) at position off using big endian byte ordering
	public static void putQWord(byte[] buf, int off, long val)
	{
		Util.putDWord(buf, off, val, true);
	}

	// Extracts the double from the buffer (buf) at position off using the specified byte ordering (bigEndian)
	public static long getQWord(byte[] buf, int off, boolean bigEndian)
	{
		long val;
		if (bigEndian)
		{
			val = (((long) buf[off]) << 24) & 0xFF000000;
			val |= (((long) buf[++off]) << 16) & 0x00FF0000;
			val |= (((long) buf[++off]) << 8) & 0x0000FF00;
			val |= (((long) buf[++off])) & 0x000000FF;
		} else
		// Little endian
		{
			val = (((long) buf[off])) & 0x000000FF;
			val |= (((long) buf[++off]) << 8) & 0x0000FF00;
			val |= (((long) buf[++off]) << 16) & 0x00FF0000;
			val |= (((long) buf[++off]) << 24) & 0xFF000000;
		}
		return (val);
	}

	// getTlv(byte[] buf, int off) => byte[]
	public static byte[] getTlv(byte[] buf, int off)
	{
		if (off + 4 > buf.length)
			return (null); // Length check (#1)
		int length = Util.getWord(buf, off + 2);
		if (off + 4 + length > buf.length)
			return (null); // Length check (#2)
		byte[] value = new byte[length];
		System.arraycopy(buf, off + 4, value, 0, length);
		return (value);
	}

	// Extracts a string from the buffer (buf) starting at position off, ending at position off+len
	public static String byteArrayToString(byte[] buf, int off, int len,
			boolean utf8)
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
			} catch (Exception e)
			{
				// do nothing
			}
		}

		// CP1251 or default character encoding?
		if (Options.getBoolean(Options.OPTION_CP1251_HACK))
		{
			return (byteArray1251ToString(buf, off, len));
		} else
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

	// Converts the specific 4 byte max buffer to an unsigned long
	public static long byteArrayToLong(byte[] b)
	{
		long l = 0;
		l |= b[0] & 0xFF;
		l <<= 8;
		l |= b[1] & 0xFF;
		l <<= 8;
		if (b.length > 3)
		{
			l |= b[2] & 0xFF;
			l <<= 8;
			l |= b[3] & 0xFF;
		}
		return l;
	}

	// Converts a byte array to a hex string
	public static String byteArrayToHexString(byte[] buf)
	{
		StringBuffer hexString = new StringBuffer(buf.length);
		String hex;
		for (int i = 0; i < buf.length; i++)
		{
			hex = Integer.toHexString(0x0100 + (buf[i] & 0x00FF)).substring(1);
			hexString.append((hex.length() < 2 ? "0" : "") + hex);
		}
		return hexString.toString();
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
				byte[] raw = baos.toByteArray();
				byte[] result = new byte[raw.length - 2];
				System.arraycopy(raw, 2, result, 0, raw.length - 2);
				return result;
			} catch (Exception e)
			{
				// Do nothing
			}
		}

		// CP1251 or default character encoding?
		if (Options.getBoolean(Options.OPTION_CP1251_HACK))
		{
			return (stringToByteArray1251(val));
		} else
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
		if ((off + len > buf.length) || (len % 2 != 0))
		{
			return (null);
		}

		// Convert
		StringBuffer sb = new StringBuffer();
		for (int i = off; i < off + len; i += 2)
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

	public static void showBytes(byte[] data)
	{
		StringBuffer buffer1 = new StringBuffer(), buffer2 = new StringBuffer();

		for (int i = 0; i < data.length; i++)
		{
			int charaster = ((int) data[i]) & 0xFF;
			buffer1.append(charaster < ' ' || charaster >= 128 ? '.'
					: (char) charaster);
			String hex = Integer.toHexString(((int) data[i]) & 0xFF);
			buffer2.append(hex.length() == 1 ? "0" + hex : hex);
			buffer2.append(" ");

			if (((i % 16) == 15) || (i == (data.length - 1)))
			{
				while (buffer2.length() < 16 * 3)
					buffer2.append(' ');
				System.out.print(buffer2.toString());
				System.out.println(buffer1.toString());

				buffer1.setLength(0);
				buffer2.setLength(0);
			}
		}
		System.out.println();
	}

	// Removes all CR occurences
	public static String removeCr(String val)
	{
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < val.length(); i++)
		{
			char chr = val.charAt(i);
			if ((chr == 0) || (chr == '\r'))
				continue;
			result.append(chr);
		}
		return result.toString();
	}

	// Restores CRLF sequense from LF
	public static String restoreCrLf(String val)
	{
		StringBuffer result = new StringBuffer();
		int size = val.length();
		for (int i = 0; i < size; i++)
		{
			char chr = val.charAt(i);
			if (chr == '\r')
				continue;
			if (chr == '\n')
				result.append("\r\n");
			else
				result.append(chr);
		}
		return result.toString();
	}

	public static String removeClRfAndTabs(String val)
	{
		int len = val.length();
		char[] dst = new char[len];
		for (int i = 0; i < len; i++)
		{
			char chr = val.charAt(i);
			if ((chr == '\n') || (chr == '\r') || (chr == '\t'))
				chr = ' ';
			dst[i] = chr;
		}
		return new String(dst, 0, len);
	}

	// Compare to byte arrays (return true if equals, false otherwise)
	public static boolean byteArrayEquals(byte[] buf1, int off1, byte[] buf2,
			int off2, int len)
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
	public static int translateStatusReceived(int status)
	{
		if (status == ContactList.STATUS_OFFLINE)
			return (ContactList.STATUS_OFFLINE);
		if ((status & ContactList.STATUS_DND) != 0)
			return (ContactList.STATUS_DND);
		if ((status & ContactList.STATUS_INVISIBLE) != 0)
			return (ContactList.STATUS_INVISIBLE);
		if ((status & ContactList.STATUS_OCCUPIED) != 0)
			return (ContactList.STATUS_OCCUPIED);
		if ((status & ContactList.STATUS_NA) != 0)
			return (ContactList.STATUS_NA);
		if ((status & ContactList.STATUS_CHAT) != 0)
			return (ContactList.STATUS_CHAT);
		if ((status & ContactList.STATUS_LUNCH) == ContactList.STATUS_LUNCH)
			return (ContactList.STATUS_LUNCH);
                if ((status & ContactList.STATUS_EVIL) == ContactList.STATUS_EVIL)
			return (ContactList.STATUS_EVIL);
		if ((status & ContactList.STATUS_HOME) == ContactList.STATUS_HOME)
			return (ContactList.STATUS_HOME);
		if ((status & ContactList.STATUS_WORK) == ContactList.STATUS_WORK)
			return (ContactList.STATUS_WORK);
		if ((status & ContactList.STATUS_AWAY) != 0)
			return (ContactList.STATUS_AWAY);
		if ((status & ContactList.STATUS_DEPRESSION) == ContactList.STATUS_DEPRESSION)
			return (ContactList.STATUS_DEPRESSION);
		return (ContactList.STATUS_ONLINE);
	}

	// Get online status set value
	public static int translateStatusSend(int status)
	{
		if (status == ContactList.STATUS_AWAY)
			return (Util.SET_STATUS_AWAY);
		if (status == ContactList.STATUS_CHAT)
			return (Util.SET_STATUS_CHAT);
		if (status == ContactList.STATUS_DND)
			return (Util.SET_STATUS_DND);
		if (status == ContactList.STATUS_INVISIBLE)
			return (Util.SET_STATUS_INVISIBLE);
		if (status == ContactList.STATUS_INVIS_ALL)
			return (Util.SET_STATUS_INVISIBLE);
		if (status == ContactList.STATUS_NA)
			return (Util.SET_STATUS_NA);
		if (status == ContactList.STATUS_OCCUPIED)
			return (Util.SET_STATUS_OCCUPIED);
		if (status == ContactList.STATUS_EVIL)
			return (Util.SET_STATUS_EVIL);
		if (status == ContactList.STATUS_DEPRESSION)
			return (Util.SET_STATUS_DEPRESSION);
		if (status == ContactList.STATUS_HOME)
			return (Util.SET_STATUS_HOME);
		if (status == ContactList.STATUS_WORK)
			return (Util.SET_STATUS_WORK);
		if (status == ContactList.STATUS_LUNCH)
			return (Util.SET_STATUS_LUNCH);
		return (Util.SET_STATUS_ONLINE);
	}

	//  If the numer has only one digit add a 0
	public static String makeTwo(int number)
	{
		if (number < 10)
		{
			return ("0" + String.valueOf(number));
		} else
		{
			return (String.valueOf(number));
		}
	}

	// Byte array IP to String
	public static String ipToString(byte[] ip)
	{
		if (ip == null)
			return null;
		StringBuffer strIP = new StringBuffer();

		for (int i = 0; i < 4; i++)
		{
			int tmp = (int) ip[i] & 0xFF;
			if (strIP.length() != 0)
				strIP.append('.');
			strIP.append(tmp);
		}

		return strIP.toString();
	}

	// String IP to byte array
	public static byte[] ipToByteArray(String ip)
	{
		byte[] arrIP = explodeToBytes(ip, '.', 10);
		return ((arrIP == null) || (arrIP.length != 4)) ? null : arrIP;
	}

	//#sijapp cond.if modules_PROXY is "true"#
	// Try to parse string IP
	public static boolean isIP(String ip)
	{
		boolean isTrueIp = false;
		try
		{
			isTrueIp = (ipToByteArray(ip) != null);
		} catch (NumberFormatException e)
		{
			return false;
		}
		return isTrueIp;
	}

	//#sijapp cond.end #

	// Create a random id which is not used yet
	public static int createRandomId()
	{
		// Max value is probably 0x7FFF, lowest value is unknown.
		// We use range 0x1000-0x7FFF.
		// From miranda source

		int range = 0x6FFF;

		ContactListGroupItem[] gItems = ContactList.getGroupItems();
		ContactListContactItem[] cItems = ContactList.getContactItems();
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
					if (cItems[j]
							.getIntValue(ContactListContactItem.CONTACTITEM_ID) == randint)
					{
						randint = rand.nextInt() % range + 4096;
						found = true;
						break;
					}
				}
		} while (found == true);

		return randint;
	}

	// Check is data array utf-8 string
	public static boolean isDataUTF8(byte[] array, int start, int lenght)
	{
		if (lenght == 0)
			return false;
		if (array.length < (start + lenght))
			return false;

		for (int i = start, len = lenght; len > 0;)
		{
			int seqLen = 0;
			byte bt = array[i++];
			len--;

			if ((bt & 0xE0) == 0xC0)
				seqLen = 1;
			else if ((bt & 0xF0) == 0xE0)
				seqLen = 2;
			else if ((bt & 0xF8) == 0xF0)
				seqLen = 3;
			else if ((bt & 0xFC) == 0xF8)
				seqLen = 4;
			else if ((bt & 0xFE) == 0xFC)
				seqLen = 5;

			if (seqLen == 0)
			{
				if ((bt & 0x80) == 0x80)
					return false;
				else
					continue;
			}

			for (int j = 0; j < seqLen; j++)
			{
				if (len == 0)
					return false;
				bt = array[i++];
				if ((bt & 0xC0) != 0x80)
					return false;
				len--;
			}
			if (len == 0)
				break;
		}
		return true;
	}

	//#sijapp cond.if modules_TRAFFIC is "true" #
	// Returns String value of cost value
	public static String intToDecimal(int value)
	{
		String costString = "";
		String afterDot = "";
		try
		{
			if (value != 0)
			{
				costString = Integer.toString(value / 10000) + ".";
				afterDot = Integer.toString(value % 10000);
				while (afterDot.length() != 4)
				{
					afterDot = "0" + afterDot;
				}
				while ((afterDot.endsWith("0")) && (afterDot.length() > 2))
				{
					afterDot = afterDot.substring(0, afterDot.length() - 1);
				}
				costString = costString + afterDot;
				return costString;
			} else
			{
				return new String("0.0");
			}
		} catch (Exception e)
		{
			return new String("0.0");
		}
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
			if (i == string.length() - 1)
			{
				value = Integer.parseInt(string) * 10000;
				return (value);
			} else
			{
				while (c != string.charAt(i))
				{
					i++;
				}
				value = Integer.parseInt(string.substring(0, i)) * 10000;
				string = string.substring(i + 1, string.length());
				while (string.length() > 4)
				{
					string = string.substring(0, string.length() - 1);
				}
				while (string.length() < 4)
				{
					string = string + "0";
				}
				value = value + Integer.parseInt(string);
				return value;
			}
		} catch (Exception e)
		{
			return (0);
		}
	}

	//#sijapp cond.end#

	// Convert gender code to string
	static public String genderToString(int gender)
	{
		switch (gender)
		{
		case 1:
			return ResourceBundle.getString("female");
		case 2:
			return ResourceBundle.getString("male");
		}
		return new String();
	}

	static public int stringToGender(String gender)
	{
		if (gender == ResourceBundle.getString("female"))
			return 1;
		else
			return 2;
	}

	// Converts an Unicode string into CP1251 byte array
	public static byte[] stringToByteArray1251(String s)
	{
		byte abyte0[] = s.getBytes();
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			switch (c)
			{
			case 1025:
				abyte0[i] = -88;
				break;
			case 1105:
				abyte0[i] = -72;
				break;

			/* Ukrainian CP1251 chars section */
			case 1168:
				abyte0[i] = -91;
				break;
			case 1028:
				abyte0[i] = -86;
				break;
			case 1031:
				abyte0[i] = -81;
				break;
			case 1030:
				abyte0[i] = -78;
				break;
			case 1110:
				abyte0[i] = -77;
				break;
			case 1169:
				abyte0[i] = -76;
				break;
			case 1108:
				abyte0[i] = -70;
				break;
			case 1111:
				abyte0[i] = -65;
				break;
			/* end of section */

			default:
				char c1 = c;
				if (c1 >= '\u0410' && c1 <= '\u044F')
				{
					abyte0[i] = (byte) ((c1 - 1040) + 192);
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
		for (int k = 0; k < j; k++)
		{
			int l = abyte0[k + i] & 0xff;
			switch (l)
			{
			case 168:
				stringbuffer.append('\u0401');
				break;
			case 184:
				stringbuffer.append('\u0451');
				break;

			/* Ukrainian CP1251 chars section */
			case 165:
				stringbuffer.append('\u0490');
				break;
			case 170:
				stringbuffer.append('\u0404');
				break;
			case 175:
				stringbuffer.append('\u0407');
				break;
			case 178:
				stringbuffer.append('\u0406');
				break;
			case 179:
				stringbuffer.append('\u0456');
				break;
			case 180:
				stringbuffer.append('\u0491');
				break;
			case 186:
				stringbuffer.append('\u0454');
				break;
			case 191:
				stringbuffer.append('\u0457');
				break;
			/* end of section */

			default:
				if (l >= 192 && l <= 255)
				{
					stringbuffer.append((char) ((1040 + l) - 192));
				} else
				{
					stringbuffer.append(s.charAt(k));
				}
				break;
			}
		}
		return stringbuffer.toString();
	}

	/*/////////////////////////////////////////////////////////////////////////
	 //                                                                       //
	 //                 METHODS FOR DATE AND TIME PROCESSING                  //
	 //                                                                       //	
	 /////////////////////////////////////////////////////////////////////////*/

	private final static String error_str = "***error***";

	final public static int TIME_SECOND = 0;

	final public static int TIME_MINUTE = 1;

	final public static int TIME_HOUR = 2;

	final public static int TIME_DAY = 3;

	final public static int TIME_MON = 4;

	final public static int TIME_YEAR = 5;

	final private static byte[] dayCounts = explodeToBytes(
			"31,28,31,30,31,30,31,31,30,31,30,31", ',', 10);

	/* Creates current date (GMT or local) */
	public static long createCurrentDate(boolean gmt)
	{
	    // getTime() returns GTM time
	    long result = new Date().getTime() / 1000;

	    /* convert result to GMT time */
//	    long diff = Options.getInt(Options.OPTIONS_LOCAL_OFFSET);
//	    long dl = Options.getInt(Options.OPTIONS_DAYLIGHT_SAVING);
//	    result += ((diff + dl) * 3600);

	    /* returns GMT or local time */
	    return gmt ? result : gmtTimeToLocalTime(result);
	}

	/* Show date string */
	public static String getDateString(boolean onlyTime, long date)
	{
		if (date == 0)
			return error_str;

		int[] loclaDate = createDate(date);

		StringBuffer sb = new StringBuffer();

		if (!onlyTime)
		{
			sb.append(Util.makeTwo(loclaDate[TIME_DAY])).append('.').append(
					Util.makeTwo(loclaDate[TIME_MON])).append('.').append(
					loclaDate[TIME_YEAR]).append(' ');
		}

		sb.append(Util.makeTwo(loclaDate[TIME_HOUR])).append(':').append(
				Util.makeTwo(loclaDate[TIME_MINUTE]));

		return sb.toString();
	}

	/* Generates seconds count from 1st Jan 1970 till mentioned date */
	public static long createLongTime(int year, int mon, int day, int hour,
			int min, int sec)
	{
		int day_count, i, febCount;

		day_count = (year - 1970) * 365 + day;
		day_count += (year - 1968) / 4;
		if (year >= 2000)
			day_count--;

		if ((year % 4 == 0) && (year != 2000))
		{
			day_count--;
			febCount = 29;
		} else
			febCount = 28;

		for (i = 0; i < mon - 1; i++)
			day_count += (i == 1) ? febCount : dayCounts[i];

		return day_count * 24L * 3600L + hour * 3600L + min * 60L + sec;
	}

	// Creates array of calendar values form value of seconds since 1st jan 1970 (GMT)
	public static int[] createDate(long value)
	{
		int total_days, last_days, i;
		int sec, min, hour, day, mon, year;

		sec = (int) (value % 60);

		min = (int) ((value / 60) % 60); // min
		value -= 60 * min;

		hour = (int) ((value / 3600) % 24); // hour
		value -= 3600 * hour;

		total_days = (int) (value / (3600 * 24));

		year = 1970;
		for (;;)
		{
			last_days = total_days
					- ((year % 4 == 0) && (year != 2000) ? 366 : 365);
			if (last_days <= 0)
				break;
			total_days = last_days;
			year++;
		} // year

		int febrDays = ((year % 4 == 0) && (year != 2000)) ? 29 : 28;

		mon = 1;
		for (i = 0; i < 12; i++)
		{
			last_days = total_days - ((i == 1) ? febrDays : dayCounts[i]);
			if (last_days <= 0)
				break;
			mon++;
			total_days = last_days;
		} // mon

		day = total_days; // day

		return new int[]
		{ sec, min, hour, day, mon, year };
	}

	public static String getDateString(boolean onlyTime)
	{
		return getDateString(onlyTime, createCurrentDate(false));
	}

	public static long gmtTimeToLocalTime(long gmtTime)
	{
		long diff = Options.getInt(Options.OPTIONS_GMT_OFFSET);
		long dl = Options.getInt(Options.OPTIONS_DAYLIGHT_SAVING);
		return gmtTime + (diff + dl) * 3600L;
	}

	public static String longitudeToString(long seconds)
	{
		StringBuffer buf = new StringBuffer();
		int days = (int) (seconds / 86400);
		seconds %= 86400;
		int hours = (int) (seconds / 3600);
		seconds %= 3600;
		int minutes = (int) (seconds / 60);

		if (days != 0)
			buf.append(days).append(' ').append(
					ResourceBundle.getString("days")).append(' ');
		if (hours != 0)
			buf.append(hours).append(' ').append(
					ResourceBundle.getString("hours")).append(' ');
		if (minutes != 0)
			buf.append(minutes).append(' ').append(
					ResourceBundle.getString("minutes"));

		return buf.toString();
	}

	/*====================================================*/
	/*                                                    */
	/*                     MD5 stuff                      */
	/*                                                    */
	/*====================================================*/

	static final byte[] AIM_MD5_STRING = explodeToBytes(
			"*AOL Instant Messenger (SM)", ',', 16);

	static final int S11 = 7;

	static final int S12 = 12;

	static final int S13 = 17;

	static final int S14 = 22;

	static final int S21 = 5;

	static final int S22 = 9;

	static final int S23 = 14;

	static final int S24 = 20;

	static final int S31 = 4;

	static final int S32 = 11;

	static final int S33 = 16;

	static final int S34 = 23;

	static final int S41 = 6;

	static final int S42 = 10;

	static final int S43 = 15;

	static final int S44 = 21;

	static final byte[] PADDING = explodeToBytes(
			"-128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"
					+ "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", ',', 10);

	static private long[] state = new long[4];

	static private long[] count = new long[2];

	static private byte[] buffer = new byte[64];

	static private byte[] digest = new byte[16];

	static public byte[] calculateMD5(byte[] inbuf)
	{
		md5Init();
		md5Update(inbuf, inbuf.length);
		md5Final();
		return digest;
	}

	static private void md5Init()
	{
		count[0] = 0L;
		count[1] = 0L;
		state[0] = 0x67452301L;
		state[1] = 0xefcdab89L;
		state[2] = 0x98badcfeL;
		state[3] = 0x10325476L;
		return;
	}

	static private long F(long x, long y, long z)
	{
		return (x & y) | ((~x) & z);
	}

	static private long G(long x, long y, long z)
	{
		return (x & z) | (y & (~z));
	}

	static private long H(long x, long y, long z)
	{
		return x ^ y ^ z;
	}

	static private long I(long x, long y, long z)
	{
		return y ^ (x | (~z));
	}

	static private long FF(long a, long b, long c, long d, long x, long s,
			long ac)
	{
		a += F(b, c, d) + x + ac;
		a = ((int) a << s) | ((int) a >>> (32 - s));
		a += b;
		return a;
	}

	static private long GG(long a, long b, long c, long d, long x, long s,
			long ac)
	{
		a += G(b, c, d) + x + ac;
		a = ((int) a << s) | ((int) a >>> (32 - s));
		a += b;
		return a;
	}

	static private long HH(long a, long b, long c, long d, long x, long s,
			long ac)
	{
		a += H(b, c, d) + x + ac;
		a = ((int) a << s) | ((int) a >>> (32 - s));
		a += b;
		return a;
	}

	static private long II(long a, long b, long c, long d, long x, long s,
			long ac)
	{
		a += I(b, c, d) + x + ac;
		a = ((int) a << s) | ((int) a >>> (32 - s));
		a += b;
		return a;
	}

	static private void md5Update(byte[] inbuf, int inputLen)
	{
		int i, index, partLen;
		byte[] block = new byte[64];
		index = (int) (count[0] >>> 3) & 0x3F;
		if ((count[0] += (inputLen << 3)) < (inputLen << 3))
			count[1]++;
		count[1] += (inputLen >>> 29);
		partLen = 64 - index;
		if (inputLen >= partLen)
		{
			md5Memcpy(buffer, inbuf, index, 0, partLen);
			md5Transform(buffer);
			for (i = partLen; i + 63 < inputLen; i += 64)
			{
				md5Memcpy(block, inbuf, 0, i, 64);
				md5Transform(block);
			}
			index = 0;
		} else
			i = 0;
		md5Memcpy(buffer, inbuf, index, i, inputLen - i);
	}

	static private void md5Final()
	{
		byte[] bits = new byte[8];
		int index, padLen;
		Encode(bits, count, 8);
		index = (int) (count[0] >>> 3) & 0x3f;
		padLen = (index < 56) ? (56 - index) : (120 - index);
		md5Update(PADDING, padLen);
		md5Update(bits, 8);
		Encode(digest, state, 16);
	}

	static private void md5Memcpy(byte[] output, byte[] input, int outpos,
			int inpos, int len)
	{
		int i;
		for (i = 0; i < len; i++)
			output[outpos + i] = input[inpos + i];
	}

	static private void md5Transform(byte block[])
	{
		long a = state[0], b = state[1], c = state[2], d = state[3];
		long[] x = new long[16];
		Decode(x, block, 64);
		a = FF(a, b, c, d, x[0], S11, 0xd76aa478L); /* 1 */
		d = FF(d, a, b, c, x[1], S12, 0xe8c7b756L); /* 2 */
		c = FF(c, d, a, b, x[2], S13, 0x242070dbL); /* 3 */
		b = FF(b, c, d, a, x[3], S14, 0xc1bdceeeL); /* 4 */
		a = FF(a, b, c, d, x[4], S11, 0xf57c0fafL); /* 5 */
		d = FF(d, a, b, c, x[5], S12, 0x4787c62aL); /* 6 */
		c = FF(c, d, a, b, x[6], S13, 0xa8304613L); /* 7 */
		b = FF(b, c, d, a, x[7], S14, 0xfd469501L); /* 8 */
		a = FF(a, b, c, d, x[8], S11, 0x698098d8L); /* 9 */
		d = FF(d, a, b, c, x[9], S12, 0x8b44f7afL); /* 10 */
		c = FF(c, d, a, b, x[10], S13, 0xffff5bb1L); /* 11 */
		b = FF(b, c, d, a, x[11], S14, 0x895cd7beL); /* 12 */
		a = FF(a, b, c, d, x[12], S11, 0x6b901122L); /* 13 */
		d = FF(d, a, b, c, x[13], S12, 0xfd987193L); /* 14 */
		c = FF(c, d, a, b, x[14], S13, 0xa679438eL); /* 15 */
		b = FF(b, c, d, a, x[15], S14, 0x49b40821L); /* 16 */
		a = GG(a, b, c, d, x[1], S21, 0xf61e2562L); /* 17 */
		d = GG(d, a, b, c, x[6], S22, 0xc040b340L); /* 18 */
		c = GG(c, d, a, b, x[11], S23, 0x265e5a51L); /* 19 */
		b = GG(b, c, d, a, x[0], S24, 0xe9b6c7aaL); /* 20 */
		a = GG(a, b, c, d, x[5], S21, 0xd62f105dL); /* 21 */
		d = GG(d, a, b, c, x[10], S22, 0x2441453L); /* 22 */
		c = GG(c, d, a, b, x[15], S23, 0xd8a1e681L); /* 23 */
		b = GG(b, c, d, a, x[4], S24, 0xe7d3fbc8L); /* 24 */
		a = GG(a, b, c, d, x[9], S21, 0x21e1cde6L); /* 25 */
		d = GG(d, a, b, c, x[14], S22, 0xc33707d6L); /* 26 */
		c = GG(c, d, a, b, x[3], S23, 0xf4d50d87L); /* 27 */
		b = GG(b, c, d, a, x[8], S24, 0x455a14edL); /* 28 */
		a = GG(a, b, c, d, x[13], S21, 0xa9e3e905L); /* 29 */
		d = GG(d, a, b, c, x[2], S22, 0xfcefa3f8L); /* 30 */
		c = GG(c, d, a, b, x[7], S23, 0x676f02d9L); /* 31 */
		b = GG(b, c, d, a, x[12], S24, 0x8d2a4c8aL); /* 32 */
		a = HH(a, b, c, d, x[5], S31, 0xfffa3942L); /* 33 */
		d = HH(d, a, b, c, x[8], S32, 0x8771f681L); /* 34 */
		c = HH(c, d, a, b, x[11], S33, 0x6d9d6122L); /* 35 */
		b = HH(b, c, d, a, x[14], S34, 0xfde5380cL); /* 36 */
		a = HH(a, b, c, d, x[1], S31, 0xa4beea44L); /* 37 */
		d = HH(d, a, b, c, x[4], S32, 0x4bdecfa9L); /* 38 */
		c = HH(c, d, a, b, x[7], S33, 0xf6bb4b60L); /* 39 */
		b = HH(b, c, d, a, x[10], S34, 0xbebfbc70L); /* 40 */
		a = HH(a, b, c, d, x[13], S31, 0x289b7ec6L); /* 41 */
		d = HH(d, a, b, c, x[0], S32, 0xeaa127faL); /* 42 */
		c = HH(c, d, a, b, x[3], S33, 0xd4ef3085L); /* 43 */
		b = HH(b, c, d, a, x[6], S34, 0x4881d05L); /* 44 */
		a = HH(a, b, c, d, x[9], S31, 0xd9d4d039L); /* 45 */
		d = HH(d, a, b, c, x[12], S32, 0xe6db99e5L); /* 46 */
		c = HH(c, d, a, b, x[15], S33, 0x1fa27cf8L); /* 47 */
		b = HH(b, c, d, a, x[2], S34, 0xc4ac5665L); /* 48 */
		a = II(a, b, c, d, x[0], S41, 0xf4292244L); /* 49 */
		d = II(d, a, b, c, x[7], S42, 0x432aff97L); /* 50 */
		c = II(c, d, a, b, x[14], S43, 0xab9423a7L); /* 51 */
		b = II(b, c, d, a, x[5], S44, 0xfc93a039L); /* 52 */
		a = II(a, b, c, d, x[12], S41, 0x655b59c3L); /* 53 */
		d = II(d, a, b, c, x[3], S42, 0x8f0ccc92L); /* 54 */
		c = II(c, d, a, b, x[10], S43, 0xffeff47dL); /* 55 */
		b = II(b, c, d, a, x[1], S44, 0x85845dd1L); /* 56 */
		a = II(a, b, c, d, x[8], S41, 0x6fa87e4fL); /* 57 */
		d = II(d, a, b, c, x[15], S42, 0xfe2ce6e0L); /* 58 */
		c = II(c, d, a, b, x[6], S43, 0xa3014314L); /* 59 */
		b = II(b, c, d, a, x[13], S44, 0x4e0811a1L); /* 60 */
		a = II(a, b, c, d, x[4], S41, 0xf7537e82L); /* 61 */
		d = II(d, a, b, c, x[11], S42, 0xbd3af235L); /* 62 */
		c = II(c, d, a, b, x[2], S43, 0x2ad7d2bbL); /* 63 */
		b = II(b, c, d, a, x[9], S44, 0xeb86d391L); /* 64 */
		state[0] += a;
		state[1] += b;
		state[2] += c;
		state[3] += d;
	}

	static private void Encode(byte[] output, long[] input, int len)
	{
		int i, j;
		for (i = 0, j = 0; j < len; i++, j += 4)
		{
			output[j] = (byte) (input[i] & 0xffL);
			output[j + 1] = (byte) ((input[i] >>> 8) & 0xffL);
			output[j + 2] = (byte) ((input[i] >>> 16) & 0xffL);
			output[j + 3] = (byte) ((input[i] >>> 24) & 0xffL);
		}
	}

	static private void Decode(long[] output, byte[] input, int len)
	{
		int i, j;
		for (i = 0, j = 0; j < len; i++, j += 4)
			output[i] = b2iu(input[j]) | (b2iu(input[j + 1]) << 8)
					| (b2iu(input[j + 2]) << 16) | (b2iu(input[j + 3]) << 24);
		return;
	}

	public static long b2iu(byte b)
	{
		return b < 0 ? b & 0x7F + 128 : b;
	}

	public static String getCurrentDay()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		String day = "";

		switch (cal.get(Calendar.DAY_OF_WEEK))
		{
		case Calendar.MONDAY:
			day = "monday";
			break;

		case Calendar.TUESDAY:
			day = "tuesday";
			break;

		case Calendar.WEDNESDAY:
			day = "wednesday";
			break;

		case Calendar.THURSDAY:
			day = "thursday";
			break;

		case Calendar.FRIDAY:
			day = "friday";
			break;

		case Calendar.SATURDAY:
			day = "saturday";
			break;

		case Calendar.SUNDAY:
			day = "sunday";
			break;
		}
		return ResourceBundle.getString(day);
	}

	private static boolean isURLChar(char chr, boolean before)
	{
		if (before)
			return ((chr >= 'A') && (chr <= 'Z'))
					|| ((chr >= 'a') && (chr <= 'z'))
					|| ((chr >= '0') && (chr <= '9'));
		if ((chr <= ' ') || (chr == '\"'))
			return false;
		return ((chr & 0xFF00) == 0);
	}

	public static Vector parseMessageForURL(String msg)
	{
		if (msg.indexOf('.') == -1)
			return null;

		Vector result = new Vector();
		int size = msg.length();
		int findIndex = 0, beginIdx, endIdx;
		for (;;)
		{
			if (findIndex >= size)
				break;
			int ptIndex = msg.indexOf('.', findIndex);
			if (ptIndex == -1)
				break;

			for (beginIdx = ptIndex - 1; beginIdx >= 0; beginIdx--)
				if (!isURLChar(msg.charAt(beginIdx), true))
					break;
			for (endIdx = ptIndex + 1; endIdx < size; endIdx++)
				if (!isURLChar(msg.charAt(endIdx), false))
					break;
			if ((beginIdx == -1) || !isURLChar(msg.charAt(beginIdx), true))
				beginIdx++;

			findIndex = endIdx;
			if ((ptIndex == beginIdx) || (endIdx - ptIndex < 2))
				continue;

			result.addElement("http:\57\57" + msg.substring(beginIdx, endIdx));
		}

		return (result.size() == 0) ? null : result;
	}

	static public int strToIntDef(String str, int defValue)
	{
		if (str == null)
			return defValue;
		int result = defValue;
		try
		{
			result = Integer.parseInt(str);
		} catch (Exception e)
		{
		}
		return result;
	}

	static public String replaceStr(String original, String from, String to)
	{
		int index = original.indexOf(from);
		if (index == -1)
			return original;
		return original.substring(0, index) + to
				+ original.substring(index + from.length(), original.length());
	}

	static public byte[] explodeToBytes(String text, char serparator, int radix)
	{
		String[] strings = explode(text, serparator);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		for (int i = 0; i < strings.length; i++)
		{
			String item = strings[i];
			if (item.charAt(0) == '*')
				for (int j = 1; j < item.length(); j++)
					bytes.write((byte) item.charAt(j));
			else
				bytes.write(Integer.parseInt(item, radix));

		}
		return bytes.toByteArray();
	}

	/* Divide text to array of parts using serparator charaster */
	static public String[] explode(String text, char serparator)
	{
		Vector tmp = new Vector();
		StringBuffer strBuf = new StringBuffer();
		int len = text.length();
		for (int i = 0; i < len; i++)
		{
			char chr = text.charAt(i);
			if (chr == serparator)
			{
				tmp.addElement(strBuf.toString());
				strBuf.delete(0, strBuf.length());
			} else
				strBuf.append(chr);
		}
		tmp.addElement(strBuf.toString());
		String[] result = new String[tmp.size()];
		tmp.copyInto(result);
		return result;
	}
}
