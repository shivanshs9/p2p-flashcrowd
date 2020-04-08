#Documentation

 * DHTProtocol
  * Properties
   * Each node has its own copy of this object
   * address - its location value
   *
  * Components
   * linkable - (Protocol) how nodes are connected
   * transport - the links the connect nodes (delay, latency, etc)
   * router - defines the mechanism used to route messages



  * DHTRouter
   * Components
    * loop - defines the mechanism to detect loops when routing
    * backtrack - defines if routing protocol can back track

  * DHTRouterGreedy::DHTRouter
   * Components
    * randomness - 0 -1