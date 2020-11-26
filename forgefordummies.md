# Installing Forge (for dummies)

## Before you download
Make sure you have Java installed (you can google how yourself lol)

Make sure you have run the version of the game you are installing forge for at least once for it to install properly. 

*(eg, if you are installing forge for 1.8.9, run vanilla Minecraft 1.8.9 at least once if you haven't before)*


## Downloading Forge
Download the latest forge installer from [here](http://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.8.9.html). *Select the Installer option, **not** universal, mdk, nor windows*

![1](/media/forgefordummies/1.png)

When it sends you to the adfoc.us site, don't click anything except the *Skip* button in the top right, then save the file. 

![2](/media/forgefordummies/2.png)

Open this file. Once it is open, make sure *"Install Client"* is selected, then click OK.
![3](/media/forgefordummies/3.png)

Let it finish installing and then continue on to the next step.

## Setting up Forge
Open the minecraft launcher, and might should be a new profile *"forge"*. Run this once and then close your game.

If there isn't a forge profile, go to Installations, click *"New"*, name the profile and click on version and scroll to the bottom and select the forge version. Then click *"Create"*, and run it once then close your game.

![4](/media/forgefordummies/4.png)

Now, go to your minecraft directory *(steps below)*, and if there isn't already, create a folder called *"mods"*.

If you wish to have multiple game versions of forge installed, with different mods in each, create subfolders in the mods folder with the name of the game version *("1.8.9" in this case)*.

Now you can start downloading mods.

## Minecraft Directory
Windows: `C:\Users\<user>\AppData\Roaming\.minecraft`

If you are navigating to those folders manually, make sure "Show Hidden" is enabled in the *"View"* tab of File Explorer.

![4](/media/forgefordummies/5.png)

Mac: `~/Library/Application Support/minecraft`

(Use quick search, can't provide image as I run on Windows so try searching for a tutorial yourself)

## Recommended mods
[Optifine L5 or preview L6](https://optifine.net/downloads). *(Direct download to: [L5](http://optifine.net/adloadx?f=OptiFine_1.8.9_HD_U_L5.jar), [L6](http://optifine.net/adloadx?f=preview_OptiFine_1.8.9_HD_U_L6_pre1.jar))*

[Patcher](https://sk1er.club/mods/patcher)

[Sk1er's Old Animations](https://sk1er.club/beta) - This one is currently in beta, so if you don't want to use it, another option is [Orange's Old Animations](https://www.curseforge.com/minecraft/mc-mods/old-animations-mod), but it has some compatibility issues

[powns' togglesneak](https://download.powns.dev/togglesneak189) - Toggle sprint + sneak.

[5zig Reborn](https://5zigreborn.eu/) - This is a HUD mod that has what most individual HUD mods have combined, which can be good but is also a bit bloaty and hard to navigate (for the first few times using it)

## Mods Patcher replaces (you will not need any of these)
* CaseCommands - Sk1er LLC
* CommandPatcher - Sk1er LLC
* CompactChat - Sk1er LLC
* CrossChat - Sk1er LLC
* Frames+ - Sk1er LLC
* ItemOptimizations - Sk1er LLC
* MouseBindFix - Sk1er LLC
* ResourceExploitFix - Sk1er LLC
* WindowedFullscreen - Sk1er LLC
* CleanView - LainMI
* FastChat - 2Pi
* MemoryFix - prplz
* MouseDelayFix - prplz
* NoCloseMyChat - Cecer
* VanillaEnhancements - OrangeMarshall
* VoidChat - skyerzz

## Minecraft Settings
One of the reasons many people don't use forge is because "it is very laggy". Which, a majority of the time, is because they have bad ingame settings, which other clients automatically have set.

**I recommend starting off with extremely low settings (see below)**, and working your way up enabling/increasing what is most important to you (eg, if you're a builder you might like having *Smooth Lighting* maxed)

Also note, unless you are trying to record/stream, or are frequently screen-sharing on Discord, you really don't need more than 30% of your monitors refresh rate (most commonly 60hz). *(I say +30% to account for any frame drops)*

### General
* Graphics - Fast
* Smooth Lighting - Off
* Smooth Lighting Level - 0%
* Dynamic Lights - Off *(this doesn't have much of a difference, so feel free to change if you get more FPS than nessecary once all settings are set and adjusted)*
* Render Distance - 4
* Max Framerate - Unlimited (cap to your screens refresh rate or set to VSync if you experience screen tearing)
* VBOS - Off (try testing FPS with them on as well to see which performs better in your case)

### Details
* Clouds - Off
* Trees - Fast
* Sky - Off
* Fog - Off
* Translucent Blocks - Off
* Dropped Items - Fast
* Vignette - Fast
* Swamp Colors - Off
* Cloud Height - Off
* Rain and Snow - Off or Fast
* Stars - Off
* Entity Shadows - Off
* Smooth Biomes - Off

### Animations
*Click the "All Off" button, then set Particles to All as the following shouldn't affect much*
* Redstone - On *(TNT indicator in bedwars)*
* Potion Particles - On

### Quality
* Minimap Levels - Off
* Anistropic Filtering - Off
* Better Grass - Off
* Custom Sky - Off (Leave this on if you want a custom sky, requires sun and moon to be turned on in Details)
* Minimap Type - Nearest
* Antialiasing - Off
* Better Snow - Off

### Performance
* Smooth FPS - Off (test for yourself)
* Fast Render - On
* Chunk Updates - 1
* Region Renders Off
* Smart Animations - On
* Fast Math - On