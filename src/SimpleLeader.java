import comp34120.ex2.PlayerImpl;
import comp34120.ex2.PlayerType;
import comp34120.ex2.Record;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A very simple leader implementation that only generates random prices
 * @author Xin
 */
final class SimpleLeader
	extends PlayerImpl
{
	/* The randomizer used to generate random price */
	private final Random m_randomizer = new Random(System.currentTimeMillis());

	private SimpleLeader()
		throws RemoteException, NotBoundException
	{
		super(PlayerType.LEADER, "Simple Leader");
	}

	@Override
	public void goodbye()
		throws RemoteException
	{
		ExitTask.exit(500);
	}

	/**
	 * To inform this instance to proceed to a new simulation day
	 * @param p_date The date of the new day
	 * @throws RemoteException
	 */
	@Override
	public void proceedNewDay(int p_date)
		throws RemoteException
	{
		m_platformStub.publishPrice(m_type, (float)maximise());

		Record record = m_platformStub.query(PlayerType.LEADER, p_date);
		m_platformStub.log(PlayerType.LEADER, "profit: " + calculateProfit(record.m_leaderPrice, record.m_followerPrice));

	}


	public float[] calculateAB() throws RemoteException {
		Record record;
		float leaderPrice = 0;
		int time = 100;
		float followerPrice = 0, sumUl = 0, sumUf = 0, sumUlSquare = 0, sumOfUlUf = 0;
		for (int i = 1; i <= time; i++) {
			record = m_platformStub.query(PlayerType.LEADER, i);
			leaderPrice = record.m_leaderPrice;
			followerPrice = record.m_followerPrice;
			sumUl += leaderPrice;
			sumUf += followerPrice;
			sumUlSquare += leaderPrice * leaderPrice;
			sumOfUlUf += leaderPrice * followerPrice;
		}
		float a = (sumUlSquare * sumUf - sumUl * sumOfUlUf) /  (time * sumUlSquare - (sumUl * sumUl));
		float b = (time * sumOfUlUf - sumUl * sumUf) / (time * sumUlSquare - (sumUl * sumUl));

		return new float[]{a,b};
	}

	public double maximise() throws RemoteException {
		float[] ab = calculateAB();

//		m_platformStub.log(PlayerType.LEADER, "a: " + ab[0] + " b: " + ab[1]);

		return (3+0.3*(ab[0]-ab[1])) / (2-0.6*ab[1]);
	}


	public double calculateProfit(float Ul, float Uf){
		return (Ul - 1) * (2 - Ul + 0.3*Uf);
	}
	/**
	 * Generate a random price based Gaussian distribution. The mean is p_mean,
	 * and the diversity is p_diversity
	 * @param p_mean The mean of the Gaussian distribution
	 * @param p_diversity The diversity of the Gaussian distribution
	 * @return The generated price
	 */
	private float genPrice(final float p_mean, final float p_diversity)
	{
		return (float) (p_mean + m_randomizer.nextGaussian() * p_diversity);
	}

	public static void main(final String[] p_args)
		throws RemoteException, NotBoundException
	{
		new SimpleLeader();
	}



	/**
	 * The task used to automatically exit the leader process
	 * @author Xin
	 */
	private static class ExitTask
		extends TimerTask
	{
		static void exit(final long p_delay)
		{
			(new Timer()).schedule(new ExitTask(), p_delay);
		}
		
		@Override
		public void run()
		{
			System.exit(0);
		}
	}
}
