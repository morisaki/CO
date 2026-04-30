/* abstract decorator base class (part of a DECORATOR)
 */
public abstract class CommLayer implements CommChannel{

	protected CommChannel baseChannel; // the channel underlying this one

	public void fail(){

		baseChannel.fail();
	}

	public State state(){

		return baseChannel.state();
	}

	public abstract Result open();
	public abstract Result transmit(Datablock d);
	public abstract Result receive(Datablock d);
	public abstract Result reset();
	public abstract Result close();
}
