import comp34120.ex2.Record;
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;
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
    Float lambda = 0.97f;
    // prediction error
    FloatMatrix P = new FloatMatrix(2,2);
    // parameters
    FloatMatrix theta;

    public FloatMatrix baseCondition(){

        int dataSize = historicalData.size();

        for (int i = 0; i < dataSize; i++) {
            FloatMatrix phi = new FloatMatrix(2,1);
            int rows = phi.rows;
            int cols = phi.columns;

            phi.put(0,1);
            phi.put(1,historicalData.get(i).m_leaderPrice);

            // todo: datasize-1 part
            P = P.add(phi.mmul(phi.transpose()).mmul((float) Math.pow(lambda, dataSize-i-1)));
        }

        theta = new FloatMatrix(2,1);

        FloatMatrix thetaFollow = new FloatMatrix(2,1);

        for (int i = 0; i < dataSize; i++) {
            FloatMatrix phi = new FloatMatrix(2,1);

            phi.put(0, 1);
            phi.put(1,historicalData.get(i).m_leaderPrice);

            // todo: datasize-1 part
            thetaFollow = thetaFollow.add(phi.mmul(historicalData.get(i).m_followerPrice).mmul((float)Math.pow(lambda, dataSize-i-1)));
        }

        float[][] forInverse = new float[2][2];

        forInverse[0][0] = P.get(0);
        forInverse[0][1] = P.get(1);
        forInverse[1][0] = P.get(2);
        forInverse[1][1] = P.get(3);

        forInverse = Inverse.invert(forInverse);
        FloatMatrix inverseOfP = new FloatMatrix(forInverse);

        theta = inverseOfP.mmul(thetaFollow);

        return theta;
    }

    FloatMatrix adjustingFactor;

    public FloatMatrix update(int date){
        FloatMatrix phi = new FloatMatrix(2,1);

        phi.put(0, 1);
        //todo: date -2 must be checked
        phi.put(1, historicalData.get(date-1).m_leaderPrice);

        FloatMatrix adjustingFactorDeter = phi.transpose().mmul(P).mmul(phi).add(lambda);
        adjustingFactor = P.mmul(phi).div(adjustingFactorDeter);

        FloatMatrix Pnum, Pdenom;

        Pnum = P.mmul(phi).mmul(phi.transpose()).mmul(P);

        Pdenom = phi.transpose().mmul(P).mmul(phi).add(lambda);

        P = P.sub(Pnum.div(Pdenom)).mmul(1/lambda);

        FloatMatrix followerPrice = new FloatMatrix(1,1);
        //todo: date -2 must be checked
        followerPrice.put(0, historicalData.get(date-1).m_followerPrice);
        theta = theta.add(adjustingFactor.mmul((followerPrice.sub(phi.transpose().mmul(theta)))));
        return theta;
    }
}
