package ma.laile.context;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;

/**
 * Created by zhao on 2016/11/24.
 */
public class MyApplication extends Application {
    public static Context context;

    public String token;
    public String name;
    public String username;
    public Bitmap icon;

    @Override
    public void onCreate() {
        super.onCreate();
        OpenCVLoader.initDebug();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }
}
