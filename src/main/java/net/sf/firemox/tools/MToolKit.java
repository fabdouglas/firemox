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
package net.sf.firemox.tools;

import java.awt.Font;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.token.IdCardColors;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.xml.XmlDeckTranslator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.grip.DragBumpsGripPainter;
import org.jvnet.substance.painter.AlphaControlBackgroundComposite;
import org.mortbay.util.Password;

import sun.misc.BASE64Encoder;

/**
 * Utility class.
 * 
 * @author Fabrice Daugan
 * @author brius
 * @since 0.53
 * @since 0.54 added "save..." methods
 */
public final class MToolKit {

	/**
	 * Return path used to determine the root directory for the development mode.
	 */
	private final static String RESOURCE_DEV = "run.sh";

	/**
	 * Return path used to determine the root directory for the test mode.
	 */
	private final static String RESOURCE_TEST = "foo.test";

	/**
	 * The root directory
	 */
	private static String rootDir = null;

	/**
	 * Create a new instance of this class.
	 */
	private MToolKit() {
		super();
	}

	/**
	 * Return the root directory.
	 * 
	 * @return the root directory.
	 */
	public static String getRootDir() {
		if (rootDir == null) {
			String requiredFile = RESOURCE_DEV;
			URL url = Thread.currentThread().getContextClassLoader().getResource(
					requiredFile);
			if (url == null) {
				requiredFile = RESOURCE_TEST;
				url = Thread.currentThread().getContextClassLoader().getResource(
						requiredFile);
			}
			if (url == null) {
				rootDir = new File("").getAbsolutePath();
			} else {
				try {
					return StringUtils.removeEnd(new File(url.toURI()).getAbsolutePath(),
							requiredFile);
				} catch (URISyntaxException e) {
					rootDir = StringUtils.removeEnd(url.getPath(), requiredFile);
				}
			}
			if (rootDir.startsWith("/")) {
				rootDir = rootDir.substring(1);
			}
			rootDir = FilenameUtils.getFullPathNoEndSeparator(rootDir+"/") + "/";
		}
		return rootDir;
	}

	/**
	 * Replace occurrences into a string.
	 * 
	 * @param data
	 *          the string to replace occurrences into
	 * @param from
	 *          the occurrence to replace.
	 * @param to
	 *          the occurrence to be used as a replacement.
	 * @return the new string with replaced occurrences.
	 */
	public static String replaceAll(String data, String from, String to) {
		if (from.length() > 0 && from.length() > 0) {
			final StringBuilder buf = new StringBuilder(data.length());
			int pos = -1;
			int i = 0;
			while ((pos = data.indexOf(from, i)) != -1) {
				buf.append(data.substring(i, pos)).append(to);
				i = pos + from.length();
			}
			buf.append(data.substring(i));
			return buf.toString();
		}
		return data;
	}

	/**
	 * @param idToken
	 *          the value to translate.
	 * @return the real value of the specified value. If the value is a constant
	 *         the returned value can be positive or negative.
	 */
	public static int getConstant(int idToken) {
		if (idToken != IdConst.ALL
				&& (idToken & 0xFF80) == IdConst.NEGATIVE_NUMBER_MASK) {
			// a negative number
			return -(idToken & 0x7F);
		}
		return idToken;
	}

	/**
	 * The default charset used by this application.
	 */
	public static final String CHARSET = Charset.forName("ISO-8859-1").name();

	/**
	 * read a string from inputStream ending with \0. The maximum size of this
	 * string is 200 chars.
	 * 
	 * @param inputStream
	 *          is the input stream
	 * @return the read string
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public synchronized static String readString(InputStream inputStream)
			throws IOException {
		int count = 0;
		try {
			while ((mBuffer[count] = (byte) inputStream.read()) != 0) {
				count++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IOException("The given data exceed the expected size '"
					+ mBuffer.length + "' reading string '"
					+ new String(mBuffer, 0, count, CHARSET) + "'\n\tdump : '"
					+ ArrayUtils.toString(mBuffer));
		} catch (Exception e) {
			throw new IOException("Exception '" + e + "' reading string '"
					+ new String(mBuffer, 0, count, CHARSET) + "'\n\tdump : '"
					+ ArrayUtils.toString(mBuffer));
		}

		// Return the text from the stream against a specific charset.
		return new String(mBuffer, 0, count, CHARSET);
	}

	/**
	 * write a string to output stream ending with \0.
	 * 
	 * @param out
	 *          is the input stream
	 * @param string
	 *          the string to write
	 */
	public static void writeString(OutputStream out, String string) {
		try {
			if (string != null) {
				out.write(string.getBytes(CHARSET));
			}
			out.write(0);
		} catch (Exception e) {
			throw new InternalError("writing string in file," + e);
		}
	}

	/**
	 * read a string from input The strings read end with \0 with no limit of size
	 * 
	 * @param inputStream
	 *          is the input stream containing content
	 * @return the read string from the specified input stream
	 */
	public static String readText(InputStream inputStream) {
		StringBuffer string = new StringBuffer(100);
		try {
			int intRead;
			while ((intRead = inputStream.read()) != 0) {
				string.append((char) intRead);
			}
		} catch (Exception e) {
			throw new InternalError("reading text in file," + e);
		}
		return string.toString();
	}

	/**
	 * Read a byte array 256*256*256 bytes maximum sized from the given stream.
	 * 
	 * @param inputStream
	 *          is the input stream containing content.
	 * @return the byte array.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static byte[] readByteArray(InputStream inputStream)
			throws IOException {
		final int size = MToolKit.readInt24(inputStream);
		final byte[] array = new byte[size];
		int readCounter = 0;
		while (readCounter != size) {
			readCounter += inputStream.read(array, readCounter, size - readCounter);
		}
		return array;
	}

	/**
	 * Read an icon from the given stream.
	 * 
	 * @param inputStream
	 *          is the input stream containing content.
	 * @return the read icon.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static ImageIcon readImageIcon(InputStream inputStream)
			throws IOException {
		return new ImageIcon(readByteArray(inputStream));
	}

	/**
	 * Read an image from the given stream.
	 * 
	 * @param inputStream
	 *          is the input stream containing content.
	 * @return the read image.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static Image readImage(InputStream inputStream) throws IOException {
		return Toolkit.getDefaultToolkit().createImage(readByteArray(inputStream));
	}

	/**
	 * Return 0 or 1 at random way
	 * 
	 * @return 0 or 1 at random way
	 */
	public static int getRandom() {
		return getRandom(2);
	}

	/**
	 * Return a number [0,length[ at random way
	 * 
	 * @param length
	 *          maximum+1 value to return.
	 * @return a number [0,length[ at random way
	 */
	public static int getRandom(int length) {
		lastRandom = random.nextInt(length);
		return lastRandom;
	}

	/**
	 * Copy the source file throw the output stream.
	 * 
	 * @param src
	 *          the source file.
	 * @param out
	 *          the output stream.
	 * @throws IOException
	 */
	public static void writeFile(File src, OutputStream out) throws IOException {
		InputStream in = new FileInputStream(src);
		MToolKit.writeInt24(out, (int) src.length());

		// Transfer bytes from in to out
		final byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) >= 0) {
			out.write(buf, 0, len);
		}
		IOUtils.closeQuietly(in);
	}

	/**
	 * file filter used for MDB files
	 */
	private static FileFilterPlus mdbFilter = new FileFilterPlus("mdb",
			"MDB File For Firemox");

	/**
	 * file filter used for deck files
	 */
	private static FileFilterPlus txtFilter = new FileFilterPlus("txt",
			"TXT format: CardName;qty");

	/**
	 * file filter used for JPG pictures
	 */
	private static FileFilterPlus pictureFilter = new FileFilterPlus(
			new String[] { "jpg", "gif", "png" }, "Images");

	/**
	 * Return the deck file chosen with an "open" dialog.
	 * 
	 * @return the file chosen, or the previous one if canceled.
	 */
	public static String getDeckFile() {
		return getDeckFile(JFileChooser.OPEN_DIALOG);
	}

	/**
	 * Return the deck file chosen
	 * 
	 * @param type
	 *          open or save option
	 * @return the file chosen, or the previous one if canceled.
	 */
	public static String getDeckFile(int type) {
		return getDeckFile(MagicUIComponents.magicForm, type);
	}

	/**
	 * Return the deck file chosen
	 * 
	 * @param parent
	 *          is parent of this dialog
	 * @param type
	 *          open or save option
	 * @return the file chosen, or the previous one if canceled.
	 */
	public static String getDeckFile(JFrame parent, int type) {
		String deckFile = Configuration.getString("decks.deck(0)", "");
		try {
			File file = MToolKit.getFile(deckFile);
			if (file == null) {
				file = showDialogFile("choose your deck file", 'o', null, txtFilter,
						parent, type);
			} else {
				file = showDialogFile("choose your deck file", 'o', file
						.getAbsoluteFile(), txtFilter, parent, type);
			}

			if (file != null) {
				deckFile = getShortDeckFile(file.getCanonicalPath());
			} else
				return null;
			return deckFile;

		} catch (IOException e) {
			JOptionPane.showMessageDialog(parent,
					"Error occurred reading the specified file", "File problem",
					JOptionPane.ERROR_MESSAGE);
			return Configuration.getString("decks.deck(0)", "");
		} catch (Exception e) {
			// cancel of user;
			return null;
		}
	}

	/**
	 * Open a DialogFile dialog and return the chosen MDB file.
	 * 
	 * @param oldFile
	 *          the old MDB file, will be returned if user cancel operation
	 * @param parent
	 *          is parent of this dialog
	 * @return the file chosen, or oldFile if canceled
	 */
	public static String getMdbFile(String oldFile, JFrame parent) {
		try {
			return showDialogFile("choose your mdb file", 'o',
					oldFile == null ? null : MToolKit.getFile(oldFile), mdbFilter,
					parent, JFileChooser.OPEN_DIALOG).getCanonicalPath();
		} catch (java.io.IOException e) {
			javax.swing.JOptionPane.showMessageDialog(parent,
					"Error occurred reading the specified file", "File problem",
					JOptionPane.ERROR_MESSAGE);
			return oldFile;
		} catch (Exception e) {
			// cancel of user;
			return null;
		}
	}

	/**
	 * return the picture file chosen
	 * 
	 * @param oldFile
	 *          the old picture, will be returned if user cancel operation
	 * @param parent
	 *          is parent of this dialog
	 * @return the picture file chosen, or oldFile if canceled
	 */
	public static String getPictureFile(String oldFile, JFrame parent) {
		try {
			return showDialogFile("choose your a picture", 'o', null, pictureFilter,
					parent, JFileChooser.OPEN_DIALOG).getCanonicalPath();
		} catch (java.io.IOException e) {
			throw new InternalError("choosing picture file");
		} catch (Exception e) {
			// cancel of user;
			return null;
		}
	}

	/**
	 * Display a file dialog with many options
	 * 
	 * @param titreBoite
	 *          title of this dialog box
	 * @param etiquetteBouton
	 *          button "yes"
	 * @param infoBulle
	 *          tooltip
	 * @param raccourciBouton
	 *          shortcut for "yes" button
	 * @param fichier
	 *          old file
	 * @param fileFilter
	 *          file filter to apply
	 * @param parent
	 *          is parent of this dialog
	 * @param type
	 *          the dialog type
	 * @return the File object corresponding to the user's choice
	 * @see JFileChooser#setDialogType(int)
	 */
	private static File showDialogFile(String titreBoite, char raccourciBouton,
			File fichier, FileFilterPlus fileFilter, JFrame parent, int type) {
		return showDialogFile(titreBoite, raccourciBouton, fichier, fileFilter,
				parent, type, JFileChooser.FILES_AND_DIRECTORIES);
	}

	/**
	 * Display a file dialog with many options
	 * 
	 * @param titreBoite
	 *          title of this dialog box
	 * @param raccourciBouton
	 *          shortcut for "yes" button
	 * @param fichier
	 *          old file
	 * @param fileFilter
	 *          file filter to apply
	 * @param parent
	 *          is parent of this dialog
	 * @param type
	 *          the dialog type
	 * @param mode
	 * @return the File object corresponding to the user's choice
	 * @see JFileChooser#setDialogType(int)
	 */
	public static File showDialogFile(String titreBoite, char raccourciBouton,
			File fichier, FileFilterPlus fileFilter, JFrame parent, int type, int mode) {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
		}
		fileChooser.setDialogType(type);
		fileChooser.setDialogTitle(titreBoite);
		fileChooser.setApproveButtonMnemonic(raccourciBouton);
		if (fileFilter != null) {
			fileFilter.addExtension(".");
			fileChooser.setFileFilter(fileFilter);
		}
		fileChooser.setFileSelectionMode(mode);
		fileChooser.rescanCurrentDirectory();
		fileChooser.setSelectedFile(fichier);
		/*
		 * if (filePreview != null) { filePreview.setFileChooser(fileChooser);
		 * fileChooser.setAccessory(filePreview); }
		 */

		int resultat = fileChooser.showDialog(parent, null);

		return resultat == JFileChooser.APPROVE_OPTION ? fileChooser
				.getSelectedFile() : null;
	}

	/**
	 * An utility function that layers on top of the LookAndFeel's
	 * isSupportedLookAndFeel() method. Returns true if the LookAndFeel is
	 * supported. Returns false if the LookAndFeel is not supported and/or if
	 * there is any kind of error checking if the LookAndFeel is supported. The
	 * L&F menu will use this method to detemine whether the various L&F options
	 * should be active or inactive.
	 * 
	 * @param laf
	 *          L1F name
	 * @return true if successfully loaded
	 */
	public static boolean isAvailableLookAndFeel(String laf) {
		try {
			Class.forName(laf);
			// final Class lnfClass = Class.forName(laf);
			// final LookAndFeel newLAF = (LookAndFeel) (lnfClass.newInstance());
			// return newLAF.isSupportedLookAndFeel();
			return true;
		} catch (Exception e) { // If ANYTHING weird happens, return false
			// try {
			// final Class lnfClass2 = JarClassLoader.getInstance().loadClass(laf);
			// final LookAndFeel newLAF = (LookAndFeel) (lnfClass2.newInstance());
			// return newLAF.isSupportedLookAndFeel();
			// } catch (Exception e2) { // If ANYTHING weird happens, return false
			return false;
			// }
		}
	}

	/**
	 * Return the L&F instance from the givent name.
	 * 
	 * @param laf
	 *          the L&F class name
	 * @return the L&F instance
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static LookAndFeel geLookAndFeel(String laf)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		final Class<?> lnfClass = Class.forName(laf);
		return (LookAndFeel) (lnfClass.newInstance());
	}

	/**
	 * Read the next 2 bytes from the specified input stream and return the
	 * integer value coded with 32bits
	 * 
	 * @param input
	 *          is the file containing the short (2bytes)to read
	 * @return the read integer built with the 2 next bytes read from the
	 *         specified input stream
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public static int readInt16(InputStream input) throws IOException {
		int res = input.read() << 8;
		return res + input.read();
	}

	/**
	 * Read the next 2 bytes from the specified input stream and return the
	 * integer value coded with 32bits
	 * 
	 * @param high
	 *          the high byte.
	 * @param low
	 *          the low byte.
	 * @return the read integer built with the 2 next bytes read from the
	 *         specified input stream
	 */
	public static int readInt16(byte high, byte low) {
		int res = high << 8;
		return res + low;
	}

	/**
	 * Read the next 3 bytes from the specified input stream and return the
	 * integer value coded with 48bits
	 * 
	 * @param input
	 *          is the file containing the short (3bytes)to read
	 * @return the read integer built with the 3 next bytes read from the
	 *         specified input stream
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public static int readInt24(InputStream input) throws IOException {
		int res = input.read() << 16;
		res += input.read() << 8;
		return res + input.read();
	}

	/**
	 * Write the specified positive short coded on 2 bytes to the specified input
	 * stream
	 * 
	 * @param out
	 *          is the output stream where the specified integer would be written
	 * @param int16
	 *          the 2 bytes integer to write
	 */
	public static void writeInt16(OutputStream out, int int16) {
		try {
			out.write(int16 / 256);
			out.write(int16 % 256);
		} catch (Exception e) {
			throw new InternalError("writing int16 in file," + e);
		}
	}

	/**
	 * Write the specified positive integer coded on 3 bytes to the specified
	 * input stream
	 * 
	 * @param out
	 *          is the output stream where the specified integer would be written
	 * @param int24
	 *          the 3 bytes integer to write
	 */
	public static void writeInt24(OutputStream out, int int24) {
		try {
			out.write(int24 / 65536);
			out.write(int24 % 65536 / 256);
			out.write(int24 % 256);
		} catch (Exception e) {
			throw new InternalError("writing int24 in file," + e);
		}
	}

	/**
	 * Create, and return the connection established with a http server. May use a
	 * http proxy if the settings have been set
	 * 
	 * @return the SMTP properties.
	 */
	public static Properties getSmtpProperties() {
		if (Configuration.getBoolean("useProxy", false)) {
			// we use the proxy configuration
			System.setProperty("socksProxySet", "true");
			System.setProperty("socksProxyHost", Configuration.getString("proxyHost",
					"192.168.0.252"));
			System.setProperty("socksProxyPort", "25");
			final String clearLoginPwd = Password.deobfuscate(Configuration
					.getString("proxyObfuscatedLoginPwd", "anonymous:"));
			System.setProperty("socksProxyUserName", clearLoginPwd.substring(0,
					clearLoginPwd.indexOf(':')));
			System.setProperty("socksProxyPassword", clearLoginPwd
					.substring(clearLoginPwd.indexOf(':') + 1));

			System.setProperty("socks.useProxy", "true");
			System.setProperty("socks.proxyHost", System
					.getProperty("socksProxyHost"));
			System.setProperty("socks.proxyPort", System
					.getProperty("socksProxyPort"));
			System.setProperty("socks.proxyUserName", System
					.getProperty("socksProxyUserName"));
			System.setProperty("socks.proxyPassword", System
					.getProperty("socksProxyPassword"));
		}
		return System.getProperties();
	}

	/**
	 * Create, and return the connection established with a http server. May use a
	 * http proxy if the settings have been set
	 * 
	 * @param url
	 *          the requested url.
	 * @return Http connection.
	 */
	public static URLConnection getHttpConnection(URL url) {
		try {
			if (Configuration.getBoolean("useProxy", false)) {
				// we use the proxy configuration
				Properties systPrp = System.getProperties();
				systPrp.put("proxySet", "true");
				systPrp.put("http.proxyHost", Configuration.getString("proxyHost",
						"192.168.0.252"));
				systPrp.put("http.proxyPort", Configuration.getString("proxyPort",
						"1299"));
				System.setProperties(systPrp);

				HttpURLConnection uc = (HttpURLConnection) url.openConnection();
				BASE64Encoder encoder = new sun.misc.BASE64Encoder();
				String encodedUserPwd = encoder.encode(Password.deobfuscate(
						Configuration.getString("proxyObfuscatedLoginPwd", "")).getBytes());
				if (encodedUserPwd.length() > 0) {
					uc.setRequestProperty("Proxy-Authorization", "Basic "
							+ encodedUserPwd);
				}
				uc.connect();
				return uc;
			}
			return url.openConnection();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Return the loaded picture from a local place.
	 * 
	 * @param localFile
	 *          the local file name.
	 * @return the local picture.
	 * @throws InterruptedException
	 */
	public static Image getLocalPicture(String localFile)
			throws InterruptedException {
		final Image result = Toolkit.getDefaultToolkit().getImage(
				getFile(localFile, true).getAbsolutePath());
		if (result == null) {
			throw new InterruptedException("Picture " + localFile
					+ " has not been found");
		}
		final MediaTracker tracker = new MediaTracker(MagicUIComponents.magicForm);
		tracker.addImage(result, 0);
		tracker.waitForAll();
		if (tracker.isErrorAny()) {
			tracker.removeImage(result, 0);
			tracker.waitForAll();
			result.flush();
			throw new InterruptedException("Malformed picture " + localFile);
		}
		return result;
	}

	/**
	 * Return the picture specific to the current TBS
	 * 
	 * @param pictureFile
	 *          the path of picture to load. This file must not begin with '/'
	 * @return the picture specific to the current TBS
	 */
	public static String getTbsHtmlPicture(String pictureFile) {
		return MToolKit.getTbsPicture(pictureFile, false).replace('\\', '/');
	}

	/**
	 * Return the picture specific to the current TBS
	 * 
	 * @param pictureFile
	 *          the path of picture to load. This file must not begin with '/'
	 * @return the picture specific to the current TBS
	 */
	public static String getTbsPicture(String pictureFile) {
		return getTbsPicture(pictureFile, true);
	}

	/**
	 * Return the picture specific to the current TBS
	 * 
	 * @param pictureFile
	 *          the path of picture to load. This file must not begin with '/'
	 * @param mustExist
	 *          <code>true</code> if the resource to search must exists,
	 *          <code>false</code> either
	 * @return the picture specific to the current TBS
	 */
	public static String getTbsPicture(String pictureFile, boolean mustExist) {
		final File file = getTbsFile(IdConst.IMAGES_DIR + pictureFile, mustExist);
		if (file == null) {
			if (mustExist) {
				return null;
			}
			return pictureFile;
		}
		return file.getAbsolutePath();
	}

	/**
	 * Return the mana picture specific to the current TBS
	 * 
	 * @param idColor
	 *          the color of mana
	 * @return the mana picture specific to the current TBS
	 */
	public static ImageIcon getTbsBigManaPicture(int idColor) {
		if (idColor == 0)
			return new ImageIcon(getTbsPicture("mana/colorless/big/"
					+ MdbLoader.colorlessBigURL));
		return new ImageIcon(getTbsPicture("mana/colored/big/"
				+ MdbLoader.coloredBigManas[idColor]));
	}

	/**
	 * Return the mana picture specific to the current TBS
	 * 
	 * @param idColor
	 *          the color of mana
	 * @param amount
	 *          requested amount.
	 * @return the mana picture specific to the current TBS
	 */
	public static String getHtmlMana(int idColor, int amount) {
		if (idColor == IdCommonToken.COLORLESS_MANA) {
			if (amount >= MdbLoader.colorlessSmlManas.length) {
				return MdbLoader.colorlessSmlManasHtml[MdbLoader.colorlessSmlManas.length - 1]
						+ getHtmlMana(idColor, amount - MdbLoader.colorlessSmlManas.length
								+ 1);
			} else if (amount == -1) {
				return MdbLoader.unknownSmlManaHtml;
			}
			return MdbLoader.colorlessSmlManasHtml[amount];

		}
		if (amount == -1) {
			return MdbLoader.unknownSmlManaHtml;
		}

		String res = "";
		for (int i = amount; i-- > 0;) {
			res += MdbLoader.coloredSmlManasHtml[idColor];
		}
		return res;
	}

	/**
	 * Return the file specific to the current TBS
	 * 
	 * @param file
	 *          the path of file to load. This file must not begin with '/'
	 * @return the picture specific to the current TBS
	 */
	public static File getTbsFile(String file) {
		return getTbsFile(file, true);
	}

	/**
	 * Return the URL specific to the current TBS
	 * 
	 * @param file
	 *          the path of file to load. This file must not begin with '/'
	 * @return the URL specific to the current TBS
	 */
	public static URL getTbsUrl(String file) {
		return Thread.currentThread().getContextClassLoader().getResource(
				new StringBuilder(IdConst.TBS_DIR).append("/").append(tbsName).append(
						"/").append(file).toString());
	}

	/**
	 * Return the URL corresponding file name.
	 * 
	 * @param file
	 *          the path of file to load. This file must not begin with '/'
	 * @return the URL corresponding file name.
	 */
	public static URL getUrl(String file) {
		return Thread.currentThread().getContextClassLoader().getResource(file);
	}

	/**
	 * Return the file specific to the current TBS
	 * 
	 * @param file
	 *          the path of file to load. This file must not begin with '/'
	 * @param mustExist
	 *          <code>true</code> if the resource to search must exists,
	 *          <code>false</code> either
	 * @return the file specific to the current TBS
	 */
	public static File getTbsFile(String file, boolean mustExist) {
		return getFile(new StringBuilder(IdConst.TBS_DIR).append("/").append(
				tbsName).append("/").append(file).toString(), mustExist);
	}

	/**
	 * Return the sound specific to the current TBS
	 * 
	 * @param soundFile
	 *          the path of picture to load. This file must not begin with '/'
	 * @return the sound specific to the current TBS
	 */
	public static String getSoundFile(String soundFile) {
		return MToolKit.getTbsFile(IdConst.SOUNDS_DIR + soundFile)
				.getAbsolutePath();
	}

	/**
	 * loadClip loads the sound-file into a clip.
	 * 
	 * @param soundFile
	 *          file to be loaded and played.
	 */
	public static void loadClip(String soundFile) {
		AudioFormat audioFormat = null;
		AudioInputStream actionIS = null;
		try {
			// actionIS = AudioSystem.getAudioInputStream(input); // Does not work !
			actionIS = AudioSystem.getAudioInputStream(MToolKit.getFile(MToolKit
					.getSoundFile(soundFile)));
			AudioFormat.Encoding targetEncoding = AudioFormat.Encoding.PCM_SIGNED;
			actionIS = AudioSystem.getAudioInputStream(targetEncoding, actionIS);
			audioFormat = actionIS.getFormat();

		} catch (UnsupportedAudioFileException afex) {
			Log.error(afex);
		} catch (IOException ioe) {

			if (ioe.getMessage().equalsIgnoreCase("mark/reset not supported")) { // Ignore
				Log.error("IOException ignored.");
			}
			Log.error(ioe.getStackTrace());
		}

		// define the required attributes for our line,
		// and make sure a compatible line is supported.

		// get the source data line for play back.
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		if (!AudioSystem.isLineSupported(info)) {
			Log.error("LineCtrl matching " + info + " not supported.");
			return;
		}

		// Open the source data line for play back.
		try {
			Clip clip = null;
			try {
				Clip.Info info2 = new Clip.Info(Clip.class, audioFormat);
				clip = (Clip) AudioSystem.getLine(info2);
				clip.open(actionIS);
				clip.start();
			} catch (IOException ioe) {
				Log.error(ioe);
			}
		} catch (LineUnavailableException ex) {
			Log.error("Unable to open the line: " + ex);
			return;
		}
	}

	/**
	 * Represents the default font
	 */
	public static Font defaultFont;

	/**
	 * Represents the MDB file corresponding to the current TBS name. Is null if
	 * no TBS is currently defined.
	 */
	public static String mdbFile;

	/**
	 * Represents the default MDB name. This is not the full name of selected TBS.
	 */
	public static String tbsName;

	/**
	 * represents the file chooser
	 */
	public static JFileChooser fileChooser;

	private static byte[] mBuffer = new byte[200];

	static int lastRandom = 0; // Last integer generated

	/**
	 * The current random sequence. May be initialized at the beginning of each
	 * play.
	 */
	public static Random random = new Random(); // Random generator

	/**
	 * Return absolute location of given component.
	 * 
	 * @param component
	 *          the component to locate
	 * @return the absolute POINT location of given component.
	 */
	public static Point getAbsoluteLocation(JComponent component) {
		return SwingUtilities.convertPoint(component, 0, 0,
				MagicUIComponents.magicForm.getContentPane());
	}

	/**
	 * Parses the string argument as a signed integer in the radix specified by
	 * the second argument. The characters in the string must all be digits of 10
	 * radix (as determined by whether
	 * {@link java.lang.Character#digit(char, int)} returns a nonnegative value),
	 * except that the first character may be an ASCII minus sign <code>'-'</code> (<code>'&#92;u002D'</code>)
	 * to indicate a negative value. The resulting integer value is returned.
	 * <p>
	 * The <code>Integer#MIN_VALUE</code> value is returned if any of the
	 * following situations occurs:
	 * <ul>
	 * <li>The first argument is <code>null</code> or is a string of length
	 * zero.
	 * <li>Any character of the string is not a digit of the specified radix,
	 * except that the first character may be a minus sign <code>'-'</code> (<code>'&#92;u002D'</code>)
	 * provided that the string is longer than length 1.
	 * <li>The value represented by the string is not a value of type
	 * <code>int</code>.
	 * </ul>
	 * <p>
	 * Examples: <blockquote> parseInt(&quot;0&quot;) returns 0 <br>
	 * parseInt(&quot;473&quot;) returns 473 <br>
	 * parseInt(&quot;-0&quot;) returns 0 <br>
	 * parseInt(&quot;2147483647&quot;) returns 2147483647 <br>
	 * parseInt(&quot;-2147483648&quot;) returns -2147483648 <br>
	 * parseInt(&quot;2147483648&quot;) returns Integer#MIN_VALUE <br>
	 * parseInt(&quot;Kona&quot;) returns Integer#MIN_VALUE <br>
	 * </blockquote>
	 * 
	 * @param s
	 *          the <code>String</code> containing the integer representation to
	 *          be parsed
	 * @return the integer represented by the string argument in the 10 radix.
	 *         Return Integer.MIN_VALUE when error
	 * @see Integer#MIN_VALUE
	 */
	public static int parseInt(String s) {
		if (s == null) {
			return Integer.MIN_VALUE;
		}
		int result = 0;
		boolean negative = false;
		int i = 0;
		int max = s.length();
		int limit;
		int multmin;
		int digit;

		if (max > 0) {
			if (s.charAt(0) == '-') {
				negative = true;
				limit = Integer.MIN_VALUE;
				i++;
			} else {
				limit = -Integer.MAX_VALUE;
			}
			multmin = limit / 10;
			if (i < max) {
				digit = Character.digit(s.charAt(i++), 10);
				if (digit < 0) {
					return Integer.MIN_VALUE;
				}
				result = -digit;
			}
			while (i < max) {
				// Accumulating negatively avoids surprises near MAX_VALUE
				digit = Character.digit(s.charAt(i++), 10);
				if (digit < 0) {
					return Integer.MIN_VALUE;
				}
				if (result < multmin) {
					return Integer.MIN_VALUE;
				}
				result *= 10;
				if (result < limit + digit) {
					return Integer.MIN_VALUE;
				}
				result -= digit;
			}
		} else {
			return Integer.MIN_VALUE;
		}
		if (negative) {
			if (i > 1) {
				return result;
			}
			/* Only got "-" */
			return Integer.MIN_VALUE;
		}
		return -result;
	}

	/**
	 * Return the canonical path of working directory.
	 * 
	 * @return the canonical path of working directory.
	 * @throws IOException
	 */
	public static String getRelativePath() throws IOException {
		return new File("").getAbsoluteFile().getCanonicalPath().replace('\\', '/');
	}

	/**
	 * Return the specified card name without any special char. The returned name
	 * may be used to build a file. The returned string is in lower case.
	 * 
	 * @param cardName
	 *          the card name as it is displayed.
	 * @return the specified card name without any special char.
	 */
	public static String getKeyName(String cardName) {
		return getExactKeyName(cardName).toLowerCase();
	}

	/**
	 * Return the specified card name without any special char. The returned name
	 * may be used to build a file.
	 * 
	 * @param cardName
	 *          the card name as it is displayed.
	 * @return the specified card name without any special char.
	 */
	public static String getExactKeyName(String cardName) {
		if (translator == null) {
			try {
				translator = new XmlDeckTranslator(MToolKit.tbsName);
			} catch (Exception e1) {
				e1.printStackTrace();
				Log.warn("NO DECK TRANSLATOR FOUND");
			}
		}
		return translator.convert(cardName);
	}

	/**
	 * Returns the given path with '\' char replaced by '/' char.
	 * 
	 * @param canonicalPath
	 * @return the given path with '\' char replaced by '/' char.
	 * @throws IOException
	 */
	public static String getRelativePath(String canonicalPath) throws IOException {
		if (canonicalPath.replace('\\', '/').startsWith(getRelativePath())) {
			return canonicalPath.substring(getRelativePath().length() + 1).replace(
					'\\', '/');
		}
		return canonicalPath;
	}

	/**
	 * Return logging info of the given card.
	 * 
	 * @param card
	 *          the card to Log.
	 * @return logging info of the given card.
	 */
	public static String getLogCardInfo(MCard card) {
		if (card != null) {
			if (card != SystemCard.instance) {
				return new StringBuilder(", card=").append(card.getName()).append(
						"@" + Integer.toHexString(card.hashCode())).toString();
			}
			return ", card=" + card.getName();
		}
		return "";
	}

	/**
	 * The translator transforming a card name into a file-serializable value. Is
	 * <code>null</code> while not used.
	 */
	public static XmlDeckTranslator translator = null;

	/**
	 * Return the specified string without any local white spaces. Would be
	 * replaced when
	 * {@link org.apache.commons.lang.StringUtils#deleteWhitespace(String)} would
	 * work.
	 * 
	 * @param string
	 *          the string to normalize.
	 * @return the given string without any space char.
	 * @see Character#isWhitespace(char)
	 */
	public static String replaceWhiteSpaces(String string) {
		final StringBuilder workingString = new StringBuilder(StringUtils
				.deleteWhitespace(string));
		for (int count = workingString.length(); count-- > 0;) {
			if (Character.isSpaceChar(workingString.charAt(count))) {
				// delete this char
				workingString.deleteCharAt(count);
			}
		}
		return workingString.toString();
	}

	/**
	 * Return the stream associated to the given resource.
	 * 
	 * @param resource
	 *          the resource path to search.
	 * @return the stream associated to the given resource.
	 */
	public static InputStream getResourceAsStream(String resource) {
		if (new File(resource).exists())
			try {
				return new FileInputStream(resource);
			} catch (Exception e) {
				// Error, so ignore it and get data from another way
			}
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(
				resource);
	}

	/**
	 * Return the File associated to the given fileName.
	 * 
	 * @param fileName
	 *          the file name path to search.
	 * @param mustExist
	 *          <code>true</code> if the resource to search must exists,
	 *          <code>false</code> either
	 * @return the File associated to the given fileName.
	 */
	public static File getFile(String fileName, boolean mustExist) {
		final File simpleFile = new File(fileName);
		if (simpleFile.exists()) {
			return simpleFile.getAbsoluteFile();
		}

		String file = fileName;

		if (file.startsWith("/")) {
			file = file.substring(1);
		}

		final File f = new File(getRootDir() + file);
		if (!f.exists() && mustExist) {
			return null;
		}
		return f;
	}

	/**
	 * Return the File associated to the given fileName.
	 * 
	 * @param fileName
	 *          the file name path to search.
	 * @return the File associated to the given fileName.
	 */
	public static File getFile(String fileName) {
		return getFile(fileName, true);
	}

	/**
	 * Returns the absolute path of an icon used for the UI.
	 * 
	 * @param fileName
	 *          the icon path relative to the images directory
	 * @return the absolute path of an icon used for the UI
	 */
	public static String getIconPath(String fileName) {
		File file = getFile(IdConst.IMAGES_DIR + fileName);
		if (file == null)
			return "";
		return file.getAbsolutePath();
	}

	/**
	 * Return the given deck file without the current path off application. If the
	 * given deck is valid, it becomes the new deck reference.
	 * 
	 * @param deckFile
	 *          the full deck file.
	 * @return the given deck file without the current path off application.
	 */
	public static String getShortDeckFile(String deckFile) {
		String fullDeckFile = deckFile.replace('\\', '/');
		String shortName = fullDeckFile;
		try {
			if (fullDeckFile.toUpperCase().startsWith(
					MToolKit.getRelativePath().toUpperCase())) {
				shortName = fullDeckFile.substring(
						MToolKit.getRelativePath().length() + 1).replace('\\', '/');
			}
		} catch (IOException e) {
			// we'll use the full deck name
		}
		Configuration.addRecentProperty("decks.deck", shortName);
		return shortName;
	}

	/**
	 * Specifies that a component should have overlay functionality.
	 * 
	 * @param superPanel
	 *          the panel to overlay.
	 */
	public static void addOverlay(JComponent superPanel) {
		/*
		 * superPanel.putClientProperty(SubstanceLookAndFeel.OVERLAY_PROPERTY,
		 * Boolean.TRUE);
		 */
		superPanel.putClientProperty(SubstanceLookAndFeel.BACKGROUND_COMPOSITE,
				new AlphaControlBackgroundComposite(0.3f, 0.5f));
		superPanel.putClientProperty(SubstanceLookAndFeel.GRIP_PAINTER,
				new DragBumpsGripPainter());
	}

	/**
	 * Sum the mana.
	 * 
	 * @param registers
	 *          the array of mana.
	 * @return the sum of mana.
	 */
	public static int manaPool(int[] registers) {
		int res = 0;
		for (int i = IdCardColors.CARD_COLOR_NAMES.length; i-- > 0;) {
			res += registers[i];
		}
		return res;
	}

}