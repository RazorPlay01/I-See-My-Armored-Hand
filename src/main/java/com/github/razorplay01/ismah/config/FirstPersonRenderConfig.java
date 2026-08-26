package com.github.razorplay01.ismah.config;

import com.github.razorplay01.ismah.ModTemplate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class FirstPersonRenderConfig {
	private static final String KEY_ARMOR = "render_first_person_armor";
	private static final String KEY_ARROWS = "render_first_person_arrows";
	private static final String KEY_LEASH = "render_first_person_leash";

	private static boolean loaded;
	private static boolean renderArmor = true;
	private static boolean renderArrows = true;
	private static boolean renderLeash = true;

	private FirstPersonRenderConfig() {
	}

	public static synchronized boolean isArmorEnabled() {
		loadIfNeeded();
		return renderArmor;
	}

	public static synchronized boolean isArrowsEnabled() {
		loadIfNeeded();
		return renderArrows;
	}

	public static synchronized boolean isLeashEnabled() {
		loadIfNeeded();
		return renderLeash;
	}

	public static synchronized void setArmorEnabled(boolean enabled) {
		loadIfNeeded();
		renderArmor = enabled;
		save();
	}

	public static synchronized void setArrowsEnabled(boolean enabled) {
		loadIfNeeded();
		renderArrows = enabled;
		save();
	}

	public static synchronized void setLeashEnabled(boolean enabled) {
		loadIfNeeded();
		renderLeash = enabled;
		save();
	}

	public static synchronized OptionInstance<Boolean> createArmorOption() {
		loadIfNeeded();
		return OptionInstance.createBoolean("options.ismah.show_first_person_armor", renderArmor, FirstPersonRenderConfig::setArmorEnabled);
	}

	public static synchronized OptionInstance<Boolean> createArrowsOption() {
		loadIfNeeded();
		return OptionInstance.createBoolean("options.ismah.show_first_person_arrows", renderArrows, FirstPersonRenderConfig::setArrowsEnabled);
	}

	public static synchronized OptionInstance<Boolean> createLeashOption() {
		loadIfNeeded();
		return OptionInstance.createBoolean("options.ismah.show_first_person_leash", renderLeash, FirstPersonRenderConfig::setLeashEnabled);
	}

	private static void loadIfNeeded() {
		if (loaded) {
			return;
		}

		loaded = true;
		Path configPath = getConfigPath();
		if (!Files.exists(configPath)) {
			return;
		}

		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(configPath)) {
			properties.load(reader);
			renderArmor = Boolean.parseBoolean(properties.getProperty(KEY_ARMOR, Boolean.toString(renderArmor)));
			renderArrows = Boolean.parseBoolean(properties.getProperty(KEY_ARROWS, Boolean.toString(renderArrows)));
			renderLeash = Boolean.parseBoolean(properties.getProperty(KEY_LEASH, Boolean.toString(renderLeash)));
		} catch (IOException exception) {
			ModTemplate.LOGGER.warn("Failed to load {} config from {}", ModTemplate.MOD_ID, configPath, exception);
		}
	}

	private static void save() {
		Path configPath = getConfigPath();
		try {
			Files.createDirectories(configPath.getParent());
		} catch (IOException exception) {
			ModTemplate.LOGGER.warn("Failed to create config directory for {}", ModTemplate.MOD_ID, exception);
			return;
		}

		Properties properties = new Properties();
		properties.setProperty(KEY_ARMOR, Boolean.toString(renderArmor));
		properties.setProperty(KEY_ARROWS, Boolean.toString(renderArrows));
		properties.setProperty(KEY_LEASH, Boolean.toString(renderLeash));

		try (Writer writer = Files.newBufferedWriter(configPath)) {
			properties.store(writer, "I See My Armored Hand client options");
		} catch (IOException exception) {
			ModTemplate.LOGGER.warn("Failed to save {} config to {}", ModTemplate.MOD_ID, configPath, exception);
		}
	}

	private static Path getConfigPath() {
		return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(ModTemplate.MOD_ID + "-client.properties");
	}
}
