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
 File: src/jimm/comm/Util.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher / Sergey Chernov / Andrey B. Ivlev
 *******************************************************************************/


package jimm.comm;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import jimm.ContactList;
import jimm.util.ResourceBundle;


public class Util
{


  // Password encryption key
  public static final byte[] PASSENC_KEY = {(byte) 0xF3, (byte) 0x26, (byte) 0x81, (byte) 0xC4,
                                            (byte) 0x39, (byte) 0x86, (byte) 0xDB, (byte) 0x92,
                                            (byte) 0x71, (byte) 0xA3, (byte) 0xB9, (byte) 0xE6,
                                            (byte) 0x53, (byte) 0x7A, (byte) 0x95, (byte) 0x7C};


  // Online status (set values)
  public static final long SET_STATUS_AWAY = 0x00000001;
  public static final long SET_STATUS_CHAT = 0x00000020;
  public static final long SET_STATUS_DND = 0x00000013;
  public static final long SET_STATUS_INVISIBLE = 0x00000100;
  public static final long SET_STATUS_NA = 0x00000005;
  public static final long SET_STATUS_OCCUPIED = 0x00000011;
  public static final long SET_STATUS_ONLINE = 0x00000000;


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
    {   // Big endian
      val = (((int) buf[off]) << 8) & 0x0000FF00;
      val |= (((int) buf[++off])) & 0x000000FF;
    }
    else
    {   // Little endian
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
    {   // Big endian
      buf[off] = (byte) ((val >> 8) & 0x000000FF);
      buf[++off] = (byte) ((val) & 0x000000FF);
    }
    else
    {   // Little endian
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
    {   // Big endian
      val = (((long) buf[off]) << 24) & 0xFF000000;
      val |= (((long) buf[++off]) << 16) & 0x00FF0000;
      val |= (((long) buf[++off]) << 8) & 0x0000FF00;
      val |= (((long) buf[++off])) & 0x000000FF;
    }
    else
    {   // Little endian
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
    {   // Big endian
      buf[off] = (byte) ((val >> 24) & 0x00000000000000FF);
      buf[++off] = (byte) ((val >> 16) & 0x00000000000000FF);
      buf[++off] = (byte) ((val >> 8) & 0x00000000000000FF);
      buf[++off] = (byte) ((val) & 0x00000000000000FF);
    }
    else
    {   // Little endian
      buf[off] = (byte) ((val) & 0x00000000000000FF);
      buf[++off] = (byte) ((val >> 8) & 0x00000000000000FF);
      buf[++off] = (byte) ((val >> 16) & 0x00000000000000FF);
      buf[++off] = (byte) ((val >> 24) & 0x00000000000000FF);
    }
  }


  //  the specified double (val) into the buffer (buf) at position off using big endian byte ordering
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

    // #sijapp cond.if cp1251 is "true" #
    // Read string in CP1251 (Cyrillic)
    return (Encoding.byteArray1251ToString(buf, off, len));
    // #sijapp cond.else #
    // Read string in default character encoding
    return (new String(buf, off, len));
    // #sijapp cond.end #

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

    // #sijapp cond.if cp1251 is "true" #
    // Write string in CP1251 (Cyrillic)
    return (Encoding.stringToByteArray1251(val));
    // #sijapp cond.else #
    // Write string in default character encoding
    return (val.getBytes());
    // #sijapp cond.end #

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


  // DeCipher password
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
    if ((status & ContactList.STATUS_OCCUPIED) != 0) return (ContactList.STATUS_OCCUPIED);
    if ((status & ContactList.STATUS_NA) != 0) return (ContactList.STATUS_NA);
    if ((status & ContactList.STATUS_AWAY) != 0) return (ContactList.STATUS_AWAY);
    if ((status & ContactList.STATUS_CHAT) != 0) return (ContactList.STATUS_CHAT);
    return (ContactList.STATUS_ONLINE);
  }


  // Get online status set value
  public static long translateStatusSend(long status)
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
      return ("0" + String.valueOf(number));
    else
      return (String.valueOf(number));
  }


}
