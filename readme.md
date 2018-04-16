Babylon Message Swap
====================

I would like to present my concept that’s aim is to help you develop softwares which is written different programming languages and that can intercommunicate with each other. 

Quick access
------------

###Terminology###

- BMS (abbreviation of Babylon Message Swap )
- Station ( main software what is communicate other modules but there is no significant difference to the modules )
- Miniship ( BMS module which is communicate with each others and with Station )
- Pipe ( Intercommunicate within a device like IPC )
- Radio ( network communication )



Configuration patterns
----------------------
Station-- Miniship

Station-- Miniship-- Miniship 

Station-- Miniship
	|-- Miniship
	|-- Miniship


Station-- Miniship
	|	|-- Miniship
	|	|-- Miniship
	|
	|-- Miniship
	|-- Miniship


![ConfigPatternImage](http://babylonms.com/babylonms/en/img/patterns.png "BMS Design pattern")






