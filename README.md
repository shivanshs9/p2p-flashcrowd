# Flash Crowd & Churn Handling in P2P Live Streaming

### Abstract
Peer-to-Peer Network is a popular distributed application architecture which has
greatly enhanced the sharing and streaming experience with minimal or zero central
control required. However, the practical usage of such networks is subject to several
challenges due to phenomena like the Flash-crowd and Churning. Flash-crowd in
simple terms can be described as the arrival of thousands of peers in a very short
span of time. This phenomenon causes competition among incoming peers for limited
initial resources, popularly known as the race condition. Churning is defined as the
sudden leaving of a random subset of nodes from the overlay.

In this report, we first introduce an efficient Multi-tree based distributed algorithm for handling the Flash-crowd situation in Peer-to-peer Systems. The algorithm
organizes the newly arriving peers into hierarchical positions or ranks which are used
to create different sub-stream trees with data flowing from top peers to bottom ones.

After that, we propose an efficient distributed algorithm to mitigate churning by converting our multi-tree overlay to an hybrid topology. These task are performed using
PeerSim, a Java based framework for simulating P2P environments.

### Tech Stack
1. Kotlin
2. [Peersim](https://peersim.sourceforge.net/) for P2P simulation
3. Kademlia multi-tree DHT algo implementation ( https://github.com/shivanshs9/peersim-kademlia )

### Submissions
Link to Report: [Report](https://drive.google.com/file/d/1IB-H335HqRN1bk-NulC7evqRTkrl6pp2/view?usp=sharing)

We proposed a novel flashcrowd protocol on top of the Kademlia DHT (distributed data-store) algorithm, using a multi-tree mesh overlay and tracking fertile and sterile streaming nodes. However, this mesh design is vulnerable to effects of churning (i. e. large, random subset of nodes leaving the network at the same time). To mitigate that, we proposed a Hybrid topology based on whether a new node joined in the flashcrowd phase or the swarming phase.

### Results
![image](https://github.com/shivanshs9/p2p-flashcrowd/assets/8217199/7789c703-e5e9-4d5a-aa7a-ad027821b9c8)

![image](https://github.com/shivanshs9/p2p-flashcrowd/assets/8217199/db5ebb05-0a6e-4030-9473-18e1f591be9b)

Please refer to the report for more details.

### Team:
1. @shivanshs9
2. [@raghav-blacklist1](https://github.com/raghav-blacklist1)
