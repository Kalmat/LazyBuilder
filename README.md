# Lazy Builder for Minecraft 1.16.3 #

Build huge and complex structures quickly, copy and paste any structure you like, and undo your building mistakes.

## TUTORIAL ##
< https://docs.google.com/document/d/1T8tFxeFUa-Ad0CZNsnDvd69oOKFd71T0WPDsnyLz-3w >

### BUILDING ###

* Use Start (green) Block to determine the beginning of your structure
* Fill it with the building-blocks your structure will require (it works like a chest, but will only admit blocks)
* Then place an Intermediate (yellow) Block to determine the height of the structure (height = 0 --> Horizontal Structure, like a floor or a roof)
* Place other Intermediate (yellow) Block to shape your structure as requested (horizontal structures will only require up to 2 Intermediate Blocks)
* Place End (red) Block to set the end of the structure
* Activate (right-click) the End (red) Block

What you will need for every structure type:

* One Line (row or column): 1 Start Block + 1 End Block
* A wall: 1 Start Block + 1 Intermediate Block (wich sets the height) + 1 End Block
* 2-N walls: 1 Start Block + 2-N Intermediate Blocks + 1 End Block
* A floor (or a roof): 1 Start Block + 1 Intermediate Block (at the same height than the Start Block) + 1 End Block

### MAGIC COLUMN ###

* If you place and activate Start Block when standing right on it and sneaking it will generate a 10-Dirt-Blocks column which may be useful to build or quickly escape from monsters

### COPY & PASTE ###

* Use Copy-Paste (blue) Block to determine the beginning of your structure
* Fill it with the "fuel" items your structure will require (it works like a chest, but will only admit Gold Ingots, Emeralds or Redstone Blocks). You will need 1 itme for every 25 non-air blocks to be copied
* Place an Intermediate (yellow) Block to determine the height of the structure
* Place the End (red) Block, shaping a 3D-Rectangle
* Activate (right-click) the End (red) Block
* If it worked, Copy-Paste Block will nicely glow
* Harvest Copy-Paste Block and place it wherever you want to paste your copied structure (it will drop back all unused items)
* When you place it again, you will see two line indicators showing where Intermediate (yellow) and End (red) Blocks were placed when the structure was copied
* You can rotate the whole structure before pasting it by pressing the "R" key
* Activate it (right-click while sneaking)
* If you don't want to paste the copied structure, you can reset the Copy-Paste Block by left-activating it (left-click) while sneaking. Be aware the items used to copy the structure will not be recovered.

### UNDO ###

* If you make a mistake when placing or breaking a block, just press Ctl-Z (max. 50 actions back)


### How do I Install it? ###

* Prepare your mod development environment (eclipse, OpenJDK and forge (there are many tutorials on the Web)
* Download repository
* Copy and replace "src" folder onto your forge project folder
* Import the gradle project in eclipse
* Enjoy viewing or modifying it

### How do I get the jar file? ###

* Download the .jar file
* Copy the .jar file to your mods folder (local and server)

### Who do I talk to? ###

* alescribano@gmail.com
* Will try to answer as much as I can