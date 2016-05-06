import comp34120.ex2.Record;
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;

import java.util.ArrayList;

/**
 * Created by fiz on 07/05/2016.
 */
public class SquareRecursive {

    ArrayList<Record> historicalData;

    // constructor
    public SquareRecursive(ArrayList<Record> historicalData){
        this.historicalData = historicalData;
    }

    public void addData(Record record){
        historicalData.add(record);
    }

    // fogetting factor
    Float lambda = 0.97f;
    // prediction error
    FloatMatrix P = new FloatMatrix(3,3);
    // parameters
    FloatMatrix theta;


    public FloatMatrix baseCondition(){

        int dataSize = historicalData.size();

        for (int i = 0; i < dataSize; i++) {
            FloatMatrix phi = new FloatMatrix(3,1);

            float leaderPrice = historicalData.get(i).m_leaderPrice;
            phi.put(0,1);
            phi.put(1,leaderPrice);
            phi.put(2, leaderPrice*leaderPrice);

            // todo: datasize-1 part
            P = P.add(phi.mmul(phi.transpose()).mmul((float) Math.pow(lambda, dataSize-i)));
        }

        theta = new FloatMatrix(3,1);
//
//        DoubleMatrix thetaFollow = new DoubleMatrix(2,1);
//
//        for (int i = 0; i < dataSize; i++) {
//            DoubleMatrix phi = new DoubleMatrix(2,1);
//
//            phi.put(0, 1);
//            phi.put(1,historicalData.get(i).m_leaderPrice);
//
//            thetaFollow = thetaFollow.add(phi.mmul(historicalData.get(i).m_followerPrice).mmul(Math.pow(lambda, dataSize-i)));
//        }
//
//        double[][] forInverse = new double[2][2];
//
//        forInverse[0][0] = P.get(0);
//        forInverse[0][1] = P.get(1);
//        forInverse[1][0] = P.get(2);
//        forInverse[1][1] = P.get(3);
//
//        P.print();
//        forInverse = Inverse.invert(forInverse);
//        DoubleMatrix inverseOfP = new DoubleMatrix(forInverse);
//        inverseOfP.print();
//        theta = inverseOfP.mmul(thetaFollow);
//
        return theta;

    }

}
