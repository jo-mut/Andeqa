package com.andeqa.andeqa.impressions;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.models.Impression;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ImpressionTracker {
    private static final String TAG = ImpressionTracker.class.getSimpleName();
    private static final long VISIBILITY_CHECK_DELAY_MILLIS = 100;
    private WeakHashMap<View, TrackingInfo> mTrackedViews = new WeakHashMap<>();
    private ViewTreeObserver.OnPreDrawListener mOnPreDrawListener;
    private VisibilityTrackerListener mVisibilityTrackerListener;
    private boolean mIsVisibilityCheckScheduled;
    private VisibilityChecker mVisibilityChecker;
    private Handler mVisibilityHandler;
    private Runnable mVisibilityRunnable;
    private String postId;
    //current user impression
    private boolean processCompiledImpression = false;
    //all users impressions
    private boolean processOverallImpressions = false;
    private boolean processOverallImpression = false;

    private DatabaseReference impressionReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    public interface VisibilityTrackerListener {
        void onVisibilityChanged(List<View> visibleViews, List<View> invisibleViews);
    }

    public static class TrackingInfo {
        View view;
        String post_id;
        long duration;
        int minVisiblePercentage;
    }

    public ImpressionTracker(Activity activity) {
        View rootView = activity.getWindow().getDecorView();
        ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        mVisibilityHandler = new Handler();
        mVisibilityChecker = new VisibilityChecker();
        mVisibilityRunnable = new VisibilityRunnable();

        if (viewTreeObserver.isAlive()) {
            mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    scheduleVisibilityCheck();
                    return true;
                }
            };
            viewTreeObserver.addOnPreDrawListener(mOnPreDrawListener);
        } else {
            Log.d(ImpressionTracker.class.getSimpleName(), "root view is not live");
        }

        initFirebase();

    }

    private void initFirebase(){
        //initialize firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();
        //firebase references
        if (firebaseAuth.getCurrentUser() != null){
            impressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
            databaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
            impressionReference.keepSynced(true);
        }
    }

    public void addView(@NonNull View view, int minVisiblePercentageViewed, long duration, String postId) {

        TrackingInfo trackingInfo = mTrackedViews.get(view);
        if (trackingInfo == null) {
            // view is not yet being tracked
            trackingInfo = new TrackingInfo();
            mTrackedViews.put(view, trackingInfo);
            scheduleVisibilityCheck();
        }

        trackingInfo.view = view;
        trackingInfo.minVisiblePercentage = minVisiblePercentageViewed;
        trackingInfo.post_id = postId;
        trackingInfo.duration = duration;
    }

    public void setVisibilityTrackerListener(VisibilityTrackerListener listener) {
        mVisibilityTrackerListener = listener;
    }

    public void removeVisibilityTrackerListener() {
        mVisibilityTrackerListener = null;
    }

    private void scheduleVisibilityCheck() {
        if (mIsVisibilityCheckScheduled) {
            return;
        }
        mIsVisibilityCheckScheduled = true;
        mVisibilityHandler.postDelayed(mVisibilityRunnable, VISIBILITY_CHECK_DELAY_MILLIS);
    }


    static class VisibilityChecker {
        private final Rect mClipRect = new Rect();


        boolean isVisible(@Nullable final View view, final int minPercentageViewed) {
            if (view == null || view.getVisibility() != View.VISIBLE || view.getParent() == null) {
                return false;
            }

            if (!view.getGlobalVisibleRect(mClipRect)) {
                return false;
            }

            final long visibleArea = (long) mClipRect.height() * mClipRect.width();
            final long totalViewArea = (long) view.getHeight() * view.getWidth();

            return totalViewArea > 0 && 100 * visibleArea >= minPercentageViewed * totalViewArea;

        }


    }

    class VisibilityRunnable implements Runnable {
        private final List<View> mVisibleViews;
        private final List<View> mInvisibleViews;


        VisibilityRunnable() {
            mVisibleViews = new ArrayList<>();
            mInvisibleViews = new ArrayList<>();
        }

        @Override
        public void run() {
            mIsVisibilityCheckScheduled = false;
            processOverallImpressions = true;
            processCompiledImpression = true;
            processOverallImpression = true;
            for (final Map.Entry<View, TrackingInfo> entry : mTrackedViews.entrySet()) {
                final View view = entry.getKey();
                final int minPercentageViewed = entry.getValue().minVisiblePercentage;
                final String postId = entry.getValue().post_id;
                final long duration = entry.getValue().duration;
                final long time = System.currentTimeMillis();
                final String impressionId = databaseReference.child("generateId").push().getKey();

                if (processOverallImpression){
                    if (duration >= 5000){
                        impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                .child(postId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (processOverallImpressions){
                                    if (dataSnapshot.exists()){
                                        Impression impression = dataSnapshot.getValue(Impression.class);
                                        final String type = impression.getType();
                                        final long recentDuration = impression.getRecent_duration();
                                        final long total_duration = impression.getCompiled_duration();
                                        final long newTotalDuration = total_duration + duration;
                                        final long newRecentDuration = recentDuration + duration;
                                        Log.d("recent duration", recentDuration + "");
                                        Log.d("total duration", total_duration + "");
                                        Log.d("duration", duration + "");
                                        impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                                .child(postId).child("compiled_duration").setValue(newTotalDuration);
                                        impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                                .child(postId).child("recent_duration").setValue(newRecentDuration)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        impressionReference.child("compiled_views").child(postId)
                                                                .addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        if (processCompiledImpression){
                                                                            if (dataSnapshot.exists()){
                                                                                Impression impress = dataSnapshot.getValue(Impression.class);
                                                                                if (type.equals("liked")){
                                                                                    final long newDuration = impress.getCompiled_duration() + newRecentDuration;
                                                                                    impressionReference.child("compiled_views").child(postId)
                                                                                            .child("compiled_duration").setValue(newDuration);
                                                                                    impressionReference.child("compiled_views").child(postId)
                                                                                            .child("recent_duration").setValue(newRecentDuration);
                                                                                    impressionReference.child("compiled_views").child(postId)
                                                                                            .child("un_compiled_duration").setValue(0);
                                                                                    impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                                                                            .child(postId).child("recent_duration").setValue(0);
                                                                                }else if (type.equals("disliked")){
                                                                                    final long newDuration = impress.getCompiled_duration() - newRecentDuration;
                                                                                    impressionReference.child("compiled_views").child(postId)
                                                                                            .child("compiled_duration").setValue(newDuration);
                                                                                    impressionReference.child("compiled_views").child(postId)
                                                                                            .child("recent_duration").setValue(newRecentDuration);
                                                                                    impressionReference.child("compiled_views").child(postId)
                                                                                            .child("un_compiled_duration").setValue(0);
                                                                                    impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                                                                            .child(postId).child("recent_duration").setValue(0);
                                                                                }else {
                                                                                    final long newDuration = impress.getUn_compiled_duration() + newRecentDuration;
                                                                                    impressionReference.child("compiled_views").child(postId)
                                                                                            .child("un_compiled_duration").setValue(newDuration);
                                                                                    impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                                                                            .child(postId).child("recent_duration").setValue(newRecentDuration);
                                                                                }

                                                                                processCompiledImpression = false;
                                                                            }else {
                                                                                Impression impression = new Impression();
                                                                                impression.setCompiled_duration(newTotalDuration);
                                                                                impression.setRecent_duration(newRecentDuration);
                                                                                impression.setUn_compiled_duration(newTotalDuration);
                                                                                impression.setPost_id(postId);
                                                                                impression.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                                                                impression.setImpression_id(impressionId);
                                                                                impression.setTime(time);
                                                                                impression.setType("compiled");
                                                                                impressionReference.child("compiled_views").child(postId)
                                                                                        .setValue(impression);
                                                                                processCompiledImpression = false;
                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });
                                                    }
                                                });
                                        processOverallImpressions = false;

                                    }else {
                                        Impression impression = new Impression();
                                        impression.setCompiled_duration(duration);
                                        impression.setRecent_duration(duration);
                                        impression.setUn_compiled_duration(duration);
                                        impression.setPost_id(postId);
                                        impression.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                        impression.setImpression_id(impressionId);
                                        impression.setTime(time);
                                        impression.setType("un_compiled");
                                        impressionReference.child("user_views").child(firebaseAuth.getCurrentUser().getUid())
                                                .child(postId).setValue(impression);
                                        processOverallImpressions = false;
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    processOverallImpression = false;
                }
            }

            if (mVisibilityTrackerListener != null) {
                mVisibilityTrackerListener.onVisibilityChanged(mVisibleViews, mInvisibleViews);
            }

            mVisibleViews.clear();
            mInvisibleViews.clear();
        }
    }
}
