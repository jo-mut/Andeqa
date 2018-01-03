package com.cinggl.cinggl;

import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.cinggl.cinggl.models.TraceData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by J.EL on 1/2/2018.
 */

public class Trace {
    // Time from which a particular view has been started viewing.
    private long startTime = 0;

    // Time at which a particular view has been stopped viewing.
    private long endTime = 0;

    // Flag is required because 'addOnGlobalLayoutListener' is called multiple times.
    // The flag limits the action inside 'onGlobalLayout' to only once.
    private boolean firstTraceFlag = false;

    // Flag to pause tracking.
    private boolean tracingPaused = false;

    // ArrayList of view positions that have been viewed for more than the threshold time.
    private ArrayList<Integer> positionOfViewsViewed = new ArrayList<>();

    // ArrayList of TrackingData class instances.
    private ArrayList<TraceData> traceData = new ArrayList<>();

    // The instance of the recyclerView whose views are supposed to be tracked.
    private RecyclerView cingleOutRecyclerView;

    // Time interval after which data should be given to the user.
    private long dataDumpInterval;

    // The minimum time user must spend on a view item for its tracking data to be considered.
    private long minimumViewingTimeThreshold;

    // Boolean flag to inform whether to dump data at an interval or not.
    private boolean dumpDataAfterInterval;

    // The minimum amount of area of the list item that should be on
    // the screen for the tracking to start.
    private double minimumVisibleHeightThreshold;

    // Reference to the `TrailTrackingListener` interface.
    private TracingListener tracingListener;

    private Timer dataDumpTimer = new Timer();

    private DatabaseReference traceRef;

    private int impression;

    private final static String TAG = Trace.class.getSimpleName();


    public Trace(Builder builder) {

        this.cingleOutRecyclerView = builder.cingleOutRecyclerView;
        this.dataDumpInterval = builder.dataDumpInterval;
        this.minimumVisibleHeightThreshold = builder.minimumVisibleHeightThreshold;
        this.minimumViewingTimeThreshold = builder.minimumViewingTimeThreshold;
        this.tracingListener = builder.tracingListener;
        this.dumpDataAfterInterval = builder.dumpDataAfterInterval;
    }

    //Start tracing the process
    public void startTracing() {

        // Track the views when the data is loaded into recycler view for the first time.
        cingleOutRecyclerView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        if(!firstTraceFlag) {

                            startTime = System.currentTimeMillis();

                            int firstVisibleItemPosition = ((LinearLayoutManager)
                                    cingleOutRecyclerView.getLayoutManager())
                                    .findFirstVisibleItemPosition();

                            int lastVisibleItemPosition = ((LinearLayoutManager)
                                    cingleOutRecyclerView.getLayoutManager())
                                    .findLastVisibleItemPosition();

                            analyzeAndAddViewData(firstVisibleItemPosition,
                                    lastVisibleItemPosition);

                            firstTraceFlag = true;
                        }
                    }
                });

        // Track the views when user scrolls through the recyclerview.
        cingleOutRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!tracingPaused) {

                    // User is scrolling, calculate and store the tracking data of the views
                    // that were being viewed before the scroll.
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {

                        endTime = System.currentTimeMillis();

                        for (int trackedViewsCount = 0;
                             trackedViewsCount < positionOfViewsViewed.size();
                             trackedViewsCount++ ) {

                            long duration = endTime - startTime;

                            if (duration > minimumViewingTimeThreshold) {

                                View itemView = recyclerView.getLayoutManager()
                                        .findViewByPosition(positionOfViewsViewed
                                                .get(trackedViewsCount));
                                try {

                                    traceData.add(prepareTrackingData(String
                                                    .valueOf(positionOfViewsViewed
                                                            .get(trackedViewsCount)),
                                            duration, getVisibleHeightPercentage(itemView)));
                                }catch (Exception e){

                                }

                            }
                        }

                        positionOfViewsViewed.clear();
                    }

                    // Scrolling has ended, start the tracking process by assigning a start time
                    // and maintaining a list of views being viewed.
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                        startTime = System.currentTimeMillis();

                        int firstVisibleItemPosition = ((LinearLayoutManager)
                                recyclerView.getLayoutManager())
                                .findFirstVisibleItemPosition();

                        int lastVisibleItemPosition = ((LinearLayoutManager)
                                recyclerView.getLayoutManager())
                                .findLastVisibleItemPosition();

                        analyzeAndAddViewData(firstVisibleItemPosition, lastVisibleItemPosition);
                    }
                }
            }
        });

        try{
            if(dumpDataAfterInterval)
                dumpDataAfterSpecifiedInterval();
        }catch (Exception e){

        }
    }

    // Stop tracing the currently visible and then stop the process;
    public void stopTracing() {

        if(!tracingPaused) {

            endTime = System.currentTimeMillis();

            int firstVisibleItemPosition = ((LinearLayoutManager)
                    cingleOutRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();

            int lastVisibleItemPosition = ((LinearLayoutManager)
                    cingleOutRecyclerView.getLayoutManager()).findLastVisibleItemPosition();

            analyzeAndAddViewData(firstVisibleItemPosition, lastVisibleItemPosition);

            for (int trackedViewsCount = 0; trackedViewsCount < positionOfViewsViewed.size();
                 trackedViewsCount++ ) {

                long duration = endTime - startTime;

                if (duration > minimumViewingTimeThreshold) {

                    View itemView = cingleOutRecyclerView.getLayoutManager()
                            .findViewByPosition(positionOfViewsViewed.get(trackedViewsCount));

                    traceData.add(prepareTrackingData(String.valueOf(
                            positionOfViewsViewed.get(trackedViewsCount)),
                            duration,
                            getVisibleHeightPercentage(itemView)));
                }
            }

            dataDumpTimer.cancel();
            positionOfViewsViewed.clear();
        }
    }

    // pause tracing
    public void pauseTracing() {

        tracingPaused = true;
    }

    //resume tracing
    public void resumeTracing() {

        tracingPaused = false;
    }

    // return the status of the tracing
    public int tracingStatus() {

        if(tracingPaused) {
            // Paused
            return 0;
        } else {
            // Running.
            return 1;
        }
    }

    // stop tracing, and hand over the trace data
    public ArrayList<TraceData> getTraceData(boolean stopTracing) {

        if(stopTracing)
            stopTracing();

        return traceData;
    }

    //clear all the tracking data
    public boolean clearAllTraceData() {

        try {

            traceData.clear();
            traceData = null;
            return true;
        } catch (Exception ex) {

            ex.printStackTrace();
            return false;
        }
    }

    //hand over all the traceData
    private void dumpDataAfterSpecifiedInterval() {

        try{
            dataDumpTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    tracingListener.traceDataDump(traceData);
                    traceData.clear();
                    dumpDataAfterSpecifiedInterval();
                }
            }, dataDumpInterval);
        }catch (Exception e){

        }
    }

    //analyse if the view is as much visible or not and start tracing the view
    private void analyzeAndAddViewData(int firstVisibleItemPosition, int lastVisibleItemPosition) {

        // Analyze all the views
        for (int viewPosition = firstVisibleItemPosition;
             viewPosition <= lastVisibleItemPosition; viewPosition++) {

            Log.i("View being considered", String.valueOf(viewPosition));

            // Get the view from its position.
            View itemView = cingleOutRecyclerView.getLayoutManager()
                    .findViewByPosition(viewPosition);

            // Check if the visibility of the view is more than or equal
            // to the threshold provided. If it falls under the desired limit, add it to the
            // tracking data.
            try{
                if (getVisibleHeightPercentage(itemView) >= minimumVisibleHeightThreshold) {

                    positionOfViewsViewed.add(viewPosition);
                }
            }catch (Exception e){

            }
        }
    }

    //calaculate how many times the itemView has been viewed above the minimum threshold time


    //calculate how much height of the view is visible on the screen
    private double getVisibleHeightPercentage(View view) {

        Rect itemRect = new Rect();
        view.getLocalVisibleRect(itemRect);

        double visibleHeight = itemRect.height();
        double totalHeightOfTheView = view.getMeasuredHeight();

        return ((visibleHeight/totalHeightOfTheView) * 100);
    }

    //store an instance of the traceData and then return that instance
    private TraceData prepareTrackingData
    (String viewId, long viewDuration, double percentageHeightVisible) {

        TraceData traceData = new TraceData();

        traceData.setViewId(viewId);
        traceData.setViewDuartion(viewDuration);
        traceData.setPercentageHeightVisible(percentageHeightVisible);

        traceRef = FirebaseDatabase.getInstance().getReference("Data");
        traceRef.setValue(traceData);


        Log.d("Traced data", traceData.getViewId());
        return traceData;
    }

    public interface TracingListener {

        //Method to dump data all the tracking data.
        void traceDataDump(ArrayList<TraceData> data);
    }

    /**
     * Class for builder pattern.
     */
    public static class Builder {

        private RecyclerView cingleOutRecyclerView;
        private long dataDumpInterval = 60000; // Default to 1 minute.
        private double minimumVisibleHeightThreshold = 60; // Default to 60 percent.
        private long minimumViewingTimeThreshold = 3000; // Default to 3 seconds.
        private boolean dumpDataAfterInterval = false;
        private TracingListener tracingListener = null;

        //Interval after which the traceData will be handed over
        public Builder setDataDumpInterval(long dataDumpInterval) {
            this.dataDumpInterval = dataDumpInterval;
            return this;
        }

        //RecyclerView whose items are supposed to be traced
        public Builder setRecyclerView(RecyclerView cingleOutRecyclerView) {
            this.cingleOutRecyclerView = cingleOutRecyclerView;
            return this;
        }

        //the minimum amount of heigh the list item should have on the screen for the tracing to start
        public Builder setMinimumVisibleHeightThreshold(double minimumVisibleHeightThreshold) {
            this.minimumVisibleHeightThreshold = minimumVisibleHeightThreshold;
            return this;
        }

        //the time the user must spend on an item for it to be considered a view;
        public Builder setMinimumViewingTimeThreshold(long minimumViewingTimeThreshold) {
            this.minimumViewingTimeThreshold = minimumViewingTimeThreshold;
            return this;
        }
        //Reference of the class implementing the TracingListener interface;
        public Builder setTracingListener(TracingListener tracingListener) {
            this.tracingListener = tracingListener;
            return this;
        }

        public Builder dumpDataAfterInterval(Boolean dumpDataAfterInterval) {
            this.dumpDataAfterInterval = dumpDataAfterInterval;
            return this;
        }

        public Trace build() {
            return new Trace(this);
        }
    }
}
