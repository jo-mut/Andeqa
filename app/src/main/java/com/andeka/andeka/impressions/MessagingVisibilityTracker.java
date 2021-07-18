package com.andeka.andeka.impressions;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.andeka.andeka.Constants;
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

public class MessagingVisibilityTracker {
    private static final String TAG = MessagingVisibilityTracker.class.getSimpleName();
    private static final long VISIBILITY_CHECK_DELAY_MILLIS = 100;
    private WeakHashMap<View, TrackingInfo> mTrackedViews = new WeakHashMap<>();
    private ViewTreeObserver.OnPreDrawListener mOnPreDrawListener;
    private ImpressionTracker.VisibilityTrackerListener mVisibilityTrackerListener;
    private boolean mIsVisibilityCheckScheduled;
    private ImpressionTracker.VisibilityChecker mVisibilityChecker;
    private Handler mVisibilityHandler;
    private Runnable mVisibilityRunnable;
    private String postId;
    //current user impression
    private boolean processCompiledImpression = false;
    //all users impressions
    private boolean processOverallImpressions = false;
    private boolean processImpression = false;

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
        String senderId;
        String receiverId;
        int minVisiblePercentage;
    }


    public MessagingVisibilityTracker(Activity activity) {
        View rootView = activity.getWindow().getDecorView();
        ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        mVisibilityHandler = new Handler();
        mVisibilityChecker = new ImpressionTracker.VisibilityChecker();
        mVisibilityRunnable = new MessagingVisibilityTracker.VisibilityRunnable();

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

    public void addView(@NonNull View view, int minVisiblePercentageViewed, String id, String type, String receiverId, String senderId) {

        TrackingInfo trackingInfo = mTrackedViews.get(view);
        if (trackingInfo == null) {
            // view is not yet being tracked
            trackingInfo = new TrackingInfo();
            mTrackedViews.put(view, trackingInfo);
            scheduleVisibilityCheck();
        }

        trackingInfo.view = view;
        trackingInfo.minVisiblePercentage = minVisiblePercentageViewed;
        trackingInfo.type = type;
        trackingInfo.id = id;
        trackingInfo.senderId = senderId;
        trackingInfo.receiverId = receiverId;

    }

    public void setVisibilityTrackerListener(ImpressionTracker.VisibilityTrackerListener listener) {
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
            processImpression = true;
            for (final Map.Entry<View, TrackingInfo> entry : mTrackedViews.entrySet()) {
                final View view = entry.getKey();
                final String viewedId = entry.getValue().id;
                final String type = entry.getValue().type;
                final long time = System.currentTimeMillis();
                final String receiverId = entry.getValue().receiverId;
                final String senderId = entry.getValue().senderId;
                final int percentage = entry.getValue().minVisiblePercentage;
                final String impressionId = databaseReference.child("generateId").push().getKey();

                if (type.equals("message")){
                    Log.d("view type is", viewedId);
                    if (percentage >= 50){
                        Log.d("view percentage is", percentage + "");
                        if (receiverId.equals(firebaseAuth.getCurrentUser().getUid())){
                            seenMessagesReference.child("seen_messages")
                                    .child(receiverId).child(senderId)
                                    .child(viewedId).child("seen").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        seenMessagesReference.child("seen_messages")
                                                .child(receiverId).child(senderId)
                                                .child(viewedId).child("seen").setValue("seen");
                                        Log.d("view is seen", viewedId);
                                    }else {
                                        Log.d("view not seen", viewedId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
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
