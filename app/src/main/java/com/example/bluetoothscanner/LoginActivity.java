package com.example.bluetoothscanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailedit,passedit;
    private Button btnlogin,btnregis;
    private FirebaseAuth mAuth;

    CheckBox remember;
    private SharedPreferences mPrefs;
    private static final String PREFS_NAME =  "PrefsFile";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        emailedit = findViewById(R.id.email);
        passedit = findViewById(R.id.password);
        btnlogin = findViewById(R.id.btnlogin);
        btnregis = findViewById(R.id.btnregis);
        remember = findViewById(R.id.rememberMe);

        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String checkbox = preferences.getString("remember","");
//        getPreferencesData();




        if (checkbox.equals("true")){
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
        }else if(checkbox.equals("false")){
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
        }



        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
                if (remember.isChecked()){
                    Boolean boolIschecked = remember.isChecked();
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("pref_name",emailedit.getText().toString());
                    editor.putString("pref_pass",passedit.getText().toString());
                    editor.putBoolean("pref_check",boolIschecked);
                    editor.apply();

                    //Toast.makeText(getApplicationContext(), "have saved", Toast.LENGTH_SHORT).show();
                }else {
                    mPrefs.edit().clear().apply();
                }
            }
        });
        btnregis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()){
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember","true");
                    editor.apply();
                    Toast.makeText(LoginActivity.this, "Đã lưu", Toast.LENGTH_SHORT).show();
                }else if (!buttonView.isChecked()){
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember","false");
                    editor.apply();
                    //Toast.makeText(LoginActivity.this, "unChecked", Toast.LENGTH_SHORT).show();
                }


            }
        });






    }

//    private void getPreferencesData() {
//        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        if (sp.contains("pref_name")){
//            String u = sp.getString("pref_name","not found");
//            emailedit.setText(u.toString());
//
//        }
//        if (sp.contains("pref_pass")){
//            String p = sp.getString("pref_pass","not found");
//                passedit.setText(p.toString());
//        }
//        if (sp.contains("pref_check")){
//            Boolean b = sp.getBoolean("pref_check",false);
//                remember.setChecked(b);
//        }
//    }

    private void register() {
        Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(i);
    }

    private void login() {
        String email,pass;
        email = emailedit.getText().toString();
        pass = passedit.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Vui lòng nhập email.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Vui lòng nhập mật khẩu.", Toast.LENGTH_SHORT).show();
            return;
        }
        // hien da nhap login
        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);

//                        emailedit.getText().clear();
//                        passedit.getText().clear();
                    return;
                }else{
                    Toast.makeText(getApplicationContext(), "Tài khoản hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}