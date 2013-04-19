/*
 * Created on Jan 6, 2005
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

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class RollingFileAppender extends org.apache.log4j.RollingFileAppender {

	/**
	 * The default constructor simply calls its
	 * {@link org.apache.log4j.FileAppender} parents constructor.
	 */
	public RollingFileAppender() {
		super();
	}

	/**
	 * Instantiate a RollingFileAppender and open the file designated by
	 * <code>filename</code>. The opened filename will become the ouput
	 * destination for this appender.
	 * <p>
	 * If the <code>append</code> parameter is true, the file will be appended
	 * to. Otherwise, the file desginated by <code>filename</code> will be
	 * truncated before being opened.
	 * 
	 * @param layout
	 *          The layout parameter does not need to be set if the appender
	 *          implementation has its own layout.
	 * @param filename
	 *          The path to the log file.
	 * @param append
	 *          If true will append to fileName. Otherwise will truncate fileName.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public RollingFileAppender(Layout layout, String filename, boolean append)
			throws IOException {
		super(layout, filename, append);
	}

	/**
	 * Instantiate a FileAppender and open the file designated by
	 * <code>filename</code>. The opened filename will become the output
	 * destination for this appender.
	 * <p>
	 * The file will be appended to.
	 * 
	 * @param layout
	 *          The layout parameter does not need to be set if the appender
	 *          implementation has its own layout.
	 * @param filename
	 *          The path to the log file.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public RollingFileAppender(Layout layout, String filename) throws IOException {
		super(layout, filename);
	}

	/**
	 * This is the identifier assigned to this logger.
	 */
	private static String id;

	/**
	 * This method differentiates RollingFileAppender from its super class.
	 * 
	 * @since 0.9.0
	 */
	@Override
	protected void subAppend(LoggingEvent event) {
		if (id == null) {
			id = "" + new Random().nextInt();
			this.qw.write("#Session:" + id + ";" + Calendar.getInstance().getTime());
			this.qw.write(Layout.LINE_SEP);
		}
		this.qw.write(id);
		this.qw.write("#");
		this.qw.write(this.layout.format(event));
		if (layout.ignoresThrowable()) {
			String[] s = event.getThrowableStrRep();
			if (s != null) {
				Thread.dumpStack();
				int len = s.length;
				for (int i = 0; i < len; i++) {
					this.qw.write(id + "#[" + event.getThreadName() + "] "
							+ event.getLevel() + " - \t");
					this.qw.write(s[i]);
					this.qw.write(Layout.LINE_SEP);
				}
			}
		}

		if (this.immediateFlush) {
			this.qw.flush();
		}
		if (fileName != null
				&& ((CountingQuietWriter) qw).getCount() >= maxFileSize) {
			this.rollOver();
		}
	}
}
