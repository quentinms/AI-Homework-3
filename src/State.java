import java.util.Arrays;

///represents the game state
public class State {

    State()
    {
    }

    ///returns the number of ducks
    int GetNumDucks()	{	return mDucks.length;	}
    ///returns a reference to the i-th duck
    Duck GetDuck(int i)	{	return mDucks[i];		}

    ///returns the index of your player among all players
    int WhoAmI() 				{	return mWhoIAm;			}

    ///returns the number of players
    int GetNumPlayers()			{	return mScores.length;	}

    ///returns your current score
    int MyScore() 				{	return mScores[mWhoIAm];	}
    ///returns the score of the i-th player
    int GetScore(int i) 		{	return mScores[i];			}
    
    ///returns the number of turns elapsed since last time Shoot was called.
    
    ///this is the amount of new data available for each duck
    int GetNumNewTurns()		{	return mNumNewTurns;	}
    
    Duck[] mDucks;
    int[] mScores;
    int mWhoIAm;
    int mNumNewTurns;
}
