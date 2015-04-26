package net.jejer.hipda.glide;

/**
 * class that load lots of images
 * Created by GreenSkinMonster on 2015-04-24.
 */
public interface ImageContainer {
    void markImageReady(String url, ImageReadyInfo imageReadyInfo);

    void loadImage(String url, GlideImageView imageView);
}
