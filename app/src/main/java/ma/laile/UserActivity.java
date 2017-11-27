package ma.laile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
                    Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }

        //set user information here
        mImageViewIcon = (RoundedImageView)findViewById(R.id.image_icon);
        Bitmap icon = Bitmap.createBitmap(240, 240, Bitmap.Config.ARGB_8888);
        icon.eraseColor(Color.parseColor("#FFFFFF"));
        mImageViewIcon.setImageBitmap(icon);

        mTextUsername = (TextView)findViewById(R.id.text_username);
        mTextUserID = (TextView)findViewById(R.id.text_userid);
        mTextUsername.setText("肖思源");
        mTextUserID.setText("201721045886");

        mButtonCourse = (Button)findViewById(R.id.button_current_course);
        mButtonHistory = (Button)findViewById(R.id.button_history);
        mButtonCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserActivity.this, CourseActivity.class);
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
