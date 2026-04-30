/* a DECORATOR class for event logging
 */
public class LoggedChannel extends CommLayer{

	private String name; // name of this channel in the log file
	private String log; // the log file

	public LoggedChannel(String name, CommChannel channel){

		this.name = name;
		baseChannel = channel;
	}


	public Result open(){

		Result r = baseChannel.open();
		log += (name + ": open " + r + " " + baseChannel.state() + "\n");
		return r;
	}

	public Result transmit(Datablock d){

		Result r = baseChannel.transmit(d);
		log += (name + ": T " + d.size() + " " + d.fingerprint() + " " + r + "\n");
		return r;
	}

	public Result receive(Datablock d){

		Result r = baseChannel.receive(d);
		log += (name + ": R " + d.size() + " " + d.fingerprint() + " " + r + "\n");
		return r;
	}

	public Result reset(){

		State oldstate = baseChannel.state();
		Result r = baseChannel.reset();
		log += (name + ": reset " + r + " " + oldstate + "\n");
		return r;
	}

	public Result close(){

		State oldstate = baseChannel.state();
		Result r = baseChannel.close();
		log += (name + ": close " + r + " " + oldstate + "\n");
		return r;
	}
}
