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
 File: src/jimm/comm/ConnectPacket.java
 Version: 0.3.1  Date: 2004/12/25
 Author(s): Manuel Linsmayer
 *******************************************************************************/


package jimm.comm;


import jimm.JimmException;


public class ConnectPacket extends Packet
{


	// Packet types
	public static final int SRV_CLI_HELLO = 1;
	public static final int CLI_COOKIE = 2;
	public static final int CLI_IDENT = 3;


	// Fixed values for various fields in CLI_IDENT packets
	public static final String FIXED_VERSION = "ICQ Inc. - Product of ICQ (TM).2003a.5.47.1.3800.85";
	public static final byte[] FIXED_UNKNOWN = {(byte) 0x01, (byte) 0x0A};
	public static final byte[] FIXED_VER_MAJOR = {(byte) 0x00, (byte) 0x05};
	public static final byte[] FIXED_VER_MINOR = {(byte) 0x00, (byte) 0x2F};
	public static final byte[] FIXED_VER_LESSER = {(byte) 0x00, (byte) 0x01};
	public static final byte[] FIXED_VER_BUILD = {(byte) 0x0E, (byte) 0xD8};
	public static final byte[] FIXED_VER_SUBBUILD = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x55};


	// Cookie (!= null only if packet type is CLI_COOKIE)
	protected byte[] cookie;


	// UIN (!= null only if packet type is CLI_IDENT)
	protected String uin;


	// Password (unencrypted, != null only if packet type is CLI_IDENT)
	protected String password;


	// Language that the client is using (!= null only if packet type is CLI_IDENT)
	protected String language;


	// Country where the client is located (!= null only if packet type is CLI_IDENT)
	protected String country;


	// Constructs a SRV_HELLO/CLI_HELLO packet
	public ConnectPacket(int sequence)
	{
		this.sequence = sequence;
		this.cookie = null;
		this.uin = null;
		this.password = null;
		this.language = null;
		this.country = null;
	}


	// Constructs a SRV_HELLO/CLI_HELLO packet
	public ConnectPacket()
	{
		this(-1);
	}


	// Constructs a CLI_COOKIE packet
	public ConnectPacket(int sequence, byte[] cookie)
	{
		this.sequence = sequence;
		this.cookie = new byte[cookie.length];
		System.arraycopy(cookie, 0, this.cookie, 0, cookie.length);
		this.uin = null;
		this.password = null;
		this.language = null;
		this.country = null;
	}


	// Constructs a CLI_COOKIE packet
	public ConnectPacket(byte[] cookie)
	{
		this(-1, cookie);
	}


	// Constructs a CLI_IDENT packet
	public ConnectPacket(int sequence, String uin, String password, String language, String country)
	{
		this.sequence = sequence;
		this.cookie = null;
		this.uin = new String(uin);
		this.password = new String(password);
		this.language = new String(language);
		this.country = new String(country);
	}


	// Constructs a CLI_IDENT packet
	public ConnectPacket(String uin, String password, String language, String country)
	{
		this(-1, uin, password, language, country);
	}


	// Returns the packet type
	public int getType()
	{
		if ((this.cookie == null) && (this.uin == null))
		{
			return (ConnectPacket.SRV_CLI_HELLO);
		}
		else if (this.uin != null)
		{
			return (ConnectPacket.CLI_IDENT);
		}
		else
		{
			return (ConnectPacket.CLI_COOKIE);
		}
	}


	// Returns the cookie, or null if packet type is not CLI_COOKIE
	public byte[] getCookie()
	{
		if (this.getType() == ConnectPacket.CLI_COOKIE)
		{
			byte[] cookie = new byte[this.cookie.length];
			System.arraycopy(this.cookie, 0, cookie, 0, this.cookie.length);
			return (cookie);
		}
		else
		{
			return (null);
		}
	}


	// Returns the UIN, or null if packet type is not CLI_IDENT
	public String getUin()
	{
		if (this.getType() == ConnectPacket.CLI_IDENT)
		{
			return (new String(this.uin));
		}
		else
		{
			return (null);
		}
	}


	// Returns the password in clear text, or null if packet type is not CLI_IDENT
	public String getPassword()
	{
		if (this.getType() == ConnectPacket.CLI_IDENT)
		{
			return (new String(this.password));
		}
		else
		{
			return (null);
		}
	}


	// Returns the language that the client is using, or null if packet type is not CLI_IDENT
	public String getLanguage()
	{
		if (this.getType() == ConnectPacket.CLI_IDENT)
		{
			return (new String(this.language));
		}
		else
		{
			return (null);
		}
	}


	// Returns the country where the client is located, or null if packet type is not CLI_IDENT
	public String getCountry()
	{
		if (this.getType() == ConnectPacket.CLI_IDENT)
		{
			return (new String(this.country));
		}
		else
		{
			return (null);
		}
	}


	// Returns the package as byte array
	public byte[] toByteArray()
	{

		// Get package length
		int length = 10;
		if (this.getType() == ConnectPacket.CLI_COOKIE)
		{
			length += 4 + this.cookie.length;
		}
		else if (this.getType() == ConnectPacket.CLI_IDENT)
		{
			length += 4 + this.uin.length();
			length += 4 + this.password.length();
			length += 4 + ConnectPacket.FIXED_VERSION.length();
			length += 4 + ConnectPacket.FIXED_UNKNOWN.length;
			length += 4 + ConnectPacket.FIXED_VER_MAJOR.length;
			length += 4 + ConnectPacket.FIXED_VER_MINOR.length;
			length += 4 + ConnectPacket.FIXED_VER_LESSER.length;
			length += 4 + ConnectPacket.FIXED_VER_BUILD.length;
			length += 4 + ConnectPacket.FIXED_VER_SUBBUILD.length;
			length += 4 + this.language.length();
			length += 4 + this.country.length();
		}

		// Allocate memory
		byte[] buf = new byte[length];

		// Marker
		int marker = 0;

		// Assemble FLAP header
		Util.putByte(buf, marker, 0x2A);   // FLAP.ID
		Util.putByte(buf, marker + 1, 0x01);   // FLAP.CHANNEL
		Util.putWord(buf, marker + 2, this.sequence);   // FLAP.SEQUENCE
		Util.putWord(buf, marker + 4, length - 6);   // FLAP.LENGTH
		marker += 6;

		// Assemble HELLO.HELLO
		Util.putDWord(buf, marker, 0x00000001);
		marker += 4;

		// Assemble CLI_COOKIE
		if (this.getType() == ConnectPacket.CLI_COOKIE)
		{

			// HELLO.COOKIE
			Util.putWord(buf, marker, 0x0006);
			Util.putWord(buf, marker + 2, this.cookie.length);
			System.arraycopy(this.cookie, 0, buf, marker + 4, this.cookie.length);
			marker += 4 + this.cookie.length;

		}
		// Assemble CLI_IDENT
		else if (this.getType() == ConnectPacket.CLI_IDENT)
		{

			// HELLO.UIN
			Util.putWord(buf, marker, 0x0001);
			Util.putWord(buf, marker + 2, this.uin.length());
			byte[] uinRaw = Util.stringToByteArray(this.uin);
			System.arraycopy(uinRaw, 0, buf, marker + 4, uinRaw.length);
			marker += 4 + uinRaw.length;

			// HELLO.PASSWORD
			Util.putWord(buf, marker, 0x0002);
			Util.putWord(buf, marker + 2, this.password.length());
			byte[] passwordRaw = Util.decipherPassword(Util.stringToByteArray(this.password));
			System.arraycopy(passwordRaw, 0, buf, marker + 4, passwordRaw.length);
			marker += 4 + passwordRaw.length;

			// HELLO.VERSION
			Util.putWord(buf, marker, 0x0003);
			Util.putWord(buf, marker + 2, ConnectPacket.FIXED_VERSION.length());
			byte[] versionRaw = Util.stringToByteArray(ConnectPacket.FIXED_VERSION);
			System.arraycopy(versionRaw, 0, buf, marker + 4, versionRaw.length);
			marker += 4 + versionRaw.length;

			// HELLO.UNKNOWN
			Util.putWord(buf, marker, 0x0016);
			Util.putWord(buf, marker + 2, ConnectPacket.FIXED_UNKNOWN.length);
			System.arraycopy(ConnectPacket.FIXED_UNKNOWN, 0, buf, marker + 4, ConnectPacket.FIXED_UNKNOWN.length);
			marker += 6;

			// HELLO.VER_MAJOR
			Util.putWord(buf, marker, 0x0017);
			Util.putWord(buf, marker + 2, ConnectPacket.FIXED_VER_MAJOR.length);
			System.arraycopy(ConnectPacket.FIXED_VER_MAJOR, 0, buf, marker + 4, ConnectPacket.FIXED_VER_MAJOR.length);
			marker += 6;

			// HELLO.VER_MINOR
			Util.putWord(buf, marker, 0x0018);
			Util.putWord(buf, marker + 2, ConnectPacket.FIXED_VER_MINOR.length);
			System.arraycopy(ConnectPacket.FIXED_VER_MINOR, 0, buf, marker + 4, ConnectPacket.FIXED_VER_MINOR.length);
			marker += 6;

			// HELLO.VER_LESSER
			Util.putWord(buf, marker, 0x0019);
			Util.putWord(buf, marker + 2, ConnectPacket.FIXED_VER_LESSER.length);
			System.arraycopy(ConnectPacket.FIXED_VER_LESSER, 0, buf, marker + 4, ConnectPacket.FIXED_VER_LESSER.length);
			marker += 6;

			// HELLO.VER_BUILD
			Util.putWord(buf, marker, 0x001A);
			Util.putWord(buf, marker + 2, ConnectPacket.FIXED_VER_BUILD.length);
			System.arraycopy(ConnectPacket.FIXED_VER_BUILD, 0, buf, marker + 4, ConnectPacket.FIXED_VER_BUILD.length);
			marker += 6;

			// HELLO.VER_SUBBUILD
			Util.putWord(buf, marker, 0x0014);
			Util.putWord(buf, marker + 2, ConnectPacket.FIXED_VER_SUBBUILD.length);
			System.arraycopy(ConnectPacket.FIXED_VER_SUBBUILD, 0, buf, marker + 4, ConnectPacket.FIXED_VER_SUBBUILD.length);
			marker += 8;

			// HELLO.LANGUAGE
			Util.putWord(buf, marker, 0x000F);
			Util.putWord(buf, marker + 2, this.language.length());
			byte[] languageRaw = Util.stringToByteArray(this.language);
			System.arraycopy(languageRaw, 0, buf, marker + 4, languageRaw.length);
			marker += 4 + languageRaw.length;

			// HELLO.COUNTRY
			Util.putWord(buf, marker, 0x000E);
			Util.putWord(buf, marker + 2, this.country.length());
			byte[] countryRaw = Util.stringToByteArray(this.country);
			System.arraycopy(countryRaw, 0, buf, marker + 4, countryRaw.length);
			marker += 4 + countryRaw.length;

		}

		// Return
		return (buf);

	}


	// Parses given byte array and returns a Packet object
	public static Packet parse(byte[] buf, int off, int len) throws JimmException
	{

		// Get FLAP sequence number
		int flapSequence = Util.getWord(buf, off + 2);

		// Get length of FLAP data
		int flapLength = Util.getWord(buf, off + 4);

		// Check HELLO
		if ((flapLength < 4) || (Util.getDWord(buf, off + 6) != 0x00000001))
		{
			throw (new JimmException(132, 0));
		}

		// Variables for all possible TLVs
		byte[] cookie = null;
		String uin = null;
		String password = null;
		String version = null;
		byte[] unknown = null;
		byte[] verMajor = null;
		byte[] verMinor = null;
		byte[] verLesser = null;
		byte[] verBuild = null;
		byte[] verSubbuild = null;
		String language = null;
		String country = null;

		// Read all TLVs
		int marker = off + 10;
		while (marker < (off + len))
		{

			// Get next TLV
			byte[] tlvValue = Util.getTlv(buf, marker);
			if (tlvValue == null)
			{
				throw (new JimmException(132, 1));
			}

			// Get type of next TLV
			int tlvType = Util.getWord(buf, marker);

			// Update markers
			marker += 4 + tlvValue.length;

			// Save value
			switch (tlvType)
			{
				case 0x0006:   // cookie
					cookie = tlvValue;
					break;
				case 0x0001:   // uin
					uin = Util.byteArrayToString(tlvValue);
					break;
				case 0x0002:   // password
					password = Util.byteArrayToString(Util.decipherPassword(tlvValue));
					break;
				case 0x0003:   // version
					version = Util.byteArrayToString(tlvValue);
					break;
				case 0x0016:   // unknown
					unknown = tlvValue;
					break;
				case 0x0017:   // verMajor
					verMajor = tlvValue;
					break;
				case 0x0018:   // verMinor
					verMinor = tlvValue;
					break;
				case 0x0019:   // verLesser
					verLesser = tlvValue;
					break;
				case 0x001A:   // verBuild
					verBuild = tlvValue;
					break;
				case 0x0014:   // verSubbuild
					verSubbuild = tlvValue;
					break;
				case 0x000F:   // language
					language = Util.byteArrayToString(tlvValue);
					break;
				case 0x000E:   // country
					country = Util.byteArrayToString(tlvValue);
					break;
				default:
					throw (new JimmException(132, 2));
			}

		}

		// SRV_HELLO/CLI_HELLO
		if ((cookie == null) && (uin == null) && (password == null) && (version == null) && (unknown == null) && (verMajor == null) &&
				(verMinor == null) && (verLesser == null) && (verBuild == null) && (verSubbuild == null) && (language == null) && (country == null))
		{
			return (new ConnectPacket(flapSequence));
		}
		// SRV_COOKIE
		else if ((cookie != null) && (uin == null) && (password == null) && (version == null) && (unknown == null) && (verMajor == null) &&
				(verMinor == null) && (verLesser == null) && (verBuild == null) && (verSubbuild == null) && (language == null) && (country == null))
		{
			return (new ConnectPacket(flapSequence, cookie));
		}
		// CLI_IDENT
		else if ((cookie == null) && (uin != null) && (password != null) && (version != null) && (unknown != null) && (verMajor != null) &&
				(verMinor != null) && (verLesser != null) && (verBuild != null) && (verSubbuild != null) && (language != null) && (country != null))
		{
			return (new ConnectPacket(flapSequence, uin, password, language, country));
		}
		// Other TLV combinations are not valid
		else
		{
			throw (new JimmException(132, 3));
		}

	}


	// Parses given byte array and returns a Packet object
	public static Packet parse(byte[] buf) throws JimmException
	{
		return (ConnectPacket.parse(buf, 0, buf.length));
	}


}
