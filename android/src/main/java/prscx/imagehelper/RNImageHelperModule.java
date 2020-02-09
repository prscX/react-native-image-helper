
package prscx.imagehelper;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import android.graphics.Bitmap;
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

public class RNImageHelperModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNImageHelperModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNImageHelper";
  }

  @TargetApi(21)
  public Drawable GenerateImage(ReadableMap props) {
    String name = props.getString("name");

    if (name != null && name.length() > 0 && name.contains(".")) {
      Resources resources = getReactApplicationContext().getResources();
      name = name.substring(0, name.lastIndexOf("."));

      final int resourceId = resources.getIdentifier(name, "drawable", getReactApplicationContext().getPackageName());
      return getReactApplicationContext().getDrawable(resourceId);
    } else {
      return this.GenerateVectorIcon(props);
    }
  }

  @TargetApi(21)
  public Drawable GenerateVectorIcon(ReadableMap icon) {
    try {
      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
      StrictMode.setThreadPolicy(policy);

      String family = icon.getString("family");
      String glyph = icon.getString("glyph");
      String color = icon.getString("color");
      int size = icon.getInt("size");

      float scale = getReactApplicationContext().getResources().getDisplayMetrics().density;
      String scaleSuffix = "@" + (scale == (int) scale ? Integer.toString((int) scale) : Float.toString(scale)) + "x";
      int fontSize = Math.round(size * scale);

      Typeface typeface = ReactFontManager.getInstance().getTypeface(family, 0, getReactApplicationContext().getAssets());
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

      return new BitmapDrawable(getReactApplicationContext().getResources(), bitmap);
    } catch (Exception exception) {
      return null;
    }
  }
}