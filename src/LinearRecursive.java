import comp34120.ex2.Record;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;

import java.util.ArrayList;

/**
 * Created by fiz on 06/05/2016.
 */
public class LinearRecursive {
    ArrayList<Record> historicalData;

    // constructor
    public LinearRecursive(ArrayList<Record> historicalData){
        this.historicalData = historicalData;
    }

    public void addData(Record record){
        historicalData.add(record);
    }

    // fogetting factor
    Double lambda = 0.97;
    // prediction error
    DoubleMatrix P = new DoubleMatrix(2,2);
    // parameters
    DoubleMatrix theta;

    public DoubleMatrix baseCondition(){

        int dataSize = historicalData.size();

        for (int i = 0; i < dataSize; i++) {
            DoubleMatrix phi = new DoubleMatrix(2,1);
            int rows = phi.rows;
            int cols = phi.columns;

            phi.put(0,1);
            phi.put(1,historicalData.get(i).m_leaderPrice);

            // todo: datasize-1 part
            P = P.add(phi.mmul(phi.transpose()).mmul(Math.pow(lambda, dataSize-i)));
        }

        theta = new DoubleMatrix(2,1);

        DoubleMatrix thetaFollow = new DoubleMatrix(2,1);

        for (int i = 0; i < dataSize; i++) {
            DoubleMatrix phi = new DoubleMatrix(2,1);

            phi.put(0, 1);
            phi.put(1,historicalData.get(i).m_leaderPrice);

            thetaFollow = thetaFollow.add(phi.mmul(historicalData.get(i).m_followerPrice).mmul(Math.pow(lambda, dataSize-i)));
        }

        double[][] forInverse = new double[2][2];

        forInverse[0][0] = P.get(0);
        forInverse[0][1] = P.get(1);
        forInverse[1][0] = P.get(2);
        forInverse[1][1] = P.get(3);

        P.print();
        forInverse = Inverse.invert(forInverse);
        DoubleMatrix inverseOfP = new DoubleMatrix(forInverse);
        inverseOfP.print();
        theta = inverseOfP.mmul(thetaFollow);

        return theta;
    }

    DoubleMatrix adjustingFactor;

    public DoubleMatrix update(int date){
        DoubleMatrix phi = new DoubleMatrix(2,1);

        phi.put(0, 1);
        //todo: date -2 must be checked
        phi.put(1, historicalData.get(date-1).m_leaderPrice);

        DoubleMatrix adjustingFactorDeter = phi.transpose().mmul(P).mmul(phi).add(lambda);
        adjustingFactor = P.mmul(phi).div(adjustingFactorDeter);

        DoubleMatrix Pnum, Pdenom;

        Pnum = P.mmul(phi).mmul(phi.transpose()).mmul(P);

        Pdenom = phi.transpose().mmul(P).mmul(phi).add(lambda);

        P = P.sub(Pnum.div(Pdenom)).mmul(1/lambda);

        DoubleMatrix followerPrice = new DoubleMatrix(1,1);
        //todo: date -2 must be checked
        followerPrice.put(0, historicalData.get(date-1).m_followerPrice);
        theta = theta.add(adjustingFactor.mmul((followerPrice.sub(phi.transpose().mmul(theta)))));
        return theta;
    }
}
