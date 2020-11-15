package com.niucong.punchcardserver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.niucong.punchcardserver.db.SignDB;

import java.util.ArrayList;
import java.util.List;

public class SignStatisticsActivity extends AppCompatActivity {

    private List<SignDB> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_statistics);

    }

}
