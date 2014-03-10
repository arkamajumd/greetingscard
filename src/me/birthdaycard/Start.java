package me.birthdaycard;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import tools.Callback;

public class Start extends JFrame {
	private static final long serialVersionUID = 1L;
	Callback c;
	JProgressBar fake;
	Timer fakeProgress = new Timer(15, new ActionListener() {
		private int timesPerformed = 0;

		@Override
		public void actionPerformed(ActionEvent evt) {
			timesPerformed++;
			if (timesPerformed < 100) {
				fake.setValue(fake.getValue() + 1);
			} else if (timesPerformed == 100) {
				fake.setString("Done!");
				java.awt.Toolkit.getDefaultToolkit().beep();
			} else if (timesPerformed == 120) {
				fakeProgress.stop();
				get().setVisible(false);
				c.start();
			}
		}
	});

	public Start(Callback callback) {
		c = callback;
		this.setUndecorated(true);
		Icon pict = new ImageIcon(getClass().getResource("noisemaker.png"));
		this.setContentPane(new JPanel(new BorderLayout()));
		this.getContentPane().add(new JLabel(pict), BorderLayout.CENTER);
		fake = new JProgressBar(0, 100);
		fake.setStringPainted(true);
		fake.setString("Loading...");
		this.getContentPane().add(fake, BorderLayout.SOUTH);
		this.pack();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setAlwaysOnTop(true);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fakeProgress.start();
	}

	private Start get() {
		return this;
	}
}