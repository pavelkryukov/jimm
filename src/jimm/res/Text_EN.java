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
 File: src/jimm/res/Text_EN.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


// #sijapp cond.if lang_EN is "true" #


package jimm.res;


import jimm.util.ResourceBundle;


public class Text_EN extends ResourceBundle
{


  public Text_EN()
  {

    // Labels
    resources.put(".",                           ".");
    resources.put("about",                       "About");
    resources.put("about_info",                  "Jimm - Mobile Messaging\n\nICQ client for J2ME\nVersion ###VERSION###\nSee http://www.jimm.org/");
    resources.put("account",                     "Account");
    resources.put("add_user",                    "Add User");
    resources.put("back",                        "Back");
    // #sijapp cond.if target is "SIEMENS" #
    resources.put("beep",                        "Beep");
    // #sijapp cond.end #
    resources.put("byte",                        "Byte");
    resources.put("cancel",                      "Cancel");
    resources.put("close",                       "Close");
    resources.put("connect",                     "Connect");
    resources.put("connecting",                  "Connecting");
    resources.put("contact_list",                "List");
    resources.put("cpd",                         "Cost per day");
    resources.put("cpp",                         "Cost per packet");
    resources.put("currency",                    "Currency");
    resources.put("delete_chat",                 "Delete chat");
    resources.put("user_menu",                   "User Menu");
    resources.put("disconnect",                  "Disconnect");
    resources.put("disconnecting",               "Disconnecting");
    resources.put("display_advertisement",       "Append Jimm tip?");
    resources.put("display_date",                "Display date on splash screen?");
    resources.put("email",                       "Email");
    resources.put("error",                       "Error");
    resources.put("exit",                        "Exit");
    resources.put("hide_offline",                "Hide offline contacts");
    resources.put("info",                        "Info");
	resources.put("keep_chat",					 "Keep chat histroy?");
    resources.put("kb",                          "kB");
    resources.put("keep_conn_alive",             "Keep connection alive?");
    resources.put("keylock_enable",              "Enable keylock");
    resources.put("keylock_enabled",             "Keylock enabled");
    resources.put("language",                    "Language");
    resources.put("lang_EN",                     "English");
    resources.put("lang_DE",                     "German");
    resources.put("lang_RU",                     "Russian");
    resources.put("lang_ES",                     "Spanish");
    resources.put("lang_BR",                     "Portuguese (Brazil)");
	resources.put("lang_CZ",                     "Czech");
	resources.put("lang_SE",                     "Swedish");
    resources.put("loading",                     "Loading");
    resources.put("me",                          "me");
    resources.put("menu",                        "Menu");
    resources.put("message",                     "Message");
    resources.put("message_from",                "Message from");
    // #sijapp cond.if target is "SIEMENS" #
    resources.put("message_notification",        "Message notification");
    // #sijapp cond.end #
    resources.put("name",                        "Name");
    resources.put("nick",                        "Nick");
    resources.put("no",                          "No");
    resources.put("not_implemented",             "Function not yet implemented.");
    resources.put("notice",                      "Notice");
    resources.put("ok",                          "OK");
    resources.put("once_a_session",              "Once a session");
    resources.put("options",                     "Options");
    resources.put("options_account",             "Account");
    resources.put("options_cost",                "Cost");
    resources.put("options_effect",              "You may need to reconnect for some changes to take effect!");
    resources.put("options_interface",           "Interface");
    resources.put("options_network",             "Network");
    resources.put("options_other",               "Other");
    resources.put("password",                    "Password");
    resources.put("plength",                     "Length of charge packet in kB");
    resources.put("remove",                      "Remove from List");
    resources.put("reply",                       "Reply");
    resources.put("reset",                       "Reset");
    resources.put("save",                        "Save");
    resources.put("search_user",                 "Search for User");
    resources.put("send",                        "Send");
    resources.put("send_message",                "New Message");
    resources.put("send_url",                    "New URL");
    resources.put("server",                      "Login server");
    resources.put("server_host",                 "Hostname");
    resources.put("server_port",                 "Port");
    resources.put("session",                     "Session");
    resources.put("set_status",                  "Set status");
    resources.put("since",                       "Since");
    resources.put("sound",                       "ICQ Sound");
    resources.put("sound_file_name",			 "Sound File Name");
    resources.put("sort_by",                     "Sort contact list");
    resources.put("sort_by_name",                "By name");
    resources.put("sort_by_status",              "By status");
    resources.put("status_away",                 "Away");
    resources.put("status_chat",                 "Free for Chat");
    resources.put("status_dnd",                  "Do Not Disturb");
    resources.put("status_invisible",            "Invisible");
    resources.put("status_na",                   "Not Available");
    resources.put("status_occupied",             "Occupied");
    resources.put("status_offline",              "Offline");
    resources.put("status_online",               "Online");
    resources.put("traffic",                     "Traffic");
    resources.put("uin",                         "UIN");
    resources.put("url",                         "URL");
    resources.put("user_add",                    "Add user");
    resources.put("user_search",                 "Search for user");
    // #sijapp cond.if target is "SIEMENS" #
    resources.put("vibration",                   "Vibration");
    // #sijapp cond.end #
    resources.put("wait",                        "Please wait ...");
    resources.put("warning",                     "Warning");
    resources.put("yes",                         "Yes");

    // Generic errors
    resources.put("error_100",                   "Unknown error (#100.EXT)");

    // Login specific errors
    resources.put("error_110",                   "Multiple logins on same UIN (#110.EXT)");
    resources.put("error_111",                   "Bad password (#111.EXT)");
    resources.put("error_112",                   "Non-existant UIN (#112.EXT)");
    resources.put("error_113",                   "Too many clients from same IP (#113.EXT)");
    resources.put("error_114",                   "Rate exceeded (#114.EXT)");
    resources.put("error_115",                   "Contact list could not be parsed (#115.EXT)");
    resources.put("error_116",                   "Offline message could not be parsed (#116.EXT)");
    resources.put("error_117",                   "Empty UIN and/or password (#117.EXT)");
    resources.put("error_118",                   "No response from server (#118.EXT)");

    // Network communication specific exceptions
    resources.put("error_120",                   "An I/O error occured (#120.EXT)");
    resources.put("error_121",                   "Requested TCP connection cannot be made (#121.EXT)");
    resources.put("error_122",                   "Specified server host and/or port is invalid (#122.EXT)");
    resources.put("error_123",                   "Connection has not been established (#123.EXT)");
    resources.put("error_124",                   "Input stream is out of sync (#124.EXT)");

    // Parsing specific errors
    resources.put("error_130",                   "FLAP header could not be parsed (#130.EXT)");
    resources.put("error_131",                   "Unknown channel (#131.EXT)");
    resources.put("error_132",                   "Connect channel packet  could not be parsed (#132.EXT)");
    resources.put("error_133",                   "SNAC header could not be parsed (#133.EXT)");
    resources.put("error_134",                   "Error channel packet could not be parsed (#134.EXT)");
    resources.put("error_135",                   "Disconnect channel packet could not be parsed (#135.EXT)");
    resources.put("error_136",                   "Ping channel packet could not be parsed (#136.EXT)");
    resources.put("error_137",                   "Old ICQ protocol header could not be parsed (#137.EXT)");

    // Action errors
    resources.put("error_140",                   "Requested action cannot be queued for execution at this time (#140.EXT)");

    // Specific action errors
    resources.put("error_150",                   "Received message could not be understood (#150.EXT)");
    resources.put("error_151",                   "Received type 1 message could not be understood (#151.EXT)");
    resources.put("error_152",                   "Received type 2 message could not be understood (#152.EXT)");
    resources.put("error_153",                   "Received type 4 message could not be understood (#153.EXT)");
    resources.put("error_154",                   "Contact list update failed (#154.EXT)");

    // Other errors
    resources.put("error_160",                   "Possibly not enough heap memory available (#160.EXT)");
    resources.put("error_161",                   "Could not fetch meta info (#161.EXT)");

  }


}


// #sijapp cond.end #
