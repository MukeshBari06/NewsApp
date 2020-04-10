package com.example.webview;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    SQLiteDatabase  articlesDataBase;
    static ArrayList<News> arrayList;
    CustomAdapter customAdapter;
    ListView listView;


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


    public static class News {

        private String title;
        private String description;
        private String newsUrl ;
        private String image ;
        private  String publishedAt;

        //constructor
        public News(String title,String description,String newsUrl,String image,String publishedAt){

            this.title = title;
            this.description = description;
            this.newsUrl = newsUrl;
            this.image = image;
            this.publishedAt = publishedAt;
        }

        //getters
        public String getTitle() {return title; }
        public String getDescription() {return description; }
        public String getNewsUrl() {return newsUrl; }
        public String getImage() { return image; }
        public String getPublishedAt() { return publishedAt; }
    }


    public class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView=getLayoutInflater().inflate(R.layout.sample_layout,null);
            ImageView image=(ImageView)convertView.findViewById(R.id.image);
            TextView title=(TextView)convertView.findViewById(R.id.title);
            TextView description=(TextView)convertView.findViewById(R.id.description);
            TextView publishedAt=(TextView)convertView.findViewById(R.id.publishedAt);


            //set address and description
            title.setText(arrayList.get(position).getTitle());

            //display trimmed excerpt for description
            int descriptionLength = arrayList.get(position).getDescription().length();
            if(descriptionLength >= 100){
                String descriptionTrim = arrayList.get(position).getDescription().substring(0, 100) + "...";
                description.setText(descriptionTrim);
            }else{
                description.setText(arrayList.get(position).getDescription());
            }

            //set publishedAt
            publishedAt.setText(arrayList.get(position).getPublishedAt());

            //get the image associated with this property
            DownloadImageTask task=new DownloadImageTask(image);
            try{
                task.execute(arrayList.get(position).getImage());
            }catch (Exception e){
                e.printStackTrace();
            }

            return convertView;        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.timeswhite);

         articlesDataBase=this.openOrCreateDatabase("Articles",MODE_PRIVATE,null);
         articlesDataBase.execSQL("CREATE TABLE IF NOT EXISTS articles(id INTEGER PRIMARY KEY, articletitle VARCHAR, articledescription VARCHAR," +
                "articleurl VARCHAR, imageurllink VARCHAR, publishdate VARCHAR)");

        listView=findViewById(R.id.listView);
        arrayList=new ArrayList<>();

        DownloadTask task=new DownloadTask();
        try{
           task.execute("http://newsapi.org/v2/top-headlines?sources=google-news-in&apiKey=034fd3e824a6486588b8e289d3f6429e");
        }catch (Exception e){
            e.printStackTrace();
        }


        customAdapter=new CustomAdapter();
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent=new Intent(getApplicationContext(),newssite.class);
                intent.putExtra("newsURL",position);
                startActivity(intent);

            }
        });
        updateListView();


    }

    public void updateListView() {
        Cursor c =  articlesDataBase.rawQuery("SELECT * FROM articles", null);
        int titleIndex = c.getColumnIndex("articletitle");
        int descriptionIndex = c.getColumnIndex("articledescription");
        int newsURLIndex = c.getColumnIndex("articleurl");
        int imgURLIndex = c.getColumnIndex("imageurllink");
        int publishDateIndex = c.getColumnIndex("publishdate");

        if (c.moveToFirst()){
            arrayList.clear();
            do{
               arrayList.add(new News(c.getString(titleIndex),c.getString(descriptionIndex),c.getString(newsURLIndex),
                       c.getString(imgURLIndex),c.getString(publishDateIndex)));
            } while (c.moveToNext());
            customAdapter.notifyDataSetChanged();
        }
    }


    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... URLs) {
            String result="";
            URL url;
            HttpURLConnection httpURLConnection=null;
            try {
                url=new URL(URLs[0]);
                httpURLConnection=(HttpURLConnection)url.openConnection();
                InputStream inputStream=httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader=new InputStreamReader(inputStream);

                int data=inputStreamReader.read();
                while (data!=-1){
                    char current=(char)data;
                    result+=current;
                    data=inputStreamReader.read();
                }

                try {
                    JSONObject jsonObject = new JSONObject(result);

                    String newsArticles = jsonObject.getString("articles");
                    JSONArray newsList = new JSONArray(newsArticles);
                     articlesDataBase.execSQL("DELETE FROM articles");
                    for (int i = 0; i < newsList.length(); i++) {
                        JSONObject news = newsList.getJSONObject(i);
                        String title = news.getString("title");
                        String description = news.getString("description");
                        String newsUrl = news.getString("url");
                        String urlToImage = news.getString("urlToImage");
                        String publishedAt = news.getString("publishedAt");


                        String sqlString="INSERT INTO articles (articletitle , articledescription ,"+
                                "articleurl , imageurllink , publishdate ) VALUES (?,?,?,?,?)";
                        SQLiteStatement statement=  articlesDataBase.compileStatement(sqlString);
                        statement.bindString(1,title);
                        statement.bindString(2,description);
                        statement.bindString(3,newsUrl);
                        statement.bindString(4,urlToImage);
                        statement.bindString(5,publishedAt);

                        statement.execute();

                        Log.i("news", title+"\n"+ description+"\n"+ newsUrl+"\n"+ urlToImage+"\n"+ publishedAt+"\n");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Unable to connect news",Toast.LENGTH_SHORT).show();
                }

                return result;

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateListView();
        }

    }


    //MENU

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.worldnews){
            finish();
            Intent intent=new Intent(getApplicationContext(),worldNews.class);
            startActivity(intent);
            return true;
        }
        return false;
    }



}
