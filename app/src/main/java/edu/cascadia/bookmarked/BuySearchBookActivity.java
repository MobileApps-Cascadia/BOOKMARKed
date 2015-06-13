package edu.cascadia.bookmarked;

/**
 * Created by DebraW on 6/10/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class BuySearchBookActivity extends Activity{
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_search_book);
        initPostWantedAdButton();
      //  addListenerOnButtonTextbookList();
        addListenerOnButtonBuy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post_wanted_ad, menu);
        return true;
    }

    // Method for Post a Wanted Ad button
    private void initPostWantedAdButton(){
        Button adButton = (Button) findViewById(R.id.buttonEditPost);
        adButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(BuySearchBookActivity.this, PostWantedAdActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            }
        });
    }

    // Buy Book navigation button
    public void addListenerOnButtonBuy() {
        ImageView btnBuy2 = (ImageView)findViewById(R.id.imageButtonBuy);
        btnBuy2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentbuy2 = new Intent(BuySearchBookActivity.this, BuySearchBookActivity.class);
                startActivity(intentbuy2);
            }
        });
    }

    // Wanted Book List navigation button
    public void addListenerOnButtonTextbookList() {
        ImageView btnList2 = (ImageView)findViewById(R.id.imageButtonBookWanted);
        btnList2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentbooklist2 = new Intent(BuySearchBookActivity.this, TextbookListActivity.class);
                startActivity(intentbooklist2);
            }
        });
    }
}
