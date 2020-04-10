package com.example.webview;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class newssite extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newssite);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.timeswhite);;

        Intent intent = getIntent();
        int position = intent.getIntExtra("newsURL", -1);
        if (position != -1) {
            String url = MainActivity.arrayList.get(position).getNewsUrl();

            WebView webView = findViewById(R.id.webView2);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(url);
            Log.i("news url", url);
        } else {
            Toast.makeText(getApplicationContext(), "Unable to connect", Toast.LENGTH_LONG).show();
            Log.i("Error", "Unable to connect");
        }
    }
}

