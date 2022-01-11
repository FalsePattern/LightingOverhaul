## Lighting Overhaul
Lighting Overhaul is an open source Forge Core-Mod that aims to replace Minecraft's lighting engine with a system which adds three new channels of light.  A new set of lighting update routines will spread colors around, while a modified rendering engine deals with the new information.  That's not all though!  While colored lights are awesome, the ultimate goal of this project is to extend an API for other mod writers to use!  Expect to see some mods show up that hook into the API we provide!

Exclusive to Minecraft Forge 1.7.10, don't ask for any other versions, or you will be ignored!

## Dependencies
SpongeMixins 1.3.3 (available at the releases)
Triangulator 1.1.1 or newer (https://github.com/FalsePattern/Triangulator/releases)


![splash](http://i.imgur.com/JszmQ0h.png "Minecraft Forge 1.7.10")

## Features
- Light gets colored when it passes through stained glass (even sunlight!)
- Smooth mixing of colored light
- Custom colored light values for any light-emitting modded block

## Fork changes
- Migrated to a way better build script.
- Rebranded to Lighting Overhaul
- Cleaned up source code directory clutter. Now the entire code is in a comprehensible structure instead of scattered around everywhere.
- Reworked all mixins from @Overwrite to more extensible alternatives (in progress)
- Removed colored glowstone -- This is now purely a coremod and API for other mods to hook into.

## Known issues/incompatibilities
- Does not work with any OptiFine versions

## Special Thanks
[basdxz](https://github.com/basdxz)
- For helping me with migrating the ancient code to a more modern buildscript, and giving me guidance with mixins and Minecraft's render engine.