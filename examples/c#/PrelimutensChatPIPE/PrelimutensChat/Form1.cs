/*
 * 
 *  Simplest not dumbresistent code for BabylonMS PIPELINE
 *  
 */ 
 

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Net.NetworkInformation;
using System.Threading;
using System.Threading.Tasks;
using System.Reflection;

namespace PrelimutensChat
{
    public partial class Form1 : Form
    {
        Recv recv;
        SendR sendr;
        public Form1()
        {
            InitializeComponent();
            recv = new Recv();
            sendr=new SendR();
        }

        private delegate void SetControlPropertyThreadSafeDelegate(
            Control control,
            string propertyName,
            object propertyValue);

        public static void SetControlPropertyThreadSafe(
            Control control,
            string propertyName,
            object propertyValue)
        {
            if (control.InvokeRequired)
            {
                control.Invoke(new SetControlPropertyThreadSafeDelegate
                (SetControlPropertyThreadSafe),
                new object[] { control, propertyName, propertyValue });
            }
            else
            {
                control.GetType().InvokeMember(
                    propertyName,
                    BindingFlags.SetProperty,
                    null,
                    control,
                    new object[] { propertyValue });
            }
        }

        private void mytext_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
            {
                var t = (sender as TextBox);
                String s = t.Lines[t.Lines.Length - 1];
                recv.Session.outputPack.ClearFields();
                recv.Session.outputPack.AddField("Text", BabylonMS.BabylonMS.CONST_FT_BYTE).Value(s);
                recv.Session.TransferPacket(true);
            }
        }

        private void yourtext_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
            {
                var t = (sender as TextBox);
                String s = t.Lines[t.Lines.Length - 1];
                sendr.Session.outputPack.ClearFields();
                sendr.Session.outputPack.AddField("Text", BabylonMS.BabylonMS.CONST_FT_BYTE).Value(s);
                sendr.Session.TransferPacket(true);
            }
        }
    }

    class Recv
    {        
        public BabylonMS.BabylonMS bms;
        public BabylonMS.BMSEventSessionParameter Session;
        public Recv()
        {
            bms = BabylonMS.BabylonMS.ShipDocking(Program.shipUUID, Program.Args);
            bms.Connected += ClientConnected;
            bms.Disconnected += Disconnected;
            bms.NewInputFrame += NewInputFrame;
            bms.OpenGate(false);//client    
        }
        void Disconnected(BabylonMS.BMSEventSessionParameter session)
        {
            String txt = Program.gui.you.Text;
            Form1.SetControlPropertyThreadSafe(Program.gui.you, "Text", txt+ "\r\nDisconnected");
        }
        void ClientConnected(BabylonMS.BMSEventSessionParameter session)
        {
            Session = session;
            String txt = Program.gui.you.Text;
            Form1.SetControlPropertyThreadSafe(Program.gui.you, "Text", txt + "\r\nConnected");
        }

        void NewInputFrame(BabylonMS.BMSEventSessionParameter session)
        {
            String txt = Program.gui.me.Text;
            String s = session.inputPack.GetFieldByName("Text").GetString();
            Form1.SetControlPropertyThreadSafe(Program.gui.me, "Text", txt + "\r\nNewInputFrame\r\n" + s);
        }
    }

    class SendR
    {        
        BabylonMS.BabylonMS bms;
        public BabylonMS.BMSEventSessionParameter Session;        
        
        public SendR()
        {
            bms = BabylonMS.BabylonMS.LaunchMiniShip(Program.shipUUID, Program.shipUUID, Program.instanceUUID); //UUID
            Console.WriteLine("SendR Ship launched.");
            bms.NewInputFrame += NewInputFrame;
            bms.ServerReadyForTransfer += ReadyForTransfer;
            bms.PrepareGate();
        }
        void ReadyForTransfer(BabylonMS.BMSEventSessionParameter session)
        {
            Session = session;
            String txt = Program.gui.me.Text;
            Form1.SetControlPropertyThreadSafe(Program.gui.me, "Text", txt + "\r\nReadyForTransfer");
        }

        void NewInputFrame(BabylonMS.BMSEventSessionParameter session)
        {
            String txt = Program.gui.you.Text;
            String s = session.inputPack.GetFieldByName("Text").GetString();
            Form1.SetControlPropertyThreadSafe(Program.gui.you, "Text", txt + "\r\nNewInputFrame\r\n"+s);
        }

    }
}
