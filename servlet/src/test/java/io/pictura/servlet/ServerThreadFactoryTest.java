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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Steffen Kremp
 */
public class ServerThreadFactoryTest {

    @Test
    public void testNewThread() {
	System.out.println("newThread");

	Runnable r = mock(Runnable.class);
	ServerThreadFactory instance = new ServerThreadFactory();

	Thread result = instance.newThread(r);

	assertNotNull(result);
	assertNotNull(result.getThreadGroup());
	assertNull(result.getContextClassLoader());
	assertTrue(result.isDaemon());
	assertTrue(result.getName().startsWith("pictura-"));
	assertEquals(result.getPriority(), Thread.MIN_PRIORITY);
    }

}
