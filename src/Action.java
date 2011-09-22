///represents an action of a bird, and its movement direction

///this class is used both for representing a past action and for
///shooting at a bird
public class Action {

    //used to represent each of the possible actions
    static final int ACTION_ACCELERATE=0;
    static final int ACTION_KEEPSPEED=1;
    static final int ACTION_STOP=2;

    ///used to represent the current movement state of a duck
    static final int BIRD_STOPPED=0;
    static final int MOVE_WEST=(1<<0);
    static final int MOVE_EAST=(1<<1);
    static final int MOVE_UP=(1<<2);
    static final int MOVE_DOWN=(1<<3);
    static final int BIRD_DEAD=(1<<4);

    ///construct a bird action object
    
    /// \param pBirdNumber the bird index
    /// \param pHorz the horizontal action of the bird
    /// \param pVert the horizontal action of the bird
    /// \param pMovement the movement of the bird 
    Action(int pBirdNumber,int pHorz,int pVert,int pMovement)
    {
        mBirdNumber=pBirdNumber;
        mHorz=pHorz;
        mVert=pVert;
        mMovement=pMovement;
    }

    ///returns true if the bird is dead
    boolean IsDead()  	{	return (mMovement&BIRD_DEAD)!=0;	}

    ///the index of the bird this action corresponds too
    int GetBirdNumber() 	{	return mBirdNumber;	}

    ///the horizontal action (one of ACTION_ACCELERATE, ACTION_KEEPSPEED or ACTION_STOP)
    int GetHAction()	{	return mHorz;	}
    ///the vertical action (one of ACTION_ACCELERATE, ACTION_KEEPSPEED or ACTION_STOP)
    int GetVAction()	{	return mVert;	}
    
    ///the movement of the bird
    
    ///can be either BIRD_STOPPED or a combination of one or two of MOVE_WEST, MOVE_EAST, MOVE_UP and MOVE_DOWN
    /// for example, MOVE_WEST|MOVE_UP  or MOVE_EAST or MOVE_EAST|MOVE_DOWN
    /// of course, MOVE_WEST can't be combined with MOVE_EAST and MOVE_UP can't be combined with MOVE_DOWN
    int GetMovement() {	return mMovement;	}

    ///represents a no-shoot action
    boolean IsDontShoot() {	return (mBirdNumber==-1);	}
    
    ///prints the content of this action object
    void Print()
    {
        if(IsDontShoot())
            System.out.println("DONT SHOOT");
        else
        {
            System.out.print(mBirdNumber);
            System.out.print(" ");
        
            if(IsDead())
                System.out.print("DEAD DUCK");
            else
            {
                if(mHorz==ACTION_ACCELERATE)
                    System.out.print("ACCELERATE ");
                else if(mHorz==ACTION_KEEPSPEED)
                    System.out.print("KEEPSPEED ");
                else 
                    System.out.print("STOP ");
                if(mVert==ACTION_ACCELERATE)
                    System.out.print("ACCELERATE");
                else if(mVert==ACTION_KEEPSPEED)
                    System.out.print("KEEPSPEED");
                else 
                    System.out.print("STOP");
            
                if(mMovement==BIRD_STOPPED)
                    System.out.print(" STOPPED");
                else
                {
                    if((mMovement&MOVE_UP)!=0)
                        System.out.print(" UP");
                    if((mMovement&MOVE_DOWN)!=0)
                        System.out.print(" DOWN");
                    if((mMovement&MOVE_WEST)!=0)
                        System.out.print(" WEST");
                    if((mMovement&MOVE_EAST)!=0)
                        System.out.print(" EAST");
                }
            }
            System.out.println();
        }
    }



    int mBirdNumber;
    int mHorz;
    int mVert;
    int mMovement;
    
    static final Action cDontShoot=new Action(-1,0,0,0);
}
