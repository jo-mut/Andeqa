package com.cinggl.cinggl.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.bitmap.ImageThread;
import com.cinggl.cinggl.camera.CingleCreateFragment;


public class ImageAdapter extends BaseAdapter {

    private Context context;
    private long[] thumbnails;
    private String[] urls;
    private OnClickItemListener listener;

    public ImageAdapter(Context context, long[] thumbnails, String[] urls, OnClickItemListener listener) {
        this.context = context;
        this.thumbnails = thumbnails;
        this.urls = urls;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return thumbnails.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.thumbnail, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.image);
            holder.view = convertView.findViewById(R.id.holder);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.id = thumbnails[position];

        CingleCreateFragment.executor.execute(new ImageThread(context, holder));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(urls[position]);
            }
        });

        return convertView;
    }

    public interface OnClickItemListener {
        public void onClick(String url);
    }

    public class ViewHolder {
        public ImageView imageView;
        public View view;
        public long id;
    }
}