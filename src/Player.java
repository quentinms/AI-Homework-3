import java.util.Date;

public class Player {

//	int blackState = -1;
//	int tour = 0;
//	int shoot = 0;
//	int hit = 0;

	// /constructor
	// /There is no data in the beginning, so not much should be done here.
	Player() {
	}

	// /shoot!

	// /This is the function where you should do all your work.
	// /
	// /you will receive a variable pState, which contains information about all
	// ducks,
	// /both dead and alive. Each duck contains all past actions.
	// /
	// /The state also contains the scores for all players and the number of
	// /time steps elapsed since the last time this function was called.
	// /
	// /Check their documentation for more information.
	// /\param pState the state object
	// /\param pDue time before which we must have returned
	// /\return the position we want to shoot at, or cDontShoot if we
	// /prefer to pass
	Action Shoot(State pState, Date pDue) {

		if (pState.mDucks.length == 1 && pState.mDucks[0].mSeq.size() > 100)
			return ShootPractice(pState, pDue);
		else
			return ShootGame(pState, pDue);

	}

	Action ShootGame(State pState, Date pDue) {
		AnalysisResult max = new AnalysisResult(Action.cDontShoot, 0);
		Action result = Action.cDontShoot;
		AnalysisResult current;
		Duck maxDuck = null;
		if (pState.mDucks[0].mSeq.size() > 0) {
			for (Duck d : pState.mDucks) {
				if (d.IsAlive()) {
					current = new Analysis(d, pDue, false).analyseDuck();
					if (current.probability > max.probability) {
						max = current;
						maxDuck = d;
					}
				}

			}

//			if (Analysis.ducksEach3Set.get(0).size() != 1
//					&& Analysis.ducksEach3Set.get(1).size() != 1
//					&& Analysis.ducksEach3Set.get(2).size() != 1
//					&& Analysis.ducksEach3Set.get(3).size() != 1
//					|| Analysis.ducksEach3Set.get(0).size()
//					+ Analysis.ducksEach3Set.get(1).size()
//					+ Analysis.ducksEach3Set.get(2).size()
//					+ Analysis.ducksEach3Set.get(3).size()!=180) {
//				Analysis.ducksEach3Set = Analysis.initiateArrayList();
//			} else
//				Analysis.checkStates = false;
//
//			blackState = Analysis.findBlackState();
//			//
//			if (blackState == -1) {
//				Analysis.ducksEach3Set = Analysis.initiateArrayList();
//			} else {
//				Analysis.checkStates = false;
//			}
//
//			System.out.println(tour + " - "
//					+ Analysis.ducksEach3Set.get(0).size() + " "
//					+ Analysis.ducksEach3Set.get(1).size() + " "
//					+ Analysis.ducksEach3Set.get(2).size() + " "
//					+ Analysis.ducksEach3Set.get(3).size());

			int move = -1;
			//
			// if (blackState != -1)
			// System.out.println("Blackbird found!");
			// // System.out.println(blackState);
			// if (maxDuck != null
			// && blackState != -1
			// && max.probability > 0.7
			// && !Analysis.ducksEach3Set.get(blackState).contains(
			// max.action.mBirdNumber))
			if (maxDuck != null) {
				move = Analysis.findMovement(maxDuck, max.action);
				//
				if (move != -1) {
					if (max.probability > 0.33) {
						result = new Action(maxDuck.GetLastAction()
								.GetBirdNumber(), max.action.GetHAction(),
								max.action.GetVAction(), move);
						//shoot++;
						// System.out.println("BANG! ---> " + hit + " / " +
						// shoot);
					}
				}

			}

		}

		return result;
	}

	Action ShootPractice(State pState, Date pDue) {

		return new Analysis(pState.mDucks[0], pDue, true).analyseDuck().action;

	}

	// /guess the species!

	// /This function will be called at the end of the game, to give you
	// /a chance to identify the species of the surviving ducks for extra
	// /points.
	// /
	// /For each alive duck in the vector, you must call the SetSpecies
	// function,
	// /passing one of the ESpecies constants as a parameter
	// /\param pDucks the vector of all ducks. You must identify only the ones
	// that are alive
	// /\param pDue time before which we must have returned
	void Guess(Duck[] pDucks, Date pDue) {
		/*
		 * Here you should write your clever algorithms to guess the species of
		 * each alive bird. This skeleton guesses that all of them are white...
		 * they were the most likely after all!
		 */
		System.out.print("$");

		/* Killing all birds most of the time so... */

		for (int i = 0; i < pDucks.length; i++) {
			if (pDucks[i].IsAlive())
				pDucks[i].SetSpecies(Duck.SPECIES_WHITE);
		}
	}

	// /This function will be called whenever you hit a duck.
	// /\param pDuck the duck index
	// /\param pSpecies the species of the duck (it will also be set for this
	// duck in pState from now on)
	void Hit(int pDuck, int pSpecies) {
		//hit++;
		if (pSpecies == 1) {
			System.err.println("HIT BLACK!");
		} else
			System.out.println("HIT DUCK!!! " + getColor(pSpecies));
	}

	private String getColor(int species) {
		String s = "";

		switch (species) {

		case -1:
			s = "Unknown";
			break;
		case 0:
			s = "White";
			break;
		case 2:
			s = "Blue";
			break;
		case 3:
			s = "Red";
			break;
		case 4:
			s = "Green";
			break;
		case 5:
			s = "Yellow";
			break;

		}

		return s;
	}
}
