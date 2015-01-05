package com.buddyware.treefrog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.concurrent.Task;

public class ThreadPool {

	private final static ExecutorService mCACHED_EXECUTOR = Executors
			.newCachedThreadPool();

	private static ThreadPool mInstance = new ThreadPool();

	private ThreadPool() {
	}

	public static ThreadPool getInstance() {
		return mInstance;
	}

	public static void shutdown() {
		mCACHED_EXECUTOR.shutdownNow();
	}

	public <S> Future<S> executeCachedTask(Task<S> task) {
		return (Future<S>) mCACHED_EXECUTOR.submit(task);
	}

	public <S> Future<S> executeCachedTask(Runnable runnable) {
		return (Future<S>) mCACHED_EXECUTOR.submit(runnable);
	}

}