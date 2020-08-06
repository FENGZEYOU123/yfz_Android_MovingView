package com.example.main_movingview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉标题栏
        setContentView(R.layout.activity_main);
        //移动MovingView的代码 在MovingView.java 里

    }
}
