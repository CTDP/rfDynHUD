This is the help readme for rfDynHUD and the rfDynHUD Editor. 

rfDynHUD means rFactor Dynamic HUD. (Please don't call it "That new TV-Style" or something like that.)
It is a new, feature rich, configurable and extendable dynamic HUD system for rFactor. It is configured through ini files, which are created and modified using the provided editor, which is kinda self-documentary. Please read the following clauses carefully. We will happily answer all your questions, if you got some, but please avoid asking things explained here. 

Configuration files - overlay*.ini
The "config" folder contains rfDynHUD configuration files. The default location of this folder is under the plugin folder itself. But this can be changed through the rfdynhud.ini file to be found in the plugin's root folder. 

The plugin supports individual configurations for each mod, car and session type and even specific ones for being in the garage. The plugin always tries to load the most specific one and falls back to more general ones whenever it doesn't find a specific one. If no specific configurations are found, the default configuration (overlay.ini) will be used. Use the editor to create a valid configuration of your preference.

So if you want one single configuration for everything, simply delete all overlay_*.ini files and just leave the overlay.ini. Don't save the same configuration under all possible names. This is nonsense. And don't accidently delete files like input_bindings.ini or three_letter_codes.ini, etc. 

Mod-specific configurations are searched for in subfolders named like the mod. So if you want to create a specific config for the "F1CTDP06" mod, you have to save an "overlay*.ini" config file in a subfolder of the config folder called "F1CTDP06". 

If you want to use an individual configuration for a car called "MyFunnyCar" of the Mod "MyMod", the config file must be stored as "MyMod\overlay_MyFunnyCar.ini". The rfdynhud.log file will tell you about the exact car name. If you set the log level (in rfdynhud.ini) to DEBUG, you will even be promted the exact order of probed file names. 

To use session-type specific configurations, you have to insert the session type identifier into the name as the last part.
Valid identifiers are: 
  * TEST_DAY 
  * PRACTICE1 
  * PRACTICE2 
  * PRACTICE3 
  * PRACTICE4 
  * PRACTICE (wildcard for PRACTICE1, PRACTICE2, etc.) 
  * QUALIFYING 
  * WARMUP 
  * RACE 
Please refer to the log file to know the names of the mod and car, if you don't already know them. 

If the mod is "F1CTDP06" and the vehicle class is "F12006" and the car is "Scuderia Ferrari Marlboro 2006" and the session type is "PRACTICE3", the search order is as follows.  
  1. F1CTDP06\overlay_Scuderia Ferrari Marlboro 2006_PRACTICE3.ini 
  2. F1CTDP06\overlay_F12006_PRACTICE3.ini 
  3. F1CTDP06\overlay_Scuderia Ferrari Marlboro 2006_PRACTICE.ini 
  4. F1CTDP06\overlay_F12006_PRACTICE.ini 
  5. F1CTDP06\overlay_PRACTICE3.ini 
  6. F1CTDP06\overlay_PRACTICE.ini 
  7. overlay_PRACTICE3.ini 
  8. overlay_PRACTICE.ini 
  9. F1CTDP06\overlay_Scuderia Ferrari Marlboro 2006.ini 
  10. F1CTDP06\overlay_F12006.ini 
  11. F1CTDP06\overlay.ini 
  12. overlay.ini 
When you enter the cockpit and start in the garage the above order is worked down twice. The first time the prefix "overlay" is replaced by "overlay_garage". When leaving the garage the overlay is reloaded only using the above order once.

***new*** 
It is now possible to render an overlay on the session monitor preview. There is a small preview, that you know in the lower right of the session monitor and a fullscreen version, that you get to when clicking onto the small one. These two scenes don't use the regular overlay.ini, but are rendered, if and only if the above order matches a file using the prefix "monitor_small" resp. "monitor_big" in the first pass and "monitor" in the second pass. So you can use one configuration for both monitor screens. But since they are very different in scale, it is advisable to use two configurations.

As a result you can use one single configuration for all sessions, mods and cars, if you delete all "overlay*.ini" files and only leave the "overlay.ini" file itself. If neither "overlay_monitor_small*.ini", nor "overlay_monitor_big*.ini", nor "overlay_monitor*.ini" exist, no overlay is rendered on the session monitor. 

Configuration files - three letter codes
The "config" folder also contains the three letter codes configuration file used by the StandingsWidget and others. This file keeps mappings for known driver names to short forms (not necessarily 3 letters), that can be displayed instead to save some room on the screen. The file is accessed as "three_letter_codes.ini" and has entries of the form: 
Rubens Barrichello=BAR
Michael Schumacher=Mich
Pedro De La Rosa=DLR;P. d.l.Rosa
As you can see you can also separate a second short form by a semicolon from the actual three letter code. This short form is normally generated. But for some names it can be usefull to define it yourself. 

All missing three-letter-codes and short forms are auto-generated and a note is placed in the rfdynhud.log file.
Three-letter-codes are generated using the first letter of the first name and the first two letters of the last name (e.g. John Doe => JDO). 

Configuration files - input bindings
Another file, searched for in this folder, is the "input_bindings.ini". It keeps mappings of key-strokes or button-presses to input actions on named Widgets. Use the InputBindingsManager from within the editor to generate and maintain that file. 

Important!
Make sure to always bind the IncBoost, DecBoost and TempBoost actions exactly like you did in rFactor. Otherwise engine wear calculations will be wrong. If any of these bindings are missing, the plugin will display an in-game warning. 

Editor - Different backgrounds
You can change the editor background images, so that they look like you use to see your rFactor. Do this by creating one screenshot for each of the resolutions, that you see under the "Resolutions" menu (menu bar). If you're lazy, just create a screenshot for the resultion, that you want to use in the editor. But we encourage you to create a complete set of screenshots and send them to us, so that we can provide them as additional downloads.
After you created those screenshots copy them to a new folder under the editor\backgrounds folder in the plugin folder. There you'll find a folder called "CTDPF106_Fer_T-Cam", which contains the default screenshot-set. Now select the new set from the properties editor to the right after deselecting any Widget (click on the background). 

Please don't mix your screenshots with the default ones and don't encourage anyone to do so. 

Editor - Basic workflow
Add a new Widget by selecting an entry from the Widgets menu. 

Remove a widget by selecting it with the mouse and then either pressing the DELETE key or choosing the "Remove selected Widget" entry from the Widgets menu (last item).
Position or scale a Widget by either dragging it resp. its borders with the mouse or by changing the appropriate properties in the properties editor on the right side of the editor.
Positions and sizes can either be given in absolute pixels or percentages. Only use pixels, if you're creating a private configuration and never plan to switch your game resolution.
If you want to share your new configuration, you must use percentages for both position and size. Otherwise your configuration will look different on different screens and resolutions.
You can change the default, that is used when a new Widget is created in the options window. 

All other properties are only configurable through the properties editor. Select a Widget with the mouse and then modify the properties as you like. Every single property is documented. So if you want to know more about a specific property, select it in the properties editor and the description will be displayed in the panel below. 

Templates
It is possible to define templates for any Widget type. This is simply done by storing a Widget configuration file containing one Widget of any kind. Then select the configuration file for the "templateConfig" property in the property editor to the right after deselecting any Widget. 

Screenshots
You can create screenshots from your currently displayed configuration. Select "Take Screenshot" from the "Tools" menu. This will create a new image file in your "UserData\ScreenShots" folder. 

Translations ***new***
Every Widget set (folders under Plugins\rfDynHUD\widget_sets) keeps files named "localizations_*.ini". The * is usually something like "en" or "de", but can be anything and only serves your recognition. These files are loaded in alphabetical order and settings from a file read later overwrite those from files read earlier. This way you can overwrite default translations without having to modify the original files.
Each of these files must begin with the following header: 
[GENERAL]
codepage = UTF-8
language = en
The codepage must be exactly the one, with which the file has been saved. Otherwise special characters might be messed up when the file is loaded.
The language id must match exactly the value from the rfdynhud.ini (main plugin's folder) for the setting language.
All "localization_*.ini" files from these Widget set folders are read and all those with the selected language code are used, the others are ignored. 

Class scoring ***new***
It is now possible to activate vehicle class relative scoring. Activate it in the editor after deselecting all Widgets (click on the background) and change the "use class scoring" property's value. Then the StandingsWidget (for example) will display cars in your class only. 

Have fun! :-)
