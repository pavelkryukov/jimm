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
 File: src/jimm/res/Text_IT.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Mario B. Napoli
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
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nClient ICQ per Java J2ME\nVersione 0.3.1\nVedi http://www.jimm.org/");
		resources.put("account",                     "Account");
		resources.put("add_user",                    "Aggiungi Utente");
		resources.put("add_to_list",                 "Aggiungi alla lista");
		resources.put("age",                         "Eta'");
		resources.put("async",                       "Async");
		resources.put("auth",                        "Autorizzazione");
		resources.put("back",                        "Indietro");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("beep",                        "Beep");
		// #sijapp cond.end #
		resources.put("byte",                        "Byte");
		resources.put("cancel",                      "Annulla");
		resources.put("city",                        "Citta'");
		resources.put("close",                       "Chiudi");
		resources.put("conn_type",                   "Tipo di connessione");
		resources.put("con_wait",                    "Intervallo tra le connessioni (sec)");
		resources.put("connect",                     "Connettiti");
		resources.put("connecting",                  "Connessione in corso");
		resources.put("contact_list",                "Lista");
		resources.put("cp1251",                      "Usare hack CP1251?");
		resources.put("cpd",                         "Costo giornaliero");
		resources.put("cpp",                         "Costo per pacchetto");
		resources.put("currency",                    "Valuta");
		resources.put("delete_chat",                 "Cancella");
		resources.put("del_group",                   "Elimina gruppo");
		resources.put("deny",						 "Nega Autorizzazione");
		resources.put("denyedby",					 "La tua richiesta di autorizzazione è stata rifiutata da: ");
        resources.put("description",                 "Descrizione"); 
        resources.put("name_desc",                   "Nome File e descrizione"); 
		resources.put("user_menu",                   "Menu Utente");
		resources.put("disconnect",                  "Disconnettiti");
		resources.put("disconnecting",               "Disconnessione in corso");
		resources.put("display_advertisement",       "Aggiungere promo Jimm?");
		resources.put("display_date",                "Visualizzare data nella schermata iniziale?");
		resources.put("email",                       "Email");
		resources.put("error",                       "Errore");
		resources.put("exit",                        "Esci");
		resources.put("female",                      "F");
        resources.put("filetransfer",                "Trasferimento File"); 
        resources.put("filepath",                    "Percorso File"); 
		resources.put("firstname",                   "Nome");
        resources.put("ft_name",                     "Invia file"); 
        resources.put("ft_cam",                      "Invia immagine Camera"); 
		resources.put("gender",                      "Sesso");
		resources.put("grant",						 "Concedi Autorizzazione");
		resources.put("grantedby",					 "La tua richiesta di autorizzazione è stata accettata da:");
		resources.put("hide_offline",                "Nascondere contatti non connessi");
		resources.put("info",                        "Info");
        resources.put("init_ft",                     "Inizializzazione...");
		resources.put("kb",                          "kB");
		resources.put("keep_chat",                   "Mantenere cronologia delle chat?");
		resources.put("keep_conn_alive",             "Mantenere attiva la connessione?");
		resources.put("keylock_enable",              "Attiva blocco tasti");
		resources.put("keylock_enabled",             "Blocco tasti attivato");
		resources.put("keyword",                     "Parola chiave");
		resources.put("language",                    "Lingua");
		resources.put("lang_BG",                     "Bulgaro");
		resources.put("lang_BR",                     "Portugues (Brazil)");
		resources.put("lang_CZ",                     "Czech");
		resources.put("lang_DE",                     "Deutsch");
		resources.put("lang_EN",                     "English");
		resources.put("lang_ES",                     "Espanol");
		resources.put("lang_IT",                     "Italiano");		
		resources.put("lang_LT",                     "Lithuanian");
		resources.put("lang_RU",                     "Russian");
		resources.put("lang_SE",                     "Svensk");
		resources.put("lang_SR",                     "Српски");
        resources.put("lang_HE", 					 "עברית");
		resources.put("lastname",                    "Cognome");
		resources.put("loading",                     "Caricamento");
		resources.put("male",                        "M");
		resources.put("me",                          "io");
		resources.put("menu",                        "Menu");
		resources.put("msg_sound_file_name",         "File Audio per Messaggio");
		resources.put("message_notification",        "Notifica messaggio");
		resources.put("message",                     "Messaggio");
		resources.put("message_from",                "Messaggio da");
		resources.put("name",                        "Nome");
		resources.put("next",                        "Successivo");
		resources.put("nick",                        "Nick");
		resources.put("no",                          "No");
		resources.put("no_results",                  "Nessun risultato");
		resources.put("no_not_empty_gr",             "Impossibile rimuovere gruppo non vuoto");
		resources.put("not_implemented",             "Funzione non ancora implementata.");
		resources.put("noreason",             		 "Motivo omesso.");
		resources.put("notice",                      "Avviso");
		resources.put("nr",                          "N.");
		resources.put("ok",                          "OK");
		resources.put("once_a_session",              "Una volta a sessione");
		resources.put("onl_notification",            "Notifica contatti in arrivo");
		resources.put("onl_sound_file_name",         "File Audio per Connessione Contatto");
		resources.put("only_online",                 "Mostra solo contatti connessi");
		resources.put("options",                     "Opzioni");
		resources.put("options_account",             "Account");
		resources.put("options_cost",                "Costi");
		resources.put("options_effect",              "Può essere necessario riconnettersi per applicare alcune modifiche!");
		resources.put("options_interface",           "Interfaccia");
		resources.put("options_network",             "Rete");
		resources.put("options_other",               "Altro");
        resources.put("options_signaling",           "Notifiche");
		resources.put("password",                    "Password");
		resources.put("plength",                     "Dimensione del pacchetto addebitato in kB");
		resources.put("plsauthme",                   "Ciao! Per favore autorizzami per aggiungerti alla mia Lista Contatti.");
		resources.put("prev",                        "Precedente");
		resources.put("reason",						 "Motivo");
		resources.put("remove",                      "Rimuovi dalla Lista");
		resources.put("rename",                      "Rinomina");
		resources.put("reply",                       "Rispondi");
		resources.put("requauth",                    "Chiedi autorizzazione");
		resources.put("requ",                        "Richiesto");
		resources.put("requno",                      "Non richiesto");
		resources.put("reset",                       "Azzera");
		resources.put("results",                     "Risultati");
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
		resources.put("sound",                       "File Audio");
		resources.put("sort_by",                     "Ordinare lista contatti");
		resources.put("sort_by_name",                "Per nome");
		resources.put("sort_by_status",              "Per stato");
		resources.put("status",                      "Stato");
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
		resources.put("vibration",                   "Vibrazione");
        resources.put("viewfinder",                  "Anteprima"); 
        resources.put("volume",                      "Volume"); 
		resources.put("wait",                        "Attendere prego...");
		resources.put("warning",                     "Attenzione");
		resources.put("wantsyourauth",				 " vuole la tua Autorizzazione. Motivo: ");
		resources.put("whichgroup",				 	 "Quale gruppo?");
		resources.put("yes",                         "Sì");
		resources.put("youwereadded",				 "Sei stato aggiunto da UIN: ");
        resources.put("chat_small_font",             "Font piccolo nella chat"); 
        resources.put("select",                      "Scegli");
        resources.put("attention",                   "Attenzione!");
  	    resources.put("have_unread_mess",            "Hai messaggi non letti. Uscire comunque?");
    
        // User management ******** 
        resources.put("group_is_not_empty",          "Il gruppo non è vuoto!\nSposta gli utenti in altri gruppi!"); 
        resources.put("add_group",                   "Aggiungi gruppo"); 
        resources.put("group_name",                  "Nome Gruppo"); 
        resources.put("exec",                        "Esegui"); 
        resources.put("remove_user",                 "Rimuovi utente"); 
        resources.put("remove_group",                "Rimuovi gruppo"); 
        resources.put("show_user_groups",            "Mostra gruppi utenti"); 

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
		// Connection to server
		resources.put("error_120",                   "Errore di I/O (#120.EXT)");
		resources.put("error_121",                   "Impossibile effettuare la connessione TCP richiesta (#121.EXT)");
		resources.put("error_122",                   "Indirizzo e/o porta del server specificati non validi (#122.EXT)");
		resources.put("error_123",                   "Impossibile stabilire la connessione (#123.EXT)");
		resources.put("error_124",                   "Il flusso in ingresso è fuori sincronia (#124.EXT)");

       // Peer connection 
       resources.put("error_125",                   "Errore di I/O (#125.EXT)"); 
       resources.put("error_126",                   "Impossibile effettuare la connessione TCP richiesta (#126.EXT)"); 
       resources.put("error_127",                   "Indirizzo e/o porta del server specificati non validi (#127.EXT)"); 
       resources.put("error_128",                   "Impossibile stabilire la connessione (#128.EXT)"); 
       resources.put("error_129",                   "Il flusso in ingresso è fuori sincronia (#129.EXT)"); 

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
		resources.put("error_155",                   "L'oggetto è già nella Lista sul server (#155.EXT)");
		resources.put("error_156",                   "Errore durante l'aggiunta (#156.EXT)");
		resources.put("error_157",                   "Raggiunto limite massimo di elementi per questo tipo (#157.EXT)");
		resources.put("error_158",                   "Hai provato ad aggiungere un contatto ICQ a una lista AIM (#158.EXT)");
 		resources.put("error_159",                   "Nessuna risposta del server circa la ricerca. Riprovare (#159.EXT)");
 		resources.put("error_160",                   "Errore durante la ricerca (#160.EXT)");
 		resources.put("error_161",                   "Nessun gruppo trovato. Aggiungerne uno (#161.EXT)");

		// Other errors
		resources.put("error_170",                   "Probabile insufficienza di memoria heap disponibile (#170.EXT)");
		resources.put("error_171",                   "Impossibile leggere le metainfo (#171.EXT)");

        // Camera errors 
        resources.put("error_180",                   "Errore nella creazione del VideoControl (#180.EXT)"); 
        resources.put("error_181",                   "Errore nell'inizializzazione dell'anteprima (#181.EXT)"); 
        resources.put("error_182",                   "Errore nell'avvio dell'anteprima (#182.EXT)"); 
        resources.put("error_183",                   "Errore nella cattura (#183.EXT)"); 
        resources.put("error_185",                   "Cattura immagini non supportata (#185.EXT)"); 
    
        // File transfer errors 
        resources.put("error_190",                   "Trasferimento con client precedenti a ICQv8 non supportato (#190.EXT)"); 
        resources.put("error_191",                   "Errore di lettura del file. Funzione forse non supportata (#191.EXT)"); 
        resources.put("error_192",                   "Errore di lettura del file. Percorso sbagliato o funzione non supportata (#192.EXT)"); 

	}


}


// #sijapp cond.end #