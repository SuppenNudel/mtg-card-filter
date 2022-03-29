package com.rohm.mtg.utils.cardmanager.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.rohm.mtg.utils.cardmanager.config.UserConfig;
import com.rohm.mtg.utils.cardmanager.config.UserConfigKey;

import de.rohmio.mtg.mtgtop8.api.model.CompLevel;
import de.rohmio.mtg.mtgtop8.api.model.MtgTop8Format;

public class ConfigTest {

	private static UserConfig config;
	private static File file = new File("test-config.properties");

	@BeforeAll
	public static void initConfig() {
		config = UserConfig.getConfig(file);
	}

	@AfterAll
	public static void removeConfigFile() {
		file.delete();
	}

	private void printFile() {
		System.out.println("----------");
		try {
			String readFileToString = FileUtils.readFileToString(file, "UTF-8");
			System.out.println(readFileToString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@ParameterizedTest
	@MethodSource("provideListUserConfigKey")
	public <T> void listTest(UserConfigKey<T[]> key, Class<T> clazz, List<T> temp) {
		List<T> currentValue = config.getList(key, clazz);

		config.setList(key, temp);
		assertEquals(temp, config.getList(key, clazz));
		printFile();

		config.setList(key, currentValue);
		assertEquals(currentValue, config.getList(key, clazz));
		printFile();
	}

	@ParameterizedTest
	@MethodSource("provideSignleUserConfigKey")
	public <T> void singleTest(UserConfigKey<T> key, T temp) {
		T currentValue = config.get(key);

		config.set(key, temp);
		assertEquals(temp, config.get(key));
		printFile();

		config.set(key, currentValue);
		assertEquals(currentValue, config.get(key));
		printFile();
	}

	private static Stream<Arguments> provideListUserConfigKey() {
	    return Stream.of(
	      Arguments.of(UserConfigKey.FORMATS, MtgTop8Format.class, Arrays.asList(MtgTop8Format.values())),
	      Arguments.of(UserConfigKey.COMP_LEVELS, CompLevel.class, Arrays.asList(CompLevel.values()))
	    );
	}

	private static Stream<Arguments> provideSignleUserConfigKey() {
	    return Stream.of(
	  	      Arguments.of(UserConfigKey.TIME_PERIOD, 5),
		      Arguments.of(UserConfigKey.TIME_UNIT, ChronoUnit.CENTURIES)
	    );
	}

}
