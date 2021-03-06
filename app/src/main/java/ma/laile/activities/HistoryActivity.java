package ma.laile.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

import ma.laile.PostRequest;
import ma.laile.R;

public class HistoryActivity extends AppCompatActivity {
    private TextView mActionBarTitle;
    private Button mButtonBack;
    private Button mButtonLoggout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            bar.setCustomView(R.layout.action_bar_default);
            mButtonBack = bar.getCustomView().findViewById(R.id.button_back);
            mButtonLoggout = bar.getCustomView().findViewById(R.id.button_logout);
            mActionBarTitle = bar.getCustomView().findViewById(R.id.action_bar_title);

            mActionBarTitle.setText("当前课程");
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
                    Intent intent = new Intent(HistoryActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }
    }
}
