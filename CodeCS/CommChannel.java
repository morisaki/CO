/* class implementing a basic bidirectional communication channel with
   optional compression, encryption, and logging.
 */

public class CommChannel {


	// general:
	private State channelState;
	// basic channel:
	private String partner;
	private Socket channel; // the actual basic channel object
	// compression:
	private boolean doCompression;
	// encryption:
	private boolean doEncryption;
	private EncryptionStream encrypt; // encryption state machine for sending
	private DecryptionStream decrypt; // decryption state machine for receiving
	// logging:
	private boolean doLogging;
	private String logname; // name of this channel in the log file
	private String log; // the log "file"

	// open the basic channel
	Result basicOpen(){

		if (channelState != State.CLOSED)
			return Result.IMPOSSIBLE;

		// not yet initialized
		if (channel == null) 
			channel = CommServices.getPartner(partner);

		// initialization failed
		if (channel == null){

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

	//channels may be opened and closed multiple times. During each such cycle
	//multiple transmits and receives are allowed. Channels are bidirectional.

	// create a basic communication channel (no extra functionality):
	public CommChannel(String partner, boolean compress, String key, String logchannelname){
		// general:
		channelState = State.CLOSED;
		// basic channel:
		this.partner = partner;
		// compression:
		doCompression = compress;
		// encryption:
		doEncryption = (key != null);
		if (doEncryption){

			encrypt = new EncryptionStream(key);
			decrypt = new DecryptionStream(key);
		}
		// logging:
		doLogging = (logchannelname != null);
		logname = logchannelname;
	}

	public Result open(){

		Result result = basicOpen();
		// encryption, compression: 
		//   nothing to do
		// logging:
		if (doLogging){

			log += (logname + ": open " + result + " " + channelState + "\n");
		}

		return result;
	}

	public Result transmit(Datablock d){

		Result result;
		Datablock compressed;
		Datablock encrypted;
		// compression:
		if (doCompression){

			compressed = new Datablock((int)(0.8 * d.size()));
			Compressor.compress(d, compressed);
		} else {
			// do not compress, but do 'as if'
			compressed = d; 
		}

		// encryption:
		if (doEncryption){

			encrypted = new Datablock(encrypt.encryptedsize(d.size()));
			encrypt.encrypt(compressed, encrypted);
		} else {
			// do not encrypt, but do 'as if'
			encrypted = compressed; 
		}

		// basic channel:
		if (channel.write(encrypted) < 0) {
			channelState = State.FAILED;
			result = Result.FAILURE;
		} else {
			result = Result.OK;
		}

		// logging:
		if (doLogging){

			log += (logname + ": T " + encrypted.size() + " " + encrypted.fingerprint() + " " + result + "\n");
		}

		// cleanup:
		if (doCompression)
			(compressed) = null;
		if (doEncryption)
			(encrypted) = null;
		return result;

	}


	public Result receive(Datablock d){

		Result result;
		Datablock encrypted;
		Datablock compressed;
		// compression:
		if (doCompression)
			compressed = new Datablock();
		else
			compressed = d;
		// encryption:
		if (doEncryption)
			encrypted = new Datablock();
		else
			encrypted = compressed;
		// basic channel:
		if (channel.read(encrypted) < 0){

			channelState = State.FAILED;
			result = Result.FAILURE;
		} else {
			result = Result.OK;
		}
		// logging:
		if (doLogging) {
			log += (logname + ": R " + encrypted.size() + " " + encrypted.fingerprint() + " " + result + "\n");
		}
		// encryption:
		if (doEncryption) {
			decrypt.decrypt(encrypted, compressed);
			(encrypted) = null;
		}
		// compression:
		if (doCompression) {

			int r2 = Compressor.decompress(compressed, d);
			(compressed) = null;
			if (r2 < 0) {
				channelState = State.FAILED;
				result = Result.FAILURE;
			}
		}
		return result;
	}


	public Result reset() {

		State oldstate = channelState;
		Result result = null;
		switch (channelState) {
			case OPENED:
				result = Result.OK;
				break;
			case CLOSED:
				result = Result.IMPOSSIBLE;
				break;
			case FAILED: 
				// recover from error if possible:
				// encryption:
				if (doEncryption) {
					// cannot reset an encrypted connection, because losing data
					// invalidates the internal state of the encryption:
					result = Result.IMPOSSIBLE;
				}
				// basic channel:
				else if (channel != null){
					channel.clear();
					channelState = State.OPENED;
					result = Result.OK;
				} else {
					// open failed: recovery is impossible
					result = Result.FAILURE;
				}
		}
		// logging:
		if (doLogging) {
			log += (logname + ": reset " + result + " " + oldstate + "\n");
		}

		return result;
	}

	public Result close() {
		Result result = null;
		State oldstate = channelState;
		switch (channelState){
			case OPENED:
			case FAILED:
				// basic channel:
				if (channel != null)
					channel.close();
				// encryption:
				if (doEncryption) {
					encrypt.reset(); // set back to initial state for next connection
					decrypt.reset(); // set back to initial state for next connection
				}
				channelState = State.CLOSED;
				result = Result.OK;
				break;
			case CLOSED:
				result = Result.IMPOSSIBLE;
		}

		// logging:
		if (doLogging) {
			log += (logname + ": close " + result + " " + oldstate + "\n");
		}

		return result;
	}
}
