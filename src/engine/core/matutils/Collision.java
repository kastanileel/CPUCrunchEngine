package src.engine.core.matutils;

import src.engine.core.dataContainers.BoundingBox;
import src.engine.core.gamemanagement.GameComponents;

import java.util.Arrays;

public class Collision {
    public static BoundingBox createBoundingBox(GameComponents.Collider collider, Quaternion rotation) {
        Vector3 min, max;

        switch (collider.colliderType) {
            case BOX:
                Vector3[] corners = getRotatedBoxCorners(collider.center, collider.colliderSize, rotation);
                min = calculateMin(corners);
                max = calculateMax(corners);
                break;
            case SPHERE:
                float maxExtent = Math.max(collider.colliderSize.x, Math.max(collider.colliderSize.y, collider.colliderSize.z)) / 2;
                Vector3 size = new Vector3(maxExtent, maxExtent, maxExtent);
                min = collider.center.subtract(size);
                max = collider.center.add(size);
                break;
            default:
                throw new IllegalArgumentException("Unsupported collider type");
        }

        return new BoundingBox(min, max);
    }

    public static Vector3[] getRotatedBoxCorners(Vector3 center, Vector3 size, Quaternion rotation) {
        Vector3 halfSize = size.divide(2);
        Vector3[] corners = new Vector3[]{
                center.add(new Vector3(-halfSize.x, -halfSize.y, -halfSize.z)),
                center.add(new Vector3(halfSize.x, -halfSize.y, -halfSize.z)),
                center.add(new Vector3(-halfSize.x, halfSize.y, -halfSize.z)),
                center.add(new Vector3(halfSize.x, halfSize.y, -halfSize.z)),
                center.add(new Vector3(-halfSize.x, -halfSize.y, halfSize.z)),
                center.add(new Vector3(halfSize.x, -halfSize.y, halfSize.z)),
                center.add(new Vector3(-halfSize.x, halfSize.y, halfSize.z)),
                center.add(new Vector3(halfSize.x, halfSize.y, halfSize.z))
        };

        Matrix3 rotationMatrix = quaternionToRotationMatrix(rotation);

        return Arrays.stream(corners)
                .map(rotationMatrix::multiply)
                .toArray(Vector3[]::new);
    }

    public static Matrix3 quaternionToRotationMatrix(Quaternion quaternion) {
        float w = quaternion.w;
        float x = quaternion.x;
        float y = quaternion.y;
        float z = quaternion.z;

        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float xy = x * y;
        float xz = x * z;
        float yz = y * z;
        float wx = w * x;
        float wy = w * y;
        float wz = w * z;

        return new Matrix3(
                new Vector3(1 - 2 * (yy + zz),     2 * (xy - wz),     2 * (xz + wy)),
                new Vector3(    2 * (xy + wz), 1 - 2 * (xx + zz),     2 * (yz - wx)),
                new Vector3(    2 * (xz - wy),     2 * (yz + wx), 1 - 2 * (xx + yy))
        );
    }

    public static Vector3 calculateMin(Vector3[] corners) {
        Vector3 min = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        for (Vector3 corner : corners) {
            min.x = Math.min(min.x, corner.x);
            min.y = Math.min(min.y, corner.y);
            min.z = Math.min(min.z, corner.z);
        }
        return min;
    }

    public static Vector3 calculateMax(Vector3[] corners) {
        Vector3 max = new Vector3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        for (Vector3 corner : corners) {
            max.x = Math.max(max.x, corner.x);
            max.y = Math.max(max.y, corner.y);
            max.z = Math.max(max.z, corner.z);
        }
        return max;
    }


    public static boolean checkCollision(BoundingBox box1, BoundingBox box2) {
        return (box1.min.x <= box2.max.x && box1.max.x >= box2.min.x) &&
                (box1.min.y <= box2.max.y && box1.max.y >= box2.min.y) &&
                (box1.min.z <= box2.max.z && box1.max.z >= box2.min.z);
    }
}
