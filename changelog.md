# 7.0.0

* Giant initial update. This list may have missing parts.
* Added mca villager and zombie villager
* Added genetics, personality, traits and mood
* Added dialogue engine
    * Ported classic interactions
    * Added adoption
    * Added divorce and divorce papers
* Added enhanced gifting
    * Has a saturation Queue
    * Respects villagers specific needs
* Added wedding ring and engagement ring
* Added Grim Reaper
* Added graves, resurrection, Staff of Life and the Scythe
* Added guards and archers
* Added blueprint
    * Added village management
    * Added automatic building and village recognition
    * Added initial building types to extend village functions
    * Added rank, task system
* Added taxes
* Added chores
* Added book with enhanced visuals
* Added Advancements
* Added Architecture to support Fabric and Forge
* Added voice acting
* Added initial translations

# 7.0.1

* Fixed traits syncing issues and chance math
* Fixed translation keys

# 7.0.2

* Fixed Server crash
* Fixed crash when setting clothes or haircut when playing on a server
* Added config flag to disable voice acting
* Fixed scythe loosing its charge on non-tombstones
* Fixed staff of life charges
* You can no longer adopt adults
* Fixed grown-up message appearing after world join
* Fixed building detection on certain coordinates
* Fixed tall villagers being too tall to live
* Fixed phrases not being translated on dedicated servers
* Synced Translations

# 7.0.3

* Attempting to talk to a zombie won't prevent you from performing an action
* Fixed interaction fatigue reset
* Added Interaction and gift analysis
* Overhauled gift desaturation.
    * Hearts reward will decrease, but won't drop below 0.
    * Desaturation uses a configurable exponential curve, slightly favoring awesome stuff.
    * Once a day by default, the villager forgets about the latest gift in the queue
* Fixed "datapack" crash
* Building tasks are now required to advance in ranks
* Removed bed reserving, beds are searched on demand
* Fixed villager-keep-following-you problem
* Fixed greeting AI
* Increase percentage of adult villagers
* Fixed changing clothes of unemployed villagers
* Increased frequency of marriage, births and guard spawns

# 7.0.4

* Fixed widow icon
* Player and villager marriage symbol now swapped
* Taxes are initially set to 0%
* Whistle recipe now requires gold instead of rose gold
* Rings are no longer usable as gold ingots
* Fixed a crash related to building detection
* Integrated community re-shaded dna icon
* Added Vegetarian trait
* Fixed missing meat gift phrase
* Replaced names by accurate database of babies born in the US in 2010
* Fixed graves text for formatted names
* Fixed reviving for villager died by height or void
* When adopting, your spouse also becomes your children's mother
* Decreased villager knockback
* Fixed incorrect amount of bounty hunters
* Added two more headstones
* Fixed crash caused by zombie villagers on dedicated servers
* Only player with merchant rank or higher will receive tax notifications
* mca-admin commands no require op permission
* Fixed smaller issues with building recognition
* Automatic building scanning can now be disabled
* Next to Buildings, you can now add more restrictive "rooms" instead in case your build is not recognized otherwise
* Buildings can no longer intersect
* If adding a building fails, a proper error message is now shown
* Updating existing, intersected buildings work now
* Fixed some villagers being confused on where they live
* Fixed outdated translation variables
* Setting the workplace makes them jobless for now, effectively causing them to look for a new job
* You use both matchmaker rings now
* Gifting cake works on every adult married villager
* Buildings can now be marked as restricted, preventing villagers from moving in
* Voice acting is now disabled by default
* Fixed guards on duty randomly looking into the sky when talking to
* Fixed at least one teleporting-away-while-following bug

# 7.0.5

* Fixed issue with natural breeding
* Blueprint will now better display vertically stacked buildings
* Villager preview in the editor is now animated
* Fixed wasting charges on already reviving villagers
* Fixed a crash
* Fixed opposite gender bug
* Fixed villager marrying relatives
* Guards now attack mca zombie villagers
* No more sliding baby zombie villagers
* Slightly enhanced village boundary determination
* Fixed uninitialized zombie villager babies
* Fixed flower pots with flowers not being recognized
* Lost babies can now be retrieved by the spouse
* Fixed crash on dedicated server when using randomized baby name
* Village will now interact with each other
* Iron golems will now slap the villager when hit accidentally and then chill
* Guards will now support their citizen and have a custom dialogue when the player is the attacker
* Improved archer AI
* Fixed villager getting stuck in doors
* Guards no longer panic when a raid happens
* A wiped-out village will only send a last, bigger bountyhunter wave
* Added all items to recipe book
* Reworked female villager model
* Fixed a bunch of marriage issues caused on death
* Spouse and parents can now be modified in the villager editor
* Fixed guard spam
* Rank Mayor can now make villagers guards or archers manually
* If the Grim Reaper summoning fails, feedback on why is given
* Villager are now silent by default, configurable
* Villages can now be renamed
* Unlocked King rank

# 7.0.6

* Fixed guards aggression towards mobs
* Fixed profession change not always switching clothes
* Added Family Tree item to search
* Fixed crash
* Fixed reaper summoning on some server

# 7.0.7

* Experienced villagers no longer become guards
* The king can assign archers and guards at will
* Fixed king rank
* Can no longer pickup teens
* Fixed curing zombie villagers
* Added missing translations
* Added book of supporter
* Fixed gift desaturation not working
* Improved teleportation, especially when following the player
* Fixed the pixel gap of headstones
* Fixed sleeping villagers not waking up when moved around
* Added letter of condolence
* Fixed dimension issues with player and villager data
* Added mail system, used to notify the player about the death of family members
* Glass roofs are now supported
* Added more jobless skins
* Updated translations and fixed wrong variable syntax
* Added some admin commands
* Temporary disabled baby tracker
* You can now trade with family
* Fixed inventory duplication bug
* Fixed deadlock in relation with reaper spawner
* Villager marriages now respect player hearts
* Fixed gifting golden apple not reducing by 1
* Fixed crash when hovering over unmarried villagers marriage-symbol
* Villagers will also update baby time
* Fixed datapack crash on some system locales
* Hopefully fixed stuck-at-sleeping issues after loading world
* Adding a building will also look for graveyards to decrease player confusion

# 7.0.8

* Readded blacksmith functionality
* Fixed scaling-flickering with iguana tweaks
* Added text when trying to assign to invalid buildings
* Improved interaction layout
* Staff of Life can no longer be enchanted
* Fixed chores phrase names
* Command kill no longer counts as murder
* Added config flag to disable name tags
* Fixed log spam regarding invalid bounding boxes
* Fixed issues when assigning family in editor
* Buildings now support modded chests
* Villagers will now use your editor name
* Fixed letter author and creative mode usage
* Strengthened Grim Reaper
* Added mod support for atmospheric, autumity, berry good, buzzier bees, environmental, neopolitan, and upgrade aquatic
* Villager now recognize and estimate the value of every (modded) armor, tool, sword, bow and food as a gift (accuracy not guaranteed)
