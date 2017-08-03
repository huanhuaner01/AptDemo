package com.study.huanzhang.aptdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.huan.BindView;
import com.study.huanzhang.viewlib.ViewUtil;

public class MainActivity extends AppCompatActivity {
   @BindView(R.id.btn)
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtil.bind(this);
        btn.setText("huanhuan");
    }
}
