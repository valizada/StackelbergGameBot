import comp34120.ex2.Record;
import org.jblas.FloatMatrix;

import java.util.ArrayList;

/**
 * Created by fiz on 08/05/2016.
 */

public class QuarticRecursive {

    ArrayList<Record> historicalData;

    // constructor
    public QuarticRecursive(ArrayList<Record> historicalData){
        this.historicalData = historicalData;
    }

    public void addData(Record record){
        historicalData.add(record);
    }

    // fogetting factor
    Float lambda = 0.93f;
    // prediction error
    FloatMatrix P = new FloatMatrix(5,5);
    // parameters
    FloatMatrix theta;

    public FloatMatrix baseCondition(){

        int dataSize = historicalData.size();

        for (int i = 0; i < dataSize; i++) {
            FloatMatrix phi = new FloatMatrix(5,1);

            float leaderPrice = historicalData.get(i).m_leaderPrice;
            phi.put(0,1);
            phi.put(1,leaderPrice);
            phi.put(2, leaderPrice*leaderPrice);
            phi.put(3,leaderPrice*leaderPrice*leaderPrice);
            phi.put(4,leaderPrice*leaderPrice*leaderPrice*leaderPrice);

            // todo: datasize-1 part
            P = P.add(phi.mmul(phi.transpose()).mmul((float) Math.pow(lambda, dataSize-i-1)));
        }

        theta = new FloatMatrix(5,1);

        FloatMatrix thetaFollow = new FloatMatrix(5,1);

        for (int i = 0; i < dataSize; i++) {
            FloatMatrix phi = new FloatMatrix(5,1);

            float leaderPrice = historicalData.get(i).m_leaderPrice;
            phi.put(0,1);
            phi.put(1,leaderPrice);
            phi.put(2, leaderPrice*leaderPrice);
            phi.put(3,leaderPrice*leaderPrice*leaderPrice);
            phi.put(4,leaderPrice*leaderPrice*leaderPrice*leaderPrice);

            thetaFollow = thetaFollow.add(phi.mmul(historicalData.get(i).m_followerPrice).mmul((float) Math.pow(lambda, dataSize-i-1)));
        }

        float[][] forInverse = new float[5][5];

        forInverse[0][0] = P.get(0);
        forInverse[0][1] = P.get(1);
        forInverse[0][2] = P.get(2);
        forInverse[0][3] = P.get(3);
        forInverse[0][4] = P.get(4);

        forInverse[1][0] = P.get(5);
        forInverse[1][1] = P.get(6);
        forInverse[1][2] = P.get(7);
        forInverse[1][3] = P.get(8);
        forInverse[1][4] = P.get(9);


        forInverse[2][0] = P.get(10);
        forInverse[2][1] = P.get(11);
        forInverse[2][2] = P.get(12);
        forInverse[2][3] = P.get(13);
        forInverse[2][4] = P.get(14);


        forInverse[3][0] = P.get(15);
        forInverse[3][1] = P.get(16);
        forInverse[3][2] = P.get(17);
        forInverse[3][3] = P.get(18);
        forInverse[3][4] = P.get(19);

        forInverse[4][0] = P.get(20);
        forInverse[4][1] = P.get(21);
        forInverse[4][2] = P.get(22);
        forInverse[4][3] = P.get(23);
        forInverse[4][4] = P.get(24);

//        P.print();
        forInverse = Inverse.invert(forInverse);
        FloatMatrix inverseOfP = new FloatMatrix(forInverse);
//        inverseOfP.print();
        theta = inverseOfP.mmul(thetaFollow);

        return theta;
    }

    FloatMatrix adjustingFactor;

    public FloatMatrix update(int date){
        FloatMatrix phi = new FloatMatrix(5,1);

        float leaderPrice = historicalData.get(date-1).m_leaderPrice;

        phi.put(0, 1);
        //todo: date -2 must be checked
        phi.put(1, leaderPrice);
        phi.put(2, leaderPrice*leaderPrice);
        phi.put(3,leaderPrice*leaderPrice*leaderPrice);
        phi.put(4,leaderPrice*leaderPrice*leaderPrice*leaderPrice);


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
