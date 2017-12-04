package com.cinggl.cinggl.firestore;

import com.firebase.ui.common.BaseSnapshotParser;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Created by J.EL on 11/16/2017.
 */

public interface SnapshotParser<T> extends BaseSnapshotParser<DocumentSnapshot, T> {}
