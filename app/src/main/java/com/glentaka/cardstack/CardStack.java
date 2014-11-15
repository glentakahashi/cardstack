package com.glentaka.cardstack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class CardStack extends Activity {

    private static final int[] baseRolls = {
            2,
            3,3,
            4,4,4,
            5,5,5,5,
            6,6,6,6,6,
            7,7,7,7,7,7,
            8,8,8,8,8,
            9,9,9,9,
            10,10,10,
            11,11,
            12
    };
    private List<Integer> rolls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_stack);
        this.rolls = fisherYates(baseRolls);
        final TextView rollView = (TextView) findViewById(R.id.flippedCardsTextView);
        Button b = (Button) findViewById(R.id.flipButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rolls.size() == 0) {
                    rolls = fisherYates(baseRolls);
                    rollView.setText("Deck finished, shuffling!\n" + rollView.getText());
                }
                rollView.setText(rolls.remove(0) + "\n" + rollView.getText());
            }
        });
    }

    private List<Integer> fisherYates(final int[] baseRolls) {
        Random rand = new Random();
        List<Integer> rolls = new ArrayList<Integer>();
        for(int i : baseRolls) {
            rolls.add(i);
        }
        for(int i = rolls.size()-1; i >= 0; i--) {
            int shuffle = rand.nextInt(i + 1);
            int temp = rolls.get(i);
            rolls.set(i, rolls.get(shuffle));
            rolls.set(shuffle, temp);
        }
        return rolls;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_card_stack, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_reset) {
            new AlertDialog.Builder(this)
                    .setPositiveButton(R.string.menu_reset_pos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            rolls = fisherYates(baseRolls);
                            ((TextView) findViewById(R.id.flippedCardsTextView)).setText("");
                        }
                    })
                    .setNegativeButton(R.string.menu_reset_neg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create().show();
        }

        return super.onOptionsItemSelected(item);
    }
}
