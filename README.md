# Lazy Builder for Minecraft 1.16.3 #

Build huge and complex structures quickly by automating the routinary task of placing blocks, copy and paste any structure you like, wipe any structure or area you need, protect your constructions, undo your building mistakes or quickly scape from mobs!

## TUTORIAL ##
< https://docs.google.com/document/d/1T8tFxeFUa-Ad0CZNsnDvd69oOKFd71T0WPDsnyLz-3w >

### BUILDING ###

* Use Start (green) Block to determine the beginning of your structure
* Fill it with the building-blocks your structure will require (it works like a chest, but will only admit blocks)
* Then place an Intermediate (yellow) Block to determine the height of the structure (if height = 0 --> Horizontal Structure, like a floor or a roof)
* Place other Intermediate (yellow) Block to shape your structure as required (horizontal structures will only require 1 Intermediate Block)
* Place End (red) Block to set the end of the structure
* Activate (right-click) the End (red) Block
* Harvest Start (green) Block to recover all structure Blocks as well as unused building-blocks remaining inside

What you will need for every structure type:

* One Line (row or column): 1 Start Block + 1 End Block
* A wall: 1 Start Block + 1 Intermediate Block (wich sets the height) + 1 End Block
* 2-N walls: 1 Start Block + 1 Intermediate Block (wich sets the height) + N Intermediate Blocks (at the corners) + 1 End Block
* A floor (or a roof): 1 Start Block + 1 Intermediate Block (at the same exact height than the Start Block) + 1 End Block

### MAGIC COLUMN ###

* If you place and activate Start Block when standing right on it and sneaking, it will generate a 10-Dirt-Blocks column which may be useful to build or quickly escape from monsters

### COPY & PASTE ###

* Use Copy-Paste (blue) Block to determine the beginning of your structure
* Fill it with the "fuel" items your structure will require (it works like a chest, but will only admit Gold Ingots, Emeralds or Redstone Blocks). You will need 1 item for every 20 non-air blocks to be copied
* Place the End (red) Block, shaping a 3D-Cube
* Activate (right-click) the End (red) Block
* If it worked, Copy-Paste Block will nicely glow
* Harvest Copy-Paste Block and place it wherever you want to paste your copied structure (it will drop back all unused items)
* When you place it again, you will see the copied structure outlined. This is he space the structure will occupy when pasted
* You can rotate the whole structure before pasting it by pressing the "R" key vwhile looking at the Copy-Paste (blue) Block
* Activate it (right-click while sneaking)
* If you don't want to paste the copied structure, you can reset the Copy-Paste Block by left-activating it (left-click while sneaking). Be aware the items used to copy the structure will not be recovered.

### DESTRUCTION ###

* Use Destruction (black&yellow) Block to determine the beginning of the area to be wiped (it can remove any kind of block as well as water and lava sources)
* You don't need to fill it with any item or block
* Place the End (red) BLock, shaping a 3D-Cube
* Activate (right-click) the End (red) BLock
* It can remove water and lava sources
* It will wipe the entire area you selected, but be aware you will no receive any drop or loot (so be careful where you use it or you could lose valuable things)
* No, it can not be Undone!

### PROTECTION ###

* Place Protection (black&white-Shield) Block at the center of the area you want to protect
* Fill it with diamonds: every diamond will increase protection radius by 3 blocks (max. radius is 27 blocks / 9 diamonds)
* The blocks Within this radius (in a 3D-Sphere shape), can not be harvested by any other player apart from yourself, and can not be broken by any kind of explosion
* You can place as many of these blocks as you want. Their protection radius will be determined by the number of diamonds contained by each one separatedly

### UNDO ###

* If you make a mistake when placing or breaking a block, just press Ctl-Z (max. 50 actions back)

### How do I Install it? ###

* Prepare your mod development environmen: eclipse, OpenJDK8U-jdk_x64_windows_hotspot_8u265b01 and forge-1.16.3-34.1.0-mdk (there are many tutorials on the Web)
* Download repository
* Copy and replace "src" folder onto your forge example project folder
* Import the gradle project in eclipse
* Enjoy testing/improving it

### How do I get the jar file? ###

* Download the .jar file
* Copy the .jar file into your mods folder (local and server)

### Who do I talk to? ###

* palookjones@gmail.com
* Will try to answer as much as I can
