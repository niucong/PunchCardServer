package com.niucong.punchcardserver;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.niucong.punchcardserver.databinding.ActivityMainBinding;
import com.niucong.punchcardserver.net.ServerListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
//        setContentView(R.layout.activity_main);
//        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.setHandlers(new MainClickHandlers());

    }

    public class MainClickHandlers {
        public void onClickName(View v) {
            Log.d("MainActivity", "MainClickHandlers");
            switch (v.getId()) {
                case R.id.main_sign:
//                    BlueToothUtils utils = new BlueToothUtils();
//                    utils.setContext(MainActivity.this);
//                    utils.getAc().run();

                    new ServerListener().start();

                    break;
                case R.id.main_member:
                    startActivity(new Intent(MainActivity.this, MemberListActivity.class));
                    break;
            }
        }
    }

}
