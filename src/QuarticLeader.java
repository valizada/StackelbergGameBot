
import comp34120.ex2.PlayerImpl;
import comp34120.ex2.PlayerType;
import comp34120.ex2.Record;
import org.jblas.FloatMatrix;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fiz on 07/05/2016.
 */

final class QuarticLeader
        extends PlayerImpl {
    ArrayList<Record> historicalData;

    private QuarticLeader()
            throws RemoteException, NotBoundException {
        super(PlayerType.LEADER, "Quadratic Leader");
    }

    @Override
    public void goodbye()
            throws RemoteException {
        ExitTask.exit(500);
    }

    /**
     * To inform this instance to proceed to a new simulation day
     *
     * @param p_date The date of the new day
     * @throws RemoteException
     */
    @Override
    public void proceedNewDay(int p_date)
            throws RemoteException {

        if (p_date == 101) {
            m_platformStub.publishPrice(m_type, maximiseRecursive(theta));
//            System.out.println("a: " + theta.get(0) + " b: " + theta.get(1));
        } else {
            quarticRecursive.addData(m_platformStub.query(PlayerType.LEADER, p_date - 1));
            theta = quarticRecursive.update(p_date - 1);
            m_platformStub.publishPrice(m_type, maximiseRecursive(theta));
        }

        // code for only 100 days
//		m_platformStub.publishPrice(m_type, (float)maximise(p_date));
//      m_platformStub.log(m_type, "current date: " + p_date);
    }


    QuarticRecursive quarticRecursive;
    FloatMatrix theta;
    int p_steps = 0;

    /**
     * You may want to delete this method if you don't want to do any
     * initialization
     *
     * @param p_steps Indicates how many steps will the simulation perform
     * @throws RemoteException If implemented, the RemoteException *MUST* be
     *                         thrown by this method
     */
    @Override
    public void startSimulation(int p_steps)
            throws RemoteException {
        super.startSimulation(p_steps);
        this.p_steps = p_steps;

        historicalData = new ArrayList<>();

        for (int i = 1; i <= 100; i++)
            historicalData.add(m_platformStub.query(PlayerType.LEADER, i));

        quarticRecursive = new QuarticRecursive(historicalData);
        theta = quarticRecursive.baseCondition();
    }

    /**
     * You may want to delete this method if you don't want to do any
     * finalization
     *
     * @throws RemoteException If implemented, the RemoteException *MUST* be
     *                         thrown by this method
     */
    @Override
    public void endSimulation()
            throws RemoteException {
        Record record;
        float totalProfit = 0;

        for (int i = 1; i <= p_steps; i++) {
            record = m_platformStub.query(PlayerType.LEADER, 100 + i);

            totalProfit += calculateProfit(record.m_leaderPrice, record.m_followerPrice);
        }
        m_platformStub.log(PlayerType.LEADER, "profit: " + totalProfit);
    }

    public float maximiseRecursive(FloatMatrix theta) throws RemoteException {

        int count = 0;
        float max = 0.000f;
        float currentUl = 0.000f;
        for (float i = 1.001f; i <= 3.500f; i = i + 0.001f) {

            // profit = (Ul - 1) * (2 - Ul + 0.3*Uf)
            // Uf = a + b * Ul + c * Ul*Ul

            float profit = (float) ((i - 1) * (2 - i + 0.3 * (theta.get(0) + theta.get(1) * i + theta.get(2) * i * i
                                                              + theta.get(3) * i * i * i + theta.get(4) * i * i * i * i)));

            if (profit > max) {
                max = profit;
                currentUl = i;
//                System.out.println("current best: " + currentUl);
            }
            count++;
        }

//        System.out.println("looped " + count + " times");
        System.out.println();
        return currentUl;
    }

    public double calculateProfit(float Ul, float Uf) {
        return (Ul - 1) * (2 - Ul + 0.3 * Uf);
    }

    public static void main(final String[] p_args)
            throws RemoteException, NotBoundException {
        new QuarticLeader();
    }

    /**
     * The task used to automatically exit the leader process
     *
     * @author Xin
     */
    private static class ExitTask
            extends TimerTask {
        static void exit(final long p_delay) {
            (new Timer()).schedule(new ExitTask(), p_delay);
        }

        @Override
        public void run() {
            System.exit(0);
        }
    }
}
