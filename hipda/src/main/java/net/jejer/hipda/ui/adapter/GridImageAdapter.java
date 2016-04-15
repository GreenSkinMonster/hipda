package net.jejer.hipda.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import net.jejer.hipda.R;
import net.jejer.hipda.job.UploadImage;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by GreenSkinMonster on 2016-04-12.
 */
public class GridImageAdapter extends BaseAdapter {
    private Activity mContext;
    private Collection<UploadImage> mImages = new ArrayList<>();

    public GridImageAdapter(Activity c) {
        mContext = c;
    }

    public void setImages(Collection<UploadImage> images) {
        mImages = images;
        notifyDataSetChanged();
    }

    public int getCount() {
        return mImages.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        UploadImage image = mImages.toArray(new UploadImage[mImages.size()])[position];
        View squareLayout;
        if (convertView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            squareLayout = inflater.inflate(R.layout.item_grid_image, parent, false);
        } else {
            squareLayout = convertView;
        }
        ImageView imageView = (ImageView) squareLayout.findViewById(R.id.image);
        imageView.setImageBitmap(image.getThumb());
        imageView.setTag(image.getImgId());
        return imageView;
    }

}