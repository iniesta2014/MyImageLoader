package com.example.qiaowenhao.myimageloader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by qiaowenhao on 17-11-21.
 */

public class ImageAdapter extends ArrayAdapter {
    private ListView mListView;
    private Bitmap mLoadingBitmap;
    private LruCache<String, BitmapDrawable> mMemoryCache;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        mListView = (ListView) parent;
        String url = (String) getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.image_item, null);
        } else {
            view = convertView;
        }

        ImageView image = view.findViewById(R.id.image);
        BitmapDrawable drawable = getBitMapFromCache(url);
        image.setImageResource(R.drawable.default_icon);
        image.setTag(url);
        if (drawable != null) {
            image.setImageDrawable(drawable);
        } else if (cancelPotentialWork(url, image)) {
            BitmapWorkerTask task = new BitMapWorkerTask(image);
            AsyncDrawable asyncDrawable = new AsyncDrawable(getContext().getResources(), )
        }
        return view;
    }

    private boolean cancelPotentialWork(String url, ImageView image) {

    }

    private BitmapDrawable getBitMapFromCache(String key) {
        return mMemoryCache.get(key);
    }

    private void addBitMapFromCache(String key, BitmapDrawable drawable) {
        if (getBitMapFromCache(key) == null) {
            mMemoryCache.put(key, drawable);
        }
    }

    public ImageAdapter(@NonNull Context context, int resource, @NonNull Object[] objects) {
        super(context, resource, objects);
        mLoadingBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_icon);
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getByteCount();
            }
        };
    }

    class BitMapWorkerTask extends AsyncTask<String, Void, BitmapDrawable> {
        String imageUrl;
        private WeakReference<ImageView> imageViewWeakReference;

        public BitMapWorkerTask(ImageView imageView) {
            imageViewWeakReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected void onPostExecute(BitmapDrawable drawable) {
            ImageView imageView = getAttachedImageView();
            if (imageView != null && drawable != null) {
                imageView.setImageDrawable(drawable);
            }
        }

        private ImageView getAttachedImageView() {
            ImageView imageView = imageViewWeakReference.get();
            BitMapWorkerTask bitMapWorkerTask = getBitmapWorkerTask(imageView);
            if (this == bitMapWorkerTask) {
                return imageView;
            }
            return null;
        }

        @Override
        protected BitmapDrawable doInBackground(String... strings) {
            imageUrl = strings[0];
            Bitmap bitmap = downLoadBitmap(imageUrl);
            BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), bitmap);
            addBitMapFromCache(imageUrl, drawable);
            return drawable;
        }

        private Bitmap downLoadBitmap(String imageUrl) {
            Bitmap bitmap = null;
            HttpURLConnection con = null;
            try {
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection();
                bitmap = BitmapFactory.decodeStream(con.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
            return bitmap;
        }
    }

    private BitMapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private class AsyncDrawable extends BitmapDrawable {
        private WeakReference<BitMapWorkerTask> bitMapWorkerTaskWeakReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitMapWorkerTask bitMapWorkerTask) {
            super(res, bitmap);
            this.bitMapWorkerTaskWeakReference = new WeakReference<BitMapWorkerTask>(
                    bitMapWorkerTask);
        }
        public BitMapWorkerTask getBitmapWorkerTask() {
            return bitMapWorkerTaskWeakReference.get();
        }
    }
}
