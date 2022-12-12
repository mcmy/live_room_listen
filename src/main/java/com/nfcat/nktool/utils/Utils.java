package com.nfcat.nktool.utils;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Slf4j
public class Utils {
    public static void sleep(int mil) {
        try {
            TimeUnit.MILLISECONDS.sleep(mil);
        } catch (InterruptedException e) {
            log.error("sleep error.", e);
        }
    }

    public static byte @Nullable [] uncompress(@NotNull ByteString bytes1) {
        final byte[] bytes = bytes1.toByteArray();
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[4096];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            log.error("gzip uncompress error.", e);
        }

        return out.toByteArray();
    }
}
