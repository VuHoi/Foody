package com.example.vukhachoi.demo_foody;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import Adapter.MyDatabaseAdapter;
import Model.Restaurant;

public class location_chooser extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_chooser);
        Button btnShow=findViewById(R.id.btnShow);
        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(getResources().getColor(R.color.my_color));
//       getSupportActionBar().hide();
        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Searching();
                finish();
            }
        });

    }

    private void Searching() {
        MyDatabaseAdapter myDatabase;
        SQLiteDatabase database;
        myDatabase= new MyDatabaseAdapter(this);
        myDatabase.Khoitai();
        database=myDatabase.getMyDatabase();

        EditText editText=findViewById(R.id.edtyoudress);
        try {
            if (!editText.getText().toString().equals("")) {
                String selectQuery = " select * from Restaurant where title like  '%" + editText.getText().toString() + "%'";


                ArrayList<Restaurant> arrayList = new ArrayList<>();
                Cursor cursor = database.rawQuery(selectQuery, null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String name = cursor.getString(1);
                    String restype = cursor.getString(7);
                    String address = cursor.getString(4);
                    byte[] hinhanh = cursor.getBlob(3);
                    arrayList.add(new Restaurant(name, address, restype, hinhanh));
                    cursor.moveToNext();
                }
                cursor.close();

                Intent intent = new Intent();
                intent.putExtra("arraylist", arrayList);
                setResult(RESULT_OK, intent);
            }else
            {
                setResult(RESULT_CANCELED);
                finish();
            }
        }catch (Exception e){};

    }
}
