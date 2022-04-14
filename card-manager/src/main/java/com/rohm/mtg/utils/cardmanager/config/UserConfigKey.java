package com.rohm.mtg.utils.cardmanager.config;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import de.rohmio.mtg.mtgtop8.api.model.CompLevel;
import de.rohmio.mtg.mtgtop8.api.model.MtgTop8Format;

public class UserConfigKey<T> {

	public static final UserConfigKey<Integer> TIME_PERIOD = new UserConfigKey<>("time-period", Integer.class, 6);
	public static final UserConfigKey<ChronoUnit> TIME_UNIT = new UserConfigKey<>("time-unit", ChronoUnit.class, ChronoUnit.MONTHS, ChronoUnit::name);

	public static final UserConfigKey<CompLevel[]> COMP_LEVELS = new UserConfigKey<>("complevels", CompLevel[].class, CompLevel.values());

	public static final UserConfigKey<MtgTop8Format[]> FORMATS = new UserConfigKey<>("formats", MtgTop8Format[].class, new MtgTop8Format[] {
			MtgTop8Format.PIONEER,
			MtgTop8Format.MODERN,
			MtgTop8Format.LEGACY,
			MtgTop8Format.PAUPER,
	});

	public static final UserConfigKey<File> INIT_DIR = new UserConfigKey<>("init-dir", File.class, new File("."));

	private String key;
	private Class<T> type;
	private T defaultValue;
	private Function<T, String> customConversion;

	private UserConfigKey(String key, Class<T> type, T defaultValue) {
		this.key = key;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	private UserConfigKey(String key, Class<T> type, T defaultValue, Function<T, String> customConversion) {
		this(key, type, defaultValue);
		this.customConversion = customConversion;
	}

	public String convertValue(T value) {
		if(customConversion == null) {
			return value.toString();
		} else {
			return customConversion.apply(value);
		}
	}

	public String getKey() {
		return key;
	}

	public Class<T> getType() {
		return type;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

//	public static UserConfigKey<Boolean> getCompLevel(CompLevel compLevel) {
//		return compLevels.get(compLevel);
//	}

}
