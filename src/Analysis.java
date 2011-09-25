import java.util.Vector;

public class Analysis {

	Action action;
	double probability;

	final static double[] Pi = { 0.25, 0.25, 0.25, 0.25 };
	final static double CONVERGENCE_THRESHOLD = 0.01;

	final static int NUMBER_OF_STATES = 4;
	
	final static int MIGRATING = 0;
	final static int QUACKING = 1;
	final static int PANICKING = 2;
	final static int FEIGNING_DEATH = 3;

	final static int ACCELERATE = 0;
	final static int STOP = 1;
	final static int KEEP_SPEED = 2;

	final static int KEEP_DIRECTION = 0;
	final static int CHANGE_DIRECTION = 1;
	final static int NO_DIRECTION = 2;

	/* Initialize probabilities (state to state, state to move, etc...) */

	double[][] stateToStateProbs = { { 0.25, 0.25, 0.25, 0.25 },// Fr. Migr. to.
			{ 0.25, 0.25, 0.25, 0.25 }, // From Quacking to...
			{ 0.25, 0.25, 0.25, 0.25 }, // From Panicking to...
			{ 0.25, 0.25, 0.25, 0.25 } }; // From Feigning death to...

	double[][] stateToSpeedHProbs = { { 0.2, 0.1, 0.7 }, // Migrating
			{ 0.2, 0.2, 0.6 }, // Quacking
			{ 0.8, 0.1, 0.1 },// Panicking
			{ 0.1, 0.7, 0.2 },// Feigning death
	};
	double[][] stateToSpeedVProbs = { { 0.1, 0.7, 0.2 }, // Migrating
			{ 0.2, 0.2, 0.6 }, // Quacking
			{ 0.8, 0.1, 0.1 },// Panicking
			{ 0.7, 0.1, 0.2 },// Feigning death
	};

	double[][] stateToDirectionHProbs = { { 0.9, 0.0, 0.1 }, // Migrating
			{ 0.2, 0.6, 0.2 }, // Quacking
			{ 0.8, 0.1, 0.1 },// Panicking
			{ 0.2, 0.1, 0.7 },// Feigning death
	};
	double[][] stateToDirectionVProbs = { { 0.1, 0.1, 0.8 }, // Migrating
			{ 0.2, 0.6, 0.2 }, // Quacking
			{ 0.8, 0.1, 0.1 },// Panicking
			{ 0.7, 0.1, 0.2 },// Feigning death
	};

	Duck duck;

	public AnalysisResult analyseDuck() {

		baumWelch(duck.mSeq);

		return new AnalysisResult(Action.cDontShoot, 0);
	}

	private void baumWelch(Vector<Action> observedSequence) {

		forward(observedSequence);
		backward(observedSequence);

		
	}

	private double[][] forward(Vector<Action> observedSequence) {
		return null;
	}

	private double[][] backward(Vector<Action> observedSequence) {
		return null;
	}

	private double probaCalculation() {
		return 0;
	}

	private double gamma() {
		return 0;
	}

	public Analysis(Duck ducky) {

		duck = ducky;
	}

	// private void countMoves() {
	//
	// // int total=ducky.mSeq.size();
	//
	// int current;
	// int max = 0;
	// ;
	//
	// for (Action act : duck.mSeq) {
	// int h = act.GetHAction();
	// int v = act.GetVAction();
	// int m = act.GetMovement();
	//
	// /* Horizontal movements */
	//
	// if (h == Action.ACTION_ACCELERATE) {
	// if ((m & Action.MOVE_EAST) != 0) {
	// acc_east++;
	// } else if ((m & Action.MOVE_WEST) != 0) {
	// acc_west++;
	// }
	//
	// } else if (h == Action.ACTION_STOP) {
	//
	// stop_horz++;
	//
	// } else if (h == Action.ACTION_KEEPSPEED) {
	//
	// if ((m & Action.MOVE_EAST) != 0) {
	// keep_speed_east++;
	// } else if ((m & Action.MOVE_WEST) != 0) {
	// keep_speed_west++;
	// }
	// }
	//
	// /* Vertical movements */
	//
	// if (v == Action.ACTION_ACCELERATE) {
	// if ((m & Action.MOVE_UP) != 0) {
	// acc_up++;
	// } else if ((m & Action.MOVE_DOWN) != 0) {
	// acc_down++;
	// }
	//
	// } else if (v == Action.ACTION_STOP) {
	//
	// stop_vert++;
	//
	// } else if (v == Action.ACTION_KEEPSPEED) {
	//
	// if ((m & Action.MOVE_UP) != 0) {
	// keep_speed_up++;
	// } else if ((m & Action.MOVE_DOWN) != 0) {
	// keep_speed_down++;
	// }
	// }
	// }
	// }
}

class AnalysisResult {
	Action action;
	double probability;

	public AnalysisResult(Action action, double probability) {
		this.action = action;
		this.probability = probability;
	}
}
