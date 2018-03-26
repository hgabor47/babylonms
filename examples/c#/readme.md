This is an examples directory for C# developments.


1. Get a UUID for your app (https://www.uuidgenerator.net/)

2. Start a new project in VS.
3. A right way to create a class for BabylonMS communication like this:

	class ImageBuffer
    {
        public static string ImageBufferUUID = "52f1b4e2-61e2-4ca0-8d21-e70b509e7693";  //This Pod is a SHIP
        public BabylonMS.BabylonMS bms;
        public bool ready=false;
        WindowsList windowslist;
        Process proc_imagebuffer;
        public ImageBuffer()
        {
            if (!Program.TEST_IMAGEBUFFER)
            {
                bms = BabylonMS.BabylonMS.LaunchMiniShip(out proc_imagebuffer, "ImageBuffer.exe", ImageBufferUUID, ImageBufferUUID, Program.instanceUUID); //UUID
            }
            else
            {
                bms = BabylonMS.BabylonMS.LaunchMiniShip(ImageBufferUUID, ImageBufferUUID, Program.instanceUUID); //DEBUG because Manual start //UUID
            }
            try
            {
                bms.ChangeMiniShipToNetwork(Program.ip_imagebuffer, Program.port_imagebuffer); //started file but switchOn Radio
                bms.ServerReadyForTransfer += ReadyForTransfer;
                bms.PrepareGate();
            }
            catch (Exception ) {
                Console.WriteLine(Program.ERR_IMAGEBUFFERLOAD);
                Program.terminate();
            }

            while ((bms.IsReady) || (!ready)) { Thread.Sleep(100); }; //TODO Biztosan nem "!bms.IsReady" kell?
            ready = false;
            bms.ServerReadyForTransfer -= ReadyForTransfer;  //NEED!! tedd vissza a norm'l haszn'lathoz            
        }
        void ReadyForTransfer(BabylonMS.BMSEventSessionParameter session)
        {
            bms.Disengage();
            ready = true;
            windowslist = new WindowsList(2f);
            Console.WriteLine("ImageBuffer started (WindowsList created)");
        }
        public void destroy()
        {
            try
            {
                proc_imagebuffer.Kill();
                windowslist.destroy();
            }
            catch (Exception ) { };
        }
    }	

