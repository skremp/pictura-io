/**
 * Copyright 2016 Steffen Kremp
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
package io.pictura.builder;

import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * @author Steffen Kremp
 */
public class URLBuilderTest {

    @Test
    public void testSetParameter() throws Exception {
        assertEquals("http://localhost:8084/img/s=h200/e=ae/bg=fcfcfc/c=t10/lenna.png",
                new URLBuilder("http://localhost:8084/img")
                .setImagePath("lenna.png")
                .setImageParameter("s", "w320")
                .setImageParameter("e", "AE")
                .setImageParameter("bg", "fcfcfc")
                .setImageParameter("c", "t10")
                .setImageParameter("s", "h200")
                .toString());
    }

    @Test
    public void testJoinParameter() throws Exception {
        assertEquals("http://localhost:8084/img/s=w320,h200,dpr1.5/e=ae,gl/bg=fcfcfc/c=t10/lenna.png",
                new URLBuilder("http://localhost:8084/img")
                .setImagePath("lenna.png")
                .setImageParameter("s", "w320")
                .setImageParameter("e", "AE")
                .setImageParameter("bg", "fcfcfc")
                .setImageParameter("c", "t10")
                .joinImageParameter("s", "h200")
                .joinImageParameter("s", "dpr1.5")
                .joinImageParameter("e", "gl")
                .toString());
    }

    @Test
    public void testReset() throws Exception {
        URLBuilder builder = new URLBuilder("http://localhost:8084/img")
                .setImagePath("lenna.png")
                .setImageParameter("s", "w320")
                .setImageParameter("e", "AE")
                .setImageParameter("bg", "fcfcfc")
                .setImageParameter("c", "t10")
                .joinImageParameter("s", "h200")
                .joinImageParameter("s", "dpr1.5")
                .joinImageParameter("e", "gl")
                .setQueryParameter("foo", "bar");

        assertEquals("http://localhost:8084/img/s=w320,h200,dpr1.5/e=ae,gl/bg=fcfcfc/c=t10/lenna.png?foo=bar", builder.toString());

        builder.reset();
        assertEquals("http://localhost:8084/img/lenna.png", builder.setImagePath("lenna.png").toString());
    }

    @Test
    public void testToURL() throws Exception {
        URLBuilder builder = new URLBuilder("http://localhost:8084/img")
                .setImagePath("lenna.png")
                .setImageParameter("s", "w320")
                .setImageParameter("e", "AE")
                .setImageParameter("bg", "fcfcfc")
                .setImageParameter("c", "t10")
                .joinImageParameter("s", "h200")
                .joinImageParameter("s", "dpr1.5")
                .joinImageParameter("e", "gl");

        URL url = builder.toURL();

        assertNotNull(url);
        assertEquals("http://localhost:8084/img/s=w320,h200,dpr1.5/e=ae,gl/bg=fcfcfc/c=t10/lenna.png", url.toExternalForm());
    }

    @Test
    public void testImagePathComponents() throws Exception {
        assertEquals("http://localhost:8084/img/s=h200/e=ae/bg=fcfcfc/c=t10/lenna.png",
                new URLBuilder("http://localhost:8084/img")
                .setImagePath("lenna.png?")
                .setImageParameter("s", "w320")
                .setImageParameter("e", "AE")
                .setImageParameter("bg", "fcfcfc")
                .setImageParameter("c", "t10")
                .setImageParameter("s", "h200")
                .toString());

        assertEquals("http://localhost:8084/img/s=h200/e=ae/bg=fcfcfc/c=t10/lenna.png",
                new URLBuilder("http://localhost:8084/img")
                .setImagePath("lenna.png#")
                .setImageParameter("s", "w320")
                .setImageParameter("e", "AE")
                .setImageParameter("bg", "fcfcfc")
                .setImageParameter("c", "t10")
                .setImageParameter("s", "h200")
                .toString());

        assertEquals("http://localhost:8084/img/s=h200/e=ae/bg=fcfcfc/c=t10/http%3A%2F%2Ffoobar.com%2Flenna.png%3Fx%3Dy",
                new URLBuilder("http://localhost:8084/img")
                .setImagePath("http://foobar.com/lenna.png?x=y")
                .setImageParameter("s", "w320")
                .setImageParameter("e", "AE")
                .setImageParameter("bg", "fcfcfc")
                .setImageParameter("c", "t10")
                .setImageParameter("s", "h200")
                .toString());
    }

    @Test
    public void testSetQueryParameter() throws Exception {
        assertEquals("http://localhost:8084/img/s=w200/lenna.png?rnd=1234567890",
                new URLBuilder("http://localhost:8084/img")
                .setImagePath("lenna.png?")
                .setImageParameter("s", "w200")
                .setQueryParameter("rnd", "1234567890")
                .toString());
        
        assertEquals("http://localhost:8084/img/s=w200/lenna.png?dl=lenna",
                new URLBuilder("http://localhost:8084/img")
                .setImagePath("lenna.png?")
                .setImageParameter("s", "w200")
                .setQueryParameter("dl", "lenna")
                .toString());
    }

    @Test
    public void testToURI() throws Exception {
        assertEquals("/img/s=w200/lenna.png",
                new URLBuilder("/img")
                .setImagePath("lenna.png?")
                .setImageParameter("s", "w200")
                .toURI().toString());
    }

    @Test
    public void testImagePathNull() throws Exception {
        assertEquals("/img/s=w200/",
                new URLBuilder("/img")
                .setImageParameter("s", "w200")
                .toURI().toString());
    }

    @Test
    public void testProtocolHostPathConstructor() throws Exception {
        assertEquals("http://localhost/img/s=w200/lenna.png",
                new URLBuilder("http", "localhost", "/img")
                .setImagePath("lenna.png?")
                .setImageParameter("s", "w200")
                .toString());
    }

    @Test
    public void testProtocolHostPortPathConstructor() throws Exception {
        assertEquals("http://localhost/img/s=w200/lenna.png",
                new URLBuilder("http", "localhost", -1, "/img")
                .setImagePath("lenna.png?")
                .setImageParameter("s", "w200")
                .toString());

        assertEquals("http://localhost:8090/img/s=w200/lenna.png",
                new URLBuilder("http", "localhost", 8090, "/img")
                .setImagePath("lenna.png?")
                .setImageParameter("s", "w200")
                .toString());
    }

    @Test
    public void testSetEncoding() throws Exception {
        URLBuilder b = new URLBuilder("/img")
                .setImagePath("lenna.png?")
                .setImageParameter("s", "w200")
                .setQueryParameter("foo", "öäü");

        assertEquals("/img/s=w200/lenna.png?foo=%C3%B6%C3%A4%C3%BC", b.toURI().toString());

        b.setEncoding("ISO-8859-1");
        assertEquals("/img/s=w200/lenna.png?foo=%F6%E4%FC", b.toURI().toString());
    }

    @Test
    public void testClone() throws Exception {
        URLBuilder b1 = new URLBuilder("http://localhost:9090/service")
                .setImagePath("lenna.gif")
                .setImageParameter("q", "m")
                .setImageParameter("s", "w50p")
                .joinImageParameter("s", "dpr1.5")
                .setQueryParameter("t", "123456");

        URLBuilder b2 = b1.clone();

        assertNotSame(b1, b2);
        assertEquals(b1.toString(), b2.toString());

        b1.setImageParameter("c", "50");
        assertNotEquals(b1.toString(), b2.toString());

        b2.reset();
        b2.setImagePath("/foo.png");
        assertNotEquals(b1.toString(), b2.toString());
    }

    @Test
    public void testBuild() throws Exception {
        URLBuilder builder = new URLBuilder("http://localhost:8084/img")
                .setImagePath("lenna.png")
                .setImageParameter("s", "w320")
                .setImageParameter("bg", "fcfcfc")
                .setImageParameter("c", "t10")
                .joinImageParameter("s", "h200")
                .joinImageParameter("s", "dpr1.5")
                .joinImageParameter("e", "gl");

        URL url = builder.build();

        assertNotNull(url);
        assertEquals("http://localhost:8084/img/s=w320,h200,dpr1.5/bg=fcfcfc/c=t10/e=gl/lenna.png", url.toExternalForm());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProtocolHostPathConstructorIllegalArgument() throws Exception {
        new URLBuilder("foobar", "localhost", "/img");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetImagePathEmpty() throws Exception {
        new URLBuilder("http://localhost:8084").setImagePath("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetImageParameterNameNull() throws Exception {
        new URLBuilder("http://localhost:8084").setImageParameter(null, "w200");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetImageParameterNameEmpty() throws Exception {
        new URLBuilder("http://localhost:8084").setImageParameter("", "w200");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutImageParameterValueNull() throws Exception {
        new URLBuilder("http://localhost:8084").putImageParameter("s", null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetImageParameterValueEmpty() throws Exception {
        new URLBuilder("http://localhost:8084").setImageParameter("s", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetIllegalImageParameterName() throws Exception {
        new URLBuilder("http://localhost:8084").setImageParameter("xy/?=Ü", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetQueryParameterNameNull() throws Exception {
        new URLBuilder("http://localhost:8084").setQueryParameter(null, "1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetQueryParameterNameEmpty() throws Exception {
        new URLBuilder("http://localhost:8084").setQueryParameter("", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetIllegalQueryParameterName() throws Exception {
        new URLBuilder("http://localhost:8084").setQueryParameter("xy/?=Ü", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetIllegalEncoding() throws Exception {
        new URLBuilder("http://localhost:8084").setEncoding("foobar+#+");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyEncoding() throws Exception {
        new URLBuilder("http://localhost:8084").setEncoding("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullEndpoint() throws Exception {
        new URLBuilder(new String[]{null});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyEndpoint() throws Exception {
        new URLBuilder("");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetImageParamI() throws Exception {
        new URLBuilder("http://localhost:8084/pictura")
                .setImageParameter("I", "/lenna.jpg")
                .build();
    }

    @Test(expected = RuntimeException.class)
    public void testBuildRuntimeException() throws Exception {
        new URLBuilder("/img")
                .setImagePath("lenna.png")
                .setImageParameter("s", "w320")
                .build();
    }
    
}
