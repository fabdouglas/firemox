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

import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public final class Log {

	private static Logger log = null;

	private Log() {
		super();
	}

	/**
	 * @param message
	 */
	public static void fatal(String message) {
		if (log == null) {
			System.out.println("FATAL : " + message);
		} else {
			log.fatal(message);
		}
		throw new RuntimeException(message);
	}

	/**
	 * @param t
	 */
	public static void fatal(Throwable t) {
		if (log == null) {
			System.out.println(t);
		} else {
			log.fatal(t);
		}
		throw new RuntimeException(t);
	}

	/**
	 * @param stack
	 */
	public static void fatal(StackTraceElement[] stack) {
		if (log == null) {
			System.out.println(stack);
		} else {
			log.fatal(stack);
		}
		throw new RuntimeException("StackTraceElement[] stack");
	}

	/**
	 * @param message
	 * @param t
	 */
	public static void fatal(String message, Throwable t) {
		if (log == null) {
			System.out.println("FATAL : " + message);
		} else {
			log.fatal(message, t);
		}
		throw new RuntimeException(message, t);
	}

	/**
	 * @param message
	 */
	public static void debug(String message) {
		if (log == null) {
			System.out.println(message);
		} else {
			log.debug(message);
		}
	}

	/**
	 * @param object
	 */
	public static void debug(Object object) {
		if (log == null) {
			System.out.println(object);
		} else {
			log.debug(object);
		}
	}

	/**
	 * @param t
	 */
	public static void debug(Throwable t) {
		if (log == null) {
			System.out.println(t);
		} else {
			log.debug(t);
		}
	}

	/**
	 * @param stack
	 */
	public static void debug(StackTraceElement[] stack) {
		if (log == null) {
			System.out.println(stack);
		} else {
			log.debug(stack);
		}
	}

	/**
	 * @param message
	 * @param t
	 */
	public static void debug(String message, Throwable t) {
		if (log == null) {
			System.out.println(message);
		} else {
			log.debug(message, t);
		}
	}

	/**
	 * @param message
	 */
	public static void warn(String message) {
		if (log == null) {
			System.out.println("WARNING : " + message);
		} else {
			log.warn(message);
		}
	}

	/**
	 * @param t
	 */
	public static void warn(Throwable t) {
		if (log == null) {
			System.out.println(t);
		} else {
			log.warn(t);
		}
	}

	/**
	 * @param stack
	 */
	public static void warn(StackTraceElement[] stack) {
		if (log == null) {
			System.out.println("WARNING : " + Arrays.toString(stack));
		} else {
			log.warn(stack);
		}
	}

	/**
	 * @param message
	 * @param t
	 */
	public static void warn(String message, Throwable t) {
		if (log == null) {
			System.out.println("WARNING : " + message);
		} else {
			log.info(message, t);
		}
	}

	/**
	 * @param message
	 */
	public static void error(String message) {
		if (log == null) {
			System.err.println("ERROR : " + message);
		} else {
			log.error(message);
		}
	}

	/**
	 * @param t
	 */
	public static void error(Throwable t) {
		if (log == null) {
			System.out.println(t);
		} else {
			log.error(t);
		}
	}

	/**
	 * @param stack
	 */
	public static void error(StackTraceElement[] stack) {
		if (log == null) {
			System.out.println("ERROR : " + Arrays.toString(stack));
		} else {
			log.error(stack);
		}
	}

	/**
	 * @param message
	 * @param t
	 */
	public static void error(String message, Throwable t) {
		if (log == null) {
			System.out.println("ERROR : " + message);
		} else {
			log.error(message, t);
		}

	}

	/**
	 * @param message
	 */
	public static void info(String message) {
		if (log == null) {
			System.out.println(message);
		} else {
			log.info(message);
		}
	}

	/**
	 * @param t
	 */
	public static void info(Throwable t) {
		if (log == null) {
			System.out.println(t);
		} else {
			log.info(t);
		}
	}

	/**
	 * @param stack
	 */
	public static void info(StackTraceElement[] stack) {
		if (log == null) {
			System.out.println(stack);
		} else {
			log.info(stack);
		}
	}

	/**
	 * @param message
	 * @param t
	 */
	public static void info(String message, Throwable t) {
		if (log == null) {
			System.out.println(message);
		} else {
			log.info(message, t);
		}
	}

	/**
	 * 
	 */
	public static void init() {
		log = Logger.getRootLogger();
		log.setLevel(Level.DEBUG);
		log.setLevel(Level.toLevel(Configuration.getString("logLevel", "DEBUG")));
	}
}
