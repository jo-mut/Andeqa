package com.cinggl.cinggl.message;

import android.content.Context;
import android.view.ViewGroup;

import com.cinggl.cinggl.firestore.FirestoreAdapter;
import com.cinggl.cinggl.viewholders.MessageViewHolder;
import com.google.firebase.firestore.Query;

/**
 * Created by J.EL on 1/4/2018.
 */

public class MessagingUsersAdapter extends FirestoreAdapter<MessageViewHolder> {
    private static final String TAG = MessagingUsersAdapter.class.getSimpleName();
    private Context mContext;
    private static final String KEY_LAYOUT_POSITION = "layout pooition";
    private static final String EXTRA_USER_UID = "uid";
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;

    public MessagingUsersAdapter(Query query, Context mContext) {
        super(query);
        this.mContext = mContext;
    }


    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }



}
