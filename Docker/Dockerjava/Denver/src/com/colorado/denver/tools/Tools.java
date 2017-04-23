package com.colorado.denver.tools;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Tools {

	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Tools.class);

	public static void printJsonObject(JSONObject jObject) throws JSONException {
		Iterator<?> keys = jObject.keys();
		LOGGER.debug("=======PRINTING JSON========");
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (jObject.get(key) instanceof JSONObject) {
				LOGGER.debug(key);
			}
		}
		LOGGER.debug("=======END OF JSON========");
	}

	public static void printGson(String json) {
		LOGGER.debug("=======PRINTING JSON========");
		System.out.println(json);
		LOGGER.debug("=======END OF JSON========");
	}

	public static JsonObject generateJsonObject(Object o) {
		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(o);
		JsonObject object = element.getAsJsonObject();
		return object;
	}

	public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
	}

	public static String quote(String string) {
		if (string == null || string.length() == 0) {
			return "\"\"";
		}

		char c = 0;
		int i;
		int len = string.length();
		StringBuilder sb = new StringBuilder(len + 4);
		String t;

		sb.append('"');
		for (i = 0; i < len; i += 1) {
			c = string.charAt(i);
			switch (c) {
			case '\\':
			case '"':
				sb.append('\\');
				sb.append(c);
				break;
			case '/':
				// if (b == '<') {
				sb.append('\\');
				// }
				sb.append(c);
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\r':
				sb.append("\\r");
				break;
			default:
				if (c < ' ') {
					t = "000" + Integer.toHexString(c);
					sb.append("\\u" + t.substring(t.length() - 4));
				} else {
					sb.append(c);
				}
			}
		}
		sb.append('"');
		return sb.toString();
	}

	public final static String unescape_string(String oldstr) {

		/*
		 * In contrast to fixing Java's broken regex charclasses,
		 * this one need be no bigger, as unescaping shrinks the string
		 * here, where in the other one, it grows it.
		 */

		StringBuffer newstr = new StringBuffer(oldstr.length());

		boolean saw_backslash = false;

		for (int i = 0; i < oldstr.length(); i++) {
			int cp = oldstr.codePointAt(i);
			if (oldstr.codePointAt(i) > Character.MAX_VALUE) {
				i++; /**** WE HATES UTF-16! WE HATES IT FOREVERSES!!! ****/
			}

			if (!saw_backslash) {
				if (cp == '\\') {
					saw_backslash = true;
				} else {
					newstr.append(Character.toChars(cp));
				}
				continue; /* switch */
			}

			if (cp == '\\') {
				saw_backslash = false;
				newstr.append('\\');
				newstr.append('\\');
				continue; /* switch */
			}

			switch (cp) {

			case 'r':
				newstr.append('\r');
				break; /* switch */

			case 'n':
				newstr.append('\n');
				break; /* switch */

			case 'f':
				newstr.append('\f');
				break; /* switch */

			/* PASS a \b THROUGH!! */
			case 'b':
				newstr.append("\\b");
				break; /* switch */

			case 't':
				newstr.append('\t');
				break; /* switch */

			case 'a':
				newstr.append('\007');
				break; /* switch */

			case 'e':
				newstr.append('\033');
				break; /* switch */

			/*
			 * A "control" character is what you get when you xor its
			 * codepoint with '@'==64. This only makes sense for ASCII,
			 * and may not yield a "control" character after all.
			 *
			 * Strange but true: "\c{" is ";", "\c}" is "=", etc.
			 */
			case 'c': {
				if (++i == oldstr.length()) {
					die("trailing \\c");
				}
				cp = oldstr.codePointAt(i);
				/*
				 * don't need to grok surrogates, as next line blows them up
				 */
				if (cp > 0x7f) {
					die("expected ASCII after \\c");
				}
				newstr.append(Character.toChars(cp ^ 64));
				break; /* switch */
			}

			case '8':
			case '9':
				die("illegal octal digit");
				/* NOTREACHED */

				/*
				 * may be 0 to 2 octal digits following this one
				 * so back up one for fallthrough to next case;
				 * unread this digit and fall through to next case.
				 */
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
				--i;
				/* FALLTHROUGH */

				/*
				 * Can have 0, 1, or 2 octal digits following a 0
				 * this permits larger values than octal 377, up to
				 * octal 777.
				 */
			case '0': {
				if (i + 1 == oldstr.length()) {
					/* found \0 at end of string */
					newstr.append(Character.toChars(0));
					break; /* switch */
				}
				i++;
				int digits = 0;
				int j;
				for (j = 0; j <= 2; j++) {
					if (i + j == oldstr.length()) {
						break; /* for */
					}
					/* safe because will unread surrogate */
					int ch = oldstr.charAt(i + j);
					if (ch < '0' || ch > '7') {
						break; /* for */
					}
					digits++;
				}
				if (digits == 0) {
					--i;
					newstr.append('\0');
					break; /* switch */
				}
				int value = 0;
				try {
					value = Integer.parseInt(
							oldstr.substring(i, i + digits), 8);
				} catch (NumberFormatException nfe) {
					die("invalid octal value for \\0 escape");
				}
				newstr.append(Character.toChars(value));
				i += digits - 1;
				break; /* switch */
			} /* end case '0' */

			case 'x': {
				if (i + 2 > oldstr.length()) {
					die("string too short for \\x escape");
				}
				i++;
				boolean saw_brace = false;
				if (oldstr.charAt(i) == '{') {
					/* ^^^^^^ ok to ignore surrogates here */
					i++;
					saw_brace = true;
				}
				int j;
				for (j = 0; j < 8; j++) {

					if (!saw_brace && j == 2) {
						break; /* for */
					}

					/*
					 * ASCII test also catches surrogates
					 */
					int ch = oldstr.charAt(i + j);
					if (ch > 127) {
						die("illegal non-ASCII hex digit in \\x escape");
					}

					if (saw_brace && ch == '}') {
						break;
						/* for */ }

					if (!((ch >= '0' && ch <= '9')
							||
							(ch >= 'a' && ch <= 'f')
							||
							(ch >= 'A' && ch <= 'F'))) {
						die(String.format(
								"illegal hex digit #%d '%c' in \\x", ch, ch));
					}

				}
				if (j == 0) {
					die("empty braces in \\x{} escape");
				}
				int value = 0;
				try {
					value = Integer.parseInt(oldstr.substring(i, i + j), 16);
				} catch (NumberFormatException nfe) {
					die("invalid hex value for \\x escape");
				}
				newstr.append(Character.toChars(value));
				if (saw_brace) {
					j++;
				}
				i += j - 1;
				break; /* switch */
			}

			case 'u': {
				if (i + 4 > oldstr.length()) {
					die("string too short for \\u escape");
				}
				i++;
				int j;
				for (j = 0; j < 4; j++) {
					/* this also handles the surrogate issue */
					if (oldstr.charAt(i + j) > 127) {
						die("illegal non-ASCII hex digit in \\u escape");
					}
				}
				int value = 0;
				try {
					value = Integer.parseInt(oldstr.substring(i, i + j), 16);
				} catch (NumberFormatException nfe) {
					die("invalid hex value for \\u escape");
				}
				newstr.append(Character.toChars(value));
				i += j - 1;
				break; /* switch */
			}

			case 'U': {
				if (i + 8 > oldstr.length()) {
					die("string too short for \\U escape");
				}
				i++;
				int j;
				for (j = 0; j < 8; j++) {
					/* this also handles the surrogate issue */
					if (oldstr.charAt(i + j) > 127) {
						die("illegal non-ASCII hex digit in \\U escape");
					}
				}
				int value = 0;
				try {
					value = Integer.parseInt(oldstr.substring(i, i + j), 16);
				} catch (NumberFormatException nfe) {
					die("invalid hex value for \\U escape");
				}
				newstr.append(Character.toChars(value));
				i += j - 1;
				break; /* switch */
			}

			default:
				newstr.append('\\');
				newstr.append(Character.toChars(cp));
				/*
				 * say(String.format(
				 * "DEFAULT unrecognized escape %c passed through",
				 * cp));
				 */
				break; /* switch */

			}
			saw_backslash = false;
		}

		/* weird to leave one at the end */
		if (saw_backslash) {
			newstr.append('\\');
		}

		return newstr.toString();
	}

	/*
	 * Return a string "U+XX.XXX.XXXX" etc, where each XX set is the
	 * xdigits of the logical Unicode code point. No bloody brain-damaged
	 * UTF-16 surrogate crap, just true logical characters.
	 */
	public final static String uniplus(String s) {
		if (s.length() == 0) {
			return "";
		}
		/* This is just the minimum; sb will grow as needed. */
		StringBuffer sb = new StringBuffer(2 + 3 * s.length());
		sb.append("U+");
		for (int i = 0; i < s.length(); i++) {
			sb.append(String.format("%X", s.codePointAt(i)));
			if (s.codePointAt(i) > Character.MAX_VALUE) {
				i++; /**** WE HATES UTF-16! WE HATES IT FOREVERSES!!! ****/
			}
			if (i + 1 < s.length()) {
				sb.append(".");
			}
		}
		return sb.toString();
	}

	private static final void die(String foa) {
		throw new IllegalArgumentException(foa);
	}

	private static final void say(String what) {
		System.out.println(what);
	}
}