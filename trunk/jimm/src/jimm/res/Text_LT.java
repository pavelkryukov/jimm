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
 File: src/jimm/res/Text_LT.java
 Version: ###VERSION###  Date: ###DATE###
 Based on CVS 1.74 Text_EN (2005.09.26 09:00)
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
		// MOTOROLA formatted buttons
		// #sijapp cond.if target is "MOTOROLA"#
    	resources.put("backlight_timeout",           "Backlight timeout (sec)");
	    resources.put("backlight_manual",            "Manual backlight (* key)");
		resources.put("select",                      "Pasirinkti  ");
		resources.put("reset",                       "   Atstatyti");
		resources.put("cancel",                      "  Atsisakyti");
		resources.put("back",                        "       Atgal");
		resources.put("exit_button",                 "      Išeiti");
		resources.put("menu_button",                 "     Meniu");
		resources.put("save",                        "Saugoti    ");
		resources.put("ok",                          "OK          ");
		resources.put("reply",                       "Atsakyti    ");
		resources.put("close",                       "    Uždaryti");
		resources.put("search_user",                 "Ieškoti vartotojo");
		resources.put("send",                        "Saugoti     ");
		// #sijapp cond.else#
		resources.put("reply",                       "Atsakyti");
		resources.put("close",                       "Uždaryti");
		resources.put("select",                      "Pasirinkti");
		resources.put("reset",                       "Atstatyti");
		resources.put("cancel",                      "Atsisakyti");
		resources.put("back",                        "Atgal");
		resources.put("save",                        "Saugoti");
		resources.put("ok",                          "OK");
		resources.put("search_user",                 "Ieškoti vartotojo");
		resources.put("send",                        "Siųsti");
		// #sijapp cond.end#

		resources.put(".",                           ".");
		resources.put("about",                       "Apie");
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nICQ klientas J2ME palaikantiems telefonams\n\nVersija: ###VERSION###\nTipas: ###TARGET###\nModuliai: ###MODULES###\n\nDaugiau informacijos http://www.jimm.org/");
		resources.put("account",                     "Paskyra");
		resources.put("add_group",                   "Sukurti grupę");
		resources.put("add_user",                    "Pridėti vartotoją");
		resources.put("add_to_list",                 "Įtraukti į sarašą");
		resources.put("age",                         "Amžius");		
		resources.put("async",                       "Asinchroniškas");
		resources.put("attention",                   "Dėmėsio!");
		resources.put("auth",                        "Autor.");		
		resources.put("auto_connect",                "Automatiškai prisijungti paleidus programą");
		resources.put("beep",                        "Pyptelėjimas");
		resources.put("black_on_white",              "Juodu ant balto");
		resources.put("byte",                        "Baitas");
		resources.put("chat",                        "Pokalbis");
		resources.put("chat_small_font",             "Naudoti mažą šriftą dialogo lange");
		resources.put("city",                        "Miestas");		
		resources.put("clear",                       "Išvalyti");
		resources.put("color_scheme",                "Spalvinė schema");
		resources.put("conn_type",                   "Prisijungimo tipas");
		resources.put("con_wait",                    "Laukti tarp prisijungimų (sec)");
		resources.put("connect",                     "Prisijungti");
		resources.put("connecting",                  "Jungiamasi");
		resources.put("copy_text",                   "Kopijuoti tekstą");
		resources.put("cost",                        "Kaina");
		resources.put("contact_list",                "Sąrašas");
		resources.put("cp1251",                      "Naudoti CP1251 pritaikymą?");
		resources.put("cpd",                         "Kaina per dieną");
		resources.put("cpp",                         "Kaina per paketą");
		resources.put("currency",                    "Valiuta");
		resources.put("dc_info",                     "DC Info");
		resources.put("del_group",                   "Ištrinti vartotojų grupę");
		resources.put("delete_chat",                 "Ištrinti pokalbio istoriją");
		resources.put("delete_chat",                 "Ištrinti istoriją");
		resources.put("deny",				 		 "Atmesti autorizavimą");
		resources.put("denyedby",					 "Atorizavimas atmestas: ");
		resources.put("description",                 "Aprašymas");
		resources.put("name_desc",                   "Failas vardas ir aprašymas");		
		resources.put("user_menu",                   "Vartotojo meniu");
		resources.put("disconnect",                  "Atsijungti");
		resources.put("disconnecting",               "Atsijungiama");
		resources.put("display_date",                "Rodyti datą startiniame lange?");
		resources.put("email",                       "El.paštas");
		resources.put("emotions",                    "Šypsenėlės");
		resources.put("error",                       "Klaida");
		resources.put("exec",                        "Vykdyti");
		resources.put("exit",                        "Išeiti");
		resources.put("female",                      "M");
		resources.put("female_male",                 "M/V");
		resources.put("filename",                    "Bylos pavadinimas");
		resources.put("filetransfer",                "File transfer");
		resources.put("filepath",                    "File path");		
		resources.put("find",                        "Rasti");
		resources.put("firstname",                   "Vardas");
		resources.put("free_heap",                   "Laisva atmintis");
		resources.put("ft_name",                     "Siųsti failą");		
		resources.put("ft_cam",                      "Siųsti nuotrauką iš kameros");		
		resources.put("gender",                      "Lytis");		
		resources.put("grant",						 "Autorizuoti");
		resources.put("grantedby",					 "Jūs autorizuotas:");	
		resources.put("group_name",                  "Grupės vardas");
		resources.put("group_is_not_empty",          "Pasirinkta grupė nėra tuščia!\nPerkelkite iš pradžių įrašus į kitas grupes!");
		resources.put("have_unread_mess",            "Turite neatsakytų žinučių. Norite išeiti?");			
		resources.put("hide_offline",                "Paslėpti neprisijungusius");
		resources.put("info",                        "Informacija");
		resources.put("kb",                          "kB");
		resources.put("kbs",                         "kb/s");
		resources.put("init_ft",                     "Pradedama");
		resources.put("insert_emotion",              "Įterpti veiduką");
		resources.put("keep_chat",                   "Išsaugoti istoriją?");
		resources.put("keep_conn_alive",             "Palaikyti pastovų ryšį?");
		resources.put("keylock",                     "Užrakinta klaviatūra");
		resources.put("keylock_message",             "Palaikykite \"#\" norėdami atblokuoti klaviatūrą");
		resources.put("keylock_enable",              "Užrakinti klaviatūrą");
		resources.put("keylock_enabled",             "Klaviatūra užrakinta");
		resources.put("keyword",                     "raktažodis");		
		resources.put("language",                    "Kalba");
		resources.put("lang_BG",                     "Bulgarų");
		resources.put("lang_BR",                     "Portugalų (Brazilija)");
		resources.put("lang_CZ",                     "Čekų");
		resources.put("lang_DE",                     "Vokiečių");
		resources.put("lang_EN",                     "Anglų");
		resources.put("lang_ES",                     "Ispanų");
		resources.put("lang_HE",                     "Hebrajų");
		resources.put("lang_IT",                     "Italų");		
		resources.put("lang_LT",                     "Lietuvių");
		resources.put("lang_PL",                     "Lenkų");
		resources.put("lang_RU",                     "Rusų");
		resources.put("lang_SE",                     "Švedų");
		resources.put("lang_SR",                     "Serbų");
		resources.put("lang_UA",                     "Ukrainiečių");	
		resources.put("lastname",                    "Pavardė");		
		resources.put("loading",                     "Kraunasi");
		resources.put("male",                        "V");		
		resources.put("me",                          "aš");
		resources.put("menu",                        "Meniu");
		resources.put("message_notification",        "Žinutės pranešimas");		
		resources.put("msg_sound_file_name",         "Žinutės garsas");
		resources.put("message",                     "Žinutė");
		resources.put("message_from",                "Žinutė nuo");
		resources.put("minimize",                    "Sumažintas");
		resources.put("name",                        "Vardas");
		resources.put("next",	 					 "Toliau");		
		resources.put("nick",                        "Slapyvardis");
		resources.put("no",                          "Ne");
		resources.put("not",                         "ne");
		resources.put("no_results",                  "be rezultatų");
		resources.put("no_not_empty_gr",             "Ne tuščių grupių trynimas dar nepalaikomas!");
		resources.put("no_not_empty_gr",             "Tik tuščias grupes galima trinti (kol kas)");
		resources.put("not_implemented",             "Funkcija dar neveikia.");
		resources.put("noreason",                    "Nepateikė priežasties.");
		resources.put("notice",                      "Pranešimas");
		resources.put("nr",                          "Nr");
		resources.put("once_a_session",              "1k.per sesiją?");
		resources.put("onl_notification",            "Pranešimas apie prisijungusį");
  		resources.put("onl_sound_file_name",         "Online garsas");
	  	resources.put("only_online",                 "Rodyti tik prisijungusius");
		resources.put("options",                     "Savybės");
		resources.put("options_account",             "Vartotojas");
		resources.put("options_cost",                "Kaina");
		resources.put("options_effect",              "Jums gali tekti prisijungti dar kartą, kad pamatytumėte pakeitimus!");
		resources.put("options_interface",           "Sąsaja");
		resources.put("options_network",             "Tinklas");
		resources.put("options_other",               "Kiti");
		resources.put("options_signaling",           "Pranešimai");
		resources.put("password",                    "Slaptažodis");
		resources.put("plength",                     "Apmokestinamo paketo dydis kilobaitais");
	    resources.put("plsauthme",                   "Hi! Please authorise my request to add you to my contact list.");
	  	resources.put("prev",                        "Ankstesnis");
		resources.put("quote",                       "Cituoti");
		resources.put("reason",		    			 "Priežastis");		
		resources.put("remove",                      "Pašalinti iš sąrašo");
		resources.put("remove_me",                   "Pašalinti mane");
		resources.put("remove_me_from",              "Pašalinti jus iš kontaktų sąrašo ");
		resources.put("rename",                      "Pervadinti");
		resources.put("remove_group",                "Ištrinti grupę");
		resources.put("remove_user",                 "Ištrinti vartotoją");
		resources.put("requauth",                    "Prašyti autorizavimo");
		resources.put("reqstatmsg",                  "Sužinoti statusą");
		resources.put("requ",                        "Reikalingas");
		resources.put("requno",                      "Nereikalingas");
		resources.put("res",                         "Raiška");
		resources.put("results",                     "Rezultatai");
		resources.put("search_user",                 "Ieškoti vartotojo");
		resources.put("send_img",                    "Siųsti paveiksliuką");				
		resources.put("send_message",                "Nauja Žinutė");
		resources.put("send_url",                    "Nauja Nuoroda");
		resources.put("server",                      "Prisijungimo serveris");
		resources.put("server_host",                 "Serveris");
		resources.put("server_port",                 "Prievadas");
		resources.put("session",                     "Sesija");
		resources.put("set_status",                  "Būsena");
		resources.put("shadow_con",                  "Shadow connection (Motorola?)");		
		resources.put("show_user_groups",            "Rodyti grupes");
		resources.put("since",                       "Nuo");
		resources.put("size",                        "Dydis");
		resources.put("sound",                       "ICQ Garsas");
		resources.put("sound_file_name",             "Garsinis failas");
		resources.put("sort_by",                     "Rušiuoti sarašą");
		resources.put("sort_by_name",                "Pagal vardą");
		resources.put("sort_by_status",              "Pagal statusą");
		resources.put("speed",                       "Greitis");
		resources.put("status",                      "Statusas");
		resources.put("status_message",              "Būsenos pranešimas");
		resources.put("status_away",                 "Kažkur šalia");
		resources.put("status_chat",                 "Noriu kalbėtis!");
		resources.put("status_dnd",                  "Netrukdyti!");
		resources.put("status_invisible",            "Pasislėpęs");
		resources.put("status_na",                   "Nepasiekiamas");
		resources.put("status_occupied",             "Užimtas");
		resources.put("status_offline",              "Atsijungęs");
		resources.put("status_online",               "Prisijungęs");
		resources.put("successful",                  "sėkmingas");
		resources.put("sysnotice",                   "Sistemos pranešimas");
		resources.put("traffic",                     "Srautas");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "Nuoroda");
		resources.put("use_history",                 "Saugoti istoriją");
		resources.put("use_smiles",                  "Rodyti veidukus");
		resources.put("user_add",                    "Prideti vartotoja");
		resources.put("user_search",                 "Ieškoti vartotojo");
		resources.put("vibration",                   "Vibracija");
		resources.put("viewfinder",                  "Viewfinder");
		resources.put("volume",                      "Garsas");		
		resources.put("wait",                        "Prašome palaukti ...");
		resources.put("when_locked",                 "Kai užrakinta");
		resources.put("warning",                     "Įspėjimas");
		resources.put("wantsyourauth",	  			 " nori būti autorizuotas. Tikslas: ");
		resources.put("was",                         "buvo");
		resources.put("whichgroup",                  "Kuri grupė?");
		resources.put("white_on_black",              "Baltu ant juodo");
		resources.put("white_on_blue",               "Baltu ant mėlyno");
		resources.put("yes",                         "Taip");
		resources.put("youwereadded",	    		 "Įtraukas į sąrašą pas: ");
		
		//#sijapp cond.if modules_HISTORY is "true" #
		resources.put("add_to_history",              "Įtraukti į istoriją");
		resources.put("text_to_find",                "Tekstas");
		resources.put("find_backwards",              "Atgalinis");
		resources.put("find_case_sensitiv",          "Case sensitive");
		resources.put("history_info",                "Informacija");
		resources.put("hist_cur",                    "Žinutės");
		resources.put("hist_rc",                     "Viso žinučių");
		resources.put("hist_size",                   "Sunaudota (kB)");
		resources.put("hist_avail",                  "Viso (kB)");
		resources.put("history",                     "Pokalbių istorija");
		resources.put("not_found",                   "nerasta");
		resources.put("clear_all",                   "Išvalyti istoriją");
		resources.put("clear_all2",                  "Išvalyti visų kontaktų istorijas?");
		resources.put("clear_history",               "Išvalyti išsaugotas istorijas");
		resources.put("ch_never",                    "Niekada");
		resources.put("ch_day",                      "Kiekvieną dieną");
		resources.put("ch_week",                     "Kiekvieną savaitę");
		resources.put("ch_month",                    "Kiekvieną mėnesį");
	 	//#sijapp cond.end#
		
		// Generic errors
		resources.put("error_100",                   "Nežinoma klaida (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "Dar vienas prisijungimas tuo pačiu UIN (#110.EXT)");
		resources.put("error_111",                   "Negeras slaptažodis (#111.EXT)");
		resources.put("error_112",                   "Neegzstuojantis UIN (#112.EXT)");
		resources.put("error_113",                   "Per daug prisijungimu iš to paties IP (#113.EXT)");
		resources.put("error_114",                   "Rate exceeded (#114.EXT)");
		resources.put("error_115",                   "Negaliu perskaityti kontaktų sarašo (#115.EXT)");
		resources.put("error_116",                   "Neperskaitau Offline žinutės (#116.EXT)");
		resources.put("error_117",                   "Neįrašytas UIN ir/arba slaptažodis (#117.EXT)");
		resources.put("error_118",                   "Nėra atsakymo iš serverio (#118.EXT)");

		// Network communication specific exceptions
		// Connection to server
		resources.put("error_120",                   "I/O klaida! (#120.EXT)");
		resources.put("error_121",                   "Requested TCP connection cannot be made (#121.EXT)");
		resources.put("error_122",                   "Neteisingas nurodyto serverio adresas ir/arba prievadas (#122.EXT)");
		resources.put("error_123",                   "Nepavyko prisijungti (#123.EXT)");
		resources.put("error_124",                   "Input stream is out of sync (#124.EXT)");

		// Peer connection
		resources.put("error_125",                   "I/O klaida (#125.EXT)");
		resources.put("error_126",                   "Requested TCP connection cannot be made (#126.EXT)");
		resources.put("error_127",                   "Specified server host and/or port is invalid (#127.EXT)");
		resources.put("error_128",                   "Nepavyko prisijungti (#128.EXT)");
		resources.put("error_129",                   "Input stream is out of sync (#129.EXT)");

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
		resources.put("error_140",                   "Prašomas darbas negali būti šiuo metu vykdomas (#140.EXT)");

		// Specific action errors
		resources.put("error_150",                   "Nesuprantu gautos žinutes (#150.EXT)");
		resources.put("error_151",                   "Nesuprantu gautos 1 tipo žinutes (#151.EXT)");
		resources.put("error_152",                   "Nesuprantu gautos 2 tipo žinutes (#152.EXT)");
		resources.put("error_153",                   "Nesuprantu gautos 4 tipo žinutes (#153.EXT)");
		resources.put("error_154",                   "Kontaktų sarašo atnaujinimas nepavyko (#154.EXT)");
		resources.put("error_155",                   "Objektas jau jūsų saraše (serveryje) (#155.EXT)");
		resources.put("error_156",                   "Klaida įtraukiant į sąrašą. Bandykite vėliau (#156.EXT)");
		resources.put("error_157",                   "Nebeleidžiama pridėti naujų (#157.EXT)");
		resources.put("error_158",                   "Jūs bandėt įtraukti ICQ kontaktą į AIM sąrašą (#158.EXT)");
		resources.put("error_159",                   "Nėra atsakymo iš serverio. Bandykite vėliau (#159.EXT)");
		resources.put("error_160",                   "Klaida atliekant paiešką (#160.EXT)");
		resources.put("error_161",                   "Nėra sukurtų vartotojų grupių. Sukurkite grupę! (#161.EXT)");

		// Other errors
		resources.put("error_170",                   "Greičiausiai neužtenka meta atminties (#160.EXT)");
		resources.put("error_171",                   "Negaliu perskaityti meta informacijos (#161.EXT)");
		
		// Camera errors
		resources.put("error_180",                   "Klaida sukuriant vaizdo apdorojimo objektą (#180.EXT)");
		resources.put("error_181",                   "Išankstinės fotografavimo vaizdo paieškos bandymo sukurti klaida (#181.EXT)");
		resources.put("error_182",                   "Išankstinės fotografavimo vaizdo paieškos klaida (#182.EXT)");
		resources.put("error_183",                   "Fotografavimo klaida (#183.EXT)");
		resources.put("error_185",                   "Fotografavimas iš Java programų nepalaikomas (#185.EXT)");
		
		// File transfer errors
		resources.put("error_190",                   "Apsikeitimas su  < ICQv8 protokolo klientais nepalaikomas (#190.EXT)");
		resources.put("error_191",                   "Klaida skaitant failą. Greičiausiai nepalaikoma (#191.EXT)");
		resources.put("error_192",                   "Klaida skaitant failą. Blogai nurodytas kelias arba nepalaikoma (#192.EXT)");
		resources.put("error_193",                   "Klaida bandant naršyti failų sistemą. Saugumo klaida (#193.EXT)");		
	}
}

// #sijapp cond.end #