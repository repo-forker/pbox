package me.pbox.http;

import me.pbox.env.Environment;
import me.pbox.invoke.InvokeException;
import me.pbox.invoke.InvokeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * @author Mike Mirzayanov (mirzayanovmr@gmail.com)
 */
public class HttpUtil {
    private static final Logger logger = Logger.getLogger(HttpUtil.class);

    public static boolean get(String url, File targetFile, boolean inheritOutput, boolean inheritError) {
        try {
            logger.info("Downloading '" + url + "'...");

            long beforeTimeMillis = System.currentTimeMillis();

            boolean result = InvokeUtil.run(inheritOutput, inheritError, Environment.getBin("wget"), "--no-check-certificate", "--tries=3", "--retry-connrefused", "--output-document=" + targetFile, url) == 0;
            long durationTimeMillis = System.currentTimeMillis() - beforeTimeMillis;

            if (result) {
                long size = targetFile.length();
                logger.info("Successfully downloaded '" + url + "' to the '" + targetFile + "' [" + size + " bytes in " + durationTimeMillis + " ms].");
            } else {
                logger.warn("Failed to download '" + url + "' to the '" + targetFile + "' [in " + durationTimeMillis + " ms].");
            }
            return result;
        } catch (InvokeException e) {
            logger.warn("Can't download '" + url + "'.", e);
            return false;
        }
    }

    public static File getTemporaryFile(String url, boolean inheritOutput, boolean inheritError) {
        int lastQuestionMarkIndex = url.lastIndexOf('?');

        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex < 0) {
            throw new IllegalArgumentException("Expected slash in URL '" + url + "'.");
        }

        String output = "";
        if (lastQuestionMarkIndex >= 0) {
            output = url.substring(lastSlashIndex + 1, lastQuestionMarkIndex);
        } else {
            output = url.substring(lastSlashIndex + 1);
        }

        output = output.trim();
        if (StringUtils.isBlank(output)) {
            throw new IllegalArgumentException("Can't construct output document filename from URL '" + url + "'.");
        }

        File targetFile = new File(Environment.getPboxTempAsFile(), output);
        if (!get(url, targetFile, inheritOutput, inheritError)) {
            return null;
        } else {
            return targetFile;
        }
    }
}
