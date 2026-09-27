# Reverser
Reverser is a Minecraft Fabric mod that is made to help reverse engineer world generation process

> NOTE! This mod is not made for normal servers with players.

## Versions
| Mc Version  | Mod version | Fabric loader | Fabric API
|-|-|-|-|
| 26.3 | 0.1.2 | >= 0.19.5 | 0.161.0+26.3 

## Basic commands
- `getnoise random <radius> <type>` gets noise (from type argument) from random seed in entered radius
- `getnoise <seed> <radius> <type>` gets noise (from type argument) from set seed in entered radius

## Use this mod
1. Download [Fabric server](https://fabricmc.net/use/server/) with your Minecraft version and Fabric loader.
2. Launch the server and agree to [EULA](https://minecraft.net/eula).
3. Download Fabric API from [Modrinth](https://modrinth.com/mod/fabric-api) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api) and paste both FAPI and Reverser into `mods` folder.
4. Edit `max-tick-time` to `-1` in `server.propeties` to prevent crashes
5. Relaunch the server and you should be done.

## Building from source
1. Clone this repository
2. Run `./gradlew build`
3. You should find your mod in `build/libs/`
