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
 File: src/jimm/res/Text_ES.java
 Version: 0.3.1  Date: 2004/12/25
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


// #sijapp cond.if lang_ES is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_ES extends ResourceBundle
{


	public Text_ES()
	{

		// Labels
		resources.put(".",                           ".");
		resources.put("about",                       "Información");
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nICQ client for J2ME\nVersion 0.1b4\nSee http://www.jimm.org/");
		resources.put("account",                     "Cuenta");
		resources.put("add_user",                    "Nuevo Usuario");
		resources.put("async",                       "Async");
		resources.put("back",                        "Regresar");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("beep",                        "Beep");
		// #sijapp cond.end #
		resources.put("byte",                        "Byte");
		resources.put("cancel",                      "Cancelar");
		resources.put("close",                       "Cerrar");
		resources.put("conn_type",                   "Connection type");
		resources.put("connect",                     "Conectar");
		resources.put("connecting",                  "Conectando");
		resources.put("contact_list",                "Lista");
		resources.put("cp1251",                      "Use CP1252 hack?");
		resources.put("cpd",                         "Cost per day");
		resources.put("cpp",                         "Cost per packet");
		resources.put("currency",                    "Currency");
		resources.put("delete_chat",                 "Delete chat");
		resources.put("user_menu",                   "User Menu");
		resources.put("disconnect",                  "Desconectar");
		resources.put("disconnecting",               "Desconectando");
		resources.put("display_advertisement",       "Agregar Jimm pie de pagina?");
		resources.put("display_date",                "Display date on splash screen?");
		resources.put("email",                       "Email");
		resources.put("error",                       "Error");
		resources.put("exit",                        "Salir");
		resources.put("hide_offline",                "Hide offline contacts");
		resources.put("info",                        "Info");
		resources.put("kb",                          "kB");
		resources.put("keep_chat",                   "Keep chat histroy?");
		resources.put("keep_conn_alive",             "Mantener conección?");
		resources.put("keylock_enable",              "Activar bloqueado del teclado");
		resources.put("keylock_enabled",             "Bloqueado activado");
		resources.put("language",                    "Idioma");
		resources.put("lang_BR",                     "Portugués (Brasil)");
		resources.put("lang_CZ",                     "Checo");
		resources.put("lang_DE",                     "Alemán");
		resources.put("lang_EN",                     "Inglés");
		resources.put("lang_ES",                     "Español");
		resources.put("lang_LT",                     "Lituano");
		resources.put("lang_RU",                     "Ruso");
		resources.put("lang_SE",                     "Sueco");
		resources.put("loading",                     "Cargando");
		resources.put("me",                          "me");
		resources.put("menu",                        "Menu");
		resources.put("message",                     "Mensaje");
		resources.put("message_from",                "Mensaje de");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("message_notification",        "Notificación de nuevos mensajes");
		// #sijapp cond.end #
		resources.put("name",                        "Nombre");
		resources.put("nick",                        "Nick");
		resources.put("no",                          "No");
		resources.put("not_implemented",             "Funcción todavía no implementada");
		resources.put("notice",                      "Nota");
		resources.put("ok",                          "OK");
		resources.put("once_a_session",              "Sólo una vez en sesión");
		resources.put("options",                     "Opciones");
		resources.put("options_account",             "Account");
		resources.put("options_cost",                "Cost");
		resources.put("options_effect",              "Quizá sea necesario reconectar para que los cambios se activen!");
		resources.put("options_interface",           "Interface");
		resources.put("options_network",             "Network");
		resources.put("options_other",               "Other");
		resources.put("password",                    "Contraseña");
		resources.put("plength",                     "Length of charge packet in kB");
		resources.put("remove",                      "Remover de la lista");
		resources.put("reply",                       "contestar");
		resources.put("reset",                       "Reset");
		resources.put("save",                        "guardar");
		resources.put("search_user",                 "buscar usuario");
		resources.put("send",                        "mandar");
		resources.put("send_message",                "Nuevo Mensaje");
		resources.put("send_url",                    "Nuevo URL");
		resources.put("server",                      "Login server");
		resources.put("server_host",                 "Hostname");
		resources.put("server_port",                 "Puerto");
		resources.put("session",                     "Session");
		resources.put("set_status",                  "Establecer status");
		resources.put("since",                       "Since");
		resources.put("sound",                       "ICQ Sound");
		resources.put("sound_file_name",             "Sound File Name");
		resources.put("sort_by",                     "Sort contact list");
		resources.put("sort_by_name",                "By name");
		resources.put("sort_by_status",              "By status");
		resources.put("status_away",                 "Ausente");
		resources.put("status_chat",                 "Libre para chatear");
		resources.put("status_dnd",                  "No molestar");
		resources.put("status_invisible",            "Invisible");
		resources.put("status_na",                   "No disponible");
		resources.put("status_occupied",             "Ocupado");
		resources.put("status_offline",              "Offline");
		resources.put("status_online",               "Online");
		resources.put("traffic",                     "Traffic");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("user_add",                    "Agregar usuario");
		resources.put("user_search",                 "buscar usuario");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("vibration",                   "Vibración");
		// #sijapp cond.end #
		resources.put("wait",                        "Por favor espere ...");
		resources.put("warning",                     "Warning");
		resources.put("yes",                         "Sí");

		// Generic errors
		resources.put("error_100",                   "Error desconocido (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "Multiples logins en el mismo UIN (#110.EXT)");
		resources.put("error_111",                   "Contraseña incorrecta (#111.EXT)");
		resources.put("error_112",                   "UIN no existente (#112.EXT)");
		resources.put("error_113",                   "Demasiados clientes para el mismo IP (#113.EXT)");
		resources.put("error_114",                   "Demasiadas conecciones al mismo tiempo (#114.EXT)");
		resources.put("error_115",                   "Lista de contactos ne se pudo leer (#115.EXT)");
		resources.put("error_116",                   "Mensaje fuera de línea no se pudo leer (#116.EXT)");
		resources.put("error_117",                   "contraseña y/o UIN vacíos (#117.EXT)");
		resources.put("error_118",                   "No respuesta del server (#118.EXT)");

		// Network communication specific exceptions
		resources.put("error_120",                   "Un I/O error ha ocurrido (#120.EXT)");
		resources.put("error_121",                   "Conección al TCP no se puede (#121.EXT)");
		resources.put("error_122",                   "server específico y/o puerto es inválido (#122.EXT)");
		resources.put("error_123",                   "Conección no ha sido establecida (#123.EXT)");
		resources.put("error_124",                   "Input stream is out of sync (#124.EXT)");

		// Parsing specific errors
		resources.put("error_130",                   "FLAP header could not be parsed (#130.EXT)");
		resources.put("error_131",                   "Canal desconocido (#131.EXT)");
		resources.put("error_132",                   "Connect channel packet  could not be parsed (#132.EXT)");
		resources.put("error_133",                   "SNAC header could not be parsed (#133.EXT)");
		resources.put("error_134",                   "Error channel packet could not be parsed (#134.EXT)");
		resources.put("error_135",                   "Disconnect channel packet could not be parsed (#135.EXT)");
		resources.put("error_136",                   "Ping channel packet could not be parsed (#136.EXT)");
		resources.put("error_137",                   "Old ICQ protocol header could not be parsed (#137.EXT)");

		// Action errors
		resources.put("error_140",                   "Requested action cannot be queued for execution at this time (#140.EXT)");

		// Specific action errors
		resources.put("error_150",                   "Mensaje recibido no fue comprendido (#150.EXT)");
		resources.put("error_151",                   "Received type 1 message could not be understood (#151.EXT)");
		resources.put("error_152",                   "Received type 2 message could not be understood (#152.EXT)");
		resources.put("error_153",                   "Received type 4 message could not be understood (#153.EXT)");
		resources.put("error_154",                   "Actualización de la lista de contactos ha fallado (#154.EXT)");

		// Other errors
		resources.put("error_160",                   "No suficiente memoria heap disponible (#160.EXT)");
		resources.put("error_161",                   "Could not fetch meta info (#161.EXT)");

	}


}


// #sijapp cond.end #
