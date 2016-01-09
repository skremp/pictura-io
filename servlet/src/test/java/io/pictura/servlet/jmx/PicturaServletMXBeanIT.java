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
package io.pictura.servlet.jmx;

import io.pictura.servlet.PicturaServletIT;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class PicturaServletMXBeanIT extends PicturaServletIT {

    @Test
    public void testMBeanRegistered() throws Exception {
	System.out.println("mbean_registered");

	MBeanServer jmx = ManagementFactory.getPlatformMBeanServer();
	assertTrue(jmx.isRegistered(new ObjectName("io.pictura.servlet.servlet:type=PicturaServlet,name=pictura-test")));
    }

}
