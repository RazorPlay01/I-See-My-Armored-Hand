package com.github.razorplay01.ismah;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ISMAH {
    public static final String MOD_ID = "ismah";
    public static final String MOD_NAME = "I See My Armored Hand";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private ISMAH() {
        // []
    }

    public static void init() {
        ISMAH.LOGGER.info("I See My Armored Hand initialized.");
    }
}