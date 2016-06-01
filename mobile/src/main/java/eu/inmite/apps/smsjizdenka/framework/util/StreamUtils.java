/*
 * Copyright 2015 AVAST Software s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.inmite.apps.smsjizdenka.framework.util;

import java.io.*;

import eu.inmite.apps.smsjizdenka.framework.DebugLog;


/**
 * Stream utils class.
 * <p/>
 * This class is support of my ATomSoftCommonUtils.
 * Metrics: http://t.atomsoft.cz/sonar/project/index/cz.atomsoft:atomsoft-common-utils
 * Download: http://maven.atomsoft.cz/content/repositories/public-snapshots/cz/atomsoft/atomsoft-common-utils/
 *
 * @author Tomáš Procházka &lt;<a href="mailto:tomas.prochazka@inmite.eu">tomas.prochazka@inmite.eu</a>&gt;
 * @version $Revision: 0$ ($Date: 22.10.2010 14:52:36$)
 */
public class StreamUtils {

    private StreamUtils() {}

    public static String streamToString(InputStream is) throws IOException {
        try {
            return streamToString(new InputStreamReader(is, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            DebugLog.e("StreamUtils.streamToString() failed", e);
            return "";
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                DebugLog.e("StreamUtils.streamToString() failed", e);
            }
        }
    }

    public static String streamToString(InputStreamReader isr) throws IOException {
        if (isr != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[128];
            try {
                Reader reader = new BufferedReader(isr);
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } catch (UnsupportedEncodingException e) {
                DebugLog.e("StreamUtils.streamToString() failed", e);
            } finally {
                try {
                    isr.close();
                } catch (IOException e) {
                    DebugLog.e("StreamUtils.streamToString() failed", e);
                }
            }
            return writer.toString();
        } else {
            return "";
        }
    }

}
