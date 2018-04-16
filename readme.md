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



digraph finite_state_machine {
	rankdir=LR;
	size="8,5"


	node [shape = doublecircle color="blue" style=filled]; Station05;
	node [shape = pentagon color="lightgray" ];
	Station05 -> Miniship051 [ label = "radio" color="green" style="dashed"];	
	Miniship051 -> Miniship0511 [ label = "pipe" ];	
	Station05 -> Miniship052 [ label = "radio" color="green" style="dashed"];	
	Station05 -> Miniship053 [ label = "pipe" ];	

	node [shape = doublecircle color="blue"]; Station04;
	node [shape = pentagon color="lightgray"];
	Station04 -> Miniship041 [ label = "pipe" ];	
	Miniship041 -> Miniship0411 [ label = "pipe" ];	
	Miniship041 -> Miniship0412 [ label = "pipe" ];	
	Station04 -> Miniship042 [ label = "pipe" ];	
	Station04 -> Miniship043 [ label = "pipe" ];	


	node [shape = doublecircle color="blue"]; Station03;
	node [shape = pentagon color="lightgray"];
	Station03 -> Miniship031 [ label = "pipe" ];	
	Miniship031 -> Miniship0311 [ label = "pipe" ];	


	node [shape = doublecircle color="blue"]; Station02;
	node [shape = pentagon color="lightgray"];
	Station02 -> Miniship021 [ label = "pipe" ];	
	Station02 -> Miniship022 [ label = "pipe" ];	
	Station02 -> Miniship023 [ label = "pipe" ];	

	
	node [shape = doublecircle color="blue"]; Station01;
	node [shape = pentagon color="lightgray"];
	Station01 -> Miniship011 [ label = "pipe" ];	

}







