/*
 * Created on Dec 24, 2004
 * 
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public final class Converter {

	/**
	 * Create a new instance of this class.
	 */
	private Converter() {
		super();
	}

	/**
	 * DCK format should be :
	 * <ul>
	 * <li>;comment</li>
	 * <li>...</li>
	 * <li>...\t$number $card-name</li>
	 * </ul>
	 * 
	 * @param directory
	 *          the directory containing the files to translate.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static void convertDCK(String directory) throws IOException {
		final File file = MToolKit.getFile(directory);
		if (!file.isDirectory()) {
			throw new RuntimeException("The given parameter should be a directory");
		}

		File[] list = file.listFiles((FileFilter) FileFilterUtils
				.suffixFileFilter("dck"));
		for (File fileI : list) {
			String fileName = FilenameUtils.removeExtension(fileI.getAbsolutePath())
					+ ".txt";
			FileOutputStream out = new FileOutputStream(fileName);
			InputStream in = new FileInputStream(fileI);
			BufferedReader bufIn = new BufferedReader(new InputStreamReader(in));
			PrintWriter bufOut = new PrintWriter(out);
			String line = null;
			while ((line = bufIn.readLine()) != null) {
				if (line.startsWith(";")) {
					// this is a comment
					bufOut.println("#" + line.substring(1));
				} else if (line.startsWith(".") && line.indexOf('\t') > 1) {
					// this is a line containing card definition
					line = line.substring(line.indexOf('\t') + 1).trim();
					int delim = line.indexOf('\t');
					String cardName = line.substring(delim + 1);
					cardName = cardName.replaceAll(" : ", "_").replaceAll(" :", "_")
							.replaceAll(": ", "_").replaceAll(":", "_").replaceAll(" ", "_")
							.replaceAll("'", "").replaceAll(",", "").replaceAll("-", "");
					bufOut.println(cardName + ";" + line.substring(0, delim));
				}
			}
			IOUtils.closeQuietly(bufOut);
			IOUtils.closeQuietly(bufIn);
		}
	}
}
