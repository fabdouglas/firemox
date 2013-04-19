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
package net.sf.firemox.xml.magic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.XMLConstants;

import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class Oracle2XmlNoRules {

	/**
	 * May the validated cards (recycled directory) be patched by the new ones?
	 */
	private static final boolean UPDATE_CARD = false;

	/**
	 * Bounds
	 */
	private static final int MAXI = Integer.MAX_VALUE;

	private static Options options;

	/**
	 * Prevent this class to be instantiated.
	 */
	private Oracle2XmlNoRules() {
		super();
	}

	/**
	 * @param oracleFile
	 *          the oracle format text file.
	 * @param destinationDir
	 *          the directory where built card will be placed.
	 * @param recycledDir
	 *          the directory where already built card are placed.
	 */
	public void serialize(File oracleFile, File destinationDir, File recycledDir) {
		if (!oracleFile.exists() || !oracleFile.isFile()) {
			System.err.println("The file '" + oracleFile + "' does not exist");
			System.exit(-1);
			return;
		}

		if (!destinationDir.exists() || !destinationDir.isDirectory()) {
			System.err.println("The destination directory '" + destinationDir
					+ "' does not exist");
			System.exit(-1);
			return;
		}
		final StringBuilder cardText = new StringBuilder();
		int nbCard = 0;
		MToolKit.tbsName = "norules-mtg";
		try {
			final BufferedReader in = new BufferedReader(new FileReader(oracleFile));
			PrintWriter out = null;
			while (nbCard < MAXI) {
				String line = in.readLine();
				if (line == null)
					break;
				String cardName = line.trim();
				if (cardName.length() == 0) {
					continue;
				}

				// a new card starts
				String fileName = MToolKit.getExactKeyName(cardName) + ".xml";
				if (new File(recycledDir, fileName).exists()) {
					if (UPDATE_CARD) {
						File patchFile = MToolKit.getFile("tbs/norules-mtg/recycled/"
								+ fileName);
						File tempFile = File.createTempFile(fileName, "temp");
						FileUtils.copyFile(patchFile, tempFile);
						final BufferedReader inExist = new BufferedReader(new FileReader(
								tempFile));
						final PrintWriter outExist = new PrintWriter(new FileOutputStream(
								patchFile));
						String lineExist = null;
						boolean found = false;
						while ((lineExist = inExist.readLine()) != null) {
							if (!found && lineExist.contains("name=\"")) {
								lineExist = lineExist
										.substring(0, lineExist.indexOf("name=\""))
										+ "name=\""
										+ cardName
										+ lineExist.substring(lineExist.indexOf("\"", lineExist
												.indexOf("name=\"")
												+ "name=\"".length() + 2));
								found = true;
							}
							outExist.println(lineExist);
						}
						IOUtils.closeQuietly(inExist);
						IOUtils.closeQuietly(outExist);

						if (!found) {
							System.err.println(">> Error patching card '" + cardName + "'");
							// } else {
							// patching card
						}
					}
					skipCard(in);
					continue;
				}

				out = new PrintWriter(new FileOutputStream(new File(destinationDir,
						fileName)));

				// record the card text
				cardText.setLength(0);
				cardText.append("<!--\n\t");

				line = in.readLine().trim().replaceAll("\\(.*\\)", "").toLowerCase();
				cardText.append('\t').append(line).append("\n");

				while (true) {
					line = in.readLine();
					if (line == null || line.length() == 0)
						break;
					line = line.replaceAll("--", "");
					cardText.append('\t').append(line).append('\n');
				}

				cardText.append(" -->");
				out.println("<?xml version='1.0'?>");

				out.print("<card xmlns='");
				out.println(IdConst.NAME_SPACE + "'");
				out.print("\txmlns:xsi='");
				out.print(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
				out.println("'");
				out.print("\txsi:schemaLocation='");
				out.println(IdConst.NAME_SPACE + " ../../" + IdConst.TBS_XSD + "'");
				out.println("\tname=\"" + cardName + "\">");
				out.println("<rules-author-comment>riclas</rules-author-comment>\n");
				out.println(cardText.toString());
				out.println("</card>");
				IOUtils.closeQuietly(out);
				nbCard++;
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error openning file '" + oracleFile + "' : " + e);
		} catch (IOException e) {
			System.err.println("IOError reading file '" + oracleFile + "' : " + e);
		}
		System.out.println("Success Parsing : " + nbCard + " card(s)");
	}

	/**
	 * <ul>
	 * Argument are (in this order :
	 * <li>Oracle source file
	 * <li>destination directory
	 * </ul>
	 * 
	 * @param args
	 */
	public static void main(String... args) {
		options = new Options();
		final CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			// Display help
			if (!options.isHelp()) {
				System.out.println("Wrong parameters : " + e.getMessage());
			} else {
				System.out.println("Usage");
			}
			parser.setUsageWidth(100);
			parser.printUsage(System.out);
			System.exit(-1);
			return;
		}

		if (options.isVersion()) {
			// Display version
			System.out.println("Version is " + IdConst.VERSION);
			System.exit(-1);
			return;
		}

		if (options.isHelp()) {
			// Display help
			System.out.println("Usage");
			parser.setUsageWidth(100);
			parser.printUsage(System.out);
			System.exit(-1);
			return;
		}

		// The oracle source file
		final String oracle = options.getOracleFile();

		// the directory destination end with the file separator
		String destination = FilenameUtils.separatorsToWindows(options
				.getDestination());
		if (!destination.endsWith("/")) {
			destination += "/";
		}

		// Create the destination directories
		try {
			new File(destination).mkdirs();
		} catch (Exception e) {
			// Ignore this error and continue
		}
		new Oracle2XmlNoRules().serialize(MToolKit.getFile(oracle), MToolKit
				.getFile(destination), MToolKit.getFile("tbs/norules-mtg/recycled/"));
	}

	/**
	 * @param in
	 */
	private void skipCard(BufferedReader in) throws IOException {
		String line = in.readLine();
		while (line != null && line.length() > 0) {
			line = in.readLine();
		}
	}
}
