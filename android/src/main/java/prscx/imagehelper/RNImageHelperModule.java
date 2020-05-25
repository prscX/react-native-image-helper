
package prscx.imagehelper;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Color;
import android.graphics.Paint;
import com.facebook.react.views.text.ReactFontManager;
import android.graphics.Typeface;
import android.os.StrictMode;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import android.graphics.drawable.Drawable;
import android.annotation.TargetApi;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RNImageHelperModule extends ReactContextBaseJavaModule {

  private static ReactApplicationContext reactContext;

  public RNImageHelperModule(ReactApplicationContext reactContext) {
    super(reactContext);
    RNImageHelperModule.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNImageHelper";
  }

  @TargetApi(21)
  public static Drawable GenerateImage(ReadableMap props) {
    String name = props.getString("name");
    String url = null;

    if (props.hasKey("url")) {
      try {
        ReadableMap iconUrl = props.getMap("url");
        if (iconUrl.hasKey("uri")) {
          url = iconUrl.getString("uri");
        }
      } catch (Exception e) { }
    }

    if (url != null && url.length() > 0) {
      try {
        Bitmap bitmap = drawableFromUrl(url);
        return new BitmapDrawable(RNImageHelperModule.reactContext.getResources(), bitmap);
      } catch(Exception e) {
        Log.e("RNImageHelper", e.getMessage());
      }

      return null;
    } else if (name != null && name.length() > 0 && name.contains(".")) {
      Resources resources = RNImageHelperModule.reactContext.getResources();
      name = name.substring(0, name.lastIndexOf("."));

      final int resourceId = resources.getIdentifier(name, "drawable", RNImageHelperModule.reactContext.getPackageName());
      return RNImageHelperModule.reactContext.getDrawable(resourceId);
    } else {
      return RNImageHelperModule.GenerateVectorIcon(props);
    }
  }

  private static Bitmap drawableFromUrl(String url) throws IOException {
    Bitmap bitmap;
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestProperty("User-agent","Mozilla/4.0");

    connection.connect();
    InputStream input = connection.getInputStream();

    bitmap = BitmapFactory.decodeStream(input);

    return getCircularBitmap(bitmap);
  }


  private static Bitmap getCircularBitmap(Bitmap srcBitmap) {
    int squareBitmapWidth = Math.min(srcBitmap.getWidth(), srcBitmap.getHeight());

    Bitmap dstBitmap = Bitmap.createBitmap(
            squareBitmapWidth, // Width
            squareBitmapWidth, // Height
            Bitmap.Config.ARGB_8888 // Config
    );

    Canvas canvas = new Canvas(dstBitmap);

    Paint paint = new Paint();
    paint.setAntiAlias(true);

    Rect rect = new Rect(0, 0, squareBitmapWidth, squareBitmapWidth);
    RectF rectF = new RectF(rect);
    canvas.drawOval(rectF, paint);
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    float left = (squareBitmapWidth-srcBitmap.getWidth())/2;
    float top = (squareBitmapWidth-srcBitmap.getHeight())/2;
    canvas.drawBitmap(srcBitmap, left, top, paint);

    srcBitmap.recycle();

    return dstBitmap;
  }

  @TargetApi(21)
  public static Drawable GenerateVectorIcon(ReadableMap icon) {
    try {
      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
      StrictMode.setThreadPolicy(policy);

      String family = icon.getString("family");
      String glyph = icon.getString("glyph");
      String color = icon.getString("color");
      int size = icon.getInt("size");

      float scale = RNImageHelperModule.reactContext.getResources().getDisplayMetrics().density;
      String scaleSuffix = "@" + (scale == (int) scale ? Integer.toString((int) scale) : Float.toString(scale)) + "x";
      int fontSize = Math.round(size * scale);

      Typeface typeface = ReactFontManager.getInstance().getTypeface(family, 0, RNImageHelperModule.reactContext.getAssets());
      Paint paint = new Paint();
      paint.setTypeface(typeface);

      if (color != null && color.length() == 4) {
        color = color + color.substring(1);
      }

      if (color != null && color.length() > 0) {
        paint.setColor(Color.parseColor(color));
      }

      paint.setTextSize(fontSize);
      paint.setAntiAlias(true);
      Rect textBounds = new Rect();
      paint.getTextBounds(glyph, 0, glyph.length(), textBounds);

      Bitmap bitmap = Bitmap.createBitmap(textBounds.width(), textBounds.height(), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);
      canvas.drawText(glyph, -textBounds.left, -textBounds.top, paint);

      return new BitmapDrawable(RNImageHelperModule.reactContext.getResources(), bitmap);
    } catch (Exception exception) {
      return null;
    }
  }
}