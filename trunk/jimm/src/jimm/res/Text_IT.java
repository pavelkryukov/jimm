/********************************************************************************
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
 File: src/jimm/res/Text_IT.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


// #sijapp cond.if lang_IT is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_IT extends ResourceBundle
{


	public Text_IT()
	{

		// Labels
		resources.put(".",                           ".");
		resources.put("about",                       "Informazioni");
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nClient ICQ per Java J2ME\nVersione ###VERSION###\nVedi http://jimm.sourceforge.net/");
		resources.put("account",                     "Account");
		resources.put("add_user",                    "Aggiungi Utente");
		resources.put("async",                       "Async");
		resources.put("back",                        "Indietro");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("beep",                        "Beep");
		// #sijapp cond.end #
		resources.put("byte",                        "Byte");
		resources.put("cancel",                      "Annulla");
		resources.put("close",                       "Chiudi");
		resources.put("conn_type",                   "Tipo di connessione");
		resources.put("connect",                     "Connettiti");
		resources.put("connecting",                  "Connessione in corso");
		resources.put("contact_list",                "Lista");
		resources.put("cp1251",                      "Usare hack CP1252?");
		resources.put("cpd",                         "Costo giornaliero");
		resources.put("cpp",                         "Costo per pacchetto");
		resources.put("currency",                    "Valuta");
		resources.put("delete_chat",                 "Cancella chat");
		resources.put("deny",						 "Nega Autorizzazione");
		resources.put("denyedby",					 "La tua richiesta di autorizzazione è stata rifiutata da: ");
		resources.put("user_menu",                   "Menu Utente");
		resources.put("disconnect",                  "Disconnettiti");
		resources.put("disconnecting",               "Disconnessione in corso");
		resources.put("display_advertisement",       "Aggiungere promo Jimm?");
		resources.put("display_date",                "Visualizzare data nella schermata iniziale?");
		resources.put("email",                       "Email");
		resources.put("error",                       "Errore");
		resources.put("exit",                        "Esci");
		resources.put("grant",						 "Concedi Autorizzazione");
		resources.put("grantedby",					 "La tua richiesta di autorizzazione è stata accettata da:");
		resources.put("hide_offline",                "Nascondere contatti non connessi");
		resources.put("info",                        "Info");
		resources.put("kb",                          "kB");
		resources.put("keep_chat",                   "Mantenere cronologia delle chat?");
		resources.put("keep_conn_alive",             "Mantenere attiva la connessione?");
		resources.put("keylock_enable",              "Attiva blocco tasti");
		resources.put("keylock_enabled",             "Blocco tasti attivato");
		resources.put("language",                    "Lingua");
		resources.put("lang_BR",                     "Portugues (Brazil)");
		resources.put("lang_CZ",                     "Czech");
		resources.put("lang_DE",                     "Deutsch");
		resources.put("lang_EN",                     "English");
		resources.put("lang_ES",                     "Espanol");
		resources.put("lang_IT",                     "Italiano");		
		resources.put("lang_LT",                     "Lithuanian");
		resources.put("lang_RU",                     "Russian");
		resources.put("lang_SE",                     "Svensk");
		resources.put("loading",                     "Caricamento");
		resources.put("me",                          "io");
		resources.put("menu",                        "Menu");
		resources.put("message",                     "Messaggio");
		resources.put("message_from",                "Messaggio da");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("message_notification",        "Notifica messaggio");
		// #sijapp cond.end #
		resources.put("name",                        "Nome");
		resources.put("nick",                        "Nick");
		resources.put("no",                          "No");
		resources.put("not_implemented",             "Funzione non ancora implementata.");
		resources.put("noreason",             		 "Motivo omesso.");
		resources.put("notice",                      "Avviso");
		resources.put("ok",                          "OK");
		resources.put("once_a_session",              "Una volta a sessione");
		resources.put("options",                     "Opzioni");
		resources.put("options_account",             "Account");
		resources.put("options_cost",                "Costi");
		resources.put("options_effect",              "Può essere necessario riconnettersi per applicare alcune modifiche!");
		resources.put("options_interface",           "Interfaccia");
		resources.put("options_network",             "Rete");
		resources.put("options_other",               "Altro");
		resources.put("password",                    "Password");
		resources.put("plength",                     "Dimensione del pacchetto addebitato in kB");
		resources.put("plsauthme",                   "Ciao! Per favore autorizzami per aggiungerti alla mia Lista Contatti.");
		resources.put("reason",						 "Motivo");
		resources.put("remove",                      "Rimuovi dalla Lista");
		resources.put("reply",                       "Rispondi");
		resources.put("requauth",                    "Chiedi autorizzazione");
		resources.put("reset",                       "Azzera");
		resources.put("save",                        "Salva");
		resources.put("search_user",                 "Cerca Utente");
		resources.put("send",                        "Invia");
		resources.put("send_message",                "Nuovo Messaggio");
		resources.put("send_url",                    "Nuova URL");
		resources.put("server",                      "Server di accesso");
		resources.put("server_host",                 "Nome Host");
		resources.put("server_port",                 "Porta");
		resources.put("session",                     "Sessione");
		resources.put("set_status",                  "Imposta stato");
		resources.put("since",                       "Dal");
		resources.put("sound",                       "Suono di ICQ");
		resources.put("sound_file_name",             "Nome del file audio");
		resources.put("sort_by",                     "Ordinare lista contatti");
		resources.put("sort_by_name",                "Per nome");
		resources.put("sort_by_status",              "Per stato");
		resources.put("status_away",                 "Assente");
		resources.put("status_chat",                 "Disposto a Chattare");
		resources.put("status_dnd",                  "Non Disturbare");
		resources.put("status_invisible",            "Invisibile");
		resources.put("status_na",                   "Non Disponibile");
		resources.put("status_occupied",             "Occupato");
		resources.put("status_offline",              "Non Connesso");
		resources.put("status_online",               "Connesso");
		resources.put("sysnotice",					 "Avviso di Sistema");
		resources.put("traffic",                     "Traffico");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("user_add",                    "Aggiungi utente");
		resources.put("user_search",                 "Cerca utente");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("vibration",                   "Vibrazione");
		// #sijapp cond.end #
		resources.put("wait",                        "Attendere prego...");
		resources.put("warning",                     "Attenzione");
		resources.put("wantsyourauth",				 " vuole la tua Autorizzazione. Motivo: ");
		resources.put("whichgroup",				 	 "Quale gruppo?");
		resources.put("yes",                         "Sì");
		resources.put("youwereadded",				 "Sei stato aggiunto da UIN: ");

		// Generic errors
		resources.put("error_100",                   "Errore sconosciuto (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "Accessi multipli sullo stesso UIN (#110.EXT)");
		resources.put("error_111",                   "Password errata (#111.EXT)");
		resources.put("error_112",                   "UIN inesistente (#112.EXT)");
		resources.put("error_113",                   "Troppi client dallo stesso IP (#113.EXT)");
		resources.put("error_114",                   "Frequenza massima superata (#114.EXT)");
		resources.put("error_115",                   "Impossibile processare la Lista Contatti (#115.EXT)");
		resources.put("error_116",                   "Impossibile processare i messaggi Offline (#116.EXT)");
		resources.put("error_117",                   "UIN e/o password vuoti (#117.EXT)");
		resources.put("error_118",                   "Nessuna risposta dal server (#118.EXT)");

		// Network communication specific exceptions
		resources.put("error_120",                   "Errore di I/O (#120.EXT)");
		resources.put("error_121",                   "Impossibile effettuare la connessione TCP richiesta (#121.EXT)");
		resources.put("error_122",                   "Indirizzo e/o porta del server specificati non validi (#122.EXT)");
		resources.put("error_123",                   "La connessione non è stata stabilita (#123.EXT)");
		resources.put("error_124",                   "Il flusso in ingresso è fuori sincronia (#124.EXT)");

		// Parsing specific errors
		resources.put("error_130",                   "Impossibile processare l'header FLAP (#130.EXT)");
		resources.put("error_131",                   "Canale sconosciuto (#131.EXT)");
		resources.put("error_132",                   "Impossibile processare il pacchetto del canale di connessione (#132.EXT)");
		resources.put("error_133",                   "Impossibile processare l'header SNAC (#133.EXT)");
		resources.put("error_134",                   "Impossibile processare il pacchetto del canale di errore (#134.EXT)");
		resources.put("error_135",                   "Impossibile processare il pacchetto del canale di disconnessione (#135.EXT)");
		resources.put("error_136",                   "Impossibile processare il pacchetto del canale di ping (#136.EXT)");
		resources.put("error_137",                   "Impossibile processare l'header del vecchio protocollo di ICQ (#137.EXT)");

		// Action errors
		resources.put("error_140",                   "L'azione richiesta non può essere accodata in questo momento (#140.EXT)");

		// Specific action errors
		resources.put("error_150",                   "Impossibile interpretare il messaggio ricevuto (#150.EXT)");
		resources.put("error_151",                   "Impossibile interpretare il messaggio di tipo 1 ricevuto (#151.EXT)");
		resources.put("error_152",                   "Impossibile interpretare il messaggio di tipo 2 ricevuto (#152.EXT)");
		resources.put("error_153",                   "Impossibile interpretare il messaggio di tipo 4 ricevuto (#153.EXT)");
		resources.put("error_154",                   "Aggiornamento della Lista Contatti fallito (#154.EXT)");
		resources.put("error_155",                   "L'oggetto è già nella tua Lista sul server (#155.EXT)");
		resources.put("error_156",                   "Errore durante l'aggiunta (#156.EXT)");
		resources.put("error_157",                   "Raggiunto limite massimo di elementi per questo tipo (#157.EXT)");
		resources.put("error_158",                   "Hai provato ad aggiunger un contatto ICQ a una lista AIM (#158.EXT)");

		// Other errors
		resources.put("error_160",                   "Probabile insufficienza di memoria heap disponibile (#160.EXT)");
		resources.put("error_161",                   "Impossibile leggere le metainfo (#161.EXT)");

	}


}


// #sijapp cond.end #
