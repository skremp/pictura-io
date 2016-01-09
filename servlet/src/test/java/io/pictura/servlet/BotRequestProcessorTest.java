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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.internal.verification.VerificationModeFactory;

/**
 * @author Steffen Kremp
 */
public class BotRequestProcessorTest {

    @Test
    public void testIsCacheable() throws Exception {
        assertFalse(new BotRequestProcessor().isCacheable());
    }
    
    @Test
    public void testIsBotRequest() throws Exception {
	System.out.println("isBotRequest");

	// Bot/Crawlser user agents from http://user-agent-string.info/de/list-of-ua/bots
	BotRequestProcessor rp = new BotRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getHeader("User-Agent")).thenReturn(null);
	assertFalse(rp.isBotRequest(req));
	assertFalse(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("");
	assertFalse(rp.isBotRequest(req));
	assertFalse(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Linux; U; Android 3.0; en-us; Xoom Build/HRI39) AppleWebKit/525.10 (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2");
	assertFalse(rp.isBotRequest(req));
	assertFalse(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (iPhone; CPU iPhone OS 7_0 like Mac OS X; en-us) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53");
	assertFalse(rp.isBotRequest(req));
	assertFalse(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Linux; U; Android 4.0; en-us; LT28at Build/6.1.C.1.111) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
	assertFalse(rp.isBotRequest(req));
	assertFalse(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Linux; U; en-us; KFTHWI Build/JDQ39) AppleWebKit/535.19 (KHTML, like Gecko) Silk/3.13 Safari/535.19 Silk-Accelerated=true");
	assertFalse(rp.isBotRequest(req));
	assertFalse(rp.isPreferred(req));

	//
	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
	assertTrue(rp.isBotRequest(req));
	assertTrue(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)");
	assertTrue(rp.isBotRequest(req));
	assertTrue(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)");
	assertTrue(rp.isBotRequest(req));
	assertTrue(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots)");
	assertTrue(rp.isBotRequest(req));
	assertTrue(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("WorldBrewBot/2.1 (+http://www.marketbrew.com/)");
	assertTrue(rp.isBotRequest(req));

	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (compatible; MJ12bot/v1.4.5; http://www.majestic12.co.uk/bot.php?+)");
	assertTrue(rp.isBotRequest(req));
	assertTrue(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (compatible; spbot/4.3.0; +http://OpenLinkProfiler.org/bot )");
	assertTrue(rp.isBotRequest(req));
	assertTrue(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (compatible; SMTBot/1.0; +http://www.similartech.com/smtbot)");
	assertTrue(rp.isBotRequest(req));
	assertTrue(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("PostPost/1.0 (+http://postpost.com/crawlers)");
	assertTrue(rp.isBotRequest(req));
	assertTrue(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("flatlandbot/baypup (Flatland Industries Web Spider; http://www.flatlandindustries.com/flatlandbot; jason@flatlandindustries.com)");
	assertTrue(rp.isBotRequest(req));
	assertTrue(rp.isPreferred(req));

	when(req.getHeader("User-Agent")).thenReturn("Willow Internet Crawler by Twotrees V2.1");
	assertTrue(rp.isBotRequest(req));
	assertTrue(rp.isPreferred(req));

    }

    @Test
    public void testCreateRequestProcessor() {
	ImageRequestStrategy s = new BotRequestProcessor();
	assertNotNull(s.createRequestProcessor());
	assertTrue(s.createRequestProcessor() instanceof BotRequestProcessor);
    }

    @Test
    public void testGetRedirectURL_1() {
	BotRequestProcessor rp = new BotRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/myContext");
	when(req.getServletPath()).thenReturn("/myServlet");
	when(req.getRequestURI()).thenReturn("/lenna.jpg");

	assertEquals("/myContext/myServlet/lenna.jpg", rp.getRedirectURL(req));
    }

    @Test
    public void testGetRedirectURL_2() {
	BotRequestProcessor rp = new BotRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("/myServlet");
	when(req.getRequestURI()).thenReturn("/lenna.jpg");

	assertEquals("/myServlet/lenna.jpg", rp.getRedirectURL(req));
    }

    @Test
    public void testDoProcess() throws Exception {
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("/myServlet");
	when(req.getRequestURI()).thenReturn("/f=png/lenna.jpg");
	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)");

	HttpServletResponse resp = mock(HttpServletResponse.class);

	when(resp.isCommitted()).thenReturn(Boolean.FALSE);

	BotRequestProcessor rp = new BotRequestProcessor();
	rp.setRequest(req);
	rp.setResponse(resp);

	BotRequestProcessor rpSpy = spy(rp);

	when(rpSpy.getRequest()).thenReturn(req);
	when(rpSpy.getResponse()).thenReturn(resp);

	assertTrue(rpSpy.isPreferred(req));

	rpSpy.doProcess(req, resp);

	verify(rpSpy.getResponse(), VerificationModeFactory.atLeastOnce()).setStatus(301);

	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
	assertFalse(rpSpy.isPreferred(req));

	rpSpy.doProcess(req, resp);
    }

}
