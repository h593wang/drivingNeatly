# Driving NEATly


Driving NEATly is an AI enabled driving simulation with simple self driving capabilities enabled by the NEAT algorithm.
The machine learning components includes:
  - Species feature and control to protect innovation
  - Mutation rate and genome specifications are adjustable
  - Swing based UI to monitor driving and view debug info


### Usage
Running the program will initialize the NEAT algorithm.
If debug mode is set to true, WASD controls the player car. If debug mode is disabled, E & Q control the framerate

The changeable parameters are included in Constants.java:
- C1, C2, C3 are the weights for excess genes, disjoint genes, and average weight difference for compatibility distance
- DT is the maximum compatibility distance for two genes to be the same species
- Mutation rate is the rate a gene's weight will mutate
- Add connection rate is the rate new connections forms between existing nodes
- Add node rate is the rate new nodes will be injected into the gene
- Max stagnation is the maximum generation count before a non-progressing species is killed off
- Stagnation decay is the percent decrease in score for each generation a non-progressing species suffers.
- Probability perturbing is the probability a mutated weight will resemble the original value
- Inactive count is the frame before an inactive car is declared crashed
- Activation threshold is the output needed for the action to activate (0-1)
- Frame rate fast and Frame rate slow are the fast and slow delays between frames
- FOV is the degree radius a car can see
- Sensor count is the number of equally spaces sensors each car will have
- WITH W S controls weather the genes will have access to the forward and back controls
- Max fitness is the max fitness before a gene is considered to have found a solution

### Resources Used

- [Implementing NEAT algorithm in java] by Hydrozoa, used as a tutorial for implementation
- [Evolving Neural Networks through Augmenting Topologies] by Kenneth O. Stanley & Risto Miikkulainen 
published in The MIT Press Journals, used as an algorithm explenation and analysis
- [MarI/O - Machine Learning for Video Games] by Sethbling for inspiration and introduction to the topic
- [But what *is* a Neural Network? | Deep learning] by 3Brown1Blue for topic explanation
- [Deep Learning Cars] by Samuel Arzt for project inspiration
- [Various videos] by Code Bullet for inspiration 


[//]: #

   [Implementing NEAT algorithm in java]: <https://www.youtube.com/watch?v=1I1eG-WLLrY>
   [Evolving Neural Networks through Augmenting Topologies]: <http://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf>
   [MarI/O - Machine Learning for Video Games]: https://www.youtube.com/watch?v=qv6UVOQ0F44
   [But what *is* a Neural Network? | Deep learning]: https://www.youtube.com/watch?v=aircAruvnKk
   [Deep Learning Cars]: https://www.youtube.com/watch?v=Aut32pR5PQA
   [Various videos]: https://www.youtube.com/channel/UC0e3QhIYukixgh5VVpKHH9Q/featured
