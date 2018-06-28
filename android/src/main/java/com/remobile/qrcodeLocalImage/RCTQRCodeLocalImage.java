package com.remobile.qrcodeLocalImage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;


public class RCTQRCodeLocalImage extends ReactContextBaseJavaModule {
    public RCTQRCodeLocalImage(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RCTQRCodeLocalImage";
    }

    @ReactMethod
    public void decode(String path, Callback callback) {
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);

        Bitmap scanBitmap;

        if (path.startsWith("http://")||path.startsWith("https://")) {
            scanBitmap = this.getBitmap(path);
        } else {
            scanBitmap = BitmapFactory.decodeFile(path);
        }

        if (scanBitmap == null) {
            callback.invoke("cannot load image");
            return;
        }

        int[] pixels = new int[scanBitmap.getWidth()*scanBitmap.getHeight()];
        scanBitmap.getPixels(pixels, 0, scanBitmap.getWidth(), 0, 0, scanBitmap.getWidth(), scanBitmap.getHeight());

        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap.getWidth(), scanBitmap.getHeight(), pixels);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            Result result = reader.decode(bitmap, hints);

            if (result == null) {
                callback.invoke("image format error");

            } else {
                callback.invoke(null, result.toString());
            }

        } catch (Exception e) {
            callback.invoke("decode error");
        }
    }

    public static Bitmap getBitmap(String imageUri) {
        Bitmap bitmap = null;
        try {
            URL myFileUrl = new URL(imageUri);
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            bitmap = null;
        } catch (IOException e) {
            e.printStackTrace();
            bitmap = null;
        }
        return bitmap;
    }
}
