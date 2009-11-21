This folder contains rfDynHUD configuration files.

The plugin supports individual configurations for each mod, car and session and even specific ones for being in the garage. If no specific configurations are found, the default configuration will be used. This default configuration is expected in this folder under the name "overlay.ini". Use the editor to create a valid configuration of your preference.

Mod-specific configurations are searched in subfolders named like the mod. So if you want to create a specific config for the "F1CTDP06" mod, you have to save the config file in a subfolder of the config folder called "F1CTDP06" and config file is to be named "overlay.ini".

If you want to use an individual configuration for a car called "R26" of the Mod "F1CTDP06", the config file must be stored as "F1CTDP06\overlay_R26.ini".

To use session-type specific configurations, you have to insert the session type identifier into the name as the last part. Valid identifiers are:
    TEST_DAY
    PRACTICE1
    PRACTICE2
    PRACTICE3
    PRACTICE4
    PRACTICE
    QUALIFYING
    WARMUP
    RACE
PRACTICE is a wildcard for PREACTICE1, PRACTICE2, etc.

Please refer to the log file to know the names of the mod and car, if you don't already know them.


The search order, if the mod is "F1CTDP06" and the car is "R26" and the session type is PRACTICE3, is the following:
F1CTDP06\overlay_garage_R26_PRACTICE3.ini
F1CTDP06\overlay_garage_R26_PRACTICE.ini
F1CTDP06\overlay_garage_PRACTICE3.ini
F1CTDP06\overlay_garage_PRACTICE.ini
overlay_garage_PRACTICE3.ini
overlay_garage_PRACTICE.ini
F1CTDP06\overlay_garage_R26.ini
F1CTDP06\overlay_garage.ini
overlay_garage.ini
F1CTDP06\overlay_R26_PRACTICE3.ini
F1CTDP06\overlay_R26_PRACTICE.ini
F1CTDP06\overlay_PRACTICE3.ini
F1CTDP06\overlay_PRACTICE.ini
overlay_PRACTICE3.ini
overlay_PRACTICE.ini
F1CTDP06\overlay_R26.ini
F1CTDP06\overlay.ini
overlay.ini

The "_garage" block is skipped, when leaving the garage.

If none of them could be found, the built-in factory default configuration is used. 


This folder also contains the three letter codes configuration file for the StandingsWidget (also usable by other Widget implementations). This file keeps mappings for known driver names to short forms (not necessarily 3 letters), that can be displayed instead to save some room on the screen. The file is accessed as "three_letter_codes.ini" and has entries of the form:
Rubens Barrichello=BAR
Michael Schumacher=Mich


Another file, searched for in this folder, is the input_bindings.ini. It keeps mappings of key-strokes or button-presses to input actions on named Widgets. Use the editor to generate and maintain that file.
