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
 File: src/jimm/res/Text_RU.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher, Andrey B. Ivlev
 *******************************************************************************/


// #sijapp cond.if lang_RU is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_RU extends ResourceBundle
{


	public Text_RU()
	{

		// Labels
		resources.put(".",                           ".");
		resources.put("about",                       "О программе");
		resources.put("about_info",                  "Jimm - Мобильный ICQ\nICQ клиент для J2ME\n\nВерсия ###VERSION###\n http://jimm.sourceforge.net/");
		resources.put("account",                     "Пользователь");
		resources.put("add_user",                    "Добавить пользователя");
		resources.put("async",                       "Async");
		resources.put("back",                        "Назад");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("beep",                        "Сигнал");
		// #sijapp cond.end #
		resources.put("byte",                        "Byte");
		resources.put("cancel",                      "Отмена");
		resources.put("close",                       "Закрыть");
		resources.put("conn_type",                   "Connection type");
		resources.put("connect",                     "Подключиться");
		resources.put("connecting",                  "Подключение");
		resources.put("contact_list",                "Контакты");
		resources.put("cp1251",                      "Use CP1252 hack?");
		resources.put("cpd",                         "Стоимость за день");
		resources.put("cpp",                         "Стоимость пакета");
		resources.put("currency",                    "Валюта");
		resources.put("delete_chat",                 "Удалить разговоры");
		resources.put("user_menu",                   "Меню пользователя");
		resources.put("disconnect",                  "Отключиться");
		resources.put("disconnecting",               "Отключение");
		resources.put("display_advertisement",       "Добавить подпись Jimm?");
		resources.put("display_date",                "Показывать дату на заставке?");
		resources.put("email",                       "Почта");
		resources.put("error",                       "Ошибка");
		resources.put("exit",                        "Выход");
		resources.put("hide_offline",                "Прятать отключившихся");
		resources.put("info",                        "Информация");
		resources.put("kb",                          "kB");
		resources.put("keep_chat",                   "Keep chat histroy?");
		resources.put("keep_conn_alive",             "Поддерживать соединение?");
		resources.put("keylock_enable",              "Включить блокировку клавиатуры");
		resources.put("keylock_enabled",             "Блокировка клавиатуры включена");
		resources.put("language",                    "Язык");
		resources.put("lang_BR",                     "Португальский (Бразилия)");
		resources.put("lang_CZ",                     "Чешском");
		resources.put("lang_DE",                     "Немецкий");
		resources.put("lang_EN",                     "Английский");
		resources.put("lang_ES",                     "Испанский");
		resources.put("lang_LT",                     "Литовском");
		resources.put("lang_RU",                     "Русский");
		resources.put("lang_SE",                     "Шведском");
		resources.put("loading",                     "Загрузка");
		resources.put("me",                          "Я");
		resources.put("menu",                        "Меню");
		resources.put("message",                     "Сообщение");
		resources.put("message_from",                "Сообщение от");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("message_notification",        "Уведомление");
		// #sijapp cond.end #
		resources.put("name",                        "Имя");
		resources.put("nick",                        "Псевдоним");
		resources.put("no",                          "Нет");
		resources.put("not_implemented",             "Функция не поддерживается.");
		resources.put("notice",                      "Замечание");
		resources.put("ok",                          "OK");
		resources.put("once_a_session",              "Один раз за сессию");
		resources.put("options",                     "Настройки");
		resources.put("options_account",             "Учетная запись");
		resources.put("options_cost",                "Стоимость");
		resources.put("options_effect",              "Изменения вступят в силу после переподключения!");
		resources.put("options_interface",           "Интерфейс");
		resources.put("options_network",             "Сеть");
		resources.put("options_other",               "Другое");
		resources.put("password",                    "Пароль");
		resources.put("plength",                     "Длина оплачиваемого пакета в kB");
		resources.put("remove",                      "Удалить из списка");
		resources.put("reply",                       "Ответ");
		resources.put("reset",                       "Сброс");
		resources.put("save",                        "Сохрани");
		resources.put("search_user",                 "Поиск пользователя");
		resources.put("send",                        "Послать");
		resources.put("send_message",                "Новое сообщение");
		resources.put("send_url",                    "Новый URL");
		resources.put("server",                      "Начать сеанс");
		resources.put("server_host",                 "Имя сервера");
		resources.put("server_port",                 "Порт");
		resources.put("session",                     "Сессия");
		resources.put("set_status",                  "Установить состояние");
		resources.put("since",                       "После");
		resources.put("sound",                       "Звук ICQ");
		resources.put("sound_file_name",             "Sound File Name");
		resources.put("sort_by",                     "Сортировать контакты");
		resources.put("sort_by_name",                "По имени");
		resources.put("sort_by_status",              "По статусу");
		resources.put("status_away",                 "Отсутствую");
		resources.put("status_chat",                 "Свободен для беседы");
		resources.put("status_dnd",                  "Не беспокоить");
		resources.put("status_invisible",            "Невидимый");
		resources.put("status_na",                   "Недоступен");
		resources.put("status_occupied",             "Занят");
		resources.put("status_offline",              "Отключен");
		resources.put("status_online",               "В сети");
		resources.put("traffic",                     "Тарификация");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("user_add",                    "Добавить пользователя");
		resources.put("user_search",                 "Поиск пользователя");
		// #sijapp cond.if target is "SIEMENS" #
		resources.put("vibration",                   "Вибрация");
		// #sijapp cond.end #
		resources.put("wait",                        "Подождитите пожалуйста ...");
		resources.put("warning",                     "Предупреждение");
		resources.put("yes",                         "Да");

		// Generic errors
		resources.put("error_100", "Неизвестная ошибка (#100.EXT)");

		// Login specific errors
		resources.put("error_110",                   "Множественный вход с тем же UIN (#110.EXT)");
		resources.put("error_111",                   "Ошибка пароля (#111.EXT)");
		resources.put("error_112",                   "Несуществующий UIN (#112.EXT)");
		resources.put("error_113",                   "Слишком много клиентов с одного IP (#113.EXT)");
		resources.put("error_114",                   "Попытки исчерпаны (#114.EXT)");
		resources.put("error_115",                   "Контакты не разобраны (#115.EXT)");
		resources.put("error_116",                   "Сообщение во время отключения не разобрано (#116.EXT)");
		resources.put("error_117",                   "Пустые UIN и/или пароль (#117.EXT)");
		resources.put("error_118",                   "Нет ответа от сервера (#118.EXT)");

		// Network communication  specific exceptions
		resources.put("error_120",                   "Произошла ошибка ввода-вывода (#120.EXT)");
		resources.put("error_121",                   "Требуемое TCP соединение неосуществимо (#121.EXT)");
		resources.put("error_122",                   "Указанный сервер и/или порт некорректны (#122.EXT)");
		resources.put("error_123",                   "Соединение не может быть установлено (#123.EXT)");
		resources.put("error_124",                   "Входной поток не синхронизован (#124.EXT)");

		// Parsing specific errors
		resources.put("error_130",                   "FLAP заголовок не разобран (#130.EXT)");
		resources.put("error_131",                   "Неизвестный канал (#131.EXT)");
		resources.put("error_132",                   "Пакет подсоединенного канала не разобран (#132.EXT)");
		resources.put("error_133",                   "SNAC заголовок не разобран (#133.EXT)");
		resources.put("error_134",                   "Ошибка пакета канала не разобрана (#134.EXT)");
		resources.put("error_135",                   "Пакет отсоединенного канала не разобран (#135.EXT)");
		resources.put("error_136",                   "Пакет канала ping не разобран (#136.EXT)");
		resources.put("error_137",                   "Заголовок старого протокола ICQ не разобран (#137.EXT)");

		// Action errors
		resources.put("error_140",                   "Требуемое действие не может быть выполнено в данное время (#140.EXT)");

		// Specififc action errors
		resources.put("error_150",                   "Полученное сообщение не разобрано (#150.EXT)");
		resources.put("error_151",                   "Полученное сообщение 1 типа не разобрано (#151.EXT)");
		resources.put("error_152",                   "Полученное сообщение 2 типа не разобрано (#152.EXT)");
		resources.put("error_153",                   "Полученное сообщение 4 типа не разобрано (#153.EXT)");
		resources.put("error_154",                   "Ошибка обновления списка пользователей (#154.EXT)");

		// Other errors
		resources.put("error_160",                   "Возможно, недостаточно памяти (#160.EXT)");
		resources.put("error_161",                   "Невозможно получить мета информацию (#161.EXT)");

	}


}


// #sijapp cond.end #
