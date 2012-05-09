/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.rosehulman.android.directory.compat;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.content.Loader;
import android.support.v4.util.TimeUtils;
import android.util.Log;

/**
 * Abstract Loader that provides an {@link ModernAsyncTask} to do the work.  See
 * {@link Loader} and {@link android.app.LoaderManager} for more details.
 *
 * <p>Here is an example implementation of an ModernAsyncTaskLoader subclass that
 * loads the currently installed applications from the package manager.  This
 * implementation takes care of retrieving the application labels and sorting
 * its result set from them, monitoring for changes to the installed
 * applications, and rebuilding the list when a change in configuration requires
 * this (such as a locale change).
 *
 * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/LoaderCustom.java
 *      loader}
 *
 * <p>An example implementation of a fragment that uses the above loader to show
 * the currently installed applications in a list is below.
 *
 * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/LoaderCustom.java
 *      fragment}
 *
 * @param <D> the data type to be loaded.
 */
public abstract class AsyncLoader<D> extends Loader<D> {
    static final String TAG = "ModernAsyncTaskLoader";
    public static boolean DEBUG = false;

    final class LoadTask extends ModernAsyncTask<Void, Void, D> implements Runnable {

        D result;
        boolean waiting;

        private CountDownLatch done = new CountDownLatch(1);

        /* Runs on a worker thread */
        @Override
        protected D doInBackground(Void... params) {
            if (DEBUG) Log.v(TAG, this + " >>> doInBackground");
            result = AsyncLoader.this.onLoadInBackground();
            if (DEBUG) Log.v(TAG, this + "  <<< doInBackground");
            return result;
        }

        /* Runs on the UI thread */
        @Override
        protected void onPostExecute(D data) {
            if (DEBUG) Log.v(TAG, this + " onPostExecute");
            try {
                AsyncLoader.this.dispatchOnLoadComplete(this, data);
            } finally {
                done.countDown();
            }
        }

        @Override
        protected void onCancelled() {
            if (DEBUG) Log.v(TAG, this + " onCancelled");
            try {
                AsyncLoader.this.dispatchOnCancelled(this, result);
            } finally {
                done.countDown();
            }
        }

        @Override
        public void run() {
            waiting = false;
            AsyncLoader.this.executePendingTask();
        }
    }

    volatile LoadTask mTask;
    volatile LoadTask mCancellingTask;

    long mUpdateThrottle;
    long mLastLoadCompleteTime = -10000;
    Handler mHandler;

    public AsyncLoader(Context context) {
        super(context);
    }

    /**
     * Set amount to throttle updates by.  This is the minimum time from
     * when the last {@link #onLoadInBackground()} call has completed until
     * a new load is scheduled.
     *
     * @param delayMS Amount of delay, in milliseconds.
     */
    public void setUpdateThrottle(long delayMS) {
        mUpdateThrottle = delayMS;
        if (delayMS != 0) {
            mHandler = new Handler();
        }
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        cancelLoad(true);
        mTask = new LoadTask();
        if (DEBUG) Log.v(TAG, "Preparing load: mTask=" + mTask);
        executePendingTask();
    }

    /**
     * Attempt to cancel the current load task. See {@link ModernAsyncTask#cancel(boolean)}
     * for more info.  Must be called on the main thread of the process.
     *
     * <p>Cancelling is not an immediate operation, since the load is performed
     * in a background thread.  If there is currently a load in progress, this
     * method requests that the load be cancelled, and notes this is the case;
     * once the background thread has completed its work its remaining state
     * will be cleared.  If another load request comes in during this time,
     * it will be held until the cancelled load is complete.
     *
     * @return Returns <tt>false</tt> if the task could not be cancelled,
     *         typically because it has already completed normally, or
     *         because {@link #startLoading()} hasn't been called; returns
     *         <tt>true</tt> otherwise.
     */
    public boolean cancelLoad(boolean interrupt) {
        if (DEBUG) Log.v(TAG, "cancelLoad: mTask=" + mTask);
        if (mTask != null) {
            if (mCancellingTask != null) {
                // There was a pending task already waiting for a previous
                // one being canceled; just drop it.
                if (DEBUG) Log.v(TAG,
                        "cancelLoad: still waiting for cancelled task; dropping next");
                if (mTask.waiting) {
                    mTask.waiting = false;
                    mHandler.removeCallbacks(mTask);
                }
                mTask = null;
                return false;
            } else if (mTask.waiting) {
                // There is a task, but it is waiting for the time it should
                // execute.  We can just toss it.
                if (DEBUG) Log.v(TAG, "cancelLoad: task is waiting, dropping it");
                mTask.waiting = false;
                mHandler.removeCallbacks(mTask);
                mTask = null;
                return false;
            } else {
                boolean cancelled = mTask.cancel(interrupt);
                if (DEBUG) Log.v(TAG, "cancelLoad: cancelled=" + cancelled);
                if (cancelled) {
                    mCancellingTask = mTask;
                }
                mTask = null;
                return cancelled;
            }
        }
        return false;
    }

    /**
     * Called if the task was canceled before it was completed.  Gives the class a chance
     * to properly dispose of the result.
     */
    public void onCanceled(D data) {
    }

    void executePendingTask() {
        if (mCancellingTask == null && mTask != null) {
            if (mTask.waiting) {
                mTask.waiting = false;
                mHandler.removeCallbacks(mTask);
            }
            if (mUpdateThrottle > 0) {
                long now = SystemClock.uptimeMillis();
                if (now < (mLastLoadCompleteTime+mUpdateThrottle)) {
                    // Not yet time to do another load.
                    if (DEBUG) Log.v(TAG, "Waiting until "
                            + (mLastLoadCompleteTime+mUpdateThrottle)
                            + " to execute: " + mTask);
                    mTask.waiting = true;
                    mHandler.postAtTime(mTask, mLastLoadCompleteTime+mUpdateThrottle);
                    return;
                }
            }
            if (DEBUG) Log.v(TAG, "Executing: " + mTask);
            mTask.executeOnExecutor(ModernAsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }
    }

    void dispatchOnCancelled(LoadTask task, D data) {
        onCanceled(data);
        if (mCancellingTask == task) {
            if (DEBUG) Log.v(TAG, "Cancelled task is now canceled!");
            mLastLoadCompleteTime = SystemClock.uptimeMillis();
            mCancellingTask = null;
            executePendingTask();
        }
    }

    void dispatchOnLoadComplete(LoadTask task, D data) {
        if (mTask != task) {
            if (DEBUG) Log.v(TAG, "Load complete of old task, trying to cancel");
            dispatchOnCancelled(task, data);
        } else {
            if (isAbandoned()) {
                // This cursor has been abandoned; just cancel the new data.
                onCanceled(data);
            } else {
                mLastLoadCompleteTime = SystemClock.uptimeMillis();
                mTask = null;
                if (DEBUG) Log.v(TAG, "Delivering result");
                deliverResult(data);
            }
        }
    }

    /**
     */
    public abstract D loadInBackground();

    /**
     * Called on a worker thread to perform the actual load. Implementations should not deliver the
     * result directly, but should return them from this method, which will eventually end up
     * calling {@link #deliverResult} on the UI thread. If implementations need to process
     * the results on the UI thread they may override {@link #deliverResult} and do so
     * there.
     *
     * @return Implementations must return the result of their load operation.
     */
    protected D onLoadInBackground() {
        return loadInBackground();
    }

    /**
     * Locks the current thread until the loader completes the current load
     * operation. Returns immediately if there is no load operation running.
     * Should not be called from the UI thread: calling it from the UI
     * thread would cause a deadlock.
     * <p>
     * Use for testing only.  <b>Never</b> call this from a UI thread.
     *
     * @hide
     */
    public void waitForLoader() {
        LoadTask task = mTask;
        if (task != null) {
            try {
                task.done.await();
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        if (mTask != null) {
            writer.print(prefix); writer.print("mTask="); writer.print(mTask);
                    writer.print(" waiting="); writer.println(mTask.waiting);
        }
        if (mCancellingTask != null) {
            writer.print(prefix); writer.print("mCancellingTask="); writer.print(mCancellingTask);
                    writer.print(" waiting="); writer.println(mCancellingTask.waiting);
        }
        if (mUpdateThrottle != 0) {
            writer.print(prefix); writer.print("mUpdateThrottle=");
                    TimeUtils.formatDuration(mUpdateThrottle, writer);
                    writer.print(" mLastLoadCompleteTime=");
                    TimeUtils.formatDuration(mLastLoadCompleteTime,
                            SystemClock.uptimeMillis(), writer);
                    writer.println();
        }
    }
    
    /**
     * Copy of the required parts of AsyncTask from Android 3.0 that is needed
     * to support AsyncTaskLoader.  We use this rather than the one from the platform
     * because we rely on some subtle behavior of AsyncTask that is not reliable on
     * older platforms.
     */
    private static abstract class ModernAsyncTask<Params, Progress, Result> {
        private static final String LOG_TAG = "AsyncTask";

        private static final int CORE_POOL_SIZE = 5;
        private static final int MAXIMUM_POOL_SIZE = 128;
        private static final int KEEP_ALIVE = 1;

        private static final ThreadFactory sThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "ModernAsyncTask #" + mCount.getAndIncrement());
            }
        };

        private static final BlockingQueue<Runnable> sPoolWorkQueue =
                new LinkedBlockingQueue<Runnable>(10);

        /**
         * An {@link Executor} that can be used to execute tasks in parallel.
         */
        public static final Executor THREAD_POOL_EXECUTOR
                = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                        TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

        private static final int MESSAGE_POST_RESULT = 0x1;
        private static final int MESSAGE_POST_PROGRESS = 0x2;

        private static final InternalHandler sHandler = new InternalHandler();

        private static volatile Executor sDefaultExecutor = THREAD_POOL_EXECUTOR;
        private final WorkerRunnable<Params, Result> mWorker;
        private final FutureTask<Result> mFuture;

        private volatile Status mStatus = Status.PENDING;

        private final AtomicBoolean mTaskInvoked = new AtomicBoolean();

        /**
         * Indicates the current status of the task. Each status will be set only once
         * during the lifetime of a task.
         */
        public enum Status {
            /**
             * Indicates that the task has not been executed yet.
             */
            PENDING,
            /**
             * Indicates that the task is running.
             */
            RUNNING,
            /**
             * Indicates that {@link AsyncTask#onPostExecute} has finished.
             */
            FINISHED,
        }

        /** @hide Used to force static handler to be created. */
        public static void init() {
            sHandler.getLooper();
        }

        /** @hide */
        public static void setDefaultExecutor(Executor exec) {
            sDefaultExecutor = exec;
        }

        /**
         * Creates a new asynchronous task. This constructor must be invoked on the UI thread.
         */
        public ModernAsyncTask() {
            mWorker = new WorkerRunnable<Params, Result>() {
                public Result call() throws Exception {
                    mTaskInvoked.set(true);
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    return postResult(doInBackground(mParams));
                }
            };

            mFuture = new FutureTask<Result>(mWorker) {
                @Override
                protected void done() {
                    try {
                        final Result result = get();

                        postResultIfNotInvoked(result);
                    } catch (InterruptedException e) {
                        android.util.Log.w(LOG_TAG, e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException("An error occured while executing doInBackground()",
                                e.getCause());
                    } catch (CancellationException e) {
                        postResultIfNotInvoked(null);
                    } catch (Throwable t) {
                        throw new RuntimeException("An error occured while executing "
                                + "doInBackground()", t);
                    }
                }
            };
        }

        private void postResultIfNotInvoked(Result result) {
            final boolean wasTaskInvoked = mTaskInvoked.get();
            if (!wasTaskInvoked) {
                postResult(result);
            }
        }

        @SuppressWarnings("unchecked")
		private Result postResult(Result result) {
            Message message = sHandler.obtainMessage(MESSAGE_POST_RESULT,
                    new AsyncTaskResult<Result>(this, result));
            message.sendToTarget();
            return result;
        }

        /**
         * Returns the current status of this task.
         *
         * @return The current status.
         */
        public final Status getStatus() {
            return mStatus;
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         *
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         *
         * @return A result, defined by the subclass of this task.
         *
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        protected abstract Result doInBackground(Params... params);

        /**
         * Runs on the UI thread before {@link #doInBackground}.
         *
         * @see #onPostExecute
         * @see #doInBackground
         */
        protected void onPreExecute() {
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         *
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param result The result of the operation computed by {@link #doInBackground}.
         *
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        protected void onPostExecute(Result result) {
        }

        /**
         * Runs on the UI thread after {@link #publishProgress} is invoked.
         * The specified values are the values passed to {@link #publishProgress}.
         *
         * @param values The values indicating progress.
         *
         * @see #publishProgress
         * @see #doInBackground
         */
        protected void onProgressUpdate(Progress... values) {
        }

        /**
         * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
         * {@link #doInBackground(Object[])} has finished.</p>
         *
         * <p>The default implementation simply invokes {@link #onCancelled()} and
         * ignores the result. If you write your own implementation, do not call
         * <code>super.onCancelled(result)</code>.</p>
         *
         * @param result The result, if any, computed in
         *               {@link #doInBackground(Object[])}, can be null
         *
         * @see #cancel(boolean)
         * @see #isCancelled()
         */
        protected void onCancelled(Result result) {
            onCancelled();
        }

        /**
         * <p>Applications should preferably override {@link #onCancelled(Object)}.
         * This method is invoked by the default implementation of
         * {@link #onCancelled(Object)}.</p>
         *
         * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
         * {@link #doInBackground(Object[])} has finished.</p>
         *
         * @see #onCancelled(Object)
         * @see #cancel(boolean)
         * @see #isCancelled()
         */
        protected void onCancelled() {
        }

        /**
         * Returns <tt>true</tt> if this task was cancelled before it completed
         * normally. If you are calling {@link #cancel(boolean)} on the task,
         * the value returned by this method should be checked periodically from
         * {@link #doInBackground(Object[])} to end the task as soon as possible.
         *
         * @return <tt>true</tt> if task was cancelled before it completed
         *
         * @see #cancel(boolean)
         */
        public final boolean isCancelled() {
            return mFuture.isCancelled();
        }

        /**
         * <p>Attempts to cancel execution of this task.  This attempt will
         * fail if the task has already completed, already been cancelled,
         * or could not be cancelled for some other reason. If successful,
         * and this task has not started when <tt>cancel</tt> is called,
         * this task should never run. If the task has already started,
         * then the <tt>mayInterruptIfRunning</tt> parameter determines
         * whether the thread executing this task should be interrupted in
         * an attempt to stop the task.</p>
         *
         * <p>Calling this method will result in {@link #onCancelled(Object)} being
         * invoked on the UI thread after {@link #doInBackground(Object[])}
         * returns. Calling this method guarantees that {@link #onPostExecute(Object)}
         * is never invoked. After invoking this method, you should check the
         * value returned by {@link #isCancelled()} periodically from
         * {@link #doInBackground(Object[])} to finish the task as early as
         * possible.</p>
         *
         * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
         *        task should be interrupted; otherwise, in-progress tasks are allowed
         *        to complete.
         *
         * @return <tt>false</tt> if the task could not be cancelled,
         *         typically because it has already completed normally;
         *         <tt>true</tt> otherwise
         *
         * @see #isCancelled()
         * @see #onCancelled(Object)
         */
        public final boolean cancel(boolean mayInterruptIfRunning) {
            return mFuture.cancel(mayInterruptIfRunning);
        }

        /**
         * Waits if necessary for the computation to complete, and then
         * retrieves its result.
         *
         * @return The computed result.
         *
         * @throws CancellationException If the computation was cancelled.
         * @throws ExecutionException If the computation threw an exception.
         * @throws InterruptedException If the current thread was interrupted
         *         while waiting.
         */
        public final Result get() throws InterruptedException, ExecutionException {
            return mFuture.get();
        }

        /**
         * Waits if necessary for at most the given time for the computation
         * to complete, and then retrieves its result.
         *
         * @param timeout Time to wait before cancelling the operation.
         * @param unit The time unit for the timeout.
         *
         * @return The computed result.
         *
         * @throws CancellationException If the computation was cancelled.
         * @throws ExecutionException If the computation threw an exception.
         * @throws InterruptedException If the current thread was interrupted
         *         while waiting.
         * @throws TimeoutException If the wait timed out.
         */
        public final Result get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            return mFuture.get(timeout, unit);
        }

        /**
         * Executes the task with the specified parameters. The task returns
         * itself (this) so that the caller can keep a reference to it.
         *
         * <p>Note: this function schedules the task on a queue for a single background
         * thread or pool of threads depending on the platform version.  When first
         * introduced, AsyncTasks were executed serially on a single background thread.
         * Starting with {@link android.os.Build.VERSION_CODES#DONUT}, this was changed
         * to a pool of threads allowing multiple tasks to operate in parallel.  After
         * {@link android.os.Build.VERSION_CODES#HONEYCOMB}, it is planned to change this
         * back to a single thread to avoid common application errors caused
         * by parallel execution.  If you truly want parallel execution, you can use
         * the {@link #executeOnExecutor} version of this method
         * with {@link #THREAD_POOL_EXECUTOR}; however, see commentary there for warnings on
         * its use.
         *
         * <p>This method must be invoked on the UI thread.
         *
         * @param params The parameters of the task.
         *
         * @return This instance of AsyncTask.
         *
         * @throws IllegalStateException If {@link #getStatus()} returns either
         *         {@link AsyncTask.Status#RUNNING} or {@link AsyncTask.Status#FINISHED}.
         */
        public final ModernAsyncTask<Params, Progress, Result> execute(Params... params) {
            return executeOnExecutor(sDefaultExecutor, params);
        }

        /**
         * Executes the task with the specified parameters. The task returns
         * itself (this) so that the caller can keep a reference to it.
         *
         * <p>This method is typically used with {@link #THREAD_POOL_EXECUTOR} to
         * allow multiple tasks to run in parallel on a pool of threads managed by
         * AsyncTask, however you can also use your own {@link Executor} for custom
         * behavior.
         *
         * <p><em>Warning:</em> Allowing multiple tasks to run in parallel from
         * a thread pool is generally <em>not</em> what one wants, because the order
         * of their operation is not defined.  For example, if these tasks are used
         * to modify any state in common (such as writing a file due to a button click),
         * there are no guarantees on the order of the modifications.
         * Without careful work it is possible in rare cases for the newer version
         * of the data to be over-written by an older one, leading to obscure data
         * loss and stability issues.  Such changes are best
         * executed in serial; to guarantee such work is serialized regardless of
         * platform version you can use this function with {@link #SERIAL_EXECUTOR}.
         *
         * <p>This method must be invoked on the UI thread.
         *
         * @param exec The executor to use.  {@link #THREAD_POOL_EXECUTOR} is available as a
         *              convenient process-wide thread pool for tasks that are loosely coupled.
         * @param params The parameters of the task.
         *
         * @return This instance of AsyncTask.
         *
         * @throws IllegalStateException If {@link #getStatus()} returns either
         *         {@link AsyncTask.Status#RUNNING} or {@link AsyncTask.Status#FINISHED}.
         */
        public final ModernAsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec,
                Params... params) {
            if (mStatus != Status.PENDING) {
                switch (mStatus) {
                    case RUNNING:
                        throw new IllegalStateException("Cannot execute task:"
                                + " the task is already running.");
                    case FINISHED:
                        throw new IllegalStateException("Cannot execute task:"
                                + " the task has already been executed "
                                + "(a task can be executed only once)");
                }
            }

            mStatus = Status.RUNNING;

            onPreExecute();

            mWorker.mParams = params;
            exec.execute(mFuture);

            return this;
        }

        /**
         * Convenience version of {@link #execute(Object...)} for use with
         * a simple Runnable object.
         */
        public static void execute(Runnable runnable) {
            sDefaultExecutor.execute(runnable);
        }

        /**
         * This method can be invoked from {@link #doInBackground} to
         * publish updates on the UI thread while the background computation is
         * still running. Each call to this method will trigger the execution of
         * {@link #onProgressUpdate} on the UI thread.
         *
         * {@link #onProgressUpdate} will note be called if the task has been
         * canceled.
         *
         * @param values The progress values to update the UI with.
         *
         * @see #onProgressUpdate
         * @see #doInBackground
         */
        protected final void publishProgress(Progress... values) {
            if (!isCancelled()) {
                sHandler.obtainMessage(MESSAGE_POST_PROGRESS,
                        new AsyncTaskResult<Progress>(this, values)).sendToTarget();
            }
        }

        private void finish(Result result) {
            if (isCancelled()) {
                onCancelled(result);
            } else {
                onPostExecute(result);
            }
            mStatus = Status.FINISHED;
        }

        private static class InternalHandler extends Handler {
            @SuppressWarnings({"unchecked", "rawtypes"})
            @Override
            public void handleMessage(Message msg) {
                AsyncTaskResult result = (AsyncTaskResult) msg.obj;
                switch (msg.what) {
                    case MESSAGE_POST_RESULT:
                        // There is only one result
                        result.mTask.finish(result.mData[0]);
                        break;
                    case MESSAGE_POST_PROGRESS:
                        result.mTask.onProgressUpdate(result.mData);
                        break;
                }
            }
        }

        private static abstract class WorkerRunnable<Params, Result> implements Callable<Result> {
            Params[] mParams;
        }

        @SuppressWarnings("rawtypes")
        private static class AsyncTaskResult<Data> {
			final ModernAsyncTask mTask;
            final Data[] mData;

            AsyncTaskResult(ModernAsyncTask task, Data... data) {
                mTask = task;
                mData = data;
            }
        }
    }
}
