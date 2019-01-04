import java.util.List;

public class Boundry {
    int xInit;
    int yInit;
    int xEnd;
    int yEnd;

    public Boundry(int x, int y, int x2, int y2) {
        this.xInit = x;
        this.yInit = y;
        this.xEnd = x2;
        this.yEnd = y2;
    }

    public boolean checkCrash(java.util.List<Coord> carBoundries) {
        int maxX = Math.max(xInit, xEnd);
        int minX = Math.min(xInit, xEnd);
        int maxY = Math.max(yInit, yEnd);
        int minY = Math.min(yInit, yEnd);


        if ((xEnd - xInit) == 0) {
            int negCount = 0;
            int posCount = 0;
            for (int i = 0; i < 4; i++) {
                Coord carPoint = carBoundries.get(i);
                if ( (carPoint.y > maxY || carPoint.y < minY) ) {
                    negCount++;
                    posCount++;
                    continue;
                }
                if (carBoundries.get(i).x >= xInit) {
                    posCount++;
                } else {
                    negCount++;
                }
            }
            if (negCount != 4 && posCount != 4) {
                return true;
            }
            return false;
        }
        double slope = ((double)(yEnd - yInit)) / (xEnd - xInit);
        double yint = yInit - slope * xInit;
        int negCount = 0;
        int posCount = 0;
        for (int i = 0; i < 4; i++) {
            Coord carPoint = carBoundries.get(i);
            if (carPoint.x > maxX || carPoint.x < minX && (carPoint.y > maxY || carPoint.y < minY)) {
                negCount++;
                posCount++;
                continue;
            }
            double expY = carPoint.x * slope + yint;
            if (carBoundries.get(i).y >= expY) {
                posCount++;
            } else {
                negCount++;
            }
        }
        if (negCount != 4 && posCount != 4) {
            return true;
        }
        return false;
    }

    public Coord getIntercept(double initX, double initY, double endX, double endY) {
        if (endX - initX == xEnd - xInit && endX - initX == 0) return null;
        if (endX - initX > -0.01 && endX - initX < 0.01) {
            //vertical sightLine
            if (initX < Math.max(xEnd, xInit) && initX > Math.min(xEnd,xInit)) {
                double boundrySlope = (yEnd - yInit)/(xEnd - xInit);
                double boundryB = yEnd - boundrySlope*xEnd;
                double y = initX * boundrySlope + boundryB;
                if (y < Math.min(yInit, yEnd) || y > Math.max(yInit,yEnd) || y < Math.min(initY, endY) || y > Math.max(initY,endY)) {
                    return null;
                }
                return new Coord(initX, y);
            }
            return null;
        }
        if (xEnd - xInit > -0.01 && xEnd - xInit < 0.01) {
            //vertical Boundry
            if (xInit < Math.max(endX, initX) && xInit > Math.min(initX,endX)) {
                double sightSlope = (endY - initY)/(endX - initX);
                double sightB = endY - sightSlope*endX;
                double y = xEnd * sightSlope + sightB;
                if (y < Math.min(yInit, yEnd) || y > Math.max(yInit,yEnd) || y < Math.min(initY, endY) || y > Math.max(initY,endY)) {
                    return null;
                }
                return new Coord(xEnd, y);
            }
            return null;
        }
        double boundrySlope = (double)(yEnd - yInit)/(xEnd - xInit);
        double sightSlope = (endY - initY)/(endX - initX);
        if ( boundrySlope == sightSlope) return null;
        double boundryB = yEnd - boundrySlope*xEnd;
        double sightB = endY - sightSlope*endX;

        double x = (sightB - boundryB) / (boundrySlope - sightSlope);
        if (x < Math.min(xInit, xEnd) || x > Math.max(xInit,xEnd) || x < Math.min(initX, endX) || x > Math.max(initX,endX)) {
            return null;
        }
        double y = x * sightSlope + sightB;
        return new Coord(x,y);
    }
}
