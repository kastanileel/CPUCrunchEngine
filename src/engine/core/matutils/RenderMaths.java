package src.engine.core.matutils;

import java.awt.geom.AffineTransform;

public class RenderMaths {

    public static float[][] identityMatrix() {
        float[][] erg = new float[4][4];
        erg[0][0] = 1.0f;
        erg[1][1] = 1.0f;
        erg[2][2] = 1.0f;
        erg[3][3] = 1.0f;
        return erg;
    }

    public static Vector3 multiplyMatrixVector(Vector3 i, float[][] m) {
        Vector3 v = new Vector3();

        v.x = i.x * m[0][0] + i.y * m[0][1] + i.z * m[0][2] + i.w * m[0][3];
        v.y = i.x * m[1][0] + i.y * m[1][1] + i.z * m[1][2] + i.w * m[1][3];
        v.z = i.x * m[2][0] + i.y * m[2][1] + i.z * m[2][2] + i.w * m[2][3];
        v.w = i.x * m[3][0] + i.y * m[3][1] + i.z * m[3][2] + i.w * m[3][3];

        return v;
    }

    public static float[][] multiplyMatrices(float[][] m1, float[][] m2){
        float[][] matrix = new float[4][4];
        for (int column = 0; column < 4; column++)
            for (int row = 0; row < 4; row++)
                matrix[row][column] = m1[row][0] * m2[0][column] + m1[row][1] * m2[1][column] + m1[row][2] * m2[2][column] + m1[row][3] * m2[3][column];
        return matrix;
    }

    public static float[][] makeRotationMatrix(float x, float y, float z){
        return  multiplyMatrices(matrixRotateY(y), multiplyMatrices(matrixRotateX(x), matrixRotateZ(z)));
    }

    public static Vector3 substractVectors(Vector3 v1, Vector3 v2) {
        return new Vector3(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    public static Vector3 addVectors(Vector3 v1, Vector3 v2) {
        return new Vector3(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }
    public static Vector3 multiplyVector(Vector3 v, float k) {
        return new Vector3(v.x * k, v.y * k, v.z * k);
    }

    public static float dotProduct(Vector3 v1, Vector3 v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public static float lengthVector(Vector3 v) {
        return (float) Math.sqrt((float) dotProduct(v, v));
    }

    public static Vector3 normalizeVector(Vector3 v) {
        float l = lengthVector(v);
        return new Vector3(v.x / l, v.y / l, v.z / l);
    }

    public static Vector3 crossProduct(Vector3 v1, Vector3 v2) {
        Vector3 erg = new Vector3();

        erg.x = v1.y * v2.z - v1.z * v2.y;
        erg.y = v1.z * v2.x - v1.x * v2.z;
        erg.z = v1.x * v2.y - v1.y * v2.x;

        return erg;
    }

    public static float[][] viewMatrix(Vector3 pos, Vector3 target, Vector3 up) {
        // Calculate new forward direction
        Vector3 newForward = substractVectors(target, pos);
        newForward = normalizeVector(newForward);

        // Calculate new Up direction
        Vector3 a = multiplyVector(newForward, dotProduct(up, newForward));
        Vector3 newUp = substractVectors(up, a);
        newUp = normalizeVector(newUp);

        // New Right direction is easy, its just cross product
        Vector3 newRight = crossProduct(newUp, newForward);

        // Construct Dimensioning and Translation Matrix
        float[][] matrix = new float[4][4];
        matrix[0][0] = newRight.x;
        matrix[1][0] = newRight.y;
        matrix[2][0] = newRight.z;

        matrix[0][1] = newUp.x;
        matrix[1][1] = newUp.y;
        matrix[2][1] = newUp.z;

        matrix[0][2] = newForward.x;
        matrix[1][2] = newForward.y;
        matrix[2][2] = newForward.z;

        matrix[0][3] = pos.x;
        matrix[1][3] = pos.y;
        matrix[2][3] = pos.z;
        matrix[3][3] = 1.0f;
        return matrix;

    }

    public static float[][] projectionMatrix(float fFovDeg, float fNear, float fFar, float fAspectRatio) {
        float fFovRad = (float) (1.0f / Math.tan(fFovDeg * 0.5 / 180.0f * Math.PI));
        float[][] m = new float[4][4];
        m[0][0] = fAspectRatio * fFovRad;
        m[1][1] = fFovRad;
        m[2][2] = fFar / (fFar - fNear);
        m[3][2] = (-fFar * fNear) / (fFar - fNear);
        m[2][3] = 1.0f;
        m[3][3] = 0.0f;

        return m;
    }

    public static Vector3 rotateVectorY(Vector3 vec, float angle){
        Vector3 erg = new Vector3();
        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);

        erg.x = vec.x * cosAngle - vec.z * sinAngle;
        erg.y = vec.y;
        erg.z = vec.x * sinAngle + vec.z * cosAngle;

        return erg;
    }

    public static float[][] inverseMatrix(float[][] m) {

        float[][] matrix = new float[4][4];
        matrix[0][0] = m[0][0];

        matrix [0][1] = m [1][0];
        matrix [1][0] = m [0][1];

        matrix [0][2] = m [2][0];
        matrix [2][0] = m [0][2];




        matrix [1][1] = m [1][1];
        matrix [1][2] = m [2][1];
        matrix [2][1] = m [1][2];
        matrix [2][2] = m [2][2];

        matrix [3][1] = 0.0f;
        matrix [3][0] = 0.0f;

        matrix [3][2] = 0.0f;
        matrix [0][3] = -(m [0][3] * matrix [0][0] + m [1][3] * matrix [0][1] + m [2][3] * matrix [0][2]);
        matrix [1][3] = -(m [0][3] * matrix [1][0] + m [1][3] * matrix [1][1] + m [2][3] * matrix [1][2]);
        matrix [2][3] = -(m [0][3] * matrix [2][0] + m [1][3] * matrix [2][1] + m [2][3] * matrix [2][2]);
        matrix [3][3] = 1.0f;
        return matrix;


    }

    public static float[][] matrixRotateX(float angle) {
        float[][] mRotX = new float[4][4];
        mRotX[1][1] = (float) Math.cos(angle );
        mRotX[1][2] = (float) -Math.sin(angle );
        mRotX[2][1] = (float) Math.sin(angle );
        mRotX[2][2] = (float) Math.cos(angle );
        mRotX[3][3] = 1.0f;
        mRotX[0][0] = 1.0f;

        return mRotX;
    }

    public static float[][] matrixRotateY(float angle) {
        float[][] mRotY = new float[4][4];
        mRotY[0][0] = (float) Math.cos(angle);
        mRotY[0][2] = (float) -Math.sin(angle);
        mRotY[2][0] = (float) Math.sin(angle);
        mRotY[1][1] = 1.0f;
        mRotY[2][2] = (float) Math.cos(angle);
        mRotY[3][3] = 1.0f;

        return mRotY;
    }

    public static float[][] matrixRotateZ(float angleRad) {
        float[][] matrix = new float[4][4];
        matrix[0][0] = (float) Math.cos(angleRad);
        matrix[0][1] = (float) -Math.sin(angleRad);
        matrix[1][0] = (float) Math.sin(angleRad);
        matrix[1][1] = (float) Math.cos(angleRad);
        matrix[2][2] = 1.0f;
        matrix[3][3] = 1.0f;
        return matrix;
    }

    public static float[][] makeTranslationMatrix(float x, float y, float z) {
        float[][] t = new float[4][4];
        t[0][0] = 1.0f;
        t[1][1] = 1.0f;
        t[2][2] = 1.0f;
        t[3][3] = 1.0f;
        t[0][3] = x;
        t[1][3] = y;
        t[2][3] = z;
        return t;
    }

    public static AffineTransform getAffineTransformation(Vector3 v1, Vector3 v2, Vector3 v3, Vector3 textureCoord1, Vector3 textureCoord2, Vector3 textureCoord3) {
        double[][] matrix = new double[3][3];

        // Build the matrix equation for the affine transformation
        matrix[0][0] = v1.x;
        matrix[0][1] = v1.y;
        matrix[0][2] = 1;
        matrix[1][0] = v2.x;
        matrix[1][1] = v2.y;
        matrix[1][2] = 1;
        matrix[2][0] = v3.x;
        matrix[2][1] = v3.y;
        matrix[2][2] = 1;

        double[] b = new double[3];
        b[0] = textureCoord1.x;
        b[1] = textureCoord2.x;
        b[2] = textureCoord3.x;

        // Solve the matrix equation for the coefficients of the affine transformation
        double[] x = solve(matrix, b);

        b[0] = textureCoord1.y;
        b[1] = textureCoord2.y;
        b[2] = textureCoord3.y;

        double[] y = solve(matrix, b);

        // Build the affine transformation matrix from the coefficients
        AffineTransform transform = new AffineTransform(x[0], y[0], x[1], y[1], x[2], y[2]);
        return transform;
    }


    /**
     * Solves a system of linear equations of the form Ax = b, where A is a square matrix and x and b are vectors.
     *
     * @param A     The matrix A.
     * @param b     The vector b.
     * @return      The vector x that solves the equation.
     */
    private static double[] solve(double[][] A, double[] b) {
        int n = A.length;

        // Create an augmented matrix [A|b]
        double[][] augmented = new double[n][n+1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                augmented[i][j] = A[i][j];
            }
            augmented[i][n] = b[i];
        }

        // Reduce the augmented matrix to row echelon form using Gaussian elimination
        for (int i = 0; i < n; i++) {
            int max = i;
            for (int j = i+1; j < n; j++) {
                if (Math.abs(augmented[j][i]) > Math.abs(augmented[max][i])) {
                    max = j;
                }
            }
            double[] temp = augmented[i];
            augmented[i] = augmented[max];
            augmented[max] = temp;

            for (int j = i+1; j <= n; j++) {
                augmented[i][j] /= augmented[i][i];
            }

            for (int j = 0; j < n; j++) {
                if (j != i) {
                    for (int k = i+1; k <= n; k++) {
                        augmented[j][k] -= augmented[j][i] * augmented[i][k];
                    }
                }
            }
        }

        // Extract the solution vector from the reduced row echelon form
        double[] x = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = augmented[i][n];
        }

        return x;
    }



    /**
     * Computes the barycentric coordinates of a point p with respect to a triangle defined by its vertices a, b, and c.
     *
     * @param a     The first vertex of the triangle.
     * @param b     The second vertex of the triangle.
     * @param c     The third vertex of the triangle.
     * @param p     The point for which to compute the barycentric coordinates.
     * @return      An array containing the barycentric coordinates of the point p with respect to the triangle.
     */
    public static Vector3 getBarycentricCoordinates(Vector3 a, Vector3 b, Vector3 c, Vector3 p) {
        // Compute the vectors from a to b and from a to c

        float baryA = (b.y - c.y) * (p.x - c.x) + (c.x - b.x) * (p.y - c.y);
        baryA = baryA / ((b.y - c.y) * (a.x - c.x) + (c.x - b.x) * (a.y - c.y));
        float baryB = (c.y - a.y) * (p.x - c.x) + (a.x - c.x) * (p.y - c.y);
        baryB = baryB / ((b.y - c.y) * (a.x - c.x) + (c.x - b.x) * (a.y - c.y));
        float baryC = 1.0f - baryA - baryB;


        return new Vector3(baryA, baryB, baryC);
    }




}
