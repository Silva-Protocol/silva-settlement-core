package org.silva.settlement.core.chain.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildInfo {

    private static final Logger logger = LoggerFactory.getLogger("general");

    public static String buildHash;
    public static String buildTime;
    public static String buildBranch;

    static {
        try {
            Properties props = new Properties();
            InputStream is = BuildInfo.class.getResourceAsStream("/build-info.properties");

            if (is != null) {
                props.load(is);

                buildHash = props.getProperty("build.getHash");
                buildTime = props.getProperty("build.time");
                buildBranch = props.getProperty("build.branch");
            } else {
                logger.warn("File not found `build-info.properties`. Run `gradle build` to generate it");
            }
        } catch (IOException e) {
            logger.error("Error reading /build-info.properties", e);
        }
    }

    public static void printInfo(){
        logger.info("git.getHash: [{}]", buildHash);
        logger.info("build.time: {}", buildTime);
    }
}
