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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default {@link ThreadFactory} used by the internal {@link ExecutorService} to
 * creates execution {@link Thread}s for image processing.
 *
 * @see ThreadFactory
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
final class ServerThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final ThreadGroup group;
    private final String namePrefix;

    private final int priority;

    /**
     * Creates a new server thread factory instance.
     */
    ServerThreadFactory() {
	this(Thread.MIN_PRIORITY);
    }

    /**
     * Creates a new server thread factory instance.
     *
     * @param priority Thread priorities.
     */
    ServerThreadFactory(int priority) {
	SecurityManager manager = System.getSecurityManager();

	// Determine the group that threads created by this factory will be in
	group = (manager == null ? Thread.currentThread().getThreadGroup()
		: manager.getThreadGroup());

	// Define a common name prefix for the threads created by this factory.
	namePrefix = "pictura-" + POOL_NUMBER.getAndIncrement() + "-worker-";

	this.priority = priority;
    }

    /**
     * Used to create a deamon {@link Thread} capable of executing the given
     * {@link Runnable}.
     * <p/>
     * Thread created by this factory are utilized by the parent
     * {@link ExecutorService} when processing queued up image operations.
     */
    @Override
    public Thread newThread(Runnable r) {
	Thread t = new Thread(group, r, namePrefix
		+ threadNumber.getAndIncrement(), 0);

	// Configure thread according to class or subclass
	t.setContextClassLoader(null);
	t.setDaemon(true);
	t.setPriority(priority);

	return t;
    }

}
