package com.uber.okbuck.example;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.github.piasy.rxscreenshotdetector.RxScreenshotDetector;
import com.promegu.xlog.base.XLog;
import com.uber.okbuck.example.common.Calc;
import com.uber.okbuck.example.common.CalcMonitor;
import com.uber.okbuck.example.common.IMyAidlInterface;
import com.uber.okbuck.example.dummylibrary.DummyActivity;
import com.uber.okbuck.example.dummylibrary.DummyAndroidClass;
import com.uber.okbuck.example.javalib.DummyJavaClass;
import com.uber.okbuck.java.Pojo;
import com.uber.okbuck.kotlin.KotlinDataClass;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;
import androidx.annotation.Nullable;

@XLog
public class MainActivity extends AppCompatActivity {
  @Inject @Nullable DummyJavaClass mDummyJavaClass;
  @Inject DummyAndroidClass mDummyAndroidClass;
  @Nullable IMyAidlInterface mIMyAidlInterface;

  @Nullable private Unbinder mUnbinder;

  @BindView(R2.id.mTextView)
  @Nullable TextView mTextView;

  @BindView(R2.id.mTextView2)
  TextView mTextView2;

  private ServiceConnection mServiceConnection =
      new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, @Nullable IBinder service) {
          mIMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(@Nullable ComponentName name) {
          mIMyAidlInterface = null;
        }
      };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mUnbinder = ButterKnife.bind(this);

    View view = findViewById(android.R.id.content);
    if (view != null) {
      view.setOnClickListener(v -> Log.d("TAG", "Hello, lambda! My view is: " + v));
    }

    DummyComponent component = DaggerDummyComponent.builder().build();
    component.inject(this);

    mTextView.setText(
        String.format(
            "%s %s, --from %s.",
            getString(com.uber.okbuck.example.dummylibrary.R.string.dummy_library_android_str),
            mDummyAndroidClass.getAndroidWord(this),
            mDummyJavaClass.getJavaWord()));

    if (BuildConfig.CAN_JUMP) {
      mTextView.setOnClickListener(
          v -> startActivity(new Intent(MainActivity.this, DummyActivity.class)));
    }

    Log.d("test", "1 + 2 = " + new Calc(new CalcMonitor(this)).add(1, 2));

    RxScreenshotDetector.start(this)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            new Observer<String>() {
              @Override
              public void onComplete() {}

              @Override
              public void onError(Throwable e) {
                e.printStackTrace();
              }

              @Override
              public void onNext(@Nullable String path) {
                mTextView.setText(mTextView.getText() + "\nScreenshot: " + path);
              }

              @Override
              public void onSubscribe(Disposable disposable) {}
            });

    KotlinDataClass data = new KotlinDataClass("foo", com.uber.okbuck.kotlin.android.R.string.foo);
    Pojo pojo = new Pojo();
  }

  @Override
  protected void onDestroy() {
    mUnbinder.unbind();
    super.onDestroy();
  }
}
