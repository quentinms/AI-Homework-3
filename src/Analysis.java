import java.util.Date;
import java.util.Vector;

public class Analysis {

	Action action;
	double probability;

	Date pDue;

	double[] pi;
	final static int NUMBER_OF_STATES = 3;
	final static int NUMBER_OF_OUTPUTS = 9;

	// final static int MIGRATING = 0;
	// final static int QUACKING = 1;
	// final static int PANICKING = 2;
	// final static int FEIGNING_DEATH = 3;

	/* Initialize probabilities (state to state, state to move, etc...) */

	double[][] a;

	double[][] b;
	double[][] alpha;
	double[][] beta;

	double[][] gamma;
	double[][][] gammaT;

	double[] c;

	private static double LOG_REITERATION_THRESHOLD = 0.0000000001;
	int timeThreshold;

	final static int MAX_ITERS = 5000;
	int iters = 0;
	double oldLogProb = Double.NEGATIVE_INFINITY;

	Duck duck;

	// TODO
	public AnalysisResult analyseDuck() {

		int[] O = convertToO(duck.mSeq);

		baumWelch(O);

		int maxMove = 0;
		double max = 0;

		double prob = 0;

		for (int move = 0; move < NUMBER_OF_OUTPUTS; move++) {

			prob = sumProbs(move);
			if (prob > max) {
				max = prob;
				maxMove = move;
			}
		}

		return new AnalysisResult(indexToAction(maxMove), max);
	}

	private double sumProbs(int move) {
		double sum = 0;
		for (int i = 0; i < NUMBER_OF_STATES; i++) {
			for (int j = 0; j < NUMBER_OF_STATES; j++) {
				sum += alpha[i][alpha[0].length - 1] * a[i][j] * b[j][move];
			}
		}
		return sum;
	}

	private void baumWelch(int[] O) {

		do {
			alphaPass(O);
			betaPass(O);
			computeGammas(O);
			reestimatePi(O);
			reestimateA(O);
			reestimateB(O);
		} while (iterateHamlet(O));

	}

	private void alphaPass(int[] O) {

		int T = O.length;

		// Compute alpha-0(i)
		c[0] = 0;
		for (int i = 0; i < NUMBER_OF_STATES; i++) {
			alpha[i][0] = pi[i] * b[i][O[0]];
			c[0] = c[0] + alpha[i][0];
		}

		// scale the alpha-0(i)
		c[0] = 1.0 / c[0];
		for (int i = 0; i < NUMBER_OF_STATES; i++) {
			alpha[i][0] = c[0] * alpha[i][0];
		}

		// compute alpha-t(i)
		for (int t = 1; t < T; t++) {
			c[t] = 0;
			for (int i = 0; i < NUMBER_OF_STATES; i++) {
				alpha[i][t] = 0;
				for (int j = 0; j < NUMBER_OF_STATES; j++) {
					alpha[i][t] = alpha[i][t] + alpha[j][t - 1] * a[j][i];
				}
				alpha[i][t] = alpha[i][t] * b[i][O[t]];
				c[t] = c[t] + alpha[i][t];
			}

			// scale alpha-t(i)
			c[t] = 1.0 / c[t];
			for (int i = 0; i < NUMBER_OF_STATES; i++) {
				alpha[i][t] = alpha[i][t] * c[t];
			}
		}

	}

	private void betaPass(int[] O) {
		int T = O.length;
		// Scale beta_T-1(i)
		for (int i = 0; i < NUMBER_OF_STATES; i++) {
			beta[i][T - 1] = c[T - 1];
		}

		// Beta-pass

		for (int t = T - 2; t >= 0; t--) {
			for (int i = 0; i < NUMBER_OF_STATES; i++) {
				beta[i][t] = 0;
				for (int j = 0; j < NUMBER_OF_STATES; j++) {
					beta[i][t] = beta[i][t] + a[i][j] * b[j][O[t + 1]]
							* beta[j][t + 1];
				}
				// scale beta-t(i)
				beta[i][t] = c[t] * beta[i][t];
			}
		}

	}

	private void computeGammas(int[] O) {
		int T = O.length;
		for (int t = 0; t < T - 1; t++) {
			double denom = 0;
			for (int i = 0; i < NUMBER_OF_STATES; i++) {
				for (int j = 0; j < NUMBER_OF_STATES; j++) {
					denom = denom + alpha[i][t] * a[i][j] * b[j][O[t + 1]]
							* beta[j][t + 1];
				}
			}
			for (int i = 0; i < NUMBER_OF_STATES; i++) {
				gamma[i][t] = 0;
				for (int j = 0; j < NUMBER_OF_STATES; j++) {
					gammaT[i][j][t] = (alpha[i][t] * a[i][j] * b[j][O[t + 1]] * beta[j][t + 1])
							/ denom;
					gamma[i][t] = gamma[i][t] + gammaT[i][j][t];
				}
			}
		}
	}

	private void reestimatePi(int[] O) {
		for (int i = 0; i < NUMBER_OF_STATES; i++) {
			pi[i] = gamma[i][0];
		}
	}

	private void reestimateA(int[] O) {
		int T = O.length;
		for (int i = 0; i < NUMBER_OF_STATES; i++) {
			for (int j = 0; j < NUMBER_OF_STATES; j++) {
				double numer = 0;
				double denom = 0;
				for (int t = 0; t < T - 1; t++) {
					numer = numer + gammaT[i][j][t];
					denom = denom + gamma[i][t];
				}

				a[i][j] = numer / denom;
			}
		}
	}

	private void reestimateB(int[] O) {
		int T = O.length;
		for (int i = 0; i < NUMBER_OF_STATES; i++) {
			for (int j = 0; j < NUMBER_OF_OUTPUTS; j++) {
				double numer = 0;
				double denom = 0;
				for (int t = 0; t < T - 1; t++) {
					if (O[t] == j) {
						numer = numer + gamma[i][t];
					}
					denom = denom + gamma[i][t];
				}
				b[i][j] = numer / denom;
			}
		}
	}

	private double computeLog(int[] O) {
		int T = O.length;
		double logProb = 0;
		for (int i = 0; i < T; i++) {
			logProb = logProb + Math.log(c[i]);
		}
		logProb = -logProb;

		return logProb;
	}

	private boolean iterateHamlet(int[] O) {
		iters = iters + 1;
		double logProb = computeLog(O);
		if (pDue.getTime() > new Date().getTime() + timeThreshold && iters < MAX_ITERS
				&& logProb > (oldLogProb + LOG_REITERATION_THRESHOLD)) {
			oldLogProb = logProb;
			return true;
		}
		return false;
	}

	// TODO
	public Analysis(Duck ducky, Date dueDate, boolean isPractice) {

		duck = ducky;
		int T = ducky.mSeq.size();
		pDue = dueDate;
		c = new double[T];

		alpha = new double[NUMBER_OF_STATES][T];
		beta = new double[NUMBER_OF_STATES][T];

		gamma = new double[NUMBER_OF_STATES][T];
		gammaT = new double[NUMBER_OF_STATES][NUMBER_OF_STATES][T];

		pi = new double[NUMBER_OF_STATES];
		a = new double[NUMBER_OF_STATES][NUMBER_OF_STATES];
		b = new double[NUMBER_OF_STATES][NUMBER_OF_OUTPUTS];

		if (isPractice) {
			timeThreshold = 100;
		} else {
			timeThreshold = 300;
		}

		fill(pi);
		fill(a);
		fill(b);

		// print(pi);
		// System.out.println("-");
		// print(a);
		// System.out.println("-");
		// print(b);
	}

	// TODO
	private void fill(double[] matrix, int index) {

		int length = matrix.length;

		double blu = 1.0 / length;
		int plop = 1;

		int mod = 1;

		double sum = 0;

		if (length % 2 == 0) {
			mod = 0;
		}

		for (int i = 0; i < length; i++) {

			if (mod == 0 || (mod == 1 && i != length - 1)) {
				matrix[i] = blu + (Math.pow(-1, i % 2)) * (plop) * 0.001
						* (index + 1);

				sum += matrix[i];
			} else {
				matrix[i] = 1 - sum;
			}

			if (i % 2 == 1)
				plop++;

		}

		swap(matrix, index);

	}

	private void swap(double[] matrix, int index) {
		double a = matrix[matrix.length - 1];
		int swap = matrix.length - index - 1;
		double b = matrix[swap];
		matrix[matrix.length - 1] = b;
		matrix[swap] = a;
	}

	private void fill(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			fill(matrix[i], i);
		}
	}

	private void fill(double[] matrix) {
		fill(matrix, 0);
	}

	private void checkMatrix(double[] matrix) {
		double sum = 0;

		for (int i = 0; i < matrix.length; i++) {
			sum += matrix[i];
		}

		if (sum != 1)
			System.err.println("!=1 : " + sum);
	}

	public void print(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.printf("%1.6f  ", matrix[i][j]);
			}
			System.out.println();
		}
	}

	private void print(double[] vector) {
		for (int i = 0; i < vector.length; i++) {
			System.out.printf("%1.6f ", vector[i]);
		}
		System.out.println();
	}

	private void print(int[] vector) {
		for (int i = 0; i < vector.length; i++) {
			System.out.printf("%d ", vector[i]);
		}
		System.out.println();
	}

	private Action indexToAction(int index) {
		Action act = null;

		switch (index) {
		case 0:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_STOP, Action.ACTION_STOP, Action.BIRD_STOPPED);
			break;
		case 1:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_STOP, Action.ACTION_ACCELERATE,
					Action.MOVE_UP);
			break;
		case 2:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_STOP, Action.ACTION_KEEPSPEED, Action.MOVE_UP);
			break;
		case 3:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_STOP,
					Action.MOVE_WEST);
			break;
		case 4:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_ACCELERATE,
					Action.MOVE_WEST);
			break;
		case 5:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_KEEPSPEED,
					Action.MOVE_EAST + Action.MOVE_UP);
			break;
		case 6:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_STOP,
					Action.MOVE_EAST + Action.MOVE_UP);
			break;
		case 7:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_ACCELERATE,
					Action.MOVE_EAST + Action.MOVE_UP);
			break;
		case 8:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_KEEPSPEED,
					Action.MOVE_WEST + Action.MOVE_UP);
			break;

		default:
			break;
		}
		return act;
	}

	private int[] convertToO(Vector<Action> actions) {
		int[] o = new int[actions.size()];
		for (int i = 0; i < o.length; i++) {
			o[i] = actionToIndex(actions.get(i));
		}

		return o;

	}

	private int actionToIndex(Action act) {
		int index = 0;

		if (act.GetHAction() == Action.ACTION_STOP) {
			if (act.GetVAction() == Action.ACTION_STOP) {
				index = 0;
			} else if (act.GetVAction() == Action.ACTION_ACCELERATE) {
				index = 1;
			} else if (act.GetVAction() == Action.ACTION_KEEPSPEED) {
				index = 2;
			}
		} else if (act.GetHAction() == Action.ACTION_ACCELERATE) {
			if (act.GetVAction() == Action.ACTION_STOP) {
				index = 3;
			} else if (act.GetVAction() == Action.ACTION_ACCELERATE) {
				index = 4;
			} else if (act.GetVAction() == Action.ACTION_KEEPSPEED) {
				index = 5;
			}
		} else if (act.GetHAction() == Action.ACTION_KEEPSPEED) {
			if (act.GetVAction() == Action.ACTION_STOP) {
				index = 6;
			} else if (act.GetVAction() == Action.ACTION_ACCELERATE) {
				index = 7;
			} else if (act.GetVAction() == Action.ACTION_KEEPSPEED) {
				index = 8;
			}
		}

		return index;

	}

	static int findMovement(Duck d, Action act) {
		int movement = -1;
		Action lastAct = d.GetLastAction();
		int prevHAction = lastAct.GetHAction();
		int prevVAction = lastAct.GetVAction();

		int lastMovement = lastAct.GetMovement();
		int prevHDirection = getHDirection(lastMovement);
		int prevVDirection = getVDirection(lastMovement);
		int hAction = act.GetHAction();
		int vAction = act.GetVAction();
		int hDirection = -1;
		int vDirection = -1;

		if (prevHAction == Action.ACTION_STOP && hAction != Action.ACTION_STOP) {
			return -1;
		} else if (hAction == Action.ACTION_STOP) {
			hDirection = Action.BIRD_STOPPED;
		} else if ((prevHAction == Action.ACTION_KEEPSPEED && hAction == Action.ACTION_KEEPSPEED)
				|| (prevHAction == Action.ACTION_KEEPSPEED && hAction == Action.ACTION_ACCELERATE)
				|| (prevHAction == Action.ACTION_ACCELERATE && hAction == Action.ACTION_KEEPSPEED)
				|| (prevHAction == Action.ACTION_ACCELERATE && hAction == Action.ACTION_ACCELERATE)) {
			hDirection = prevHDirection;
		}

		if (prevVAction == Action.ACTION_STOP && vAction != Action.ACTION_STOP) {
			return -1;
		} else if (vAction == Action.ACTION_STOP) {
			vDirection = Action.BIRD_STOPPED;
		} else if ((prevVAction == Action.ACTION_KEEPSPEED && vAction == Action.ACTION_KEEPSPEED)
				|| (prevVAction == Action.ACTION_KEEPSPEED && vAction == Action.ACTION_ACCELERATE)
				|| (prevVAction == Action.ACTION_ACCELERATE && vAction == Action.ACTION_KEEPSPEED)
				|| (prevVAction == Action.ACTION_ACCELERATE && vAction == Action.ACTION_ACCELERATE)) {
			vDirection = prevVDirection;
		}

		if (hDirection != -1 && vDirection != -1) {
			movement = hDirection + vDirection;
		}
		return movement;
	}

	private static int getVDirection(int movement) {
		int vDir = 0;

		if ((movement & Action.MOVE_DOWN) != 0)
			vDir = Action.MOVE_DOWN;
		else if ((movement & Action.MOVE_UP) != 0)
			vDir = Action.MOVE_UP;

		return vDir;
	}

	private static int getHDirection(int movement) {
		int hDir = 0;

		if ((movement & Action.MOVE_EAST) != 0)
			hDir = Action.MOVE_EAST;
		else if ((movement & Action.MOVE_WEST) != 0)
			hDir = Action.MOVE_WEST;

		return hDir;
	}

}

class AnalysisResult {
	Action action;
	double probability;

	public AnalysisResult(Action action, double probability) {
		this.action = action;
		this.probability = probability;
	}
}
