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

import jimm.ContactListContactItem;
import jimm.ContactListGroupItem;
import jimm.ContactList;
import jimm.Options;
import jimm.util.ResourceBundle;


public class Util
{
	// Client CAPS
	public static final byte[] CAP_AIM_SERVERRELAY = { (byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x49, (byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1, (byte) 0x82, (byte) 0x22,            (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00};
	public static final byte[] CAP_UTF8 = { (byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x4E, (byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1, (byte) 0x82, (byte) 0x22,            (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00};
	public static final byte[] CAP_UTF8_GUID = { (byte) 0x7b, (byte) 0x30, (byte) 0x39, (byte) 0x34, (byte) 0x36, (byte) 0x31, (byte) 0x33, (byte) 0x34, (byte) 0x45, (byte) 0x2D,            (byte) 0x34, (byte) 0x43, (byte) 0x37, (byte) 0x46, (byte) 0x2D, (byte) 0x31, (byte) 0x31, (byte) 0x44, (byte) 0x31, (byte) 0x2D, (byte) 0x38, (byte) 0x32, (byte) 0x32, (byte) 0x32, (byte) 0x2D, (byte) 0x34, (byte) 0x34, (byte) 0x34, (byte) 0x35, (byte) 0x35, (byte) 0x33, (byte) 0x35, (byte) 0x34, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x7D};
	private static final byte[] CAP_MIRANDAIM = {(byte)0x4D, (byte)0x69, (byte)0x72, (byte)0x61,(byte)0x6E,(byte) 0x64, (byte)0x61,(byte) 0x4D, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
	private static final byte[] CAP_TRILLIAN  = { (byte) 0x97, (byte) 0xb1, (byte) 0x27, (byte) 0x51, (byte) 0x24, (byte) 0x3c, (byte) 0x43, (byte) 0x34, (byte) 0xad, (byte) 0x22, (byte) 0xd6, (byte) 0xab, (byte) 0xf7, (byte) 0x3f, (byte) 0x14, (byte) 0x09};
	private static final byte[] CAP_TRILCRYPT = { (byte) 0xf2, (byte) 0xe7, (byte) 0xc7, (byte) 0xf4, (byte) 0xfe, (byte) 0xad, (byte) 0x4d, (byte) 0xfb, (byte) 0xb2, (byte) 0x35, (byte) 0x36, (byte) 0x79, (byte) 0x8b, (byte) 0xdf, (byte) 0x00, (byte) 0x00};
	private static final byte[] CAP_SIM       = {'S', 'I', 'M', ' ', 'c', 'l', 'i', 'e', 'n', 't', ' ', ' ', (byte) 0, (byte) 0, (byte) 0, (byte) 0};
	private static final byte[] CAP_SIMOLD    = { (byte) 0x97, (byte) 0xb1, (byte) 0x27, (byte) 0x51, (byte) 0x24, (byte) 0x3c, (byte) 0x43, (byte) 0x34, (byte) 0xad, (byte) 0x22, (byte) 0xd6, (byte) 0xab, (byte) 0xf7, (byte) 0x3f, (byte) 0x14, (byte) 0x00};
	private static final byte[] CAP_LICQ      = {'L', 'i', 'c', 'q', ' ', 'c', 'l', 'i', 'e', 'n', 't', ' ', (byte) 0, (byte) 0, (byte) 0, (byte) 0};
	private static final byte[] CAP_KOPETE    = {'K', 'o', 'p', 'e', 't', 'e', ' ', 'I', 'C', 'Q', ' ', ' ', (byte) 0, (byte) 0, (byte) 0, (byte) 0};
	private static final byte[] CAP_MICQ      = {'m', 'I', 'C', 'Q', ' ', (byte) 0xA9, ' ', 'R', '.', 'K', '.', ' ', (byte) 0, (byte) 0, (byte) 0, (byte) 0};
	private static final byte[] CAP_ANDRQ     = {'&', 'R', 'Q', 'i', 'n', 's', 'i', 'd', 'e', (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
	private static final byte[] CAP_QIP       = { (byte) 0x56, (byte) 0x3F, (byte) 0xC8, (byte) 0x09, (byte) 0x0B, (byte) 0x6F, (byte) 0x41, 'Q', 'I', 'P', ' ', '2', '0', '0', '5', 'a'};
	private static final byte[] CAP_IM2       = { (byte) 0x74, (byte)  0xED, (byte)  0xC3, (byte)  0x36, (byte)  0x44, (byte)  0xDF, (byte)  0x48, (byte)  0x5B, (byte)  0x8B, (byte)  0x1C, (byte)  0x67, (byte)  0x1A, (byte)  0x1F, (byte)  0x86, (byte)  0x09, (byte)  0x9F}; // IM2 Ext Msg
	private static final byte[] CAP_MACICQ    = { (byte) 0xdd, (byte)  0x16, (byte)  0xf2, (byte)  0x02, (byte)  0x84, (byte)  0xe6, (byte)  0x11, (byte)  0xd4, (byte)  0x90, (byte)  0xdb, (byte)  0x00, (byte)  0x10, (byte)  0x4b, (byte)  0x9b, (byte)  0x4b, (byte)  0x7d};
	private static final byte[] CAP_RICHTEXT  = { (byte) 0x97, (byte)  0xb1, (byte)  0x27, (byte)  0x51, (byte)  0x24, (byte)  0x3c, (byte)  0x43, (byte)  0x34, (byte)  0xad, (byte)  0x22, (byte)  0xd6, (byte)  0xab, (byte)  0xf7, (byte)  0x3f, (byte)  0x14, (byte)  0x92};
	private static final byte[] CAP_IS2001    = { (byte) 0x2e, (byte)  0x7a, (byte)  0x64, (byte)  0x75, (byte)  0xfa, (byte)  0xdf, (byte)  0x4d, (byte)  0xc8, (byte)  0x88, (byte)  0x6f, (byte)  0xea, (byte)  0x35, (byte)  0x95, (byte)  0xfd, (byte)  0xb6, (byte)  0xdf};
	private static final byte[] CAP_IS2002    = { (byte) 0x10, (byte)  0xcf, (byte)  0x40, (byte)  0xd1, (byte)  0x4c, (byte)  0x7f, (byte)  0x11, (byte)  0xd1, (byte)  0x82, (byte)  0x22, (byte)  0x44, (byte)  0x45, (byte)  0x53, (byte)  0x54, (byte)  0x00, (byte)  0x00};
	private static final byte[] CAP_STR20012  = { (byte) 0xa0, (byte)  0xe9, (byte)  0x3f, (byte)  0x37, (byte)  0x4f, (byte)  0xe9, (byte)  0xd3, (byte)  0x11, (byte)  0xbc, (byte)  0xd2, (byte)  0x00, (byte)  0x04, (byte)  0xac, (byte)  0x96, (byte)  0xdd, (byte)  0x96};
	private static final byte[] CAP_AIMICON   = { (byte) 0x09, (byte)  0x46, (byte)  0x13, (byte)  0x46, (byte)  0x4c, (byte)  0x7f, (byte)  0x11, (byte)  0xd1, (byte)  0x82, (byte)  0x22, (byte)  0x44, (byte)  0x45, (byte)  0x53, (byte)  0x54, (byte)  0x00, (byte)  0x00}; // CAP_AIM_BUDDYICON
	private static final byte[] CAP_AIMIMIMAGE= { (byte) 0x09, (byte)  0x46, (byte)  0x13, (byte)  0x45, (byte)  0x4c, (byte)  0x7f, (byte)  0x11, (byte)  0xd1, (byte)  0x82, (byte)  0x22, (byte)  0x44, (byte)  0x45, (byte)  0x53, (byte)  0x54, (byte)  0x00, (byte)  0x00}; // CAP_AIM_BUDDYICON	
	private static final byte[] CAP_AIMCHAT   = { (byte) 0x74, (byte)  0x8F, (byte)  0x24, (byte)  0x20, (byte)  0x62, (byte)  0x87, (byte)  0x11, (byte)  0xD1, (byte)  0x82, (byte)  0x22, (byte)  0x44, (byte)  0x45, (byte)  0x53, (byte)  0x54, (byte)  0x00, (byte)  0x00};
	private static final byte[] CAP_UIM       = { (byte) 0xA7, (byte)  0xE4, (byte)  0x0A, (byte)  0x96, (byte)  0xB3, (byte)  0xA0, (byte)  0x47, (byte)  0x9A, (byte)  0xB8, (byte)  0x45, (byte)  0xC9, (byte)  0xE4, (byte)  0x67, (byte)  0xC5, (byte)  0x6B, (byte)  0x1F};
	private static final byte[] CAP_RAMBLER   = { (byte) 0x7E, (byte)  0x11, (byte)  0xB7, (byte)  0x78, (byte)  0xA3, (byte)  0x53, (byte)  0x49, (byte)  0x26, (byte)  0xA8, (byte)  0x02, (byte)  0x44, (byte)  0x73, (byte)  0x52, (byte)  0x08, (byte)  0xC4, (byte)  0x2A};
	private static final byte[] CAP_ABV       = { (byte) 0x00, (byte)  0xE7, (byte)  0xE0, (byte)  0xDF, (byte)  0xA9, (byte)  0xD0, (byte)  0x4F, (byte)  0xe1, (byte)  0x91, (byte)  0x62, (byte)  0xC8, (byte)  0x90, (byte)  0x9A, (byte)  0x13, (byte)  0x2A, (byte)  0x1B};
	private static final byte[] CAP_NETVIGATOR= { (byte) 0x4C, (byte)  0x6B, (byte)  0x90, (byte)  0xA3, (byte)  0x3D, (byte)  0x2D, (byte)  0x48, (byte)  0x0E, (byte)  0x89, (byte)  0xD6, (byte)  0x2E, (byte)  0x4B, (byte)  0x2C, (byte)  0x10, (byte)  0xD9, (byte)  0x9F};
	private static final byte[] CAP_XTRAZ	  = { (byte) 0x1A,  (byte) 0x09,  (byte) 0x3C,  (byte) 0x6C,  (byte) 0xD7,  (byte) 0xFD,  (byte) 0x4E,  (byte) 0xC5,  (byte) 0x9D,  (byte) 0x51,  (byte) 0xA6,  (byte) 0x47,  (byte) 0x4E,  (byte) 0x34,  (byte) 0xF5,  (byte) 0xA0};
	private static final byte[] CAP_AIMFILE	  = { (byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x43, (byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1, (byte) 0x82, (byte) 0x22, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00};
	private static final byte[] CAP_DIRECT	  = { (byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x44, (byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1, (byte) 0x82, (byte) 0x22, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00};	
	private static final byte[] CAP_JIMM	  = {'J', 'i','m','m',' '};
	private static final byte[] CAP_AVATAR	  = { (byte) 0x09, (byte) 0x46, (byte) 0x13, (byte) 0x4C, (byte) 0x4C, (byte) 0x7F, (byte) 0x11, (byte) 0xD1, (byte) 0x82, (byte) 0x22, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00};
	private static final byte[] CAP_TYPING	  = { (byte) 0x56, (byte) 0x3f, (byte) 0xc8, (byte) 0x09, (byte) 0x0b, (byte) 0x6f, (byte) 0x41, (byte) 0xbd, (byte) 0x9f, (byte) 0x79, (byte) 0x42, (byte) 0x26, (byte) 0x09, (byte) 0xdf, (byte) 0xa2, (byte) 0xf3};

	// No capability
	public static final int CAPF_NO_INTERNAL = 0x00000000;
	// Client unterstands type-2 messages
	public static final int CAPF_AIM_SERVERRELAY_INTERNAL = 0x00000001;
	// Client unterstands UTF-8 messages
	public static final int CAPF_UTF8_INTERNAL = 0x00000002;
	// Client capabilities for detection
	public static final int CAPF_MIRANDAIM = 0x00000004;
 	public static final int CAPF_TRILLIAN =0x00000008;
	public static final int CAPF_TRILCRYPT = 0x00000010;
	public static final int CAPF_SIM = 0x00000020;
	public static final int CAPF_SIMOLD = 0x00000040;
	public static final int CAPF_LICQ = 0x00000080;
	public static final int CAPF_KOPETE = 0x00000100;
	public static final int CAPF_MICQ = 0x00000200;
	public static final int CAPF_ANDRQ = 0x00000400;
	public static final int CAPF_QIP = 0x000000800;
	public static final int CAPF_IM2 = 0x00001000;
	public static final int CAPF_MACICQ = 0x00002000;
	public static final int CAPF_RICHTEXT = 0x00004000;
	public static final int CAPF_IS2001 = 0x00008000;
	public static final int CAPF_IS2002 = 0x00010000;
	public static final int CAPF_STR20012 = 0x00020000;
	public static final int CAPF_AIMICON = 0x00040000;
	public static final int CAPF_AIMCHAT = 0x00080000;
	public static final int CAPF_UIM = 0x00100000;
	public static final int CAPF_RAMBLER = 0x00200000;
	public static final int CAPF_ABV = 0x00400000;
	public static final int CAPF_NETVIGATOR = 0x00800000;
	public static final int CAPF_XTRAZ = 0x01000000;
	public static final int CAPF_AIMFILE = 0x02000000;
	public static final int CAPF_JIMM = 0x04000000;
	public static final int CAPF_AIMIMIMAGE = 0x08000000;
	public static final int CAPF_AVATAR = 0x10000000;
	public static final int CAPF_DIRECT = 0x20000000;
	public static final int CAPF_TYPING = 0x40000000;

	// Client IDs
	public static final byte CLI_NONE = 0;
	public static final byte CLI_QIP = 1;
	public static final byte CLI_MIRANDA = 2;
	public static final byte CLI_LICQ = 3;
	public static final byte CLI_TRILLIAN = 4;
	public static final byte CLI_SIM = 5;
	public static final byte CLI_KOPETE = 6;
	public static final byte CLI_MICQ = 7;
	public static final byte CLI_ANDRQ = 8;
	public static final byte CLI_IM2 = 9;
	public static final byte CLI_MACICQ = 10;
	public static final byte CLI_AIM = 11;
	public static final byte CLI_UIM = 12;
	public static final byte CLI_WEBICQ = 13;
	public static final byte CLI_GAIM = 14;
	public static final byte CLI_ALICQ = 15;
	public static final byte CLI_STRICQ = 16;
	public static final byte CLI_YSM = 17;
	public static final byte CLI_VICQ = 18;
	public static final byte CLI_LIBICQ2000 = 19;
	public static final byte CLI_JIMM = 20;
	public static final byte CLI_SMARTICQ = 21;
	public static final byte CLI_ICQLITE4 = 22;
	public static final byte CLI_ICQLITE5 = 23;
	public static final byte CLI_ICQ98 = 24;
	public static final byte CLI_ICQ99 = 25;
	public static final byte CLI_ICQ2001B = 26;
	public static final byte CLI_ICQ2002A2003A = 27;
	public static final byte CLI_ICQ2000 = 28;
	public static final byte CLI_ICQ2003B = 29;
	public static final byte CLI_ICQLITE = 30;
	public static final byte CLI_GNOMEICQ = 31;	
	public static final byte CLI_AGILE = 32;
	public static final byte CLI_SPAM = 33;
	public static final byte CLI_CENTERICQ = 34;
	public static final byte CLI_LIBICQJABBER = 35;
	public static final byte CLI_ICQ2GO = 36;
	public static final byte CLI_ICQPPC = 37;
	public static final byte CLI_STICQ = 38;
	
	private static final String[] clientNames = {
		"Not detected",
		"QIP",
		"Miranda",
		"LIcq",
		"Trillian",
		"SIM",
		"Kopete",
		"MICQ",
		"&RQ",
		"IM2",
		"ICQ for MAC",
		"AIM",
		"UIM",
		"WebICQ",
		"Gaim",
		"Alicq",
		"StrICQ",
		"YSM",
		"vICQ",
		"Libicq2000",
		"Jimm",
		"SmartICQ",
		"ICQ Lite v4",
		"ICQ Lite v5",
		"ICQ 98",
		"ICQ 99",
		"ICQ 2001b",
		"ICQ 2002a/2003a",
		"ICQ 2000",
		"ICQ 2003b",
		"ICQ Lite",
		"Gnome ICQ",
		"Agile Messenger",
		"SPAM:)",
		"CenterICQ",
		"Libicq2000 from Jabber",
		"ICQ2GO!",
		"ICQ for Pocket PC",
		"StIcq"
	};
	
	public static void detectUserClient(String uin, int dwFP1, int dwFP2, int dwFP3, int caps, int wVersion)
	{
		int client = CLI_NONE;
		String szVersion=null;
		
		switch(1)
		{
		default:
			if ( (caps&CAPF_QIP) !=0)
			{
				client = CLI_QIP;
				break;
			}
		
			if ( ((caps&(CAPF_TRILLIAN+CAPF_TRILCRYPT))!=0) && (dwFP1 == 0x3b75ac09 ) )
			{
				client = CLI_TRILLIAN;
				break;
			}
			
			if ( ((caps&CAPF_IM2)!=0) && (dwFP1 == 0x3FF19BEB) )
			{
				client = CLI_IM2;
				break;
			}
			
			if ( (caps&(CAPF_SIM+CAPF_SIMOLD))!=0 )
			{
				client = CLI_SIM;
				break;
			}
			
			if ( (caps&CAPF_KOPETE)!=0 )
			{
				client = CLI_KOPETE;
				break;
			}
			
			if ( (caps&CAPF_LICQ)!=0 )
			{
				client = CLI_LICQ;
				break;
			}
			
			if (((caps&CAPF_AIMICON)!=0)&&((caps&CAPF_AIMFILE)!=0)&&((caps&CAPF_AIMIMIMAGE)!=0) )
			{
				client = CLI_GAIM;
				break;
			}
			
			if ( (caps&CAPF_UTF8_INTERNAL)!=0)
			{
				switch (wVersion) {
					case 10:
					if ( ((caps&CAPF_TYPING)!=0) && ((caps&CAPF_RICHTEXT)!=0) )
					{
						client = CLI_ICQ2003B;
					}
					case 7:
					if ( ((caps&CAPF_AIM_SERVERRELAY_INTERNAL)==0)&&((caps&CAPF_DIRECT)==0) && (dwFP1==0) && (dwFP2==0) && (dwFP3==0) )
					{
						client = CLI_ICQ2GO;
					}
					break;
					default:
					if ( (dwFP1==0) && (dwFP2==0) && (dwFP3==0) )
					{
						if ( (caps&CAPF_RICHTEXT)!=0 )
						{
							client = CLI_ICQLITE;
							if ( ((caps&CAPF_AVATAR)!=0) && ((caps&CAPF_XTRAZ)!=0) )
							{
								if ( (caps&CAPF_AIMFILE)!=0 ) // TODO: add more
									client = client = CLI_ICQLITE5;
								else
									client = client = CLI_ICQLITE4;
							}
						}
						else
							if ( (caps&CAPF_UIM)!=0 )
								client = CLI_UIM;
							else
								client = CLI_AGILE;
					}
					break;
				}
			}
			
			if ( (caps&CAPF_MACICQ)!=0 )
			{
				client = CLI_MACICQ;
				break;
			}
			
			if ( (caps&CAPF_AIMCHAT)!=0 )
			{
				client = CLI_AIM;
				break;
			}
			
			if (  (dwFP1 & 0xFF7F0000) == 0x7D000000 )
			{
				client = CLI_LICQ;
				int ver = dwFP1 & 0xFFFF;
				if (ver % 10 !=0)
				{
					szVersion += ver / 1000 + "."+(ver / 10) % 100+"."+ ver % 10;
				}
				else
				{
					szVersion += ver / 1000 + "."+(ver / 10) % 100;
				}
				break;
			}
			
			switch (dwFP1) {
				case 0xFFFFFFFF:
				if ((dwFP3 == 0xFFFFFFFF) && (dwFP2 == 0xFFFFFFFF)) 
				{
					client = CLI_GAIM;
					break;
				}
				if ( (dwFP2==0) && (dwFP3 != 0xFFFFFFFF) )
				{
					if (wVersion == 7) 
					{
						client = CLI_WEBICQ;
						break;
					}
					if ( (dwFP3 == 0x3B7248ED) && ((caps&CAPF_UTF8_INTERNAL)==0) && ((caps&CAPF_RICHTEXT)==0 ) ) 
					{
						client = CLI_SPAM;
						break;
					}
				}
				client = CLI_MIRANDA;
				szVersion = ((dwFP2>>24)&0x7F)+"."+((dwFP2>>16)&0xFF)+"."+((dwFP2>>8)&0xFF)+"."+(dwFP2&0xFF);
				break;
				case 0xFFFFFFFE:
				if (dwFP3==dwFP1) 
				{
					client = CLI_JIMM;
				}
				break;
				case 0xFFFFFF8F:
				client = CLI_STRICQ;
				break;
				case 0xFFFFFF42:
				client = CLI_MICQ;
				break;
				case 0xFFFFFFBE:
				client = CLI_ALICQ;
				break;
				case 0xFFFFFF7F:
				client = CLI_ANDRQ;
				szVersion = ((dwFP2>>24)&0xFF)+"."+((dwFP2>>16)&0xFF)+"."+((dwFP2>>8)&0xFF)+"."+(dwFP2&0xFF);
				break;
				case 0xFFFFFFAB:
				client = CLI_YSM;
				break;
				case 0x04031980:
				client = CLI_VICQ;
				break;
				case 0x3AA773EE:
				if ((dwFP2 == 0x3AA66380) && (dwFP3 == 0x3A877A42))
				{
					if (wVersion==7)
					{
						if ( ((caps&CAPF_AIM_SERVERRELAY_INTERNAL)!=0) && ((caps&CAPF_DIRECT)!=0) )
						{
							if ( (caps&CAPF_RICHTEXT)!=0 ) 
							{
								client = CLI_CENTERICQ;
								break;
							}
							client = CLI_LIBICQJABBER;
						}
					}
					client = CLI_LIBICQ2000;
				}
				break;
				case 0x3b75ac09:
				client = CLI_TRILLIAN;
				break;
				case 0x3BA8DBAF: // FP2: 0x3BEB5373; FP3: 0x3BEB5262;
				if (wVersion==2)
					client = CLI_STICQ;
				break;
				case 0x3FF19BEB:
				if ( (wVersion==8) && (dwFP1 == dwFP3) ) //FP2: 0x3FEC05EB; FP3: 0x3FF19BEB;
				client = CLI_IM2;
				break;
				case 0x4201F414:
				if ( ((dwFP2 & dwFP3) == dwFP1) && (wVersion == 8) )
					client = CLI_SPAM;
				break;
				default: break;
			}
			
			if (client != CLI_NONE) break;
			
			if ( (dwFP1!=0) && (dwFP1 == dwFP3) && (dwFP3 == dwFP2) && (caps==0)) 
			{
				client = CLI_VICQ;
				break;
			}
			if ( ((caps&CAPF_AIM_SERVERRELAY_INTERNAL)!=0) && ((caps&CAPF_DIRECT)!=0) && ((caps&CAPF_UTF8_INTERNAL)!=0) && ((caps&CAPF_RICHTEXT)!=0) ) 
			{
				
				if ( (dwFP1!=0) && (dwFP2!=0) && (dwFP3!=0) )
					client = CLI_ICQ2002A2003A;
				break;
			}
			if ( ((caps&(CAPF_STR20012+CAPF_AIM_SERVERRELAY_INTERNAL))!=0) && ((caps&CAPF_IS2001)!=0) )
			{
				if ( (dwFP1==0) && (dwFP2==0) && (dwFP3==0) && (wVersion==0))
					client = CLI_ICQPPC;
				else
					client = CLI_ICQ2001B; //FP1: 1068985885; FP2:0; FP3:1068986138
				break;
			}
			if (wVersion==7) 
			{
				if( ((caps&CAPF_AIM_SERVERRELAY_INTERNAL)!=0)&&((caps&CAPF_DIRECT)!=0) )
				{
					if ( (dwFP1==0) && (dwFP2==0) && (dwFP3==0) )
						client = CLI_ANDRQ;
					else
						client = CLI_ICQ2000;
					break;
				}
				else
				if ( (caps&CAPF_RICHTEXT)!=0 ) 
				{
					client = CLI_GNOMEICQ;
					break;
				}
			}
			if (dwFP1 > 0x35000000 && dwFP1 < 0x40000000) 
			{
				switch(wVersion) 
				{
					case 6:  client = CLI_ICQ99;break;
					case 7:  client = CLI_ICQ2000;break;
					case 8:  client = CLI_ICQ2001B;break;
					case 9:  client = CLI_ICQLITE;break;
					case 10: client = CLI_ICQ2003B;break;
				}
				break;
			} 
		}
		ContactListContactItem item = ContactList.getItembyUIN(uin);
		if ((item!=null)&&(client!=CLI_NONE))
		{
			item.setIntValue(ContactListContactItem.CONTACTITEM_CLIENT,client);
			if ( szVersion!=null )
				item.setStringValue(ContactListContactItem.CONTACTITEM_CLIVERSION,szVersion);
		}
	}
	
	public static String getClientString(byte cli)
	{
		return ( clientNames[cli] );
	}

    private static void detectClientVersion(String uin, byte[] buf1, int cli, int tlvNum)
    {
	    byte[] buf = new byte[16];
	    System.arraycopy(buf1,tlvNum*16,buf,0,16);
	    String ver = "";
	    if (cli == Util.CAPF_MIRANDAIM )
	    {
		    if ( (buf[0xC]==0)&&(buf[0xD]==0)&&(buf[0xE]==0)&&(buf[0xF]==1) )
		    {
			    ver = "0.1.2.0";
		    }
		    else if ( (buf[0xC]==0)&&(buf[0xD]<=3)&&(buf[0xE]<=3)&&(buf[0xF]<=1) )
		    {
			    ver = "0." + buf[0xD] + "." +buf[0xE] +"." + buf[0xF];
		    }
		    else
		    {
			    ver = buf[0x8]+ "." + buf[0x9] + "." + buf[0xA] +"." + buf[0xB];
		    }
	    }
	    else if (cli == Util.CAPF_LICQ)
	    {
		    ver = buf[0xC] + "." + (buf[0xD]%100) +"." + buf[0xE];
	    }
	    else if (cli == Util.CAPF_KOPETE)
	    {
		    ver = buf[0xC] + "." +buf[0xD] + "." +buf[0xE] + "." +buf[0xF];
	    }
	    else if (cli == Util.CAPF_ANDRQ)
	    {
		    ver = (char)buf[0xC] + "." + (char)buf[0xB];// + "." +buf[0xA] + "." +buf[9];
	    }
	    else if (cli == Util.CAPF_JIMM)
		    ver = Util.byteArrayToString(buf,5,11);
	    else if (cli == Util.CAPF_QIP)
		    ver = Util.byteArrayToString(buf,11,5);
	    
	    ContactListContactItem item = ContactList.getItembyUIN(uin);
		if (item!=null)
				item.setStringValue(ContactListContactItem.CONTACTITEM_CLIVERSION,ver);
    }
    
    public static int parseCapabilities(String uin, byte[] tlvData)
    {
    	int capabilities = 0;
    	for (int j = 0; j < tlvData.length / 16; j++)
    	{
    		if (Util.byteArrayEquals(tlvData, j * 16, CAP_AIM_SERVERRELAY, 0, 16))
    		{
    			capabilities |= CAPF_AIM_SERVERRELAY_INTERNAL;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_UTF8, 0, 16))
    		{
    			capabilities |= CAPF_UTF8_INTERNAL;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_MIRANDAIM, 0, 8))
    		{
    			capabilities |= CAPF_MIRANDAIM;
    			detectClientVersion(uin, tlvData,CAPF_MIRANDAIM,j);
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_TRILLIAN, 0, 16))
    		{
    			capabilities |= CAPF_TRILLIAN;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_TRILCRYPT, 0, 16))
    		{
    			capabilities |= CAPF_TRILCRYPT;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_SIM, 0, 0xC))
    		{
    			capabilities |= CAPF_SIM;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_SIMOLD, 0, 16))
    		{
    			capabilities |= CAPF_SIMOLD;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_LICQ, 0, 0xC))
    		{
    			capabilities |= CAPF_LICQ;
    			detectClientVersion(uin, tlvData, CAPF_LICQ,j);
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_KOPETE, 0, 0xC))
    		{
    			capabilities |= CAPF_KOPETE;
    			detectClientVersion(uin, tlvData, CAPF_KOPETE,j);
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_MICQ, 0, 16))
    		{
    			capabilities |= CAPF_MICQ;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_ANDRQ, 0, 9))
    		{
    			capabilities |= CAPF_ANDRQ;
    			detectClientVersion(uin, tlvData,CAPF_ANDRQ,j);
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_QIP, 0, 11))
    		{
    			capabilities |= CAPF_QIP;
    			detectClientVersion(uin, tlvData,CAPF_QIP,j);
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_IM2, 0, 16))
    		{
    			capabilities |= CAPF_IM2;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_MACICQ, 0, 16))
    		{
    			capabilities |= CAPF_MACICQ;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_RICHTEXT, 0, 16))
    		{
    			capabilities |= CAPF_RICHTEXT;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_IS2001, 0, 16))
    		{
    			capabilities |= CAPF_IS2001;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_IS2002, 0, 16))
    		{
    			capabilities |= CAPF_IS2002;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_STR20012, 0, 16))
    		{
    			capabilities |= CAPF_STR20012;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_AIMICON, 0, 16))
    		{
    			capabilities |= CAPF_AIMICON;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_AIMCHAT, 0, 16))
    		{
    			capabilities |= CAPF_AIMCHAT;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_UIM, 0, 16))
    		{
    			capabilities |= CAPF_UIM;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_RAMBLER, 0, 16))
    		{
    			capabilities |= CAPF_RAMBLER;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_ABV, 0, 16))
    		{
    			capabilities |= CAPF_ABV;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_NETVIGATOR, 0, 16))
    		{
    			capabilities |= CAPF_NETVIGATOR;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_XTRAZ, 0, 16))
    		{
    			capabilities |= CAPF_XTRAZ;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_AIMFILE, 0, 16))
    		{
    			capabilities |= CAPF_AIMFILE;
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_JIMM, 0, 5))
    		{
    			capabilities |= CAPF_JIMM;
    			detectClientVersion(uin, tlvData,CAPF_JIMM,j);
    		} else if (Util.byteArrayEquals(tlvData, j * 16, CAP_AIMIMIMAGE, 0, 16))
    			capabilities |= CAPF_AIMIMIMAGE;
    		else if (Util.byteArrayEquals(tlvData, j * 16, CAP_AVATAR, 0, 16))
    			capabilities |= CAPF_AVATAR;
    		else if (Util.byteArrayEquals(tlvData, j * 16, CAP_DIRECT, 0, 16))
    			capabilities |= CAPF_DIRECT;
    		else if (Util.byteArrayEquals(tlvData, j * 16, CAP_TYPING, 0, 16))
    			capabilities |= CAPF_TYPING;			      
    	}
    	return capabilities;
    }
    
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
		return (getDateString(onlyTime, new Date()));
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
        // #sijapp cond.else#
        date.setTime(value.getTime() + TimeZone.getDefault().getRawOffset());
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
		if (Options.getBooleanOption(Options.OPTION_CP1251_HACK))
		{
			return (byteArray1251ToString(buf, off, len));
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
    public static String byteArrayToHexString(byte[] buf) {
        StringBuffer hexString = new StringBuffer(buf.length);
        String hex;
        for (int i = 0; i < buf.length; i++) {
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
				return (baos.toByteArray());
			}
			catch (Exception e)
			{
				// Do nothing
			}
		}

		// CP1251 or default character encoding?
		if (Options.getBooleanOption(Options.OPTION_CP1251_HACK))
		{
			return (stringToByteArray1251(val));
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
		if (status == ContactList.STATUS_INVIS_ALL) return (Util.SET_STATUS_INVISIBLE);
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
    
    // #sijapp cond.if modules_PROXY is "true"#
    // Try to parse string IP
    public static boolean isIP(String ip)
    {
        int digit = 0;
        int i;
        try
        {
            for (int j = 0; j < 3; j++)
            {

                for (i = 0; i < 3; i++)
                {
                    if (ip.charAt(i) == '.') break;
                }

                digit = Integer.parseInt(ip.substring(0, i));
                ip = ip.substring(i + 1);
            }

            digit = Integer.parseInt(ip);

            return true;
        } catch (NumberFormatException e)
        {
            return false;
        }
    }

    // #sijapp cond.end #
    
    // Create a random id which is not used yet
    public static int createRandomId()
    {
		// Max value is probably 0x7FFF, lowest value is unknown.
		// We use range 0x1000-0x7FFF.
        // From miranda source
        
        int range  = 0x6FFF;
        
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
                    if (cItems[j].getIntValue(ContactListContactItem.CONTACTITEM_ID) == randint)
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


	// Convert gender code to string
	static public String genderToString(int gender)
	{
        switch (gender)
		{
        case 1: return ResourceBundle.getString("female");
        case 2: return ResourceBundle.getString("male");
        }
        return new String();
	}
	
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

