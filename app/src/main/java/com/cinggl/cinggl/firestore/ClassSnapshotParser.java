package com.cinggl.cinggl.firestore;

import com.firebase.ui.common.Preconditions;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Created by J.EL on 11/16/2017.
 */

public class ClassSnapshotParser <T> implements SnapshotParser<T> {

    private final Class<T> mModelClass;

    public ClassSnapshotParser(Class<T> modelClass) {
        mModelClass = Preconditions.checkNotNull(modelClass);
    }

    @Override
    public T parseSnapshot(DocumentSnapshot snapshot) {
        return snapshot.toObject(mModelClass);
    }
}
