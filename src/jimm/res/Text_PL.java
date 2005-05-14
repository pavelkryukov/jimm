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
 File: src/jimm/res/Text_PL.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Pawe� G�ogowski
 *******************************************************************************/


// #sijapp cond.if lang_PL is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_PL extends ResourceBundle
{


	public Text_PL()
	{


		// Labels
		// MOTOROLA formatted buttons
		// #sijapp cond.if target is "MOTOROLA"#
		resources.put("backlight_manual",            "W��czenie pod�wietlania  (Klawisz *)");
		resources.put("backlight_timeout",           "Automatyczne wy��czenie  pod�wietlania (w sekundach)");	
		resources.put("select",                      "Zaznacz     ");
		resources.put("reset",                       "       Reset");
		resources.put("cancel",                      "      Anuluj");
		resources.put("back",                        "     Wstecz");
		resources.put("exit_button",                 "       Wyjdź");
		resources.put("menu_button",                 "      Menu");
		resources.put("save",                        "Zapisz     ");
		resources.put("ok",                          "OK          ");
		resources.put("reply",                       "Odpowiedz   ");
		resources.put("close",                       "     Zamknij");
		resources.put("send",                        "Wyślij      ");
		resources.put("next",                        "      Dalej");
		// #sijapp cond.else#
		resources.put("reply",                       "Odpowiedz");
		resources.put("close",                       "Zamknij");
		resources.put("select",                      "Zaznacz");
		resources.put("reset",                       "Reset");
		resources.put("cancel",                      "Anuluj");
		resources.put("back",                        "Wstecz");
		resources.put("save",                        "Zapisz");
		resources.put("ok",                          "OK");
		resources.put("send",                        "Wyślij");
		resources.put("next",                        "Dalej");
		// #sijapp cond.end#

		resources.put(".",                           ".");
		resources.put("about",                       "O programie");
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nKlient ICQ na platform� J2ME\n\nWersja: ###VERSION###\nObiekt: ###TARGET###\nModu�: ###MODULES###\n\nZobacz: http://www.jimm.org/");
		resources.put("account",                     "Konto");
		resources.put("add_group",                   "Dodaj grupę");
		resources.put("add_user",                    "Dodaj użytkownika");
		resources.put("add_to_list",                 "Dodaj do listy");
		resources.put("age",                         "Wiek");
		resources.put("async",                       "Async");
		resources.put("attention",                   "Ostrzeżenie!");
		resources.put("auth",                        "Auth");
		resources.put("auto_connect",                "Automatycznie łącz podczas uruchamiania");	
		resources.put("beep",                        "Beep");
		resources.put("black_on_white",              "Czarno biały");
		resources.put("byte",                        "B");
		resources.put("chat_small_font",             "Male litery podczas rozmowy");
		resources.put("city",                        "Miasto");
		resources.put("clear",                       "Wyczyść");
		resources.put("color_scheme",                "Schemat kolorów");
		resources.put("conn_type",                   "Typ połączenia");
		resources.put("connect",                     "Połącz");
		resources.put("connecting",                  "Łączenie");
		resources.put("cost",                        "Koszt");
		resources.put("contact_list",                "Lista");
		resources.put("cp1251",                      "Użyć kodowania CP1251?");
		resources.put("cpd",                         "Koszt dzienny");
		resources.put("cpp",                         "Koszt pakietu");
		resources.put("currency",                    "Aktualnie");
		resources.put("del_group",                   "Usuń grupę");
		resources.put("delete_chat",                 "Usuń rozmowę");
		resources.put("deny",                        "Autoryzacja odrzucona");
		resources.put("denyedby",                    "Żądanie autoryzacji zostało odrzucone przez: ");
		resources.put("description",                 "Opis");
		resources.put("name_desc",                   "Nazwa pliku i opis");
		resources.put("user_menu",                   "Menu użytkownika");
		resources.put("disconnect",                  "Rozłącz");
		resources.put("disconnecting",               "Rozłączanie");
		resources.put("display_date",                "Wyświetlać datę na ekranie tytułowym?");
		resources.put("email",                       "Email");
		resources.put("error",                       "Błąd");
		resources.put("exec",                        "Wykonaj");
		resources.put("exit",                        "Wyjdź");
		resources.put("female",                      "Kobieta");
		resources.put("filetransfer",                "Transfer pliku");
		resources.put("filepath",                    "Ścieżka do pliku");
		resources.put("find",                        "Znajdź");
		resources.put("firstname",                   "Imię");
		resources.put("free_heap",                   "Wolna pamięć");
		resources.put("ft_name",                     "Wyślij plik");
		resources.put("ft_cam",                      "Wyślij obraz z kamery");
		resources.put("gender",                      "Płeć");
		resources.put("grant",                       "Autoryzacja przyjęta");
		resources.put("grantedby",                   "Żądanie autoryzacji zostało przyjęte przez:");
		resources.put("group_name",                  "Nazwa grupy");
		resources.put("group_is_not_empty",          "Zaznacz grupę jeśeli nie jest pusta!\nPrzenieś wszystkich użytkowników do innej grupy!");
		resources.put("have_unread_mess",            "Masz nieprzeczytane wiadomości. Napewno chcesz wyjść?");
		resources.put("hide_offline",                "Ukryj wylogowanych użytkowników");
		resources.put("info",                        "Informacja");
		resources.put("init_ft",                     "Inicjalizacja");
		resources.put("kb",                          "kB");
		resources.put("kbs",                         "kb/s");
		resources.put("keep_chat",                   "Zachować historię rozmowy?");
		resources.put("keep_conn_alive",             "Utrzymać aktywne połączenie?");
		resources.put("keylock",                     "Blokada klawiszy");
		resources.put("keylock_message",             "Przytrzymaj \"#\" w celu wyłączenia blokady klawiszy");
		resources.put("keylock_enable",              "Zablokuj klawisze");
		resources.put("keylock_enabled",             "Klawisze zablokowane");
		resources.put("keyword",                     "Hasło");
		resources.put("language",                    "Język");
		resources.put("lang_BG",                     "Bułgarski");
		resources.put("lang_BR",                     "Portugalski");
		resources.put("lang_CZ",                     "Czeski");
		resources.put("lang_DE",                     "Niemiecki");
		resources.put("lang_EN",                     "Angielski");
		resources.put("lang_ES",                     "Hiszpa�ski");
		resources.put("lang_HE",                     "Hebrajski");
		resources.put("lang_IT",                     "Włoski");
		resources.put("lang_LT",                     "Litewski");
		resources.put("lang_PL",                     "Polski");
		resources.put("lang_RU",                     "Rosyjski");
		resources.put("lang_SE",                     "Szwedzki");
		resources.put("lang_SR",                     "Serbski");
		resources.put("lang_UA",                     "Ukraiński");
		resources.put("lastname",                    "Nazwisko");
		resources.put("loading",                     "Uruchamianie");
		resources.put("male",                        "Męczyzna");
		resources.put("me",                          "ja");
		resources.put("menu",                        "Menu");
		resources.put("message_notification",        "Wiadomość");
		resources.put("msg_sound_file_name",         "Dźwięk wiadomości");
		resources.put("message",                     "Wiadomość");
		resources.put("message_from",                "Wiadomość od");
		resources.put("minimize",                    "Minimalizuj");
		resources.put("name",                        "Nazwa");
		resources.put("nick",                        "Nick");
		resources.put("no",                          "Nie");
		resources.put("not",                         "nie");
		resources.put("no_results",                  "Brak wyników");
		resources.put("no_not_empty_gr",             "Usuwanie nie pustych grup nie jest jeszcze obsługiwane");
		resources.put("not_implemented",             "Funkcja jeszce nie zaimplementowana.");
		resources.put("noreason",                    "Nie podano powodu.");
		resources.put("notice",                      "Powiadomienie");
		resources.put("nr",                          "Nr");
		resources.put("once_a_session",              "Raz na sesję");
		resources.put("onl_notification",            "Powiadom o dostępności użytkownika");
		resources.put("onl_sound_file_name",         "Dźwięśk dostępności użytkownika");
		resources.put("only_online",                 "Pokaż tylko zalogowanych użytkowników");
		resources.put("options",                     "Opcje");
		resources.put("options_account",             "Konto");
		resources.put("options_cost",                "Koszt");
		resources.put("options_effect",              "Aby zmiany zostały uwzględnione musisz ponownie się zalogować!");
		resources.put("options_interface",           "Interfejs");
		resources.put("options_network",             "Sieć");
		resources.put("options_other",               "Inne");
		resources.put("options_signaling",           "Sygnalizacja");
		resources.put("password",                    "Hasło");
		resources.put("plength",                     "Ilość danych objętych opłatą w kB");
		resources.put("plsauthme",                   "Cześć! Proszę autoryzować moją proźbę o dodanie Cię do mojej listy kontaktów.");
		resources.put("prev",                        "Poprzedni");
		resources.put("reason",                      "Powód");
		resources.put("remove",                      "Usuń z listy");
		resources.put("remove_group",                "Usuń grupę");
		resources.put("remove_user",                 "Usuń użytkownika");
		resources.put("rename",                      "Zmień");
		resources.put("requauth",                    "Żądaj autoryzacji");
		resources.put("requ",                        "Wymagane");
		resources.put("requno",                      "Nie wymagane");
		resources.put("res",                         "Rozwiązanie");
		resources.put("results",                     "Rezultat");
		resources.put("search_user",                 "Szukaj użytkownika");
		resources.put("send_img",                    "Wyślij obrazek");
		resources.put("send_message",                "Nowa wiadfomość");
		resources.put("send_url",                    "Nowy URL");
		resources.put("server",                      "Serwer logowania");
		resources.put("server_host",                 "Nazwa hosta");
		resources.put("server_port",                 "Port");
		resources.put("session",                     "Sesja");
		resources.put("set_status",                  "Ustaw status");
		resources.put("shadow_con",                  "Łączenie w tle");
		resources.put("show_user_groups",            "Pokaż grupę użytkowników");
		resources.put("since",                       "Czas");
		resources.put("size",                        "Rozmiar");
		resources.put("sound",                       "Plik dźwiękowy");
		resources.put("sound_file_name",             "Nazwa pliku dźwiękowego");
		resources.put("sort_by",                     "Sortuj listę kontaktów");
		resources.put("sort_by_name",                "Według nazwy");
		resources.put("sort_by_status",              "Według statusu");
		resources.put("speed",                       "Szybkość");
		resources.put("status",                      "Status");
		resources.put("status_away",                 "Oddalony");
		resources.put("status_chat",                 "Porozmawiajmy");
		resources.put("status_dnd",                  "Nie przeszkadzać");
		resources.put("status_invisible",            "Niewidoczny");
		resources.put("status_na",                   "Niedostępny");
		resources.put("status_occupied",             "Zajęty");
		resources.put("status_offline",              "Wylogowany");
		resources.put("status_online",               "Zalogowany");
		resources.put("successful",                  "Powodzenie");
		resources.put("sysnotice",                   "Wiadomosś systemowa");
		resources.put("traffic",                     "Ruch");
		resources.put("uin",                         "Numer");
		resources.put("url",                         "URL");
		resources.put("use_history",                 "Zapisana historia");
		resources.put("user_add",                    "Dodaj użytkownika");
		resources.put("user_search",                 "Szukaj użytkownika");
		resources.put("vibration",                   "Wibracja");
		resources.put("viewfinder",                  "Podgląd");
		resources.put("volume",                      "Głośność");
		resources.put("wait",                        "Proszę czekać ...");
		resources.put("warning",                     "Uwaga");
		resources.put("wantsyourauth",               " żąda Twojej autoryzacji. Powód: ");
		resources.put("was",                         "być");
		resources.put("whichgroup",                  "Która grupa?");
		resources.put("white_on_black",              "Biało czarny");
		resources.put("white_on_blue",               "Biało niebieski");
		resources.put("yes",                         "Tak");
		resources.put("youwereadded",                "Zostałeś dodany przez użytkownika o numerze: ");

		//#sijapp cond.if modules_HISTORY is "true" #
		resources.put("text_to_find",                "Tekst");
		resources.put("find_backwards",              "Wcześniej");
		resources.put("find_case_sensitiv",          "Nie rozróniaj dużych i małych liter");
		resources.put("history_info",                "Informacje o pamięci");
		resources.put("hist_cur",                    "Aktualny numer wiadomości");
		resources.put("hist_rc",                     "Ilość wszystkich wiadomości");
		resources.put("hist_size",                   "Zajęta pamięć (kB)");
		resources.put("hist_avail",                  "Całkowita pamięć (kB)");
		resources.put("history",                     "Zachowana historia");
		resources.put("not_found",                   "nie znaleziono");
		//#sijapp cond.end#

		// Generic errors
		resources.put("error_100",                   "Nieznany b��d (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "Wielokrotne logowanie na ten sam numer (#110.EXT)");
		resources.put("error_111",                   "Złe hasło (#111.EXT)");
		resources.put("error_112",                   "Nieistniejący numer (#112.EXT)");
		resources.put("error_113",                   "Za dużo połączeń z tego samego adresu IP (#113.EXT)");
		resources.put("error_114",                   "Przekroczona kwota (#114.EXT)");
		resources.put("error_115",                   "Lista kontaktów nie może być zprasowana (#115.EXT)");
		resources.put("error_116",                   "Wiadomości Offline nie moga być zrasowane (#116.EXT)");
		resources.put("error_117",                   "Pusty numer i/lub hasło (#117.EXT)");
		resources.put("error_118",                   "Brak odpowiedzi z serwera (#118.EXT)");

		// Network communication specific exceptions
		// Connection to server
		resources.put("error_120",                   "Błąd Wejścia/Wyjścia (#120.EXT)");
		resources.put("error_121",                   "Żadane połączene TCP nie może zostać zrealizowane (#121.EXT)");
		resources.put("error_122",                   "Określony serwer i/lub port jest błędny (#122.EXT)");
		resources.put("error_123",                   "Nie można nawiązać połączenia (#123.EXT)");
		resources.put("error_124",                   "Dane wejściowe nie mogą być zsynchronizowane (#124.EXT)");

		// Peer connection
		resources.put("error_125",                   "Błąd Wejścia/Wyjścia (#125.EXT)");
		resources.put("error_126",                   "Żadane połączene TCP nie może zostać zrealizowane (#126.EXT)");
		resources.put("error_127",                   "Określony serwer i/lub port jest błędny (#127.EXT)");
		resources.put("error_128",                   "Nie można nawiązać połączenia (#128.EXT)");
		resources.put("error_129",                   "Dane wejściowe nie mogą być zsynchronizowane (#129.EXT)");

		// Parsing specific errors
		resources.put("error_130",                   "Nagłówek FLAP jnie moze być zprasowany (#130.EXT)");
		resources.put("error_131",                   "Nieznany kanał (#131.EXT)");
		resources.put("error_132",                   "Pakiet podłączenia kanału nie może być zprasowany (#132.EXT)");
		resources.put("error_133",                   "Nagłówek SNAC nie moze być zprasowany (#133.EXT)");
		resources.put("error_134",                   "Pakiet błędu kanału nie może być zprasowany (#134.EXT)");
		resources.put("error_135",                   "Pakiet rozłączenia kana�u nie może być zprasowane (#135.EXT)");
		resources.put("error_136",                   "Pakiet Ping kanału nie może być zprasowany (#136.EXT)");
		resources.put("error_137",                   "Stary nagłówek protokołu ICQ nie może być zprasowany (#137.EXT)");

		// Action errors
		resources.put("error_140",                   "Żadana akcja nie może być teraz zakolejkowana do wykonania (#140.EXT)");

		// Specific action errors
		resources.put("error_150",                   "Odbierana wiadomość może być niezrozumiała (#150.EXT)");
		resources.put("error_151",                   "Odbierana wiadomość typu 1 może być niezrozumiała (#151.EXT)");
		resources.put("error_152",                   "Odbierana wiadomość typu 2 może być niezrozumiała (#152.EXT)");
		resources.put("error_153",                   "Odbierana wiadomość typu 4 może być niezrozumiała (#153.EXT)");
		resources.put("error_154",                   "Błąd podczas aktualizacji listy kontaktów (#154.EXT)");
		resources.put("error_155",                   "Objekt jest dostępny na twojej liście (#155.EXT)");
		resources.put("error_156",                   "Błąd podczas dodawania. Spróbuj jeszcze raz (#156.EXT)");
		resources.put("error_157",                   "Więcej elementów tego typu jest niedostępnych (#157.EXT)");
		resources.put("error_158",                   "Próbujesz dodać kontakt ICQ do listy AIM (#158.EXT)");
		resources.put("error_159",                   "Serwer nie odpowiada na żądanie szukania. Spróbuj ponownie (#159.EXT)");
		resources.put("error_160",                   "Błąd podszas szukania (#160.EXT)");
		resources.put("error_161",                   "Nie znaleziono grupy. Proszę dodać grupę (#161.EXT)");

		// Other errors
		resources.put("error_170",                   "Prawdpodobnie brak wystarczającej ilości pamięci (#170.EXT)");
		resources.put("error_171",                   "Nie można pobrać informacji o nagłówku (#171.EXT)");

		// Camera errors
		resources.put("error_180",                   "Błąd tworzenia VideoControl (#180.EXT)");
		resources.put("error_181",                   "Błąd inicjalizacji podglądu (#181.EXT)");
		resources.put("error_182",                   "Błąd uruchamiania podglądu (#182.EXT)");
		resources.put("error_183",                   "Błąd podczas robienia zdjęcia (#183.EXT)");
		resources.put("error_185",                   "Obieranie obrazków nieobsługiwane (#185.EXT)");

		// File transfer errors
		resources.put("error_190",                   "Przesyłanie pliku < ICQv8 klient nieobsługiwany (#190.EXT)");
		resources.put("error_191",                   "Błąd odczytu pliku. Plik prawdopodobnie nieobsługiwany (#191.EXT)");
		resources.put("error_192",                   "Błąd odczytu pliku. Zła ścieżka lub plik nieobsługiwany (#192.EXT)");
		resources.put("error_193",                   "Błąd dostępu do systemu plików podczas przeglądanjia. Błąd ochrony (#193.EXT)");

	}
}

// #sijapp cond.end #