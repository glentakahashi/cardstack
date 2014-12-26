package com.glentaka.cardstack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Random;
import java.util.zip.Inflater;


public class CardStack extends Activity {

    private List<Card> stack;
    private int numDice = 2;
    private int sidesOnDice = 6;
    private int extraCards = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_stack);
        reset();
        final TextView rollView = (TextView) findViewById(R.id.flippedCardsTextView);
        final TextView lastDrawnTextView = (TextView) findViewById(R.id.lastDrawnTextView);
        Button b = (Button) findViewById(R.id.flipButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollView.setText(lastDrawnTextView.getText() + "\n" + rollView.getText());
                if(stack.size() == 0) {
                    reroll();
                    rollView.setText("Deck finished, shuffling!\n" + rollView.getText());
                }
                lastDrawnTextView.setText(stack.remove(0).toString());
            }
        });
    }

    private void reroll() {
        stack = buildDeck(numDice, sidesOnDice, extraCards);
        fisherYates(stack, 50);
    }

    private void reset() {
        reroll();
        ((TextView) findViewById(R.id.flippedCardsTextView)).setText("");
    }

    private void fisherYates(List deck, int n) {
        Random rand = new Random();
        for(;n>0;n--) {
            for (int i = deck.size() - 1; i >= 0; i--) {
                int shuffle = rand.nextInt(i + 1);
                Object temp = deck.get(i);
                deck.set(i, deck.get(shuffle));
                deck.set(shuffle, temp);
            }
        }
    }

    class Card {
        List<Integer> rolls;

        public Card() {
            rolls = Lists.newArrayList();
        }

        public Card(List<Integer> rolls) {
            this();
            this.rolls.addAll(rolls);
        }

        public Card(Card card) {
            this(card.getRolls());
        }

        public Card(Card card, int roll) {
            this(card);
            rolls.add(roll);
        }

        public List<Integer> getRolls() {
            return rolls;
        }

        public int sum() {
            int sum = 0;
            for(int i : rolls) {
                sum += i;
            }
            return sum;
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("Dice: ");
            String separator = "";
            for(int i : rolls) {
                s.append(separator + i);
                separator = ", ";
            }
            s.append(" Roll: " + sum());
            return s.toString();
        }
    }

    private List<Card> buildDeck(int numDice, int sidesOnDice, int extraCards) {
        if(numDice < 1) {
            throw new IllegalArgumentException("Cannot have less than 1 die.");
        }
        List<Card> cards = buildDeckHelper(new Card(), numDice, sidesOnDice);
        Random random = new Random();
        if(extraCards > 0) {
            //duplicate a random card
            for (int i = 0; i < extraCards; i++) {
                cards.add(new Card(cards.get(random.nextInt(cards.size()))));
            }
        } else if (extraCards < 0) {
            //delete a random card
            for (int i = 0; i > extraCards && cards.size() > 1; i--) {
                cards.remove(random.nextInt(cards.size()));
            }
        }
        return cards;
    }

    private List<Card> buildDeckHelper(Card baseCard, int numDice, int sidesOnDice) {
        List<Card> cards = Lists.newArrayList();
        if(numDice == 1) {
            for(int i = 1; i <= sidesOnDice; i++) {
                cards.add(new Card(baseCard, i));
            }
        } else {
            for(int i = 1; i <= sidesOnDice; i++) {
                cards.addAll(buildDeckHelper(new Card(baseCard, i), numDice-1, sidesOnDice));
            }
        }
        return cards;
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
                    .setTitle(R.string.dialog_reset_title)
                    .setPositiveButton(R.string.dialog_reset_pos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            reset();
                        }
                    })
                    .setNegativeButton(R.string.dialog_reset_neg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create().show();
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_settings) {
            LayoutInflater inflater = CardStack.this.getLayoutInflater();
            final AlertDialog diag = new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_settings_title)
                    .setView(inflater.inflate(R.layout.dialog_settings, null))
                    .setPositiveButton(R.string.dialog_settings_pos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            numDice = Integer.parseInt(((TextView) findViewById(R.id.numDiceEditText)).getText().toString());
                            sidesOnDice = Integer.parseInt(((TextView) findViewById(R.id.numSidesEditText)).getText().toString());
                            extraCards = Integer.parseInt(((TextView) findViewById(R.id.numExtraEditText)).getText().toString());
                            //reset();
                        }
                    })
                    .setNegativeButton(R.string.dialog_settings_neg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create();
            diag.show();
            ((EditText) diag.findViewById(R.id.numExtraEditText)).setText(""+extraCards);
            ((EditText) diag.findViewById(R.id.numSidesEditText)).setText(""+sidesOnDice);
            ((EditText) diag.findViewById(R.id.numDiceEditText)).setText(""+numDice);
            diag.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int nd = Integer.parseInt(((TextView) diag.findViewById(R.id.numDiceEditText)).getText().toString());
                        int sd = Integer.parseInt(((TextView) diag.findViewById(R.id.numSidesEditText)).getText().toString());
                        int ec = Integer.parseInt(((TextView) diag.findViewById(R.id.numExtraEditText)).getText().toString());
                        if(nd != numDice || sd != sidesOnDice || ec != extraCards) {
                            numDice = nd;
                            sidesOnDice = sd;
                            extraCards = ec;
                            reset();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(v.getContext(), "Please enter valid numbers.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    diag.dismiss();
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }
}
