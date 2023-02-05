# BardingzzClient
This client-side mod for Wurm Unlimited allows for server mods to load in custom horse barding models.

## What players have to know
If a server owner tells you to get this mod, download it from the right side under "Releases"
then install it like any other mod, and you'll be able to see some custom bardings.

## What server owners have to know
To load in custom bardings all you need is the specific mod that adds them in, 
like [Bardings](https://github.com/Tyoda/Bardings). There is no server-side
"BardingzzServer" equivalent.

Furthermore, for unmodded clients the custom bardings will appear like their vanilla
cotton/leather/chain counterparts, with no negative side effects.

## What modders have to know
All you need to do to create custom bardings is throw the clothBarding.wom (or chain/leather) and
your custom texture in a serverpack and then in the mappings.txt put two entries per barding. 

The first entry will be when the item is on the ground. This **has to start with** `mod.bardingzz.` and then (to
make sure it is unique) your nickname, then your mod's name, and finally the barding's name 
followed by cotton (or leather/iron/copper/...). So for my Bardings mod it looks like this:
`mod.bardingzz.tyoda.bardings.smiley.cotton = clothBarding.wom?clothBardingMat.texture=smiley.dds`

The second entry is for when it is on the horse. This **has to be the exact same**,
except with `.riding` instead of `.cotton` (or leather/iron) at the end, and only referencing the texture.
So for the same item in my mod it will read:
`mod.bardingzz.tyoda.bardings.smiley.riding = smiley.dds`

**Note that even though you map to `.cotton` in the mappings.txt, in the ItemTemplateBuilder you should not include that,
only up to `...smiley.` (including the dot at the end)**

**Also note that the only materials you should use for the barding in the ItemTemplateBuilder are cotton, leather, and any vanilla metal.
This is required so as not to cause issues for unmodded clients.**
