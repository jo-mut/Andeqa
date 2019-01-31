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
import com.andeqa.andeqa.models.ViewDuration;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.SecureRandom;
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
    private DatabaseReference impressionReference;
    private DatabaseReference databaseReference;
    private DatabaseReference seenMessagesReference;
    private FirebaseAuth firebaseAuth;

    public interface VisibilityTrackerListener {
        void onVisibilityChanged(List<View> visibleViews, List<View> invisibleViews);
    }

    public static class TrackingInfo {
        View view;
        String id;
        String type;
        String userId;
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
            seenMessagesReference = FirebaseDatabase.getInstance().getReference(Constants.MESSAGES);

            impressionReference.keepSynced(true);
            seenMessagesReference.keepSynced(true);
        }
    }

    public void addView(@NonNull View view, int minVisiblePercentageViewed, String postId, String type, String userId) {

        TrackingInfo trackingInfo = mTrackedViews.get(view);
        if (trackingInfo == null) {
            // view is not yet being tracked
            trackingInfo = new TrackingInfo();
            mTrackedViews.put(view, trackingInfo);
            scheduleVisibilityCheck();
        }

        trackingInfo.view = view;
        trackingInfo.minVisiblePercentage = minVisiblePercentageViewed;
        trackingInfo.id = postId;
        trackingInfo.type = type;
        trackingInfo.userId = userId;

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
            for (final Map.Entry<View, TrackingInfo> entry : mTrackedViews.entrySet()) {
                final View view = entry.getKey();
                final String viewedId = entry.getValue().id;
                final String type = entry.getValue().type;
                final long time = System.currentTimeMillis();
                final int percentage = entry.getValue().minVisiblePercentage;
                final String impressionId = databaseReference.child("generateId").push().getKey();


                if (type.equals("post")){
                    impressionReference.child("post_views").child(viewedId)
                            .child(firebaseAuth.getCurrentUser().getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (!dataSnapshot.exists()){
                                        Impression impression = new Impression();
                                        impression.setTime(time);
                                        impression.setImpression_id(impressionId);
                                        impression.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                        impression.setPost_id(viewedId);
                                        impressionReference.child("post_views").child(viewedId)
                                                .child(firebaseAuth.getCurrentUser().getUid())
                                                .setValue(impression);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
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
