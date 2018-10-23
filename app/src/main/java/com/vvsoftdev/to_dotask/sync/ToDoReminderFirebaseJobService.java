package com.vvsoftdev.to_dotask.sync;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.RetryStrategy;

//  ToDoReminderFirebaseJobService should extend from JobService
public class ToDoReminderFirebaseJobService extends JobService {

    private AsyncTask mBackgroundTask;

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     *
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return whether there is more work remaining.
     */
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        // By default, jobs are executed on the main thread, so make an anonymous class extending
        //  AsyncTask called mBackgroundTask.
        // Here's where we make an AsyncTask so that this is no longer on the main thread
        mBackgroundTask = new AsyncTask() {

            // Override doInBackground
            @Override
            protected Object doInBackground(Object[] params) {
                // Use ReminderTasks to execute the new To-Do reminder task you made, use
                // this service as the context (ToDoReminderFirebaseJobService.this) and return null
                // when finished.
                Context context = ToDoReminderFirebaseJobService.this;
                ReminderTasks.executeTask(context, ReminderTasks.ACTION_REMIND_NOTIFICATION);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                //  Override onPostExecute and called jobFinished. Pass the job parameters
                // and false to jobFinished. This will inform the JobManager that your job is done
                // and that you do not want to reschedule the job.

                /*
                 * Once the AsyncTask is finished, the job is finished. To inform JobManager that
                 * you're done, you call jobFinished with the jobParameters that were passed to your
                 * job and a boolean representing whether the job needs to be rescheduled. This is
                 * usually if something didn't work and you want the job to try running again.
                 */

                jobFinished(jobParameters, false);
            }
        };

        //  Execute the AsyncTask
        mBackgroundTask.execute();
        return true;
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @return whether the job should be retried
     * @see Job.Builder#setRetryStrategy(RetryStrategy)
     * @see RetryStrategy
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        //  If mBackgroundTask is valid, cancel it
        //  Return true to signify the job should be retried
        if (mBackgroundTask != null) mBackgroundTask.cancel(true);
        return true;
    }
}