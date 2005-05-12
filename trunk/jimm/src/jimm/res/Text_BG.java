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
File: src/jimm/res/Text_BG.java
Version: ###VERSION###  Date: ###DATE###
Author(s): Manuel Linsmayer, Andreas Rossbacher, Ivaylo Donchev
*******************************************************************************/


//#sijapp cond.if lang_BG is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_BG extends ResourceBundle
{


	public Text_BG()
	{

	
		// Labels
		// MOTOROLA formatted buttons
 	    // #sijapp cond.if target is "MOTOROLA"#
 	    resources.put("select",                      "Избери      ");
 	    resources.put("reset",                       "       Ресет");
 	    resources.put("cancel",                      "      Отказ");
 	    resources.put("back",                        "       Назад");
 	    resources.put("exit_button",                 "       Изход");
 	    resources.put("menu_button",                 "      Меню");
 	    resources.put("save",                        "Запази       ");
 	    resources.put("ok",                          "OK          ");
 	    resources.put("reply",                       "Отговори       ");
 	    resources.put("close",                       "       Затвори");
 	    resources.put("send",                        "Изпрати        ");
 	    resources.put("next",                        "       Нататък");
 	    // #sijapp cond.else#
 	    resources.put("reply",                       "Отговори");
 	    resources.put("close",                       "Затвори");
 	    resources.put("select",                      "Избери");
 	    resources.put("reset",                       "Ресет");
 	    resources.put("cancel",                      "Отказ");
 	    resources.put("back",                        "Назад");
 	    resources.put("save",                        "Запази");
 	    resources.put("ok",                          "OK");
 	    resources.put("send",                        "Изпрати");
 	    resources.put("next",                        "Нататък");
 	    // #sijapp cond.end#
 	 
		resources.put(".",                           ".");
		resources.put("about",                       "Относно");
		resources.put("about_info",                  "Jimm - Мобилен ICQ\n\nICQ клиент за J2ME\n\nВерсия: ###VERSION###\n За: ###TARGET###\nМодули: ###MODULES###\n\nВиж http://www.jimm.org/");
		resources.put("account",                     "Акаунт");
		resources.put("add_group",                   "Добави група");
		resources.put("add_user",                    "Добави Потребител");
		resources.put("add_to_list",                 "Добави в листа");
		resources.put("age",                         "Год.");
		resources.put("async",                       "Асинхр.");
		resources.put("attention",                   "Внимание!");
		resources.put("auth",				     	 "Оторизация");
		resources.put("auto_connect",                "Автоматично свързване при старт");
		resources.put("beep",                        "Beep");
		resources.put("black_on_white",              "Черно-бяло");
		resources.put("byte",                        "Байт");
		resources.put("chat_small_font",             "Малки шрифтове за чат");
		resources.put("city",                        "Град");
		resources.put("clear",                       "Изчисти");
 	    resources.put("color_scheme",                "Цветова схема");
		resources.put("conn_type",                   "Тип на връзката");
	
		resources.put("connect",                     "Свържи");
		resources.put("connecting",                  "Свързване");
		resources.put("cost",                        "Цена");
		resources.put("contact_list",                "Лист");
		resources.put("cp1251",                      "Използвай CP1251 hack?");
		resources.put("cpd",                         "Цена за ден");
		resources.put("cpp",                         "Цена за пакет");
		resources.put("currency",                    "Валута");
		resources.put("del_group",                   "Изтрий група");
		resources.put("delete_chat",                 "Изтрий чат");
		resources.put("deny",						 "Откажи оторизация");
		resources.put("denyedby",					 "Беше ви отказана оторизация от: ");
		resources.put("description",                 "Описание");
		resources.put("name_desc",                   "Име на файл и описание");
		resources.put("user_menu",                   "Потр. Меню");
		resources.put("disconnect",                  "Прекъсни");
		resources.put("disconnecting",               "Прекъсване");
		
		resources.put("display_date",                "Изписвай дата на нов екран?");
		resources.put("email",                       "Email");
		resources.put("error",                       "Грешка");
		resources.put("exec",                        "Изпълни");
		resources.put("exit",                        "Изход");
		resources.put("female",                      "Ж");
		resources.put("filetransfer",                "Файлов трансфер");
		resources.put("filepath",                    "Път до файл");
		resources.put("find",                        "Намери");
		resources.put("firstname",                   "Първо име");
		resources.put("free_heap",                   "Освободи паметта");
		resources.put("ft_name",                     "Изпрати файл");		
		resources.put("ft_cam",                      "Изпрати образ от камера");		
		resources.put("gender",                      "Пол");
		resources.put("grant",						 "Потвърди оторизация");
		resources.put("grantedby",					 "Оторизацията потвърдена от:");
		resources.put("group_name",                  "Име на група");
 	    resources.put("group_is_not_empty",          "Избраната група не е празна!\nПреместете всички потребители в други групи!");
 	    resources.put("have_unread_mess",            "Имате непрочетени съобщения. Да изляза ли?");
		resources.put("hide_offline",                "Скрий офлайн контактите");
		resources.put("info",                        "Инфо");
		resources.put("init_ft",                     "Инициализиране");
		resources.put("kb",                          "kB");
		resources.put("kbs",                         "kb/s");
		resources.put("keep_chat",                   "Пази чат историята?");
		resources.put("keep_conn_alive",             "Дръж връзката жива?");
		resources.put("keylock",                     "Keylock");
 	    resources.put("keylock_message",             "Задръжте \"#\" за да изключите keylock-а");
		resources.put("keylock_enable",              "Заключи клавиатура");
		resources.put("keylock_enabled",             "Клавиатура заключена");
		resources.put("keyword",                     "Ключова дума");
		resources.put("language",                    "Език");
		resources.put("lang_BG", 					 "Български");
		resources.put("lang_BR",                     "Португалски (Бразилия)");
		resources.put("lang_CZ",                     "Чешки");
		resources.put("lang_DE",                     "Немски");
		resources.put("lang_EN",                     "Англйиски");
		resources.put("lang_ES",                     "Испански");
		resources.put("lang_HE", 					 "Еврейски");
		resources.put("lang_IT",                     "Италиански");
		resources.put("lang_LT",                     "Литовски");
		resources.put("lang_PL",                     "Полски");
		resources.put("lang_RU",                     "Руски");
		resources.put("lang_SE",                     "Шведски");
		resources.put("lang_SR",                     "Сръбски");
		resources.put("lang_UA",                     "Украински");

		resources.put("lastname",                    "Фамилия");
		resources.put("loading",                     "Зареждане");
		resources.put("male",                        "M");
		resources.put("me",                          "Аз");
		resources.put("menu",                        "Меню");
		
		resources.put("message_notification",        "Нотификация при съобщение");
		resources.put("msg_sound_file_name",         "Звук за съобщение");
		resources.put("message",                     "Съобщение");
		resources.put("message_from",                "Съобщение от");
		resources.put("minimize",                    "Минимизирай");
		resources.put("name",                        "Име");
		
		resources.put("nick",                        "Псевдоним");
		resources.put("no",                          "Не");
		resources.put("not",                         "не");
		resources.put("no_results",                  "Няма резултати");
		resources.put("no_not_empty_gr",             "Премахването на непразни групи все още не се поддържа");
		resources.put("not_implemented",             "Тази функция още не е включена.");
		resources.put("noreason",             		 "Не е зададена причина.");
		resources.put("notice",                      "Нотификация");
		resources.put("nr",				     		 "Nr");
		
		resources.put("once_a_session",              "Веднъж на сесия");
		resources.put("onl_notification",            "Нотификация за входящ контакт");
		resources.put("onl_sound_file_name",         "Звук за 'онлайн'");
		resources.put("only_online",                 "Показвай само онлайн контакти");
		resources.put("options",                     "Опции");
		resources.put("options_account",             "Акаунт");
		resources.put("options_cost",                "Цена");
		resources.put("options_effect",              "Трябва да се свържете наново, за да могат някой промени да се активират!");
		resources.put("options_interface",           "Интерфейс");
		resources.put("options_network",             "Мрежа");
		resources.put("options_other",               "Други");
		resources.put("options_signaling",           "Сигнализиране");
		resources.put("password",                    "Парола");
		resources.put("plength",                     "Дължина на пакет данни в in kB");
		resources.put("plsauthme",                   "Hi! Please authorise my request to add you to my contact list.");
		resources.put("prev",						 "Предишен");
		resources.put("reason",						 "Причина");
		resources.put("remove",                      "Изтрий от листа");
		resources.put("remove_group",                "Изтрий група");
 	    resources.put("remove_user",                 "Изтрий потребител");
		resources.put("rename",                      "Преименувай");
		
		resources.put("requauth",                    "Поискай отор.");
		resources.put("requ",                        "Изискван(и)");
		resources.put("requno",                      "Неизискван(и)");
		
		resources.put("res",                         "Резолюция");
		resources.put("results",                     "Резултати");
		
		resources.put("search_user",                 "Търси потребител");
		
		
		resources.put("send_img",                    "Прати картинка");
		resources.put("send_message",                "Ново съобщение");		
		resources.put("send_url",                    "Нов URL");
		resources.put("server",                      "Login сървър");
		resources.put("server_host",                 "Име");
		resources.put("server_port",                 "Порт");
		resources.put("session",                     "Сесия");
		resources.put("set_status",                  "Промени статус");
		resources.put("shadow_con",                  "Shadow connection");
 	    resources.put("show_user_groups",            "Показвай групи");
		resources.put("since",                       "От");
		resources.put("size",                        "Размер");
		resources.put("sound",                       "Звуков файл");
		resources.put("sound_file_name",             "Име на звуковия файл");
		resources.put("sort_by",                     "Подреди контакт листа");
		resources.put("sort_by_name",                "По имена");
		resources.put("sort_by_status",              "По статус");
		resources.put("speed",                       "Скорост");
		resources.put("status",                      "Статус");
		resources.put("status_away",                 "Ауей");
		resources.put("status_chat",                 "Свободен за чат");
		resources.put("status_dnd",                  "Не ме притеснявай");
		resources.put("status_invisible",            "Невидим");
		resources.put("status_na",                   "Няма ме");
		resources.put("status_occupied",             "Зает");
		resources.put("status_offline",              "Офлайн");
		resources.put("status_online",               "Онлайн");
		resources.put("successful",                  "успешен");
		resources.put("sysnotice",					 "Системна нотификация");
		resources.put("traffic",                     "Трафик");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("use_history",                 "Пази история");
		resources.put("user_add",                    "Добави потр.");
		resources.put("user_search",                 "Търси потребител");
		resources.put("vibration",                   "Вибрация");
		resources.put("viewfinder",                  "Viewfinder");
		resources.put("volume",                      "Сила");		
		resources.put("wait",                        "Моля изчакайте ...");
		resources.put("warning",                     "Предупреждение");
		resources.put("wantsyourauth",				 " иска вашата оторизация. Причина: ");
		resources.put("was",                         "бе");
		resources.put("whichgroup",				 	 "В коя група?");
		resources.put("white_on_black",              "Бяло на черен фон");
 	    resources.put("white_on_blue",               "Бяло на син фон");
		resources.put("yes",                         "Да");
		resources.put("youwereadded",				 "Бяхте добавен от UIN: ");
		
		
		
		
		
		//#sijapp cond.if modules_HISTORY is "true" #
		resources.put("text_to_find",                "Текст");
		resources.put("find_backwards",              "Назад");
		resources.put("find_case_sensitiv",          "Case sensitive");
		resources.put("history_info",                "Информация за историята");
		resources.put("hist_cur",                    "Текущ брой съобщения от контакта");
		resources.put("hist_rc",                     "Общ брой съобщения");
		resources.put("hist_size",                   "Използвано място (kB)");
		resources.put("hist_avail",                  "Място (kB)");
 	    resources.put("history",                     "История");
 	    resources.put("not_found",                   "не е намерен(а)");
 	    //#sijapp cond.end#
		
		// Generic errors
		resources.put("error_100",                   "Незнайна грешка (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "Много връзки от един UIN (#110.EXT)");
		resources.put("error_111",                   "Грешна парола (#111.EXT)");
		resources.put("error_112",                   "Несъществуващ UIN (#112.EXT)");
		resources.put("error_113",                   "Много клиенти от едно IP (#113.EXT)");
		resources.put("error_114",                   "Надвишен размер (#114.EXT)");
		resources.put("error_115",                   "Не мога да заредя контакт листа (#115.EXT)");
		resources.put("error_116",                   "Не мога да заредя офлайн съобщението (#116.EXT)");
		resources.put("error_117",                   "Невъведен UIN и/или парола (#117.EXT)");
		resources.put("error_118",                   "Сървърът не отговаря (#118.EXT)");

		// Network communication specific exceptions
		// Connection to server
		resources.put("error_120",                   "I/O грешка (#120.EXT)");
		resources.put("error_121",                   "Не мога да установя TCP връзката (#121.EXT)");
		resources.put("error_122",                   "Грешно въведени име и/или порт на сървъра (#122.EXT)");
		resources.put("error_123",                   "Връзката не бе установена (#123.EXT)");
		resources.put("error_124",                   "Не мога да синхронизирам входящия поток (#124.EXT)");
		
		// Peer connection
		resources.put("error_125",                   "I/O грешка (#125.EXT)");
		resources.put("error_126",                   "Не мога да установя TCP връзката (#126.EXT)");
		resources.put("error_127",                   "Грешно въведени име и/или порт на сървъра (#127.EXT)");
		resources.put("error_128",                   "Връзката не бе установена (#128.EXT)");
		resources.put("error_129",                   "Не мога да синхронизирам входящия поток (#129.EXT)");

		// Parsing specific errors
		resources.put("error_130",                   "FLAP хедъра не може да бъде зареден (#130.EXT)");
		resources.put("error_131",                   "Непознат канал (#131.EXT)");
		resources.put("error_132",                   "'Connect' канален пакет не може да бъде зареден (#132.EXT)");
		resources.put("error_133",                   "SNAC хедъра не може да бъде зареден (#133.EXT)");
		resources.put("error_134",                   "'Error' канален пакет за грешки не може да бъде зареден (#134.EXT)");
		resources.put("error_135",                   "'Disconnect' канален пакет не може да бъде зареденd (#135.EXT)");
		resources.put("error_136",                   "'Ping' канален пакет не може да бъде зареден (#136.EXT)");
		resources.put("error_137",                   "Старият ICQ протколен хедър не може да бъде зареден(#137.EXT)");

		// Action errors
		resources.put("error_140",                   "Заявеното действие не може да бъде изпълнено в момента (#140.EXT)");

		// Specific action errors
		resources.put("error_150",                   "Полученото съобщение не може да бъде разчетено (#150.EXT)");
		resources.put("error_151",                   "Полученото съобщение тип 1 не може да бъде разчетено (#151.EXT)");
		resources.put("error_152",                   "Полученото съобщение тип 2 не може да бъде разчетено (#152.EXT)");
		resources.put("error_153",                   "Полученото съобщение тип 4 не може да бъде разчетено (#153.EXT)");
		resources.put("error_154",                   "Грешка при ъпдейта на контакт листа ви (#154.EXT)");
		resources.put("error_155",                   "Обектът вече е в сървър листа ви (#155.EXT)");
		resources.put("error_156",                   "Грешка при добавянето. Опитайте отново (#156.EXT)");
		resources.put("error_157",                   "Не се допускат повече елементи от този тип (#157.EXT)");
		resources.put("error_158",                   "Опитахте се да добавите ICQ контакт към AIM лист (#158.EXT)");
		resources.put("error_159",                   "Сървърът не отговаря на търсената заявка. Опитайте отново (#159.EXT)");
		resources.put("error_160",                   "Грешка при търсене (#160.EXT)");
		resources.put("error_161",                   "Няма намерени групи. Моля добавете група (#161.EXT)");

		// Other errors
		resources.put("error_170",                   "Вероятна недостатъчност на сумарна памет (#170.EXT)");
		resources.put("error_171",                   "Не мога да заредя мета информацията (#171.EXT)");
		
		// Camera errors
		resources.put("error_180",                   "Грешка при създаването на VideoControl (#180.EXT)");
		resources.put("error_181",                   "Грешка при инизиализирането на Viewfinder (#181.EXT)");
		resources.put("error_182",                   "Грешка при стартирането на Viewfinder (#182.EXT)");
		resources.put("error_183",                   "Грешка при снимането (#183.EXT)");
		resources.put("error_185",                   "Не можете да снимате (#185.EXT)");
		
		// File transfer errors
		resources.put("error_190",                   "Трансферът на файлове с < ICQv8 клиенти не се поддържа (#190.EXT)");
		resources.put("error_191",                   "Грешка при четенето на файла. Няма поддръжка за него (#191.EXT)");
		resources.put("error_192",                   "Грешка при четенето на файла. Файлът не  енамерен или не се поддържа (#192.EXT)");
 	    resources.put("error_193",                   "Грешка при достъпа до файловата система. Грешка по сигурноста. (#193.EXT)");

	}
}


//#sijapp cond.end #

