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
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Paweł Głogowski
 *******************************************************************************/


// #sijapp cond.if lang_PL is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_PL extends ResourceBundle
{


	public Text_PL()
	{

		// Labels
		resources.put(".",							".");
		resources.put("about",						"O programie");
		resources.put("about_info",					"Jimm - Mobile Messaging\n\nKlient ICQ dla platformy J2ME\nWersja 0.3.1\nInformacje: http://jimm.sourceforge.net/");
		resources.put("account",					"Konto");
		resources.put("add_user",					"Dodaj użytkownika");
		resources.put("add_to_list",				"Dodaj do listy");
		resources.put("age",						"Wiek");
		resources.put("async",							"Async");
		resources.put("auth",							"Auth");
		resources.put("back",						"Wstecz");
		resources.put("beep",						"Beep");
		resources.put("byte",						"B");
		resources.put("cancel",						"Anuluj");
		resources.put("city",						"Miasto");
		resources.put("close",						"Zamknij");
		resources.put("conn_type",					"Typ połączenia");
		resources.put("con_wait",                   "Czas między połączeniami (sec.)");
		resources.put("connect",					"Połącz");
		resources.put("connecting",					"Łączenie");
		resources.put("contact_list",				"Lista");
		resources.put("cp1251",						"Użyć CP1251 hack?");
		resources.put("cpd",						"Cena za dzień");
		resources.put("cpp",						"Cena za pakiet");
		resources.put("currency",					"Waluta");
		resources.put("del_group",                  "Usuń grupę");
		resources.put("delete_chat",				"Skasuj rozmowę");
		resources.put("deny",						"Autoryzacja odrzucona");
		resources.put("denyedby",					"Żądanie autoryzacji zostało odrzucone przez: ");
		resources.put("description",				"Opis");
		resources.put("name_desc",					"Nazwa pliku i opis");
		resources.put("user_menu",					"Menu użytkownika");
		resources.put("disconnect",					"Rozłącz");
		resources.put("disconnecting",				"Rozłączanie");
		resources.put("display_advertisement",		"Dołączyć wskazówki?");
		resources.put("display_date",				"Wyświetlać datę na ekranie tytułowym?");
		resources.put("email",						"E-mail");
		resources.put("error",						"Błąd");
		resources.put("exit",						"Wyjdź");
		resources.put("female",						"Kobieta");
		resources.put("filetransfer",				"Transfer pliku");
		resources.put("filepath",					"Ścieżka do pliku");
		resources.put("firstname",					"Imię");
		resources.put("ft_name",					"Wyślij plik");
		resources.put("ft_cam",						"Wyślij obraz z kamery");
		resources.put("gender",						"Płeć");
		resources.put("grant",						"Autoryzacja przyjęta");
		resources.put("grantedby",					"Żądanie autoryzacji zostało przyjęte przez:");
		resources.put("hide_offline",				"Ukryj wylogowanych użytkowników");
		resources.put("info",						"Informacje");
		resources.put("init_ft",					"Inicjowanie");
		resources.put("kb",							"kB");
		resources.put("keep_chat",					"Zachować rozmowę?");
		resources.put("keep_conn_alive",			"Utrzymać aktywne połączenie?");
		resources.put("keylock_enable",				"Blokowanie klawiszy");
		resources.put("keylock_enabled",			"Włączona blokada klawiatury");
		resources.put("keyword",					"Hasło");
		resources.put("language",					"Język");
		resources.put("lang_BR",					"Portugalski");
		resources.put("lang_CZ",					"Czeski");
		resources.put("lang_DE",					"Nemiecki");
		resources.put("lang_EN",					"Angielski");
		resources.put("lang_ES",					"Hiszpański");
		resources.put("lang_IT",					"Włoski");
		resources.put("lang_LT",					"Litewski");
		resources.put("lang_RU",					"Rosyjski");
		resources.put("lang_SE",					"Szwedzki");
		resources.put("lang_SR",					"Serbski");
		resources.put("lang_HE",						"עברית");
		resources.put("lang_BG", 					"Bułgarski");
		resources.put("lang_PL",					"Polski");
		resources.put("lastname",					"Nazwisko");
		resources.put("loading",					"Uruchamianie");
		resources.put("male",						"Mężczyzna");
		resources.put("me",							"Ja");
		resources.put("menu",						"Menu");
		resources.put("msg_sound_file_name",		"Dzwonek przychodzącej wiadomości");
		resources.put("message_notification",		"Powiadomienie o nowej wiadomości");
		resources.put("message",					"Wiadomość");
		resources.put("message_from",				"Wiadomość od");
		resources.put("name",						"Nazwa");
		resources.put("next",						"Dalej");
		resources.put("nick",						"Nick");
		resources.put("no",							"Nie");
		resources.put("no_results",					"Brak wyników");
		resources.put("no_not_empty_gr",			"Usuwanie nie pustych grum nie jest jeszcze obsługiwane");
		resources.put("not_implemented",			"Funkcja jeszcze nie zaimplementowana.");
		resources.put("noreason",					"Nie podano powodu.");
		resources.put("notice",						"Powiadomienie");
		resources.put("nr",							"Nr");
		resources.put("ok",							"OK");
		resources.put("once_a_session",				"Raz na sesję");
		resources.put("onl_notification",			"Powiadom o dostępności użytkownika");
		resources.put("onl_sound_file_name",		"Dzwonek dostępności użytkownika");
		resources.put("only_online",				"Pokaż tylko zalogowanych użytkowników");
		resources.put("options",					"Opcje");
		resources.put("options_account",			"Konto");
		resources.put("options_cost",				"Koszty");
		resources.put("options_effect",				"Aby zmiany zostały uwzględnione, musisz się ponownie zalogować!");
		resources.put("options_interface",			"Interfejs");
		resources.put("options_network",			"Sieć");
		resources.put("options_other",				"Inne");
		resources.put("options_signaling",			"Sygnalizacja");
		resources.put("password",					"Hasło");
		resources.put("plength",					"Ilość danych objętych opłatą w kB");
		resources.put("plsauthme",					"Cześć! Proszę autoryzować moją prośbę o dodanie Cię do mojej listy kontaktów.");
		resources.put("prev",						"Poprzedni");
		resources.put("reason",						"Powód");
		resources.put("remove",						"Usuń z listy");
		resources.put("rename",                     "Zmień");
		resources.put("reply",						"Odpowiedz");
		resources.put("requauth",					"Wymagaj autoryzacji");
		resources.put("requ",						"Wymagane");
		resources.put("requno",						"Nie wymagane");
		resources.put("reset",						"Reset");
		resources.put("res",						"Rozwiązanie");
		resources.put("results",					"Rezultat");
		resources.put("save",						"Zapisz");
		resources.put("search_user",				"Szukaj użytkownika");
		resources.put("send",						"Wyślij");
		resources.put("send_message",				"Nowa wiadomosc");
		resources.put("send_img",					"Wyślij obrazek");
		resources.put("send_url",					"Nowy URL");
		resources.put("server",						"Login server");
		resources.put("server_host",				"Nazwa hosta");
		resources.put("server_port",				"Port");
		resources.put("session",					"Sesja");
		resources.put("set_status",					"Ustaw status");
		resources.put("since",						"Od");
		resources.put("sound",						"Plik dźwiękowy");
		resources.put("sort_by",					"Sortuj listę kontaktów");
		resources.put("sort_by_name",				"Według nazwy");
		resources.put("sort_by_status",				"Według statusu");
		resources.put("status",						"Status");
		resources.put("status_away",				"Oddalony");
		resources.put("status_chat",				"Porozmawiajmy");
		resources.put("status_dnd",					"Nie przeszkadzać");
		resources.put("status_invisible",			"Niewidoczny");
		resources.put("status_na",					"Niedostępny");
		resources.put("status_occupied",			"Zajęty");
		resources.put("status_offline",				"Wylogowany");
		resources.put("status_online",				"Zalogowany");
		resources.put("sysnotice",					"Powiadomienie systemowe");
		resources.put("traffic",					"Transfer");
		resources.put("uin",						"UIN");
		resources.put("url",						"URL");
		resources.put("user_add",					"Dodaj użytkownika");
		resources.put("user_search",				"Szukaj użytkownika");
		resources.put("vibration",					"Wibracja");
		resources.put("viewfinder",					"Podgląd");
		resources.put("volume",						"Głośność");
		resources.put("wait",						"Proszę czekać ...");
		resources.put("warning",					"Uwaga");
		resources.put("wantsyourauth",				" żąda twojej autoryzacji. Powód: ");
		resources.put("whichgroup",					"Która grupa?");
		resources.put("yes",						"Tak");
		resources.put("youwereadded",				"Zostałeś dodany przez UIN: ");
		resources.put("chat_small_font",			"Małe liery podczas rozmowy");
		resources.put("select",						"Wybierz");
		resources.put("attention",                  "Uwaga!");
		resources.put("have_unread_mess",           "Masz nieprzeczytane wiadomości. Napewno wyjść?");
		
		// User management ********
		resources.put("group_is_not_empty",			"Zaznacz gupę jeżeli nie jest pusta!\nPrzenieś wszystkich użytkowników do innej grupy!");
		resources.put("add_group",					"Dodaj grupe");
		resources.put("group_name",					"Nazwa grupy");
		resources.put("exec",						"Wykonaj");
		resources.put("remove_user",				"Usuń użytkownika");
		resources.put("remove_group",				"Usuń grupe");
		resources.put("show_user_groups",			"Pokaż grupy");

		// Generic errors
		resources.put("error_100",					"Nieznany błąd (#100.EXT)");

		// Login specific errors
		resources.put("error_110",					"Wielokrotne logowanie na ten sam UIN (#110.EXT)");
		resources.put("error_111",					"Złe hasło (#111.EXT)");
		resources.put("error_112",					"Nieistniejący UIN (#112.EXT)");
		resources.put("error_113",					"Za dużo połączeń z tego samego IP (#113.EXT)");
		resources.put("error_114",					"Przekroczona kwota (#114.EXT)");
		resources.put("error_115",					"Lista kontaktów nie może być zprasowana (#115.EXT)");
		resources.put("error_116",					"Wiadomości Offline nie moga być zrasowane (#116.EXT)");
		resources.put("error_117",					"Brak UIN-u i/lub hasła (#117.EXT)");
		resources.put("error_118",					"Brak odpowiedzi z serwera (#118.EXT)");

		// Network communication specific exceptions
		// Connection to server
		resources.put("error_120",					"Błąd Wejścia/Wyjścia (#120.EXT)");
		resources.put("error_121",					"Żadane połączene TCP nie może zostać zrealizowane (#121.EXT)");
		resources.put("error_122",					"Określony serwer i/lub port jest błędny (#122.EXT)");
		resources.put("error_123",					"Nie można nawiązać połączenia (#123.EXT)");
		resources.put("error_124",					"Dane wejściowe nie mogą być zsynchronizowane (#124.EXT)");

		// Peer connection
		resources.put("error_125",					"Błąd Wejścia/Wyjścia (#125.EXT)");
		resources.put("error_126",					"Żadane połączene TCP nie może zostać zrealizowane (#126.EXT)");
		resources.put("error_127",					"Podana nazwa serwera i/lub port jest błędna (#127.EXT)");
		resources.put("error_128",					"Nie można nawiązać połączenia (#128.EXT)");
		resources.put("error_129",					"Dane wejściowe nie mogą być zsynchronizowane (#129.EXT)");

		// Parsing specific errors
		resources.put("error_130",					"Nagłówek FLAP jnie moze być zprasowany (#130.EXT)");
		resources.put("error_131",					"Nieznany kanał (#131.EXT)");
		resources.put("error_132",					"Pakiet podłączenia kanału nie może być zprasowant (#132.EXT)");
		resources.put("error_133",					"Nagłówek SNAC jnie moze być zprasowany (#133.EXT)");
		resources.put("error_134",					"Pakiet błędu kanału nie może być zprasowany (#134.EXT)");
		resources.put("error_135",					"Pakiet rozłączenia kanału nie może być zprasowane (#135.EXT)");
		resources.put("error_136",					"Pakiet Ping kanału nie może być zprasowany (#136.EXT)");
		resources.put("error_137",					"Stary nagłówek protokołu ICQ nie może być zprasowany (#137.EXT)");

		// Action errors
		resources.put("error_140",					"Żadana akcja nie może być teraz zakolejkowana do wykonania (#140.EXT)");

		// Specific action errors
		resources.put("error_150",					"Odbierana wiadomość może być niezrozumiała (#150.EXT)");
		resources.put("error_151",					"Odbierana wiadomość typu 1 może być niezrozumiała (#151.EXT)");
		resources.put("error_152",					"Odbierana wiadomość typu 2 może być niezrozumiała (#152.EXT)");
		resources.put("error_153",					"Odbierana wiadomość typu 4 może być niezrozumiała (#153.EXT)");
		resources.put("error_154",					"Błąd podczas aktualizacji listy kontaktów (#154.EXT)");
		resources.put("error_155",					"Objekt jest dostępny na twojej liście (#155.EXT)");
		resources.put("error_156",					"Błąd podczas dodawania. Spróbuj jeszcze raz (#156.EXT)");
		resources.put("error_157",					"Więcej elementów tego typu jest niedostępnych (#157.EXT)");
		resources.put("error_158",					"Próbujesz oddać kontakt ICQ do listy AIM (#158.EXT)");
		resources.put("error_159",					"Serwer nie odpowiada na żądanie szukania. Spróbuj ponownie (#159.EXT)");
		resources.put("error_160",					"Błąd podszas szukania (#160.EXT)");
		resources.put("error_161",                  "Nie znaleziono grupy. Proszę dodać grupę (#161.EXT)");

		// Other errors
		resources.put("error_170",					"Prawdpodobnie brak wystarczającej ilości pamięci (#170.EXT)");
		resources.put("error_171",					"Nie można pobrać informacji o nagłówku (#171.EXT)");

		// Camera errors
		resources.put("error_180",					"Błąd tworzenia VideoControl (#180.EXT)");
		resources.put("error_181",					"Błąd inicjalizacji podglądu (#181.EXT)");
		resources.put("error_182",					"Błąd uruchamiania podglądu(#182.EXT)");
		resources.put("error_183",					"Błąd podczas robienia zdjęcia (#183.EXT)");
		resources.put("error_185",					"Pobieranie obrazków nieobsługiwane (#185.EXT)");

		// File transfer errors
		resources.put("error_190",					"Przesyłanie pliku < ICQv8 klient nieobsługiwany (#190.EXT)");
		resources.put("error_191",					"Błąd odczytu pliku. Plik prawdopodobnie nieobsługiwany (#191.EXT)");
		resources.put("error_192",					"Błąd odczytu pliku. Zła ścieżka lub plik nieobsługiwany (#192.EXT)");

	}


}


// #sijapp cond.end #
