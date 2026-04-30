/* basic communication channel, can be decorated (is part of a DECORATOR)
 */
public class BareChannel implements CommChannel{

	private String partner;
	private Socket channel;
	private State channelState;

	public void fail(){

		channelState = State.FAILED;
	}

	public BareChannel(String partner){

		this.partner = partner;
		this.channel = null;
		channelState = State.CLOSED;
	}

	public State state(){

		return channelState;
	}

	public Result open(){

		if (channelState != State.CLOSED)
			return Result.IMPOSSIBLE;
		// not yet initialized
		if (channel == null) 
			channel = CommServices.getPartner(partner);
		// initialization failed
		if (channel == null) {

			channelState = State.FAILED;
			return Result.FAILURE;
		} 
		// initialization OK, now open:
		else {
			// open failed
			if (channel.open() < 0) {
				channelState = State.FAILED;
				return Result.FAILURE;
			} else {
				channelState = State.OPENED;
				return Result.OK;
			}
		}
	}

	public Result transmit(Datablock d) {

		if (channel.write(d) < 0) {

			channelState = State.FAILED;
			return Result.FAILURE;
		} else {
			return Result.OK;
		}
	}

	public Result receive(Datablock d) {

		if (channel.read(d) < 0) {
			channelState = State.FAILED;
			return Result.FAILURE;
		} else {
			return Result.OK;
		}
	}

	public Result reset() {
		switch (channelState) {
		case OPENED:
			return Result.OK;
		case CLOSED:
			return Result.IMPOSSIBLE;
		case FAILED: // recover from error if possible:
			if (channel != null) {
				channel.clear();
				channelState = State.OPENED;
				return Result.OK;
			} else {

				return Result.FAILURE; // open failed: recovery is impossible
			}

		}

		return null;
	}

	public Result close() {
		switch (channelState) {
		case OPENED:
		case FAILED:
			if (channel != null)
				channel.close();
			channelState = State.CLOSED;
			return Result.OK;
		case CLOSED:
			return Result.IMPOSSIBLE;
		}

		return null;
	}
}
