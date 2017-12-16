package ma.laile.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import ma.laile.PostRequest;
import ma.laile.R;
import ma.laile.context.MyApplication;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity /*implements LoaderCallbacks<Cursor>*/ {
    private static final int USERNAME_LENGTH = 12;

    public static boolean isSeen = false;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;

    private Button mUserSignInButton;

    private ActionBar mBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        mBar = getSupportActionBar();
        if (!isSeen) {
            findViewById(R.id.layout_login).setBackgroundResource(R.drawable.lailema);
            findViewById(R.id.email_login_form).setVisibility(View.GONE);
            mBar.hide();
        } else {
            findViewById(R.id.layout_login).setBackground(new ColorDrawable(Color.WHITE));
            findViewById(R.id.email_login_form).setVisibility(View.VISIBLE);
        }

        if (mBar != null) {
            mBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            mBar.setCustomView(R.layout.action_bar_login);
        }

        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mUserSignInButton = (Button) findViewById(R.id.user_sign_in_button);
        mUserSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        if (!isSeen) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    SharedPreferences pref = getSharedPreferences("lailema", 0);
                    final String token = pref.getString("token", null);

                    if (token != null) {
                        PostRequest request = new PostRequest();
                        request.setOnReceiveDataListener(new PostRequest.OnReceiveDataListener() {
                            @Override
                            public void onReceiveData(String strResult, int StatusCode) {
                                Log.d("httpDebugLoginToken", strResult);
                                if (!strResult.equals("false")) {
                                    JSONTokener parser = new JSONTokener(strResult);
                                    try {
                                        JSONObject result = (JSONObject) parser.nextValue();
                                        JSONObject student = (JSONObject) result.get("data");

                                        MyApplication context = (MyApplication) getApplicationContext();
                                        context.setName(student.getString("name"));
                                        context.setUsername(student.getString("username"));

                                        context.setToken(token);

                                        byte[] bytes = Base64.decode(result.getString("icon"), Base64.DEFAULT);
                                        Bitmap icon = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        context.setIcon(icon);

                                        isSeen = false;
                                        Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mBar.show();
                                            findViewById(R.id.layout_login).setBackground(new ColorDrawable(Color.WHITE));
                                            findViewById(R.id.email_login_form).setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            }
                        });

                        List<NameValuePair> p = new ArrayList<NameValuePair>();
                        request.iniRequest(PostRequest.Login, p, token);
                        request.execute();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mBar.show();
                                findViewById(R.id.layout_login).setBackground(new ColorDrawable(Color.WHITE));
                                findViewById(R.id.email_login_form).setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) ||
                !TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mUserSignInButton.setEnabled(false);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }
    private boolean isUsernameValid(String username) {
        return username.matches("\\d{" + USERNAME_LENGTH + "}");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6 && password.length() <= 15;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            PostRequest request = new PostRequest();
            request.setOnReceiveDataListener(new PostRequest.OnReceiveDataListener() {
                @Override
                public void onReceiveData(String strResult, int StatusCode) {
                    Log.d("httpDebugLogin", strResult);
                    if (strResult.equals("false")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                                mUserSignInButton.setEnabled(true);
                            }
                        });
                    } else {
                        JSONTokener parser = new JSONTokener(strResult);
                        try {
                            JSONObject result = (JSONObject) parser.nextValue();
                            JSONObject student = (JSONObject) result.get("data");

                            MyApplication context = (MyApplication)getApplicationContext();
                            context.setName(student.getString("name"));
                            context.setToken(student.getString("token"));
                            context.setUsername(student.getString("username"));

                            SharedPreferences pref = getSharedPreferences("lailema", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("token", student.getString("token"));
                            editor.commit();

                            byte[] bytes = Base64.decode(result.getString("icon"), Base64.DEFAULT);
                            Bitmap icon = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            context.setIcon(icon);

                            isSeen = false;
                            Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            List<NameValuePair> p = new ArrayList<NameValuePair>();
            p.add(new BasicNameValuePair("username", mUsername));
            p.add(new BasicNameValuePair("password", mPassword));
            request.iniRequest(PostRequest.Login, p);

            request.execute();

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            mUserSignInButton.setEnabled(true);
        }
    }
}

