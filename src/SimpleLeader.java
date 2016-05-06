
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
        m_platformStub.publishPrice(m_type, genPrice(1.8f, 0.05f));
    }


    int p_steps = 0;
    /**
     * You may want to delete this method if you don't want to do any
     * initialization
     * @param p_steps Indicates how many steps will the simulation perform
     * @throws RemoteException If implemented, the RemoteException *MUST* be
     * thrown by this method
     */
    @Override
    public void startSimulation(int p_steps)
            throws RemoteException
    {
        super.startSimulation(p_steps);
        this.p_steps = p_steps;
        //TO DO: delete the line above and put your own initialization code here
    }

    /**
     * You may want to delete this method if you don't want to do any
     * finalization
     * @throws RemoteException If implemented, the RemoteException *MUST* be
     * thrown by this method
     */
    @Override
    public void endSimulation()
            throws RemoteException
    {
        Record record;
        double totalProfit = 0;
        for (int i = 1; i <= p_steps; i++){
            record = m_platformStub.query(PlayerType.LEADER, 100+i);

            totalProfit += calculateProfit(record.m_leaderPrice, record.m_followerPrice);
        }
        m_platformStub.log(PlayerType.LEADER, "profit: " + totalProfit);
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