package org.powerbot.service.scripts;

import org.powerbot.script.Manifest;
import org.powerbot.script.Script;
import org.powerbot.util.StringUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paris
 */
public final class ScriptDefinition implements Comparable<ScriptDefinition> {
	private final String name, id, description, website;
	private final double version;
	private final String[] authors;

	public String className;
	public byte[] key;
	public String source;
	public int session;
	public boolean local = false;
	private Category category = null;

	public enum Category {
		AGILITY("course"),
		AIO(""),
		COMBAT("fight|kill|duel|soul"),
		CONSTRUCTION("constr"),
		COOKING("cook"),
		CRAFTING("craft|spin|crush|grind|flax|vial"),
		DUNGEONEERING("dung"),
		FARMING("farm"),
		FIREMAKING("fire"),
		FISHING("fish"),
		FLETCHING("fletch"),
		HERBLORE("herb|clean|mix"),
		HUNTER("hunt|catch"),
		MAGIC("mage"),
		MINIGAME("sorc|puzzle|minigame|pest|cape"),
		MINING("mine|ore"),
		MONEY("gp|gold"),
		PRAYER("pray|bones"),
		QUEST("quest"),
		RANGED("range|arrow"),
		RUNECRAFTING("rune|ess"),
		SMITHING("smith|bars"),
		SUMMONING("summ"),
		THIEVING("thief|steal|thiev"),
		WOODCUTTING("wood|chop");

		public final String regex;

		private Category(final String regex) {
			this.regex = regex;
		}
	}

	public ScriptDefinition(final Script script) {
		this(script.getClass().getAnnotation(Manifest.class));
	}

	public ScriptDefinition(final Manifest manifest) {
		name = manifest.name();
		id = null;
		description = manifest.description();
		version = manifest.version();
		authors = manifest.authors();
		website = manifest.website();
	}

	public ScriptDefinition(final String name, final String id, final String description, final double version, final String[] authors, final String website) {
		this.name = name;
		this.id = id;
		this.description = description;
		this.version = version;
		this.authors = authors;
		this.website = website;
	}

	private String getCleanText(String s) {
		return s == null || s.isEmpty() ? "" : StringUtil.stripHtml(s.trim());
	}

	public String getName() {
		return getCleanText(name);
	}

	public String getID() {
		return id == null ? "" : id;
	}

	public String getDescription() {
		return getCleanText(description);
	}

	public double getVersion() {
		return version;
	}

	public String[] getAllAuthors() {
		return authors;
	}

	public String getAuthors() {
		if (authors == null || authors.length == 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		final String[] authors = getAllAuthors();
		for (int i = 0; i < authors.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(StringUtil.stripHtml(authors[i].trim()));
		}
		return sb.toString();
	}

	public String getWebsite() {
		final String url = website;
		return url != null && !url.isEmpty() && (url.startsWith("http://") || url.startsWith("https://")) ? url : null;
	}

	public Category getCategory() {
		if (category != null) {
			return category;
		}

		category = Category.AIO;
		final String sig = (getName() + " " + getDescription()).toLowerCase();
		int x = 0xfff;

		for (final Category c : Category.class.getEnumConstants()) {
			int z = sig.indexOf(c.name().toLowerCase());
			if (z != -1 && z < x) {
				x = z;
				category = c;
			}
			if (c.regex.length() == 0) {
				continue;
			}
			final Matcher m = Pattern.compile(c.regex).matcher(sig);
			if (m.find(0)) {
				z = m.start();
				if (z != -1 && z < x) {
					x = z;
					category = c;
				}
			}
		}

		return category;
	}

	public boolean matches(final String query) {
		final String tag = String.format("%s %s %s", getName(), getDescription(), getAuthors()).toLowerCase();
		return tag.contains(query.toLowerCase());
	}

	@Override
	public String toString() {
		return getName().toLowerCase();
	}

	public static ScriptDefinition fromMap(final Map<String, String> data) {
		final String name = data.containsKey("name") ? data.get("name") : null;
		final String id = data.containsKey("id") ? data.get("id") : null;
		final String description = data.containsKey("description") ? data.get("description") : null;
		final String website = data.containsKey("website") ? data.get("website") : null;
		final String[] authors = data.containsKey("authors") ? data.get("authors").split(",") : new String[]{};
		double version = 1d;

		if (data.containsKey("version")) {
			try {
				version = Double.parseDouble(data.get("version"));
			} catch (final NumberFormatException ignored) {
			}
		}

		return name == null || name.isEmpty() ? null : new ScriptDefinition(name, id, description, version, authors, website);
	}

	@Override
	public int compareTo(final ScriptDefinition o) {
		final String a = getID(), b = o.getID();
		return a == null || b == null ? 0 : a.compareTo(b);
	}
}
