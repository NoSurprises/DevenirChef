package antitelegram.devenirchef.utils;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;

public class PhotoRedactor {

    public Bitmap getScaledPhoto(Bitmap image) {
        int width = 1080;
        int height = 1080;
        return Bitmap.createScaledBitmap(image, width, height, false);
    }

    public Bitmap getCroppedPhoto(Bitmap image) {

        if (image.getWidth() >= image.getHeight()) {
            return Bitmap.createBitmap(
                    image,
                    image.getWidth() / 2 - image.getHeight() / 2,
                    0,
                    image.getHeight(),
                    image.getHeight()
            );
        }
        return Bitmap.createBitmap(
                image,
                0,
                image.getHeight() / 2 - image.getWidth() / 2,
                image.getWidth(),
                image.getWidth()
        );
    }


    public Bitmap getRotatedPhoto(InputStream in, Bitmap image) {

        try {
            int rotation = getImageRotation(new ExifInterface(in));
            return getRotatedBitmap(image, rotation);
        } catch (IOException e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    private Bitmap getRotatedBitmap(Bitmap image, int rotation) {
        Matrix matrix = new Matrix();
        matrix.setRotate(rotation, (float) image.getWidth() / 2, (float) image.getHeight() / 2);
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }

    private int getImageRotation(ExifInterface exifInterface) {
        int rotation = 0;
        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
        }
        return rotation;
    }
}
