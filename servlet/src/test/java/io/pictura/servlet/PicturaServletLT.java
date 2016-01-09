/**
 * Copyright 2015 Steffen Kremp
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.pictura.servlet;

import static io.pictura.servlet.PicturaServletIT.doGetResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Simple performance/load test.
 * 
 * @author Steffen Kremp
 */
public class PicturaServletLT extends PicturaServletIT {

    private static final String[] TEST_URL_SET = new String[]{
        "/F=${format}/lenna.jpg",
        "/F=${format}/lenna.png",
        "/F=${format}/lenna.gif",
        "/F=${format}/lenna.psd",
        
        "/F=${format}/S=W100/lenna.jpg",
        "/F=${format}/S=W150/lenna.jpg",
        "/F=${format}/S=W200/lenna.jpg",
        "/F=${format}/S=W250/lenna.jpg",
        "/F=${format}/S=W300/lenna.jpg",
        "/F=${format}/S=W350/lenna.jpg",
        
        "/F=${format}/S=W100,DPR1.2/lenna.jpg",
        "/F=${format}/S=W200,DPR1.2/lenna.jpg",
        "/F=${format}/S=W300,DPR1.2/lenna.jpg",
        
        "/F=${format}/S=W100,H150/lenna.jpg",
        
        "/F=${format}/E=G/lenna.jpg",
        "/F=${format}/E=GL/lenna.jpg",
        "/F=${format}/E=SP/lenna.jpg",
        "/F=${format}/E=SS/lenna.jpg",
        "/F=${format}/E=I/lenna.jpg",
        "/F=${format}/E=PX/lenna.jpg",
        "/F=${format}/E=T/lenna.jpg",
        "/F=${format}/E=A/lenna.jpg",
        "/F=${format}/E=B/lenna.jpg",
        "/F=${format}/E=D/lenna.jpg",
        "/F=${format}/E=N/lenna.jpg", 
        "/F=${format}/E=S/lenna.jpg",
        "/F=${format}/E=AC/lenna.jpg",
        "/F=${format}/E=AS/lenna.jpg",
        "/F=${format}/E=AL/lenna.jpg",
        "/F=${format}/E=AE/lenna.jpg",
        
        "/F=${format}/O=75/lenna.jpg",
        "/F=${format}/O=65/lenna.jpg",
        "/F=${format}/O=55/lenna.jpg",
        
        "/F=${format}/R=L/lenna.jpg",
        "/F=${format}/R=R/lenna.jpg",
        "/F=${format}/R=RL/lenna.jpg",
        "/F=${format}/R=H/lenna.jpg",
        "/F=${format}/R=V/lenna.jpg",
        
        "/F=${format}/C=X5,Y5/lenna.jpg",
        "/F=${format}/C=X5,Y5,W100,H200/lenna.jpg",
        "/F=${format}/C=T10,B5/lenna.jpg",
        "/F=${format}/C=T10,B5,L10,R15/lenna.jpg",
        "/F=${format}/C=AR16x9/lenna.jpg",
        "/F=${format}/C=SQ/lenna.jpg",
        
        "/F=${format}/T=5/lenna-trim.jpg",
        
        "/F=${format}/O=70/S=W250/C=AR16x9/E=A,AE,GL/T=5/lenna-trim.jpg"
    };
    
    // format; expected content type; max avg. response time in millis (first byte)
    private static final String[] TEST_RULES = new String[]{
        "JPG;image/jpeg;150", 
        "JP2;image/jp2;150", 
        "WEBP;image/webp;250", 
        "PNG;image/png;150", 
        "GIF;image/gif;100",
        "BMP;image/bmp;100",
        "WBMP;image/vnd.wap.wbmp;100",
        "PNM;image/x-portable-anymap;100",
        "PCX;image/pcx;100",
        "TIFF;image/tiff;100"
    };
            
    @Test
    public void test() {        
        File responseDir = new File(props.getProperty("lt.response.dir"));
        if (!responseDir.exists()) {
            assertTrue(responseDir.mkdirs());
        }        
        
        ArrayList<Metrics> results = new ArrayList<>();
        long overallAvg = 0L;
        try {
            for (String format : TEST_RULES) {                            
                final String formatName = format.split(";")[0];
                final String contentType = format.split(";")[1];                
                final int maxDelta0Time = Integer.parseInt(format.split(";")[2]);
                
                System.out.println("");
                
                Set<URL> urlSet = new HashSet<>();
                for (String url : TEST_URL_SET) {
                    urlSet.add(new URL("http://" + getHost() + url.replace("${format}", formatName)));                    
                }
                
                ArrayList<Request> requests = new ArrayList<>();
                for (URL url : urlSet) {
                    String filename = responseDir.getAbsolutePath() 
                            + File.separator + url.getPath()
                            .replaceFirst("/", "")
                            .replace("/", "-")
                            .replace(".", "-")
                            .replace(",", "-")
                            .replace("=", "-")
                            ;
                    filename += "." + formatName.toLowerCase(Locale.ENGLISH);                    
                    requests.add(new Request(url, contentType, filename));
                }
                
                long sumDelta = 0L;
                for (int i=0; i<requests.size(); i++) {
                    int t0 = i, t1 = t0 + 1;
                    requests.get(t0).start();
                    if (t1 < requests.size()) {
                        requests.get(t1).start();
                        i++;                        
                    }
                    requests.get(t0).join();                    
                    if (t1 < requests.size()) {
                        requests.get(t1).join();
                        results.add(requests.get(t1).result);
                        sumDelta += requests.get(t1).result.startBytesSendMillis - requests.get(t1).result.startTimeMillis;
                        System.out.println(requests.get(t1).result.toString());
                    }
                    results.add(requests.get(t0).result);
                    sumDelta += requests.get(t0).result.startBytesSendMillis - requests.get(t0).result.startTimeMillis;
                    System.out.println(requests.get(t0).result.toString());
                }
                
                long delta0Time = (sumDelta / requests.size());
                overallAvg += delta0Time;
                System.out.println("Avg.: " + delta0Time + "ms");
                
                
                assertTrue("Bad average process time for content type " + contentType, delta0Time < maxDelta0Time);
            }
        } catch (NumberFormatException | MalformedURLException | InterruptedException e) {
            System.err.println(e);
        }
        
        System.out.println("");
        System.out.println("Overall Agv.: " + (overallAvg / TEST_RULES.length) + "ms");
        System.out.println("");
        
        // Sort by response time (first byte)
        Collections.sort(results);
        StringBuilder csv = new StringBuilder("\"Start Time\"; \"Start Time Bytes Send\"; \"End Time\"; \"Process Time\"; \"Content Length\"; URL");
        for (Metrics m : results) {
            csv.append(m).append("\n");
        }
        
        try (FileOutputStream fos = new FileOutputStream(new File(props.getProperty("lt.report")))) {
            fos.write(csv.toString().getBytes());
        } catch (IOException e) {
            System.err.println(e);
        }
    }    

    private static final class Metrics implements Comparable<Metrics> {

        private String url;
        
        private long startTimeMillis = -1L;
        private long startBytesSendMillis = -1L;
        private long endTimeMillis = -1L;
        private long contentLength = -1L;

        @Override
        public String toString() {
            return startTimeMillis + "; " + startBytesSendMillis + "; "
                    + endTimeMillis + "; " + (startBytesSendMillis - startTimeMillis)
                    + "; " + contentLength + "; \"" + url + "\"";
        }

        @Override
        public int compareTo(Metrics o) {
            if (o == null) {
                return 1;
            }
            if ((startBytesSendMillis - startTimeMillis) == (o.startBytesSendMillis - o.startTimeMillis)) {
                return 0;
            } else if ((startBytesSendMillis - startTimeMillis) > (o.startBytesSendMillis - o.startTimeMillis)) {
                return 1;
            }
            return -1;
        }
    }

    private static final class Request extends Thread {

        private final Metrics result;
        
        private final URL url;
        private final String expectedContentType;
        
        private final String filename;

        private Request(URL url, String expectedContentType, String filename) {
            this.url = url;
            this.expectedContentType = expectedContentType;
            this.filename = filename;
            this.result = new Metrics();
            this.result.url = url.toExternalForm();
        }

        @Override
        public void run() {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            result.startTimeMillis = System.currentTimeMillis();
            long size = 0;
            try {                
                HttpURLConnection con = doGetResponse(url, HttpURLConnection.HTTP_OK, expectedContentType);                
                InputStream is = con.getInputStream();

                int r0 = is.read();
                result.startBytesSendMillis = System.currentTimeMillis();
                
                if (r0 > -1) {
                    size++;
                    bos.write(r0);
                }
                
                int len;
                byte[] buf = new byte[10 * 1024];
                while ((len = is.read(buf)) > -1) {
                    bos.write(buf, 0, len);
                    size += len;
                }
                
                if (filename != null) {
                    try (FileOutputStream fos = new FileOutputStream(filename)) {
                        fos.write(bos.toByteArray());
                    }
                }
            } catch (Exception e) {
                System.err.println(e);
            } finally {
                result.endTimeMillis = System.currentTimeMillis();
                result.contentLength = size;
            }
        }

    }    

}
