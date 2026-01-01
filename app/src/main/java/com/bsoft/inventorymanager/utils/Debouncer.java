package com.bsoft.inventorymanager.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Utility class to debounce actions, ensuring they are only executed
 * after a specified delay has passed without a new request.
 * Useful for search inputs to prevent filtering on every keystroke.
 */
public class Debouncer {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private final long delayMillis;

    /**
     * @param delayMillis The delay in milliseconds before executing the action.
     */
    public Debouncer(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    /**
     * Schedules the runnable to be executed after the delay.
     * If a runnable was already scheduled, it is cancelled and the timer restarts.
     *
     * @param runnable The action to execute.
     */
    public void debounce(Runnable runnable) {
        if (this.runnable != null) {
            handler.removeCallbacks(this.runnable);
        }
        this.runnable = runnable;
        handler.postDelayed(this.runnable, delayMillis);
    }

    /**
     * Cancels any pending runnable.
     */
    public void cancel() {
        if (this.runnable != null) {
            handler.removeCallbacks(this.runnable);
            this.runnable = null;
        }
    }
}
