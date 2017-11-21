package com.example.qiaowenhao.myimageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by qiaowenhao on 17-11-21.
 */

public class ImageAdapter extends ArrayAdapter {
    private LruCache<String, BitmapDrawable> mMemoryCache;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String url = (String) getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.image_item, null);
        } else {
            view = convertView;
        }

        ImageView image = view.findViewById(R.id.image);
        BitmapDrawable drawable = getBitMapFromCache(url);
        if (drawable != null) {
            image.setImageDrawable(drawable);
        } else {
            new BitMapWorkerTask(image).execute(url);
        }
        return view;
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
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, BitmapDrawable>(cacheSize){
            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getByteCount();
            }
        };
    }

    class BitMapWorkerTask extends AsyncTask<String, Void, BitmapDrawable> {
        private ImageView mImageView;

        @Override
        protected void onPostExecute(BitmapDrawable drawable) {
            if (mImageView != null && drawable != null) {
                mImageView.setImageDrawable(drawable);
            }
        }

        @Override
        protected BitmapDrawable doInBackground(String... strings) {
            String imageUrl = strings[0];
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

        public BitMapWorkerTask(ImageView imageView) {
            mImageView = imageView;
        }
    }

}
