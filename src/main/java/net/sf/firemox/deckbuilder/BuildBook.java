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
package net.sf.firemox.deckbuilder;

import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.firemox.tools.MToolKit;

import org.apache.commons.io.IOUtils;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPRow;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author <a href="mailto:kismet-sl@users.sourceforge.net">Stefano "Kismet"
 *         Lenzi</a>
 * @since 0.91
 */
public class BuildBook {

	private String cachedImageDirStore;

	private ArrayList<String> cachedImageDir;

	private ArrayList<String> imageSources;

	private String checklist;

	private String pdfBook;

	private String dedfaultImage;

	private HashMap<String, Integer> basicLands;

	BuildBook() {
		cachedImageDirStore = "src/main/resources/tbs/mtg/images/gp";
		dedfaultImage = cachedImageDirStore + File.pathSeparator
				+ "magic_the_gathering.jpg";
		cachedImageDir = new ArrayList<String>();
		cachedImageDir.add(cachedImageDirStore);
		// cachedImageDir.add("src/main/resources/tbs/mtg/images");

		imageSources = new ArrayList<String>();
		imageSources
				.add("http://www.wizards.com/global/images/magic/general/{name}.jpg");

		checklist = "etc/cardlists/list-gp.txt";

		pdfBook = "etc/pdf/Guildpact.pdf";

		basicLands = new HashMap<String, Integer>();
		basicLands.put("Forest", 1);
		basicLands.put("Island", 1);
		basicLands.put("Swamp", 1);
		basicLands.put("Plains", 1);
		basicLands.put("Mountain", 1);
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void build() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					MToolKit.getFile(checklist)), Charset.forName("ISO-8859-1")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		// String charset = Charset.forName("ISO-8859-1").name();
		String line = null;
		int cur = 0;
		int row = 0;
		PdfPCell[] cells = new PdfPCell[3];
		PdfPTable table = new PdfPTable(3);
		Document document = new Document(PageSize.A4);
		try {
			PdfWriter.getInstance(document, new FileOutputStream(this.pdfBook));
			document.open();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		try {
			in.readLine(); // Removing header line
			String[] tokens = new String[] { "\t", "\\s{2,}" };
			while ((line = in.readLine()) != null) {
				// line = new String(line.getBytes(),charset);
				// Card# Card Name Artist Color Rarity
				String[] fields = null;
				for (String token : tokens) {
					fields = line.split(token);
					if (fields.length == 5) {
						break;
					}
				}

				if (fields == null || fields.length < 5) {
					System.err.println("Unable to parse " + line);
					continue;
				} else if (fields.length > 5) {
					System.out.println("Too many value found on " + line);
				}
				fields[1] = fields[1].trim();
				fields[1] = fields[1].replaceAll("[ -]", "_");
				fields[1] = fields[1].replaceAll("[/',\\u0092]", "");
				fields[1] = fields[1].replaceAll("Æ", "AE");
				System.out.println("Inserting " + fields[1]);
				if (basicLands.containsKey(fields[1])) {
					int x = basicLands.get(fields[1]);
					cells[cur] = new PdfPCell(getImage(fields[1] + x), true);
					x++;
					basicLands.put(fields[1], x);
				} else {
					cells[cur] = new PdfPCell(getImage(fields[1]), true);
				}
				cur++;
				if (cur == 3) {
					for (int j = 0; j < cells.length; j++) {
						cells[j].setPadding(2.0f);
					}
					table.getRows().add(new PdfPRow(cells));
					row++;
					cur = 0;
					cells = new PdfPCell[3];
				}
				if (row == 3) {
					table.setWidthPercentage(100);
					document.add(table);
					table = new PdfPTable(3);
					row = 0;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		document.close();
	}

	/**
	 * @param cardName
	 *          the key card name.
	 * @return the image associated to this card
	 * @throws BadElementException
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	@SuppressWarnings("null")
	public Image getImage(String cardName) throws BadElementException,
			IOException {
		Iterator<String> i = null;
		i = cachedImageDir.iterator();
		File img = null;
		while (i.hasNext()) {
			String[] extensions = new String[] { ".jpg", ".jpeg", ".png", ".gif",
					".tiff" };
			String base = i.next();
			for (int j = 0; j < extensions.length; j++) {
				img = new File(base, cardName.toLowerCase() + extensions[j]);
				if (img.exists()) {
					break;
				}
				img = null;
			}
			if (img != null)
				break;
			img = null;
		}
		if (img != null) {
			java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(
					img.getAbsolutePath());
			java.awt.Image awtImageX4 = awtImage.getScaledInstance(800, -1,
					java.awt.Image.SCALE_SMOOTH);
			awtImage = null;
			return Image.getInstance(awtImageX4, null);
		}

		i = imageSources.iterator();
		while (i.hasNext()) {
			String urlpath = i.next();
			urlpath = urlpath.replace("{name}", cardName);
			URL url = new URL(urlpath);
			URLConnection cn = url.openConnection();
			if (cn.getContentType() == null
					|| cn.getContentType().startsWith("image")
					&& cn.getInputStream() != null) {
				img = new File(this.cachedImageDirStore, cardName.toLowerCase()
						+ ".jpg");
				BufferedOutputStream fw = new BufferedOutputStream(
						new FileOutputStream(img));
				BufferedInputStream is = new BufferedInputStream(cn.getInputStream());

				IOUtils.copy(is, fw);
				fw.close();

				java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(
						img.getAbsolutePath());
				java.awt.Image awtImageX4 = null;
				awtImageX4 = awtImage.getScaledInstance(800, -1,
						java.awt.Image.SCALE_SMOOTH);
				return Image.getInstance(awtImageX4, null);
			}
		}
		return Image.getInstance(this.dedfaultImage);
	}

	/**
	 * A cell with an image.
	 * 
	 * @param args
	 *          no arguments needed
	 */
	public static void main(String[] args) {
		new BuildBook().build();
	}
}
