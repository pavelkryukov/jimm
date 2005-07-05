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
        // MOTOROLA formatted buttons
        // #sijapp cond.if target is "MOTOROLA"#
    	resources.put("backlight_timeout",           "Hintergrundlicht Timeout(sek)");
	    resources.put("backlight_manual",            "Hintergrundlicht manuell(*)");
        resources.put("select",                      "Auswahl     ");
        resources.put("reset",                       "Zurücksetzen");
        resources.put("cancel",                      "   Abbrechen");
        resources.put("back",                        "      Zurück");
        resources.put("exit_button",                 "      Beenden");
        resources.put("menu_button",                 "      Menü");
        resources.put("save",                        "Sicher    ");
        resources.put("ok",                          "OK          ");
        resources.put("reply",                       "Antworten   ");
        resources.put("close",                       "    Sliessen");
        resources.put("send",                        "Senden   ");
        // #sijapp cond.else# 
        resources.put("reply",                       "Antworten");
        resources.put("close",                       "Schließen");
        resources.put("select",                      "Auswahl");
        resources.put("reset",                       "Zurücksetzen");
        resources.put("cancel",                      "Abbrechen");
        resources.put("back",                        "Zurück");
        resources.put("save",                        "Sichern");
        resources.put("ok",                          "OK");
        resources.put("send",                        "Senden");
    	// #sijapp cond.end#
        
		resources.put(".",                           ",");
		resources.put("about",                       "Über");
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nICQ Client für J2ME\n\nVersion: ###VERSION###\nZiel: ###TARGET###\nModule: ###MODULES###\n\nSiehe http://www.jimm.org/");
		resources.put("account",                     "Account");
		resources.put("add_group",                   "Gruppe hinzufügen");
		resources.put("add_user",                    "Benutzer hinzufügen");
		resources.put("add_to_list",                 "Zur Liste hinzuf.");
		resources.put("age",                         "Alter");
		resources.put("async",                       "Async");
		resources.put("attention",                   "Achtung!");
		resources.put("auth",				     	 "Auth");
		resources.put("auto_connect",				 "Nach dem Start automatisch verbinden");
		resources.put("beep",                        "Beep");
		resources.put("black_on_white",              "schwarz auf weiss");
		resources.put("byte",                        "Byte");
		resources.put("chat_small_font",			 "Kleiner Font im Chat");
		resources.put("city",                        "Stadt");
		resources.put("clear",                       "Löschen");                        
		resources.put("color_scheme",                "Farbschema");
		resources.put("con_wait",                    "Verzögerung zwischen Verbindungen(sek)");
		resources.put("conn_type",                   "Verbindungsart");
		resources.put("connect",                     "Verbinden");
		resources.put("connecting",                  "Verbinde");
		resources.put("contact_list",                "Liste");
		resources.put("cost",                        "Kosten");
		resources.put("cp1251",                      "CP1251-Hack benutzen?");
		resources.put("cpd",                         "Tagesnutzungsgebühr (cent)");
		resources.put("cpp",                         "Paketgebühr (cent)");
		resources.put("currency",                    "Währung");
		resources.put("del_group",                   "Gruppe löschen");
		resources.put("delete_chat",                 "Verwerfe Chat");
		resources.put("deny",						 "Auth ablehnen");
		resources.put("denyedby",					 "Ihre Authorisierungsanfrage wurde abgelehnt durch: ");
		resources.put("description",                 "Beschreibung");
		resources.put("name_desc",                   "Dateiname und Beschreibung");
		resources.put("user_menu",                   "Benutzer Menü");	
		resources.put("disconnect",                  "Trennen");
		resources.put("disconnecting",               "Trenne");
		resources.put("display_date",                "Datum auf dem Splash-Screen anzeigen?");
		resources.put("email",                       "E-Mail");
		resources.put("error",                       "Fehler");
		resources.put("exec",                        "Ausführen");
		resources.put("exit",                        "Beenden");
		resources.put("filetransfer",                "Dateiübertragung");	
		resources.put("filepath",                    "Dateipfad");
		resources.put("find",                        "Finden");
		resources.put("firstname",                   "Vorname");
		resources.put("free_heap",                   "Freier Heap");
		resources.put("ft_name",                     "Datei senden");		
		resources.put("ft_cam",                      "Kamerabild senden");		
		resources.put("gender",                      "Geschlecht");
		resources.put("grant",						 "Auth genehmigen");
		resources.put("grantedby",					 "Ihre Authorisierungsanfrage wurde genehmingt durch");
		resources.put("group_name",                  "Gruppenname");
		resources.put("group_is_not_empty",          "Gewählte Gruppe ist leer!\nAlle Kontakte in andere Gruppe verschieben?");
		resources.put("have_unread_mess",            "Sie haben ungelesene Nachrichten. Trotzdem verlassen?");
		resources.put("hide_offline",                "Offline-Kontakte verbergen");
		resources.put("info",                        "Info");
		resources.put("init_ft",                     "Initialisierung");
		resources.put("kb",                          "kB");
		resources.put("kbs",                         "kb/s");
		resources.put("keep_chat",                   "Chat-History behalten?");
		resources.put("keep_conn_alive",             "Verbindung erhalten?");
		resources.put("keylock",                     "Tastensperre");
		resources.put("keylock_message",             "\"#\" zum lösen der Tastenspeere halten");
		resources.put("keylock_enable",              "Tastensperre aktivieren");
		resources.put("keylock_enabled",             "Tastensperre aktiv");
		resources.put("keyword",                     "Schlüsselwort");
		resources.put("language",                    "Sprache");
		resources.put("lang_BG", 					 "Bulgarisch");
		resources.put("lang_BR",                     "Portugiesisch (Brasilien)");
		resources.put("lang_CZ",                     "Tschechisch");
		resources.put("lang_DE",                     "Deutsch");
		resources.put("lang_EN",                     "Englisch");
		resources.put("lang_ES",                     "Spanisch");
		resources.put("lang_HE", 					 "Hebräisch");
		resources.put("lang_IT",                     "Italienisch");
		resources.put("lang_LT",                     "Litauisch");
		resources.put("lang_PL",                     "Polnisch");
		resources.put("lang_RU",                     "Russisch");
		resources.put("lang_SE",                     "Schwedisch");
		resources.put("lang_SR",                     "Serbisch");
		resources.put("lang_UA", 					 "Ukrainisch");
		resources.put("lastname",                    "Nachname");
		resources.put("loading",                     "Lade");
		resources.put("male",                        "M");
		resources.put("me",                          "Ich");
		resources.put("menu",                        "Menü");
		resources.put("msg_sound_file_name",         "Nachricht Sound Datei");
		resources.put("message",                     "Nachricht");
		resources.put("message_from",                "Nachricht von");
		resources.put("message_notification",        "Hinweis bei Nachricht-Eingang");
		resources.put("minimize",                    "Minimieren");
		resources.put("name",                        "Name");
		resources.put("next",						 "Nächstes");
		resources.put("nick",                        "Nick");
		resources.put("no",                          "Nein");
		resources.put("not",                         "nicht");
		resources.put("no_results",                  "Keine Ergebnisse");
		resources.put("no_not_empty_gr",             "Löschen von nichtleeren Gruppen wird noch nicht unterstützt");
		resources.put("not_implemented",             "Funktion noch nicht verfügbar.");
		resources.put("noreason",             		 "Es wurde kein Grund angegeben.");
		resources.put("notice",                      "Hinweis");
		resources.put("nr",				     		 "Nr");
		resources.put("once_a_session",              "Einmal pro Sitzung");
		resources.put("onl_notification",            "Hinweis bei Onlinewechsel");
		resources.put("onl_sound_file_name",         "Online Sound Datei");
		resources.put("only_online",                 "Nur Online Kontakte zeigen");
		resources.put("options",                     "Einstellungen");
		resources.put("options_account",             "Benutzer");
		resources.put("options_cost",                "Kosten");
		resources.put("options_effect",              "Möglicherweise werden einige Änderungen erst nach einem erneuten Verbindungsaufbau wirksam!");
		resources.put("options_interface",           "Interface");
		resources.put("options_network",             "Netzwerk");
		resources.put("options_other",               "Sonstiges");
		resources.put("options_signaling",           "Signalisierung");
		resources.put("password",                    "Passwort");
		resources.put("plength",                     "Länge des Abrechnungspakets in kB");
		resources.put("plsauthme",                   "Hallo! Bitte authorisieren sie meine Anfrage sie zu meiner Kontaktliste hinzuzufügen.");
		resources.put("prev",						 "Voheriges");
		resources.put("reason",						 "Grund");
		resources.put("remove",                      "Aus der Liste entfernen");
        resources.put("remove_me",                   "Lösche mich"); 
        resources.put("remove_me_from",              "Lösche Deinen Eintrag in der Liste von ");        
		resources.put("remove_group",                "Gruppe löschen");
		resources.put("remove_user",                 "Kontakt löschen");
		resources.put("rename",                      "Umbenennen");
		resources.put("requauth",                    "Beantrage Auth");
        resources.put("reqstatmsg",                  "Statusnachricht anfordern");
		resources.put("requ",                        "Benötigt");
		resources.put("requno",                      "Nicht Ben.");
		resources.put("res",                         "Auflösung");
		resources.put("results",                     "Ergebnisse");
		resources.put("search_user",                 "Benutzer suchen");
		resources.put("send_img",                    "Bild senden");
		resources.put("send_message",                "Neue Nachricht");
		resources.put("send_url",                    "Neue URL");
		resources.put("server",                      "Login-Server");
		resources.put("server_host",                 "Hostname");
		resources.put("server_port",                 "Port");
		resources.put("session",                     "Sitzung");
		resources.put("set_status",                  "Status festlegen");
		resources.put("shadow_con",                  "Schattenverbindung");
		resources.put("show_user_groups",            "Gruppen anzeigen");	
		resources.put("since",                       "Seit dem");
		resources.put("size",                        "Größe");
		resources.put("sound",                       "Sounddatei");
		resources.put("sound_file_name",             "Sound File Name");
		resources.put("sort_by",                     "Sortiere Kontaktliste");
		resources.put("sort_by_name",                "nach Name");
		resources.put("sort_by_status",              "nach Status");
		resources.put("speed",                       "Geschwindigkeit");
		resources.put("status",                      "Status");
        resources.put("status_message",              "Status Nachricht");
		resources.put("status_away",                 "Abwesend");
		resources.put("status_chat",                 "Free for Chat");
		resources.put("status_dnd",                  "Bitte nicht stören");
		resources.put("status_invisible",            "Unsichtbar");
		resources.put("status_na",                   "Nicht verfügbar");
		resources.put("status_occupied",             "Beschäftigt");
		resources.put("status_offline",              "Offline");
		resources.put("status_online",               "Online");
		resources.put("successful",                  "erfolgreich");
		resources.put("sysnotice",					 "System Nachricht");
		resources.put("traffic",                     "Traffic");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("use_history",                 "Speichere History");
		resources.put("user_add",                    "Benutzer hinzufügen");
		resources.put("user_search",                 "Benutzer suchen");
		resources.put("vibration",                   "Vibration");
		resources.put("viewfinder",                  "Sucher");
		resources.put("volume",                      "Lautstärke");	
		resources.put("wait",                        "Bitte warten ...");
		resources.put("was",                         "war");
		resources.put("warning",                     "Warnung");
		resources.put("wantsyourauth",				 " möchte Ihre Authorisierung. Grund: ");
		resources.put("whichgroup",				 	 "Welche Gruppe?");
		resources.put("white_on_black",              "weiss auf schwarz");
		resources.put("white_on_blue",               "weiss auf blau");
		resources.put("yes",                         "Ja");
		resources.put("youwereadded",				 "Sie wurden hinzugefügt von UIN: ");

		//#sijapp cond.if modules_HISTORY is "true" #
		resources.put("text_to_find",                "Text");
		resources.put("find_backwards",              "Rückwärts");
		resources.put("find_case_sensitiv",          "Gross/Klein beachten");
		resources.put("history_info",                "Speicher Info");
		resources.put("hist_cur",                    "Nachrichten des aktuellen Kontakts");
		resources.put("hist_rc",                     "Nachrichten aller Kontakte");
		resources.put("hist_size",                   "Benutzer Platz (kB)");
		resources.put("hist_avail",                  "Gesamter Platz (kB)"); 
		resources.put("history",                     "Gespeicherte History");
		resources.put("not_found",                   "nicht gefunden");
		//#sijapp cond.end#
		
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
		// Connection to server
		resources.put("error_120",                   "Ein I/O-Fehler ist aufgetreten (#120.EXT)");
		resources.put("error_121",                   "Angeforderte TCP-Verbindung konnte nicht hergestellt werden (#121.EXT)");
		resources.put("error_122",                   "Angegebener Serverhost und/oder -port sind ungültig (#122.EXT)");
		resources.put("error_123",                   "Verbindung wurde nicht hergestellt (#123.EXT)");
		resources.put("error_124",                   "Eingangsdatenstrom ist unsynchronisiert (#124.EXT)");
		
		// Peer connection
		resources.put("error_125",                   "Ein I/O-Fehler ist aufgetreten (#125.EXT)");
		resources.put("error_126",                   "Angeforderte TCP-Verbindung konnte nicht hergestellt werden (#126.EXT)");
		resources.put("error_127",                   "Angegebener Serverhost und/oder -port sind ungültig (#127.EXT)");
		resources.put("error_128",                   "Verbindung wurde nicht hergestellt (#128.EXT)");
		resources.put("error_129",                   "Eingangsdatenstrom ist unsynchronisiert (#129.EXT)");
		
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
		resources.put("error_154",                   "Fehler beim aktualisieren der Kontaktliste (#154.EXT)");
		resources.put("error_155",                   "Objekt ist bereits auf dem Server vorhanden  (#155.EXT)");
		resources.put("error_156",                   "Fehler beim Hinzufügen. Nochmal versuchen (#156.EXT)");
		resources.put("error_157",                   "Keine weiteren Objekte dieses Typs zulässig (#157.EXT)");
		resources.put("error_158",                   "Versuch einen ICQ-Kontact einer AIM-Liste hinzuzufügen (#158.EXT)");
		resources.put("error_159",                   "Server antwortete nicht auf Suchanfrage. Nochmal versuchen (#159.EXT)");
		resources.put("error_160",                   "Fehler beim Suchen (#160.EXT)");
		resources.put("error_161",                   "Keine Gruppen vorhanden. Erst eine Gruppe erstellen (#161.EXT)");
		
		// other errors
		resources.put("error_170",                   "Möglicherweise nicht genügend Heap-Speicher verfügbar (#170.EXT)");
		resources.put("error_171",                   "Meta-Informationen nicht verfügbar (#171.EXT)");
        resources.put("error_172",                   "Einstellungen konnten nicht gespeichert werden (#172.EXT)");
		
		// Camera errors
		resources.put("error_180",                   "Fehler beim Erstellen der VideoControl (#180.EXT)");
		resources.put("error_181",                   "Fehler beim Initialisieren des Suchers (#181.EXT)");
		resources.put("error_182",                   "Fehler beim Starten des Suchers (#182.EXT)");
		resources.put("error_183",                   "Fehler beim Erfassen des Bildes (#183.EXT)");
		resources.put("error_185",                   "Bilder erstellen wird nicht unterstzützt (#185.EXT)");
		
		// File transfer errors
		resources.put("error_190",                   "Dateiübertragung zu < ICQv8 Clients nicht unterstützt (#190.EXT)");
		resources.put("error_191",                   "Fehler beim Lesen der Datei. Möglichwerweise nicht unterstützt (#191.EXT)");
		resources.put("error_192",                   "Fehler beim lesen der Datei. Datei nicht gefunden oder nicht unterstützt(#192.EXT)");
		resources.put("error_193",                   "Fehler Öffnen des Dateisystems. Sicherheitsfehler(#193.EXT)");

	}
}

//#sijapp cond.end #
