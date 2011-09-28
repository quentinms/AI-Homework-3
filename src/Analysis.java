import java.util.Date;
import java.util.Vector;

public class Analysis {

	Action action;
	double probability;

	Date pDue;
	
	double[] pi = { 0.27, 0.26, 0.24, 0.23 };
	final static double CONVERGENCE_THRESHOLD = 0.01;

	final static int NUMBER_OF_STATES = 4;

	final static int NUMBER_OF_OUTPUTS = 25;

	final static int MIGRATING = 0;
	final static int QUACKING = 1;
	final static int PANICKING = 2;
	final static int FEIGNING_DEATH = 3;

	/* Initialize probabilities (state to state, state to move, etc...) */

	double[][] a = { { 0.4, 0.1, 0.3, 0.2 },// Fr. Migr. to.
			{ 0.2, 0.15, 0.25, 0.4 }, // From Quacking to...
			{ 0.2, 0.3, 0.4, 0.1 }, // From Panicking to...
			{ 0.1, 0.2, 0.2, 0.5 } }; // From Feigning death to...

	double[][] b = {
			{ 0.023166023166023165, 0.02355212355212355, 0.023938223938223938,
					0.01969111969111969, 0.020077220077220077,
					0.05791505791505792, 0.06177606177606178,
					0.05405405405405406, 0.05444015444015444,
					0.03861003861003861, 0.038996138996139,
					0.03938223938223938, 0.039768339768339774,
					0.040154440154440155, 0.04247104247104247,
					0.04285714285714286, 0.04324324324324324,
					0.043629343629343634, 0.044015444015444015,
					0.0444015444015444, 0.044787644787644784,
					0.04517374517374517, 0.04555984555984556,
					0.04594594594594595, 0.04633204633204633 },
			{ 0.05154639175257732, 0.020618556701030927, 0.021649484536082474,
					0.08247422680412371, 0.08350515463917525,
					0.022680412371134023, 0.023711340206185566,
					0.0845360824742268, 0.08556701030927835,
					0.01958762886597938, 0.018556701030927835,
					0.01752577319587629, 0.016494845360824743,
					0.024742268041237112, 0.02577319587628866,
					0.026804123711340208, 0.027835051546391754,
					0.028865979381443297, 0.029896907216494843,
					0.030000000000000002, 0.030103092783505155,
					0.0865979381443299, 0.08762886597938144,
					0.08865979381443298, 0.08969072164948452 },
			{ 0.012195121951219513, 0.049999999999999996, 0.051219512195121955,
					0.03780487804878049, 0.03902439024390244,
					0.0524390243902439, 0.05365853658536586,
					0.04024390243902439, 0.041463414634146344,
					0.054878048780487805, 0.05609756097560975,
					0.05731707317073171, 0.058536585365853655,
					0.042682926829268296, 0.04390243902439025,
					0.0451219512195122, 0.046341463414634146,
					0.0475609756097561, 0.04878048780487805,
					0.059756097560975614, 0.06097560975609756,
					0.036585365853658534, 0.03536585365853658,
					0.03414634146341463, 0.032926829268292684 },
			{ 0.037037037037037035, 0.018518518518518517, 0.14814814814814814,
					0.01888888888888889, 0.18518518518518517,
					0.01925925925925926, 0.019629629629629632, 0.02,
					0.040740740740740744, 0.020370370370370372,
					0.020740740740740744, 0.02111111111111111,
					0.02148148148148148, 0.04111111111111111,
					0.04148148148148149, 0.04185185185185185,
					0.04222222222222222, 0.04259259259259259,
					0.04296296296296296, 0.04333333333333333,
					0.0437037037037037, 0.04407407407407407,
					0.044444444444444446, 0.1111111111111111,
					0.12962962962962962 } };

	double[][] alpha;
	double[][] beta;

	double[][] gamma;
	double[][][] gammaT;

	double[] c;

	final static int maxIters = 20;
	int iters = 0;
	double oldLogProb = Double.NEGATIVE_INFINITY;

	Duck duck;

	public AnalysisResult analyseDuck() {

		int[] O = convertToO(duck.mSeq);

		baumWelch(O);

		int maxMove = 0;
		double max = 0;

		int[] Op = new int[O.length + 1];

		System.arraycopy(O, 0, Op, 0, O.length);

		double prob = 0;

		for (int move = 0; move < 25; move++) {
			
			prob=sumProbs(move);
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
				sum+=alpha[i][alpha[0].length-1]*a[i][j]*b[j][move];
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
		//
		// System.out.println("----");
		// print(pi);
		// System.out.println("----");
		// print(a);
		// System.out.println("-----");
		// print(b);
		// System.out.println("-----");
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
		c[0] = 1 / c[0];
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
			c[t] = 1 / c[t];
			for (int i = 0; i < NUMBER_OF_STATES; i++) {
				alpha[i][t] = alpha[i][t] * c[t];
			}
		}
		// System.out.println("alpha");
		// print(alpha);
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

		// System.out.println("beta");
		// print(beta);
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
					gammaT[i][j][t] = alpha[i][t] * a[i][j] * b[j][O[t + 1]]
							* beta[j][t + 1]/denom;
					gamma[i][t] = gamma[i][t] + gammaT[i][j][t];
				}
			}
		}
		// System.out.println("gamma");
		// print(gamma);
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
		if (iters < maxIters && logProb > oldLogProb) {
			oldLogProb = logProb;
			return true;
		}
		return false;
	}

	// TODO
	public Analysis(Duck ducky, Date dueDate) {

		duck = ducky;
		int T = ducky.mSeq.size();
		pDue=dueDate;
		c = new double[T];

		alpha = new double[NUMBER_OF_STATES][T];
		beta = new double[NUMBER_OF_STATES][T];

		gamma = new double[NUMBER_OF_STATES][T];
		gammaT = new double[NUMBER_OF_STATES][NUMBER_OF_STATES][T];
	}

	public void print(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.printf("%1.2f ", matrix[i][j]);
			}
			System.out.println();
		}
	}

	private void print(double[] vector) {
		for (int i = 0; i < vector.length; i++) {
			System.out.printf("%1.2f ", vector[i]);
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
					Action.ACTION_STOP, Action.ACTION_ACCELERATE,
					Action.MOVE_DOWN);
			break;
		case 3:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_STOP, Action.ACTION_KEEPSPEED, Action.MOVE_UP);
			break;
		case 4:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_STOP, Action.ACTION_KEEPSPEED,
					Action.MOVE_DOWN);
			break;
		case 5:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_STOP,
					Action.MOVE_WEST);
			break;
		case 6:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_STOP,
					Action.MOVE_EAST);
			break;
		case 7:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_STOP,
					Action.MOVE_WEST);
			break;
		case 8:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_STOP,
					Action.MOVE_EAST);
			break;
		case 9:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_ACCELERATE,
					Action.MOVE_EAST + Action.MOVE_UP);
			break;
		case 10:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_ACCELERATE,
					Action.MOVE_WEST + Action.MOVE_UP);
			break;
		case 11:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_ACCELERATE,
					Action.MOVE_EAST + Action.MOVE_DOWN);
			break;
		case 12:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_ACCELERATE,
					Action.MOVE_WEST + Action.MOVE_DOWN);
			break;
		case 13:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_KEEPSPEED,
					Action.MOVE_EAST + Action.MOVE_UP);
			break;
		case 14:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_KEEPSPEED,
					Action.MOVE_WEST + Action.MOVE_UP);
			break;
		case 15:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_KEEPSPEED,
					Action.MOVE_EAST + Action.MOVE_DOWN);
			break;
		case 16:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_ACCELERATE, Action.ACTION_KEEPSPEED,
					Action.MOVE_WEST + Action.MOVE_DOWN);
			break;
		case 17:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_ACCELERATE,
					Action.MOVE_EAST + Action.MOVE_UP);
			break;
		case 18:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_ACCELERATE,
					Action.MOVE_WEST + Action.MOVE_UP);
			break;
		case 19:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_ACCELERATE,
					Action.MOVE_EAST + Action.MOVE_DOWN);
			break;
		case 20:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_ACCELERATE,
					Action.MOVE_WEST + Action.MOVE_DOWN);
			break;
		case 21:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_KEEPSPEED,
					Action.MOVE_EAST + Action.MOVE_UP);
			break;
		case 22:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_KEEPSPEED,
					Action.MOVE_WEST + Action.MOVE_UP);
			break;
		case 23:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_KEEPSPEED,
					Action.MOVE_EAST + Action.MOVE_DOWN);
			break;
		case 24:
			act = new Action(duck.GetLastAction().GetBirdNumber(),
					Action.ACTION_KEEPSPEED, Action.ACTION_KEEPSPEED,
					Action.MOVE_WEST + Action.MOVE_DOWN);
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

	private void convertToO(Vector<Action> actions, int[] o) {

		for (int i = 0; i < o.length; i++) {
			o[i] = actionToIndex(actions.get(i));
		}

	}

	private int actionToIndex(Action act) {
		int index = 0;

		if (act.GetHAction() == Action.ACTION_STOP) {
			if (act.GetVAction() == Action.ACTION_STOP) {
				index = 0;
			} else if (act.GetVAction() == Action.ACTION_ACCELERATE) {
				if ((act.GetMovement() & Action.MOVE_UP) != 0) {
					index = 1;
				} else if ((act.GetMovement() & Action.MOVE_DOWN) != 0) {
					index = 2;
				}
			} else if (act.GetVAction() == Action.ACTION_KEEPSPEED) {
				if ((act.GetMovement() & Action.MOVE_UP) != 0) {
					index = 3;
				} else if ((act.GetMovement() & Action.MOVE_DOWN) != 0) {
					index = 4;
				}
			}
		} else if (act.GetHAction() == Action.ACTION_ACCELERATE) {
			if (act.GetVAction() == Action.ACTION_STOP) {
				if ((act.GetMovement() & Action.MOVE_WEST) != 0) {
					index = 5;
				} else if ((act.GetMovement() & Action.MOVE_EAST) != 0) {
					index = 6;
				}
			} else if (act.GetVAction() == Action.ACTION_ACCELERATE) {
				if ((act.GetMovement() & Action.MOVE_UP) != 0
						&& (act.GetMovement() & Action.MOVE_EAST) != 0) {
					index = 9;
				} else if ((act.GetMovement() & Action.MOVE_UP) != 0
						&& (act.GetMovement() & Action.MOVE_WEST) != 0) {
					index = 10;
				} else if ((act.GetMovement() & Action.MOVE_DOWN) != 0
						&& (act.GetMovement() & Action.MOVE_EAST) != 0) {
					index = 11;
				} else if ((act.GetMovement() & Action.MOVE_DOWN) != 0
						&& (act.GetMovement() & Action.MOVE_WEST) != 0) {
					index = 12;
				}
			} else if (act.GetVAction() == Action.ACTION_KEEPSPEED) {
				if ((act.GetMovement() & Action.MOVE_UP) != 0
						&& (act.GetMovement() & Action.MOVE_EAST) != 0) {
					index = 13;
				} else if ((act.GetMovement() & Action.MOVE_UP) != 0
						&& (act.GetMovement() & Action.MOVE_WEST) != 0) {
					index = 14;
				} else if ((act.GetMovement() & Action.MOVE_DOWN) != 0
						&& (act.GetMovement() & Action.MOVE_EAST) != 0) {
					index = 15;
				} else if ((act.GetMovement() & Action.MOVE_DOWN) != 0
						&& (act.GetMovement() & Action.MOVE_WEST) != 0) {
					index = 16;
				}
			}
		} else if (act.GetHAction() == Action.ACTION_KEEPSPEED) {
			if (act.GetVAction() == Action.ACTION_STOP) {
				if ((act.GetMovement() & Action.MOVE_WEST) != 0) {
					index = 7;
				} else if ((act.GetMovement() & Action.MOVE_EAST) != 0) {
					index = 8;
				}
			} else if (act.GetVAction() == Action.ACTION_ACCELERATE) {
				if ((act.GetMovement() & Action.MOVE_UP) != 0
						&& (act.GetMovement() & Action.MOVE_EAST) != 0) {
					index = 17;
				} else if ((act.GetMovement() & Action.MOVE_UP) != 0
						&& (act.GetMovement() & Action.MOVE_WEST) != 0) {
					index = 18;
				} else if ((act.GetMovement() & Action.MOVE_DOWN) != 0
						&& (act.GetMovement() & Action.MOVE_EAST) != 0) {
					index = 19;
				} else if ((act.GetMovement() & Action.MOVE_DOWN) != 0
						&& (act.GetMovement() & Action.MOVE_WEST) != 0) {
					index = 20;
				}
			} else if (act.GetVAction() == Action.ACTION_KEEPSPEED) {
				if ((act.GetMovement() & Action.MOVE_UP) != 0
						&& (act.GetMovement() & Action.MOVE_EAST) != 0) {
					index = 21;
				} else if ((act.GetMovement() & Action.MOVE_UP) != 0
						&& (act.GetMovement() & Action.MOVE_WEST) != 0) {
					index = 22;
				} else if ((act.GetMovement() & Action.MOVE_DOWN) != 0
						&& (act.GetMovement() & Action.MOVE_EAST) != 0) {
					index = 23;
				} else if ((act.GetMovement() & Action.MOVE_DOWN) != 0
						&& (act.GetMovement() & Action.MOVE_WEST) != 0) {
					index = 24;
				}
			}
		}

		return index;

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
