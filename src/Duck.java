import java.util.Vector;

///represents a duck
public class Duck {

    static final int SPECIES_UNKNOWN=-1;		///< the species is unknown
    static final int SPECIES_WHITE=0;			///< the duck belongs to the white (common) species
    static final int SPECIES_BLACK=1;			///< the duck belongs to the black (endangered) species
    static final int SPECIES_BLUE=2;			///< the duck belongs to the blue species
    static final int SPECIES_RED=3; 			///< the duck belongs to the red species
    static final int SPECIES_GREEN=4;			///< the duck belongs to the green species
    static final int SPECIES_YELLOW=5;			///< the duck belongs to the yellow species

    Duck()
    {
        mSeq=new Vector<Action>();
        mSpecies=SPECIES_UNKNOWN;
    }

    ///length of the sequence of past actions of the duck
    int GetSeqLength() 			{	return mSeq.size();		}
    
    ///returns the last action of the duck
    Action GetLastAction() 	{	return GetAction(mSeq.size()-1);	}
    ///returns one action in the sequence of actions of the duck
    Action GetAction(int i)	{	return mSeq.elementAt(i);		}
    
    ///returns true if the duck is dead
    boolean IsDead()				{	return GetLastAction().IsDead();	}
    ///returns true if the duck was dead at time step i
    boolean WasDead(int i) 		{	return GetAction(i).IsDead();		}
    ///returns true if the duck is alive
    boolean IsAlive()				{	return !IsDead();	}
    ///returns true if the duck was alive at time step i
    boolean WasAlive(int i)			{	return !WasDead(i);		}

    ///returns the species of the duck (this will only be set if you
    ///have shot the duck)
    int GetSpecies()		{	return mSpecies;	}

    ///used in the Guess function to guess the species of a duck
    void SetSpecies(int pSpecies)	{	mSpecies=pSpecies;	}




    void PushBackAction(Action pAction)	{   mSeq.add(pAction);	}
    Vector<Action> mSeq;
    int mSpecies;
}
