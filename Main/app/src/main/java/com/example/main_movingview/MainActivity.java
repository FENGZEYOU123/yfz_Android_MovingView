package com.example.main_movingview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private yfz_ios_spring_listview listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉标题栏
        setContentView(R.layout.activity_main);
        //移动MovingView的代码 在MovingView.java 里

        listview = (yfz_ios_spring_listview) findViewById(R.id.mylistview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, new String[] { "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A"});
        listview.setAdapter(adapter);
//
//        yfz_ios_Moving_LinearLayout aaaaa= findViewById(R.id.aaaaa);
//        TextView vv= new TextView(this);
//        vv.setWidth(10);
//        vv.setHeight(10);
//
//        vv.setText("adasdasdas");
//        aaaaa.addView(vv);
//        vv.measure(10,10);



    }
}
