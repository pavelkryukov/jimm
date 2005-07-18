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
 File: src/jimm/res/Text_RU.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Alexei Solonets
 *******************************************************************************/


// #sijapp cond.if lang_RU is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_RU extends ResourceBundle
{


	public Text_RU()
	{

		// Labels
		// MOTOROLA formatted buttons
		// #sijapp cond.if target is "MOTOROLA"#
		resources.put("backlight_timeout",           "����� ��������� (���)");
		resources.put("backlight_manual",            "������ ��������� (*)");
		resources.put("select",                      "�����     ");
		resources.put("reset",                       "      �����");
		resources.put("cancel",                      "    ������");
		resources.put("back",                        "     �����");
		resources.put("exit_button",                 "     �����");
		resources.put("menu_button",                 "      ����");
		resources.put("save",                        "��������� ");
		resources.put("ok",                          "OK         ");
		resources.put("reply",                       "�����      ");
		resources.put("close",                       "   �������");
		resources.put("search_user",                 "�����      ");
		resources.put("send",                        "���������");
		resources.put("next",						 "���������");
		// #sijapp cond.else#
		resources.put("reply",                       "�����");
		resources.put("close",                       "�������");
		resources.put("select",                      "�������");
		resources.put("reset",                       "�����");
		resources.put("cancel",                      "������");
		resources.put("back", "�����");
		resources.put("save",                        "����.");
		resources.put("ok",                          "OK");
		resources.put("search_user",                 "����� ������������");
		resources.put("send",                        "���������");
		resources.put("next",						 "���������");
		// #sijapp cond.end#

		resources.put(".",                           ".");
		resources.put("about",                       "� ���������");
		resources.put("about_info",                  "Jimm - ��������� ICQ\n\nICQ ������ ��� J2ME\n������ ###VERSION###\n ����������: ###TARGET###\n������: ###MODULES###\n\nhttp://www.jimm.org/");
		resources.put("account",                     "������� ������");
		resources.put("add_group",                   "�������� ������");
		resources.put("add_user",                    "�������� ������������");
		resources.put("add_to_list",                 "��������");
		resources.put("age",                         "�������");
		resources.put("async",                       "����������� ��������");
		resources.put("attention",                   "��������!"); 
		resources.put("auth",				     	 "�����������");
		resources.put("auto_connect",				 "������������ �������������");
		resources.put("beep",                        "�����");
		resources.put("black_on_white",              "׸���� �� �����");
		resources.put("byte",                        "����");
		resources.put("chat_small_font",             "������ ����� � ����");                                          
		resources.put("city",                        "�����");
		resources.put("clear",                       "������� ��");
		resources.put("color_scheme",                "�������� �����");
		resources.put("conn_type",                   "��� �����������");
		resources.put("connect",                     "������������");
		resources.put("connecting",                  "�����������");
		resources.put("cost",                        "���������");
		resources.put("contact_list",                "������");
		resources.put("cp1251",                      "���������� ������ CP1251?");
		resources.put("cpd",                         "��������� � ����");
		resources.put("cpp",                         "��������� ������");
		resources.put("currency",                    "������");
		resources.put("dc_info",                     "DC Info");        
		resources.put("del_group",                   "������� ������");
		resources.put("delete_chat",                 "������� ���");
		resources.put("deny",						 "��������� �����������");
		resources.put("denyedby",					 "��� ������ �� ����������� ��������: ");
		resources.put("description",                 "��������");
		resources.put("name_desc",                   "��� ����� � ��������");
		resources.put("user_menu",                   "���� ������������");
		resources.put("disconnect",                  "�����������");
		resources.put("disconnecting",               "����������");
		resources.put("display_date",                "���������� ���� �� ��������?");
		resources.put("email",                       "E-mail");
		resources.put("error",                       "������");
		resources.put("exit",                        "�����");
		resources.put("female",                      "�");
		resources.put("filename",                    "��� �����");
		resources.put("filetransfer",                "�������� �����");
		resources.put("filepath",                    "���� � �����");
		resources.put("find",                        "�����");
		resources.put("firstname",                   "���");
		resources.put("free_heap",                   "��������� ������");
		resources.put("ft_name",                     "������� ����");		
		resources.put("ft_cam",                      "������� ���� � ������");		
		resources.put("gender",                      "���");
		resources.put("grant",						 "��������������");
		resources.put("grantedby",					 "��� �������������: ");
		resources.put("group_name",                  "��� ������");
		resources.put("group_is_not_empty",          "Selected group is not empty!\nMove all users to other groups!");
		resources.put("have_unread_mess",            "� ��� �������� ������������� ���������. ����� �� ���������?");
		resources.put("hide_offline",                "�������� �������������");
		resources.put("info",                        "����������");
		resources.put("init_ft",                     "�������������...");
		resources.put("kb",                          "��");
		resources.put("kbs",                         "kb/s");
		resources.put("keep_chat",                   "������� ������� ����?");
		resources.put("keep_conn_alive",             "������������ �����������?");
		resources.put("keylock",                     "����������");
		resources.put("keylock_message",             "��� ������������� ������� � ����������� ������� #");
		resources.put("keylock_enable",              "���������� ����������");
		resources.put("keylock_enabled",             "���������� ��������");
		resources.put("keyword",                     "�������� �����");
		resources.put("language",                    "����");
		resources.put("lang_BG", 					 "����������");
		resources.put("lang_BR",                     "������������� (��������)");
		resources.put("lang_CZ",                     "�������");
		resources.put("lang_DE",                     "��������");
		resources.put("lang_EN",                     "����������");
		resources.put("lang_ES",                     "���������");
		resources.put("lang_HE", 					 "���������");
		resources.put("lang_IT",                     "�����������");
		resources.put("lang_LT",                     "���������");
		resources.put("lang_PL",                     "��������");
		resources.put("lang_RU",                     "�������");
		resources.put("lang_SE",                     "��������");
		resources.put("lang_SR",                     "������");
		resources.put("lang_UA", 					 "����������");
		resources.put("lastname",                    "�������");
		resources.put("loading",                     "��������");
		resources.put("male",                        "M");
		resources.put("me",                          "�");
		resources.put("menu",                        "����");
		resources.put("message_notification",        "����������� � ���������");
		resources.put("msg_sound_file_name",         "���� ����� ���������");
		resources.put("message",                     "���������");
		resources.put("message_from",                "��������� ��");
		resources.put("minimize",                    "������� �����");
		resources.put("name",                        "���");
		resources.put("nick",                        "���");
		resources.put("no",                          "���");
		resources.put("not",                         "not");
		resources.put("no_results",                  "��� �����������");
		resources.put("no_not_empty_gr",             "�������� �� ������ ����� �� ��������������");
		resources.put("not_implemented",             "������� �� ��������������");
		resources.put("noreason",             		 "������� �� ����");
		resources.put("notice",                      "�����������");
		resources.put("nr",				     		 "�����");	
		resources.put("once_a_session",              "���� ��� �� ������");
		resources.put("onl_notification",            "������������� � �������������� ��������");
		resources.put("onl_sound_file_name",         "���� ����� �����������");
		resources.put("only_online",                 "���������� ������ ������ ��������");
		resources.put("options",                     "���������");
		resources.put("options_account",             "������� ������");
		resources.put("options_cost",                "���������");
		resources.put("options_effect",              "��� ���������� ���������������� ��� ���������� ���������");
		resources.put("options_interface",           "���������");
		resources.put("options_network",             "����");
		resources.put("options_other",               "������");
		resources.put("options_signaling",           "����");
		resources.put("password",                    "������");
		resources.put("plength",                     "����� ������������� ������ � ��");
		resources.put("plsauthme",                   "������! ����������, ����������� ���� � ������� �������� ���� � �������-����");
		resources.put("prev",						 "����������");
		resources.put("reason",						 "�������");
		resources.put("remove",                      "������� �� ������");
		resources.put("remove_me",                   "������� ���� �� ������������ ������������"); 
		resources.put("remove_me_from",              "������� ���� �� ������� ����� ");
		resources.put("remove_group",                "������� ������");
		resources.put("remove_user",                 "������� �������");
		resources.put("rename",                      "�������������");
		resources.put("requauth",                    "������ �����������");
		resources.put("reqstatmsg",                  "��������� ���������");
		resources.put("requ",                        "���������");
		resources.put("requno",                      "�� ���������");
		resources.put("res",                         "����������");
		resources.put("results",                     "����������");		
		resources.put("send_img",                    "������� ����");
		resources.put("send_message",                "����� ���������");
		resources.put("send_url",                    "����� URL");
		resources.put("server",                      "������");
		resources.put("server_host",                 "��� �������");
		resources.put("server_port",                 "����");
		resources.put("session",                     "������");
		resources.put("set_status",                  "���������� ������");
		resources.put("shadow_con",                  "�������������� �����������");
		resources.put("show_user_groups",            "������������ ��������");
		resources.put("since",                       "������� �");
		resources.put("size",                        "������");
		resources.put("sound",                       "�������� ����");
		resources.put("sort_by",                     "����������� �������-����");
		resources.put("sort_by_name",                "�� �����");
		resources.put("sort_by_status",              "�� �������");
		resources.put("speed",                       "��������");
		resources.put("status",                      "������");
		resources.put("status_message",              "Status message");
		resources.put("status_away",                 "����������");
		resources.put("status_chat",                 "�������� ��� ������");
		resources.put("status_dnd",                  "�� ����������");
		resources.put("status_invisible",            "���������");
		resources.put("status_na",                   "����������");
		resources.put("status_occupied",             "�����");
		resources.put("status_offline",              "�������");
		resources.put("status_online",               "������");
		resources.put("successful",                  "��������");
		resources.put("sysnotice",					 "��������� �����������");
		resources.put("traffic",                     "������");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("use_history",                 "������� ������� ���������");
		resources.put("user_add",                    "�������� ������������");
		resources.put("user_search",                 "����� ������������");
		resources.put("vibration",                   "��������");
		resources.put("viewfinder",                  "������������");
		resources.put("volume",                      "���������");
		resources.put("wait",                        "����������, ����� ...");
		resources.put("when_locked",                 "���� ������������");
		resources.put("warning",                     "��������������");
		resources.put("wantsyourauth",				 " ����� ����������������. �������: ");
		resources.put("was",                         "����");
		resources.put("whichgroup",				 	 "� ����� ������?");
		resources.put("white_on_black",              "����� �� ������");
		resources.put("white_on_blue",               "����� �� �����");
		resources.put("yes",                         "��");
		resources.put("youwereadded",				 "��� �������: ");

		//#sijapp cond.if modules_HISTORY is "true" #
		resources.put("text_to_find",                "�����");
		resources.put("find_backwards",              "� �������� �����������");
		resources.put("find_case_sensitiv",          "� ������ ��������");
		resources.put("history_info",                "����������");
		resources.put("hist_cur",                    "����� ��������� ��������");
		resources.put("hist_rc",                     "����� ��������� � �������");
		resources.put("hist_size",                   "������������ (��)");
		resources.put("hist_avail",                  "����� ����� (��)"); 
		resources.put("history",                     "������� ���������");
		resources.put("not_found",                   "�� ������");
		//#sijapp cond.end#

		// Generic errors
		resources.put("error_100",                   "����������� ������ (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "������������� ���� � ��� �� UIN (#110.EXT)");
		resources.put("error_111",                   "�������� ������ (#111.EXT)");
		resources.put("error_112",                   "�������������� UIN (#112.EXT)");
		resources.put("error_113",                   "������� ����� �������� � ������ IP (#113.EXT)");
		resources.put("error_114",                   "������� ��������� (#114.EXT)");
		resources.put("error_115",                   "�������-���� �� ����� ���� ��������� (#115.EXT)");
		resources.put("error_116",                   "������� ��������� �� ����� ���� ���������� (#116.EXT)");
		resources.put("error_117",                   "��� UIN �/��� ������ (#117.EXT)");
		resources.put("error_118",                   "������ �� �������� (#118.EXT)");

		// Network communication specific exceptions
		// Connection to server
		resources.put("error_120",                   "��������� ������ �����-������ (#120.EXT)");
		resources.put("error_121",                   "��������� TCP ���������� ������������� (#121.EXT)");
		resources.put("error_122",                   "��������� ������ �/��� ���� ����������� (#122.EXT)");
		resources.put("error_123",                   "���������� �� ����� ���� ����������� (#123.EXT)");
		resources.put("error_124",                   "������� ����� �� ������������� (#124.EXT)");


		// Parsing specific errors
		resources.put("error_130",                   "FLAP ��������� �� ��������� (#130.EXT)");
		resources.put("error_131",                   "����������� ����� (#131.EXT)");
		resources.put("error_132",                   "����� ��������������� ������ �� ��������� (#132.EXT)");
		resources.put("error_133",                   "SNAC ��������� �� ��������� (#133.EXT)");
		resources.put("error_134",                   "������ ������ ������ �� ���������� (#134.EXT)");
		resources.put("error_135",                   "����� �������������� ������ �� ��������� (#135.EXT)");
		resources.put("error_136",                   "����� ������ ping �� ��������� (#136.EXT)");
		resources.put("error_137",                   "��������� ������� ��������� ICQ �� ��������� (#137.EXT)");

		// Action errors
		resources.put("error_140",                   "��������� �������� �� ����� ���� ��������� � ������ ����� (#140.EXT)");

		// Specific action errors
		resources.put("error_150",                   "���������� ��������� �� ���������� (#150.EXT)");
		resources.put("error_151",                   "���������� ��������� 1 ���� �� ���������� (#151.EXT)");
		resources.put("error_152",                   "���������� ��������� 2 ���� �� ���������� (#152.EXT)");
		resources.put("error_153",                   "���������� ��������� 4 ���� �� ���������� (#153.EXT)");
		resources.put("error_154",                   "������ ���������� ������ ������������� (#154.EXT)");
		resources.put("error_155",                   "������ ��� ��������� � ������ �� ������� (#155.EXT)");
		resources.put("error_156",                   "������ ����������. ���������� ����� (#156.EXT)");
		resources.put("error_157",                   "��������� �� ������ ���������� ��������� ����� ���� (#157.EXT)");
		resources.put("error_158",                   "�� ��������� ������� �������� ������������ ICQ � ����� AIM (#158.EXT)");
		resources.put("error_159",                   "������ �� �������� �� ������ ������. ���������� ����� (#159.EXT)");
		resources.put("error_160",                   "������ ������ (#160.EXT)");
		resources.put("error_161",                   "� ��� ��� �� ����� ������ � ������. ����������, �������� ����� ������ (#161.EXT)");

		// Other errors
		resources.put("error_170",                   "��������, ������������ ������ (#160.EXT)");
		resources.put("error_171",                   "���������� �������� ���� ���������� (#161.EXT)");

		// Camera errors
		resources.put("error_180",                   "������ �������� ������� 'VideoControl' (#180.EXT)");
		resources.put("error_181",                   "������ ������������� ������������� (#181.EXT)");
		resources.put("error_182",                   "������ ������� ������������� (#182.EXT)");
		resources.put("error_183",                   "������ �� ����� ��������� ������ (#183.EXT)");
		resources.put("error_185",                   "���������� �� �������������� (#185.EXT)");

		// File transfer errors
		resources.put("error_190",                   "�������� ������ �� �������������� ��� �������� ICQ ���� 8-�� ������ (#190.EXT)");
		resources.put("error_191",                   "������ ������ �����. ��������, ������ ������� �� �������������� (#191.EXT)");
		resources.put("error_192",                   "������������ ���� � �����. ��������, ������ ������� �� �������������� (#192.EXT)");		

	}
}

// #sijapp cond.end #
