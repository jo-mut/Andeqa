package com.cinggl.cinggl.utils;

import com.cinggl.cinggl.models.Cingle;

import java.util.List;

/**
 * Created by J.EL on 7/14/2017.
 */

public interface OnLoadMoreListener {
    void  onSuccess(List<Cingle> cingles);
    void  onFailure (String msg, Exception e);
}
