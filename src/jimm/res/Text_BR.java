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
 File: src/jimm/res/Text_BR.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Eduardo Luis Pinheiro Silva
 *******************************************************************************/


// #sijapp cond.if lang_BR is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_BR extends ResourceBundle
{


	public Text_BR()
	{

		// Labels
		resources.put(".",                           ".");
		resources.put("about",                       "Sobre");
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nCliente de ICQ para J2ME\nVersão 0.3.1\nVeja http://www.jimm.org/");
		resources.put("account",                     "Conta");
		resources.put("add_user",                    "Adicionar usuário");
		resources.put("async",                    	 "Async");
		resources.put("back",                        "Voltar");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("beep",                        "Beep");
		// #sijapp cond.end #
		resources.put("byte",                        "Byte");
		resources.put("cancel",                      "Cancelar");
		resources.put("close",                       "Fechar");
		resources.put("conn_type",                   "Connection type");
		resources.put("connect",                     "Conectar");
		resources.put("connecting",                  "Conectando");
		resources.put("contact_list",                "Lista");
		resources.put("cp1251",                      "Use CP1252 hack?");
		resources.put("cpd",                         "Custo por dia");
		resources.put("cpp",                         "Custo por pacote");
		resources.put("currency",                    "Moeda corrente");
		resources.put("delete_chat",                 "Apagar chat");
		resources.put("user_menu",                   "Menu de usuário");
		resources.put("disconnect",                  "Desconectar");
		resources.put("disconnecting",               "Desconectando");
		resources.put("display_advertisement",       "Adicionar dica Jimm?");
		resources.put("display_date",                "Mostrar data na tela de splash?");
		resources.put("email",                       "Email");
		resources.put("error",                       "Erro");
		resources.put("exit",                        "Sair");
		resources.put("hide_offline",                "Esconder contatos desconectados");
		resources.put("info",                        "Info");
		resources.put("kb",                          "kB");
		resources.put("keep_chat",                   "Keep chat histroy?");
		resources.put("keep_conn_alive",             "manter conexão viva?");
		resources.put("keylock_enable",              "Habilitar keylock");
		resources.put("keylock_enabled",             "Keylock habilitado");
		resources.put("language",                    "Linguagem");
		resources.put("lang_BR",                     "Português (Brasil)");
		resources.put("lang_CZ",                     "Tcheco");
		resources.put("lang_DE",                     "Alemão");
		resources.put("lang_EN",                     "Inglês");
		resources.put("lang_ES",                     "Espanhol");
		resources.put("lang_LT",                     "Lituano");
		resources.put("lang_RU",                     "Russo");
		resources.put("lang_SE",                     "Sueco");
		resources.put("loading",                     "Carregando");
		resources.put("me",                          "eu");
		resources.put("menu",                        "Menu");
		resources.put("message",                     "Menssagem");
		resources.put("message_from",                "Menssagem de");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("message_notification",        "Notificação de mensagem");
		// #sijapp cond.end #
		resources.put("name",                        "Nome");
		resources.put("nick",                        "Apelido");
		resources.put("no",                          "Não");
		resources.put("not_implemented",             "Função ainda não implementada.");
		resources.put("notice",                      "Avisar");
		resources.put("ok",                          "OK");
		resources.put("once_a_session",              "Uma por seção");
		resources.put("options",                     "Opções");
		resources.put("options_account",             "conta");
		resources.put("options_cost",                "Custo");
		resources.put("options_effect",              "Você tem que reconectar para que algumas mudanças tenham efeito!");
		resources.put("options_interface",           "Interface");
		resources.put("options_network",             "Rede");
		resources.put("options_other",               "Outros");
		resources.put("password",                    "Senha");
		resources.put("plength",                     "Custo por pacote em kB");
		resources.put("remove",                      "Remover da Lista");
		resources.put("reply",                       "Responder");
		resources.put("reset",                       "Limpar");
		resources.put("save",                        "Salvar");
		resources.put("search_user",                 "Procurar por usuário");
		resources.put("send",                        "Enviar");
		resources.put("send_message",                "Nova Menssagem");
		resources.put("send_url",                    "Nova URL");
		resources.put("server",                      "Servidor de login");
		resources.put("server_host",                 "Hostname");
		resources.put("server_port",                 "Porta");
		resources.put("session",                     "Sessão");
		resources.put("set_status",                  "Colocar status");
		resources.put("since",                       "Desde");
		resources.put("sound",                       "Som do ICQ");
		resources.put("sound_file_name",             "Sound File Name");
		resources.put("sort_by",                     "Ordernar lista de contatos");
		resources.put("sort_by_name",                "Por nome");
		resources.put("sort_by_status",              "Por status");
		resources.put("status_away",                 "Afastado");
		resources.put("status_chat",                 "Livre para Chat");
		resources.put("status_dnd",                  "Não perturbe");
		resources.put("status_invisible",            "Invisível");
		resources.put("status_na",                   "Não disponível");
		resources.put("status_occupied",             "Ocupado");
		resources.put("status_offline",              "Offline");
		resources.put("status_online",               "Online");
		resources.put("traffic",                     "Tráfego");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("user_add",                    "Adicionar usuário");
		resources.put("user_search",                 "Procurar usuário");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("vibration",                   "Vibração");
		// #sijapp cond.end #
		resources.put("wait",                        "Aguarde ...");
		resources.put("warning",                     "Atenção");
		resources.put("yes",                         "Sim");

		// Generic errors
		resources.put("error_100",                   "Erro desconhecido (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "Multiplos logins no mesmo UIN (#110.EXT)");
		resources.put("error_111",                   "Senha errada (#111.EXT)");
		resources.put("error_112",                   "UIN não existente (#112.EXT)");
		resources.put("error_113",                   "Muitos clientes do mesmo IP (#113.EXT)");
		resources.put("error_114",                   "Taxa excedida (#114.EXT)");
		resources.put("error_115",                   "A lista do contatos não processada (#115.EXT)");
		resources.put("error_116",                   "Mensagem offine não processada (#116.EXT)");
		resources.put("error_117",                   "UIN e/ou senha vazios (#117.EXT)");
		resources.put("error_118",                   "Sem resposta do servidor (#118.EXT)");

		// Network communication specific exceptions
		resources.put("error_120",                   "Um erro de I/O ocorrido (#120.EXT)");
		resources.put("error_121",                   "Conexão TCP não pode ser realizada (#121.EXT)");
		resources.put("error_122",                   "Servidor ou porta não pode ser acessada (#122.EXT)");
		resources.put("error_123",                   "conexão não foi estabelecida (#123.EXT)");
		resources.put("error_124",                   "Dados de entrada fora de sincronia (#124.EXT)");

		// Parsing specific errors
		resources.put("error_130",                   "Cabeçalho FLAP não pode ser processado (#130.EXT)");
		resources.put("error_131",                   "Canal desconhecido (#131.EXT)");
		resources.put("error_132",                   "Pacote de canal de conexão não pode ser processado (#132.EXT)");
		resources.put("error_133",                   "Cabeçalho SNAC não pode ser processado (#133.EXT)");
		resources.put("error_134",                   "Pacote de canal de erro não pode ser processado (#134.EXT)");
		resources.put("error_135",                   "Pacote de canal de desconexão não pode ser processado (#135.EXT)");
		resources.put("error_136",                   "Pacote de canal de ping não pode ser processado (#136.EXT)");
		resources.put("error_137",                   "Protocolo antigo do ICQ não pode ser processado (#137.EXT)");

		// Action errors
		resources.put("error_140",                   "Ação requisitada não pode ser colocada na fila agora (#140.EXT)");

		// Specific action errors
		resources.put("error_150",                   "Mensagem recebida pode não ser entendida (#150.EXT)");
		resources.put("error_151",                   "Mensagem (tipo 1) recebida pode não ser entendida (#151.EXT)");
		resources.put("error_152",                   "Mensagem (tipo 2) recebida pode não ser entendida (#152.EXT)");
		resources.put("error_153",                   "Mensagem (tipo 4) recebida pode não ser entendida (#153.EXT)");
		resources.put("error_154",                   "Atualização de lista contatos com erro (#154.EXT)");

		// Other errors
		resources.put("error_160",                   "Possívelmente não há memória suficiente (#160.EXT)");
		resources.put("error_161",                   "Informação META não pôde ser recuperada (#161.EXT)");

	}


}


// #sijapp cond.end #
