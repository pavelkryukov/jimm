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
    resources.put("add_user",                    "Pridėti vartotoją");
	resources.put("async",                    	 "Asinchroniškas");    
    resources.put("back",                        "Atgal");
    // #sijapp cond.if target is "SIEMENS" #
    resources.put("beep",                        "Pyptelėjimas");
    // #sijapp cond.end #
    resources.put("byte",                        "Baitas");
    resources.put("cancel",                      "Atsisakyti");
    resources.put("close",                       "Uždaryti");
	resources.put("conn_type",                   "Prisijungimo tipas");    
    resources.put("connect",                     "Prisijungti");
    resources.put("connecting",                  "Jungiamasi");
    resources.put("contact_list",                "Sąrašas");
	resources.put("cp1251",                      "Naudoti CP1252 pritaikymą?");    
    resources.put("cpd",                         "Kaina per dieną");
    resources.put("cpp",                         "Kaina per paketą");
    resources.put("currency",                    "Valiuta");
    resources.put("delete_chat",                 "Ištrinti istoriją");
    resources.put("user_menu",                   "Vartotojo meniu");
    resources.put("disconnect",                  "Atsijungti");
    resources.put("disconnecting",               "Atsijungiama");
    resources.put("display_advertisement",       "Reklamuoti Jimm'ą?");
    resources.put("display_date",                "Rodyti datą startiniame lange?");
    resources.put("email",                       "El.paštas");
    resources.put("error",                       "Klaida");
    resources.put("exit",                        "Išėjimas");
    resources.put("hide_offline",                "Paslėpti neprisijungusius");
    resources.put("info",                        "Informacija");
    resources.put("kb",                          "kB");
    resources.put("keep_chat",                   "Pasilikti pokalbių išklotinę?");
    resources.put("keep_conn_alive",             "Palaikyti pastovų ryšį?");
    resources.put("keylock_enable",              "Užrakinti klaviatūrą");
    resources.put("keylock_enabled",             "Klaviatūra užrakinta");
    resources.put("language",                    "Kalba");
    resources.put("lang_BR",                     "Portugalų (Brazilija)");
    resources.put("lang_CZ",                     "Čekų");
    resources.put("lang_DE",                     "Vokiečių");
    resources.put("lang_EN",                     "Anglų");
    resources.put("lang_ES",                     "Ispanų");
    resources.put("lang_RU",                     "Rusų");
    resources.put("lang_SE",                     "Švedų");
	resources.put("lang_LT",                     "Lietuvių");    
    resources.put("loading",                     "Kraunasi");
    resources.put("me",                          "aš");
    resources.put("menu",                        "Meniu");
    resources.put("message",                     "Žinutė");
    resources.put("message_from",                "Žinutė nuo");
    // #sijapp cond.if target is "SIEMENS" #
    resources.put("message_notification",        "Pranešti apie žinutę");
    // #sijapp cond.end #
    resources.put("name",                        "Vardas");
    resources.put("nick",                        "Slapyvardis");
    resources.put("no",                          "Ne");
    resources.put("not_implemented",             "Funkcija dar neveikia.");
    resources.put("notice",                      "Pranešimas");
    resources.put("ok",                          "OK");
    resources.put("once_a_session",              "1k.per sesiją?");
    resources.put("options",                     "Savybės");
    resources.put("options_account",             "Vartotojas");
    resources.put("options_cost",                "Kaina");
    resources.put("options_effect",              "Jums gali tekti prisijungti dar kartą, kad pamatytumėte pakeitimus!");
    resources.put("options_interface",           "Sąsaja");
    resources.put("options_network",             "Tinklas");
    resources.put("options_other",               "Kiti");
    resources.put("password",                    "Slaptažodis");
    resources.put("plength",                     "Apmokestinamo paketo dydis kilobaitais");
    resources.put("remove",                      "Pašalinti iš sąrašo");
    resources.put("reply",                       "Atsakyti");
    resources.put("reset",                       "Valyti");
    resources.put("save",                        "Išsaugoti");
    resources.put("search_user",                 "Ieškoti vartotojo");
    resources.put("send",                        "Siųsti");
    resources.put("send_message",                "Nauja Žinutė");
    resources.put("send_url",                    "Nauja Nuoroda");
    resources.put("server",                      "Prisijungimo serveris");
    resources.put("server_host",                 "Serveris");
    resources.put("server_port",                 "Prievadas");
    resources.put("session",                     "Sesija");
    resources.put("set_status",                  "Nustatyti statusą");
    resources.put("since",                       "Nuo");
    resources.put("sound",                       "ICQ Garsas");
    resources.put("sound_file_name",             "Garsinis failas");
    resources.put("sort_by",                     "Rušiuoti sąrašą");
    resources.put("sort_by_name",                "Pagal vardą");
    resources.put("sort_by_status",              "Pagal statusą");
    resources.put("status_away",                 "Kažkur šalia");
    resources.put("status_chat",                 "Noriu kalbėtis!");
    resources.put("status_dnd",                  "Netrukdyti!");
    resources.put("status_invisible",            "Pasislėpęs");
    resources.put("status_na",                   "Nepasiekiamas");
    resources.put("status_occupied",             "Užimtas");
    resources.put("status_offline",              "Atsijungęs");
    resources.put("status_online",               "Prisijungęs");
    resources.put("traffic",                     "Srautas");
    resources.put("uin",                         "UIN");
    resources.put("url",                         "Nuoroda");
    resources.put("user_add",                    "Pridėti vartotoją");
    resources.put("user_search",                 "Ieškoti vartotojo");
    // #sijapp cond.if target is "SIEMENS" #
    resources.put("vibration",                   "Vibracija");
    // #sijapp cond.end #
    resources.put("wait",                        "Prašome palaukti ...");
    resources.put("warning",                     "Įspėjimas");
    resources.put("yes",                         "Taip");

    // Generic errors
    resources.put("error_100",                   "Nežinoma klaida (#100.EXT)");

    // Login specific errors
    resources.put("error_110",                   "Dar vienas prisijungimas tuo pačiu UIN (#110.EXT)");
    resources.put("error_111",                   "Negeras slaptažodis (#111.EXT)");
    resources.put("error_112",                   "Neegzstuojantis UIN (#112.EXT)");
    resources.put("error_113",                   "Per daug prisijungimų iš to paties IP (#113.EXT)");
    resources.put("error_114",                   "Rate exceeded (#114.EXT)");
    resources.put("error_115",                   "Negaliu perskaityti kontaktų sąrašo (#115.EXT)");
    resources.put("error_116",                   "Offline message could not be parsed (#116.EXT)");
    resources.put("error_117",                   "Neįrašytas UIN ir/arba slaptažodis (#117.EXT)");
    resources.put("error_118",                   "Nėra atsakymo iš serverio (#118.EXT)");

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
    resources.put("error_150",                   "Nesuprantu gautos žinutės (#150.EXT)");
    resources.put("error_151",                   "Nesuprantu gautos 1 tipo žinutės (#151.EXT)");
    resources.put("error_152",                   "Nesuprantu gautos 2 tipo žinutės (#152.EXT)");
    resources.put("error_153",                   "Nesuprantu gautos 4 tipo žinutės (#153.EXT)");
    resources.put("error_154",                   "Kontaktų sąrašo atnaujinimas nepavyko (#154.EXT)");

    // Other errors
    resources.put("error_160",                   "Possibly not enough heap memory available (#160.EXT)");
    resources.put("error_161",                   "Could not fetch meta info (#161.EXT)");

  }


}


// #sijapp cond.end #