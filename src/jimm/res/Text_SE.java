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
 File: src/jimm/res/Text_SE.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Martin Björkman
 *******************************************************************************/


// #sijapp cond.if lang_SE is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_SE extends ResourceBundle
{


	public Text_SE()
	{

		// Labels
		resources.put(".",                           ".");
		resources.put("about",                       "Om");
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nICQ klient för J2ME\nVersion ###VERSION###\nSe http://www.jimm.org/");
		resources.put("account",                     "Konto");
		resources.put("add_user",                    "Lägg till användare");
		resources.put("async",                       "Async");
		resources.put("back",                        "Tillbaka");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("beep",                        "Pip");
		// #sijapp cond.end #
		resources.put("byte",                        "Byte");
		resources.put("cancel",                      "Avbryt");
		resources.put("close",                       "Stäng");
		resources.put("conn_type",                   "Connection type");
		resources.put("connect",                     "Anslut");
		resources.put("connecting",                  "Ansluter");
		resources.put("contact_list",                "Lista");
		resources.put("cp1251",                      "Use CP1252 hack?");
		resources.put("cpd",                         "Kostnad per dag");
		resources.put("cpp",                         "Kostnad per IP-paket");
		resources.put("currency",                    "Valuta");
		resources.put("delete_chat",                 "Radera chat");
		resources.put("user_menu",                   "Användarmeny");
		resources.put("disconnect",                  "Avsluta anslutningen");
		resources.put("disconnecting",               "Frånkopplar anslutning");
		resources.put("display_advertisement",       "Lägg till Jimm annons?");
		resources.put("display_date",                "Visa datum på splash-screen?");
		resources.put("email",                       "Email");
		resources.put("error",                       "Fel");
		resources.put("exit",                        "Avsluta");
		resources.put("hide_offline",                "Dölj offline kontakter");
		resources.put("info",                        "Info");
		resources.put("kb",                          "kB");
		resources.put("keep_chat",                   "Spara chatt-loggar?");
		resources.put("keep_conn_alive",             "Håll anslutningen vid liv?");
		resources.put("keylock_SEable",              "Aktivera knapplås?");
		resources.put("keylock_SEabled",             "Knapplås aktiverat");
		resources.put("language",                    "Språk");
		resources.put("lang_BR",                     "Portugisiska (Brasilien)");
		resources.put("lang_CZ",                     "Tjeckiska");
		resources.put("lang_DE",                     "Tyska");
		resources.put("lang_EN",                     "Engelska");
		resources.put("lang_ES",                     "Spanska");
		resources.put("lang_LT",                     "Litauiska");
		resources.put("lang_RU",                     "Ryska");
		resources.put("lang_SE",                     "Svenska");
		resources.put("loading",                     "Laddar");
		resources.put("me",                          "jag");
		resources.put("menu",                        "Meny");
		resources.put("message",                     "Meddelande");
		resources.put("message_from",                "Meddelande från");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("message_notification",        "Meddelande-signal");
		// #sijapp cond.end #
		resources.put("name",                        "Namn");
		resources.put("nick",                        "Smeknamn");
		resources.put("no",                          "Nej");
		resources.put("not_implemented",             "Funktionen är inte implementerad ännu.");
		resources.put("notice",                      "Notice");
		resources.put("ok",                          "OK");
		resources.put("once_a_session",              "En gång per session");
		resources.put("options",                     "Alternativ");
		resources.put("options_account",             "Konto");
		resources.put("options_cost",                "Kostnad");
		resources.put("options_effect",              "Du kanske måste återansluta för att inställningarna skall ta effekt!");
		resources.put("options_interface",           "Gränssnitt");
		resources.put("options_network",             "Nätverk");
		resources.put("options_other",               "Annat");
		resources.put("password",                    "Lösenord");
		resources.put("plength",                     "IP-Paket längd i kB");
		resources.put("remove",                      "Ta bort från lista");
		resources.put("reply",                       "Svara");
		resources.put("reset",                       "Nollställ");
		resources.put("save",                        "Spara");
		resources.put("search_user",                 "Sök efter användare");
		resources.put("send",                        "Sänd");
		resources.put("send_message",                "Nytt meddelande");
		resources.put("send_url",                    "Ny URL");
		resources.put("server",                      "Inloggnings-server");
		resources.put("server_host",                 "Värdnamn");
		resources.put("server_port",                 "Port");
		resources.put("session",                     "Session");
		resources.put("set_status",                  "Ändra status");
		resources.put("since",                       "Sedan");
		resources.put("sound",                       "ICQ-ljud");
		resources.put("sound_file_name",             "Filnamn");
		resources.put("sort_by",                     "Sortera kontaktlista:");
		resources.put("sort_by_name",                "Enligt namn");
		resources.put("sort_by_status",              "Enligt status");
		resources.put("status_away",                 "Borta");
		resources.put("status_chat",                 "Tillgänglig för chat");
		resources.put("status_dnd",                  "Stör ej");
		resources.put("status_invisible",            "Osynlig");
		resources.put("status_na",                   "Inte tillgänglig");
		resources.put("status_occupied",             "Upptagen");
		resources.put("status_offline",              "Offline");
		resources.put("status_online",               "Online");
		resources.put("traffic",                     "Trafik");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("user_add",                    "Lägg till användare");
		resources.put("user_search",                 "Sök efter användare");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("vibration",                   "Vibration");
		// #sijapp cond.end #
		resources.put("wait",                        "Vänligen vänta ...");
		resources.put("warning",                     "Varning");
		resources.put("yes",                         "Ja");

		// Generic errors
		resources.put("error_100",                   "Okänt fel (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "Multipla inloggningar med samma UIN (#110.EXT)");
		resources.put("error_111",                   "Fel lösenord (#111.EXT)");
		resources.put("error_112",                   "Icke-existerande UIN (#112.EXT)");
		resources.put("error_113",                   "För många klienter från samma IP (#113.EXT)");
		resources.put("error_114",                   "Flöde överskridet (#114.EXT)");
		resources.put("error_115",                   "Kontaktlista kunde inte tolkas (#115.EXT)");
		resources.put("error_116",                   "Offline meddelande kunde inte tolkas (#116.EXT)");
		resources.put("error_117",                   "Tomt UIN och/eller lösenord (#117.EXT)");
		resources.put("error_118",                   "Inget svar från servern (#118.EXT)");

		// Network communication specific exceptions
		resources.put("error_120",                   "Ett I/O fel uppstod (#120.EXT)");
		resources.put("error_121",                   "Begärd TCP anslutning kan inte upprättas (#121.EXT)");
		resources.put("error_122",                   "Angiven server och/eller port ogiltig (#122.EXT)");
		resources.put("error_123",                   "Anslutning har inte etablerats (#123.EXT)");
		resources.put("error_124",                   "Input stream är ur synk (#124.EXT)");

		// Parsing specific errors
		resources.put("error_130",                   "FLAP header kunde inte tolkas (#130.EXT)");
		resources.put("error_131",                   "Okänd kanal (#131.EXT)");
		resources.put("error_132",                   "Anslut kanal paketet kunde inte tolkas (#132.EXT)");
		resources.put("error_133",                   "SNAC header kunde inte tolkas (#133.EXT)");
		resources.put("error_134",                   "Felaktig kanal paketet kunde inte tolkas (#134.EXT)");
		resources.put("error_135",                   "Frånkoppla kanal paketet kunde inte tolkas (#135.EXT)");
		resources.put("error_136",                   "Pinga kanal paketet kunde inte tolkas (#136.EXT)");
		resources.put("error_137",                   "Äldre ICQ-protokoll header kunde inte tolkas (#137.EXT)");

		// Action errors
		resources.put("error_140",                   "Begärd handling kunde inte köas för utförning just nu (#140.EXT)");

		// Specific action errors
		resources.put("error_150",                   "Mottaget meddelande kunde inte tolkas (#150.EXT)");
		resources.put("error_151",                   "Mottaget meddelande typ 1 kunde inte tolkas (#151.EXT)");
		resources.put("error_152",                   "Mottaget meddelande typ 2 kunde inte tolkas (#152.EXT)");
		resources.put("error_153",                   "Mottaget meddelande typ 4 kunde inte tolkas (#153.EXT)");
		resources.put("error_154",                   "Uppdatering av kontaktlista misslyckades (#154.EXT)");

		// Other errors
		resources.put("error_160",                   "Möjligen för lite heapminne tillgängligt (#160.EXT)");
		resources.put("error_161",                   "Kunde inte hämta meta info (#161.EXT)");

	}


}


// #sijapp cond.end #
