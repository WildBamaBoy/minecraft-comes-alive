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
