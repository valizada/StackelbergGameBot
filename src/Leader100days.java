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
 * Created by fiz on 08/05/2016.
 */
final class Leader100days
        extends PlayerImpl {
    ArrayList<Record> historicalData;

    private Leader100days()
            throws RemoteException, NotBoundException {
        super(PlayerType.LEADER, "Our Leader");
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

//        if (p_date == 101) {
//            m_platformStub.publishPrice(m_type, (float) maximiseRecursive(theta));
//            System.out.println("a: " + theta.get(0) + " b: " + theta.get(1));
//        } else {
//            linearRecursive.addData(m_platformStub.query(PlayerType.LEADER, p_date - 1));
//            theta = linearRecursive.update(p_date - 1);
//            m_platformStub.publishPrice(m_type, (float) maximiseRecursive(theta));
//        }

//         code for only 100 days
        m_platformStub.publishPrice(m_type, (float) maximise(p_date));
        m_platformStub.log(m_type, "current date: " + p_date);
    }


    LinearRecursive linearRecursive;
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

        linearRecursive = new LinearRecursive(historicalData);
        theta = linearRecursive.baseCondition();
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

    public float[] calculateAB100() throws RemoteException {
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
        float a = (sumUlSquare * sumUf - sumUl * sumOfUlUf) / (time * sumUlSquare - (sumUl * sumUl));
        float b = (time * sumOfUlUf - sumUl * sumUf) / (time * sumUlSquare - (sumUl * sumUl));

        return new float[]{a, b};
    }


    public float[] calculateABWindow(int window, int currentDate) throws RemoteException {
        Record record;
        float leaderPrice = 0;
        int time = 100;
        int currentStep = currentDate - 100;
        float followerPrice = 0, sumUl = 0, sumUf = 0, sumUlSquare = 0, sumOfUlUf = 0;

        int startDate = time - (window - currentStep);
        int endDate = time + currentStep;
        m_platformStub.log(m_type, "current date: " + startDate + " end date: " + endDate);

        for (int i = startDate; i < endDate; i++) {
            record = m_platformStub.query(PlayerType.LEADER, i);
            leaderPrice = record.m_leaderPrice;
            followerPrice = record.m_followerPrice;
            sumUl += leaderPrice;
            sumUf += followerPrice;
            sumUlSquare += leaderPrice * leaderPrice;
            sumOfUlUf += leaderPrice * followerPrice;
        }
        float a = (sumUlSquare * sumUf - sumUl * sumOfUlUf) / (time * sumUlSquare - (sumUl * sumUl));
        float b = (time * sumOfUlUf - sumUl * sumUf) / (time * sumUlSquare - (sumUl * sumUl));

        return new float[]{a, b};
    }

    public double maximise(int currentDate) throws RemoteException {
        // todo: if 100, make it global, do not call many times.
        float[] ab = calculateAB100();

        System.out.println("a100: " + ab[0] + " b100: " + ab[1]);
        // With window
//        float[] ab = calculateABWindow(100, currentDate);
        // m_platformStub.log(PlayerType.LEADER, "a: " + ab[0] + " b: " + ab[1]);

        return (3 + 0.3 * (ab[0] - ab[1])) / (2 - 0.6 * ab[1]);
    }


    public double maximiseRecursive(FloatMatrix theta) throws RemoteException {

        return (3 + 0.3 * (theta.get(0) - theta.get(1))) / (2 - 0.6 * theta.get(1));
    }

    public double calculateProfit(float Ul, float Uf) {
        return (Ul - 1) * (2 - Ul + 0.3 * Uf);
    }

    public static void main(final String[] p_args)
            throws RemoteException, NotBoundException {
        new Leader100days();
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
