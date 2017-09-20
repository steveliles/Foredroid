package com.sjl.foreground.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.sjl.foreground.RxForeground;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;

public abstract class DemoActivity extends AppCompatActivity implements RxForeground.Listener {
  private DisposableCompletableObserver completableObserver;
  private DisposableCompletableObserver disposable;

  CompositeDisposable disposables = new CompositeDisposable();
  private RxForeground.Binding listenerBinding;

  protected abstract void startAnotherActivity();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button button = (Button) findViewById(R.id.button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startAnotherActivity();
      }
    });

    listenerBinding = RxForeground.get(getApplication()).addListener(this);
    RxForeground.init(getApplication());

    disposable =
        RxForeground.get().backgroundObservable().subscribeWith(new DisposableCompletableObserver() {
          @Override
          public void onComplete() {
            Log.i(RxForeground.TAG, getClass().getName() + " became background - from Completable");
          }

          @Override
          public void onError(@NonNull Throwable e) {

          }
        });

    completableObserver =
        RxForeground.get().foregroundObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableCompletableObserver() {
          @Override
          public void onComplete() {
            Log.i(RxForeground.TAG, getClass().getName() + " became foreground - from Completable");
          }

          @Override
          public void onError(@NonNull Throwable e) {

          }
        });

    disposables.add(completableObserver);
    disposables.add(disposable);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    disposables.clear();
    // not strictly necessary as Foreground only holds a weak reference
    // to the listener to defensively prevent leaks, but its always better
    // to be explicit and WR's play monkey with the Garbage Collector
    listenerBinding.unbind();
  }

  //@Override
  //public void onBecameForeground() {
  //    Log.i(Foreground.TAG, getClass().getName() + " became foreground");
  //}
  //
  //@Override
  //public void onBecameBackground() {
  //    Log.i(Foreground.TAG, getClass().getName() + " went background");
  //}
}
