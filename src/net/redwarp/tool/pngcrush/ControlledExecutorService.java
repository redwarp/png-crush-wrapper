/*
 * Copryright (C) 2012 Redwarp
 * 
 * This file is part of PNGCrush Wrapper.
 * PNGCrush Wrapper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * PNGCrush Wrapper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with PNGCrush Wrapper.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.redwarp.tool.pngcrush;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ControlledExecutorService implements ExecutorService, Runnable {
	private Queue<Future<?>> futures;
	private ExecutorService service;
	private volatile boolean started = false;
	private Listener listener = null;

	public ControlledExecutorService() {
		service = Executors.newFixedThreadPool(Runtime.getRuntime()
				.availableProcessors());
		futures = new LinkedList<Future<?>>();
		new Thread(this).start();
	}

	@Override
	public void execute(Runnable command) {
		service.execute(command);
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return service.awaitTermination(timeout, unit);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> List<Future<T>> invokeAll(
			Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isShutdown() {
		return service.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return service.isTerminated();
	}

	@Override
	public void shutdown() {
		service.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return service.shutdownNow();
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		Future<T> future = service.submit(task);
		boolean isStarted;
		synchronized (futures) {
			futures.add(future);
			isStarted = started;
		}
		if(!isStarted){
			started = true;
			if(listener != null){
				listener.onTasksStart();
			}
		}
		synchronized (this) {
			notifyAll();
		}
		return future;
	}

	@Override
	public Future<?> submit(Runnable task) {
		Future<?> future = service.submit(task);
		boolean isStarted;
		synchronized (futures) {
			futures.add(future);
			isStarted = started;
		}
		if(!isStarted){
			started = true;
			if(listener != null){
				listener.onTasksStart();
			}
		}
		synchronized (this) {
			notifyAll();
		}
		return future;
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		Future<T> future = service.submit(task, result);
		boolean isStarted;
		synchronized (futures) {
			futures.add(future);
			isStarted = started;
		}
		if(!isStarted){
			started = true;
			if(listener != null){
				listener.onTasksStart();
			}
		}
		synchronized (this) {
			notifyAll();
		}
		return future;
	}

	@Override
	public void run() {
		while (true) {
			Future<?> future;
			boolean isStarted;
			synchronized (futures) {
				future = futures.poll();
				isStarted = started;
			}
			if (future == null) {
				if (isStarted) {
					started = false;
					if(listener != null){
						listener.onTasksFinish();
					}
				}
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					return;
				}
			} else {
				try {
					future.get();
				} catch (InterruptedException e) {
					return;
				} catch (ExecutionException e) {
					return;
				}
			}

		}
	}
	
	public void setTasksListener(Listener listener){
		this.listener = listener;
	}

	public static interface Listener {
		public void onTasksStart();

		public void onTasksFinish();
	}
}
