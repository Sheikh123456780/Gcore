package android.app.job;

import android.app.job.JobParameters;

interface IJobService {
    void startJob(in JobParameters jobParams);
    void stopJob(in JobParameters jobParams);
}
