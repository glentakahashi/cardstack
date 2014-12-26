# Card Stack for Android
Card Stack for Android is an app that simulates card stacking as described below.

Card stack is a method of emulating dice rolls to reduce randomness by turning probabilities
of rolls into actual counts of rolls.
This is done by placing an equivalent number of cards for the probability of each roll
into a stack of cards such that each roll will be drawn exactly once.
For an example, a pair of dice will result in the following stack of cards:
    2 - one card
    3 - two cards
    4 - three cards
    5 - four cards
    6 - five cards
    7 - six cards
    8 - five cards
    9 - four cards
    10 - three cards
    11 - two cards
    12 - one card

As you can see each possible roll has the same number of cards as its probability to roll out of 36.
4 can be rolled 3 ways ({1,3},{2,2},{3,1}) so there are 3 4's in the deck.
8 can be rolled 5 ways ({2,6},{3,5},{4,4},{5,3},{6,2}) so there are 5 8's in the deck.

As the game is played on, instead of rolling dice the top card from the stack is taken.
Once the stack is empty the stack is reshuffled and drawing starts again.

Variations include:
  * Excluding some cards randomly excluded every shuffle of the stack.
  * Adding in some extra cards randomly.
  * Showing the individual dice rolls for each card.
  * Showing history or not showing history.
  * Showing the remaining counts of cards or not.
