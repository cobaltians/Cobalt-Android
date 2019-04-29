package org.cobaltians.cobalt.tools;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Image implements Parcelable {

    /***********************************************************************************************
     *
     * MEMBERS
     *
     **********************************************************************************************/

    private static final String TAG = Image.class.getSimpleName();

    private final Uri mUri;
    private final String mPath;

    /***********************************************************************************************
     *
     * CONSTRUCTORS
     *
     **********************************************************************************************/

    Image(@NonNull Uri uri, @NonNull String path) {
        mUri = uri;
        mPath = path;
    }
    public Image(@NonNull Uri uri) {
        mUri = uri;
        mPath = uri.getPath();
    }
    public Image(File directory, String extension, Context context) {
        Date now = new Date();
        String filename = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now) + "-" + now.getTime();
        File file = new File("");
        try {
            // / Make sure the Pictures directory exists.
            file = File.createTempFile(filename,    /* prefix */
                    extension,                      /* suffix */
                    directory);                     /* directory */
        }
        catch (IOException exception) {
            Log.e(TAG, "createImageFile: " + directory.getAbsolutePath() + "/" + filename
                    + extension + " could not be created.", exception);
        }
        catch (SecurityException exception) {
            Log.e(TAG, "createImageFile: " + directory.getAbsolutePath() + "/" + filename
                    + extension + " could not be created.", exception);
        }

        mUri = FileProvider.getUriForFile(context,context.getPackageName() + ".files.provider", file);
        mPath = file.getAbsolutePath();
    }

    /***********************************************************************************************
     *
     * PARCELABLE
     *
     **********************************************************************************************/

    public static Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    private Image(Parcel in) {
        mUri = in.readParcelable(Uri.class.getClassLoader());
        mPath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mUri, flags);
        dest.writeString(mPath);
    }

    /***********************************************************************************************
     *
     * GETTERS
     *
     **********************************************************************************************/

    public @NonNull Uri getUri() {
        return mUri;
    }

    public @NonNull String getPath() {
        return mPath;
    }

    public @NonNull String getUrl() {
        return "file://" + mPath;
    }

    @Override
    public String toString() {
        return "Image{uri: " + mUri + ", path: " + mPath + "}";
    }

    /***********************************************************************************************
     *
     * METHODS
     *
     **********************************************************************************************/

    private static final int NUMBER_OF_RESIZE_ATTEMPTS = 4;

    /**
     * Returns the number of degrees to rotate the picture,
     * based on the orientation column in the database.
     * If there's no tag or column, 0 degrees is returned.
     *
     * Inspired from https://android.googlesource.com/platform/packages/apps/Mms/+/master/src/com/android/mms/ui/UriImage.java
     *
     * @param context Used to get the ContentResolver
     */
    public int geRotationDegrees(@NonNull Context context) {
        // Try to get the orientation from the ORIENTATION column in the database.
        // This is much faster than reading all the exif tags from the file.
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(mUri,       // Table to query
                    new String[] {MediaStore.Images.Media.ORIENTATION}, // Projection to return
                    null,                                       // No selection clause
                    null,                                   // No selection arguments
                    null);                                     // No order
            if (cursor != null
                && cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));
            }
        }
        catch (IllegalArgumentException argumentException) {
            Log.w(TAG, "getOrientation: error occurred  while requesting MediaStore.Images. "
                    + "See Stacktrace below: ", argumentException);

            try {
                ExifInterface exif = new ExifInterface(getPath());
                return exif.getRotationDegrees();
            }
            catch (IOException ioException) {
                Log.e(TAG, "getOrientation: error occurred while requesting Exif for path "
                        + getPath() + ".", ioException);
            }
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    public @Nullable Bitmap getBitmap(Context context, int requiredSize) {
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(mUri);

            int inSampleSize = 1;

            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);

            // Find the correct scale value (should be a power of 2)
            float scale = Math.min(options.outWidth / requiredSize, options.outHeight / requiredSize);
            if (scale > 1.0) {
                inSampleSize = (int) Math.pow(2,  Math.floor(Math.log(scale) / Math.log(2))) / 2;
            }

            int attempts = 1;
            options = new BitmapFactory.Options();
            do {
                inputStream = context.getContentResolver().openInputStream(mUri);
                options.inSampleSize = inSampleSize;
                try {
                    bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                    if (bitmap == null) {
                        return null; // Couldn't decode and it wasn't because of an exception, bail.
                    }
                }
                catch (OutOfMemoryError exception) {
                    Log.w(TAG, "getBitmap: bitmap too large to decode (OutOfMemoryError), "
                            + "may try with larger sampleSize (current: " + inSampleSize + ").");
                    inSampleSize *= 2;    // works best as a power of two
                    attempts++;
                }
                finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }
                        catch (IOException exception) {
                            Log.i(TAG, "getBitmap: I/O error while closing input stream.", exception);
                        }
                    }
                }
            } while (bitmap == null
                    && attempts < NUMBER_OF_RESIZE_ATTEMPTS);

            if (bitmap == null
                    && attempts >= NUMBER_OF_RESIZE_ATTEMPTS) {
                Log.v(TAG, "getBitmap: gave up after too many attempts to resize");
            }
        }
        catch (FileNotFoundException exception) {
            Log.e(TAG, "getBitmap: file not found at uri " + mUri + "(path = " + mPath + ")",
                    exception);
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException exception) {
                    Log.i(TAG, "getBitmap: I/O error while closing input stream.", exception);
                }
            }
        }

        if (bitmap != null) {
            bitmap = rotateBitmap(bitmap, geRotationDegrees(context));
        }

        return bitmap;
    }

    private static final int BYTE_LIMIT = 2097152;  // 2MB
    private static final int QUALITY = 100;

    public @Nullable String toBase64(Context context, int requestedSize) {
        String base64 = "";

        Bitmap bitmap = getBitmap(context, requestedSize);
        if (bitmap != null) {
            ByteArrayOutputStream baos = null;
            int attempts = 1;
            int quality = QUALITY;
            int base64Length = BYTE_LIMIT;
            // Need this loop cause with some new device the max length of url is 2M characters (2097152 bytes)
            // In this loop, we attempt to compress/resize the content to fit this limit.
            do {
                quality = quality * BYTE_LIMIT / base64Length;  // watch for int division!
                Log.v(TAG, "Base64ImageAtPath: compress(2) w/ quality=" + quality);

                try {
                    // Compress the image into a JPG. Start with QUALITY.
                    // In case that the image byte size is still too large
                    // reduce the quality in proportion to the desired byte size.
                    if (baos != null) {
                        try {
                            baos.close();
                        }
                        catch (IOException exception) {
                            Log.e(TAG, exception.getMessage(), exception);
                        }
                    }
                    baos = new ByteArrayOutputStream();
                    // compressing the image
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                    // encode image
                    base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
                    base64Length = base64.length();
                }
                catch (OutOfMemoryError exception) {
                    Log.w(TAG, "Base64ImageAtPath - image too big (OutOfMemoryError), "
                            + "will try with smaller quality, cur quality: " + quality);
                    // fall through and keep trying with a smaller quality.
                }
                Log.v(TAG, "attempt=" + attempts
                        + " size=" + base64Length
                        + " quality=" + quality);
                attempts++;
            } while (base64Length > BYTE_LIMIT
                    && attempts < NUMBER_OF_RESIZE_ATTEMPTS);

            if (baos != null) {
                try {
                    baos.close();
                }
                catch (IOException exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                }
            }
        }

        return base64;
    }

    /**
     * Bitmap rotation method
     *
     * Copied from https://android.googlesource.com/platform/packages/apps/Mms/+/master/src/com/android/mms/ui/UriImage.java
     *
     * @param bitmap The input bitmap
     * @param degrees The rotation angle
     */
    private @NonNull Bitmap rotateBitmap(@NonNull Bitmap bitmap, int degrees) {
        if (degrees != 0) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            Matrix matrix = new Matrix();
            matrix.setRotate(degrees, (float) width / 2, (float) height / 2);

            try {
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                        matrix, true);
                bitmap.recycle();
                bitmap = rotatedBitmap;
            }
            catch (OutOfMemoryError exception) {
                Log.w(TAG, "rotateBitmap: OOM", exception);
                // We have no memory to rotate. Return the original bitmap.
            }
        }

        return bitmap;
    }

    //Save a Bitmap to JPG at defined path with compressRate
    public void saveBmp(Bitmap bitmap, int compressRate, int requiredSize){
        try {
            // Resizing to requiredSize
            int resizedWidth = bitmap.getWidth();
            int resizedHeight = bitmap.getHeight();
            int longestDimension = Math.max(resizedWidth, resizedHeight);
            if (longestDimension > requiredSize)
            {
                float ratio = (float) longestDimension / (float) requiredSize;
                resizedWidth = (int)Math.floor(resizedWidth / ratio);
                resizedHeight = (int)Math.floor(resizedHeight / ratio);
                bitmap = Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true);
            }

            // Save to JPG
            FileOutputStream mFileOutStream = new FileOutputStream(mPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressRate, mFileOutStream);
            mFileOutStream.flush();
            mFileOutStream.close();
        } catch (IOException exception) {
            Log.e(TAG, "toJpg: cannot save bitmap at " + mPath, exception);
        }
    }
}
