package com.andeqa.andeqa.utils;

import android.content.Context;

import com.andeqa.andeqa.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FirebaseUtil {
    private Context mContext;
    // firebase firestore
    private CollectionReference mPostsCollection;
    private CollectionReference mUsersCollection;
    private CollectionReference mCommentsCollection;
    private CollectionReference mPostsOfCollection;
    private CollectionReference mCollectionsCollection;
    private CollectionReference mLikesCollections;
    private CollectionReference mTimelineCollection;
    private CollectionReference mChannelsCollection;
    private CollectionReference mRoomsCollection;
    private CollectionReference mPeopleCollection;
    //firebase database
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mImpressionReference;
    //firebase auth
    private FirebaseAuth mFirebaseAuth;

    public FirebaseUtil(Context mContext) {
        this.mContext = mContext;
        initReferences();
    }

    private void initReferences() {
        //firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        //firebase firestore
        mPostsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        mUsersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        mCommentsCollection = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
        mPostsOfCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS_OF_COLLECTION);
        mCollectionsCollection = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        mLikesCollections = FirebaseFirestore.getInstance().collection(Constants.LIKES);
        mTimelineCollection = FirebaseFirestore.getInstance().collection(Constants.TIMELINE);
        mCommentsCollection = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
        mChannelsCollection = FirebaseFirestore.getInstance().collection(Constants.CHANNELS);
        //firebase database
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        mImpressionReference = FirebaseDatabase.getInstance().getReference(Constants.VIEWS);
        mRoomsCollection = FirebaseFirestore.getInstance().collection(Constants.MESSAGES);
        mPeopleCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_RELATIONS);


    }

    /**firebase auth*/
    public FirebaseAuth firebaseAuth(){
        return mFirebaseAuth;
    }

    /**posts collection reference*/
    public CollectionReference postsPath() {
        return mPostsCollection;
    }

    /**post document reference*/
    public DocumentReference postsPath(String postId) {
        return mPostsCollection.document(postId);
    }

    /**users collection reference*/
    public CollectionReference usersPath(){
        return mUsersCollection;
    }

    /**user document reference*/
    public DocumentReference userPath(String userId){
        return mUsersCollection.document(userId);
    }

    /**comments collection ptha*/
    public CollectionReference commentsPath(String postId) {
        return mCommentsCollection.document("post_ids").collection(postId);
    }

    /**comments query*/
    public Query commentsQuery(String postId){
        return mCommentsCollection.document("post_ids").collection(postId)
                .orderBy("comment_id").whereEqualTo("post_id", postId);

    }

    /**comment path*/
    public DocumentReference commentPath(String postId, String commentId) {
        return mCommentsCollection.document("post_ids").collection(postId)
                .document(commentId);
    }

    /** post of collection path*/
    public DocumentReference collectionPath(String collectionId){
        return mPostsOfCollection.document(collectionId);
    }

    /**collections path*/
    public CollectionReference collectionsPath(){
        return mCollectionsCollection;
    }

    /**likes collection path*/
    public CollectionReference likesPath(String postId) {
        return mLikesCollections.document("post_ids").collection(postId);
    }

    /**likes query path*/
    public Query likesQuery(String postId) {
        return mLikesCollections.document("post_ids").collection(postId)
                .whereEqualTo("user_id", firebaseAuth());
    }

    /**timeline collection path*/
    public CollectionReference timelinePath(){
        return mTimelineCollection;
    }

    /**timeline collection path*/
    public CollectionReference timelineCollectionPath(String id){
        return mTimelineCollection.document(id).collection("activities");
    }

    /**random push id*/
    public DatabaseReference pushPath(){
        return mDatabaseReference;
    }

    /**impression path*/
    public DatabaseReference impressionPath(){
        return mImpressionReference;
    }

    /**channels collection path*/
    public CollectionReference channelsPath(){
        return mChannelsCollection;
    }

    /**message collection path*/
    public CollectionReference messagesPath() {
        return mRoomsCollection;
    }

    /**message collection path*/
    public CollectionReference peoplePath() {
        return mPeopleCollection;
    }

}
