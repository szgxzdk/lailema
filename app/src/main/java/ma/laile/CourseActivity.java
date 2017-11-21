package ma.laile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CourseActivity extends AppCompatActivity {
    private final int UPDATE_TEXTS = 1;

    private TextView mTextCourseTimes;
    private TextView mTextAttendedTimes;
    private TextView mTextCourseName;
    private TextView mTextTeacherName;
    private TextView mTextStartTime;
    private TextView mTextEndTime;
    private Button mButtonAttend;

    private TextView mActionBarTitle;
    private Button mButtonBack;
    private Button mButtonLoggout;

    private View mProgressView;
    private View mInfoFormView;

    private TextView mTextNoCourse;

    private LoadInfoTask mLoadTask;
    private AttendTask mAttendTask;

    private String mCourseID;
    private int mCourseTimes, mTotalCourseTimes, mAttendTimes;
    private String mCourseName, mTeacherName, mStartTimeStr, mEndTimeStr;
    private boolean mIsAttended;

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
                    Intent intent = new Intent(CourseActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        mTextCourseTimes = (TextView)findViewById(R.id.text_course_times);
        mTextAttendedTimes = (TextView)findViewById(R.id.text_attend_times);
        mTextCourseName = (TextView)findViewById(R.id.text_course_name);
        mTextTeacherName = (TextView)findViewById(R.id.text_teacher_name);
        mTextStartTime = (TextView)findViewById(R.id.text_start_time);
        mTextEndTime = (TextView)findViewById(R.id.text_end_time);

        mButtonAttend = (Button)findViewById(R.id.button_attend);
        mButtonAttend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonAttend.setEnabled(false);
                mAttendTask = new AttendTask();
                mAttendTask.execute((Void) null);
            }
        });

        mProgressView = findViewById(R.id.load_info_progress);
        mInfoFormView = findViewById(R.id.info_form);

        mTextNoCourse = findViewById(R.id.text_no_course);

        showProgress(true);

        mLoadTask = new LoadInfoTask();
        mLoadTask.execute((Void) null);

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
                          String startTimeStr, String endTimeStr) {
        mTextCourseTimes.setText("第" + courseTimes + "/" + totalCourseTimes + "次课");
        mTextAttendedTimes.setText("已签到" + attendTimes + "次");
        mTextCourseName.setText(courseName);
        mTextTeacherName.setText(teacherName);
        mTextStartTime.setText(startTimeStr);
        mTextEndTime.setText(endTimeStr);

        if (isAttended) {
            mButtonAttend.setText("已签到");
            mButtonAttend.setEnabled(false);
        } else
            mButtonAttend.setText("签到");
    }

    public class LoadInfoTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: load information against a network service.
            //fill textviews
            String startTimeStr = "2017/11/10 10:40", endTimeStr = "2017/11/10 12:20";
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm");

            Date startTime;
            Date endTime;
            try {
                startTime = format.parse(startTimeStr);
                endTime = format.parse(endTimeStr);

                mCourseID = "000001";
                mTotalCourseTimes = 16;
                mCourseTimes = 9;
                mAttendTimes = 8;
                mCourseName = "IT项目管理";
                mTeacherName = "陈泽琳";
                mIsAttended = false;
                mStartTimeStr = format.format(startTime);
                mEndTimeStr = format.format(endTime);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            // TODO: assign the variables here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mLoadTask = null;

            if (success) {
                if (mCourseID == null || mCourseID.isEmpty()) {
                    mProgressView.setVisibility(View.GONE);
                    mInfoFormView.setVisibility(View.GONE);
                    mTextNoCourse.setVisibility(View.VISIBLE);
                } else {
                    showInfo(mCourseTimes, mTotalCourseTimes, mAttendTimes,
                            mCourseName, mTeacherName, mIsAttended, mStartTimeStr, mEndTimeStr);
                }
            } else {
                Toast.makeText(CourseActivity.this, "获取课程信息失败！请检查网络连接",
                        Toast.LENGTH_LONG).show();
            }

            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mLoadTask = null;
            showProgress(false);
        }
    }

    public class AttendTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attend against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            // TODO: assign the variables here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mLoadTask = null;

            if (success) {
                Toast.makeText(CourseActivity.this, "签到成功",
                        Toast.LENGTH_LONG).show();
                mIsAttended = true;
                mButtonAttend.setText("已签到");
                mTextAttendedTimes.setText("已签到" + ++mAttendTimes + "次");
            } else {
                Toast.makeText(CourseActivity.this, "签到失败！请检查网络连接",
                        Toast.LENGTH_LONG).show();
            }

            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mLoadTask = null;
            showProgress(false);
        }
    }
}
