import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;


public class Main extends JFrame implements ActionListener {
    // Define constants
    public static final int CANVAS_WIDTH  = 1270;
    public static final int CANVAS_HEIGHT = 980;
    public static final int viewDist = 900;
    public static final int populationSize = 200;

    public boolean W;
    public boolean A;
    public boolean D;
    public boolean S;
    public Timer timer;

    public Car car;
    public Car [] cars = new Car[populationSize];
    public Genome genomeInit;
    public Evaluator eval;
    public NeuralNetwork [] neuralNetworks = new NeuralNetwork[populationSize];
    public int genCount = 0;
    public int fittestIndex = 0;
    public int highestScore;
    private DrawCanvas canvas;
    public boolean debug = false;
    public Boundry [] boundries2 = new Boundry[] {

            new Boundry(100,100,200,100),
            new Boundry(100,100,100,850),
            new Boundry(200,100,200,650),

            new Boundry(100,850,1200,850),
            new Boundry(200,650,800,650),


            new Boundry(1200,850,1050,300),
            new Boundry(800,650,950,300),

            new Boundry(950,300,950,100),
            new Boundry(1050,300,1050,100),
    };
    public Boundry [] boundries = new Boundry[] {

            new Boundry(100,100,100,250),
            new Boundry(100,100,1000,100),
            new Boundry(100,250,950,250),

    //first turn
            new Boundry(1000,100,1248,348),
            new Boundry(950,250,1040,390),

    //closing in
            new Boundry(1248,348,980, 600),
            new Boundry(1040,390,768, 572),

    //open
            new Boundry(980, 600,1215, 878),
            new Boundry(768, 572,936, 790),

    //big corridor
            new Boundry(936,790,150,790),
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

    public Main() {
        //initalize the shit here

        if (debug) {
            car = new Car(90);
        } else {
            Counter nodeInn = new Counter();
            Counter connInn = new Counter();

            genomeInit = new Genome();
            int forwardNode = 0;
            for (int i = 0; i < Constants.SENSOR_COUNT; i++) {
                int n = nodeInn.getInnovation();
                if (i == Constants.SENSOR_COUNT / 2) forwardNode = n;
                genomeInit.nodes.put(n, new NodeGene(NodeGene.TYPE.INPUT, n));
            }
            if (Constants.WITH_W_S) {
                //W
                int n1 = nodeInn.getInnovation();
                genomeInit.nodes.put(n1, new NodeGene(NodeGene.TYPE.OUTPUT, n1));
                //S
                int n2 = nodeInn.getInnovation();
                genomeInit.nodes.put(n2, new NodeGene(NodeGene.TYPE.OUTPUT, n2));
            }
            //A
            int n3 = nodeInn.getInnovation();
            genomeInit.nodes.put(n3, new NodeGene(NodeGene.TYPE.OUTPUT, n3));
            //D
            int n4 = nodeInn.getInnovation();
            genomeInit.nodes.put(n4, new NodeGene(NodeGene.TYPE.OUTPUT, n4));

            int c1 = connInn.getInnovation();
            genomeInit.connections.put(c1, new ConnectionGene(forwardNode, n3, 0.0f, true, c1));

            eval = new Evaluator(populationSize, genomeInit, nodeInn, connInn) {
                @Override
                int evaluateGenome(Genome genome, int index) {
                    return cars[index].fitness;
                }
            };

            //setting up the cars and the neural networks
            for (int i = 0; i < populationSize; i++) {
                cars[i] = new Car();
                final int carIndex = i;
                neuralNetworks[i] = new NeuralNetwork(eval.genomes.get(carIndex)) {
                    @Override
                    public float[] getInput() {
                        return processInput(cars[carIndex].sensors);
                    }
                };
            }
        }
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
                if (keyCode == KeyEvent.VK_E) {
                    timer.setInitialDelay(Constants.FRAME_RATE_FAST);
                }
                if (keyCode == KeyEvent.VK_Q) {
                    timer.setInitialDelay(Constants.FRAME_RATE_SLOW);
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
        timer = new Timer(0, this);
        timer.setInitialDelay(Constants.FRAME_RATE_FAST);
        timer.start();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setTitle("Driving Neatly");
        setVisible(true);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        //get car.sensor info and pass them into the neural network to get output
        if (debug) {
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
                    car.level = 0;
                }
            }
        } else {
            highestScore = 0;
            W = A = S = D = false;
            float[] results;
            for (int i = 0; i < populationSize; i++) {
                if (highestScore < cars[i].fitness) {
                    fittestIndex = i;
                    highestScore = cars[i].fitness;
                }
                if (cars[i].crashed) continue;
                results = neuralNetworks[i].runCalculation();

                if (results[0] > Constants.ACTIVATION_THRESHOLD) {
                    cars[i].rotateWheel(10);
                    if (i == fittestIndex)
                        D = true;
                }
                if (results[1] > Constants.ACTIVATION_THRESHOLD) {
                    cars[i].rotateWheel(-10);
                    if (i == fittestIndex)
                        A = true;
                }
                if (Constants.WITH_W_S) {
                    if (results[0] > Constants.ACTIVATION_THRESHOLD) {
                        cars[i].addEnergy();
                        if (i == fittestIndex)
                            W = true;
                    }

                    if (results[3] > Constants.ACTIVATION_THRESHOLD) {
                        cars[i].removeEnergy();
                        if (i == fittestIndex)
                            S = true;
                    }
                } else {
                    cars[i].addEnergy();
                    if (i == fittestIndex)
                        W = true;
                }



                cars[i].move();
                calculateLevel(cars[i]);
                cars[i].fitness = calculateFitness(cars[i], cars[i].level);


                java.util.List<Coord> carBoundries = cars[i].getBounderies();
                for (Boundry boundry : boundries) {
                    if (boundry.checkCrash(carBoundries)) {
                        cars[i].crashed = true;
                    }
                }
                if (cars[i].checkInactive()) {
                    cars[i].crashed = true;
                }
            }
            canvas.repaint();

            boolean allDone = true;
            for (Car car : cars) {
                if (!car.crashed) {
                    allDone = false;
                }
            }
            if (allDone) {
                genCount++;
                if (genCount % 20 == 0)
                    GenomePrinter.printGenome(eval.fittestGenome, "D:\\storage\\MLTESTING\\" + genCount + ".png");
                eval.evaluate();
                int angle = 0;
                if (eval.passedPopulation == populationSize) {
                    angle = 90;
                    boundries = boundries2;
                    timer.setInitialDelay(Constants.FRAME_RATE_SLOW);
                }
                for (int i = 0; i < populationSize; i++) {
                    cars[i] = new Car(angle);

                    final int carIndex = i;
                    neuralNetworks[i] = new NeuralNetwork(eval.genomes.get(carIndex)) {
                        @Override
                        public float[] getInput() {
                            return processInput(cars[carIndex].sensors);
                        }
                    };
                }
            }
        }
        timer.restart();
    }

    private void calculateLevel(Car car) {
        if (car.x < 950 && car.y < 250) {
            //initial coor
            car.level = 0;
        } else if (car.level == 0 && car.y < 830 ) {
            //zigzag
            car.level = 1;
        } else if (car.level == 1 && car.y > 830) {
            //big coor
            car.level = 2;
        } else if (car.level == 2 && car.x < 150) {
            //up
            car.level = 3;
        } else if (car.level == 3 && car.y < 500) {
            //right narrow
            car.level = 4;
        } else if (car.level == 4 && car.x > 911) {
            car.level = 5;
            car.color = Color.GREEN;
        }
    }

    public int calculateFitness(Car car, int level) {
        int fitness = 0;
        if (level == 0) {
            return (int) car.x;
        } else if (level == 1) {
            return (int) (950 + (car.y - 175));
        } else if (level == 2) {
            return 950+550 + (int) (1150 - car.x);
        } else if (level == 3) {
            return (int) (950+550+1020+(835-car.y));
        } else if (level == 4 || level == 5) {
            return 950+550+1020+385 + (int) (car.x-80);
        }
        return fitness;
    }

    public class DrawCanvas extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.WHITE);

            if (debug) {
                paintCar(g, car);
                paintVision(g, car);
                g.setColor(Color.black);
                paintObstacles(g);


                g.drawString("X: " + car.x,10,10);
                g.drawString("Y: " + car.y,10,20);
                g.drawString("Speed: " + car.speed,10,30);
                g.drawString("Rotation: " + car.rotation,10,40);

                g.drawString("Fitness: " + calculateFitness(car, car.level),10,60);
                g.drawString("Phase: " + car.level,10,70);

                g.drawString("Sensor 1: " + car.sensors[0],1120,10);
                g.drawString("Sensor 2: " + car.sensors[1],1120,20);
                g.drawString("Sensor 3: " + car.sensors[2],1120,30);
                g.drawString("Sensor 4: " + car.sensors[3],1120,40);
                g.drawString("Sensor 5: " + car.sensors[4],1120,50);
            } else {
                for (int i = 0; i < populationSize; i++) {
                    if (i == fittestIndex) continue;
                    paintCar(g, cars[i]);
                    paintVision(g, cars[i]);
                }
                cars[fittestIndex].color = Color.GREEN;
                paintCar(g, cars[fittestIndex]);
                if (cars[fittestIndex].level != 5) cars[fittestIndex].color = Color.ORANGE;
                paintVision(g, cars[fittestIndex]);
                g.setColor(Color.black);
                paintObstacles(g);


                g.drawString("Generations: " + genCount, 500, 10);
                g.drawString("Highest fitness: " + eval.highestScore, 500, 20);
                g.drawString("Amount of Species: " + eval.species.size(), 500, 30);
                g.drawString("Solution found genomes: " + eval.passedPopulation, 500, 40);

                g.drawString("X: " + cars[fittestIndex].x, 10, 10);
                g.drawString("Y: " + cars[fittestIndex].y, 10, 20);
                g.drawString("Speed: " + cars[fittestIndex].speed, 10, 30);
                g.drawString("Rotation: " + cars[fittestIndex].rotation, 10, 40);

                g.drawString("Fitness: " + calculateFitness(cars[fittestIndex], cars[fittestIndex].level), 10, 60);
                g.drawString("Phase: " + cars[fittestIndex].level, 10, 70);
                g.drawString("Crashed: " + cars[fittestIndex].crashed, 10, 80);
                DecimalFormat df = new DecimalFormat("#.00");
                float [] processedInputs = processInput(cars[fittestIndex].sensors);
                g.drawString("Sensor 1: " + df.format(cars[fittestIndex].sensors[0]) + "  /  " + processedInputs[0], 1100, 10);
                g.drawString("Sensor 2: " + df.format(cars[fittestIndex].sensors[1]) + "  /  " + processedInputs[1], 1100, 30);
                g.drawString("Sensor 3: " + df.format(cars[fittestIndex].sensors[2]) + "  /  " + processedInputs[2], 1100, 50);
                g.drawString("Sensor 4: " + df.format(cars[fittestIndex].sensors[3]) + "  /  " + processedInputs[3], 1100, 70);
                g.drawString("Sensor 5: " + df.format(cars[fittestIndex].sensors[4]) + "  /  " + processedInputs[4], 1100, 90);

                for (int i = 0; i < (Constants.WITH_W_S? 4:2); i++) {
                    g.drawString("Output " + (i + 1) + ": " + neuralNetworks[fittestIndex].neurons.get(neuralNetworks[fittestIndex].output.get(i)).getOutput(), 800, 10 * i + 10);
                }
            }

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
            for (int i = (int) (-1*Constants.FOV/2); i <= Constants.FOV/2; i+=Constants.FOV/(Constants.SENSOR_COUNT-1)) {
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

                car.sensors[i/(Constants.FOV/(Constants.SENSOR_COUNT-1))+Constants.SENSOR_COUNT/2] = (float) Dist;
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



    // The entry main method
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main(); // Let the constructor do the job
            }
        });

/*
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
            int evaluateGenome(Genome genome, int index) {
                float weightSum = 0f;
                for (ConnectionGene cg : genome.connections.values()) {
                    if (cg.enabled) {
                        weightSum += Math.abs(cg.weight);
                    }
                }
                float difference = Math.abs(weightSum-100f);
                return (int) (1000/difference);
            }
        };
        for (int i = 1; i <= 300; i++) {
            eval.evaluate();
            System.out.print("Generations: " + i + "\tHighest fitness: " + eval.highestScore + "\tAmount of Species: " + eval.species.size());
            float weightSum = 0;
            for (ConnectionGene cg : eval.fittestGenome.connections.values()) {
                if (cg.enabled) {
                    weightSum += Math.abs(cg.weight);
                }
            }
            System.out.println("\tWeight sum: "+weightSum);
            if (i%30 == 0) {
                GenomePrinter.printGenome(eval.fittestGenome, "D:\\storage\\MLTESTING\\" + i + ".png");
            }
        }
        */
    }

    public static float [] processInput (float[] input) {
        float[] result = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = sigmoidActivationFunction((input[i]- 150) / 150);
        }
        return result;
    }

    public static float sigmoidActivationFunction(float in) {
        return (float)(1f/( 1f + Math.exp(-4.9d*in)));
    }
}