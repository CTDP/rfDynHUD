# rfDynHUD

rfDynHUD means rFactor Dynamic HUD. 
It is a feature rich, configurable and extendable dynamic HUD system for rFactor. It is configured through ini files, which are created and modified using the provided editor, which is kinda self-documentary. 
Please read the readme with care to learn how to use this plugin.

## Project structure

* `src/`: Main plugin source
* `editor/`: Source of the rfDynHUD Editor* `rfactor1/`: Game data interface implementations for rFactor1* `rfactor2/`: Game data interface implementations for rFactor2
* `lessons/`: Examples for Widget development* `director/`: rfDynHUD (sub-) plugin for the director mode (control rfDynHUD from the editor)* `standard_widget_set/`: Source and resources of the StandardWidgetSet* `ecclesone_tv_2010/`: Source and resources of the EcclesoneTV2012 WidgetSet

## Contributing to rfDynHUD
 
* Check out the latest master to make sure the feature hasn't been implemented or the bug hasn't been fixed yet
* Check out the issue tracker to make sure someone already hasn't requested it and/or contributed it
* Fork the project
* Start a feature/bugfix branch
* Commit and push until you are happy with your contribution, then send a pull-request
* Please try not to mess with the build, version, or history. If you want to have your own version, or is otherwise necessary, that is fine, but please isolate to its own commit so I can cherry-pick around it.

### Learn to write plugins

rFDynHUD allows to create your own HUD widgets. The quickest way to learn the interfaces is to follow the Lessons given in the [lessons-folder][lessons]. They teach the basics of displaying data, layouting and styling.

## License

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.  
[GNU General Public License v2][license].

[lessons]: https://github.com/CTDP/rfDynHUD/tree/master/java/lessons
[license]: https://github.com/CTDP/rfDynHUD/tree/master/LICENSE.md

