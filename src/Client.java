import java.io.*;
import java.net.*;
import java.util.Date;

public class Client {

	public static void main(String[] pArgs) 
	{
		if(pArgs.length<3)
		{
			System.out.println("usage: java Client host port STANDALONE [options]");
			return;
		}
	
	    boolean lStandalone=pArgs[2].equals("STANDALONE");
	
		if(!lStandalone)
		{
		    System.out.println("this version can only run in STANDALONE mode");
		    return;
		}

		String lArgs=pArgs[2];
		for(int i=3;i<pArgs.length;i++)
		{
		    lArgs=lArgs+" ";
		    lArgs=lArgs+pArgs[i];
        }
		
		Player lPlayer=new Player();

		try
		{
			Socket lSocket=new Socket(pArgs[0],Integer.parseInt(pArgs[1]));
			PrintWriter lOut=new PrintWriter(lSocket.getOutputStream());
			BufferedReader lIn=new BufferedReader(new InputStreamReader(lSocket.getInputStream()));

			lOut.println("MODE "+lArgs);
            lOut.flush();

            State lState=new State();

            while(true)
            {
                //read ducks
                {
                    lState.mNumNewTurns=0;
                    
                    int lLinesLeft=1;

                    while(lLinesLeft!=0)
                    {
                        ++lState.mNumNewTurns;

                        String lString=lIn.readLine();
                        String[] lTokens=lString.split(" ");

                        lLinesLeft=Integer.parseInt(lTokens[0]);
                        int lBirdCount=Integer.parseInt(lTokens[1]);
                        
                        if(lState.mDucks==null)
                        {
                            lState.mDucks=new Duck[lBirdCount];
                            for(int i=0;i<lBirdCount;i++)
                                lState.mDucks[i]=new Duck();
                        }
            
                        for(int i=0;i<lBirdCount;i++)
                        {
                            int lHorz=Integer.parseInt(lTokens[2+3*i]);
                            int lVert=Integer.parseInt(lTokens[3+3*i]);
                            int lMovement=Integer.parseInt(lTokens[4+3*i]);
                            lState.mDucks[i].PushBackAction(new Action(i,lHorz,lVert,lMovement));
                        }
                    }
                }

                Date lTime;
                int lEnd;

                //read state
                {
                    String lString=lIn.readLine();
                    String[] lTokens=lString.split(" ");
                    
  	        	    lTime=new Date(Long.parseLong(lTokens[0])/1000);
                    
                    int lDuck=Integer.parseInt(lTokens[1]);
                    int lSpecies=Integer.parseInt(lTokens[2]);

                    if(lDuck>=0)
                    {
                        lState.mDucks[lDuck].SetSpecies(lSpecies);
                        lPlayer.Hit(lDuck,lSpecies);
                    }

                    lEnd=Integer.parseInt(lTokens[3]);
                    lState.mWhoIAm=Integer.parseInt(lTokens[4]);
                    int lNumPlayers=Integer.parseInt(lTokens[5]);

                    if(lState.mScores==null)
                    {
                        lState.mScores=new int[lNumPlayers];
                    }

                    for(int i=0;i<lNumPlayers;i++)
                    {
                        lState.mScores[i]=Integer.parseInt(lTokens[i+6]);
                    }
                }
                
                if(lEnd!=0)
                {
                    if(lState.mDucks.length>1)
                    {
            		    if(lStandalone)
                		    lTime=new Date(new Date().getTime()+60000);
                        lPlayer.Guess(lState.mDucks,lTime);
                        
                        StringBuffer lS=new StringBuffer();
                        
                        for(int i=0;i<lState.mDucks.length;i++)
                        {
                            if(i!=0) lS.append(' ');
                            lS.append(String.valueOf(lState.mDucks[i].GetSpecies()));
                        }

                        lOut.println(lS.toString());
                        lOut.flush();
                    }
                    
                    break;
                }
                else
                {
        		    if(lStandalone)
            		    lTime=new Date(new Date().getTime()+1000);
            		    
                    Action lAction=lPlayer.Shoot(lState,lTime);

                    lOut.println(String.valueOf(lAction.GetBirdNumber())+" "+
                                String.valueOf(lAction.GetHAction())+" "+
                                String.valueOf(lAction.GetVAction())+" "+
                                String.valueOf(lAction.GetMovement()));
                    lOut.flush();
                }
            }

            System.out.println(lIn.readLine());
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
}
