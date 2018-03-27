using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace PrelimutensChat
{
    static class Program
    {
        public static string instanceUUID = "e7bdb39f-c2c1-447b-b528-4b9a40757e90";
        public static string shipUUID = "3a5c1559-91d3-4307-8fe2-5c159390e84d";
        public static string[] Args;
        public static Form1 gui;

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main(string[] args)
        {
            Args = args;
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            gui = new Form1();
            Application.Run(gui);
        }


    }
}
