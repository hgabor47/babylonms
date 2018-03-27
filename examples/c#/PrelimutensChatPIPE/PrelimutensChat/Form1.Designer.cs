namespace PrelimutensChat
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.me = new System.Windows.Forms.TextBox();
            this.you = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.mytext = new System.Windows.Forms.TextBox();
            this.yourtext = new System.Windows.Forms.TextBox();
            this.label2 = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // me
            // 
            this.me.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.me.Location = new System.Drawing.Point(356, 21);
            this.me.Multiline = true;
            this.me.Name = "me";
            this.me.Size = new System.Drawing.Size(205, 137);
            this.me.TabIndex = 0;
            // 
            // you
            // 
            this.you.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.you.Location = new System.Drawing.Point(356, 193);
            this.you.Multiline = true;
            this.you.Name = "you";
            this.you.Size = new System.Drawing.Size(205, 156);
            this.you.TabIndex = 1;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(13, 5);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(60, 13);
            this.label1.TabIndex = 2;
            this.label1.Text = "Me  (Recv)";
            // 
            // mytext
            // 
            this.mytext.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.mytext.Location = new System.Drawing.Point(2, 21);
            this.mytext.Multiline = true;
            this.mytext.Name = "mytext";
            this.mytext.Size = new System.Drawing.Size(348, 137);
            this.mytext.TabIndex = 6;
            this.mytext.KeyDown += new System.Windows.Forms.KeyEventHandler(this.mytext_KeyDown);
            // 
            // yourtext
            // 
            this.yourtext.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.yourtext.Location = new System.Drawing.Point(2, 193);
            this.yourtext.Multiline = true;
            this.yourtext.Name = "yourtext";
            this.yourtext.Size = new System.Drawing.Size(348, 156);
            this.yourtext.TabIndex = 7;
            this.yourtext.KeyDown += new System.Windows.Forms.KeyEventHandler(this.yourtext_KeyDown);
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(13, 177);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(66, 13);
            this.label2.TabIndex = 8;
            this.label2.Text = "You  (Sendr)";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(573, 354);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.yourtext);
            this.Controls.Add(this.mytext);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.you);
            this.Controls.Add(this.me);
            this.Name = "Form1";
            this.Text = "Prelimutens Chat";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        public System.Windows.Forms.TextBox me;
        public System.Windows.Forms.TextBox you;
        private System.Windows.Forms.Label label1;
        public System.Windows.Forms.TextBox mytext;
        public System.Windows.Forms.TextBox yourtext;
        private System.Windows.Forms.Label label2;
    }
}

