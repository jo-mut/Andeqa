package com.andeqa.andeqa.firestore;

import com.firebase.ui.common.BaseCachingSnapshotParser;
import com.firebase.ui.common.BaseSnapshotParser;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Created by J.EL on 11/16/2017.
 */

public class CachingSnapshotParser<T> extends BaseCachingSnapshotParser<DocumentSnapshot, T>
        implements SnapshotParser<T> {

    public CachingSnapshotParser(BaseSnapshotParser<DocumentSnapshot, T> parser) {
        super(parser);
    }

    @Override
    public String getId(DocumentSnapshot snapshot) {
        return snapshot.getId();
    }
}
