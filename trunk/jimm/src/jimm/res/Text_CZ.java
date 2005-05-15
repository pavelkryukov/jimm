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
 File: src/jimm/res/Text_CZ.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Petr Krčmář
 *******************************************************************************/


// #sijapp cond.if lang_CZ is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_CZ extends ResourceBundle
{


	public Text_CZ()
	{

		// Labels
		// MOTOROLA formatted buttons
        // #sijapp cond.if target is "MOTOROLA"#
    	resources.put("backlight_timeout",           "Backlight timeout (sec)");
	    resources.put("backlight_manual",            "Manual backlight (* key)");	    
        resources.put("select",                      "Vybrat      ");
        resources.put("reset",                       "       Reset");
        resources.put("cancel",                      "      Zrušit");
        resources.put("back",                        "       Zpět");
        resources.put("exit_button",                 "       Konec");
        resources.put("menu_button",                 "      Menu");
        resources.put("save",                        "Uložit       ");
        resources.put("ok",                          "OK          ");
        resources.put("reply",                       "Odpověď       ");
        resources.put("close",                       "       Zavřít");
        resources.put("send",                        "Odeslat     ");
        // #sijapp cond.else#
        resources.put("reply",                       "Odpověď");
        resources.put("close",                       "Zavřít");
        resources.put("select",                      "Vybrat");
        resources.put("reset",                       "Reset");
        resources.put("cancel",                      "Zrušit");
        resources.put("back",                        "Zpět");
        resources.put("save",                        "Uložit");
        resources.put("ok",                          "OK");
        resources.put("send",                        "Odeslat");
        // #sijapp cond.end#
		resources.put(".",                           ".");
		resources.put("about",                       "O programu");
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nICQ klient pro J2ME\n\nVerze: ###VERSION###\nTarget: ###TARGET###\nModules: ###MODULES###\n\nKoukněte na http://www.jimm.org/");
		resources.put("account",                     "Účet");
		resources.put("add_group",                   "Přidat skupinu");
		resources.put("add_user",                    "Přidat uživatele");
		resources.put("add_to_list",                 "Přidat do seznamu");
		resources.put("age",                         "Věk");
		resources.put("async",                       "Async");
		resources.put("attention",                   "Pozor!");
		resources.put("auth",			             "Autorizace");
		resources.put("auto_connect",                "Připojit při startu");
		resources.put("beep",                        "Píp");
		resources.put("black_on_white",              "Černé na bílem");
		resources.put("byte",                        "Byte");
		resources.put("chat_small_font",             "Malé písmo v chatu");
		resources.put("cancel",                      "Zpět");
		resources.put("city",                        "Město");
		resources.put("clear",                       "Smazat");
		resources.put("color_scheme",                "Barevné schéma");
		resources.put("conn_type",                   "Typ spojení");
		resources.put("con_wait",                    "Prodleva mezi spojeními(sec)");
		resources.put("connect",                     "Připojit");
		resources.put("connecting",                  "Připojuji");
		resources.put("cost",                        "Cena");
		resources.put("contact_list",                "Seznam");
		resources.put("cp1251",                      "Použít CP1251 hack?");
		resources.put("cpd",                         "Cena za den");
		resources.put("cpp",                         "Cena za paket");
		resources.put("currency",                    "Měna");
		resources.put("del_group",                   "Smazat skupinu");
		resources.put("delete_chat",                 "Smazat diskusi");
		resources.put("deny",						 "Odmítnout autorizaci");
		resources.put("denyedby",					 "Vaše autorizace byla odmítnuta: ");
		resources.put("description",                 "Popis");
		resources.put("name_desc",                   "Jméno souboru a popis");
		resources.put("user_menu",                   "Uživatelské Menu");
		resources.put("disconnect",                  "Odpojit");
		resources.put("disconnecting",               "Odpojuji");
		resources.put("display_date",                "Ukázat datum?");
		resources.put("email",                       "Email");
		resources.put("error",                       "Chyba");
		resources.put("exec",                        "Spustit");
		resources.put("exit",                        "Konec");
		resources.put("female",                      "Ž");
		resources.put("filetransfer",                "Přenos souboru");
		resources.put("filepath",                    "Cesta k souboru");
		resources.put("find",                        "Hledat");
		resources.put("firstname",                   "Jméno");
		resources.put("free_heap",                   "Uvolnit paměť");
		resources.put("ft_name",                     "Odeslat soubor");		
		resources.put("ft_cam",                      "Odeslat fotku");		
		resources.put("gender",                      "Pohlaví");
		resources.put("grant",						 "Přijmout autorizaci");
		resources.put("grantedby",					 "Vaše autorizace byla přijata:");
		resources.put("group_name",                  "Jméno skupiny");
  	    resources.put("group_is_not_empty",          "Skupina není prázdná!\nPřesuňte všechny uživatele do jiné!");
  	    resources.put("have_unread_mess",            "You have unread messages. Exit anyway?");
		resources.put("hide_offline",                "Skrýt odpojené uživatele");
		resources.put("info",                        "Info");
		resources.put("init_ft",                     "Inicializace");
		resources.put("kb",                          "kB");
		resources.put("kbs",                         "kb/s");
		resources.put("keep_chat",                   "Ponechat historii?");
		resources.put("keep_conn_alive",             "Podržet spojení?");
		resources.put("keylock",                     "Zámek kláves");
        resources.put("keylock_message",             "Podržte \"#\" k odemčení kláves");
		resources.put("keylock_enable",              "Zamknout klávesy");
		resources.put("keylock_enabled",             "Klávesy uzamčeny");
		resources.put("keyword",                     "Heslo");
		resources.put("language",                    "Jazyk");
		resources.put("lang_BG",                     "Bulharština");
		resources.put("lang_BR",                     "Portugalština (Brazílie)");
		resources.put("lang_CZ",                     "Čeština");
		resources.put("lang_DE",                     "Němčina");
		resources.put("lang_EN",                     "Angličtina");
		resources.put("lang_ES",                     "Španělština");
		resources.put("lang_HE",                     "Hebrejština");
		resources.put("lang_IT",                     "Italština");
		resources.put("lang_LT",                     "Litevština");
		resources.put("lang_PL",                     "Polština");
		resources.put("lang_RU",                     "Ruština");
		resources.put("lang_SE",                     "Švédština");
		resources.put("lang_SR",                     "Srbština");
		resources.put("lang_UA", 		             "עבריתUkrajinština");
		resources.put("lang_BG",                     "Bulharština");
		resources.put("lastname",                    "Příjmení");
		resources.put("loading",                     "Nahrávám");
		resources.put("male",                        "M");
		resources.put("me",                          "já");
		resources.put("menu",                        "Menu");
		resources.put("message_notification",        "Zvuk upozornění");
		resources.put("msg_sound_file_name",         "Zvuk nové zprávy");
		resources.put("message",                     "Zpráva");
		resources.put("message_from",                "Zpráva od");
		resources.put("name",                        "Jméno");
		resources.put("next",						 "Další");
		resources.put("nick",                        "Přezdívka");
		resources.put("no",                          "Ne");
		resources.put("not",                         "ne");
		resources.put("no_results",                  "Bez výsledku");
		resources.put("no_not_empty_gr",             "Mazání neprázdných skupin zatím není podporováno.");
		resources.put("not_implemented",             "Funkce zatím není implementována.");
		resources.put("noreason",             		 "Bez důvodu.");
		resources.put("notice",                      "Oznámení");
		resources.put("nr",				     		 "Čí");
		resources.put("once_a_session",              "Jednou za relaci");
		resources.put("onl_notification",            "Oznámení příchozího kontaktu");
		resources.put("onl_sound_file_name",         "Zvuk pro online");
		resources.put("only_online",                 "Ukázat jen online kontakty");
		resources.put("options",                     "Možnosti");
		resources.put("options_account",             "Účet");
		resources.put("options_cost",                "Cena");
		resources.put("options_effect",              "Musíte se odpojit a připojit, aby se projevily změny!");
		resources.put("options_interface",           "Rozhraní");
		resources.put("options_network",             "Síť");
		resources.put("options_other",               "Další");
		resources.put("options_signaling",           "Signalizace");
		resources.put("password",                    "Heslo");
		resources.put("plength",                     "Velikost paketu v kB");
		resources.put("plsauthme",                   "Nazdar! Prosím autorizuj mě pro přidání do seznamu.");
		resources.put("prev",						 "Předchozí");
		resources.put("reason",						 "Důvod");
		resources.put("remove",                      "Odstranit ze seznamu");
		resources.put("rename",                      "Přejmenovat");
		resources.put("remove_group",                "Odstranit skupinu");
  	    resources.put("remove_user",                 "Odstranit uživatele");
  	    resources.put("rename",                      "Přejmenovat");
		resources.put("requauth",                    "Autorizační požadavek");
		resources.put("requ",                        "Požadován");
		resources.put("requno",                      "Nepožadován");
		resources.put("res",                         "Rozlišení");
		resources.put("results",                     "Výsledky");
		resources.put("search_user",                 "Vyhledat uživatele");
		resources.put("send_img",                    "Poslat obrázek");
		resources.put("send_message",                "Nová zpráva");				
		resources.put("send_url",                    "Nová URL");
		resources.put("server",                      "Přihlašovací server");
		resources.put("server_host",                 "Jméno");
		resources.put("server_port",                 "Port");
		resources.put("session",                     "Sezení");
		resources.put("set_status",                  "Nastavit stav");
		resources.put("shadow_con",                  "Stínové připojení");
  	    resources.put("show_user_groups",            "Ukázat skupiny");
		resources.put("since",                       "Od");
		resources.put("size",                        "Velikost");
		resources.put("sound",                       "Ze souboru");
		resources.put("sound_file_name",             "Název souboru se zvukem");
		resources.put("sort_by",                     "Třídit seznam");
		resources.put("sort_by_name",                "Podle jmen");
		resources.put("sort_by_status",              "Podle stavu");
		resources.put("speed",                       "Rychlost");
		resources.put("status",                      "Stav");
		resources.put("status_away",                 "Pryč");
		resources.put("status_chat",                 "Chci si povídat");
		resources.put("status_dnd",                  "Neobtěžuj");
		resources.put("status_invisible",            "Neviditelný");
		resources.put("status_na",                   "Nejsem k zastižení");
		resources.put("status_occupied",             "Mám práci");
		resources.put("status_offline",              "Offline");
		resources.put("status_online",               "Online");
		resources.put("successful",                  "úspěšný");
		resources.put("sysnotice",					 "Systémové oznámení");
		resources.put("traffic",                     "Provoz");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("use_history",                 "Uložit historii");
		resources.put("user_add",                    "Přidat uživatele");
		resources.put("user_search",                 "Vyhledat uživatele");
		resources.put("vibration",                   "Vibrace");
		resources.put("viewfinder",                  "Hledáček");
		resources.put("volume",                      "Hlasitost");		
		resources.put("wait",                        "Prosím počkejte ...");
		resources.put("warning",                     "Varování");
		resources.put("wantsyourauth",				 " chce Vaši autorizaci. Důvod: ");
		resources.put("was",                         "byl");
		resources.put("whichgroup",				 	 "Která skupina?");
		resources.put("white_on_black",              "Bílé na černém");
		resources.put("white_on_blue",               "Bílé na modrém");
		resources.put("yes",                         "Ano");
		resources.put("youwereadded",				 "Byl jste přidán do seznamu UIN: ");
		resources.put("chat_small_font",			 "Malé písmo");
		resources.put("select",                      "Vybrat");
		resources.put("attention",                   "Pozor!");
		resources.put("have_unread_mess",            "Máte nepřečtené zprávy. Přesto skončit?");
		
		//#sijapp cond.if modules_HISTORY is "true" #
		resources.put("text_to_find",                "Text");
		resources.put("find_backwards",              "Dozadu");
		resources.put("find_case_sensitiv",          "Velká/malá písmena");
		resources.put("history_info",                "Informace o historii");
		resources.put("hist_cur",                    "Počet zpráv uživatele");
		resources.put("hist_rc",                     "Celkový počet zpráv");
		resources.put("hist_size",                   "Použitá kapacita (kB)");
		resources.put("hist_avail",                  "Celková kapacita (kB)");
		resources.put("history",                     "Uložená historie");
		resources.put("not_found",                   "Nenalezeno");
		//#sijapp cond.end#

		// Generic errors
		resources.put("error_100",                   "Neznámá chyba (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "Už jste přihlášen jinde na stejné UIN (#110.EXT)");
		resources.put("error_111",                   "Špatné heslo (#111.EXT)");
		resources.put("error_112",                   "Neexistující UIN (#112.EXT)");
		resources.put("error_113",                   "Příliš mnoho klientů z jedné IP (#113.EXT)");
		resources.put("error_114",                   "Cena překročena (#114.EXT)");
		resources.put("error_115",                   "Nemůžu převzít seznam kontaktů (#115.EXT)");
		resources.put("error_116",                   "Nemůžu převzít offline zprávu (#116.EXT)");
		resources.put("error_117",                   "Prázdné UIN a/nebo heslo (#117.EXT)");
		resources.put("error_118",                   "Nemám odpověď od serveru (#118.EXT)");

		// Network communication specific exceptions
		// Connection to server
		resources.put("error_120",                   "I/O chyba (#120.EXT)");
		resources.put("error_121",                   "Požadované TCP spojení nelze uskutečnit (#121.EXT)");
		resources.put("error_122",                   "Zadaný server a/nebo port jsou špatně (#122.EXT)");
		resources.put("error_123",                   "Spojení nebylo navázáno (#123.EXT)");
		resources.put("error_124",                   "Chyba vstupních dat (#124.EXT)");
		
		// Peer connection
		resources.put("error_125",                   "I/O chyba (#125.EXT)");
		resources.put("error_126",                   "Požadované TCP spojení nelze uskutečnit (#126.EXT)");
		resources.put("error_127",                   "Zadaný server a/nebo port jsou špatně (#127.EXT)");
		resources.put("error_128",                   "Spojení nebylo navázáno (#128.EXT)");
		resources.put("error_129",                   "Chyba vstupních dat (#129.EXT)");

		// Parsing specific errors
		resources.put("error_130",                   "FLAP hlavička nemůže být zpracována (#130.EXT)");
		resources.put("error_131",                   "Neznámý kanál (#131.EXT)");
		resources.put("error_132",                   "Připojovací paket namůže být zpracován (#132.EXT)");
		resources.put("error_133",                   "SNAC hlavička nemůže být zpracována (#133.EXT)");
		resources.put("error_134",                   "Chybový paket kanálu nemůže být zpracován (#134.EXT)");
		resources.put("error_135",                   "Odpojovací paket nemůže být zpracován (#135.EXT)");
		resources.put("error_136",                   "Ping paket nemůže být zpracován (#136.EXT)");
		resources.put("error_137",                   "Starý ICQ protokol nemůže být zpracován(#137.EXT)");

		// Action errors
		resources.put("error_140",                   "Požadovaná akce teď nemůže být předána ke zpracování (#140.EXT)");

		// Specific action errors
		resources.put("error_150",                   "Nerozumím přijaté zprávě (#150.EXT)");
		resources.put("error_151",                   "Nerozumím přijaté zpráva typu 1  (#151.EXT)");
		resources.put("error_152",                   "Nerozumím přijaté zprávě typu 2  (#152.EXT)");
		resources.put("error_153",                   "Nerozumím přijaté zprávě typu 4 (#153.EXT)");
		resources.put("error_154",                   "Chyba při načítání seznamu kontaktů (#154.EXT)");
		resources.put("error_155",                   "Tento objekt již ve Vašem seznamu je (#155.EXT)");
		resources.put("error_156",                   "Chyba při přidávání. Zkuste to znovu (#156.EXT)");
		resources.put("error_157",                   "Není povoleno více prvků tohoto typu (#157.EXT)");
		resources.put("error_158",                   "Zkoušíte přidat ICQ kontakt do AIM seznamu (#158.EXT)");
		resources.put("error_159",                   "Server neodpověděl na vyhledávací požadavek. Zkuste to znovu (#159.EXT)");
		resources.put("error_160",                   "Chyba při vyhledávání (#160.EXT)");
		resources.put("error_161",                   "Žádné skupiny nenalezeny. Prosím přidejte skupinu (#161.EXT)");

		// Other errors
		resources.put("error_170",                   "Asi není dost paměti (#170.EXT)");
		resources.put("error_171",                   "Nemůžu získat meta info (#171.EXT)");
		
		// Camera errors
		resources.put("error_180",                   "Chyba při vytváření VideoControl (#180.EXT)");
		resources.put("error_181",                   "Selhala inicializace hledáčku (#181.EXT)");
		resources.put("error_182",                   "Selhal start hledáčku (#182.EXT)");
		resources.put("error_183",                   "Chyba snímku (#183.EXT)");
		resources.put("error_185",                   "Focení není podporováno (#185.EXT)");
		
		// File transfer errors
		resources.put("error_190",                   "Přenos souborů s klienty nižšími než ICQv8 není podporován (#190.EXT)");
		resources.put("error_191",                   "Chyba při čtení souboru. Asi není podporováno (#191.EXT)");
		resources.put("error_192",                   "Chyba při načítání souboru. Soubor nenalezen nebo není podporován (#192.EXT)");
		resources.put("error_193",                   "Chyba při přístupu k souboru. Bezpečnostní chyba (#193.EXT)");
	}

}
// #sijapp cond.end #
