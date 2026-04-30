/* interface of all communication channels.
   root class of a DECORATOR.
 */
public interface CommChannel {

	// channels may be opened and closed multiple times. During each such cycle
	// multiple transmits and receives are allowed. Channels are bidirectional.
	State state(); // current state of the channel
	Result open(); // open channel (after creation or close)
	Result transmit(Datablock d); // send data over channel
	Result receive(Datablock d); // receive data from channel
	Result reset(); // try to clear a 'failed' state
	Result close(); // close channel (after use)
	void fail(); // signal a failure [internal operation]
}

