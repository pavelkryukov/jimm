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
 File: src/jimm/res/Text_SR.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Urukalo Milan
 *******************************************************************************/


//#sijapp cond.if lang_SR is "true"#


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_SR extends ResourceBundle
{


	public Text_SR()
	{

	
	    // Labels
        // MOTOROLA formatted buttons
        // #sijapp cond.if target is "MOTOROLA"#
    	resources.put("backlight_timeout",           "Траjање позадинског осветлења (sec)");
	    resources.put("backlight_manual",            "Ручно подешавање осветлења (* тастер)");
	    resources.put("select",                      "Одабери     ");
	    resources.put("reset",                       "       Reset");
	    resources.put("cancel",                      "      Откажи");
	    resources.put("back",                        "       Назад");
	    resources.put("exit_button",                 "       Изађи");
	    resources.put("menu_button",                 "        Мени");
	    resources.put("save",                        "Сачуваj     ");
        resources.put("ok",                          "OK          ");
        resources.put("reply",                       "Одговори    ");
        resources.put("close",                       "     Затвори");
        resources.put("send",                        "Пошаљи      ");
	    resources.put("next",                        "Даље        ");
        // #sijapp cond.else# 
        resources.put("reply",                       "Одговори");
        resources.put("close",                       "Затвори");
        resources.put("select",                      "Одабери");
        resources.put("reset",                       "Reset");
        resources.put("cancel",                      "Откажи");
        resources.put("back",                        "Назад");
        resources.put("save",                        "Сачуваj");
        resources.put("ok",                          "OK");
        resources.put("send",                        "Пошаљи");
    	resources.put("next",                        "Даље");
   	    // #sijapp cond.end#

		resources.put(".",                           ".");
		resources.put("about",                       "О програму");
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nICQ client for J2ME\n\nVersion: ###VERSION###\nTarget: ###TARGET###\nModules: ###MODULES###\n\nSee http://www.jimm.org/");
		resources.put("account",                     "Налог");
		resources.put("add_group",                   "Додаj групу");
		resources.put("add_user",                    "Додаj корисника");
		resources.put("add_to_list",                 "Додаj на листу");
		resources.put("age",                         "Године"); 
		resources.put("async",                       "Async");
		resources.put("attention",                   "Упозорење!");
		resources.put("auth",                        "Ауторизациjа"); 
		resources.put("auto_connect",    		     "Аутоматско конектовање при стартовању");
		resources.put("beep",                        "Бип");
		resources.put("black_on_white",              "Црно-Бело");
		resources.put("byte",                        "Баjт");
		resources.put("chat_small_font",	         "Мала слова");
		resources.put("city",                        "City");
		resources.put("clear",                       "Обриши");
		resources.put("color_scheme",                "Колор шема");
		resources.put("conn_type",                   "Тип конекциjе");
		resources.put("connect",                     "Конектуj се");
		resources.put("connecting",                  "Конектуjем се");
		resources.put("cost",                        "Цена");
		resources.put("contact_list",                "Контакти");
		resources.put("cp1251",                      "Use CP1251 hack?");
		resources.put("cpp",                         "Цена по дану");
		resources.put("cpd",                         "цена по пакету");
		resources.put("currency",                    "Валута");
		resources.put("del_group",                   "Обриши групу");
		resources.put("delete_chat",                 "Обриши разговор");
		resources.put("deny",                        "Одбиj ауторизациjу");
		resources.put("denyedby",                    "Вас захтев за ауторизациjу jе одбиjен од: ");
		resources.put("description",                 "Опис");
		resources.put("name_desc",                   "Име фаjла и опис");
		resources.put("user_menu",                   "Контакти");
		resources.put("disconnect",                  "Дисконектуj се");
		resources.put("disconnecting",               "Дисконектованjе");
		resources.put("display_date",                "Прикажи датум на екрану?");
		resources.put("email",                       "Пошта");
		resources.put("error",                       "Грешка");
		resources.put("exec",                        "Изврши");
		resources.put("exit",                        "Излаз");
		resources.put("female",                      "З");
		resources.put("filetransfer",                "Пренос фаjла");
		resources.put("filepath",                    "Пут до фаjла");
		resources.put("find",                        "Пронађи");
		resources.put("firstname",                   "Име");
		resources.put("free_heap",                   "Ослободи мемориjу");
		resources.put("ft_name",                     "Пошаљи фаjл");		
		resources.put("ft_cam",                      "Пошаљи слику са камере");
		resources.put("gender",                      "Пол");
		resources.put("grant",                       "Ауторизуj");
		resources.put("grantedby",                   "Ауторизовано од:");
		resources.put("group_name",                  "Назив Групе");
		resources.put("group_is_not_empty",          "Означена група ниjе празна!\nИспразни jе, премести контакте у остале групе!");
		resources.put("have_unread_mess",            "Имате непрочитаних порука. Сигурни сте да желите изаћи?");
		resources.put("hide_offline",                "Сакриj неактивне контакте");
		resources.put("info",                        "Информациjе");
		resources.put("init_ft",                     "Инциjализациjа");
		resources.put("kb",                          "kB");
		resources.put("kbs",                         "kb/s");
		resources.put("keep_chat",                   "Задржи разговор?");
		resources.put("keep_conn_alive",             "Одржаваj конекциjу?");
		resources.put("keylock",                     "Закључаj тастере");
		resources.put("keylock_message",             "Притисни \"#\" да откључаж тастере");
		resources.put("keylock_enable",              "Закључаj тастере");
		resources.put("keylock_enabled",             "Тастери закључани");
		resources.put("keyword",                     "Кључно слово");
		resources.put("language",                    "jезик");
		resources.put("lang_BG", 	         	     "Bulgarian");
		resources.put("lang_BR",                     "Portuguese (Brazil)");
		resources.put("lang_CZ",                     "Czech");
		resources.put("lang_DE",                     "German");
		resources.put("lang_EN",                     "English");
		resources.put("lang_ES",                     "Spanish");
		resources.put("lang_HE", 	        	     "Hebrew");
		resources.put("lang_IT",                     "Italian");
		resources.put("lang_LT",                     "Lithuanian");
		resources.put("lang_PL",                     "Polish");
		resources.put("lang_RU",                     "Russian");
		resources.put("lang_SE",                     "Swedish");
		resources.put("lang_SR",                     "Serbian");		
		resources.put("lang_UA", 	        	     "Ukrainian");
		resources.put("lastname",                    "Презиме");
		resources.put("loading",                     "Учитавам");
		resources.put("male",                        "M");
		resources.put("me",                          "jа");
		resources.put("menu",                        "Мени");
		resources.put("message_notification",        "Упозорење на поруку");
		resources.put("msg_sound_file_name",         "Фаjл са звуком за поруку");
		resources.put("message",                     "Порука");
		resources.put("message_from",                "Порука од");
		resources.put("minimize",                    "Смањено");
		resources.put("name",                        "Име");
		resources.put("nick",                        "Надимак");
		resources.put("no",                          "Не");
		resources.put("not",                         "ниjе");
		resources.put("no_results",                  "Без резултата");
		resources.put("no_not_empty_gr",             "Брисање група коjе нису празне, ниjе подржано");
		resources.put("not_implemented",             "Функциjа ниjе подржана");
		resources.put("noreason",                    "Упит ниjе задан.");
		resources.put("notice",                      "Упозорење");
		resources.put("nr",                          "Nr");
		resources.put("once_a_session",              "jедном по сесиjи");
		resources.put("onl_notification",            "Информациjа о долазећом контакту");
		resources.put("onl_sound_file_name",         "Фаjл са Online звуком");
		resources.put("only_online",                 "Покажи само online контакте");
		resources.put("options",                     "Опциjе");
		resources.put("options_account",             "Налог");
		resources.put("options_cost",                "Цена");
		resources.put("options_effect",              "Морате да се реконектуjете да би измене биле примљене!");
		resources.put("options_interface",           "Изглед");
		resources.put("options_network",             "Мрежа");
		resources.put("options_other",               "Остало");
		resources.put("options_signaling",           "Сигнализациjа");
		resources.put("password",                    "Лозинка");
		resources.put("plength",                     "Величина пакета у kB");
		resources.put("plsauthme",                   "Ћао, молио бих да прихватите моj захтев да ме додате на контакт листу.");
		resources.put("prev",                        "Претходни");
		resources.put("reason",                      "Разлог");
		resources.put("remove",                      "Избаци из листе");
		resources.put("remove_group",                "Избриши групу");
		resources.put("remove_user",                 "Избриши контакт");
		resources.put("rename",                      "Rename");
		resources.put("requauth",                    "Тражи ауторизациjу");
		resources.put("requ",                        "Потребно");
		resources.put("requno",                      "Ниjе потребно");
		resources.put("res",                         "Резочуциjа");
		resources.put("results",                     "Резултати");
		resources.put("search_user",                 "Пронађи корисника");
		resources.put("send_img",                    "Пошаљи слику");
		resources.put("send_message",                "Нова порука");
		resources.put("send_url",                    "Нови URL");
		resources.put("server",                      "Сервер");
		resources.put("server_host",                 "Име сервера");
		resources.put("server_port",                 "Порт");
		resources.put("session",                     "Сесиjа");
		resources.put("set_status",                  "Одабери статус");
		resources.put("shadow_con",                  "Сумњива конекциjа");
		resources.put("show_user_groups",            "Прикажи контакт групе");
		resources.put("since",                       "После");
		resources.put("size",                        "Величина");
		resources.put("sound",                       "ICQ Звук");
		resources.put("sound_file_name",             "Име фаjла са звуком");
		resources.put("sort_by",                     "Сортираj контакте");
		resources.put("sort_by_name",                "По имену");
		resources.put("sort_by_status",              "По статусу");
		resources.put("speed",                       "Брзина");
		resources.put("status",                      "Статус");
		resources.put("status_away",                 "Одсутан");
		resources.put("status_chat",                 "Слободан за разговор");
		resources.put("status_dnd",                  "Не узнемираваj");
		resources.put("status_invisible",            "Невидљив");
		resources.put("status_na",                   "Недоступан");
		resources.put("status_occupied",             "Заузет");
		resources.put("status_offline",              "Offline");
		resources.put("status_online",               "Online");
		resources.put("successful",                  "Успешно");
		resources.put("sysnotice",		     "Порука Система");
		resources.put("traffic",                     "Проток");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("use_history",                 "Сачуваj старе поруке");
		resources.put("user_add",                    "Додаj корисника");
		resources.put("user_search",                 "Пронађи корисника");
		resources.put("vibration",                   "Вибрациjа");
		resources.put("viewfinder",                  "Viewfinder");
		resources.put("volume",                      "jачина звука");	
		resources.put("wait",                        "Сачекаjте молићу...");
		resources.put("warning",                     "Упозорење");
		resources.put("wantsyourauth",               " желим ауторизациjу. Разлог: ");
		resources.put("was",                         "бити");
		resources.put("whichgroup",                  "Коjа група?");
		resources.put("white_on_black",              "Бело-Црно");
		resources.put("white_on_blue",               "Бело-Плаво");
		resources.put("yes",                         "Да");
		resources.put("youwereadded",                "Додати сте на листу код UIN: ");
		
		//#sijapp cond.if modules_HISTORY is "true" #
		resources.put("text_to_find",                "Текст");
		resources.put("find_backwards",              "Назад");
		resources.put("find_case_sensitiv",          "Пази на велика и мала слова");
		resources.put("history_info",                "Информациjе о старим порукама");
		resources.put("hist_cur",                    "Броj порука од тренутног контакта");
		resources.put("hist_rc",                     "Укупан броj порука");
		resources.put("hist_size",                   "Заузета мемориjа (kB)");
		resources.put("hist_avail",                  "Укупна мемориjа (kB)"); 
		resources.put("history",                     "Сачуване старе поруке");
		resources.put("not_found",                   "ниjе пронађено");
		//#sijapp cond.end#

		// Generic errors
		resources.put("error_100",                   "Непозната грешка (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "Више корисника на исти UIN (#110.EXT)");
		resources.put("error_111",                   "Погрешна лозинка (#111.EXT)");
		resources.put("error_112",                   "Непостоjећи UIN (#112.EXT)");
		resources.put("error_113",                   "Превише корисника са истог IP (#113.EXT)");
		resources.put("error_114",                   "Проток преслаб (#114.EXT)");
		resources.put("error_115",                   "Контакти нечитљиви (#115.EXT)");
		resources.put("error_116",                   "Оffline порука нечитљива (#116.EXT)");
		resources.put("error_117",                   "Празан UIN и/или лозинка (#117.EXT)");
		resources.put("error_118",                   "Нема одговора од сервера (#118.EXT)");

		// Network communication  specific exceptions
		// Connection to server
		resources.put("error_120",                   "Грешка на улазу-излазу (#120.EXT)");
		resources.put("error_121",                   "TCP конекциjа неможе да се оствари (#121.EXT)");
		resources.put("error_122",                   "Сервер и/или порт су погрешни (#122.EXT)");
		resources.put("error_123",                   "Конекциjа не може да се оствари (#123.EXT)");
		resources.put("error_124",                   "Улазни подаци нису синхронизовани (#124.EXT)");

		// Peer connection
		resources.put("error_125",                   "Грешка на улазу-излазу (#125.EXT)");
		resources.put("error_126",                   "TCP конекциjа неможе да се оствари (#126.EXT)");
		resources.put("error_127",                   "Сервер и/или порт су погрешни (#127.EXT)");
		resources.put("error_128",                   "Конекциjа не може да се оствари (#128.EXT)");
		resources.put("error_129",                   "Улазни подаци нису синхронизовани (#129.EXT)");

		// Parsing specific errors
		resources.put("error_130",                   "FLAP заглавље нечитљиво (#130.EXT)");
		resources.put("error_131",                   "Непознат канал (#131.EXT)");
		resources.put("error_132",                   "Конекциjа из канала нечитљива (#132.EXT)");
		resources.put("error_133",                   "SNAC заглавље нечитљиво (#133.EXT)");
		resources.put("error_134",                   "Грешка, пакети из канала нечитљиви (#134.EXT)");
		resources.put("error_135",                   "Дисконекциjа из канала нечитљива (#135.EXT)");
		resources.put("error_136",                   "Пакет ping канала нечитљив (#136.EXT)");
		resources.put("error_137",                   "Заглавље старог ICQ протокола нечитљиво (#137.EXT)");

		// Action errors
		resources.put("error_140",                   "Захтевана радња не може да се изврши овог пута (#140.EXT)");

		// Specififc action errors
		resources.put("error_150",                   "Примљена порука ниjе разумљива(#150.EXT)");
		resources.put("error_151",                   "Примљена порука типа 1 ниjе разумљива (#151.EXT)");
		resources.put("error_152",                   "Примљена порука типа 2 ниjе разумљива (#152.EXT)");
		resources.put("error_153",                   "Примљена порука типа 4 ниjе разумљива (#153.EXT)");
		resources.put("error_154",                   "Учитавање контаката ниjе успело (#154.EXT)");
		resources.put("error_155",                   "Обjекат jе већ на Вашоj листи сервера (#155.EXT)");
		resources.put("error_156",                   "Грешка приликом додавања. Покушаjте поново (#156.EXT)");
		resources.put("error_157",                   "Ниjе дозвољено више елемената овог типа (#157.EXT)");
		resources.put("error_158",                   "Покусали сте да додате ICQ контакт на AIM листу (#158.EXT)");
		resources.put("error_159",                   "Сервер ниjе одговорио на захтев за претрагу. Покушаjте поново (#159.EXT)");
		resources.put("error_160",                   "Грешка приликом претраге (#160.EXT)");
		resources.put("error_161",                   "Група ниjе пронађена, додаj групу (#161.EXT)");

		// Other errors
		resources.put("error_170",                   "Изгледа да нема довољно мемориjе (#170.EXT)");
		resources.put("error_171",                   "Не могу да дохватим мета информациjе (#171.EXT)");

		// Camera errors
		resources.put("error_180",                   "Грешка приликом креирања VideoControl (#180.EXT)");
		resources.put("error_181",                   "Viewfinder грешка приликом инциjализациjе (#181.EXT)");
		resources.put("error_182",                   "Viewfinder старт грешка (#182.EXT)");
		resources.put("error_183",                   "Грешка приликом сликања (#183.EXT)");
		resources.put("error_185",                   "Сликање ниjе подрзано(#185.EXT)");
		
		// File transfer errors
		resources.put("error_190",                   "Трансфер фаjлова са корисницима коjи имаjу стариjу верзиjу од ICQv8 ниjе подржан (#190.EXT)");
		resources.put("error_191",                   "Не могу да прочитам фаjл. Функциjа ниjе подржана(#191.EXT)");
		resources.put("error_192",                   "Не могу да прочитам фаjл. погрешан пут или формат ниjе подржан(#192.EXT)");
		resources.put("error_193",                   "Грешка приликом приступања фаjл систему. Сигурносни проблем (#193.EXT)");

	}
}

//#sijapp cond.end #
