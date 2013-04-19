/*
 *   Firemox is a turn based strategy simulator
 *   Copyright (C) 2003-2007 Fabrice Daugan
 *
 *   This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 *   This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 *
 *   You should have received a copy of the GNU General Public License along  
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sf.firemox.ui.wizard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.mail.MailUtils;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.component.JLink;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.zone.MZone;
import net.sf.firemox.zone.ZoneManager;
import sun.awt.image.ToolkitImage;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Bug extends YesNo {

	/**
	 * Creates a new instance of Bug <br>
	 */
	public Bug() {
		super(LanguageManager.getString("wiz_bugreport.title"), LanguageManager
				.getString("wiz_bugreport.description"), "wiz_bugreport.gif",
				LanguageManager.getString("send"), LanguageManager.getString("cancel"),
				600, 400);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		gameParamPanel.setLayout(new BoxLayout(gameParamPanel, BoxLayout.Y_AXIS));

		final JPanel userPanel = new JPanel();
		userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.X_AXIS));
		userPanel.add(new JLabel(LanguageManager
				.getString("wiz_sf.sourceforgename")
				+ " : "));
		userNameTxt = new JTextField(Configuration.getString("sourceforgeName", ""));
		userPanel.add(userNameTxt);
		JLink sfLabel = new JLink(
				"http://sourceforge.net/tracker/?func=add&group_id="
						+ IdConst.PROJECT_ID + "&atid=601040", UIHelper
						.getIcon("sourceforge_logo.png"));
		userPanel.add(sfLabel);
		userPanel.setMaximumSize(new Dimension(2000, 22));
		gameParamPanel.add(userPanel);

		final JPanel summaryPanel = new JPanel();
		summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.X_AXIS));
		summaryPanel.add(new JLabel(LanguageManager.getString("wiz_sf.summary")
				+ " : "));
		subjectTxt = new JTextField();
		summaryPanel.add(subjectTxt);
		summaryPanel.setMaximumSize(new Dimension(2000, 22));
		gameParamPanel.add(summaryPanel);

		StringBuilder buffer = new StringBuilder();
		messageTxt = new JTextArea(
				buffer
						.append("Version : ")
						.append(IdConst.VERSION)
						.append("\nJRE : ")
						.append(System.getProperty("java.runtime.version"))
						.append("\nJVMm : ")
						.append(System.getProperty("java.vm.version"))
						.append("\nOS : ")
						.append(System.getProperty("os.name"))
						.append("\nScreen : ")
						.append(Toolkit.getDefaultToolkit().getScreenSize().width)
						.append("x")
						.append(Toolkit.getDefaultToolkit().getScreenSize().height)
						.append(
								"\n\nConsole output :\n\t[paste here console output(s)]\n\nContext :\n\t[describe when the error appears]")
						.toString());
		final JScrollPane scroll = new JScrollPane(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setAutoscrolls(true);
		scroll.setViewportView(messageTxt);
		gameParamPanel.add(scroll);

		includeScreenShot = new JCheckBox(LanguageManager
				.getString("wiz_bugreport.includeshot"), true);
		JPanel panelTmp = new JPanel();
		panelTmp.setLayout(new BoxLayout(panelTmp, BoxLayout.X_AXIS));
		panelTmp.add(includeScreenShot);
		panelTmp.add(new JLabel(UIHelper.getIcon("snapshot.gif")));
		gameParamPanel.add(panelTmp);

		includeCardProperties = new JCheckBox(LanguageManager
				.getString("wiz_bugreport.includeproperties"));
		includeCardProperties.setEnabled(false);
		includeCardProperties.setToolTipText(LanguageManager.getString(
				"notyetimplemented", "dump card properties"));
		panelTmp = new JPanel();
		panelTmp.setLayout(new BoxLayout(panelTmp, BoxLayout.X_AXIS));
		panelTmp.add(includeCardProperties);
		panelTmp.add(new JLabel(UIHelper.getIcon("cardprops.gif")));
		gameParamPanel.add(panelTmp);

		getRootPane().setDefaultButton(null);
		okBtn.setIconTextGap(5);

		// create screenshot picture
		try {
			// create screenshot resized to 800x600
			Thread.sleep(1000);
			screenShot = (ToolkitImage) new Robot().createScreenCapture(
					new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()))
					.getScaledInstance(800, 600, Image.SCALE_FAST);
		} catch (Exception e) {
			e.printStackTrace();
			includeScreenShot.setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		final Object source = event.getSource();
		if (source == okBtn) {
			List<String> fileList = new ArrayList<String>();
			try {
				Configuration.setProperty("sourceforgeName", userNameTxt.getText());
				if (includeScreenShot.isSelected()) {
					// create screenshot and save captured image to PNG file
					final File tmpPngFile = File.createTempFile("_shot_", ".png",
							new File("").getAbsoluteFile());
					fileList.add(tmpPngFile.getAbsolutePath());
					screenShot.preload(null);
					screenShot.check(null);
					ImageIO.write(screenShot.getBufferedImage(), "png", tmpPngFile);
				}
				if (includeCardProperties.isSelected()) {
					// Include the properties of ALL cards of ALL zones
					final File tmpTxtFile = File.createTempFile("_properties_", ".txt",
							new File(MToolKit.getRootDir()));
					fileList.add(tmpTxtFile.getAbsolutePath());
					final PrintWriter outStream = new PrintWriter(new FileOutputStream(
							tmpTxtFile));
					printProperties(outStream, ZoneManager.stack);
					printProperties(outStream, ZoneManager.side);
					for (int i = StackManager.PLAYERS.length; i-- > 0;) {
						final Player player = StackManager.PLAYERS[i];
						for (int idZone = IdZones.SIDE; idZone-- > 0;) {
							printProperties(outStream, player.zoneManager
									.getContainer(idZone));
						}
					}
				}

				MailUtils.sendEmail("", "bug_report_form@" + IdConst.PROJECT_NAME
						+ ".net", new String[] { "bug_report_jfiremox@yahoo.fr" },
						"Username:" + userNameTxt.getText() + "\nSummary:"
								+ subjectTxt.getText() + "\n\n" + messageTxt.getText(), "["
								+ IdConst.PROJECT_NAME + "] " + subjectTxt.getText(), fileList,
						null, "bug_report", "mx4.mail.yahoo.com");
				// "smtp.mail.yahoo.fr");

				// Clean temp files
				for (int i = fileList.size(); i-- > 0;) {
					MToolKit.getFile(fileList.get(i)).delete();
				}
				dispose();
			} catch (Exception e) {
				e.printStackTrace();
				setVisible(true);
			}
		} else {
			super.actionPerformed(event);
			setVisible(false);
			dispose();
		}
	}

	/**
	 * Print the properties of ALL cards of the specified zone.
	 * 
	 * @param out
	 *          destination output stream.
	 * @param zone
	 *          the zone to dump
	 */
	private static void printProperties(PrintWriter out, MZone zone) {
		out.println("************* ZONE : " + zone.getZoneName() + "*************");
		out.println("Nb = " + zone.getCardCount());
		for (int i = zone.getComponentCount(); i-- > 0;) {
			Component comp = zone.getComponent(i);
			printProperties(out, (MCard) comp);
		}
	}

	/**
	 * Print the properties of ALL cards of the specified zone.
	 * 
	 * @param out
	 *          destination output stream.
	 * @param card
	 *          the card to dump
	 */
	private static void printProperties(PrintWriter out, MCard card) {
		out.println("Card = " + card.getName());
		out.print("\tregisters = [");
		for (int i = 0; i < card.registers.length; i++) {
			out.print("" + card.getValue(i));
			if (i != card.registers.length - 1) {
				out.print(",");
			}
		}
		out.println("]");
		out.print("\tcolor = " + card.getIdColor());
		out.print("\tidCard = " + card.getIdCard());
	}

	/**
	 * Textfield containing sourceforge username
	 */
	private JTextField userNameTxt;

	/**
	 * Textfield containing message
	 */
	private JTextArea messageTxt;

	/**
	 * Textfield containing the summary of this report
	 */
	private JTextField subjectTxt;

	/**
	 * Indicates if screenshot will be sent
	 */
	private JCheckBox includeScreenShot;

	/**
	 * Indicates if properties of all cards will be sent
	 */
	private JCheckBox includeCardProperties;

	private ToolkitImage screenShot;
}
