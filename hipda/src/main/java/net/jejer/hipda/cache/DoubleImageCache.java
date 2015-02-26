package net.jejer.hipda.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class DoubleImageCache implements ImageCache {
	private final String LOG_TAG = getClass().getSimpleName();
	private LruCache<String, Bitmap> mRamCache;
	private DiskLruCache mDiskCache;
	private CompressFormat mCompressFormat = CompressFormat.JPEG;
	private int mCompressQuality = 70;
	private static int IO_BUFFER_SIZE = 1024*1024*2;

	public DoubleImageCache(Context ctx, String uniqueName, int appVer, int valueCount, int diskCacheSize, int ramCacheSize) {
		mRamCache = new LruCache<String, Bitmap>(ramCacheSize);
		final File diskCacheDir = getDiskCacheDir(ctx, uniqueName);
		try {
			mDiskCache = DiskLruCache.open(diskCacheDir, appVer, valueCount, diskCacheSize);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Bitmap getBitmap(String url) {
		String key = createKey(url);

		Bitmap bitmap = null;
		bitmap = mRamCache.get(key);
		if (bitmap != null) {
			//Log.v(LOG_TAG, "GetFromMEM"+url);
			return bitmap;
		}

		DiskLruCache.Snapshot snapshot = null;
		try {

			snapshot = mDiskCache.get( key );
			if ( snapshot == null ) {
				return null;
			}
			final InputStream in = snapshot.getInputStream( 0 );
			if ( in != null ) {
				final BufferedInputStream buffIn = 
						new BufferedInputStream( in, IO_BUFFER_SIZE );
				bitmap = BitmapFactory.decodeStream( buffIn );     
				mRamCache.put(key, bitmap);
			}   
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			Log.e(LOG_TAG, "OutOfMemoryError");
		} finally {
			if ( snapshot != null ) {
				snapshot.close();
			}
		}

		//Log.v(LOG_TAG, "GetFromDISK"+url);
		return bitmap;
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		// TODO Auto-generated method stub

		//Log.v(LOG_TAG, url);

		String key = createKey(url);


		DiskLruCache.Editor editor = null;
		try {
			editor = mDiskCache.edit( key );
			if ( editor == null ) {
				return;
			}
			if( writeBitmapToFile( bitmap, editor ) ) {      
				mDiskCache.flush();
				editor.commit();
				//Log.d( "cache_test_DISK_", "image put on disk cache " + key );
			} else {
				editor.abort();
				//Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + key );
			}   
		} catch (IOException e) {
			//Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + key );
			try {
				if ( editor != null ) {
					editor.abort();
				}
			} catch (IOException ignored) {
			}           
		}
		mRamCache.put(key, bitmap);
	}

	private File getDiskCacheDir(Context ctx, String uniqueName) {
		final String cachePath = ctx.getCacheDir().getPath();
		return new File(cachePath + File.separator + uniqueName);
	}

	private String createKey(String url){
		return String.valueOf(url.hashCode());
	}

	private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor )
			throws IOException, FileNotFoundException {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream( editor.newOutputStream( 0 ), IO_BUFFER_SIZE );
			return bitmap.compress( mCompressFormat, mCompressQuality, out );
		} catch (OutOfMemoryError e) {
			Log.e(LOG_TAG, "OutOfMemoryError");
			return false;
		} finally {
			if ( out != null ) {
				out.close();
			}
		}
	}
}
