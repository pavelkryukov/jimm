/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-04  Manuel Linsmayer

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
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Miloslav Vorlíèek
 *******************************************************************************/


// #sijapp cond.if lang_CZ is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_CZ extends ResourceBundle
{


  public Text_CZ()
  {

    // Labels
    resources.put(".",                           ".");
    resources.put("about",                       "O programu");
    resources.put("about_info",                  "Jimm - Mobile Messaging\n\nICQ client pro J2ME\nVerze ###VERSION###\nBližší info\nhttp://jimm.sourceforge.netd/");
    resources.put("account",                     "Účet");
    resources.put("add_user",                    "Přidání uživatele");
    resources.put("back",                        "Zpět");
    // #sijapp cond.if target is "SIEMENS" #
    resources.put("beep",                        "Pípnutí");
    // #sijapp cond.end #
    resources.put("byte",                        "Byte");
    resources.put("cancel",                      "Zrušit");
    resources.put("close",                       "Zavřít");
    resources.put("connect",                     "Připojení");
    resources.put("connecting",                  "Připojuji");
    resources.put("contact_list",                "Seznam");
    resources.put("cpd",                         "Cena za den");
    resources.put("cpp",                         "Cena za paket");
    resources.put("currency",                    "Měna");
    resources.put("delete_chat",                 "Smazat diskuzi");
    resources.put("user_menu",                   "Uživatelské menu");
    resources.put("disconnect",                  "Odpojení");
    resources.put("disconnecting",               "Odpojuji");
    resources.put("display_advertisement",       "Připojit Jimm tip?");
    resources.put("display_date",                "Zobrazit datum na obrazovce?");
    resources.put("email",                       "Email");
    resources.put("error",                       "Chyba");
    resources.put("exit",                        "Konec");
    resources.put("hide_offline",                "Skrýt odpojené kontakty");
    resources.put("info",                        "Info");
    resources.put("kb",                          "kB");
    resources.put("keep_chat",                   "Zaznamenávat historii?");
    resources.put("keep_conn_alive",             "Udržovat spojení?");
    resources.put("keylock_enable",              "Blokování kláves");
    resources.put("keylock_enabled",             "Klávesy blokovány");
    resources.put("language",                    "Jazyk");
    resources.put("lang_BR",                     "Portugalštině Brazilské");
    resources.put("lang_CZ",                     "Ĉesky");
    resources.put("lang_DE",                     "Německy");
    resources.put("lang_EN",                     "Anglicky");
    resources.put("lang_ES",                     "Španělsky");
    resources.put("lang_RU",                     "Rusky");
    resources.put("lang_SE",                     "Svenska"); 
    resources.put("loading",                     "Nahrávám");
    resources.put("me",                          "já");
    resources.put("menu",                        "Menu");
    resources.put("message",                     "Zpráva");
    resources.put("message_from",                "Zpráva od");
    // #sijapp cond.if target is "SIEMENS" #
    resources.put("message_notification",        "Oznámení zprávy");
    // #sijapp cond.end #
    resources.put("name",                        "Jméno");
    resources.put("nick",                        "Přezdívka");
    resources.put("no",                          "Nic");
    resources.put("not_implemented",             "Funkce není zprovozněna.");
    resources.put("notice",                      "Poznámka");
    resources.put("ok",                          "Ano");
    resources.put("once_a_session",              "1x ve spojení");
    resources.put("options",                     "Nastavení");
    resources.put("options_account",             "Účet");
    resources.put("options_cost",                "Poplatky");
    resources.put("options_effect",              "Musíte se nově připojit aby se některé změny projevily!");
    resources.put("options_interface",           "Zobrazení/zvuk");
    resources.put("options_network",             "Síť");
    resources.put("options_other",               "Ostatní");
    resources.put("password",                    "Heslo");
    resources.put("plength",                     "Délka placeného paketu v kB");
    resources.put("remove",                      "Odstranit ze seznamu");
    resources.put("reply",                       "Odpověď");
    resources.put("reset",                       "Reset");
    resources.put("save",                        "Uložit");
    resources.put("search_user",                 "Vyhledání uživatele");
    resources.put("send",                        "Odeslat");
    resources.put("send_message",                "Nová zpráva");
    resources.put("send_url",                    "Nová URL");
    resources.put("server",                      "Login server");
    resources.put("server_host",                 "Jméno serveru");
    resources.put("server_port",                 "Port");
    resources.put("session",                     "Spojení");
    resources.put("set_status",                  "Nastavení stavu");
    resources.put("since",                       "Celkem od");
    resources.put("sound",                       "ICQ Zvuk");
    resources.put("sound_file_name",             "Jméno zvukového souboru");
    resources.put("sort_by",                     "Seřadit seznam");
    resources.put("sort_by_name",                "Dle jména");
    resources.put("sort_by_status",              "Dle stavu");
    resources.put("status_away",                 "Pryč");
    resources.put("status_chat",                 "Volný k diskuzi");
    resources.put("status_dnd",                  "Nerušit");
    resources.put("status_invisible",            "Neviditelný");
    resources.put("status_na",                   "Nedostupný");
    resources.put("status_occupied",             "Zaneprázdněn");
    resources.put("status_offline",              "Odpojen");
    resources.put("status_online",               "Připojen");
    resources.put("traffic",                     "Data info");
    resources.put("uin",                         "UIN");
    resources.put("url",                         "URL");
    resources.put("user_add",                    "Přidání uživatele");
    resources.put("user_search",                 "Hledání uživatele");
    // #sijapp cond.if target is "SIEMENS" #
    resources.put("vibration",                   "Vibrace");
    // #sijapp cond.end #
    resources.put("wait",                        "Čekej prosím ...");
    resources.put("warning",                     "Výstraha");
    resources.put("yes",                         "Ano");

    // Generic errors
    resources.put("error_100",                   "Neznámá chyba (#100.EXT)");

    // Login specific errors
    resources.put("error_110",                   "Vícenásobné přihlášení stejným UIN (#110.EXT)");
    resources.put("error_111",                   "Špatné heslo (#111.EXT)");
    resources.put("error_112",                   "Neexistující UIN (#112.EXT)");
    resources.put("error_113",                   "Příliš mnoho klientů stejného IP (#113.EXT)");
    resources.put("error_114",                   "Překročení rychlosti (#114.EXT)");
    resources.put("error_115",                   "Seznam kontaktů nelze přečíst (#115.EXT)");
    resources.put("error_116",                   "Offline zprávy nelze přečíst (#116.EXT)");
    resources.put("error_117",                   "Nevyplněn UIN a/nebo heslo (#117.EXT)");
    resources.put("error_118",                   "Server neodpovídá (#118.EXT)");

    // Network communication specific exceptions
    resources.put("error_120",                   "Chyba I/O (#120.EXT)");
    resources.put("error_121",                   "Požadované TCP spojení nelze vytvořit (#121.EXT)");
    resources.put("error_122",                   "Zadaný server a/nebo port neexistuje (#122.EXT)");
    resources.put("error_123",                   "Spojení nemohlo být navázáno (#123.EXT)");
    resources.put("error_124",                   "Vstupní datový tok není synchronizován (#124.EXT)");

    // Parsing specific errors
    resources.put("error_130",                   "FLAP-Záhlaví nemůže být zpracováno (#130.EXT)");
    resources.put("error_131",                   "Neznámý kanál (#131.EXT)");
    resources.put("error_132",                   "Paket na připojeném kanálu nemůže být zpracován (#132.EXT)");
    resources.put("error_133",                   "SNAC-Záhlaví nemůže být zpracováno (#133.EXT)");
    resources.put("error_134",                   "Paket na chybovém kanálu nemůže být zpracován (#134.EXT)");
    resources.put("error_135",                   "Paket na Disconnect-kanálu nemůže být zpracován (#135.EXT)");
    resources.put("error_136",                   "Paket na Ping-kanálu nemůže být zpracován (#136.EXT)");
    resources.put("error_137",                   "Starší záhlaví ICQ-Protokolu nemůže být zpracováno (#137.EXT)");

    // Action errors
    resources.put("error_140",                   "Požadovaná akce nemůže být nyní zařazena do zpracování (#140.EXT)");

    // Specific action errors
    resources.put("error_150",                   "Přijatá zpráva byla nesrozumitelná (#150.EXT)");
    resources.put("error_151",                   "Přijatá zpráva tipu 1 byla nesrozumitelná (#151.EXT)");
    resources.put("error_152",                   "Přijatá zpráva tipu 2 byla nesrozumitelná (#152.EXT)");
    resources.put("error_153",                   "Přijatá zpráva tipu 4 byla nesrozumitelná (#153.EXT)");
    resources.put("error_154",                   "Aktualizace seznamu kontaktů selhala (#154.EXT)");

    // Other errors
    resources.put("error_160",                   "Nedostatek zásobníkové paměti (#160.EXT)");
    resources.put("error_161",                   "Meta-Informace jsou nedostupné (#161.EXT)");

  }


}


// #sijapp cond.end #
