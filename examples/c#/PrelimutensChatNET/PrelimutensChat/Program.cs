using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace PrelimutensChat
{
    static class Program
    {
        public static string instanceUUID = "fcf1009e-ff19-43ec-9b8b-ef1ae9565f1b";
        public static string shipUUID = "fd3e39c4-ff45-4d63-a2a7-d59e386cdaed";
        //public static string[] Args;
        public static Form1 gui;

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main(string[] args)
        {
            //Args = args;
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            gui = new Form1();
            Application.Run(gui);
        }


    }
}
