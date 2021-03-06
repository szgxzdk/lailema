package ma.laile.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ma.laile.PostRequest;
import ma.laile.R;
import ma.laile.context.MyApplication;
import ma.laile.face.ImageUtils;
import ma.laile.face.MxNetUtils;

public class AttendActivity extends AppCompatActivity {
    public static final int CAPTURE_PHOTO_CODE = 1;
    public static final double THRESHOLD = 0.65;

    MyApplication context;

    private TextView mTextCourseTimes;
    private TextView mTextAttendedTimes;
    private TextView mTextCourseName;
    private TextView mTextTeacherName;
    private TextView mTextStartTime;
    private TextView mTextEndTime;
    private TextView mTextVenue;
    private Button mButtonAttend;

    private TextView mActionBarTitle;
    private Button mButtonBack;
    private Button mButtonLoggout;

    private View mProgressView;
    private View mInfoFormView;

    private TextView mTextNoCourse;

    private LoadInfoTask mLoadTask;
    private AttendTask mAttendTask;

    private String mLessonId;
    private int mCourseTimes, mTotalCourseTimes, mAttendTimes;
    private String mCourseName, mTeacherName, mStartTimeStr, mEndTimeStr;
    private String mVenueName;
    private boolean mIsAttended;
    private boolean mIsHere = false;

    private String currentPhotoPath;
    private Bitmap oldPhoto;
    private Bitmap newPhoto;
    private static float[] features = null;

    private LocationManager locationManager;

    private double venueLongitude;
    private double venueLatitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

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
                    Intent intent = new Intent(AttendActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }

        context = (MyApplication)getApplicationContext();

        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);

        mTextCourseTimes = (TextView) findViewById(R.id.text_course_times);
        mTextAttendedTimes = (TextView) findViewById(R.id.text_attend_times);
        mTextCourseName = (TextView) findViewById(R.id.text_course_name);
        mTextTeacherName = (TextView) findViewById(R.id.text_teacher_name);
        mTextStartTime = (TextView) findViewById(R.id.text_start_time);
        mTextEndTime = (TextView) findViewById(R.id.text_end_time);
        mTextVenue = (TextView) findViewById(R.id.text_venue);

        mButtonAttend = (Button) findViewById(R.id.button_attend);
        mButtonAttend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonAttend.setEnabled(false);
                mButtonAttend.setText("正在定位...");

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AttendActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(AttendActivity.this, "请打开GPS开关！", Toast.LENGTH_LONG).show();
                    mButtonAttend.setEnabled(true);
                    mButtonAttend.setText("签到");
                } else
                    activateLocation();
            }
        });

        mProgressView = findViewById(R.id.load_info_progress);
        mInfoFormView = findViewById(R.id.info_form);

        mTextNoCourse = findViewById(R.id.text_no_course);

        showProgress(true);

        mLoadTask = new LoadInfoTask();
        mLoadTask.execute((Void) null);

        oldPhoto = context.getIcon();
        Bitmap sFace = ImageUtils.getAlignedFaceFromImage(oldPhoto);
        if (sFace == null) {
            Toast.makeText(this, "获取证件照出错！", Toast.LENGTH_SHORT).show();
            finish();
        }
        features = MxNetUtils.getFeatures(sFace);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(AttendActivity.this, "请授予GPS定位权限！", Toast.LENGTH_LONG).show();
            mButtonAttend.setEnabled(true);
            mButtonAttend.setText("签到");
        } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(AttendActivity.this, "请打开GPS开关！", Toast.LENGTH_LONG).show();
            mButtonAttend.setEnabled(true);
            mButtonAttend.setText("签到");
        } else {
            activateLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void activateLocation() {
        if (!mIsHere) {
            locationManager.requestSingleUpdate(
                    LocationManager.GPS_PROVIDER, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d("mylocation", location.getLongitude() + " " + location.getLatitude());
                            float[] distances = new float[3];
                            Location.distanceBetween(venueLatitude, venueLongitude, location.getLatitude(), location.getLongitude()
                                    , distances);
                            Log.d("mylocation", distances[0] + "");
                            Toast.makeText(AttendActivity.this, "" + location.getLongitude() + "  " + location.getLatitude(),
                                    Toast.LENGTH_LONG).show();
                            if (distances[0] < 150) {
                                mIsHere = true;
                                dispatchTakePictureIntent();
                            } else {
                                Toast.makeText(AttendActivity.this, "你离教室太远了！", Toast.LENGTH_LONG).show();
                                mButtonAttend.setEnabled(true);
                                mButtonAttend.setText("签到");
                            }
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    }, null);
        } else {
            dispatchTakePictureIntent();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mInfoFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mInfoFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mInfoFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mInfoFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showInfo(int courseTimes, int totalCourseTimes, int attendTimes,
                          String courseName, String teacherName, boolean isAttended,
                          String startTimeStr, String endTimeStr, String venueName) {
        mTextCourseTimes.setText("第" + courseTimes + "/" + totalCourseTimes + "次课");
        mTextAttendedTimes.setText("已签到" + attendTimes + "次");
        mTextCourseName.setText(courseName);
        mTextTeacherName.setText(teacherName);
        mTextStartTime.setText(startTimeStr);
        mTextEndTime.setText(endTimeStr);
        mTextVenue.setText(venueName);

        if (isAttended) {
            mButtonAttend.setText("已签到");
            mButtonAttend.setEnabled(false);
        } else
            mButtonAttend.setText("签到");
    }

    public class LoadInfoTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            PostRequest request = new PostRequest();
            request.setOnReceiveDataListener(new PostRequest.OnReceiveDataListener() {
                @Override
                public void onReceiveData(String strResult, int StatusCode) {
                    Log.d("httpDebugCourse", strResult);
                    if (strResult.equals("false")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressView.setVisibility(View.GONE);
                                mInfoFormView.setVisibility(View.GONE);
                                mTextNoCourse.setVisibility(View.VISIBLE);
                                showProgress(false);
                            }
                        });
                    } else {
                        JSONTokener parser = new JSONTokener(strResult);
                        try {
                            JSONObject result = (JSONObject) parser.nextValue();

                            //fill textviews
                            mStartTimeStr = result.getString("startTime");
                            mEndTimeStr = result.getString("endTime");
                            mLessonId = result.getString("id");
                            mTotalCourseTimes = result.getInt("totalTimes");
                            mCourseTimes = result.getInt("curTimes");
                            mAttendTimes = result.getInt("totalAttendence");
                            mCourseName = result.getString("courseName");
                            mTeacherName = result.getString("teacherName");
                            mIsAttended = result.getBoolean("isAttended");

                            // TODO:PositionData
                            JSONObject venue = (JSONObject)result.get("venue");
                            //A1
                            //venueLongitude = 113.40563;
                            //venueLatitude = 23.048126;
                            //B8
                            mVenueName = venue.getString("name");
                            venueLongitude = venue.getDouble("venueLongitude");
                            venueLatitude = venue.getDouble("venueLatitude");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showInfo(mCourseTimes, mTotalCourseTimes, mAttendTimes,
                                        mCourseName, mTeacherName, mIsAttended, mStartTimeStr, mEndTimeStr, mVenueName);
                                showProgress(false);
                            }
                        });
                    }
                }
            });

            request.iniRequest(PostRequest.CurrentLesson, new ArrayList<NameValuePair>(), context.getToken());

            request.execute();

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mLoadTask = null;
        }

        @Override
        protected void onCancelled() {
            mLoadTask = null;
            showProgress(false);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(getApplicationContext(), "ma.laile.fileprovider", photoFile);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    uri = Uri.fromFile(photoFile);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(takePictureIntent, CAPTURE_PHOTO_CODE);
            }
        } else {
            Toast.makeText(this, "开启摄像头失败！", Toast.LENGTH_SHORT).show();
            mButtonAttend.setEnabled(true);
            mButtonAttend.setText("签到");
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                getApplicationContext().getExternalCacheDir()     /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case CAPTURE_PHOTO_CODE:
                if (resultCode == RESULT_OK) {
                    newPhoto = BitmapFactory.decodeFile(currentPhotoPath);
                    if (newPhoto == null) {
                        Toast.makeText(this, "照片打开失败！", Toast.LENGTH_SHORT).show();
                        mButtonAttend.setEnabled(true);
                        mButtonAttend.setText("签到");
                        return;
                    }
                    new FaceTask().execute(newPhoto);
                }
                break;
        }
    }

    private class FaceTask extends AsyncTask<Bitmap, Void, Float> {
        @Override
        protected Float doInBackground(Bitmap... bitmaps) {
            Bitmap sFace = ImageUtils.getAlignedFaceFromImage(bitmaps[0]);
            if (sFace == null) {
                return 0f;
            }
            float[] faceFeatures = MxNetUtils.getFeatures(sFace);
            float s = MxNetUtils.calCosineSimilarity(features, faceFeatures);
            return s;
        }

        @Override
        protected void onPostExecute(Float s) {
            if (s > THRESHOLD) {
                mAttendTask = new AttendTask();
                mAttendTask.execute((Void) null);
                return;
            } else if (s == 0f) {
                Toast.makeText(getApplicationContext(), "没有检测到人脸，请重试！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "认证失败，请重试！", Toast.LENGTH_SHORT).show();
            }
            mButtonAttend.setEnabled(true);
            mButtonAttend.setText("签到");
        }
    }

    private class AttendTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            PostRequest request = new PostRequest();
            request.setOnReceiveDataListener(new PostRequest.OnReceiveDataListener() {
                @Override
                public void onReceiveData(String strResult, int StatusCode) {
                    Log.d("httpDebugAttend", strResult);
                    if (strResult.equals("false")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AttendActivity.this, "签到失败！请检查网络连接",
                                        Toast.LENGTH_LONG).show();
                                mButtonAttend.setEnabled(true);
                                mButtonAttend.setText("签到");
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AttendActivity.this, "签到成功",
                                        Toast.LENGTH_LONG).show();
                                mIsAttended = true;
                                mButtonAttend.setText("已签到");
                                mTextAttendedTimes.setText("已签到" + ++mAttendTimes + "次");
                            }
                        });
                    }
                }
            });

            List<NameValuePair> p = new ArrayList<NameValuePair>();
            p.add(new BasicNameValuePair("lessonId", mLessonId));
            request.iniRequest(PostRequest.Attend, p, context.getToken());

            request.execute();

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mLoadTask = null;
        }

        @Override
        protected void onCancelled() {
            mLoadTask = null;
        }
    }
}
