package ma.laile.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import ma.laile.PostRequest;
import ma.laile.R;
import ma.laile.context.MyApplication;
import ma.laile.views.RoundedImageView;

public class UserActivity extends AppCompatActivity {
    private TextView mActionBarTitle;
    private Button mButtonBack;
    private Button mButtonLoggout;

    private RoundedImageView mImageViewIcon;
    private TextView mTextUsername;
    private TextView mTextUserID;
    private Button mButtonCourse;
    private Button mButtonHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        MyApplication context = (MyApplication)getApplicationContext();

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            bar.setCustomView(R.layout.action_bar_default);
            mButtonBack = bar.getCustomView().findViewById(R.id.button_back);
            mButtonLoggout = bar.getCustomView().findViewById(R.id.button_logout);
            mActionBarTitle = bar.getCustomView().findViewById(R.id.action_bar_title);

            mActionBarTitle.setText("来了吗");
            mButtonBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            mButtonLoggout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences pref = getSharedPreferences("lailema", 0);
                    String token = pref.getString("token", null);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.remove("token");
                    editor.commit();

                    PostRequest request = new PostRequest();
                    request.setOnReceiveDataListener(new PostRequest.OnReceiveDataListener() {
                        @Override
                        public void onReceiveData(String strResult, int StatusCode) {}
                    });

                    List<NameValuePair> p = new ArrayList<NameValuePair>();
                    request.iniRequest(PostRequest.Logout, p, token);
                    request.execute();

                    LoginActivity.isSeen = true;
                    Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }

        //set user information here
        mImageViewIcon = (RoundedImageView)findViewById(R.id.image_icon);
        Bitmap icon = context.getIcon();
        mImageViewIcon.setImageBitmap(icon);

        mTextUsername = (TextView)findViewById(R.id.text_username);
        mTextUserID = (TextView)findViewById(R.id.text_userid);
        mTextUsername.setText(context.getName());
        mTextUserID.setText(context.getUsername());

        mButtonCourse = (Button)findViewById(R.id.button_current_course);
        mButtonHistory = (Button)findViewById(R.id.button_history);
        mButtonCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserActivity.this, AttendActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        mButtonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserActivity.this, HistoryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

    }
}
