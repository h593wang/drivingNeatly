import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

//Created by hydrozoa

    public class GenomePrinter {

        public static void printGenome(Genome genome, String path) {
            Random r = new Random();
            HashMap<Integer, Point> nodeGenePositions = new HashMap<Integer, Point>();
            int nodeSize = 20;
            int connectionSizeBulb = 6;
            int imageSize = 512;

            BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);

            Graphics g = image.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, imageSize, imageSize);

            g.setColor(Color.BLUE);
            for (NodeGene gene : genome.nodes.values()) {
                if (gene.type == NodeGene.TYPE.INPUT) {
                    float x = ((float)gene.id/((float)countNodesByType(genome, NodeGene.TYPE.INPUT)+1f)) * imageSize;
                    float y = imageSize-nodeSize/2;
                    g.fillOval((int)(x-nodeSize/2), (int)(y-nodeSize/2), nodeSize, nodeSize);
                    nodeGenePositions.put(gene.id, new Point((int)x,(int)y));
                } else if (gene.type == NodeGene.TYPE.HIDDEN) {
                    int x = r.nextInt(imageSize-nodeSize*2)+nodeSize;
                    int y = r.nextInt(imageSize-nodeSize*3)+(int)(nodeSize*1.5f);
                    g.fillOval((int)(x-nodeSize/2), (int)(y-nodeSize/2), nodeSize, nodeSize);
                    nodeGenePositions.put(gene.id, new Point((int)x,(int)y));
                } else if (gene.type == NodeGene.TYPE.OUTPUT) {
                    int x = r.nextInt(imageSize-nodeSize*2)+nodeSize;
                    int y = nodeSize/2;
                    g.fillOval((int)(x-nodeSize/2), (int)(y-nodeSize/2), nodeSize, nodeSize);
                    nodeGenePositions.put(gene.id, new Point((int)x,(int)y));
                }
            }

            g.setColor(Color.BLACK);
            for (ConnectionGene gene : genome.connections.values()) {
                if (!gene.enabled) {
                    continue;
                }
                Point inNode = nodeGenePositions.get(gene.inNode);
                Point outNode = nodeGenePositions.get(gene.outNode);

                Point lineVector = new Point((int)((outNode.x - inNode.x) * 0.95f), (int)((outNode.y - inNode.y) * 0.95f));

                g.drawLine(inNode.x, inNode.y, inNode.x+lineVector.x, inNode.y+lineVector.y);
                g.fillRect(inNode.x+lineVector.x-connectionSizeBulb/2, inNode.y+lineVector.y-connectionSizeBulb/2, connectionSizeBulb, connectionSizeBulb);
                g.drawString(""+gene.weight, (int)(inNode.x+lineVector.x*0.25f+5), (int)(inNode.y+lineVector.y*0.25f));
            }

            g.setColor(Color.RED);
            for (NodeGene nodeGene : genome.nodes.values()) {
                Point p = nodeGenePositions.get(nodeGene.id);
                g.drawString(""+nodeGene.id, p.x, p.y);
            }


            try {
                ImageIO.write(image, "PNG", new File(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static int countNodesByType(Genome genome, NodeGene.TYPE type) {
            int c = 0;
            for (NodeGene node : genome.nodes.values()) {
                if (node.type == type) {
                    c++;
                }
            }
            return c;
        }

    }


