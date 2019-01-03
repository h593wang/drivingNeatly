import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;


public class Main extends JFrame implements ActionListener {
    // Define constants
    public static final int CANVAS_WIDTH  = 1270;
    public static final int CANVAS_HEIGHT = 980;
    public static final int viewDist = 300;

    public boolean W;
    public boolean A;
    public boolean D;
    public boolean S;
    public Timer timer;

    public Car car;
    private DrawCanvas canvas;
    public Boundry [] boundries = new Boundry[] {

            new Boundry(100,100,100,250),
            new Boundry(100,100,1000,100),
            new Boundry(100,250,950,250),

    //first turn
            new Boundry(1000,100,1248,348),
            new Boundry(950,250,1090,390),

    //closing in
            new Boundry(1248,348,1080, 640),
            new Boundry(1090,390,868, 572),

    //open
            new Boundry(1080, 640,1215, 878),
            new Boundry(868, 572,986, 790),

    //big corridor
            new Boundry(986,790,150,790),
            new Boundry(1215,878,721, 965),
            new Boundry(721, 965,20,878),

    //sharp up
            new Boundry(20,878,20,328),
            new Boundry(150,790, 150,540),
            new Boundry(150,540,220,468),

    //straight ahead
            new Boundry(20,328,520,328),
            new Boundry(220,468,518,468),

    //narrows
            new Boundry(520,328, 611,345),
            new Boundry(518,468, 594,402),

    //narrows
            new Boundry( 611,345,911, 345),
            new Boundry( 594,402, 911, 402),
    };

    public int level = 0;

    public Main() {
        car = new Car(110,175);
        canvas = new DrawCanvas();
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

        Container cp = getContentPane();
        cp.add(canvas);
        this.setFocusTraversalKeysEnabled(true);
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_W) {
                    W = true;
                }
                if (keyCode == KeyEvent.VK_D) {
                    D = true;
                }
                if (keyCode == KeyEvent.VK_A) {
                    A = true;
                }
                if (keyCode == KeyEvent.VK_S) {
                    S = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_W) {
                    W = false;
                }
                if (keyCode == KeyEvent.VK_D) {
                    D = false;
                }
                if (keyCode == KeyEvent.VK_A) {
                    A = false;
                }
                if (keyCode == KeyEvent.VK_S) {
                    S = false;
                }
            }
        });
        timer = new Timer(40, this);
        timer.setInitialDelay(40);
        timer.start();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setTitle("Driving Neatly");
        setVisible(true);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (W) {
            car.addEnergy();
        }
        if (D) {
            car.rotateWheel(10);
        }
        if (A) {
            car.rotateWheel(-10);
        }
        if (S) {
            car.removeEnergy();
        }
        car.move();
        calculateLevel(car);
        canvas.repaint();

        java.util.List<Coord> carBoundries = car.getBounderies();
        for (Boundry boundry: boundries) {
            if (boundry.checkCrash(carBoundries)) {
                car = new Car(110, 175);
                level = 0;
            }
        }
        timer.restart();
    }

    private void calculateLevel(Car car) {
        if (car.x < 950 && car.y < 250) {
            level = 0;
        } else if ( level == 0 && car.x > 950 && car.y < 348 ) {
            level = 1;
        } else if (level == 1 && car.y > 348) {
            level = 2;
        } else if (level == 2 && car.y > 730) {
            level = 3;
        } else if (level == 3 && car.x < 150) {
            level = 4;
        } else if (level == 4 && car.y < 500) {
            level = 5;
        }
    }

    public class DrawCanvas extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.WHITE);

            paintCar(g, car);
            paintVision(g, car);
            g.setColor(Color.black);
            paintObstacles(g);


            g.drawString("X: " + car.x,10,10);
            g.drawString("Y: " + car.y,10,20);
            g.drawString("Speed: " + car.speed,10,30);
            g.drawString("Rotation: " + car.rotation,10,40);

            g.drawString("Fitness: " + calculateFitness(car, level),10,60);
            g.drawString("Phase: " + level,10,70);

            g.drawString("Sensor 1: " + car.sensors[0],1120,10);
            g.drawString("Sensor 2: " + car.sensors[1],1120,20);
            g.drawString("Sensor 3: " + car.sensors[2],1120,30);
            g.drawString("Sensor 4: " + car.sensors[3],1120,40);
            g.drawString("Sensor 5: " + car.sensors[4],1120,50);
            if (W) g.fillRect(25,920,10,10);
            else g.drawRect(25,920,10,10);
            if (A) g.fillRect(10,935,10,10);
            else g.drawRect(10,935,10,10);
            if (S) g.fillRect(25,935,10,10);
            else g.drawRect(25,935,10,10);
            if (D) g.fillRect(40,935,10,10);
            else g.drawRect(40,935,10,10);
        }


        private void paintCar(Graphics g, Car car) {
            g.setColor(car.color);
            java.util.List<Coord> coords = car.getBounderies();
            int [] x = new int[4];
            int [] y = new int[4];
            for (int i = 0; i < 4 ; i++) {
                x[i] = (int) coords.get(i).x;
                y[i] = (int) coords.get(i).y;
            }
            g.fillPolygon(x, y, 4);
        }

        private void paintVision(Graphics g, Car car) {
            for (int i = -40; i <= 40; i+=20) {
                double endX =  (car.x + Math.cos(Math.toRadians(car.rotation + i)) * viewDist);
                double endY =  (car.y + Math.sin(Math.toRadians(car.rotation + i)) * viewDist);
                double drawX = -1;
                double drawY = -1;
                double Dist = viewDist;

                for (Boundry boundry : boundries) {
                    Coord intercept = boundry.getIntercept(car.x, car.y,endX, endY);
                    if (intercept != null) {
                        double Diff = (((intercept.x - car.x) * (intercept.x - car.x)) + ((intercept.y - car.y) * (intercept.y - car.y)));
                        double Cur =  (((drawX - car.x) * (drawX - car.x)) + ((drawY - car.y) * (drawY - car.y)));
                        if (Math.sqrt(Diff) > viewDist) continue;
                        if (drawX < 0) {
                            drawX =  intercept.x;
                            drawY =  intercept.y;
                            Dist = Math.sqrt(Diff);
                        } else if (Diff < Cur) {
                            drawX =  intercept.x;
                            drawY =  intercept.y;
                            Dist = Math.sqrt(Diff);
                        }
                    }
                }

                car.sensors[i/20+2] = Dist;
                if (drawX == -1) {
                    g.drawLine( (int)car.x,(int) car.y, (int) endX,(int) endY);
                } else {
                    g.drawLine((int) car.x,(int)  car.y,(int) drawX,(int) drawY);
                    //Dist, distance to wall
                    //drawX xPoint
                    //drawY yPoint
                }
            }
        }

        private void paintObstacles(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;

            for (Boundry boundry: boundries) {
                g2d.drawLine(boundry.xInit,boundry.yInit,boundry.xEnd,boundry.yEnd);

            }
        }

    }

    public int calculateFitness(Car car, int level) {
        int fitness = 0;
        if (level == 0) {
            return (int) car.x;
        } else if (level == 1) {
            return (int) (950 + ((car.y - 175) + (car.x - 950))/2);
        } else if (level == 2) {
            return 1140 + (int) (car.y - 348);
        } else if (level == 3) {
            return 1630 + (int) (1150 - car.x);
        } else if (level == 4) {
            return 2780 + (int) (835 - car.y);
        } else if (level == 5) {
            return 3100 + (int) (car.x - 20);
        }
        return fitness;
    }

    // The entry main method
    public static void main(String[] args) {
        /*
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main(); // Let the constructor do the job
            }
        });
        */

        Counter nodeInn = new Counter();
        Counter connInn = new Counter();

        Genome genome = new Genome();
        int n1 = nodeInn.getInnovation();
        int n2 = nodeInn.getInnovation();
        int n3 = nodeInn.getInnovation();
        genome.nodes.put(n1, new NodeGene(NodeGene.TYPE.INPUT, n1));
        genome.nodes.put(n2, new NodeGene(NodeGene.TYPE.INPUT, n2));
        genome.nodes.put(n3, new NodeGene(NodeGene.TYPE.OUTPUT, n3));

        int c1 = connInn.getInnovation();
        int c2 = connInn.getInnovation();
        genome.connections.put(c1, new ConnectionGene(n1, n3, 0.5f, true, c1));
        genome.connections.put(c2, new ConnectionGene(n2, n3, 0.5f, true, c2));

        Evaluator eval = new Evaluator(100, genome, nodeInn, connInn) {
            @Override
            int evaluateGenome(Genome genome) {
                return genome.connections.values().size();
            }
        };
        for (int i = 1; i <= 300; i++) {
            eval.evaluate();
            System.out.println("Generations: " + i + "\tHighest fitness: " + eval.highestScore + "\tAmount of Species: " + eval.species.size());
            if (i%30 == 0) {
                GenomePrinter.printGenome(eval.fittestGenome, "D:\\storage\\MLTESTING\\" + i + ".png");
            }
        }
    }
}