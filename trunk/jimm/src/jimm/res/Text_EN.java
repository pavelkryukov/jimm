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


                                          //MOTOROLA formatted buttons
                                          // #sijapp cond.if target is "MOTOROLA"#
                                          resources.put("select",                     "Select    ");
                                          resources.put("reset",                       "     Reset");
                                          resources.put("cancel",                    "    Cancel");
                                          resources.put("back",                       "      Back");
                                          resources.put("exit_button",            "       Exit");
                                          resources.put("menu_button",        "      Menu");
                                          resources.put("save",                       "Save      ");
                                          resources.put("ok",                            "OK        ");
                                          resources.put("reply",                       "Reply    ");
                                           resources.put("close",                       "   Close");
                                           // #sijapp cond.else#
                                          resources.put("reply",                       "Reply");
                                          resources.put("close",                       "Close");
                                           resources.put("select",                      "Select");
                                           resources.put("reset",                       "Reset");
                                           resources.put("cancel",                        "Cancel");
                                           resources.put("back",                        "Back");
                                           resources.put("save",                        "Save");
                                           resources.put("ok",                          "OK");
		 // #sijapp cond.end#
                                           resources.put(".",                           ".");
		resources.put("about",                       "About");
		resources.put("about_info",                  "Jimm - Mobile Messaging\n\nICQ client for J2ME\nVersion 0.3.3\nSee http://www.jimm.org/");
		resources.put("account",                     "Account");
		resources.put("add_group",                   "Add group");
		resources.put("add_user",                    "Add User");
		resources.put("add_to_list",                 "Add to list");
		resources.put("age",                         "Age");
		resources.put("async",                       "Async");
		resources.put("attention",                   "Attention!");
		resources.put("auth",				     	 "Auth");
		resources.put("auto_connect",				 "Auto connect on startup");
		
		resources.put("beep",                        "Beep");
		resources.put("byte",                        "Byte");
		
		resources.put("chat_small_font",			 "Small font in chat");
		resources.put("city",                        "City");
		resources.put("clear",                       "Clear");
		
		resources.put("conn_type",                   "Connection type");
		resources.put("con_wait",                    "Delay between connections(sec)");
		resources.put("connect",                     "Connect");
		resources.put("connecting",                  "Connecting");
		resources.put("contact_list",                "List");
		resources.put("cp1251",                      "Use CP1251 hack?");
		resources.put("cpd",                         "Cost per day");
		resources.put("cpp",                         "Cost per packet");
		resources.put("currency",                    "Currency");
		resources.put("del_group",                   "Delete group");
		resources.put("delete_chat",                 "Delete chat");
		resources.put("deny",						 "Deny Auth");
		resources.put("denyedby",					 "Your auth request was denyed by: ");
		resources.put("description",                 "Description");
		resources.put("name_desc",                   "File name and description");
		resources.put("user_menu",                   "User Menu");
		resources.put("disconnect",                  "Disconnect");
		resources.put("disconnecting",               "Disconnecting");
		resources.put("display_date",                "Display date on splash screen?");
		resources.put("email",                       "Email");
		resources.put("error",                       "Error");
		resources.put("exec",                        "Execute");
		resources.put("exit",                        "Exit");
		resources.put("female",                      "F");
		resources.put("filetransfer",                "File transfer");
		resources.put("filepath",                    "File path");
		resources.put("find",                        "Find");
		resources.put("firstname",                   "First name");
		resources.put("free_heap",                   "Free heap");
		resources.put("ft_name",                     "Send file");		
		resources.put("ft_cam",                      "Send camera image");		
		resources.put("gender",                      "Gender");
		resources.put("grant",						 "Grant Auth");
		resources.put("grantedby",					 "Your auth request was granted by:");
		resources.put("group_name",                  "Group name");
		resources.put("group_is_not_empty",          "Selected group is not empty!\nMove all users to other groups!");
		resources.put("have_unread_mess",            "You have unread messages. Exit anyway?");
		resources.put("hide_offline",                "Hide offline contacts");
		resources.put("history_info",                "Storage info");
		resources.put("hist_cur",                    "Current contact messages number");
		resources.put("hist_rc",                     "Total messages number");
		resources.put("hist_size",                   "Used space (kB)");
		resources.put("hist_avail",                  "Total space (kB)"); 
		resources.put("history",                     "Stored history");
		resources.put("info",                        "Info");
		resources.put("init_ft",                     "Initializeing");
		resources.put("kb",                          "kB");
		resources.put("keep_chat",                   "Keep chat history?");
		resources.put("keep_conn_alive",             "Keep connection alive?");
		resources.put("keylock",                     "Keylock");
		resources.put("keylock_message",             "Hold \"#\" to disable keylock");
		resources.put("keylock_enable",              "Enable keylock");
		resources.put("keylock_enabled",             "Keylock enabled");
		resources.put("keyword",                     "Keyword");
		resources.put("language",                    "Language");
		resources.put("lang_BG", 					 "Bulgarian");
		resources.put("lang_BR",                     "Portuguese (Brazil)");
		resources.put("lang_CZ",                     "Czech");
		resources.put("lang_DE",                     "German");
		resources.put("lang_EN",                     "English");
		resources.put("lang_ES",                     "Spanish");
		resources.put("lang_HE", 					 "Hebrew");
		resources.put("lang_IT",                     "Italian");
		resources.put("lang_LT",                     "Lithuanian");
		resources.put("lang_PL",                     "Polish");
		resources.put("lang_RU",                     "Russian");
		resources.put("lang_SE",                     "Swedish");
		resources.put("lang_SR",                     "Serbian");		
		resources.put("lang_UA", 					 "Ukrainian");
		resources.put("lastname",                    "Last name");
		resources.put("loading",                     "Loading");
		resources.put("male",                        "M");
		resources.put("me",                          "me");
		resources.put("menu",                        "Menu");
		resources.put("message_notification",        "Message Notice");
		resources.put("msg_sound_file_name",         "Message Sound File");
		resources.put("message",                     "Message");
		resources.put("message_from",                "Message from");
		resources.put("name",                        "Name");
		resources.put("next",						 "Next");
		resources.put("nick",                        "Nick");
		resources.put("no",                          "No");
		resources.put("no_results",                  "No results");
		resources.put("no_not_empty_gr",             "Removing of not empty groups not supported yet");
		resources.put("not_implemented",             "Function not yet implemented.");
		resources.put("noreason",             		 "No reason was given.");
		resources.put("notice",                      "Notice");
		resources.put("nr",				     		 "Nr");
		
		resources.put("once_a_session",              "Once a session");
		resources.put("onl_notification",            "Notice for upcoming contact");
		resources.put("onl_sound_file_name",         "Online Sound File");
		resources.put("only_online",                 "Show only online contacts");
		resources.put("options",                     "Options");
		resources.put("options_account",             "Account");
		resources.put("options_cost",                "Cost");
		resources.put("options_effect",              "You may need to reconnect for some changes to take effect!");
		resources.put("options_interface",           "Interface");
		resources.put("options_network",             "Network");
		resources.put("options_other",               "Other");
		resources.put("options_signaling",           "Signaling");
		resources.put("password",                    "Password");
		resources.put("plength",                     "Length of charge packet in kB");
		resources.put("plsauthme",                   "Hi! Please authorise my request to add you to my contact list.");
		resources.put("prev",						 "Previous");
		resources.put("reason",						 "Reason");
		resources.put("remove",                      "Remove from List");
		resources.put("remove_group",                "Remove group");
		resources.put("remove_user",                 "Remove user");
		resources.put("rename",                      "Rename");
		
		resources.put("requauth",                    "Request auth");
		resources.put("requ",                        "Required");
		resources.put("requno",                      "Not req");
		resources.put("reset",                       "Reset");
		resources.put("res",                         "Resolution");
		resources.put("results",                     "Results");
		
		resources.put("search_user",                 "Search for User");
		
		resources.put("send",                        "Send");
		resources.put("send_img",                    "Send image");
		resources.put("send_message",                "New Message");
		resources.put("send_url",                    "New URL");
		resources.put("server",                      "Login server");
		resources.put("server_host",                 "Hostname");
		resources.put("server_port",                 "Port");
		resources.put("session",                     "Session");
		resources.put("set_status",                  "Set status");
		resources.put("show_user_groups",            "Show user groups");
		resources.put("since",                       "Since");
		resources.put("sound",                       "Sound file");
		resources.put("sound_file_name",             "Sound file name");
		resources.put("sort_by",                     "Sort contact list");
		resources.put("sort_by_name",                "By name");
		resources.put("sort_by_status",              "By status");
		resources.put("status",                      "Status");
		resources.put("status_away",                 "Away");
		resources.put("status_chat",                 "Free for Chat");
		resources.put("status_dnd",                  "Do Not Disturb");
		resources.put("status_invisible",            "Invisible");
		resources.put("status_na",                   "Not Available");
		resources.put("status_occupied",             "Occupied");
		resources.put("status_offline",              "Offline");
		resources.put("status_online",               "Online");
		resources.put("sysnotice",					 "System Notice");
		resources.put("traffic",                     "Traffic");
		resources.put("uin",                         "UIN");
		resources.put("url",                         "URL");
		resources.put("use_history",                 "Store history");
		resources.put("user_add",                    "Add user");
		resources.put("user_search",                 "Search for user");
		resources.put("vibration",                   "Vibration");
		resources.put("viewfinder",                  "Viewfinder");
		resources.put("volume",                      "Volume");		
		resources.put("wait",                        "Please wait ...");
		resources.put("warning",                     "Warning");
		resources.put("wantsyourauth",				 " wants your Authorisation. Reason: ");
		resources.put("whichgroup",				 	 "Which group?");
		resources.put("yes",                         "Yes");
		resources.put("youwereadded",				 "You were added by UIN: ");
		

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
		// Connection to server
		resources.put("error_120",                   "An I/O error occured (#120.EXT)");
		resources.put("error_121",                   "Requested TCP connection cannot be made (#121.EXT)");
		resources.put("error_122",                   "Specified server host and/or port is invalid (#122.EXT)");
		resources.put("error_123",                   "Connection has not been established (#123.EXT)");
		resources.put("error_124",                   "Input stream is out of sync (#124.EXT)");
		
		// Peer connection
		resources.put("error_125",                   "An I/O error occured (#125.EXT)");
		resources.put("error_126",                   "Requested TCP connection cannot be made (#126.EXT)");
		resources.put("error_127",                   "Specified server host and/or port is invalid (#127.EXT)");
		resources.put("error_128",                   "Connection has not been established (#128.EXT)");
		resources.put("error_129",                   "Input stream is out of sync (#129.EXT)");

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
		resources.put("error_154",                   "Error updating your contact list (#154.EXT)");
		resources.put("error_155",                   "Object is already in your server list (#155.EXT)");
		resources.put("error_156",                   "Error while adding. Try again (#156.EXT)");
		resources.put("error_157",                   "No more elements of this type allowed (#157.EXT)");
		resources.put("error_158",                   "You did try to add an ICQ contact to an AIM list (#158.EXT)");
		resources.put("error_159",                   "Server did not answer on search request. Try again (#159.EXT)");
		resources.put("error_160",                   "Error while searching (#160.EXT)");
		resources.put("error_161",                   "No groups found. Please add a group (#161.EXT)");

		// Other errors
		resources.put("error_170",                   "Possibly not enough heap memory available (#170.EXT)");
		resources.put("error_171",                   "Could not fetch meta info (#171.EXT)");
		
		// Camera errors
		resources.put("error_180",                   "Error creating the VideoControl (#180.EXT)");
		resources.put("error_181",                   "Viewfinder initialization error (#181.EXT)");
		resources.put("error_182",                   "Viewfinder start error (#182.EXT)");
		resources.put("error_183",                   "Snapshot error (#183.EXT)");
		resources.put("error_185",                   "Taking pictures not supported (#185.EXT)");
		
		// File transfer errors
		resources.put("error_190",                   "File transfer with < ICQv8 clients not supported (#190.EXT)");
		resources.put("error_191",                   "Error reading the file. Possibly not supported (#191.EXT)");
		resources.put("error_192",                   "Error reading the file. Wrong path or not supported (#192.EXT)");

	}
}

// #sijapp cond.end #

