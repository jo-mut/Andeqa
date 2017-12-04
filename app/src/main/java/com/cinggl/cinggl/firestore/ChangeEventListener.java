package com.cinggl.cinggl.firestore;

import com.firebase.ui.common.BaseChangeEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

/**
 * Created by J.EL on 11/16/2017.
 */

public interface ChangeEventListener extends BaseChangeEventListener<DocumentSnapshot, FirebaseFirestoreException> {}