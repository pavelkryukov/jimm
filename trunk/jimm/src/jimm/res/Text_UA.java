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
 File: src/jimm/res/Text_UA.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Nazar Pelenjo
 *******************************************************************************/

//#sijapp cond.if lang_UA is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_UA extends ResourceBundle
{
	public Text_UA()
	{
		// Labels
		resources.put(".",                           ".");
		resources.put("about",                       "Про...");
		resources.put("about_info",                  "Jimm - Мобiльний ICQ\n\nICQ Клiєнт для J2ME\nВерсiя 0.3.1\n http://www.jimm.org/");
		resources.put("account",                     "Облiковий запис");
		resources.put("add_user",                    "Додати користувача");
		resources.put("add_to_list",                 "Додати");
		resources.put("age",                         "Вiк");
		resources.put("async",                       "Асинхронна передача");
		resources.put("auth",				     	 "Авторизацiя");
		resources.put("back",                        "Назад");
		resources.put("beep",                        "Гудок");
		resources.put("byte",                        "Байт");
		resources.put("cancel",                      "Скасувати");
		resources.put("city",                        "Мiсто");
		resources.put("close",                       "Закрити");
		resources.put("conn_type",                   "Тип з'єднання");
		resources.put("con_wait",                    "Затримка мiж з'єднаннями (секунд)");
		resources.put("connect",                     "З'єднатися");
		resources.put("connecting",                  "З'єднання");
		resources.put("contact_list",                "Список");
		resources.put("cp1251",                      "Виправляти помилку CP1251?");
		resources.put("cpd",                         "Вартiсть в день");
		resources.put("cpp",                         "Вартiсть пакету");
		resources.put("currency",                    "Валюта");
		resources.put("del_group",                   "Знищити группу");
		resources.put("delete_chat",                 "Знищити чат");

		resources.put("deny",						 "Вiдхилити авторизацiю");
		resources.put("denyedby",					 "Ваш запит на авторизацiю вiдхилив: ");
		resources.put("description",                 "Опис");
		resources.put("name_desc",                   "Iм'я файлу та опис");
		resources.put("user_menu",                   "Меню користувача");
		resources.put("disconnect",                  "Вiдхилити");
		resources.put("disconnecting",               "Вiд'єднання");
		resources.put("display_advertisement",       "Додати пiдпис Jimm?");
		resources.put("display_date",                "Вiдображати дату на заставцi?");
		resources.put("email",                       "E-mail");
		resources.put("error",                       "Помилка");
		resources.put("exit",                        "Вихiд");
		resources.put("female",                      "Ж");
		
		resources.put("filename",                    "Iм'я файлу");
		resources.put("filetransfer",                "Передача файлу");
		resources.put("filepath",                    "Шлях до файлу");
		resources.put("ft_name",                     "Послати файл");		
		resources.put("ft_cam",                      "Послати фото з камери");		
		
		resources.put("firstname",                   "Iм'я");
		resources.put("gender",                      "Стать");
		resources.put("grant",						 "Авторизувати");
		resources.put("grantedby",					 "Вас авторизував: ");
		resources.put("hide_offline",                "Приховати вiд'єднаних");
		resources.put("info",                        "Iнформацiя");
		resources.put("init_ft",                     "Iнiцiалiзацiя...");
		resources.put("kb",                          "КБ");
		resources.put("keep_chat",                   "Зберiгати iсторiю чату?");
		resources.put("keep_conn_alive",             "Пiдтримувати з'єднання?");
		resources.put("keylock_enable",              "Блокування клавiатури");
		resources.put("keylock_enabled",             "Блокування увiмкнене");
		resources.put("keyword",                     "Ключевое слово");
		resources.put("language",                    "Мова");
		resources.put("lang_BR",                     "Португальска (Бразилiя)");
		resources.put("lang_CZ",                     "Чеська");
		resources.put("lang_DE",                     "Нiмецька");
		resources.put("lang_EN",                     "Англiйська");
		resources.put("lang_ES",                     "Iспанска");
		resources.put("lang_IT",                     "Iталiйська");
		resources.put("lang_LT",                     "Литовська");
		resources.put("lang_RU",                     "Росiйська");
		resources.put("lang_SE",                     "Шведська");
		resources.put("lang_SR",                     "Сербська");
		resources.put("lang_BG", 					 "Болгарська");
		resources.put("lastname",                    "Прiзвище");
		resources.put("loading",                     "Завантаження");
		resources.put("male",                        "Ч");
		resources.put("me",                          "Я");
		resources.put("menu",                        "Меню");
		resources.put("msg_sound_file_name",         "Файл звуку звiсточки");
		resources.put("message_notification",        "Повiдомлення про звiсточку");
		resources.put("message",                     "Звiсточка");
		resources.put("message_from",                "Звiсточка вiд");
		resources.put("name",                        "Iм'я");
		resources.put("next",						 "Далi");
		resources.put("nick",                        "Нiк");
		resources.put("no",                          "Нi");
		resources.put("no_results",                  "Немає результатiв");
		resources.put("no_not_empty_gr",             "Знищення не порожнiх групп не пiдтримується");
		resources.put("not_implemented",             "Функцiя не пiдтримується");
		resources.put("noreason",             		 "Причина не дана");
		resources.put("notice",                      "Повiдомлення");
		resources.put("nr",				     		 "Номер");
		resources.put("ok",                          "OK");
		resources.put("once_a_session",              "Один раз за сесiю");
		resources.put("onl_notification",            "Iнформувати про пiд'єднанi контакти");
		resources.put("onl_sound_file_name",         "Файл звуку пiд'єднання");
		resources.put("only_online",                 "Показувати лише онлайн контакти");
		resources.put("options",                     "Налаштування");
		resources.put("options_account",             "Облiковий запис");
		resources.put("options_cost",                "Вартiсть");
		resources.put("options_effect",              "Вам необхiдно перез'єднатися для застосування змiн");
		resources.put("options_interface",           "Iнтерфейс");
		resources.put("options_network",             "Мережа");
		resources.put("options_other",               "Рiзне");
		resources.put("options_signaling",           "Звук");
		resources.put("password",                    "Пароль");
		resources.put("plength",                     "Довжина оплачуваного пакету в КБ");
		resources.put("plsauthme",                   "Привiт! Будь-ласка, авторизируй мене та дозволь добавити тебе в контакт-лист");
		resources.put("prev",						 "Попереднiй");
		resources.put("reason",						 "Причина");
		resources.put("remove",                      "Знищити зi списку");
		resources.put("rename",                      "Перейменувати");
		resources.put("reply",                       "Вiдповiдь");
		resources.put("requauth",                    "Запит авторизацiї");
		resources.put("requ",                        "Вимагається");
		resources.put("requno",                      "Не вимагається");
		resources.put("reset",                       "Скинути");
		resources.put("res",                         "Дозвiл");
		resources.put("results",                     "Результати");
		resources.put("save",                        "Зберегти");
		resources.put("search_user",                 "Знайти користувача");
		resources.put("send",                        "Надiслати");
		resources.put("send_message",                "Нова звiсточка");
		resources.put("send_img",                    "Надiслати фото");
		resources.put("send_url",                    "Новий URL");
		resources.put("server",                      "Сервер");
		resources.put("server_host",                 "Iм'я серверу");
		resources.put("server_port",                 "Порт");
		resources.put("session",                     "Сесiя");
		resources.put("set_status",                  "Встановити статус");
		resources.put("since",                       "Починаюи з");
		resources.put("sound",                       "Звуковий файл");
		resources.put("sort_by",                     "Сортувати контакт-лист");
		resources.put("sort_by_name",                "По iменi");
		resources.put("sort_by_status",              "По статусу");
		resources.put("status",                      "Статус");
		resources.put("status_away",                 "Вiдсутнiй");
		resources.put("status_chat",                 "Вiльний для чату");
		resources.put("status_dnd",                  "Не турбувати");
		resources.put("status_invisible",            "Невидимий");
		resources.put("status_na",                   "Недоступний");
		resources.put("status_occupied",             "Зайнятий");
		resources.put("status_offline",              "Оффлайн");
		resources.put("status_online",               "На дротi");
		resources.put("sysnotice",					 "Системне повiдомлення");
		resources.put("traffic",                     "Трафiк");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("user_add",                    "Додати користувача");
		resources.put("user_search",                 "Знайти користувача");
		resources.put("vibration",                   "Вiбрацiя");
		resources.put("viewfinder",                  "Видошукач");
		resources.put("volume",                      "Гучнiсть");
		resources.put("wait",                        "Будь-ласка, чекайте ...");
		resources.put("warning",                     "Попередження");
		resources.put("wantsyourauth",				 " хоче авторизуватися. Причина: ");
		resources.put("whichgroup",				 	 "В якiй групi?");
		resources.put("yes",                         "Так");
		resources.put("youwereadded",				 "Вас добавив: ");
		resources.put("chat_small_font",             "Дрiбний шрифт в чатi");
		resources.put("select",                      "Вибрати");
		
		resources.put("attention",                   "Увага!"); 
		resources.put("have_unread_mess",            "У Вас залишились непрочитанi звiсточки. Вийти з програми?");
		
		// User management **********
		resources.put("add_group",                   "Добавити групу");
		resources.put("group_name",                  "I'мя групи");
		resources.put("exec",                        "Виконати");
		resources.put("remove_user",                 "Знищити користувача");
		resources.put("show_user_groups",            "Групувати контакти");

		// Generic errors
		resources.put("error_100",                   "Невiдома помилка (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "Множинний вхiд iз тим же UIN (#110.EXT)");
		resources.put("error_111",                   "Невiрний пароль (#111.EXT)");
		resources.put("error_112",                   "Неiснуючий UIN (#112.EXT)");
		resources.put("error_113",                   "Надто багато клiєнтiв з одного IP (#113.EXT)");
		resources.put("error_114",                   "Спроби вичерпанi (#114.EXT)");
		resources.put("error_115",                   "Контакт-лист не може бути оброблений (#115.EXT)");
		resources.put("error_116",                   "Оффлайн звiсточка не може бути оброблена (#116.EXT)");
		resources.put("error_117",                   "Немає UIN та/або паролю (#117.EXT)");
		resources.put("error_118",                   "Сервер не вiдповiдає (#118.EXT)");

		// Network communication specific exceptions
		resources.put("error_120",                   "Виникла помилка вводу-виводу (#120.EXT)");
		resources.put("error_121",                   "Необхiдне TCP з'єднання неможливе (#121.EXT)");
		resources.put("error_122",                   "Вказанний сервер та/або порт некорректнi (#122.EXT)");
		resources.put("error_123",                   "З'єднання не може бути встановлено (#123.EXT)");
		resources.put("error_124",                   "Вхiдний потiк не синхронiзований (#124.EXT)");
		
		// Parsing specific errors
		resources.put("error_130",                   "FLAP заголовок не оброблений (#130.EXT)");
		resources.put("error_131",                   "Невiдомий канал (#131.EXT)");
		resources.put("error_132",                   "Пакет пiд'єднаного каналу не оброблений (#132.EXT)");
		resources.put("error_133",                   "SNAC заголовок не оброблений (#133.EXT)");
		resources.put("error_134",                   "Помилка пакету каналу не оброблена (#134.EXT)");
		resources.put("error_135",                   "Пакет вiд'єднаного каналу не оброблений (#135.EXT)");
		resources.put("error_136",                   "Пакет каналу ping не оброблений (#136.EXT)");
		resources.put("error_137",                   "Заголовок старого протоколу ICQ не оброблений (#137.EXT)");

		// Action errors
		resources.put("error_140",                   "Необхiдна дiя не може бути виконана в даний час (#140.EXT)");

		// Specific action errors
		resources.put("error_150",                   "Отримана звiсточка не оброблена (#150.EXT)");
		resources.put("error_151",                   "Отримана звiсточка 1 типу не оброблена (#151.EXT)");
		resources.put("error_152",                   "Отримана звiсточка 2 типу не оброблена (#152.EXT)");
		resources.put("error_153",                   "Отримана звiсточка 4 типу не оброблена (#153.EXT)");
		resources.put("error_154",                   "Помилка обновлення списку користувачiв (#154.EXT)");
		resources.put("error_155",                   "Об\"єкт вже знаходиться в списку на серверi (#155.EXT)");
		resources.put("error_156",                   "Помилка добавлення. Спробуйте ще раз (#156.EXT)");
		resources.put("error_157",                   "Не дозволено бiльше елементiв цього типу (#157.EXT)");
		resources.put("error_158",                   "Ви зробили спробу добавити користувача ICQ в спиок AIM (#158.EXT)");
		resources.put("error_159",                   "Сервер не вiдповiдає на запит пошуку. Спробуйте ще раз (#159.EXT)");
		resources.put("error_160",                   "Помилка пошуку (#160.EXT)");
		resources.put("error_161",                   "У Вас немає жодної групи в списку. Будь-ласка, створiть нову групу (#161.EXT)");

		// Other errors
		resources.put("error_170",                   "Неможливо, недостатньо пам'ятi (#160.EXT)");
		resources.put("error_171",                   "Неможливо отримати мета-iнформацiю (#161.EXT)");

		
		// Camera errors
		resources.put("error_180",                   "Помилка створення об'єкту 'VideoControl' (#180.EXT)");
        resources.put("error_181",                   "Помилка iнiцiалiзацiї видошукача (#181.EXT)");
        resources.put("error_182",                   "Помилка запуску видошукача (#182.EXT)");
        resources.put("error_183",                   "Помилка пiд час отримання фото (#183.EXT)");
        resources.put("error_185",                   "Фотозйомка не пiдтримуєтся (#185.EXT)");

        // File transfer errors
        resources.put("error_190",                   "Передача файлiв не пiдтримуєтся для клiєнтiв ICQ нижче 8-ої версiї (#190.EXT)");
        resources.put("error_191",                   "Помилка читання файлу. Можливо, дана функцiя не пiдтримуєтся (#191.EXT)");
        resources.put("error_192",                   "Некоректний шлях до файлу. Можливо, дана функцiя не пiдтримуєтся (#192.EXT)");		
	}

}


// #sijapp cond.end #
