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
 File: src/jimm/res/Text_EN.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Linas Gricius 
 *******************************************************************************/


// #sijapp cond.if lang_LT is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_LT extends ResourceBundle
{


	public Text_LT()
	{

		// Labels
		resources.put(".",                           ".");
		resources.put("about",                       "Apie");
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nICQ client for J2ME\nVersion ###VERSION###\nSee http://jimm.sourceforge.net/");
		resources.put("account",                     "Paskyra");
		resources.put("add_user",                    "Prideti vartotoja");
		resources.put("async",                       "Asinchroniškas");
		resources.put("back",                        "Atgal");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("beep",                        "Pyptelejimas");
		// #sijapp cond.end #
		resources.put("byte",                        "Baitas");
		resources.put("cancel",                      "Atsisakyti");
		resources.put("close",                       "Uždaryti");
		resources.put("conn_type",                   "Prisijungimo tipas");
		resources.put("connect",                     "Prisijungti");
		resources.put("connecting",                  "Jungiamasi");
		resources.put("contact_list",                "Sarašas");
		resources.put("cp1251",                      "Naudoti CP1252 pritaikyma?");
		resources.put("cpd",                         "Kaina per diena");
		resources.put("cpp",                         "Kaina per paketa");
		resources.put("currency",                    "Valiuta");
		resources.put("delete_chat",                 "Ištrinti istorija");
		resources.put("user_menu",                   "Vartotojo meniu");
		resources.put("disconnect",                  "Atsijungti");
		resources.put("disconnecting",               "Atsijungiama");
		resources.put("display_advertisement",       "Reklamuoti Jimm'a?");
		resources.put("display_date",                "Rodyti data startiniame lange?");
		resources.put("email",                       "El.paštas");
		resources.put("error",                       "Klaida");
		resources.put("exit",                        "Išejimas");
		resources.put("hide_offline",                "Paslepti neprisijungusius");
		resources.put("info",                        "Informacija");
		resources.put("kb",                          "kB");
		resources.put("keep_chat",                   "Pasilikti pokalbiu išklotine?");
		resources.put("keep_conn_alive",             "Palaikyti pastovu ryši?");
		resources.put("keylock_enable",              "Užrakinti klaviatura");
		resources.put("keylock_enabled",             "Klaviatura užrakinta");
		resources.put("language",                    "Kalba");
		resources.put("lang_BR",                     "Portugalu (Brazilija)");
		resources.put("lang_CZ",                     "Ceku");
		resources.put("lang_DE",                     "Vokieciu");
		resources.put("lang_EN",                     "Anglu");
		resources.put("lang_ES",                     "Ispanu");
		resources.put("lang_LT",                     "Lietuviu");
		resources.put("lang_RU",                     "Rusu");
		resources.put("lang_SE",                     "Švedu");
		resources.put("loading",                     "Kraunasi");
		resources.put("me",                          "aš");
		resources.put("menu",                        "Meniu");
		resources.put("message",                     "Žinute");
		resources.put("message_from",                "Žinute nuo");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("message_notification",        "Pranešti apie žinute");
		// #sijapp cond.end #
		resources.put("name",                        "Vardas");
		resources.put("nick",                        "Slapyvardis");
		resources.put("no",                          "Ne");
		resources.put("not_implemented",             "Funkcija dar neveikia.");
		resources.put("notice",                      "Pranešimas");
		resources.put("ok",                          "OK");
		resources.put("once_a_session",              "1k.per sesija?");
		resources.put("options",                     "Savybes");
		resources.put("options_account",             "Vartotojas");
		resources.put("options_cost",                "Kaina");
		resources.put("options_effect",              "Jums gali tekti prisijungti dar karta, kad pamatytumete pakeitimus!");
		resources.put("options_interface",           "Sasaja");
		resources.put("options_network",             "Tinklas");
		resources.put("options_other",               "Kiti");
		resources.put("password",                    "Slaptažodis");
		resources.put("plength",                     "Apmokestinamo paketo dydis kilobaitais");
		resources.put("remove",                      "Pašalinti iš sarašo");
		resources.put("reply",                       "Atsakyti");
		resources.put("reset",                       "Valyti");
		resources.put("save",                        "Išsaugoti");
		resources.put("search_user",                 "Ieškoti vartotojo");
		resources.put("send",                        "Siusti");
		resources.put("send_message",                "Nauja Žinute");
		resources.put("send_url",                    "Nauja Nuoroda");
		resources.put("server",                      "Prisijungimo serveris");
		resources.put("server_host",                 "Serveris");
		resources.put("server_port",                 "Prievadas");
		resources.put("session",                     "Sesija");
		resources.put("set_status",                  "Nustatyti statusa");
		resources.put("since",                       "Nuo");
		resources.put("sound",                       "ICQ Garsas");
		resources.put("sound_file_name",             "Garsinis failas");
		resources.put("sort_by",                     "Rušiuoti saraša");
		resources.put("sort_by_name",                "Pagal varda");
		resources.put("sort_by_status",              "Pagal statusa");
		resources.put("status_away",                 "Kažkur šalia");
		resources.put("status_chat",                 "Noriu kalbetis!");
		resources.put("status_dnd",                  "Netrukdyti!");
		resources.put("status_invisible",            "Pasislepes");
		resources.put("status_na",                   "Nepasiekiamas");
		resources.put("status_occupied",             "Užimtas");
		resources.put("status_offline",              "Atsijunges");
		resources.put("status_online",               "Prisijunges");
		resources.put("traffic",                     "Srautas");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "Nuoroda");
		resources.put("user_add",                    "Prideti vartotoja");
		resources.put("user_search",                 "Ieškoti vartotojo");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("vibration",                   "Vibracija");
		// #sijapp cond.end #
		resources.put("wait",                        "Prašome palaukti ...");
		resources.put("warning",                     "Ispejimas");
		resources.put("yes",                         "Taip");

		// Generic errors
		resources.put("error_100",                   "Nežinoma klaida (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "Dar vienas prisijungimas tuo paciu UIN (#110.EXT)");
		resources.put("error_111",                   "Negeras slaptažodis (#111.EXT)");
		resources.put("error_112",                   "Neegzstuojantis UIN (#112.EXT)");
		resources.put("error_113",                   "Per daug prisijungimu iš to paties IP (#113.EXT)");
		resources.put("error_114",                   "Rate exceeded (#114.EXT)");
		resources.put("error_115",                   "Negaliu perskaityti kontaktu sarašo (#115.EXT)");
		resources.put("error_116",                   "Offline message could not be parsed (#116.EXT)");
		resources.put("error_117",                   "Neirašytas UIN ir/arba slaptažodis (#117.EXT)");
		resources.put("error_118",                   "Nera atsakymo iš serverio (#118.EXT)");

		// Network communication specific exceptions
		resources.put("error_120",                   "I/O klaida! (#120.EXT)");
		resources.put("error_121",                   "Requested TCP connection cannot be made (#121.EXT)");
		resources.put("error_122",                   "Neteisingas nurodyto serverio adresas ir/arba prievadas (#122.EXT)");
		resources.put("error_123",                   "Nepavyko prisijungti (#123.EXT)");
		resources.put("error_124",                   "Input stream is out of sync (#124.EXT)");

		// Parsing specific errors
		resources.put("error_130",                   "FLAP header could not be parsed (#130.EXT)");
		resources.put("error_131",                   "Unknown channel (#131.EXT)");
		resources.put("error_132",                   "Connect channel packet  could not be parsed (#132.EXT)");
		resources.put("error_133",                   "SNAC header could not be parsed (#133.EXT)");
		resources.put("error_134",                   "Error channel packet could not be parsed (#134.EXT)");
		resources.put("error_135",                   "Disconnect channel packet could not be parsed (#135.EXT)");
		resources.put("error_136",                   "Ping channel packet could not be parsed (#136.EXT)");
		resources.put("error_137",                   "Old ICQ protocol header could not be parsed (#137.EXT)");

		// Action errors
		resources.put("error_140",                   "Requested action cannot be queued for execution at this time (#140.EXT)");

		// Specific action errors
		resources.put("error_150",                   "Nesuprantu gautos žinutes (#150.EXT)");
		resources.put("error_151",                   "Nesuprantu gautos 1 tipo žinutes (#151.EXT)");
		resources.put("error_152",                   "Nesuprantu gautos 2 tipo žinutes (#152.EXT)");
		resources.put("error_153",                   "Nesuprantu gautos 4 tipo žinutes (#153.EXT)");
		resources.put("error_154",                   "Kontaktu sarašo atnaujinimas nepavyko (#154.EXT)");

		// Other errors
		resources.put("error_160",                   "Possibly not enough heap memory available (#160.EXT)");
		resources.put("error_161",                   "Could not fetch meta info (#161.EXT)");

	}


}


// #sijapp cond.end #
