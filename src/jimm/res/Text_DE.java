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
 File: src/jimm/res/Text_DE.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


// #sijapp cond.if lang_DE is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_DE extends ResourceBundle
{


	public Text_DE()
	{

		// Labels
		resources.put(".",                           ",");
		resources.put("about",                       "Über");
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nICQ Client für J2ME\nVersion ###VERSION###\nSiehe http://jimm.sourceforge.net/");
		resources.put("account",                     "Account");
		resources.put("add_user",                    "Benutzer hinzufügen");
		resources.put("async",                       "Async");
		resources.put("back",                        "Zurück");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("beep",                        "Beep");
		// #sijapp cond.end #
		resources.put("byte",                        "Byte");
		resources.put("cancel",                      "Abbrechen");
		resources.put("close",                       "Schließen");
		resources.put("conn_type",                   "Verbindungsart");
		resources.put("connect",                     "Verbinden");
		resources.put("connecting",                  "Verbinde");
		resources.put("contact_list",                "Liste");
		resources.put("cp1251",                      "CP1251-Hack benutzen?");
		resources.put("cpd",                         "Tagesnutzungsgebühr(cent)");
		resources.put("cpp",                         "Paketgebühr(cent");
		resources.put("currency",                    "Währung");
		resources.put("delete_chat",                 "Lösche Chat");
		resources.put("deny",						 "Auth ablehnen");
		resources.put("denyedby",					 "Ihre Authorisierungsanfrage wurde abgelehnt durch: ");
		resources.put("user_menu",                   "Benutzer Menü");
		resources.put("disconnect",                  "Trennen");
		resources.put("disconnecting",               "Trenne");
		resources.put("display_advertisement",       "Füge Jimm-Tipp hinzu?");
		resources.put("display_date",                "Datum auf dem Splash-Screen anzeigen?");
		resources.put("email",                       "E-Mail");
		resources.put("error",                       "Fehler");
		resources.put("exit",                        "Beenden");
		resources.put("hide_offline",                "Offline-Kontakte verbergen");
		resources.put("grant",						 "Auth genehmigen");
		resources.put("grantedby",					 "Ihre Authorisierungsanfrage wurde genehmingt durch");
		resources.put("info",                        "Info");
		resources.put("kb",                          "kB");
		resources.put("keep_chat",                   "Chat-History behalten?");
		resources.put("keep_conn_alive",             "Verbindung erhalten?");
		resources.put("keylock_enable",              "Tastensperre aktivieren");
		resources.put("keylock_enabled",             "Tastensperre aktiv");
		resources.put("language",                    "Sprache");
		resources.put("lang_BR",                     "Portugiesisch (Brasilien)");
		resources.put("lang_CZ",                     "Tschechisch");
		resources.put("lang_DE",                     "Deutsch");
		resources.put("lang_EN",                     "Englisch");
		resources.put("lang_ES",                     "Spanisch");
		resources.put("lang_IT",                     "Italienisch");
		resources.put("lang_LT",                     "Litauisch");
		resources.put("lang_RU",                     "Russisch");
		resources.put("lang_SE",                     "Schwedisch");
		resources.put("loading",                     "Lade");
		resources.put("me",                          "Ich");
		resources.put("menu",                        "Menü");
		resources.put("message",                     "Nachricht");
		resources.put("message_from",                "Nachricht von");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("message_notification",        "Hinweis bei Nachricht-Eingang");
		// #sijapp cond.end #
		resources.put("name",                        "Name");
		resources.put("nick",                        "Nick");
		resources.put("no",                          "Nein");
		resources.put("not_implemented",             "Funktion noch nicht verfügbar.");
		resources.put("notice",                      "Hinweis");
		resources.put("ok",                          "OK");
		resources.put("once_a_session",              "Einmal pro Sitzung");
		resources.put("options",                     "Einstellungen");
		resources.put("options_account",             "Benutzer");
		resources.put("options_cost",                "Kosten");
		resources.put("options_effect",              "Möglicherweise werden einige Änderungen erst nach einem erneuten Verbindungsaufbau wirksam!");
		resources.put("options_interface",           "Interface");
		resources.put("options_network",             "Netzwerk");
		resources.put("options_other",               "Sonstiges");
		resources.put("password",                    "Passwort");
		resources.put("plength",                     "Länge des Abrechnungspakets in kB");
		resources.put("plsauthme",                   "Hallo! Bitte authorisieren sie meine Anfrage sie zu meiner Kontaktliste hinzuzufügen.");
		resources.put("reason",						 "Grund");
		resources.put("remove",                      "Aus der Liste entfernen");
		resources.put("reply",                       "Antworten");
		resources.put("requauth",                    "Beantrage Auth");
		resources.put("reset",                       "Zurücks.");
		resources.put("save",                        "Sichern");
		resources.put("search_user",                 "Benutzer suchen");
		resources.put("send",                        "Senden");
		resources.put("send_message",                "Neue Nachricht");
		resources.put("send_url",                    "Neue URL");
		resources.put("server",                      "Login-Server");
		resources.put("server_host",                 "Hostname");
		resources.put("server_port",                 "Port");
		resources.put("session",                     "Sitzung");
		resources.put("set_status",                  "Status festlegen");
		resources.put("since",                       "Seit dem");
		resources.put("sound",                       "ICQ Sound");
		resources.put("sound_file_name",             "Sound File Name");
		resources.put("sort_by",                     "Sortiere Kontaktliste");
		resources.put("sort_by_name",                "Nach Name");
		resources.put("sort_by_status",              "Nach Status");
		resources.put("status_away",                 "Abwesend");
		resources.put("status_chat",                 "Free for Chat");
		resources.put("status_dnd",                  "Bitte nicht stören");
		resources.put("status_invisible",            "Unsichtbar");
		resources.put("status_na",                   "Nicht verfügbar");
		resources.put("status_occupied",             "Beschäftigt");
		resources.put("status_offline",              "Offline");
		resources.put("status_online",               "Online");
		resources.put("sysnotice",					 "System Nachricht");
		resources.put("traffic",                     "Traffic");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("user_add",                    "Benutzer hinzufügen");
		resources.put("user_search",                 "Benutzer suchen");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("vibration",                   "Vibration");
		// #sijapp cond.end #
		resources.put("wait",                        "Bitte warten ...");
		resources.put("warning",                     "Warnung");
		resources.put("wantsyourauth",				 " möchte Ihre Authorisierung. Grund: ");
		resources.put("whichgroup",				 	 "Welche Gruppe?");
		resources.put("yes",                         "Ja");
		resources.put("youwereadded",				 "Sie wurden hinzugefügt von UIN: ");

		// Generic errors
		resources.put("error_100",                   "Unbekannter Fehler (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "UIN wird mehrfach verwendet (#110.EXT)");
		resources.put("error_111",                   "Ungültiges Passwort (#111.EXT)");
		resources.put("error_112",                   "Unbekannte UIN (#112.EXT)");
		resources.put("error_113",                   "Zu viele Anmeldungen unter derselben IP-Adresse (#113.EXT)");
		resources.put("error_114",                   "Zu viele Verbindungsversuche pro Zeiteinheit (#114.EXT)");
		resources.put("error_115",                   "Kontaktliste konnte nicht gelesen werden (#115.EXT)");
		resources.put("error_116",                   "Offline-Nachricht konnte nicht gelesen werden (#116.EXT)");
		resources.put("error_117",                   "UIN und/oder Passwort sind leer (#117.EXT)");
		resources.put("error_118",                   "Keine Antwort vom Server (#118.EXT)");

		// Network communication specific exceptions
		resources.put("error_120",                   "Ein I/O-Fehler ist aufgetreten (#120.EXT)");
		resources.put("error_121",                   "Angeforderte TCP-Verbindung konnte nicht hergestellt werden (#121.EXT)");
		resources.put("error_122",                   "Angegebener Serverhost und/oder -port sind ungültig (#122.EXT)");
		resources.put("error_123",                   "Verbindung wurde nicht hergestellt (#123.EXT)");
		resources.put("error_124",                   "Eingangsdatenstrom ist unsynchronisiert (#124.EXT)");

		// Parsing specific errors
		resources.put("error_130",                   "FLAP-Header konnte nicht verarbeitet werden (#130.EXT)");
		resources.put("error_131",                   "Unbekannter Kanal (#131.EXT)");
		resources.put("error_132",                   "Packet auf dem Connect-Kanal konnte nicht verarbeitet werden  (#132.EXT)");
		resources.put("error_133",                   "SNAC-Header konnte nicht verarbeitet werden (#133.EXT)");
		resources.put("error_134",                   "Packet auf dem Fehlerkanal konnte nicht verarbeitet werden (#134.EXT)");
		resources.put("error_135",                   "Packet auf dem Disconnect-Kanal konnte nicht verarbeitet werden (#135.EXT)");
		resources.put("error_136",                   "Packet auf dem Ping-Kanal konnte nicht verarbeitet werden (#136.EXT)");
		resources.put("error_137",                   "Alter ICQ-Protokol-Header konnte nicht verarbeitet werden (#137.EXT)");

		// Action errors
		resources.put("error_140",                   "Angeforderte Aktion kann im Moment zur Ausführung nicht eingereiht werden (#140.EXT)");

		// Specific action errors
		resources.put("error_150",                   "Empfangene Nachricht war unverständlich (#150.EXT)");
		resources.put("error_151",                   "Empfangene Nachricht vom Typ 1 war unverständlich (#151.EXT)");
		resources.put("error_152",                   "Empfangene Nachricht vom Typ 2 war unverständlich (#152.EXT)");
		resources.put("error_153",                   "Empfangene Nachricht vom Typ 4 war unverständlich (#153.EXT)");
		resources.put("error_154",                   "Aktualisierung der Kontaktliste ist fehlgeschlagen (#154.EXT)");

		// other errors
		resources.put("error_160",                   "Möglicherweise nicht genügend Heap-Speicher verfügbar (#160.EXT)");
		resources.put("error_161",                   "Meta-Informationen nicht verfügbar (#161.EXT)");

	}


}


// #sijapp cond.end #
