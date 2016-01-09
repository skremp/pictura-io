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

import io.pictura.servlet.PicturaServlet;
import javax.management.MXBean;

/**
 * JMX descriptor for the {@link PicturaServlet} class.
 *
 * @see PicturaServlet
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
@MXBean
public interface PicturaServletMXBean {

    /**
     * @return The implementation version string of the {@link PicturaServlet}.
     */
    public String getVersion();

    /**
     * @return The name of the {@link PicturaServlet} instance.
     */
    public String getServletName();

    /**
     * @return Information about the servlet.
     */
    public String getServletInfo();

    /**
     * @return <code>true</code> if the global debug option is enabled;
     * otherwise <code>false</code>.
     */
    public boolean isDebugEnabled();

    /**
     * @return The complete servlet uptime (include non alive times) since
     * servlet start in millis.
     */
    public long getUptime();

    /**
     * @return <code>true</code> if the {@link PicturaServlet} instance is alive
     * and ready to process requests; otherwise <code>false</code>.
     */
    public boolean isAlive();

    /**
     * Sets the current servlet alive status to the specified value.
     *
     * @param alive The new servlet alive state.
     */
    public void setAlive(boolean alive);

    /**
     * @return The approximate number of threads that are actively executing
     * tasks.
     */
    public int getActiveCount();

    /**
     * @return The current number of threads in the core image processor pool.
     */
    public int getPoolSize();

    /**
     * @return The approximate total number of image request tasks that have
     * ever been scheduled for execution.
     */
    public long getTaskCount();

    /**
     * @return The approximate total number of image request tasks that have
     * completed execution by the servlet instance.
     */
    public long getCompletedTaskCount();

    /**
     * @return The total number of rejected image request tasks from the servlet
     * instance.
     */
    public long getRejecedTaskCount();

    /**
     * @return The approximate process time in hours of all request tasks that
     * have completed execution by the servlet instance.
     */
    public float getInstanceHours();

    /**
     * @return The approximate average response time in seconds of all request
     * tasks that have completed execution by the servlet instance.
     */
    public float getAverageResponseTime();

    /**
     * @return The approximate average response size in bytes of all request
     * tasks that have completed execution by the servlet instance.
     */
    public float getAverageResponseSize();

    /**
     * @return The approximate outgoing bandwidth in bytes of all request tasks
     * that have completed execution by the servlet instance.
     */
    public long getOutboundTraffic();

    /**
     * @return The approximate incoming bandwidth in bytes of all request tasks
     * that have completed execution by the servlet instance.
     */
    public long getInboundTraffic();

    /**
     * @return The approximate error rate over all request tasks that have
     * completed execution by the servlet instance.
     */
    public float getErrorRate();

}
