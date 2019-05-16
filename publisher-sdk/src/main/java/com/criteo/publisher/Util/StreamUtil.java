package com.criteo.publisher.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class StreamUtil {

    private StreamUtil() {

    }

    public static String readStream(InputStream in) throws IOException {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return response.toString();
    }


}
