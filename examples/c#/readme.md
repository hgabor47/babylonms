BabylonMS simplest examples
===========================

This is an example directory for C# developments by BabylonMS.


1. Get a UUID for your app (https://www.uuidgenerator.net/)

2. Start a new project in VS.
3. A right way to create a class for BabylonMS communication
4. Need to add reference to project to System.IO.Compression (for BabylonMS)
5. See the PrelimutensChat PIPELINE version 
6. See the PrelimutensChat IP version

The difference only:


	BabylonMS.BabylonMS.ShipDocking(Program.shipUUID, Program.Args);
	
	vs 
	
	BabylonMS.BabylonMS.ShipDocking("127.0.0.1",9020,Program.shipUUID); 


But there are some other matter of principle.

*Please check out the all original modules for practical experience. *


